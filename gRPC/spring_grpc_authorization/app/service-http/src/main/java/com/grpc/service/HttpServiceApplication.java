package com.grpc.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@SpringBootApplication
public class HttpServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(HttpServiceApplication.class, args);
	}

	@Bean
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				// Equivalent to security.oauth2ResourceServer(...)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
				// Equivalent to .authorizeRequests(a -> a.allRequests().authenticated())
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.build();
	}
}

@RestController
class HelloController {

	@GetMapping("/")
	public String index() {
		return "[http] Responding Pong to "+
				(Objects.requireNonNull(SecurityContextHolder
					.getContext().getAuthentication()).getName()+"!"
		);
	}
}