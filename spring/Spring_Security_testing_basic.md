
#### Notes to perform TEST cases using spring security
  - The latest spring boot application, by default supports Junit5
  - `@RunWith` is not present in Junit5, the equivalent is `@ExtendWith`. In spring pass the class `SpringExtension.class` to this annotation.
  - using `@SpringBootTest` already, uses the `@ExtendWith(SpringExtension.class)` call.
  - Already the required dependency are included, if mockito dependency is need include that in pom.xml. Below test case example doesn't require any mockito.

[Link for documentation](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#test)

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
 ##### How does `@WithMockUser` works
   - When the springrunner is setup, the `TextContextManager` is iterating over list of `TextExecutionListener`. 
   - Spring security provides `WithSecurityContextTestExecutionListener` - this reads the annoation at the method and class level, and creates and sets up the security context for us.
   - The `@WithMockUser` class, it is annotated with `@WithSecurityContext()` and passed in a factory. The annoataion and the factory will take care of creating the security context.

- Lets see how we can refactor the code further, if we need to run more class using the Admin role.
   - _The meta annoataion can be added at the class level soe it will be appled to the test methods_. The test case reduces further as below.

```java
package com.test.learn.TestSecurityApp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@MetaAnnotationWithAdmin
@SpringBootTest
public class TestClassMessageService {

	@Autowired
	private MessageService ms;
	
	@Test
	public void testAuthorizesuccess() {
		this.ms.getMessage();
	}
}
```
  - In case if we need to override any of the test case, when  `@WithMockUser(role="ADMIN")` annotation used at class level?
     - We can use annotation at the method level, so any annoation at the method level will override the class level annotation. refer below code.

```java
package com.test.learn.TestSecurityApp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;

@MetaAnnotationWithAdmin
@SpringBootTest
public class TestClassMessageService {

	@Autowired
	private MessageService ms;
	
	@Test
	public void testAuthorizesuccess() {
		this.ms.getMessage(); //add assertion as needed
	}
	
	@Test
	@WithAnonymousUser //at method level this will override, here throws exception
	public void testAuthorizeUnsuccess() {
		this.ms.getMessage(); //add assertion as needed
	}
}
```

##### How to leverage the use of user existing in the system.
   - All above methods are when the user is not present in the system, we are mocking.
   - What happens if we need to test case to use a user present in our system
   - If we have declared `UserDetailsService` we can use that to get the information of particular user from system. This becomes handy when we have custom authentication.
   
 - We can use `@WithUserDetails` annotation to leverage custom authentication test using custom authenticated user.
 - The problem with this `@WithUserDetails` is that user should exists in our system
 - The @WithUserDetails can be used as a meta annotation as well.

###### What if we want to run as an mocked user of custom type.
  - in previous @WithUserDetails, the custom authentication is used, and the user info should exists in the system.
  - In order to mock the user, we can create a custommockuserfactory and use it with mockuser
  - With the use of `@WithSecurityContext` annotation

```java
 // new file under the test directory 
 package com.test.learn.TestSecurityApp;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;

@SpringBootTest
public class TestWithUserDetailsExample {
	@Autowired
	MessageService messageService;
	
	//@Disabled //we don't want to run this is just a demo
	// the principal in withmockuser is not custom user details so it fails
	@Test
	@WithMockUser
	// this annotation provides a different type, since we have userdetails
	/* EXECPTED output will be */
	//java.lang.IllegalArgumentException: Failed to evaluate expression 'retrunedObject.to.id == principal?.id'
	//at org.springframework.security.access.expression.ExpressionUtils.evaluateAsBoolean(ExpressionUtils.java:33)
	public void testFailIncorrectType() {
		assertThatCode(()->this.messageService.getMessage()).isInstanceOf(IllegalArgumentException.class);  // assert if needed
	}
	
	@Test
	@WithUserDetails("tim")  // when the username exists in the system 
	//@WithUserDetails(value="tim",userDetailsServiceBeanName = "userRepoUserDetailsService")
	public void testWithCustomUserTypeGranted() {
		this.messageService.getMessage();
	}
	
	@Test
	@WithMockMessageUser  // @WithSecuritycontext used for injecting custom or mock user
	public void testWithMockedUser() {
	   this.messageService.getMessage();	// apply assertion for the return value 
	}
	
	@Test
	@WithMockMessageUser(mail="user") // this will fail
	public void testWithMockedUser() {
	   this.messageService.getMessage();	// apply assertion for the return value 
	}	

}
// ------------------- Mock custom user class
package com.test.learn.TestSecurityApp;
import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class MockCustomUserFactory implements WithSecurityContextFactory<WithMockMessageUser>{
	//Below is the way to mock the user - to leverage when we don't want to load 
	// user from the system. since we might now know what exists in system. unless
	// the system has a separate identity store that never changes
	@Override
	public SecurityContext createSecurityContext(WithMockMessageUser mockMessageUser) {
		// leveraging mockito to mock the userrepository
		UserRepository user = mock(UserRepository.class);
		when(user.findByMail(ArgumentMatchers.any())).thenReturn(createMessage(mockMessageUser));
		
		//Setting that in the user repository details
		UserRepoUserDetailsService userRepo = new UserRepoUserDetailsService(user);
		//create principal, with the userdetails object
		UserDetails principal = userRepo.loadUserByUsername(mockMessageUser.mail());
		
		// set the principal in security context
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		UsernamePasswordAuthenticationToken authentication = 
		             new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),principal.getAuthorities());
		context.setAuthentication(authentication);
		return context;
	}
	private User createMessage(WithMockMessageUser mockMessageUser) {
    	   return new User(mockMessageUser.mail(),mockMessageUser.value(),new Authority("ROLE_USER"));
	}
}
//-------------------- Mock user object to generate user
package com.test.learn.TestSecurityApp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockCustomUserFactory.class)
public @interface WithMockMessageUser {
	//add attribute to be customized
	//long id() default 1L;
	String mail() default "demouser";
	String value() default "message";
}

//--------------------- message service seperate java class
package com.test.learn.TestSecurityApp;

import org.springframework.security.access.prepost.PostAuthorize;
public interface MessageService{
	//@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostAuthorize("retrunObject.to.mail == principal?.user?.mail") //returnObject is a pre-defined name of return object. use as is
	//@PostAuthorize("@authz.check(returnObject, principal?.user)")
	public Message getMessage();
}
//------------------- Message data overrides teh message service
package com.test.learn.TestSecurityApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageData implements MessageService{

	@Autowired
	MessageRepository mr;
	@Override
	public Message getMessage() {
		//Optional<Message> msg =  mr.findById(1l); // this just for testing purpose added 1 long for id
		//return msg.orElse(null);
		return new Message(1L,"Hello",new User("demouser","password",new Authority("ROLE_ADIM")));  //CURRENTLY HARDCODED THIS VALUE, IDEALLY need to fetched from db.
	}
}

//-------------------- Message Support class sperate file 
package com.test.learn.TestSecurityApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.transaction.Transactional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

public class MessageSupport {}

interface MessageRepository extends JpaRepository<Message,Long>  {

	String QUERY = "select m from Message m where m.id= ?1";
	@Query(QUERY)
	@RolesAllowed("ROLE_ADMIN")            //JSR 250 annotation which is very old and stable.
	Message findByIdRolesAllowed(Long id); // the spring data will not be able to fetch query based on name of the method
	                                       // we need to pass the custom query
}

interface UserRepository extends JpaRepository<User, Long>{
	//create a custom query to find the user in db using name
	User findByMail(String mail);
}

interface AuthorityRepository extends JpaRepository<Authority, Long>{
}

@Entity // jpa entity
@AllArgsConstructor
@NoArgsConstructor
@Data
class Message {
	public Message(String text, User to) {
		this.text = text;
		this.to = to ;
	}
	@Id
	@GeneratedValue
	private Long id;
	private String text;
	@OneToOne
	private User to;
}

@Entity // jpa entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude= "authorities") // to exclude that field since we get recursive graph due to jointable
@Data
class User{
	public User(String mail, String password, Set<Authority> authorities) {
		this.mail = mail;
		this.password = password;
		this.authorities.addAll(authorities);
	}
	public User(String mail, String password, Authority ... authorities) {
		this(mail,password,new HashSet<>(Arrays.asList(authorities)));
	}
	@Id
	@GeneratedValue
	private Long id;
	private String mail, password;
	// create a constructors as needed for all arguments
// create a derived constructor, for different prams per requirement.
	@ManyToMany(mappedBy = "user")
	private List<Authority> authorities = new ArrayList<>();
}

@Entity // jpa entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude="user") //just not to include user on toString override
@Data
class Authority{

	//create constructor
	public Authority(String authority, Set<User> user){
		this.user.addAll(user);
		this.authority = authority;
	}
	//constructor with no user
	public Authority(String authority){
		this.authority = authority;
	}
	@Id
	@GeneratedValue
	private Long id;
	private String authority;

	@ManyToMany (cascade= {CascadeType.PERSIST,CascadeType.MERGE})
	@JoinTable (name="authority_user",
			joinColumns = @JoinColumn (name = "authority_id"),
			inverseJoinColumns = @JoinColumn (name ="user_id"))
	private List<User> user= new  ArrayList<>();
}

@Service
@Log4j2
@NoArgsConstructor
//creating user details
class UserRepoUserDetailsService implements UserDetailsService{

     // this is the wrapper of the authenticated object, this can be used
    // for httpBasic or form-based logins, etc.
        //inner class
	class UserDetailsInfo implements UserDetails{
		private final User user;
		public User getUser() {return user;}
		private Set<GrantedAuthority> authorities ;
		//constructor
		public UserDetailsInfo(User user){
			this.user = user;
			this.authorities = this.user.getAuthorities()
					.stream()
					.map(a -> new SimpleGrantedAuthority("ROLE_"+a.getAuthority()))
					.collect(Collectors.toSet());
		}
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {return this.authorities;}
		@Override
		public String getPassword() {return this.user.getPassword();}
		@Override
		public String getUsername() {return this.user.getMail();}
		@Override
		public boolean isAccountNonExpired() {return true;}
		@Override
		public boolean isAccountNonLocked() {return true;}
		@Override
		public boolean isCredentialsNonExpired() {return true;}
		@Override
		public boolean isEnabled() {return true;}
	}
	private UserRepository userRepo ;
	public UserRepoUserDetailsService(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
		User usr = userRepo.findByMail(mail);
		if (null != usr){
           // we need to return the UserDetails, which is defined as implementation of userdetails form spring sec.
			UserDetails userDetails = new UserDetailsInfo(usr);
			log.info("principal info :- "+userDetails.toString());
			return userDetails;
		}
		else
			throw new  UsernameNotFoundException("User not fond "+mail);
	}
}
/// used this to debug and check the return of @PostAuthorize between returned object and prinicpal.
@Service("authz")
@Log4j2
class AuthzService {

	public boolean check(Message msg, User usr){
		log.info("checking - " + usr.getMail()+ " accessTo  message of "+msg.getTo().getMail()  );
		return msg.getTo().getMail().equals(usr.getMail()); // true when message of user and accessing user matched
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter{	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http.httpBasic();
		
		http.authorizeRequests().mvcMatchers("/**").permitAll();
	}
}
```


