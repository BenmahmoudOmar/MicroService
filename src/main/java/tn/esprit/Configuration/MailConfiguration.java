package tn.esprit.Configuration;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp-relay.brevo.com"); // Serveur SMTP Brevo
		mailSender.setPort(587); // Port TLS

		mailSender.setUsername("855381002@smtp-brevo.com"); // Identifiant SMTP
		mailSender.setPassword("b5zjfnKRVtc6XpQm"); // Mot de passe SMTP (ne pas partager en public)

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true"); // Active TLS
		props.put("mail.debug", "true"); // Debugging (optionnel)

		return mailSender;
	}

}
