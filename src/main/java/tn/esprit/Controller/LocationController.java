package tn.esprit.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import tn.esprit.payload.request.UserLocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private static final String FILE_PATH = "user_locations.json";

    @DeleteMapping("/remove-first")
    public String removeFirstLocation(@RequestParam String username) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(username + "locations.json");
            if (!file.exists()) {
                return "File does not exist.";
            }

            List<UserLocation> locations = new ArrayList<>();

            // Read the content of the file
            JsonNode rootNode = objectMapper.readTree(file);

            if (rootNode.isArray()) {
                // If it's an array, convert to List<UserLocation>
                locations = objectMapper.readValue(file, new TypeReference<List<UserLocation>>() {});
            } else {
                // If it's a single object, wrap it in a list
                UserLocation singleLocation = objectMapper.treeToValue(rootNode, UserLocation.class);
                locations.add(singleLocation);
            }

            if (locations.size() <= 1) {
                return "No more locations to remove (must have more than 1 location).";
            }

            // Remove first entry only if there are more than 1 location
            locations.remove(0);

            // Save updated list back to file
            objectMapper.writeValue(file, locations);
            return "First location removed successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error removing first location.";
        }
    }






}
