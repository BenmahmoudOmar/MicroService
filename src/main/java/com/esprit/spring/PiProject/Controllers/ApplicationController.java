package com.esprit.spring.PiProject.Controllers;

import com.esprit.spring.PiProject.Services.IApplicationService;
import com.esprit.spring.PiProject.entities.Application;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/Application")
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicationController {
    @Autowired
    IApplicationService applicationService;



    @GetMapping("/retrieve-all-application")
    public List<Application> getApplications() {
        List<Application> listApplications = applicationService.GetAllApplications();
        return listApplications;
    }
    @PostMapping("/Add-Application")
    public Application addApplication(@RequestBody Application application) {
        return applicationService.AddApplication(application);
    }
    @GetMapping("/offer/{offerId}/applications")
    public List<Application> getApplicationsByOffer(@PathVariable Long offerId) {
        return applicationService.getApplicationsByOffer(offerId);
    }

    @PutMapping("/Update-Application/{id}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable Long id,
            @RequestBody Application application) {
        Application updatedApplication = applicationService.updateApplication(id, application);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/Delete-Application/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/apply")
    public ResponseEntity<Application> applyForOffer(
            @RequestParam Long offerId,
            @RequestParam int userId,
            @RequestParam String cv,
            @RequestParam String motivationLetter) {

        Application application = applicationService.applyForOffer(offerId, userId, cv, motivationLetter);
        return ResponseEntity.ok(application);
    }
    @GetMapping("/GetApplication/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        Application application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(application);
    }
}
