package tn.esprit.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import tn.esprit.Entities.User;
import tn.esprit.Repository.UserRepository;
import tn.esprit.payload.response.MessageResponse;

import static tn.esprit.Controller.AuthController.tokenEmailPair;

@Service
public class MailSendService {
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	UserRepository userRepository;
	
	@Async
	public void sendEmail(String toEmail,String body,String subject) {
			SimpleMailMessage message = new SimpleMailMessage();

			message.setFrom("yassinehemedi2@gmail.com");
			message.setTo(toEmail);
			message.setText(body);
			message.setSubject(subject);

			mailSender.send(message);
			System.out.println("Mail Send ...");
			}


	public void sendVerificationEmail(String toEmail, String verificationUrl) {
		String subject = "Email Verification";
		String message = "Please verify your email by clicking the following link: " + verificationUrl;

		SimpleMailMessage email = new SimpleMailMessage();
		email.setFrom("Internify <yassinehemedi2@gmail.com>");        email.setTo(toEmail);
		email.setSubject(subject);
		email.setText(message);

		mailSender.send(email);
	}




}