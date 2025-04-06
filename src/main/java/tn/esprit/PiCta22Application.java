package tn.esprit;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@SpringBootApplication
public class PiCta22Application {

	public static void main(String[] args) {
		SpringApplication.run(PiCta22Application.class, args);


	}

}
