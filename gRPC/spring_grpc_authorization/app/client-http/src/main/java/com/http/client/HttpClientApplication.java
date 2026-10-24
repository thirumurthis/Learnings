package com.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

//Below is similar to the way we use HTTP client definition in spring
@ImportHttpServices(group = "hello-service", types = HelloWebClient.class)
@SpringBootApplication
public class HttpClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientApplication.class);

	static void main(String[] args) {
		SpringApplication.run(HttpClientApplication.class, args);
	}


	@Bean
	public RestClientHttpServiceGroupConfigurer helloServiceSecurityConfigurer(
			OAuth2AuthorizedClientManager authorizedClientManager) {

		// Native interceptor automatically extracts, carries context, and refreshes tokens
		OAuth2ClientHttpRequestInterceptor oauth2Interceptor =
				new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

		oauth2Interceptor.setClientRegistrationIdResolver(request -> "spring-http");

		return groups -> groups
				.filterByName("hello-service")
				// FIX 2: Change method to .forEachClient
				.forEachClient((group, clientBuilder) -> {
					clientBuilder.requestInterceptor(oauth2Interceptor);
					clientBuilder.requestInterceptor(rawTokenLoggingInterceptor());
				});

	}
	// Part B: Intercept and secure outbound RestClient traffic to the external service
	@Bean
	public RestClient  restClientConfiguration(OAuth2AuthorizedClientManager authorizedClientManager) {

		OAuth2ClientHttpRequestInterceptor requestInterceptor =
				new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

		return RestClient.builder()
				.requestInterceptor(requestInterceptor)
				.build();
	}

	private ClientHttpRequestInterceptor rawTokenLoggingInterceptor() {
		return (request, body, execution) -> {
			// Extract the Authorization header added by the OAuth2 interceptor
			String authHeader = request.getHeaders().getFirst("Authorization");
			String BEARERSTRING="Bearer ";

			if (authHeader != null && authHeader.startsWith(BEARERSTRING)) {
				String rawToken = authHeader.substring(BEARERSTRING.length());
				//logger.info("Raw OAuth2 Access Token: {}", rawToken);
				printToken(rawToken );
			} else {
				logger.warn("No Bearer token found in request headers yet!");
			}

			return execution.execute(request, body);
		};
	}
	private void printToken(String jwtTokenValue ){

        // Decode and print the claims to your console for easier reading
		try {
			java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
			String[] chunks = jwtTokenValue.split("\\.");
			if (chunks.length > 1) {
				String payload = new String(decoder.decode(chunks[1]));
				logger.info("[JWT CLAIMS PAYLOAD] {}", payload);
			}
		} catch (Exception e) {
			logger.warn("Failed to decode token payload layout for logs: {}", e.getMessage());
		}
	}
}


@RestController
class HttpClientController {

	private final HelloWebClient helloWebClient;

	public HttpClientController(HelloWebClient helloWebClient) {
		this.helloWebClient = helloWebClient;
	}

	@GetMapping("/")
	public String getInformation() {
		// Behind the scenes, the interceptor automatically applies the Auth header here
		return helloWebClient.getPongData();
	}
}
