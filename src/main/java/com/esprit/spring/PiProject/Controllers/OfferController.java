package com.esprit.spring.PiProject.Controllers;

import com.esprit.spring.PiProject.Repository.OfferRepository;
import com.esprit.spring.PiProject.Services.EmailService;
import com.esprit.spring.PiProject.Services.IOfferService;
import com.esprit.spring.PiProject.Services.OfferService;
import com.esprit.spring.PiProject.entities.Application;
import com.esprit.spring.PiProject.entities.Comment;
import com.esprit.spring.PiProject.entities.Offer;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/offers")
@CrossOrigin(origins = "http://localhost:4200")
public class OfferController {

    private OfferRepository offerRepository;
    @Autowired
    IOfferService offerService;
    @Autowired
    EmailService emailService;

    @GetMapping("/Get-All-Offers")
    public List<Offer> GetAllOffers() {
        List<Offer> listoffer=offerService.GetAllOffers();
        return listoffer;

    }
    @PostMapping("/Add-Offer")
    public Offer addOffer(@RequestBody Offer offer) {
        return offerService.AddOffer(offer);
    }

    @DeleteMapping("/Delete-Offer/{id}")
    public void deleteOffer(@PathVariable Long id) {
        offerService.DeleteOffer(id);
    }

    @PutMapping("/Update-Offer/{id}")
    public Offer updateOffer(@PathVariable Long id, @RequestBody Offer offer) {
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
    @GetMapping("/GetComments/{id}")
    public List<Comment> GetCommentsOffre(@PathVariable Long id){
        return offerService.getCommentsByOfferId(id);
    }


    @GetMapping("/NumberOfComments/{id}")
    public int NumberOfComments(@PathVariable Long id) {
        return offerService.NumberOfComment(id);
    }

    @GetMapping("/GetOfferById/{id}")
    public Offer GetOfferById(@PathVariable Long id) {
        return offerService.getOfferById(id);
    }
    @GetMapping("/GetAppById/{id}")
    public List<Application> GetAppById(@PathVariable Long id) {
        return offerService.getAppByOfferId(id);
    }
    @GetMapping("/search")
    public List<Offer> searchOffers(@RequestParam String title) {
        return offerRepository.findByTitleContainingIgnoreCase(title);
    }

    @GetMapping("/offers/{id}/pdf")
    public ResponseEntity<?> generateOfferPdf(@PathVariable long id) {
        try {
            Offer offer = offerService.getOfferById(id);
            byte[] pdfBytes = offerService.generatePdfForOffer(offer);

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=offer_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Offer not found");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate PDF: " + e.getMessage());
        }
    }
  /*  @PutMapping("/accept/{applicationId}")
    public ResponseEntity<String> acceptApplication(@PathVariable Long applicationId) {
        offerService.acceptApplication(applicationId);
        return ResponseEntity.ok("Application Accepted");
    }*/

    @DeleteMapping("/deny/{applicationId}")
    public ResponseEntity<String> denyApplication(@PathVariable Long applicationId) {
        offerService.denyApplication(applicationId);
        return ResponseEntity.ok("Application Denied and Deleted");
    }

   /* @GetMapping("/test-email")
    public String sendTestEmail(@RequestParam String to) {
        emailService.sendApplicationAcceptedEmail(to, "Test User", "Test Offer");
        return "Test email sent to " + to;
    }*/
    @GetMapping("/best-offers")
    public ResponseEntity<List<Map<String, Object>>> getBestOffers() {
        Map<Offer, Integer> bestOffers = offerService.getBestOffers();

        // Convert Offer to a simple list of maps
        List<Map<String, Object>> bestOffersData = bestOffers.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", entry.getKey().getTitle());
                    map.put("score", entry.getValue()); // Sum of comments and applications
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(bestOffersData);
    }



}
