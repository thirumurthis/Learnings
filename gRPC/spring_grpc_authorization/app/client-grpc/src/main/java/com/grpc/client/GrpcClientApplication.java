package com.grpc.client;

import com.app.client.grpc.AppRequest;
import com.app.client.grpc.AppServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.grpc.client.interceptor.security.TokenSupplier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

//@ImportGrpcClients(basePackageClasses= AppServiceGrpc.class, target = "localhost:9090" )
@ImportGrpcClients(basePackageClasses= AppServiceGrpc.class, target = "service.local:9090" )
@SpringBootApplication
public class GrpcClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(GrpcClientApplication.class);

	static void main(String[] args) {
		SpringApplication.run(GrpcClientApplication.class, args);
	}

	private final SecurityContextHolderStrategy strategy = SecurityContextHolder
			.getContextHolderStrategy();

	@Bean
	GrpcChannelBuilderCustomizer<?> grpcChannelBuilderCustomizer(OAuth2AuthorizedClientManager authorizedClientManager) {

		logger.info("$$$$$$$$$$$$$ call came here");
		TokenSupplier supplier = () -> this.token(authorizedClientManager);
		return GrpcChannelBuilderCustomizer
				//.matching(".*localhost:9090.*", builder -> builder.intercept(
				.matching(".*service.local:9090.*", builder -> builder.intercept(
							new BearerTokenAuthenticationInterceptor(supplier)));

	   }

	String token(OAuth2AuthorizedClientManager authorizedClientManager) {

		logger.info("entering the token flow....******************++++++");
		//*
		//from the git repo - https://github.com/joshlong-attic/2026-04-15-springio-bootiful-spring-grpc/blob/main/client/src/main/resources/application.properties
		var authenticatedUser = this.strategy.getContext().getAuthentication();
		if (authenticatedUser instanceof OAuth2AuthenticationToken auth2AuthenticationToken) {
			var clientId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
			//logger.info("inside token checking clientid ---- *********************** {} ", clientId);
			var oauthAuthorizationRequest = OAuth2AuthorizeRequest
					.withClientRegistrationId(clientId)
					.principal(authenticatedUser)
					.build();
			var authorized = authorizedClientManager.authorize(oauthAuthorizationRequest);
			//logger.info("[TOKEN] @@@@@@@@@@@@@@@@--- {}", authorized);
			printToken(Objects.requireNonNull(authorized).getAccessToken().getTokenValue());
			return Objects.requireNonNull(authorized).getAccessToken().getTokenValue();
		}
		return null;
	}

	private void printToken(String jwtTokenValue ){

        // Decode and print the claims to your console for easier reading
		try {
			var decoder = java.util.Base64.getUrlDecoder();
			String[] chunks = jwtTokenValue.split("\\.");
			if (chunks.length > 1) {
				String payload = new String(decoder.decode(chunks[1]));
				logger.info("[JWT CLAIMS PAYLOAD] {}", payload);
			}
		} catch (Exception e) {
			logger.warn("Failed to decode token payload layout for logs: {}", e.getMessage());
		}
	}
} // end of the class

@Controller
@ResponseBody
class GrpcClientController {
   private final AppServiceGrpc.AppServiceBlockingStub stub;

    GrpcClientController(AppServiceGrpc.AppServiceBlockingStub stub) {
        this.stub = stub;
    }

	@GetMapping("/")
	String pong(){
		return this.stub.msg(AppRequest.newBuilder()
						.setName("gRPCClientUser")
				.build())
				.getMessage();
	}
}
