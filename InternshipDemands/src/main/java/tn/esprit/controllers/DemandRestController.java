package tn.esprit.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entities.Demand;
import tn.esprit.entities.StatisticsResponse;
import tn.esprit.services.IDemandService;

import java.util.List;

@Tag(name="Demands Management")
@RestController
@AllArgsConstructor
@RequestMapping("/api/demands")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandRestController {



    IDemandService demandService;

    @GetMapping("/nearby")

    public List<Demand> getNearbyDemands(@RequestParam double lat, @RequestParam double lng, @RequestParam double radius) {

        return demandService.findNearbyDemands(lat, lng, radius);

    }

    @GetMapping("/getAllDemands")
    public List<Demand> getAllDemands() {
        return demandService.getAllDemands();
    }

    @GetMapping("/getDemandById/{id}")
    public Demand getDemandById(@PathVariable Long id) {
        return demandService.getDemandById(id);
    }
    @PutMapping("/updateDemand/{id}")
    public Demand updateDemand(@PathVariable Long id, @RequestBody Demand Demand) {
        return demandService.updateDemand(id,Demand);
    }
    @GetMapping("/statistics")
    public ResponseEntity<Object> getDemandStatistics(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String status) {
        long totalDemands = demandService.countTotalDemands();
        long demandsByField = (field != null) ? demandService.countDemandsByField(field) : 0;
        long demandsByStatus = (status != null) ? demandService.countDemandsByStatus(status) : 0;

        return ResponseEntity.ok(new StatisticsResponse(totalDemands, demandsByField, demandsByStatus));
    }
    @GetMapping("/search")
    public List<Demand> searchDemands(@RequestParam String field) {
        return demandService.searchDemandsByField(field);
    }

    @PostMapping("/addDemand")
    public Demand addDemand(@RequestBody Demand Demand) {
        return demandService.addDemand(Demand);
    }


    @DeleteMapping("/deleteDemand/{id}")
    public void deleteDemand(@PathVariable Long id) {
        demandService.deleteDemand(id);
    }
}
