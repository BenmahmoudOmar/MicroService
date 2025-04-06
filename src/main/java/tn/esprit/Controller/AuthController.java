package tn.esprit.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jakarta.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;


import tn.esprit.Entities.*;
import tn.esprit.KeycloakConfig;
import tn.esprit.Repository.EntrepriseRepository;
import tn.esprit.Repository.JobSeekerRepository;
import tn.esprit.Repository.RoleRepository;
import tn.esprit.Repository.UserRepository;
import tn.esprit.Services.MailSendService;
import tn.esprit.Services.SmsService;
import tn.esprit.Services.UserDetailsServiceImpl;
import tn.esprit.payload.SmsRequest;
import tn.esprit.payload.TokenEmailPair;
import tn.esprit.payload.request.LoginRequest;
import tn.esprit.payload.request.SignupRequest;
import tn.esprit.payload.request.UserLocation;
import tn.esprit.payload.response.JwtResponse;
import tn.esprit.payload.response.LocationResponse;
import tn.esprit.payload.response.MessageResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import tn.esprit.utility.GeolocationUtil;
import tn.esprit.utility.LocationStorageUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;

import java.util.Collections;



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
  Keycloak keycloak;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobSeekerRepository jobSeekerRepository;

    @Autowired
    EntrepriseRepository entrepriseRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;


    @Autowired
    MailSendService mailSendService;



    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JavaMailSender mailSender;


    @Autowired
    private SmsService smsService;



    public static TokenEmailPair tokenEmailPair;

    static {
        tokenEmailPair = new TokenEmailPair();  // Initialize the static field
    }

    @Autowired
    LocationStorageUtil LocationStorageUtil;

    @Autowired
    AnomalyController AnomalyController;
    private static final String API_KEY = "03b1c2c4dd8049f6ab4c1445ffa14316"; // Replace with your API key





    @GetMapping("/job-seekers")
    public List<User> getUsersByRoleUser() {
        return userDetailsService.getUsersByRoleUser();
    }

    @GetMapping("/entreprises")
    public List<User> getUsersByRoleEntreprise() {
        return userDetailsService.getUsersByRoleEntreprise();
    }




    @GetMapping("/mail")
    public void sendEmail(@RequestParam String toEmail,@RequestParam String body,@RequestParam String subject)
    { mailSendService.sendEmail(toEmail,body,subject);

    }

    //http://localhost:8085/SpringMVC/api/auth/signin



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,HttpServletRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String clientIp) throws JSONException {

        try {

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Error: Username not found."));


            AccessTokenResponse tokenResponse;
            try {
                // Get Keycloak instance from configuration
                Keycloak keycloak = KeycloakConfig.getInstance();

                // Authenticate the user with username and password
                Keycloak tempKeycloak = KeycloakBuilder.builder()
                        .serverUrl(KeycloakConfig.serverUrl)
                        .realm(KeycloakConfig.realm)
                        .grantType(OAuth2Constants.PASSWORD)
                        .clientId(KeycloakConfig.clientId)
                        .clientSecret(KeycloakConfig.clientSecret)
                        .username(loginRequest.getUsername())  // User input
                        .password(loginRequest.getPassword())  // User input
                        .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
                        .build();

                // Get Access Token
             tokenResponse = tempKeycloak.tokenManager().getAccessToken();

                // Return Token to Client


            } catch (Exception e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Invalid username or password"));            }


            if (user.getIsVerified() == 0) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Please verify your email first."));
            }


            // Get real IP (if behind proxy)

            try {
                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.getForObject("https://api64.ipify.org?format=json", String.class);

                // Parse JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response);
                clientIp = jsonNode.get("ip").asText();

                System.out.println("Public IP: " + clientIp);
            } catch (Exception e) {
                System.err.println("Failed to fetch public IP: " + e.getMessage());
            }

            String userAgent = request.getHeader("User-Agent");

            // Call the User Agent Parser API to get detailed info
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode extractedInfo = null;
            try {
                String encodedUserAgent = URLEncoder.encode(userAgent, StandardCharsets.UTF_8.toString());
                String url = "https://api.ipgeolocation.io/user-agent?ua=" + encodedUserAgent + "&apiKey=" + API_KEY;
                RestTemplate restTemplate = new RestTemplate();
                String userAgentResponse = restTemplate.getForObject(url, String.class);
                JsonNode fullUAInfo = objectMapper.readTree(userAgentResponse);
                //System.out.println("Full Parsed User-Agent Details: " + fullUAInfo.toPrettyString());

                // Extract only the necessary fields for AI use
                extractedInfo = objectMapper.createObjectNode();
                extractedInfo.put("Username", loginRequest.getUsername());

                extractedInfo.put("userAgentString", fullUAInfo.path("userAgentString").asText());
                extractedInfo.put("browserName", fullUAInfo.path("name").asText());
                extractedInfo.put("browserVersion", fullUAInfo.path("version").asText());
                extractedInfo.put("deviceName", fullUAInfo.path("device").path("name").asText());
                extractedInfo.put("deviceType", fullUAInfo.path("device").path("type").asText());
                extractedInfo.put("osName", fullUAInfo.path("operatingSystem").path("name").asText());
                extractedInfo.put("osVersion", fullUAInfo.path("operatingSystem").path("version").asText());
                extractedInfo.put("clientIp", clientIp);
                // You can add more fields if needed

                // Save the extracted info into a JSON file

            } catch (Exception e) {
                System.err.println("Failed to parse User-Agent: " + e.getMessage());
            }

            // Continue with your login logic...

            // Continue with your login logic...


            System.out.println(clientIp);
            RestTemplate restTemplate = new RestTemplate();


            // Get user's location from IP
            LocationResponse location = GeolocationUtil.getLocationFromIP(clientIp);
            if (location == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Could not determine location."));
            }

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            String city = location.getCity();
            String country = location.getCountry();


            File file = new File(loginRequest.getUsername() + "locations.json");

            LocationStorageUtil.saveUserAgentInfo(objectMapper, extractedInfo);


            ResponseEntity<String> anomalyResponse = AnomalyController.predictAnomaly();

            // Check if the anomalyResponse contains an anomaly
            JSONObject responseBody = new JSONObject(anomalyResponse.getBody());
            System.out.println(responseBody);


            if (!file.exists() || file.length() == 0) {
                LocationStorageUtil.saveLocation(user.getUsername(), latitude, longitude, city, country);
            } else {

                // Read last stored location
                UserLocation savedLocation = LocationStorageUtil.readLocation(user.getUsername());


                if ((!savedLocation.getCity().equals(city) || !savedLocation.getCountry().equals(country)) && (responseBody.has("Anomaly") && responseBody.getInt("Anomaly") == 1)) {
                    LocationStorageUtil.saveLocationNoOverwrite(user.getUsername(), latitude, longitude, city, country);
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Login from a new location detected And Another Device! Please verify via 2FA."));
                } else if (!savedLocation.getCity().equals(city) || !savedLocation.getCountry().equals(country)) {
                    LocationStorageUtil.saveLocationNoOverwrite(user.getUsername(), latitude, longitude, city, country);
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Login from a new location detected ! Please verify via 2FA."));
                } else {
                    LocationStorageUtil.saveLocation(user.getUsername(), latitude, longitude, city, country);
                }


                if (responseBody.has("Anomaly") && responseBody.getInt("Anomaly") == 1) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Login from Another Device."));
                }
            }


            // If Anomaly == 1, return a bad request with a custom message


            // If the user logs in from a different city/country, trigger 2FA


            return ResponseEntity.ok(new JwtResponse(tokenResponse.getToken(), user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), user.getIsVerified(), user.getPhone()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Password  Incorrect"));            }

    }



    //http://localhost:8085/SpringMVC/api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {


        String userId;
        try {
            // Get the Keycloak Realm
            RealmResource realmResource = keycloak.realm("ruby"); // Use your realm name
            UsersResource usersResource = realmResource.users();

            // Create User Representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setEnabled(true);
            user.setEmailVerified(false);


            if (signUpRequest.getRole() != null && signUpRequest.getRole().contains("user")) {
                Map<String, List<String>> attributes = new HashMap<>();
                attributes.put("resume", Collections.singletonList("NULL"));   // Empty string
                attributes.put("skills", Collections.singletonList("NULL"));   // Empty string
                attributes.put("education", Collections.singletonList("NULL")); // Empty string

                user.setAttributes(attributes);
                System.out.println("Attributes: " + attributes);  // Log to check if attributes are set properly

            }

            if (signUpRequest.getRole() != null && signUpRequest.getRole().contains("entreprise")) {
                Map<String, List<String>> attributes = new HashMap<>();

                attributes.put("companyDescription", Collections.singletonList("NULL"));
                attributes.put("address", Collections.singletonList("NULL"));
                attributes.put("contactNumber", Collections.singletonList("NULL"));
                attributes.put("logo", Collections.singletonList("NULL"));
                attributes.put("industry", Collections.singletonList("NULL"));
                attributes.put("companyWebsite", Collections.singletonList("NULL"));

                // Set attributes for the user
                user.setAttributes(attributes);
                System.out.println("Entreprise Attributes: " + attributes);  // Log to check if attributes are set properly
            }

            // Create User in Keycloak
            Response response = usersResource.create(user);

            // Log the response status
            System.out.println("Response Status: " + response.getStatus());
            System.out.println("Response Status Info: " + response.getStatusInfo());

            if (response.getStatus() != 201) {
                return ResponseEntity.status(response.getStatus()).body("User creation failed: " + response.getStatusInfo());
            }

            // Get Created User ID
            userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Set User Password
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(signUpRequest.getPassword());

            usersResource.get(userId).resetPassword(passwordCred);


            if (signUpRequest.getRole() != null) {
                for (String roleName : signUpRequest.getRole()) {
                    RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(role));
                }
            }


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }


        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()), userId);

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "Admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "entreprise":
                        Role modRole = roleRepository.findByName(ERole.ROLE_ENTREPRISE)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        user.setPhone(signUpRequest.getPhone());
        userRepository.save(user);
        String token = UUID.randomUUID().toString();

        // Store the token with the email in the map
        tokenEmailPair.setToken(token);
        tokenEmailPair.setEmail(user.getEmail());
        // Create the verification URL using the token
        String verificationUrl = "http://localhost:4200/front-office/verify?token=" + token;

        // Send the verification email
        mailSendService.sendVerificationEmail(user.getEmail(), verificationUrl);

        if (signUpRequest.getRole().contains("entreprise")) {
            Entreprise J = new Entreprise();
            J.setUser(userRepository.getById(user.getId()));
            entrepriseRepository.save(J);

        } else {
            JobSeeker J = new JobSeeker();
            J.setUser(userRepository.getById(user.getId()));
            jobSeekerRepository.save(J);

        }


        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }



    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {

        // 1. Retrieve the email from the stored token
        if (!token.equals(tokenEmailPair.getToken())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or expired token"));
        }

        String email = tokenEmailPair.getEmail(); // Get the email linked to the token

        // 2. Get Keycloak instance
        Keycloak keycloak = KeycloakConfig.getInstance();
        UsersResource usersResource = keycloak.realm(KeycloakConfig.realm).users();

        // 3. Retrieve the user based on email
        List<UserRepresentation> users = usersResource.search(email, 0, 1);
        if (users.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found"));
        }

        UserRepresentation userRep = users.get(0);
        UserResource userResource = usersResource.get(userRep.getId());

        // 4. Update user in Keycloak as verified
        userRep.setEmailVerified(true);
        userResource.update(userRep);
        // For simplicity, we use email as the token (UUID), but you should store and retrieve this properly.
        User user = userRepository.findByEmail(tokenEmailPair.getEmail()); // Here email is being used as the token

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User not found or invalid token"));
        }

        // Mark the user as verified
        user.setIsVerified(1);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
    }


    @PostMapping("/send")
    public String sendSms(@RequestBody SmsRequest smsRequest) throws IOException {
        return smsService.sendSms(smsRequest.getPhone(), smsRequest.getMessage());
    }

    @PostMapping("/verify-captcha")
    public ResponseEntity<?> verifyCaptcha(@RequestParam String response) {
        RestTemplate restTemplate = new RestTemplate();
        String secretKey = "ES_9959481259f046a99675225bc98e92a3";  // Replace with your actual secret key
        String verifyUrl = "https://api.hcaptcha.com/siteverify";

        Map<String, String> params = new HashMap<>();
        params.put("secret", secretKey);
        params.put("response", response);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(verifyUrl, params, Map.class);

        return ResponseEntity.ok(responseEntity.getBody());
    }






    @DeleteMapping("deleteUser/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userDetailsService.deleteUser(id);
    }

    @GetMapping("list/{id}")
    public User listUser(@PathVariable("id") Long id) {
        return userDetailsService.listUser(id);
    }

    @GetMapping("findByUsername/{username}")
    public   Optional<User> findByUsername(@PathVariable("username") String username)
    {
        return userDetailsService.findByUsername(username);
    }

    @PutMapping("updateUser")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        return userDetailsService.updateUser(user);
    }

    @PutMapping("updateUserPassword/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody String password) {
        return userDetailsService.updateUserPassword(id,password);
    }


    @GetMapping("ListUser")
    public List<User> getList() {
        return userDetailsService.getList();
    }

    @GetMapping("findByUsernameAndPassword/{username}/{password}")
    public   Optional<User> findByUsernameAndPassword(@PathVariable("username") String username,@PathVariable("password") String password)
    {
        return userDetailsService.findByUsernameAndPassword(username,password);
    }



    @PostMapping("/signin1")
    public ResponseEntity<?> authenticateUser1(
            @Valid @RequestBody LoginRequest loginRequest,HttpServletRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String clientIp) throws JSONException {

        // Get real IP (if behind proxy)



        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject("https://api64.ipify.org?format=json", String.class);

            // Parse JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            clientIp = jsonNode.get("ip").asText();

            System.out.println("Public IP: " + clientIp);
        } catch (Exception e) {
            System.err.println("Failed to fetch public IP: " + e.getMessage());
        }

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));


        AccessTokenResponse tokenResponse;
        try {
            // Get Keycloak instance from configuration
            Keycloak keycloak = KeycloakConfig.getInstance();

            // Update Keycloak credentials with user input
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(KeycloakConfig.serverUrl)
                    .realm(KeycloakConfig.realm)
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientId(KeycloakConfig.clientId)
                    .clientSecret(KeycloakConfig.clientSecret)
                    .username(loginRequest.getUsername())  // User input
                    .password(loginRequest.getPassword())  // User input
                    .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
                    .build();

            // Get Access Token
            tokenResponse = keycloak.tokenManager().getAccessToken();

            // Return Token to Client
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", tokenResponse.getToken());
            response.put("refreshToken", tokenResponse.getRefreshToken());
            response.put("expiresIn", tokenResponse.getExpiresIn());


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }














        // If the user logs in from a different city/country, trigger 2FA

        return ResponseEntity.ok(new JwtResponse(tokenResponse.getToken(), user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), user.getIsVerified(), user.getPhone()));
    }



}