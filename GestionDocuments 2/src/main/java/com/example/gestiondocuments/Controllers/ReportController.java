package com.example.gestiondocuments.Controllers;

import com.example.gestiondocuments.Entities.Report;
import com.example.gestiondocuments.Services.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins="http://localhost:4200")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    // ✅ Add Report (Upload PDF as Base64)
    @PostMapping("/add")
    public ResponseEntity<Report> addReport(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(reportService.addReport(file));
    }

    // ✅ Get All Reports
    @GetMapping("/all")
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    // ✅ Get Report by ID
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    // ✅ Update Report (Replace file)
    @PutMapping("/update/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(reportService.updateReport(id, file));
    }

    // ✅ Delete Report
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Add a signature to a report
    @PutMapping("/sign/{reportId}")
    public ResponseEntity<String> addSignature(@PathVariable Long reportId, @RequestBody SignatureRequest request) {
        boolean success = reportService.addSignatureToReport(reportId, request.getSignature());
        if (success) {
            return ResponseEntity.ok("Signature added successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report not found");
        }
    }

    // ✅ Download the signed PDF
    @GetMapping("/download/{reportId}")
    public ResponseEntity<ByteArrayResource> downloadReport(@PathVariable Long reportId) {
        Optional<byte[]> signedPdfData = reportService.getSignedPdf(reportId);
        if (signedPdfData.isPresent()) {
            byte[] pdfData = signedPdfData.get();
            ByteArrayResource resource = new ByteArrayResource(pdfData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Report_" + reportId + ".pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(resource);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // DTO for handling signature requests
    static class SignatureRequest {
        private String signature;

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }
}
