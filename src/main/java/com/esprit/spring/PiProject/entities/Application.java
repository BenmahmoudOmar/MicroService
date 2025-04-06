package com.esprit.spring.PiProject.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Date PostDate;
    @NotNull(message = "Status cannot be null")
    @Size(min = 2, max = 50, message = "Status should have between 2 and 50 characters")
    private String status;
    @NotNull(message = "CV cannot be null")
    @Size(min = 5, message = "CV should have at least 5 characters")
    private String cv;
    @NotNull(message = "Motivation Letter cannot be null")
    @Size(min = 10, message = "Motivation Letter should have at least 10 characters")
    private String MotivationLetter;
    @JsonIgnore
    @ManyToOne
    Offer offer;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getPostDate() {
        return PostDate;
    }

    public void setPostDate(Date postDate) {
        PostDate = postDate;
    }

    public @NotNull(message = "Status cannot be null") @Size(min = 2, max = 50, message = "Status should have between 2 and 50 characters") String getStatus() {
        return status;
    }

    public void setStatus(@NotNull(message = "Status cannot be null") @Size(min = 2, max = 50, message = "Status should have between 2 and 50 characters") String status) {
        this.status = status;
    }

    public @NotNull(message = "CV cannot be null") @Size(min = 5, message = "CV should have at least 5 characters") String getCv() {
        return cv;
    }

    public void setCv(@NotNull(message = "CV cannot be null") @Size(min = 5, message = "CV should have at least 5 characters") String cv) {
        this.cv = cv;
    }

    public @NotNull(message = "Motivation Letter cannot be null") @Size(min = 10, message = "Motivation Letter should have at least 10 characters") String getMotivationLetter() {
        return MotivationLetter;
    }

    public void setMotivationLetter(@NotNull(message = "Motivation Letter cannot be null") @Size(min = 10, message = "Motivation Letter should have at least 10 characters") String motivationLetter) {
        MotivationLetter = motivationLetter;
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
