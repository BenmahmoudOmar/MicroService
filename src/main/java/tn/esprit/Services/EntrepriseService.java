package tn.esprit.Services;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.Entities.Entreprise;
import tn.esprit.Entities.JobSeeker;
import tn.esprit.Entities.User;
import tn.esprit.KeycloakConfig;
import tn.esprit.Repository.EntrepriseRepository;
import tn.esprit.Repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EntrepriseService  {

    @Autowired
    UserDetailsServiceImpl UserDetailsImpl1;



    @Autowired
    EntrepriseRepository  EntrepriseRepository;

    @Autowired
    UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(User.class);



    public Entreprise updateEntreprise(Entreprise entreprise, Long id) {
        Optional<Entreprise> existingEntreprise = EntrepriseRepository.findById(entreprise.getId());
        Entreprise updatedEntreprise=new Entreprise();

        if (existingEntreprise.isPresent()) {
             updatedEntreprise = existingEntreprise.get();

            // Update fields
            updatedEntreprise.setCompanyDescription(entreprise.getCompanyDescription());
            updatedEntreprise.setAddress(entreprise.getAddress());
            updatedEntreprise.setContactNumber(entreprise.getContactNumber());
            updatedEntreprise.setLogo(entreprise.getLogo());
            updatedEntreprise.setIndustry(entreprise.getIndustry());
            updatedEntreprise.setCompanyWebsite(entreprise.getCompanyWebsite());

            // Fetch User by ID and set it
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                updatedEntreprise.setUser(user);
                updateKeycloakUserAttributes( updatedEntreprise);

            } else {
                logger.error("User not found for ID: " + id);
            }





            // Save and return updated Entreprise

        }
        else {

            updatedEntreprise = entreprise;

            // Update fields
            updatedEntreprise.setCompanyDescription(entreprise.getCompanyDescription());
            updatedEntreprise.setAddress(entreprise.getAddress());
            updatedEntreprise.setContactNumber(entreprise.getContactNumber());
            updatedEntreprise.setLogo(entreprise.getLogo());
            updatedEntreprise.setIndustry(entreprise.getIndustry());
            updatedEntreprise.setCompanyWebsite(entreprise.getCompanyWebsite());

            // Fetch User by ID and set it
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                updatedEntreprise.setUser(user);
                updateKeycloakUserAttributes( updatedEntreprise);



            }}


        return EntrepriseRepository.save(updatedEntreprise);
    }

    private void updateKeycloakUserAttributes( Entreprise entreprise) {
        Keycloak keycloak = KeycloakConfig.getInstance();
        RealmResource realmResource = keycloak.realm(KeycloakConfig.realm);
        UsersResource usersResource = realmResource.users();

        // Find user in Keycloak by email (associated with the Entreprise)
        List<UserRepresentation> users = usersResource.search(entreprise.getUser().getEmail(), 0, 1);
        if (users.isEmpty()) {
            throw new RuntimeException("User not found in Keycloak");
        }

        UserRepresentation userRep = users.get(0);
        UserResource userResource = usersResource.get(userRep.getId());

        // Set attributes in Keycloak for Entreprise
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("companyDescription", Collections.singletonList(entreprise.getCompanyDescription() != null ? entreprise.getCompanyDescription() : ""));
        attributes.put("address", Collections.singletonList(entreprise.getAddress() != null ? entreprise.getAddress() : ""));
        attributes.put("contactNumber", Collections.singletonList(entreprise.getContactNumber() != null ? entreprise.getContactNumber().toString() : ""));
        attributes.put("logo", Collections.singletonList(entreprise.getLogo() != null ? entreprise.getLogo() : ""));
        attributes.put("industry", Collections.singletonList(entreprise.getIndustry() != null ? entreprise.getIndustry() : ""));
        attributes.put("companyWebsite", Collections.singletonList(entreprise.getCompanyWebsite() != null ? entreprise.getCompanyWebsite() : ""));

        // Update Keycloak user attributes
        userRep.setAttributes(attributes);

        if (entreprise.getUser().getIsVerified() == 1) {
            userRep.setEmailVerified(true);
        }
        else             userRep.setEmailVerified(false);
        userResource.update(userRep);
    }


    public ResponseEntity<?> updateEntreprise1(Entreprise entreprise) {
        try {
            Optional<Entreprise> existingEntreprise = EntrepriseRepository.findById(entreprise.getId());
            Entreprise updatedEntreprise = new Entreprise();

            if (existingEntreprise.isPresent()) {
                updatedEntreprise = existingEntreprise.get();

                // Check if the username or email already exists
                Optional<User> existingUserByUsername = userRepository.findByUsername(entreprise.getUser().getUsername());
                if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(entreprise.getUser().getId())) {
                    return ResponseEntity.badRequest().body("Error: Username is already taken.");
                }

                Optional<User> existingUserByEmail = Optional.ofNullable(userRepository.findByEmail(entreprise.getUser().getEmail()));
                if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(entreprise.getUser().getId())) {
                    return ResponseEntity.badRequest().body("Error: Email is already in use.");
                }

                // Check phone pattern (convert Long to String and validate)
                String phone = String.valueOf(entreprise.getUser().getPhone()); // Convert phone to String
                String phonePattern = "^\\+?[0-9]{8}$"; // Example: valid phone pattern
                if (!phone.matches(phonePattern)) {
                    return ResponseEntity.badRequest().body("Error: Invalid phone number.");
                }

                // Check verification status (must be 1 or 0)
                if (entreprise.getUser().getIsVerified() != 1 && entreprise.getUser().getIsVerified() != 0) {
                    return ResponseEntity.badRequest().body("Error: Invalid verification status.");
                }

                // Update the entity fields
                updatedEntreprise.setId(entreprise.getId());
                updatedEntreprise.setCompanyDescription(entreprise.getCompanyDescription());
                updatedEntreprise.setAddress(entreprise.getAddress());
                updatedEntreprise.setContactNumber(entreprise.getContactNumber());
                updatedEntreprise.setLogo(entreprise.getLogo());
                updatedEntreprise.setIndustry(entreprise.getIndustry());
                updatedEntreprise.setCompanyWebsite(entreprise.getCompanyWebsite());
                updatedEntreprise.getUser().setEmail(entreprise.getUser().getEmail());
                updatedEntreprise.getUser().setPhone(entreprise.getUser().getPhone());
                updatedEntreprise.getUser().setUsername(entreprise.getUser().getUsername());
                updatedEntreprise.getUser().setIsVerified(entreprise.getUser().getIsVerified());

                // Save and return the updated Entreprise
                UserDetailsImpl1.updateUser(updatedEntreprise.getUser());

                updateKeycloakUserAttributes( updatedEntreprise);

                return ResponseEntity.ok(EntrepriseRepository.save(updatedEntreprise));
            } else {
                return ResponseEntity.badRequest().body("Error: Entreprise not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


    public ResponseEntity<?> addEntrepriseSeeker(Entreprise entreprise) {
        try {
            // Check if the username already exists
            Optional<User> existingUserByUsername = userRepository.findByUsername(entreprise.getUser().getUsername());
            if (existingUserByUsername.isPresent()) {
                return ResponseEntity.badRequest().body("Error: Username is already taken.");
            }

            // Check if the email already exists
            Optional<User> existingUserByEmail = Optional.ofNullable(userRepository.findByEmail(entreprise.getUser().getEmail()));
            if (existingUserByEmail.isPresent()) {
                return ResponseEntity.badRequest().body("Error: Email is already in use.");
            }

            // Check phone pattern (convert Long to String and validate)
            String phone = String.valueOf(entreprise.getUser().getPhone()); // Convert phone to String
            String phonePattern = "^\\+?[0-9]{8}$"; // Example: valid phone pattern
            if (!phone.matches(phonePattern)) {
                return ResponseEntity.badRequest().body("Error: Invalid phone number.");
            }

            // Check verification status (must be 1 or 0)
            if (entreprise.getUser().getIsVerified() != 1 && entreprise.getUser().getIsVerified() != 0) {
                return ResponseEntity.badRequest().body("Error: Invalid verification status.");
            }

            // **Step 1: Create user in Keycloak**
            Keycloak keycloak = KeycloakConfig.getInstance();
            RealmResource realmResource = keycloak.realm(KeycloakConfig.realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(entreprise.getUser().getUsername());
            keycloakUser.setEmail(entreprise.getUser().getEmail());
            keycloakUser.setEnabled(true);
            if(entreprise.getUser().getIsVerified() != 1)
            keycloakUser.setEmailVerified(false);
            else
                keycloakUser.setEmailVerified(true);
            // If verified, set email as verified

            Response response = usersResource.create(keycloakUser);
            if (response.getStatus() != 201) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to create user in Keycloak.");
            }

            // Extract the Keycloak ID from the response
            String keycloakId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // **Step 2: Set Keycloak ID in local User entity**
            entreprise.getUser().setKeycloakId(keycloakId);

            // **Step 3: Set password in Keycloak**
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(entreprise.getUser().getPassword());
            passwordCred.setTemporary(false);
            usersResource.get(keycloakId).resetPassword(passwordCred);

            // **Step 4: Assign Role to User in Keycloak**
            RoleRepresentation entrepriseRole = realmResource.roles().get("entreprise").toRepresentation();
            usersResource.get(keycloakId).roles().realmLevel().add(Collections.singletonList(entrepriseRole));












            // Save and return the new Entreprise
            return ResponseEntity.ok(EntrepriseRepository.save(entreprise));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    public void deleteEntreprise(Long id) {
        // Step 1: Fetch the Entreprise from the database
        Entreprise entreprise = EntrepriseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entreprise not found with id: " + id));

        // Step 2: Get the associated User
        User user = entreprise.getUser();

        if (user != null && user.getKeycloakId() != null && !user.getKeycloakId().isEmpty()) {
            // Step 3: Get Keycloak user ID
            String keycloakId = user.getKeycloakId();

            // Step 4: Delete user from Keycloak
            try {
                Keycloak keycloak = KeycloakConfig.getInstance();
                UsersResource usersResource = keycloak.realm(KeycloakConfig.realm).users();
                UserResource keycloakUserResource = usersResource.get(keycloakId);

                // Delete the user from Keycloak
                keycloakUserResource.remove();
            } catch (Exception e) {
                throw new RuntimeException("Error deleting user from Keycloak", e);
            }
        }

        // Step 5: Delete Entreprise from the local database
        EntrepriseRepository.deleteById(id);
    }


        public Optional<Entreprise> getEntrepriseByUserId(Long userId) {
            return EntrepriseRepository.findByUserId(userId);
        }

    public Optional<Entreprise> getEntrepriseById(Long Id) {
        return EntrepriseRepository.findById(Id);
    }


    public List<Entreprise> getEntreprise(){
        return EntrepriseRepository.findAll();
    }


    public Entreprise addEntrepriseToUser(Long userId, Entreprise entrepriseDetails) throws Exception {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new Exception("User not found with id: " + userId);
        }
        User user = optionalUser.get();

        // Link the entreprise to the user
        entrepriseDetails.setUser(user);

        // Save the entreprise (or user, depending on cascade settings)
        return EntrepriseRepository.save(entrepriseDetails);
    }


}