package com.esprit.spring.PiProject.Services;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    /*
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendApplicationAcceptedEmail(String toEmail, String userName, String offerTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Application Accepted - " + offerTitle);
            message.setText("Dear " + userName + ",\n\n" +
                    "Congratulations! Your application for the offer '" + offerTitle + "' has been accepted.\n\n" +
                    "Best regards,\nYour Company");

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MailException e) {
            e.printStackTrace();
            System.err.println("Error sending email: " + e.getMessage());
        }
    }*/
}
//mailing
