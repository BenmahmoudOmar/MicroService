package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.Repository.ApplicationRepository;
import com.esprit.spring.PiProject.Repository.OfferRepository;
import com.esprit.spring.PiProject.Repository.UserRepository;
import com.esprit.spring.PiProject.entities.Application;
import com.esprit.spring.PiProject.entities.Offer;
import com.esprit.spring.PiProject.entities.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationService implements  IApplicationService {
    ApplicationRepository applicationRepository;
    OfferRepository offerRepository;
    UserRepository userRepository;


    @Override
    public Application AddApplication(Application application) {
        return applicationRepository.save(application);
    }

    @Override
    public Application EditApplication(Long applicationId, String cv, String motivationLetter, String status) {
        // Find the Application by ID
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Update the application attributes
        application.setCv(cv);
        application.setMotivationLetter(motivationLetter);
        application.setStatus(status);

        // Optionally, you could update other fields like 'PostDate' if needed
        application.setPostDate(new Date()); // Uncomment if you want to update the post date

        // Save the updated Application object back to the database
        return applicationRepository.save(application);
    }

    @Override
    public void DeleteApplication(Long id) {
         applicationRepository.deleteById(id);
    }

    @Override
    public List<Application> GetAllApplications() {
        return applicationRepository.findAll();
    }

    @Override
    public Application applyForOffer(Long offerId, int userId, String cv, String motivationLetter) {
        // Retrieve the offer
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        // Retrieve the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new application
        Application application = new Application();
        application.setPostDate(new Date());
        application.setStatus("Pending");
        application.setCv(cv);
        application.setMotivationLetter(motivationLetter);
        application.setOffer(offer);
        application.setUser(user);

        // Save the application
        return applicationRepository.save(application);
    }
    @Override
    public List<Application> getApplicationsByOffer(Long offerId) {
        return applicationRepository.findByOfferId(offerId);
    }

    @Override
    public Application updateApplication(Long id, Application application) {
        Application existingApp = applicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        existingApp.setCv(application.getCv());
        existingApp.setMotivationLetter(application.getMotivationLetter());
        existingApp.setStatus(application.getStatus());
        return applicationRepository.save(existingApp);
    }

    @Override
    public void deleteApplication(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Détacher l'utilisateur et l'offre avant la suppression
        application.setUser(null);
        application.setOffer(null);

        applicationRepository.delete(application);
    }
    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }




}
