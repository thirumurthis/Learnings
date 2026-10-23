package com.grpc.client;

import com.app.service.grpc.AppRequest;
import com.app.service.grpc.AppServiceGrpc;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.GrpcChannelBuilderCustomizer;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor;
import org.springframework.grpc.client.interceptor.security.TokenRelayTokenSupplier;
import org.springframework.grpc.client.interceptor.security.TokenSupplier;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2Error;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.util.Objects;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

//Below is similar to the way we use HTTP client definition in spring
//@ImportHttpService(basePackageClasses = AppService.class)
//@ImportGrpcClients(basePackageClasses= AppServiceGrpc.class, target = "localhost:9090" )
@ImportGrpcClients(basePackageClasses= AppServiceGrpc.class, target = "service.local:9090" )
@SpringBootApplication
public class ClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

	static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	/*
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						// Option A: If your gRPC call happens on "GET /", require authentication first
						.requestMatchers("/").authenticated()

						// Option B: If you want / to be public, use permitAll() instead, but
						// you must ensure the gRPC stub is only called when an authenticated user is present
						//.requestMatchers("/").permitAll()

						.anyRequest().authenticated()
				)
				// Enable OAuth2 Login login flow so Spring Security handles the login handshake
				.oauth2Login(oauth2 -> {});

		return http.build();
	}
	 //*/
/*	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/").authenticated() // Forces login before handling the request
						.anyRequest().authenticated()
				)
				.oauth2Login(org.springframework.security.config.Customizer.withDefaults());

		return http.build();
	}*/

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
		//return (target, builder) -> {
		//	builder.intercept(new BearerTokenAuthenticationInterceptor(supplier));
		//};
		//return GrpcChannelBuilderCustomizer.matching(name -> true, builder -> {
		//	logger.info("Intercepting channel configuration for: " + builder);
		//	return builder.intercept(new BearerTokenAuthenticationInterceptor(supplier));
		//});
	   }

	String token(OAuth2AuthorizedClientManager authorizedClientManager) {

		logger.info("entering the token flow....******************++++++");
		//*
		//from the git repo - https://github.com/joshlong-attic/2026-04-15-springio-bootiful-spring-grpc/blob/main/client/src/main/resources/application.properties
		var authenticatedUser = this.strategy.getContext().getAuthentication();
		if (authenticatedUser instanceof OAuth2AuthenticationToken auth2AuthenticationToken) {
			var clientId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
			logger.info("inside token checking clientid ---- *********************** {} ",clientId);
			var oauthAuthorizationRequest = OAuth2AuthorizeRequest
					.withClientRegistrationId(clientId)
					.principal(authenticatedUser)
					.build();
			var authorized = authorizedClientManager.authorize(oauthAuthorizationRequest);
			logger.info("[TOKEN] @@@@@@@@@@@@@@@@--- {}",authorized);
			return Objects.requireNonNull(authorized).getAccessToken().getTokenValue();
		}
		return null;
		 //*/
/*		var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return null;
		}

		var session = attributes.getRequest().getSession(false);
		if (session == null) {
			return null;
		}

		// Pull the active security context out of the session database/memory map
		var context = (SecurityContext) session.getAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

		if (context == null || context.getAuthentication() == null) {
			return null;
		}

		var authenticatedUser = context.getAuthentication();

		if (authenticatedUser instanceof OAuth2AuthenticationToken auth2AuthenticationToken) {
			var clientId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
			var oauthAuthorizationRequest = OAuth2AuthorizeRequest
					.withClientRegistrationId(clientId)
					.principal(authenticatedUser)
					.build();

			var authorized = authorizedClientManager.authorize(oauthAuthorizationRequest);
			return Objects.requireNonNull(authorized).getAccessToken().getTokenValue();
		}
		return null;*/
	}
/*	@Bean
	GrpcChannelBuilderCustomizer<?> grpcChannelBuilderCustomizer(
			OAuth2AuthorizedClientManager auth2AuthorizedClientManager
	){
		return GrpcChannelBuilderCustomizer
				.matching("localhost:9090", channelBuilder -> channelBuilder.intercept(
						new BearerTokenAuthenticationInterceptor(
                                (TokenSupplier) ()-> Objects.requireNonNull(token(auth2AuthorizedClientManager)))));
	}
	private final SecurityContextHolderStrategy strategy = SecurityContextHolder
			.getContextHolderStrategy();

	String token(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager){
		var user = SecurityContextHolder
				.getContext()
				.getAuthentication();

		if (user instanceof OAuth2AuthenticationToken auth2AuthenticationToken){
			var registrationId = auth2AuthenticationToken
					.getAuthorizedClientRegistrationId();
			var request = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
					.principal(user)
					.build();

			return oAuth2AuthorizedClientManager
					.authorize(request)
					.getAccessToken()
					.getTokenValue();
		}

		return null;
	}*/

}

@Controller
@ResponseBody
class ClientController{
   private final AppServiceGrpc.AppServiceBlockingStub stub;

    ClientController(AppServiceGrpc.AppServiceBlockingStub stub) {
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

/*
@Controller
class AuthorizationController {
	private final WebClient webClient;
	private final String messagesBaseUri;

	public AuthorizationController(WebClient webClient,
								   @Value("${messages.base-uri}") String messagesBaseUri) {
		this.webClient = webClient;
		this.messagesBaseUri = messagesBaseUri;
	}

	@GetMapping(value = "/authorize", params = "grant_type=authorization_code")
	public String authorizationCodeGrant(Model model,
										 @RegisteredOAuth2AuthorizedClient("messaging-client-authorization-code")
                                         OAuth2AuthorizedClient authorizedClient) {

		String[] messages = this.webClient
				.get()
				.uri(this.messagesBaseUri)
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(String[].class)
				.block();
		model.addAttribute("messages", messages);

		return "index";
	}

	// '/authorized' is the registered 'redirect_uri' for authorization_code
	@GetMapping(value = "/authorized", params = OAuth2ParameterNames.ERROR)
	public String authorizationFailed(Model model, HttpServletRequest request) {
		String errorCode = request.getParameter(OAuth2ParameterNames.ERROR);
		if (StringUtils.hasText(errorCode)) {
			model.addAttribute("error",
					new OAuth2Error(
							errorCode,
							request.getParameter(OAuth2ParameterNames.ERROR_DESCRIPTION),
							request.getParameter(OAuth2ParameterNames.ERROR_URI))
			);
		}

		return "index";
	}

	@GetMapping(value = "/authorize", params = "grant_type=client_credentials")
	public String clientCredentialsGrant(Model model) {

		String[] messages = this.webClient
				.get()
				.uri(this.messagesBaseUri)
				.attributes(clientRegistrationId("spring"))
				.retrieve()
				.bodyToMono(String[].class)
				.block();
		model.addAttribute("messages", messages);

		return "index";
	}
}
//*/