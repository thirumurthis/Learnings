#### Spring security WebMVC testing
  - Create a project with web, security dependencies

 - Below code is how to set the WebMVC manually for testing.
 -  Since we have a rest controller with / endpoint.
 -  We create a WebAppContext using WebMvc builder. initially we create the spring context holder, which fails since the context should be attached to session
 -  We then create WebAppContext, this time the security context is attached to the session
```java
//---------------------- TEST CASES
package com.test.webmvc.TestWebMVCApp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.Filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

//Testing MockMVC manually
@SpringBootTest
public class ManualMVCTest {

	@Autowired
	WebApplicationContext context;
	
	@Autowired
	Filter springSecurityFilterChain;
	
	//Test case where the context is created manually
	// security context is added part of the filter
	// and spring adds it.
	@Test
	public void contextFails() throws Exception{
		MockMvc mock= MockMvcBuilders.webAppContextSetup(this.context)
				.addFilters(springSecurityFilterChain)
				.build();
		TestingAuthenticationToken token = new TestingAuthenticationToken("user","password","ROLE_USER");
		SecurityContextHolder.getContext().setAuthentication(token);
		//performing mockmvc testing
		mock.perform(get("/"))
		    .andExpect(status().isUnauthorized());
	}
	//NOTE: The above test case fails, eventhough the spring security holder is set
	//this is because the spring security context persistent filter is establishing security context based on session
	
	//Below is the how to set the security context to the session
	//Manually setting the security context setting to session instead of doing in security context holder
	@Test
	public void contextPassInSession() throws Exception{
		MockMvc mock= MockMvcBuilders.webAppContextSetup(this.context)
				.addFilters(springSecurityFilterChain)
				.build();
		TestingAuthenticationToken token = new TestingAuthenticationToken("user","password","ROLE_USER");
		
		//Create an empty security context and set the token to it.
		// this context will be attached to the session
		SecurityContext seContext = SecurityContextHolder.createEmptyContext();
		seContext.setAuthentication(token);
		
		MockHttpServletRequestBuilder request = get("/")
				.sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, seContext);
		//performing mockmvc testing
		//Note: instead of using the url here, we are directly passing the request builder object 
		// this time it will be passing the request.
		mock.perform(request)
		    .andExpect(status().isOk());
	}
}
//-------------------- MAIN application file //------------------------------
package com.test.webmvc.TestWebMVCApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TestWebMvcAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestWebMvcAppApplication.class, args);
	}
}

@RestController
class SimpleController{
	@GetMapping("/")
	public String hello() {
		return "hello User";
	}
}
//-------------------------- WEBSECURITY configuration
@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic();
		http.authorizeRequests()
		    .anyRequest().authenticated();
	}
}
```

##### Perofrming the above approach is not ideal, we can established the same using `@MockMVC`.

 - Mocking the user with Annotation `@WithUser` and using `SecurityMvcRequestPostProcessor` test methods

```java 

///----------------- The spring Restcontroller is same as the above code.
package com.test.webmvc.TestWebMVCApp;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class AutoMVCTest {
	
	@Autowired
	WebApplicationContext webcontext;
	
	MockMvc mockMvc;
	
	@BeforeEach
	public void setup() {
		this.mockMvc = MockMvcBuilders.
				 webAppContextSetup(this.webcontext)
				.apply(springSecurity()) // we are adding spring security to context using apply method,  
				                         // this adds spring security and also provides test support to mockmvc 
				.build();
	}
	
	@Test
	@WithMockUser  // we saw this on the method level security test case
	public void allowAll() throws Exception{
		mockMvc.perform(get("/"))
		       .andExpect(status().isOk());
	 
	}
	
	@Test
	//no mock user added here.
	public void failsAsNoUser() throws Exception{
		mockMvc.perform(get("/"))
		       .andExpect(status().isUnauthorized());
	 
	}
	
	//With MockMVC support, we can leverage request post processor rather than using annotation
	@Test
	public void allowedUserRequestPostProcessor() throws Exception{
		MockHttpServletRequestBuilder request = get("/")
				.with(user("user")); // using with and passing the user directly here. the default role is ROLE_USER
		mockMvc.perform(request)
		       .andExpect(status().isOk());
	}  // All the user, etc method are available on  SecurityMockMvcRequestPostProcessor.java
	// lots of other test support is available in that class like custom authentication, custom security context as well
	
	@Test
	public void allowedUserExtractedRpp() throws Exception{
		MockHttpServletRequestBuilder request = get("/")
				.with(userinfo()); // extracted to local statuc method userinfo 
		mockMvc.perform(request)
		       .andExpect(status().isOk());
	}

	//Static method extracted out this to static method
	private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor userinfo() {
		return SecurityMockMvcRequestPostProcessors.user("user");
	}		
}
```

 #### Using CSRF token test classes and FORM LOGIN testing 
 
 ```java
 // -- Few test case actually FAILS check it as an assignment
 
 package com.test.webmvc.TestWebMVCApp;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class AutoMVCTest {
	
	@Autowired
	WebApplicationContext webcontext;
	
	MockMvc mockMvc;
	
	@BeforeEach
	public void setup() {
		this.mockMvc = MockMvcBuilders.
				 webAppContextSetup(this.webcontext)
				.apply(springSecurity()) // we are adding spring security to context using apply method,  
				                         // this adds spring security and also provides test support to mockmvc 
				.build();
	}
		
	@Test
	@WithMockUser  // we saw this on the method level security test case
	public void postFails() throws Exception{
		MockHttpServletRequestBuilder request = post("/transfer")
				.param("amount", "1");
		mockMvc.perform(request)
		       .andExpect(status().isForbidden());
	}
	
	//Below is performing the above test case with valid CSRF token
	@Test
	@WithMockUser  // we saw this on the method level security test case
	public void postPassWithCSRF() throws Exception{
		MockHttpServletRequestBuilder request = post("/transfer")
				.param("amount", "1")
				.with(csrf()); // adding with csrf 
		mockMvc.perform(request)
		       .andExpect(status().isOk()); // this is failing now with 400 - check later
		// if we use post on redirect pattern after posting we are redirect to GET - user is3xxRedirection() to validate 
	}

	//FORM Based login testing example :
	@Test
	public void loginTest() throws Exception {
		mockMvc.perform(formLogin())
		       .andExpect(status().is3xxRedirection()) //check why there is a client error instead here
		       .andExpect(authenticated()); // this test case is also failing 
	}
	
	// Making a failed login case 
	@Test
	public void loginTestFail() throws Exception {
		mockMvc.perform(formLogin().password("invalid"))
		       .andExpect(status().is3xxRedirection())  //check why there is a client error instead here 
		       .andExpect(unauthenticated());
	}
}
 ```
 
 
