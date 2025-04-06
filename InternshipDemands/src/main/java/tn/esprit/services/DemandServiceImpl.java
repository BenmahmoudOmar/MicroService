package tn.esprit.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.repositories.DemandRepository;
import tn.esprit.entities.Demand;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class DemandServiceImpl implements IDemandService {

    DemandRepository demandRepository;

    @Override
    public List<Demand> getAllDemands() {
        return demandRepository.findAll();
    }
    @Override
    public long countTotalDemands() {
        return demandRepository.count();  // Get the total number of demands
    }

    @Override
    public long countDemandsByField(String field) {
        return demandRepository.countByField(field);  // Get the number of demands for a specific field
    }

    @Override
    public long countDemandsByStatus(String status) {
        return demandRepository.countByStatus(status);  // Get the number of demands with a specific status
    }


    @Override
    public Demand getDemandById(Long id) {
        return demandRepository.findById(id).orElse(null);
    }
    @Override
    public List<Demand> searchDemandsByField(String field) {
        return demandRepository.findByFieldContainingIgnoreCase(field);
    }

    @Override
    public Demand addDemand(Demand demand) {
        demand.setDate(java.sql.Date.valueOf(LocalDate.now()));
        return demandRepository.save(demand);
    }

    @Override
    public void deleteDemand(Long id) {
        demandRepository.deleteById(id);
    }

    @Override
    public Demand updateDemand(Long id,Demand demand) {
        Demand existingDemand = demandRepository.findById(id).orElse(null);
        if (existingDemand != null) {
            existingDemand.setTitle(demand.getTitle());
            existingDemand.setDescription(demand.getDescription());
            existingDemand.setField(demand.getField());
            existingDemand.setDate(java.sql.Date.valueOf(LocalDate.now()));
            existingDemand.setStatus(demand.getStatus());
            return demandRepository.save(existingDemand);
        }
        return null;
    }

    public List<Demand> findNearbyDemands(double latitude, double longitude, double radius) {

        return demandRepository.findNearbyDemands(latitude, longitude, radius);

    }
}
