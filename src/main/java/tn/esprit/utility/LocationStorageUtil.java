package tn.esprit.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import tn.esprit.payload.request.UserLocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class LocationStorageUtil {


    private static final String USER_AGENT_INFO_FILE = "user_agent_info.json";

    public void saveUserAgentInfo(ObjectMapper objectMapper, ObjectNode newRecord) {
        File file = new File(USER_AGENT_INFO_FILE);
        ArrayNode records;
        try {
            if (file.exists()) {
                // Read the existing JSON array from file
                records = (ArrayNode) objectMapper.readTree(file);
            } else {
                // Create a new JSON array
                records = objectMapper.createArrayNode();
            }
            // Append the new record
            records.add(newRecord);
            // Write the updated array back to file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, records);
            System.out.println("User-Agent information saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving User-Agent information: " + e.getMessage());
        }
    }

    // This method saves location to a user-specific file
    public static void saveLocation(String username, double latitude, double longitude, String city, String country) {
        ObjectMapper objectMapper = new ObjectMapper();
        UserLocation location = new UserLocation(username, latitude, longitude, city, country);
        File file = new File(username + "locations.json");

        try {
            objectMapper.writeValue(file, location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method saves location without overwriting, using a user-specific file
    public static void saveLocationNoOverwrite(String username, double latitude, double longitude, String city, String country) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(username + "locations.json");

        try {
            List<UserLocation> locations = new ArrayList<>();

            // Check if file exists and is not empty
            if (file.exists() && file.length() > 0) {
                JsonNode rootNode = objectMapper.readTree(file);

                if (rootNode.isArray()) {
                    locations = objectMapper.readValue(file, new TypeReference<List<UserLocation>>() {});
                } else {
                    UserLocation singleLocation = objectMapper.treeToValue(rootNode, UserLocation.class);
                    locations.add(singleLocation);
                }
            }

            // ✅ Check if the city & country combination already exists
            boolean exists = locations.stream()
                    .anyMatch(loc -> loc.getCity().equalsIgnoreCase(city) &&
                            loc.getCountry().equalsIgnoreCase(country));

            if (!exists) {
                // Add new location only if the city & country do not exist
                UserLocation newLocation = new UserLocation(username, latitude, longitude, city, country);
                locations.add(newLocation);

                // Save back as an array
                objectMapper.writeValue(file, locations);
                System.out.println("Location saved successfully.");
            } else {
                System.out.println("Location already exists for city: " + city + ", country: " + country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method reads user-specific location file
    public static UserLocation readLocation(String username) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(username + "locations.json");

        if (!file.exists() || file.length() == 0) {
            return null;
        }

        try {
            // ✅ Read the file as a generic JSON tree
            JsonNode rootNode = objectMapper.readTree(file);

            if (rootNode.isArray()) {
                // ✅ If the file contains an array, iterate through elements
                for (JsonNode node : rootNode) {
                    if (node.has("username") && node.get("username").asText().equals(username)) {
                        return objectMapper.treeToValue(node, UserLocation.class);
                    }
                }
            } else if (rootNode.isObject()) {
                // ✅ If the file contains a single object, check it directly
                if (rootNode.has("username") && rootNode.get("username").asText().equals(username)) {
                    return objectMapper.treeToValue(rootNode, UserLocation.class);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // This method ensures only the first entry is kept in each user's location file
    public void keepOnlyFirstEntry() {
        ObjectMapper objectMapper = new ObjectMapper();
        File folder = new File("locations/"); // Assuming all user files are in the "locations/" directory

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Location storage directory does not exist.");
            return;
        }

        File[] userFiles = folder.listFiles((dir, name) -> name.endsWith("locations.json"));
        if (userFiles == null || userFiles.length == 0) {
            System.out.println("No user location files found.");
            return;
        }

        // Iterate through each user file and keep only the first entry
        for (File file : userFiles) {
            try {
                List<UserLocation> locations = new ArrayList<>();

                // Read the content of the file
                JsonNode rootNode = objectMapper.readTree(file);
                if (rootNode.isArray()) {
                    // If the file contains an array, iterate and add to locations list
                    locations = objectMapper.readValue(file, new TypeReference<List<UserLocation>>() {});
                } else {
                    // If the file contains a single object, wrap it in a list
                    UserLocation singleLocation = objectMapper.treeToValue(rootNode, UserLocation.class);
                    locations.add(singleLocation);
                }

                // Keep only the first entry
                if (!locations.isEmpty()) {
                    objectMapper.writeValue(file, locations.subList(0, 1)); // Keep only the first entry
                    System.out.println("Kept only the first location entry for: " + file.getName());
                }

            } catch (IOException e) {
                System.err.println("Error processing file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

}
