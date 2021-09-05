
#### Notes to perform TEST cases using spring security
  - The latest spring boot application, by default supports Junit5
  - `@RunWith` is not present in Junit5, the equivalent is `@ExtendWith`. In spring pass the class `SpringExtension.class` to this annotation.
  - using `@SpringBootTest` already, uses the `@ExtendWith(SpringExtension.class)` call.
  - Already the required dependency are included, if mockito dependency is need include that in pom.xml. Below test case example doesn't require any mockito.

- Assume below code is the application which we need to test
```java

package com.test.learn.TestSecurityApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class TestSecurityAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestSecurityAppApplication.class, args);
	}
}
// ------------- new class in different file
package com.test.learn.TestSecurityApp;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class HelloSecurityMessageService implements MessageService {

	@Override
	public String getMessage() {
		return "Hello Security Service";
	}

}
// --------------- new interface in different file
public interface MessageService {
	public String getMessage();
}
```

##### Testing with `TestAuthenticationToken()` and manually setting to security context is a manual test approach.
  - To test the above code

```java
package com.test.learn.TestSecurityApp;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

//@ExtendWith(SpringExtension.class)  - this i already included in @SpringBootTest
@SpringBootTest
public class ManualMessageServiceTest {

	@Autowired
	MessageService msgService;
	
	@Test
	public void authenticationFailtest() {
		assertThatCode(() -> this.msgService.getMessage()).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
	}
	
	@Test
	//Test case to test user authenticated but not authorized
	public void authenticatedNotAuthorized() {
		//TestingAuthenticationProvider provider = new TestingAuthenticationProvider();
		//without ROLE provided, throws exception to provide AuthenticationProvider, 
		//since the messageservice include role based authentication
		TestingAuthenticationToken	token = new TestingAuthenticationToken("user", "password","ROLE_USER");
		SecurityContextHolder.getContext().setAuthentication(token);
		
		assertThatCode(()-> this.msgService.getMessage()).isInstanceOf(AccessDeniedException.class);
		
	}
	
	@Test
	//Test case to test user authenticated but not authorized
	public void authenticatedSuccessTest() {
		//TestingAuthenticationProvider provider = new TestingAuthenticationProvider();
		//without ROLE provided, throws exception to provide AuthenticationProvider, 
		//since the messageservice include role based authentication
		TestingAuthenticationToken	token = new TestingAuthenticationToken("user", "password","ROLE_ADMIN");
		SecurityContextHolder.getContext().setAuthentication(token);
		
		assertThatCode(()-> this.msgService.getMessage()).doesNotThrowAnyException();		
	}
}
```

##### Testing with `@WithMockUser` annotation

```java
package com.test.learn.TestSecurityApp;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
public class AutomatedMessageServiceTest {

	@Autowired
	private MessageService msgService;
	
	@Test
	//To simulate a mockuser
	@WithMockUser  //this will provide a user with role of USER by default
	//test case to validate, user with no role to access
	public void noAccessNotAuthorizedTest() {
		 assertThatCode(()->this.msgService.getMessage())
		       .isInstanceOf(AccessDeniedException.class);
	}
	
	@Test
	//To simulate a mockuser
	@WithMockUser(username="user",roles="ADMIN") // we can override username, roles if we need to test those
	//test case to validate, user with no role to access
	public void hasAccessNotAuthorizedTest() {
		 assertThatCode(()->this.msgService.getMessage())
		       .doesNotThrowAnyException();;
	}
```

#### Creating __Metaannoation__ for `@WithMockUser` for testing as specific role.
   - Create a interface, and annoate with `@WithMockUser(username="amdin",roles="ADMIN")`
   - Add `@Retention(RetentionPolicy.RUNTIME)` to make the meta annotation avaialable at runtime
   - use the meta annotation as `@<created-class-name>` example is below.
- Usage, if we have many set of test cases that needs to be executed as Admin, User, Guest, different role, we can use meta annoataion to simplify code.

 ```java
 
 // meta annoation interface
 package com.test.learn.TestSecurityApp;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import org.springframework.security.test.context.support.WithMockUser;

/*
 * in case if we need to perform lots of test case with 
 * role ADMIN we can create a meta annotation and pass it
 */

@WithMockUser(username="admin",roles="ADMIN")
//adding a retention, to make this annoation available at runtime
@Retention(RetentionPolicy.RUNTIME)  // Retention annotation doesn't apply to class
public @interface MetaAnnotationWithAdmin {
}

//-------- code in new file with meta annotation
package com.test.learn.TestSecurityApp;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
public class AutomatedMessageServiceTest {

	@Autowired
	private MessageService msgService;
	
	@Test
	@MetaAnnotationWithAdmin  //the new meta annotation 
	public void asAdmin_hasAccess_NotAuthorizedTest() {
		 assertThatCode(()->this.msgService.getMessage())
		       .doesNotThrowAnyException();;
	}
}
```
