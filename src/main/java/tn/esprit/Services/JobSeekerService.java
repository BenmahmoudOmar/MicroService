package tn.esprit.Services;

import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tn.esprit.Entities.JobSeeker;
import tn.esprit.Entities.User;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.representations.idm.CredentialRepresentation;

import tn.esprit.KeycloakConfig;
import tn.esprit.Repository.JobSeekerRepository;
import tn.esprit.Repository.UserRepository;
import org.springframework.http.ResponseEntity;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class JobSeekerService {
    @Autowired
    JobSeekerRepository JobSeekerRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDetailsServiceImpl UserDetailsImpl1;





    public JobSeeker updateJobSeeker(JobSeeker entreprise, Long id) {
        Optional<JobSeeker> existingEntreprise = JobSeekerRepository.findById(entreprise.getId());
        JobSeeker updatedEntreprise=new JobSeeker();




        if (existingEntreprise.isPresent()) {
            updatedEntreprise = existingEntreprise.get();

            updatedEntreprise.setId(entreprise.getId());
            updatedEntreprise.setResume(entreprise.getResume());
            updatedEntreprise.setSkills(entreprise.getSkills());
            updatedEntreprise.setEducation(entreprise.getEducation());

            // Fetch User by ID and set it
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                updatedEntreprise.setUser(user);
                updateKeycloakUserAttributes( updatedEntreprise);

            }





            // Save and return updated Entreprise

        }






        return JobSeekerRepository.save(updatedEntreprise);
    }



    private void updateKeycloakUserAttributes( JobSeeker jobSeeker) {
        Keycloak keycloak = KeycloakConfig.getInstance();
        RealmResource realmResource = keycloak.realm(KeycloakConfig.realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.search(jobSeeker.getUser().getEmail(), 0, 1);
        if (users.isEmpty()) {
            throw new RuntimeException("User not found in Keycloak");
        }

        UserRepresentation userRep = users.get(0);
        UserResource userResource = usersResource.get(userRep.getId());

        // Set attributes in Keycloak
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("resume", Collections.singletonList(jobSeeker.getResume() != null ? jobSeeker.getResume() : ""));
        attributes.put("skills", Collections.singletonList(jobSeeker.getSkills() != null ? jobSeeker.getSkills() : ""));
        attributes.put("education", Collections.singletonList(jobSeeker.getEducation() != null ? jobSeeker.getEducation() : ""));

        userRep.setAttributes(attributes);

        // **Set emailVerified = true if user is verified (isVerified == 1)**
        if (jobSeeker.getUser().getIsVerified() == 1) {
            userRep.setEmailVerified(true);
        }
        else             userRep.setEmailVerified(false);

        userResource.update(userRep);
    }

    public ResponseEntity<?> updateJobSeeker1(JobSeeker entreprise) {
        // Check if the JobSeeker exists by ID
        Optional<JobSeeker> existingEntreprise = JobSeekerRepository.findById(entreprise.getId());
        JobSeeker updatedEntreprise = new JobSeeker();

        String phoneNumber = String.valueOf(entreprise.getUser().getPhone());

        // Define a regex pattern for validating phone numbers (e.g., 10 digits)
        String phonePattern = "^[0-9]{8}$";
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(phoneNumber);

        // Check if the phone number matches the pattern
        if (!matcher.matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Invalid phone number format. Must be 8 digits.");
        }

        // Check if the email already exists (excluding the current JobSeeker)
        Optional<User> existingEmailUser = Optional.ofNullable(userRepository.findByEmail(entreprise.getUser().getEmail()));
        if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(entreprise.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Email already exists.");
        }

        // Check if the phone number already exists (excluding the current JobSeeker)


// Check if the verification status is valid (either 0 or 1)
        if (entreprise.getUser().getIsVerified() != 0 && entreprise.getUser().getIsVerified() != 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Verification status must be either 0 or 1.");
        }



        // Check if the JobSeeker exists
        if (existingEntreprise.isPresent()) {
            updatedEntreprise = existingEntreprise.get();

            // Check if the username already exists in the database (excluding the current JobSeeker)
            Optional<User> existingUser = userRepository.findByUsername(entreprise.getUser().getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(entreprise.getUser().getId())) {
                // If the username exists and is not the current user's username, return an error
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Username already exists.");
            }

            // Proceed with updating the JobSeeker details
            updatedEntreprise.setId(entreprise.getId());
            updatedEntreprise.setResume(entreprise.getResume());
            updatedEntreprise.setSkills(entreprise.getSkills());
            updatedEntreprise.setEducation(entreprise.getEducation());
            updatedEntreprise.getUser().setEmail(entreprise.getUser().getEmail());
            updatedEntreprise.getUser().setPhone(entreprise.getUser().getPhone());
            updatedEntreprise.getUser().setUsername(entreprise.getUser().getUsername());
            updatedEntreprise.getUser().setIsVerified(entreprise.getUser().getIsVerified());



        } else {
            updatedEntreprise = entreprise;

            // Same checks as above if the JobSeeker doesn't exist (for a new JobSeeker)
            Optional<User> existingUser = userRepository.findByUsername(entreprise.getUser().getUsername());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Username already exists.");
            }

            updatedEntreprise.setId(entreprise.getId());
            updatedEntreprise.setResume(entreprise.getResume());
            updatedEntreprise.setSkills(entreprise.getSkills());
            updatedEntreprise.setEducation(entreprise.getEducation());
            updatedEntreprise.getUser().setId(entreprise.getUser().getId());
            updatedEntreprise.getUser().setEmail(entreprise.getUser().getEmail());
            updatedEntreprise.getUser().setPhone(entreprise.getUser().getPhone());
            updatedEntreprise.getUser().setUsername(entreprise.getUser().getUsername());
            updatedEntreprise.getUser().setIsVerified(entreprise.getUser().getIsVerified());
        }

        try {
            // Save the updated JobSeeker and return a successful response
            UserDetailsImpl1.updateUser(updatedEntreprise.getUser());
            updateKeycloakUserAttributes(updatedEntreprise);

            return ResponseEntity.ok(JobSeekerRepository.save(updatedEntreprise));

        }  catch (Exception e) {
            // Handle other exceptions (e.g., general database issues)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    public ResponseEntity<?> addJobSeeker(JobSeeker jobSeeker) {
        try {
            // Check if the username or email already exists
            Optional<User> existingUserByUsername = userRepository.findByUsername(jobSeeker.getUser().getUsername());
            if (existingUserByUsername.isPresent()) {
                return ResponseEntity.badRequest().body("Error: Username is already taken.");
            }

            Optional<User> existingUserByEmail = Optional.ofNullable(userRepository.findByEmail(jobSeeker.getUser().getEmail()));
            if (existingUserByEmail.isPresent()) {
                return ResponseEntity.badRequest().body("Error: Email is already in use.");
            }

            // Check phone pattern (convert Long to String and validate)
            String phone = String.valueOf(jobSeeker.getUser().getPhone()); // Convert phone to String
            String phonePattern = "^\\+?[0-9]{8}$"; // Example: valid phone pattern
            if (!phone.matches(phonePattern)) {
                return ResponseEntity.badRequest().body("Error: Invalid phone number.");
            }

            // Check verification status (must be 1 or 0)
            if (jobSeeker.getUser().getIsVerified() != 1 && jobSeeker.getUser().getIsVerified() != 0) {
                return ResponseEntity.badRequest().body("Error: Verification status must be either 0 or 1.");
            }

            // **Step 1: Initialize Keycloak**
            Keycloak keycloak = KeycloakConfig.getInstance();
            RealmResource realmResource = keycloak.realm(KeycloakConfig.realm);
            UsersResource usersResource = realmResource.users();

            // **Step 2: Create a Keycloak user**
            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(jobSeeker.getUser().getUsername());
            keycloakUser.setEmail(jobSeeker.getUser().getEmail());
            keycloakUser.setEnabled(true);
            if(jobSeeker.getUser().getIsVerified() == 1)
            keycloakUser.setEmailVerified(true);
            else
                keycloakUser.setEmailVerified(false);

            // Set attributes
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("resume", Collections.singletonList(jobSeeker.getResume() != null ? jobSeeker.getResume() : ""));
            attributes.put("skills", Collections.singletonList(jobSeeker.getSkills() != null ? jobSeeker.getSkills() : ""));
            attributes.put("education", Collections.singletonList(jobSeeker.getEducation() != null ? jobSeeker.getEducation() : ""));
            keycloakUser.setAttributes(attributes);

            // **Step 3: Create the user in Keycloak**
            Response response = usersResource.create(keycloakUser);

            if (response.getStatus() != 201) { // 201 = Created
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Could not create user in Keycloak.");
            }

            // Extract Keycloak User ID from response
            String keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // **Step 4: Set password for Keycloak user**
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(jobSeeker.getUser().getPassword()); // Raw password, Keycloak will encrypt

            usersResource.get(keycloakUserId).resetPassword(passwordCred);

            // **Step 5: Store Keycloak user ID in database**
            jobSeeker.getUser().setKeycloakId(keycloakUserId);

            // **Step 4: Assign Role to User in Keycloak**
            RoleRepresentation userRole = realmResource.roles().get("user").toRepresentation();
            usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(userRole));











            // Save the new job seeker
            JobSeeker savedJobSeeker = JobSeekerRepository.save(jobSeeker);
            return ResponseEntity.ok(savedJobSeeker);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    public void deleteJobSeeker(Long id) {
        // Step 1: Fetch the JobSeeker from the database
        JobSeeker jobSeeker = JobSeekerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found with id: " + id));

        // Step 2: Get the associated User
        User user = jobSeeker.getUser();

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

        // Step 5: Delete JobSeeker from the local database
        JobSeekerRepository.deleteById(id);
    }

    public Optional<JobSeeker> getJobSeekerByUserId(Long userId) {
        return JobSeekerRepository.findByUserId(userId);
    }

    public List<JobSeeker> getJobSeekers(){
        return JobSeekerRepository.findAll();
    }





}