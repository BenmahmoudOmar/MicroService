package tn.esprit.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import tn.esprit.Entities.User;
import tn.esprit.KeycloakConfig;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.annotation.Transactional;


import tn.esprit.Entities.User;
import tn.esprit.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import tn.esprit.payload.response.MessageResponse;


import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }


    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    public User listUser(Long id){
        return userRepository.findById(id).orElse(null);
    }

    @Autowired
    private PasswordEncoder encoder;  // Inject the PasswordEncoder

    public ResponseEntity<?> updateUser(User user) {

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Username cannot be empty"));
        }

        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid email format"));
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(new MessageResponse("Password must be at least 6 characters long"));
        }


        User updatedUser = user;
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (existingUser.isPresent()) {
            // Update the user if it exists
            updatedUser = existingUser.get();

            updatedUser.setUsername(user.getUsername());
            updatedUser.setEmail(user.getEmail());

            // Encode the password before saving
            String encodedPassword = encoder.encode(user.getPassword());
            updatedUser.setPassword(encodedPassword);
        }

        // **Update in Keycloak**
        try {
            Keycloak keycloak = KeycloakConfig.getInstance();
            UsersResource usersResource = keycloak.realm(KeycloakConfig.realm).users();

            // Fetch the user from the database

            // Get the Keycloak ID from the database
            String keycloakId = existingUser.get().getKeycloakId();

            if (keycloakId == null || keycloakId.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("User does not have a Keycloak ID"));
            }

            // Find the Keycloak user by ID instead of email
            UserResource keycloakUserResource = usersResource.get(keycloakId);
            UserRepresentation keycloakUser;


                keycloakUser = keycloakUserResource.toRepresentation();


            // **Update user attributes in Keycloak**
            keycloakUser.setUsername(user.getUsername());
            keycloakUser.setEmail(user.getEmail());

            // Update user in Keycloak
            keycloakUserResource.update(keycloakUser);

            // **Update Password in Keycloak**
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(user.getPassword()); // Raw password (Keycloak handles encoding)
                passwordCred.setTemporary(false);

                keycloakUserResource.resetPassword(passwordCred);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Username or email duplicated"));
        }





        userRepository.save(updatedUser);

        return ResponseEntity.ok(new MessageResponse("User updated successfully"));

    }

    public ResponseEntity<?> updateUserPassword(Long id,String user) {



        User updatedUser = new User();
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            // Update the user if it exists
            updatedUser = existingUser.get();



            // Encode the password before saving
            String encodedPassword = encoder.encode(user);
            updatedUser.setPassword(encodedPassword);
        }

        // **Update in Keycloak**
        try {
            Keycloak keycloak = KeycloakConfig.getInstance();
            UsersResource usersResource = keycloak.realm(KeycloakConfig.realm).users();

            // Fetch the user from the database

            // Get the Keycloak ID from the database
            String keycloakId = existingUser.get().getKeycloakId();

            if (keycloakId == null || keycloakId.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("User does not have a Keycloak ID"));
            }

            // Find the Keycloak user by ID instead of email
            UserResource keycloakUserResource = usersResource.get(keycloakId);
            UserRepresentation keycloakUser;


            keycloakUser = keycloakUserResource.toRepresentation();



            // Update user in Keycloak
            keycloakUserResource.update(keycloakUser);

            // **Update Password in Keycloak**
            if (user != null && !user.isEmpty()) {
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(user); // Raw password (Keycloak handles encoding)
                passwordCred.setTemporary(false);

                keycloakUserResource.resetPassword(passwordCred);
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error while updating password"));
        }





        userRepository.save(updatedUser);

        return ResponseEntity.ok(new MessageResponse("User updated successfully"));

    }


    public Optional<User> findByUsername(String username)
    {return userRepository.findByUsername(username);}


    public Optional<User> findByUsernameAndPassword(String username, String plainPassword) {
        Optional<User> user = userRepository.findByUsername(username);

        // If the user is found, check if the password matches the hashed password
        if (user.isPresent()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(plainPassword, user.get().getPassword())) {
                return user;
            }
        }

        return Optional.empty();  // Return empty if user not found or password doesn't match
    }

    public List<User> getList(){
      return    userRepository.findAll();}



    public void save2FASecret(String username, String secretKey) {
        Optional<User> user = userRepository.findByUsername(username);  // Find the user by username
        if (user.isPresent()) {
            user.get().setTwoFactorSecret(secretKey);  // Assuming you have a 'twoFactorSecret' field in your User entity
            userRepository.save(user.get());  // Save the updated user
        }
    }

    // Retrieve the 2FA secret from the database
    public String get2FASecret(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        // Find the user by username
        return user.get().getTwoFactorSecret();  // Return the secret or null if not found
    }


    public List<User> getUsersByRoleUser() {
        return userRepository.findUsersByRoleUser();
    }

    public List<User> getUsersByRoleEntreprise() {
        return userRepository.findUsersByRoleEntreprise();
    }







}