package com.esprit.spring.PiProject.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank(message = "Le commentaire ne peut pas être vide.")
    @Size(min = 5, max = 500, message = "Le commentaire doit avoir entre 5 et 500 caractères.")
    private String content;
    private Date creationDate;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private Offer offer;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public @NotBlank(message = "Le commentaire ne peut pas être vide.") @Size(min = 5, max = 500, message = "Le commentaire doit avoir entre 5 et 500 caractères.") String getContent() {
        return content;
    }

    public void setContent(@NotBlank(message = "Le commentaire ne peut pas être vide.") @Size(min = 5, max = 500, message = "Le commentaire doit avoir entre 5 et 500 caractères.") String content) {
        this.content = content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
