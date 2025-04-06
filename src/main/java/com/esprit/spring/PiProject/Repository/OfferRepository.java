package com.esprit.spring.PiProject.Repository;

import com.esprit.spring.PiProject.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByDateExpBefore(Date date);
    List<Offer> findByTitleContainingIgnoreCase(String title);

    List<Offer> findByDatePubAfter(Date dateThirtyDaysAgo);
}

