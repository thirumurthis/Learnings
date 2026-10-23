package com.grpc.authz;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableWebSecurity
public class AuthzApplication {

	private static final Logger logger = LoggerFactory.getLogger(AuthzApplication.class);

	static void main(String[] args) {
		SpringApplication.run(AuthzApplication.class, args);
	}

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
		var authorizationServerConfigurer =
				new OAuth2AuthorizationServerConfigurer();

		RequestMatcher oAuthEndpoints = authorizationServerConfigurer.getEndpointsMatcher();

		RequestMatcher h2ConsoleMatcher = PathRequest.toH2Console();

		http
				//.securityMatcher(request ->
				//		oAuthEndpoints.matches(request) && !h2ConsoleMatcher.matches(request))
				.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher() )
				.with(authorizationServerConfigurer, (authorizationServer) ->
						authorizationServer
								.oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
				)
				.authorizeHttpRequests((authorize) -> authorize
						//.requestMatchers("/h2-console/**").permitAll()
						.anyRequest().authenticated()
				)
				// Redirect to the login page when not authenticated from the
				// authorization endpoint
				.exceptionHandling((exceptions) -> exceptions
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login"),
								new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
						)
				);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		http
				.authorizeHttpRequests((authorize) -> authorize
						//.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD).permitAll()
						//.requestMatchers("/h2-console/**").permitAll()
						.anyRequest().authenticated()
				)
				//.csrf(csrf -> csrf
				//		.ignoringRequestMatchers("/h2-console/**"))
				//.headers(headers -> headers
				//		.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

				// Form login handles the redirect to the login page from the
				// authorization server filter chain
				.formLogin(Customizer.withDefaults());

		return http.build();
	}

	//https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html#servlet-authentication-jdbc-bean
	@Bean
	UserDetailsManager users(DataSource dataSource) {
		UserDetails user = User.builder()
				.username("user")
				.password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
				.roles("USER")
				.build();
		UserDetails admin = User.builder()
				.username("admin")
				.password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
				.roles("USER", "ADMIN")
				.build();
		JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
		//users.createUser(user);
		//users.createUser(admin);
		return users;
	}
	//inmemory
	//@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails0 = User.withDefaultPasswordEncoder()
				//User.builder()
						.username("user")
				.password("password")
						//.password(encoder.encode("password"))
						.roles("user")
						.build();
		UserDetails userDetails1 = User.withDefaultPasswordEncoder()
				//User.builder()
						.username("spring")
				.password("password")
						//.password(encoder.encode("password"))
						.roles("user","admin")
						.build();
		/*UserDetails userDetails = User.withDefaultPasswordEncoder()
				.username("user")
				.password("password")
				.roles("USER")
				.build();*/

		return new InMemoryUserDetailsManager(userDetails0, userDetails1);
	}

	/*
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("spring")
				.clientSecret("{noop}secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				//.redirectUri("http://127.0.0.1:8080/login/oauth2/code/spring")
				//.redirectUri("http://127.0.0.1:8080/")
				//.redirectUri("http://localhost:8080/login/oauth2/code/spring")
				.redirectUri("http://client.local:8080/login/oauth2/code/spring")
				//.redirectUri("http://localhost:8080/")
				//.postLogoutRedirectUri("http://127.0.0.1:8080/")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.scope("user")
				.scope("admin")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(java.time.Duration.ofMinutes(30)).build())
				.build();

		return new InMemoryRegisteredClientRepository(oidcClient);
	}
	 */


	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}


	//custom bean to intercept the token request and add roles
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			// when using curl with client_credential grant_type
			// Check if we are generating an Access Token via the Client Credentials flow
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) &&
					AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {


				logger.info("[CLIENT_CREDENTIALS] context.principal {}",context.getAuthorizedScopes());
				// Extract authorities assigned to the Client Principal
				Set<String> roles = context.getAuthorizedScopes().stream()
						.map(scope -> scope.replace("ROLE_", "").replace("SCOPE_", ""))
						.collect(Collectors.toSet());

				// Inject the 'roles' claim into the JWT payload
				context.getClaims().claim("roles", roles);
			}

			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())){
				Set<String> authorities = context.getPrincipal().getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
						.filter(Objects::nonNull)
						.filter(role -> role.startsWith("ROLE_") )
						.map(role -> role.replace("ROLE_", ""))
						.collect(Collectors.toSet());
				logger.info("[ParameterName.ACCESS_TOKEN] context.principal {}",authorities.stream().distinct());

				context.getClaims().claim("roles", authorities);
			}

		};
	}

}

/*
@RestController
class HomeController {

	@GetMapping("/")
	public String home(Principal principal) {
		return "<h1>Authorization Server</h1>" +
				"<p>Welcome, <b>" + principal.getName() + "</b>! You have logged in successfully.</p>";
	}
}
// */