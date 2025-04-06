package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.Repository.ApplicationRepository;
import com.esprit.spring.PiProject.Repository.OfferRepository;
import com.esprit.spring.PiProject.entities.Application;
import com.esprit.spring.PiProject.entities.Comment;
import com.esprit.spring.PiProject.entities.Offer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.apache.tomcat.util.http.FastHttpDateFormat.formatDate;

@Service
@AllArgsConstructor
@Slf4j

public class OfferService implements IOfferService {
    @Autowired
    OfferRepository offerRepository;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    EmailService emailService;

    @Override
    public Offer AddOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    @Override
    public Offer EditOffer(Long id, Offer offer) {
        Offer existingOffer = offerRepository.findById(id).orElse(null);
        if (existingOffer != null) {
            existingOffer.setTitle(offer.getTitle());
            existingOffer.setDescription(offer.getDescription());
            existingOffer.setImage(offer.getImage());
            existingOffer.setDatePub(offer.getDatePub());
            existingOffer.setDateExp(offer.getDateExp());
            existingOffer.setCategory(offer.getCategory());
            return offerRepository.save(existingOffer);
        }
        return null;
    }


    @Override
    public void DeleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

    @Override
    public List<Offer> GetAllOffers() {
        return offerRepository.findAll();
    }

    @Override
    public int NumberOfComment(Long id) {
        Offer offer = offerRepository.findById(id).get();
        return offer.getComments().size();
    }

    @Override
    public int NumberOfOffers() {
        return offerRepository.findAll().size();
    }

    //add auto delete every day at midnight
    @Scheduled(cron = "0 0 0 * * ?") //every day at midnight
    public void AutoDeleteOffer() {
        Date today = new Date();
        List<Offer> expiredOffers = offerRepository.findByDateExpBefore(today);

        if (!expiredOffers.isEmpty()) {
            offerRepository.deleteAll(expiredOffers);
            System.out.println("deleted" + expiredOffers.size() + " expired offers");
        } else {
            System.out.println("no expired offers to delete");
        }
    }

    @Override
    public Offer getOfferById(Long id) {
        return offerRepository.findById(id).orElse(null);
    }

    @Override
    public List<Comment> getCommentsByOfferId(Long offerId) {
        Offer offer = offerRepository.findById(offerId).orElse(null);
        if (offer != null) {
            // Convert Set<Comment> to List<Comment>
            return new ArrayList<>(offer.getComments());
        }
        return null; // Return null if offer is not found
    }

    @Override
    public List<Application> getAppByOfferId(Long offerId) {
        Offer offer = offerRepository.findById(offerId).orElse(null);
        if (offer != null) {
            return new ArrayList<>(offer.getApplications());
        }
        return null;
    }
    //add pdf for offer details
    public byte[] generatePdfForOffer(Offer offer) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;

                // Background color
                contentStream.setNonStrokingColor(220, 230, 230);
                contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                contentStream.fill();

                // Add White Box for Content
                contentStream.setNonStrokingColor(255, 255, 255);
                contentStream.addRect(margin - 10, margin - 10, page.getMediaBox().getWidth() - 2 * (margin - 10), yPosition - margin);
                contentStream.fill();

                // Add Logo
                try {
                    PDImageXObject logo = PDImageXObject.createFromFile("src/main/resources/images/logo.png", document);
                    contentStream.drawImage(logo, margin, yPosition - 50, 150, 50);
                    yPosition -= 70;
                } catch (IOException e) {
                    System.err.println("Logo not found: " + e.getMessage());
                }

                // Title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.setNonStrokingColor(140, 100, 50);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Details d'Offre");
                contentStream.endText();
                yPosition -= 30;

                // Table Header
                float tableX = margin;
                float tableY = yPosition;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
                float rowHeight = 20;
                float col1Width = tableWidth * 0.4f;
                float col2Width = tableWidth * 0.6f;

                // Draw table headers
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.setNonStrokingColor(0, 0, 0);

                contentStream.beginText();
                contentStream.newLineAtOffset(tableX, tableY);
                contentStream.showText("");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(tableX + col1Width, tableY);
                contentStream.showText("");
                contentStream.endText();

                tableY -= rowHeight;

                // Draw table rows
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                String[][] data = {
                        {"Titre", offer.getTitle()},
                        {"Description", offer.getDescription()},
                        {"Catégorie", offer.getCategory()},
                        {"Date de publication", formatDate(offer.getDatePub())},
                        {"Date d'expiration", formatDate(offer.getDateExp())}
                };

                for (String[] row : data) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(tableX, tableY);
                    contentStream.showText(row[0]);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(tableX + col1Width, tableY);
                    contentStream.showText(row[1]);
                    contentStream.endText();

                    tableY -= rowHeight;
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    private String formatDate(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter);
    }



    @Override
    public void saveOffer(Offer offer) {
        offerRepository.save(offer);
    }
    //accept offer or deny
   /* @Override
    public void acceptApplication(Long applicationId) {
        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);
        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();
            application.setStatus("ACCEPTED");
            applicationRepository.save(application);

            // Send Email Notification
            emailService.sendApplicationAcceptedEmail("omar.ben.mahmoud2002@gmail.com",
                    "Mr User",
                    application.getOffer().getTitle());
        } else {
            throw new RuntimeException("Application not found");
        }
    }*/


    @Override
    public void denyApplication(Long applicationId) {
        Optional<Application> optionalApplication = applicationRepository.findById(applicationId);
        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();
            application.setStatus("DENIED");
            applicationRepository.delete(application); // Deletes application from DB
        } else {
            throw new RuntimeException("Application not found");
        }
    }
    @Override
    public Map<Offer, Integer> getBestOffers() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Date dateThirtyDaysAgo = java.sql.Date.valueOf(thirtyDaysAgo);

        List<Offer> offers = offerRepository.findByDatePubAfter(dateThirtyDaysAgo);
        Map<Offer, Integer> offerScores = new HashMap<>();

        for (Offer offer : offers) {
            int numberOfComments = offer.getComments().size();
            int numberOfApplications = offer.getApplications().size();
            // Calculate score, here adding comments and applications
            int score = numberOfComments + numberOfApplications;

            offerScores.put(offer, score);
        }

        return offerScores;
    }


}
