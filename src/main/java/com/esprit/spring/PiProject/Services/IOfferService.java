package com.esprit.spring.PiProject.Services;

import com.esprit.spring.PiProject.entities.Application;
import com.esprit.spring.PiProject.entities.Comment;
import com.esprit.spring.PiProject.entities.Offer;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IOfferService {
    public Offer AddOffer(Offer offer);
    Offer EditOffer(Long id, Offer offer);
    public void DeleteOffer(Long id);
    public List<Offer> GetAllOffers();
    public int NumberOfComment(Long id);
    int NumberOfOffers();
    Offer getOfferById(Long id);
    List<Comment> getCommentsByOfferId(Long offerId);
    List<Application> getAppByOfferId(Long offerId);

    byte[] generatePdfForOffer(Offer offer);
    public Map<Offer, Integer> getBestOffers();
    void saveOffer(Offer offer);

   // void acceptApplication(Long applicationId);

    void denyApplication(Long applicationId);
}
