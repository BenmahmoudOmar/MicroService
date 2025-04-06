package com.esprit.spring.PiProject.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Offer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @NotBlank(message = "Le titre est requis")
  @Size(min = 3, message = "Le titre doit contenir au moins 3 caractères")
  private String title;
  @NotBlank(message = "La description est requise")
  @Size(min = 10, message = "La description doit contenir au moins 10 caractères")
  private String description;
  private String image;
  @NotNull(message = "La date de publication est requise")
  private Date datePub;
  @NotNull(message = "La date d'expiration est requise")
  @Future(message = "La date d'expiration doit être dans le futur")
  private Date dateExp;
  @NotBlank(message = "La catégorie est requise")
  private String category;
  @JsonIgnore
  @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
  private Set<Comment> comments = new HashSet<>();
  @JsonIgnore
  @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
  private Set<Application> applications = new HashSet<>();
  @JsonIgnore
  @ManyToOne
  User user;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public @NotBlank(message = "Le titre est requis") @Size(min = 3, message = "Le titre doit contenir au moins 3 caractères") String getTitle() {
    return title;
  }

  public void setTitle(@NotBlank(message = "Le titre est requis") @Size(min = 3, message = "Le titre doit contenir au moins 3 caractères") String title) {
    this.title = title;
  }

  public @NotBlank(message = "La description est requise") @Size(min = 10, message = "La description doit contenir au moins 10 caractères") String getDescription() {
    return description;
  }

  public void setDescription(@NotBlank(message = "La description est requise") @Size(min = 10, message = "La description doit contenir au moins 10 caractères") String description) {
    this.description = description;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public @NotNull(message = "La date de publication est requise") Date getDatePub() {
    return datePub;
  }

  public void setDatePub(@NotNull(message = "La date de publication est requise") Date datePub) {
    this.datePub = datePub;
  }

  public @NotNull(message = "La date d'expiration est requise") @Future(message = "La date d'expiration doit être dans le futur") Date getDateExp() {
    return dateExp;
  }

  public void setDateExp(@NotNull(message = "La date d'expiration est requise") @Future(message = "La date d'expiration doit être dans le futur") Date dateExp) {
    this.dateExp = dateExp;
  }

  public @NotBlank(message = "La catégorie est requise") String getCategory() {
    return category;
  }

  public void setCategory(@NotBlank(message = "La catégorie est requise") String category) {
    this.category = category;
  }

  public Set<Comment> getComments() {
    return comments;
  }

  public void setComments(Set<Comment> comments) {
    this.comments = comments;
  }

  public Set<Application> getApplications() {
    return applications;
  }

  public void setApplications(Set<Application> applications) {
    this.applications = applications;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
