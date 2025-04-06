package com.example.gestiondocuments.Services;

import com.example.gestiondocuments.Entities.JobSeeker;
import com.example.gestiondocuments.Entities.Offer;
import com.example.gestiondocuments.Entities.Report;
import com.example.gestiondocuments.Repositories.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final ReportRepository reportRepository;

    @Override
    public Report addReport(MultipartFile file) throws IOException {
        // ✅ Manually create JobSeeker with ID 1
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setId(1L); // Set ID manually



        // Convert file to Base64
        String base64File = Base64.getEncoder().encodeToString(file.getBytes());

        // Create Report
        Report report = new Report();
        report.setFilePath(base64File);
        report.setSubmissionDate(new Date());
        report.setValidatedByCompany(false);
        report.setJobSeeker(jobSeeker);


        return reportRepository.save(report);
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    @Override
    public Report updateReport(Long id, MultipartFile file) throws IOException {
        Report report = getReportById(id);

        if (file != null && !file.isEmpty()) {
            String base64File = Base64.getEncoder().encodeToString(file.getBytes());
            report.setFilePath(base64File);
        }

        return reportRepository.save(report);
    }

    @Override
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean addSignatureToReport(Long reportId, String base64Signature) {
        Optional<Report> reportOptional = reportRepository.findById(reportId);
        if (reportOptional.isPresent()) {
            Report report = reportOptional.get();
            report.setSignature(base64Signature);
            report.setValidatedByCompany(true);
            reportRepository.save(report);
            return true;
        }
        return false;
    }

    @Override
    public Optional<byte[]> getSignedPdf(Long reportId) {
        Optional<Report> reportOptional = reportRepository.findById(reportId);
        if (reportOptional.isPresent()) {
            Report report = reportOptional.get();
            String filePath = report.getFilePath(); // Get file path

            if (filePath == null || filePath.isEmpty()) {
                return Optional.empty(); // No file path found
            }

            try {
                // Load PDF from file path
                File pdfFile = new File(filePath);
                if (!pdfFile.exists()) {
                    return Optional.empty(); // File not found
                }

                PDDocument document = PDDocument.load(pdfFile);
                PDPage page = document.getPage(document.getNumberOfPages() - 1);
                PDImageXObject signatureImage = createSignatureImage(report.getSignature(), document);

                if (signatureImage != null) {
                    PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
                    contentStream.drawImage(signatureImage, 450, 50, 100, 50); // Bottom right corner
                    contentStream.close();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                document.save(outputStream);
                document.close();

                return Optional.of(outputStream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public PDImageXObject createSignatureImage(String base64Signature, PDDocument document) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Signature);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));

            if (bufferedImage != null) {
                return LosslessFactory.createFromImage(document, bufferedImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
