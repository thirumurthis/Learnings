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
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(token);
		
		MockHttpServletRequestBuilder request = get("/")
				.sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
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
```
