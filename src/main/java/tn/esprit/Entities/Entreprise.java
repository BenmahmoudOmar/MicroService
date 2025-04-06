package tn.esprit.Entities;

import javax.persistence.*;

@Entity
@Table(name = "Entreprise")  // Specify a separate table for Entreprise
public class Entreprise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String companyDescription;
	private String address;
	private Number contactNumber;
	private String logo;
	private String industry;
	private String companyWebsite;

	@OneToOne(cascade = CascadeType.ALL)  // Cascade the save operation to User
	private User user;

	// Default constructor
	public Entreprise() {}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCompanyDescription() {
		return companyDescription;
	}

	public void setCompanyDescription(String companyDescription) {
		this.companyDescription = companyDescription;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Number getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(Number contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getCompanyWebsite() {
		return companyWebsite;
	}

	public void setCompanyWebsite(String companyWebsite) {
		this.companyWebsite = companyWebsite;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
