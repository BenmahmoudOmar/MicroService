package tn.esprit.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationResponse {
    private String country;
    private String regionName;
    private String city;
    @JsonProperty("lat")  // Specify the exact JSON key

    private double lat;
    @JsonProperty("lon")  // Specify the exact JSON key

    private double lon;

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }
}
