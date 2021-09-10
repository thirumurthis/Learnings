### Adding custom method security 

- Create a spring boot project with dependencies web, security.
- To create custom evaluator, we can implement the `PermissionEvaluator.java` below is the code details
- Important details, adding the spring security, use the `user` and generated password. the default user name is `user`

- The `@PostAuthorize` included in the userService accessing the methd
- The `@PreAuthorize` also used in the controller. 
      - When logged in as `user@password` and issuing `http://localhost:8080/user/admin` -> it will be a access denied message

- Assignemnt, write Test cases for controller, using spring-security-test. use `@WithMockUser` ,`@MockBean` if hitting the database.

```java
package com.general.auth;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.general.auth.sec.UserService;

@SpringBootApplication
public class GenericAuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(GenericAuthenticationApplication.class, args);
	}
}

@RestController
class SimpleController{
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/")
	public String welcome() {
		return "welcome ";
	}
	
	@GetMapping("/message")
	public String message() {
		return userService.getMessage();
	}
	
	@GetMapping("/user/{username}")
	@PreAuthorize("hasPermission(#username, 'ADMIN_USER')")
	public String access(@PathVariable("username") String username, Principal principal) {
		return "You have access, welcome "+principal.getName();
	}
}
```
 - Different package
```java
// separate class
package com.general.auth.sec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	   @Override
	   protected void configure(AuthenticationManagerBuilder auth) throws Exception{
	     //step 1 What type of authetincation is needed (in-memory)
	     auth.inMemoryAuthentication()
	     .withUser("username")
	     .password("password")  //this is not for production app's for test only
	     .roles("USER")   // role needs to be provided, that user is associated to USER role
	     .and()                  // to add more user
	     .withUser("admin")
	     .password("admin")
	     .roles("ADMIN");   
	   } 
	   
	   @Bean
	   public PasswordEncoder getPasswordEncoder(){
	      return NoOpPasswordEncoder.getInstance(); //don't use this in production system.
	   }
	   
	   @Override 
	   public void configure(HttpSecurity http) throws Exception{
	      http.authorizeRequests()
	          // .antMatchers("/admin").hasRole("ADMIN")
	          // .antMatchers("/user").hasAnyRole("USER","ADMIN") // hasAnyRole() for more than one role usage
	           .mvcMatchers("/**").authenticated()//permitAll()  //permitAll the root api can be viewed by all user
	           .and()
	           //.csrf().disable()
	           .formLogin();  // formLogin is a default and this will provides a username/password form.
	           
	           //Note: The least accessed url should be at the top, most accessed to be at the bottom
	   }
}
```
- Different package

```java
package com.general.auth.sec;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserService {

	public User getUserInfoFromIdentityStore() {
		log.atDebug().log("UserService call invoked returning username as Thiru");
		User usr = new User("Thiru");
		return usr;
		
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PostAuthorize("hasPermission(returnObject,'ADMIN_VIEW')")
	public String getMessage() {
		return "This is a secret message";
	}
}
```
```java
package com.general.auth.sec;

import java.io.Serializable;

import org.apache.catalina.realm.UserDatabaseRealm;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomPermissionEvaluator implements PermissionEvaluator{

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
  //debuging to this poing, targetDomainObject is the value passed along hasPermission in @PreAuthorize.
  //when using @PostAuthorize, the targetDomainObject will be returnObject value, the returned value of method (spring uses AOP)
		if(authentication == null || null == targetDomainObject || !(permission instanceof String)) {
			return false;
		}
		
		User usr = (User) authentication.getPrincipal();
		// this value is passed part of the haspermission
		if("ADMIN_VIEW".equals(permission)) {
		    return usr.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
		} 
		if("ADMIN_USER".equals(permission)) {
			return usr.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) && "admin".equals(usr.getUsername());
		}
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		return false;
	}
}
```
- Different class, pass the custom permissionevaluator in extended `GlobalMethodSecurityConfiguration` class
```java
package com.general.auth.sec;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class CustomMethodSecurityConf extends GlobalMethodSecurityConfiguration{
	
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler(){
		DefaultMethodSecurityExpressionHandler expHandler = new DefaultMethodSecurityExpressionHandler();
	       // pass in the created custom PermissionEvaluator// so it can be used part of hasPermission
		expHandler.setPermissionEvaluator(new CustomPermissionEvaluator());
		return expHandler;
	}
}
```
