package tn.esprit.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.entities.Demand;
import tn.esprit.entities.User;
import tn.esprit.repositories.DemandRepository;
import tn.esprit.repositories.ResponseRepository;
import tn.esprit.entities.Response;
import tn.esprit.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ResponseServiceImpl implements IResponseService {
    private final DemandRepository demandRepository;
    ResponseRepository responseRepository;
    private EmailService emailService;
    private UserRepository userRepository;
    NotificationService notificationService;

    @Override
    public List<Response> getAllResponses() {
        return responseRepository.findAll();
    }

    @Override
    public Response getResponseById(Long id) {
        return responseRepository.findById(id).orElse(null);
    }
    @Override
    public Response addResponse(Long demandId, String comment, String status) {
        Optional<Demand> demand = demandRepository.findById(demandId);
        if (demand.isPresent()) {
            Response response = new Response();
            response.setComment(comment);
            response.setStatus(status);
            response.setDemand(demand.get());
            response.setDate(java.sql.Date.valueOf(LocalDate.now()));

            // Save response before sending email
            Response savedResponse = responseRepository.save(response);

            // Use static user with ID = 1 for testing
            Optional<User> staticUser = userRepository.findById(1L);
             emailService.sendEmail(staticUser.get().getEmail(),
                    "Update on Your Internship Demand",
                    "Your demand has received a response: " + comment + "\nStatus: " + status);
            notificationService.createNotification(1L,
                    "Your demand has received a response: " + response);
            return savedResponse;
        }
        return null;
    }


    @Override
    public Response getResponse(Long demandId) {
        return (Response) responseRepository.findByDemandId(demandId);
    }


    @Override
    public Response updateResponse(Long id,Response response) {
        Response existingResponse = responseRepository.findById(id).orElse(null);
        if (existingResponse != null) {
            existingResponse.setDate(response.getDate());
            existingResponse.setStatus(response.getStatus());
            existingResponse.setComment(response.getComment());
            return responseRepository.save(existingResponse);
        }
        return null;
    }



    @Override
    public void deleteResponse(Long id) {
        responseRepository.deleteById(id);
    }

    @Override
    public Response saveResponse(Response response, Long demandId) {
        Demand demand = demandRepository.findDemandById(demandId);
        response.setDemand(demand);
        return responseRepository.save(response);
    }

    @Override
    public List<Response> getResponsesByDemand(Long demandId) {
        return responseRepository.findByDemandId(demandId);
    }

}
