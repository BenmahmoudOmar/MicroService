package tn.esprit.utility;

import org.springframework.web.client.RestTemplate;
import tn.esprit.payload.response.LocationResponse;

public class GeolocationUtil {
    private static final String GEOLOCATION_API_URL = "http://ip-api.com/json/";

    public static LocationResponse getLocationFromIP(String ip) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GEOLOCATION_API_URL + ip;
            String jsonResponse = restTemplate.getForObject(url, String.class);  // Get raw response
            System.out.println(jsonResponse);  // Log the raw JSON response

            // Now, map to LocationResponse
            return restTemplate.getForObject(url, LocationResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
