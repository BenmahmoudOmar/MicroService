package tn.esprit;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
	@Bean
	public KeycloakSpringBootConfigResolver
	keycloakSpringBootConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	static Keycloak keycloak=null;

	public final static String serverUrl = "http://localhost:8080";
	public final static String realm = "ruby";
	public final static String clientId = "gateway-service";
	public final static String clientSecret = "w2l5yKBsrEnJLXjCMBx1meQuHJSm5Tl1";
	final static String userName = "admin";
	final static String password = "123123";
	public KeycloakConfig() {
	}


	@Bean
	public static Keycloak getInstance() {
		if (keycloak == null) {
			keycloak = KeycloakBuilder.builder()
					.serverUrl(serverUrl)
					.realm(realm)
					.grantType(OAuth2Constants.PASSWORD)
					.username(userName)
					.password(password)
					.clientId(clientId)
					.clientSecret(clientSecret)
					.resteasyClient(new ResteasyClientBuilderImpl()
							.connectionPoolSize(10)
							.build())
					.build();
		}
		return keycloak;
	}
}