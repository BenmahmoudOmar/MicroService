package tn.esprit.payload;

public class TokenEmailPair {
    private String token;
    private String email;

    public TokenEmailPair() {
    }

    public TokenEmailPair(String token, String email) {
        this.token = token;
        this.email = email;
    }

    // Getters and Setters

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
