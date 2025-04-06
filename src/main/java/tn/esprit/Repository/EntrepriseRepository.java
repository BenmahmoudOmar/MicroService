package tn.esprit.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.Entities.ERole;
import tn.esprit.Entities.Entreprise;
import tn.esprit.Entities.Role;

import java.util.Optional;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {

    // Custom query method to find an Entreprise by the associated User ID
    Optional<Entreprise> findByUserId(Long userId);
}
