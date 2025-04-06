package tn.esprit.Services;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    // Twilio credentials (replace with your actual credentials)
    private final String ACCOUNT_SID = "ACd98915c3c6d8b34083e57f2da6fd5bc8"; // Replace with your Twilio Account SID
    private final String AUTH_TOKEN = "6eacb8cbe0fe9b4ab056c9f4db576c3f"; // Replace with your Twilio Auth Token
    private final String FROM_PHONE_NUMBER = "+12252636305"; // Replace with your Twilio phone number

    public SmsService() {
        // Initialize Twilio with your Account SID and Auth Token
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public String sendSms(String phoneNumber, String message) {
        try {
            // Send SMS using Twilio API
            Message messageSent = Message.creator(
                    new com.twilio.type.PhoneNumber(phoneNumber), // To phone number
                    new com.twilio.type.PhoneNumber(FROM_PHONE_NUMBER), // From phone number (Twilio number)
                    "Your verification pin is: " + message // The SMS body
            ).create();

            return "Message sent successfully: " + messageSent.getSid();
        } catch (ApiException e) {
            // Handle Twilio API exception
            return "Error sending message: " + e.getMessage();
        }
    }
}
