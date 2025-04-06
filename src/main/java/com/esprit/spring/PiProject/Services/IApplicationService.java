package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.Repository.ApplicationRepository;
import com.esprit.spring.PiProject.entities.Application;

import java.util.List;

public interface IApplicationService {
    public Application AddApplication(Application application);
    public Application EditApplication(Long applicationId, String cv, String motivationLetter, String status);
    public void DeleteApplication(Long id);
    public List<Application> GetAllApplications();
    public Application applyForOffer(Long offerId, int userId, String cv, String motivationLetter);
    List<Application> getApplicationsByOffer(Long offerId);
    Application updateApplication(Long id, Application application);
    void deleteApplication(Long id);

    Application getApplicationById(Long id);
}
