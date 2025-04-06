package tn.esprit.services;

import tn.esprit.entities.Demand;

import java.util.List;

public interface IDemandService {

    List<Demand> getAllDemands();

    long countDemandsByField(String field);

    long countDemandsByStatus(String status);

    Demand getDemandById(Long id) ;

    List<Demand> searchDemandsByField(String field);

    Demand addDemand(Demand demand) ;

    void deleteDemand(Long id);
    

    Demand updateDemand(Long id,Demand demand);

    long countTotalDemands();
     List<Demand> findNearbyDemands(double latitude, double longitude, double radius);
}
