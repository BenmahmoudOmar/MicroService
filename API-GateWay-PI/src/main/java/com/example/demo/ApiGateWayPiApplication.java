package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGateWayPiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGateWayPiApplication.class, args);
	}
	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("PiProject", r -> r.path("/piproj/offers/**")
						.uri("lb://PiProject")).route("PiProject", r -> r.path("/piproj/Application/**")
						.uri("lb://PiProject")).route("PiProject", r -> r.path("/piproj/Comment/**")
						.uri("lb://PiProject")).build();
	}

}
