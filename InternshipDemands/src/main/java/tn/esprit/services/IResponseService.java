package tn.esprit.services;

import tn.esprit.entities.Response;

import java.util.List;

public interface IResponseService {
    List<Response> getAllResponses();

    Response getResponseById(Long id);


    Response updateResponse(Long id,Response response);
    void deleteResponse(Long id) ;
    Response addResponse(Long demandId, String comment, String status);
    Response getResponse(Long demandId);
    Response saveResponse(Response response, Long demandId);

    List<Response> getResponsesByDemand(Long demandId);
}
