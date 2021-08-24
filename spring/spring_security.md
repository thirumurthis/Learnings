### Spring security:

- `authentication` 
    - providing username and password, the user is authenticated (known as knowledge based authentication)
    - using id card, mobile are possesion based authentication
    - combination is knowledge base and possesion based is multi factor (MFA)
    
- `authorization` 
     - once the authentication is completed, then comes authorization is whether this user can perform action on the application is based on the role.

- `princpal`
     - This is the user is identified by authentication process. Currently logged in user. This contains the user information.
     - This prinicipal is remembered by application at session level, so no need to provide username/password everytime.

- `Granted Authority`
      - after authorization, the set of permission that the user can do is called granted authority. The user can do a action, only if the user has the authority.
      - fine-grain control can be provided.

- `Role` 
     - This is a group of authority. For example, Manager as a role, Developer is a role.

----------------------

`spring-security` Authentication in memory.
 - `AuthenticationManager` is the one handles authentication in spring security application.
    - `AuthenticationManager` has a method `authenticate()` which does the authenication. this returns success, or faliure to authenticate.
    - A `AutenticationManagerBuilder` is used to work with the `AuthenticationManager`. 

  - Step1: `AuthenticationManagerBuilder` - what type of authentication is needed. Ex. Inmemory authentication
  - Step2: Configure the user's username, roles, etc. 

Once the step1 & step2 is done, the `AuthenticationManager` is created by the spring.

 - How to get hold of the `AuthenticationManagerBuilder`?
    - The `WebSecurityConfigurerAdaptor` class, should be extended and override the `configure(AuthetnicationManagerBuilder)` method. If not overriden, the default `configure()` will be invoked.
   
```java

@EnableWebSecurity  //this tells this is web security (another type is method level security different then this)
public class SecurityConfig externds WebSecurityConfigurerAdaptor{
   @Override
   protected void configure(AuthenticationManagerBuilder auth) throws Exception{
     //step 1 What type of authetincation is needed (in-memory)
     auth.inMemoryAuthentication()
     .withUser("username")
     .password("password")  //this is not for production app's for test only
     .roles("USER");   // role needs to be provided, that user is associated to USER role
   }
   
   //Since we are handling password here, we need to encrypt or hast it to store it. 
   //This can be done here by creating a PasswordEncoder bean
   
   // This is a requirement
   @Bean
   public PasswordEncoder getPasswordEncoder(){
      return NoOpPassowrdEncoder.getInstance(); //don't use this in production system.
   }
}
```


