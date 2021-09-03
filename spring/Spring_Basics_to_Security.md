- Create a Spring boot project using `spring web` and `spring Aop` depenecies

- `@component` and `@Bean` annotation can be used to tell spring to scan those class/bean.

```java
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
}
// implict way
@Component
class Car(){
  private final Foo foo; //spring will deduce this since we annotated with @Component
  Car(Foo foo){
     this.foo = foo;
    };
}

@Component
class Foo(){}
```

- how to tell explicitly to spring to inject 

```java
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
  @Bean
  Foo foo(){
    return new Foo();
  }
   // doing this we removed the @comonent annotation
  @Bean
  Car car(Foo foo){
    return new Car(foo);
  }
}

class Car(){
  private final Foo foo; //spring will deduce this since we annotated with @Component
  Car(Foo foo){
     this.foo = foo;
    };
}

class Foo(){}
```
- Both @component and @Bean can be mixed and used, both the beans are singleton. So pay attention in case of wirting thread based apps, make this thread safety.

#### Spring spel

- In the above example lets say Car takes an uuid

```java
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
  
  // if foo is tagged with @component, and the uuid can be injected directly to the bean like here.
  @Bean
  Car car(Foo foo, @Value("#{uuid.buildUuid()}") String uuid){ 
    return new Car(foo);
  } 
}

class Car(){
  private final Foo foo; //spring will deduce this since we annotated with @Component
  // Another approach - in case if we are not using @Bean to define this bean we can directly set @value of uuid in constructor like below
  Car(Foo foo,  @Value("#{uuid.buildUuid()}") String uuid,
                @Value("#{ 2 >1 }") boolean test){  // we can also set condition, like calling another bean and check during construction time.
     // to print just create a log
     private final LOG log = LogFactory.getLog(getClass());
     this.foo = foo;
     this.log.info(uuid);
     this.log.info(test); // this will be true since the expression evaluated is 2 > 1 evaluating condition.
    };
}

class Foo(){}

//say the logic to generate the UUID can be put as service

@Component
public class UuidService{
  public String buildUud(){
     return UUID.randomUUID().toString();
  }
}
```
- Simple restcontroller to hit the external url and fetch the data

```java
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
}

@RestController
class TestDemo{

private RestTemplate template;

TestDemo(RestTemplate template){
  this.template = template;
  }
  
 @GetMapping("/books/{isbn}")
 String getBookinfo(@PathVariable("isbn") String isbn){
    ResponseEntity<String> exchange = this.template
    .exchange("https://www.googleapis.com/books/v1/volumes?1=isbn:" + isbn, HttpMethod.GET,
    null,String.class);  // parameter passed Url, Request method, any body to be sent since this is GET, response type to be recived
    
   return exchange.getBody(); // this will get the body from response and sent back
 }
```
#### Spring AOP
  - using AspectJ pointcut defintion, a simple program to introduce functionalilty that will logs the execution of all methods in application package.

```
package com.example.demo;
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
}

@Component
@Aspect
class LoggingAspect{

private final Log log = LogFactory.getLog(getClass());
  // this package where the example demo code is preset.
  @Around("execution(* com.example..*.*(..) )");  // the expression says, want to invoke anything with package com.example. and match any type and any method with any parameter
  public Object log(ProceedingJoinPoint pjp) throws Throwable{
     //before -  in the beging of the method invocation
     this.log.info("before" + pjp.toString());
     Object obj = pjp.proceed();
     this.log.info("after" +pjp.toString());
     
     // after invoking the method
     // if some condition, then apply the logic.
     
     retunr obj
  }
}

class Car(){
  private final Foo foo; //spring will deduce this since we annotated with @Component
  // Another approach - in case if we are not using @Bean to define this bean we can directly set @value of uuid in constructor like below
  Car(Foo foo,  @Value("#{uuid.buildUuid()}") String uuid,
                @Value("#{ 2 >1 }") boolean test){  // we can also set condition, like calling another bean and check during construction time.
     // to print just create a log
     private final LOG log = LogFactory.getLog(getClass());
     this.foo = foo;
     this.log.info(uuid);
     this.log.info(test); // this will be true since the expression evaluated is 2 > 1 evaluating condition.
    };
}

class Foo(){}

//say the logic to generate the UUID can be put as service

@Component
public class UuidService{
  public String buildUud(){
     return UUID.randomUUID().toString();
  }
}
```
  - Executing the above code, if the `UuidService` is invoked from the SpringBootDemo class, then the logs should indicate the AOP pointcut applied before and after the methos calls of that uuidservice.

#### Filters in spring 
  - say we have the rest controller to fetch the books info, we can intercept the request and detect the request using filters
```java

@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
}

@RestController
class TestDemo{

private RestTemplate template;

TestDemo(RestTemplate template){
  this.template = template;
  }
  
 @GetMapping("/books/{isbn}")
 String getBookinfo(@PathVariable("isbn") String isbn){
    ResponseEntity<String> exchange = this.template
    .exchange("https://www.googleapis.com/books/v1/volumes?1=isbn:" + isbn, HttpMethod.GET,
    null,String.class);  // parameter passed Url, Request method, any body to be sent since this is GET, response type to be recived
    
   return exchange.getBody(); // this will get the body from response and sent back
 }
 
 class LoggingFilter implements javax.servlet.Filter{
 private final Log log = LogFacatory.getLog(getClass());
 @override
 public void init(FilterConfig filterconfig) throws ServletException{}
 
 @Override
 public void  doFilter(ServletRequest request, ServletResponse response, FliterChain chain) throws IOException,ServletException{
    Assert.isTrue(request instanceof HttpRequest", "a http request test");
    
    HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(request);
    String url = httpServletRequest.getRequestURI();
    this.log.info("new request : "+url);
    
    //after handling we need to forward this using the chain
    //here we are the request can be wrapped and sent to downstream system, they recieve 
    long time = System.currentTimeMilis();
    chain.doFilter(request,response);
    long diff = System.currentTimemilis() - time;
    this.log.info("request time take " + diff +" ms");
 }
 
 @Override
 public void destroy () {
 }
```
#### Spring security works based on filters, the heart is `FilterChainProxy.java` filter. This can be used to debug and check how this works.
   - we can add a debug point in this class
   - SecurityContext are updated and set to the webcontext, servletcontext,etc.
   - getFilters() in this code, will provide a list of filters used.
   - This is the entry point for all web-based security.


- How does spring security or other congigurations are configured in Spring boot? using the `springboot-autoconfigure**.jar`.
- This jar has `spring.factories` where the congigurations are specified. Auto configure key is, `org.springframework.boot.autoconfigure.EnableAutoconfiguration` key.
- Check for the security keyword and can see a bunch of configuration classes.

#### Authentication
   - User name and password to authenticaton for form based and http-basic.
   - Single-sign on token authentication
   - Certificate
   - Link in an confirmation email
 - Who is making this request?
 
#### Authroization
   - After the authentcated
 - What are proven roles the authenticated user have.

 - Most commonly form based user name password is used.
 - Usually a POST request is made, with username and password in the body of the http request.
   - The password should be encoded and compared.

- InMemoryAuthentication
  - This is only useful for development and poc purpose.
  - Create a spring web project from Spring start io with dependencies, Security, Web, Lombok.

```java
@SpringBootApplication
public class SpringInMemory {

 //After reading the below notes, we are creating a in memory user.
 @Bean
 UserDetailsManager userDetailsService(){
    return new InMemoryUserDetailsManager();
 }
 
 @Bean
 InitializingBean initializer(UserDetailsManager manager){ //injecting the pointer to the userdetailmanager bean
 // call back interface, this will call the afterproerties on the lambda
    return () -> {
      // using the builder api, withDefaultPasswordEncoder() is deprecated.
      UserDetails thiru = User.withDefaultPasswordEncoder().username("thiru").password("pass").roles("USER").build();
      manager.createUser(thiru);
      UserDetails ram = User.withUserDetails(thiru).username("ram").build();
      manager.createUser(ram);
    }
 }

  public static void main(String ... args){ SpringApplication.run(SpringInMemory.class, args); }
}

@RestController
class GreetingController{

 @GettingMapping ("/greeting")
 String greeting(Principal principal){  // The authenticated prinicpal is injected by spring security, this is available as long as the 
                                        // prinicpal is available in context
   return "hello "+prinicpal.getName();
 }
}
@Configuration
@EnableWebSecurity
/*Locking down all the request going to application unless they can authenticate using http basic*/
class SecurityConfiguration extends WebSecurityConfiguratorAdaptor{
  @Override
  protected void configure(HttpSecurity http) throws Exception{
    http.httpBasic(); //other builder like, formlogin, httpbasic, ldap - how to handle the authentication
    
    http.authorizeRequests().anyRequest().authenticated();
  }
}
```
- Notes:
```
//curl -uv username:password http://localhost:8080/greeting
//The above, spring security has already at this point taken the http header (X509 certificate, token, authorization token, cookies) and converted to authetnication object.
// The authentication object is passed it to object type UserDetails. This has the username, password, active, etc.
// check UserDetails.java is part of spring security
// UserDetailsService.java - in most case requires to be overrided since most of the case some sort of identiy store.
// Authentication.java - has creditentials, object details, isAuthenticated etc. This is authentication object, is passed to Authentication manger.
// Check AuthetincationManager.java -> authnticate() 
// This authentication object, will be passed to AuthenticationProvider to check which channel provider is available.
// DoaAuthenticationProvier.java  -> this delicates by taking a username and returns the userdetails.
```

#### JDBC authentication
  - Mostly we have database where the idetntity are stored.
  - H2 database can be used.
   - Create a spring project with dependencies jdbc, h2, web, lombok
```java

@SpringBootApplication
public class JdbcSpringSecurity {

 //After reading the below notes, we are creating a in memory user.
 @Bean
 UserDetailsManager jdbc(DataSource ds){
    //Any user creation delete can be configured here if needed
    JdbcUserDetailsManager jdbcUserDetailsManager= new JdbcUserDetailManager();  // this implementation is updated. 
    jdbcUserDetailsMananger.setDataSource(ds);
    return jdbcUserDetailManager(); 
    // create schema rquired needs to be created, create a file called schema.sql
 }
 
 // Note: create a table for user and authority - check the documentation for fields of the table.
 
 @Bean
 InitializingBean initializer(UserDetailsManager manager){ //injecting the pointer to the userdetailmanager bean
 // call back interface, this will call the afterproerties on the lambda
    return () -> {
      // using the builder api, withDefaultPasswordEncoder() is deprecated.
      UserDetails thiru = User.withDefaultPasswordEncoder().username("thiru").password("pass").roles("USER").build();
      manager.createUser(thiru);
      UserDetails ram = User.withUserDetails(thiru).username("ram").build();
      manager.createUser(ram);
    }
 }
  public static void main(String ... args){ SpringApplication.run(JdbcSpringSecurity.class, args); }
 
}

@RestController
class GreetingController{

 @GettingMapping ("/greeting")
 String greeting(Principal principal){  // The authenticated prinicpal is injected by spring security, this is available as long as the 
                                        // prinicpal is available in context
   return "hello "+prinicpal.getName();
 }
}

@Configuration
@EnableWebSecurity
/*Locking down all the request going to application unless they can authenticate using http basic*/
class SecurityConfiguration extends WebSecurityConfiguratorAdaptor{
  @Override
  protected void configure(HttpSecurity http) throws Exception{
    http.httpBasic(); //other builder like, formlogin, httpbasic, ldap - how to handle the authentication
    
    http.authorizeRequests().anyRequest().authenticated();
  }
}

```
- Note:
```
 - JdbcUserDetailsManager.java - has the bunch of queries that are used to fetch and insert to the schema. And these can be overrided.
```

#### using LDAP authenticatione, note spring provides as empty ldap embedded server, use .ldif file in the class path with user info and domain details.
   - On startup the ldif file will be loaded into memory. The application.properties should be updated with the ldap info. like port number, ldif file and domain info where to fetch user.

```
// For LDAP we need to check the authentication provider

@Configuration
@EnableWebSecurity
class SecurityConfiguration extends WebSecurityConfiguratorAdaptor{
  
  //AuthenticationManagerBuilder needs to be checked the ldap authetication
  @Override
  protected void configure(AuthetincationManagerBuilder auth){
    auth.ldapAuthentication()
    .userDnPattern("uid={0},ou=people") // this is providing template query
    .groupSearchBase("ou=groups)   //this is to look up for groups, this can be compared with Roles and groups - 
                                  // how they are stored as different tables similarly in LDAP this is different query
    .contextSource()
    .url("ldap://127.0.0.1:<port-specified-in-application-properties>/dc=<domain-component>,dc=<domain-component>")
    .and()
    .passwordCompare()
    .passwordAttribute("userPassword")
    .passwordEncoder(new LdapShaPasswordEncoder()); // sha is not a secured encoder don't use that in prod.
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception{
    http.httpBasic(); //other builder like, formlogin, httpbasic, ldap - how to handle the authentication
    
    http.authorizeRequests().anyRequest().authenticated();
  }
}
```
 ##### using formlogin
 
 ```java
 
 //for the same inmemory login example - below would be the configuration
 // note that since we are customziing the view using html
 
 // we are not going to use @RestController here. We will be using html views to redirect
 // from the custom login
 
 class SecurityConfig extends WebSecurityConfiguratorAdaptor{
   @Override
  protected void configure(HttpSecurity http) throws Exception{
    //http.httpBasic();  //- this can be used since we can't expect user to provide login credentials for every access
    
    // To enable spring security and  ask authentication details for every request   
    http.authorizeRequests().anyRequest().authenticated();
    
    http.formLogin(); // this enables default login page
    http.logout();   //this enables default logout page
    
    // The above can be customized to open our custom view
    // we can use below
   
     http.formLogin().loginPage("/login").permitAll(); // we allow access to this page to all
     http.logout().logoutUrl("/logout").logoutSuccessful("logout-success"); 
     // the logoutSuccessful() is  used to display after user hit logout html page
     
     // instead of logoutSuccessful() we can use logoutSuccessfulHandler, which is a callback handler so if we need to clear the session etc. can be done here
     // use either this or the above
     http.logout().logoutUrl("/logout").logoutSuccessHandler(new LogoutSuccessHandler{
       @Override
       public void onLogutSuccess(HttpServletRequest request, HttpServeltResponse response, Authentication auth){
        // handle logic here to clear it.
       }
     }); 
  }
  
  @Controller
  class LoginController{
    @GetMapping("/")
    String index(Model model){  // model contains the attributes from the from
      return "index"; //return view the index.html
      }
      // similarly for login, login-successful define @GetMapping...
  }
  
  // in the app above, the user info is 
  // is a object isn't a controller itself
  // this has the ability to do all that controller can do
  // it is used to create a model attributes and can be shared by other controller
  
  // Example below the currently authenticated user can be used by View and Controller
  @ControllerAdvice
  class PrincipalControllerAdvice{
  
  @ModelAttribute("currentUser")
  Principal principal(Principal p){ // if tihs Principal is null then user is about login/ had logged out.
     return p;
  }
}
 ```
--- 
#### Custom Authetication Provider
 - For example, in case the identity management system is not able to provide user details, rather provide true or false for the passed in credientials. Ex. Atlassian identity cloud system does similar to this.
 - How to handle this type of scenario? We can write our own AuthenticationProvider

- The AuthenticationProvider, mirrors the AuthenticationManager which has authenticate () method.
- The authenticate() method take authentication attempt object. This Authentication object  has isAuthenticate() boolean, getDetails(), getCredientials().
   - By default Authentication object, isAutenticate() is false.
   - When this goes to Authentication manager, if the isAuthenticate() is either set to true or false. If the isAuthenticate() is true, then this allows user.
- Only the AuthenticationProvider can be extended.
- ProviderManager manages collection of Authentication provider.
  - AuthenticationProvider supports() method - given the incoming request, can i support his kind of authentication like username/password or Ldap
- The AuthenticationManager Chain of responsibility implmentation
    
```java

//create a bean
@Configuration
@EnableWebSecurity
class CustomSecurityConfig extends WebSecurityConfiguratorAdaptor{

//inject that custom provider to the constructor
private final CustomAuthenticationProvider ap;

//construtor binding
CustomSecurityConfig(CustomAuthenticationProvider ap){
   this.ap = ap;
}

//block all the request for security
@Override
protected void configure(HttpSecurity http)throws Exception{
  http.httpBasic()
  http.authorizeRequests().anyRequest().authenticated();
}
// we will provide our custom authenticator provider here
// this is how we tie the custom authenticaion provider to spring securitu.
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception{
   auth.authenticationProvider(this.ap); // already injected to this constructor.
  }
}

// Creating our own custom authentication provider
@Component
class CustomAuthentication implements AuthenticationProvider{

 //lets create a condition to mock some service
 // since we have access to the password and info, pass it to the backend system and 
 //peform authenticaiton by matching.
 private boolean isValid(String user, String pass){
    return user.equals("thiru") && pass.equals("password");
 }  

    @Override
    public Authentication authentication(Authentication authentication) throws AuthenticationException {
    // This needs to return if the user is authenticated or not logic goes here
    
    String username = authentication.getName();
    String password= authentication.getCredentials().toString();
    if (isValid(username,password){
      return new UsernamePasswordAuthenticationToken(username, password, Collection.singletonList(return new SimpleGrantedAuthority("USER")
      ));
      // the list of Authorities can be created as a variable and passed above
    }
    //if failed then
     reutrn new BadCredientialsException("Coudln't authenicate"); // or usernamenotfound exception.
    }
    
    @Override
    public boolean support(class<?> authentication){
    // we need to determine whether we can handline any authentication type for incoming request.
    // we will do a username password authentication provider and tell it support this type of provider
       return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

#### Custom UserDetailsService
 - In case if we need to communicate to the no sql database or some different identity store. This requires custom userdetails service

 - for demo purpose the user info is stored in a concurrent hash map and lets fetch using custom UserDetails
 - 
```java
@SpringBootApplication
public class CustomAuthenticationApplication{
  
  // without the password encoder bean the application will fail
  @Bean
  PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance(); // this is strictly for development purpse only
  }
  
  @Bean
  CustomUserDetailsService customUserDetailsService(){
   Collection<UserDetails> users = Arrays.asList(
       new CustomUserDetails("thiru","password", true, "USER");
     );
   return new CustomUserDetailsService(users);
  }
}
@RestController
class WelcomeController{
  @GetMapping("/welcome")
  String greet(Principal p ){
    return "welcome "+p.getName();
  }
}

// with the main spring application is created
@Configuraton
@EnableWebSecurity
class CustomSecurityConfig extends WebSecurityConfiugreAdaptor {
   
   @Override 
   protected vod configure(HttpSecurity http)throws Exception {
      http.httpBasic();
      http.authorizeRquests().anyRequest().authenticated();
   }
 }
 
 //  @Service  - comment out this and lets the be injected as bean in the main app
 class CustomUserDetailsService implemtn UserDetailsService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    //initialize user in the constructor
    public CustomUserDetailsService(Collection<Userdetails> users){
    // map.put (key,value)
      users.forEach(users-> this.user.put(users.getUsername(), user));
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
     
     // here we use the hashmap or call the service and send the user details
     
     if(this.users.containsKey(username)){
       return this.users.get(username); // sends the userdetails since the hashmap created that way.
     }
     // if we don't find username in the hashmap
      throw new UsernameNotFoundException("Couldn't find username "+username);
    }
 }

Class CustomUserDetails implement UserDetails{
// declare to hold the authorities
  private final Set<GrantedAuthority> authorities ;
  private final String username,password;
  private final boolean active;
// we will initalize a construtor to get the values

// this is a simeple domain 
public CustomUserDetails (String username, String password, boolean active, String ... authroities){
  this.username=username;
  this.password=password;
  this.active = active;
  this.authorities = Stream.of(authorities)
    .map(SimpleGrantedAuthority::new)  // this is shortcut of a -> new SimpleGrantedAuthority()
    .collect(Collectors.toSet(); // the passed in value is converted as set here
}
  @Override 
  public Collection<? extends GrantedAuthority> getAuthotities (){
  // since we have set these values in the constructor we now send these
     return this.authorities;
  }
  
  @Override
  public String getPassword() {
    return this.password;
    }
    
  @Override
  public boolean isActive(){
    return this.active;
   }
   //other override variables 
}
```
 - Note:
 ```
 - If we have the records of type UserDetails, then it would be easy.
 - Say we have a database table user, and in this case we can have a UserDetails implementation that wraps and deligates the important bits to that user.
 - UserDetails is a simple interface, which has an authority(), username, etc
 ```
##### Encoding password is more critical

```java
//in the above example of custom user details
// the delegating password encoder, is a composite of mulitple encode option.
// this encoder contains different options as a map will encode based and match the attempted value with the encoded value.
// delegating encoder prefix the algorithm used "{bcrypt}.." which is used to identify
// we can delegate the password to be encoded by the factory
 @Bean
 PasswordEncoder passwordEncoder(){
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
 }
 
 // so the bean we injected with password will be updated - but this still for development
 // if this is a form based, then the password will be coming from login page
 @Bean
  CustomUserDetailsService customUserDetailsService(){
   Collection<UserDetails> users = Arrays.asList(
       new CustomUserDetails("thiru",passwordEncoder().encode("password"), true, "USER");
     );
   return new CustomUserDetailsService(users);
  }
  
  // in case if we need to provide a specific encoder from the delegating encoder 
  
  @Bean
  PasswordEncoder oldPasswordEncoder(){
    String md5= "md5";
      return nw DelegatingPasswordEncoder(md5,Collections.singletonMap(md5, new MessageDigestPasswordEncoder(md5));
  }
```

- Note:
```
 - in case of migrating from one algorithm to different one, better use case to create an event to Users and ask them to provide a new one. so new one inserted will be new encoded one.
```
  - In a scenario, if we nee to udpate the old password encoded with the new password encoder, automatically when the user updates, using `UserDetailsPasswordService`

```java
// spring security 2.1.0 build onwards we can use the UserDetailsPasswordService
@SpringBootApplication
public class CustomAuthenticationApplication{
  
  // without the password encoder bean the application will fail
  // this is the new encoder using bycrypt (default) at this time.
  @Bean
  PasswordEncoder passwordEncoder(){
    return NoOpPasswordEncoder.getInstance(); // this is strictly for development purpse only
  }
  
   @Bean
  PasswordEncoder oldPasswordEncoder(){
    String md5= "md5";
      return nw DelegatingPasswordEncoder(md5,Collections.singletonMap(md5, new MessageDigestPasswordEncoder(md5));
  }
  
  @Bean
  CustomUserDetailsService customUserDetailsService(){
   Collection<UserDetails> users = Arrays.asList(
    // encoding with the old Md5 encoder, this is to simulate how to user UpdatePasswordService
       new CustomUserDetails("thiru",oldPasswordEncoder().encoder("password"), true, "USER");
     );
   return new CustomUserDetailsService(users);
  }
}
@RestController
class WelcomeController{
  @GetMapping("/welcome")
  String greet(Principal p ){
    return "welcome "+p.getName();
  }
}

// with the main spring application is created
@Configuraton
@EnableWebSecurity
class CustomSecurityConfig extends WebSecurityConfiugreAdaptor {
   
   @Override 
   protected vod configure(HttpSecurity http)throws Exception {
      http.httpBasic();
      http.authorizeRquests().anyRequest().authenticated();
   }
 }
 
 @Log42J //from lombok
 //  @Service  - comment out this and lets the be injected as bean in the main app
 class CustomUserDetailsService implemtn UserDetailsService,
 UserDetailsPasswordService{  // this interface has a method updatePassword
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    //initialize user in the constructor
    public CustomUserDetailsService(Collection<Userdetails> users){
    // map.put (key,value)
      users.forEach(users-> this.user.put(users.getUsername(), user));
    }
    
    // Spring security will automatically called for us with user 
    // and new password. Since the password is no longer matching.
    //once updates we will need to update the identity store
    // this interface takes old user, and newpassword 
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword){
    // incliding a log statement
    
    log.info("updated password for user " +user.getUsername());
      //update the usedetails map here, or persist it in the database/identity store.
      this.users.put(user.getUsername(), new CustomerUserDetails(
        user.getUsername(),
        newPassword,  //setting up new password
        user.isEnabled(),        user.getAuthorities().stream.map(GrantedAuthorities::getAuthorities).collect(Collectors.toList()).toArray(String[]::new)
        
        // the aobve is different form of this - user.getAuthorities().stream.map(ga -> ((GrantedAuthority) ga).getAuthority()).collect(Collectors.toList()).toArray(new String[0])
        };
      return this.loaduserByName(user.getUsername());  // username is the key in hash map, but that needs to be validated to true or false, so call the delicate method.
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
     
     // here we use the hashmap or call the service and send the user details
     
     if(this.users.containsKey(username)){
       return this.users.get(username); // sends the userdetails since the hashmap created that way.
     }
     // if we don't find username in the hashmap
      throw new UsernameNotFoundException("Couldn't find username "+username);
    }
 }

Class CustomUserDetails implement UserDetails{
// declare to hold the authorities
  private final Set<GrantedAuthority> authorities ;
  private final String username,password;
  private final boolean active;
// we will initalize a construtor to get the values

// this is a simeple domain 
public CustomUserDetails (String username, String password, boolean active, String ... authroities){
  this.username=username;
  this.password=password;
  this.active = active;
  this.authorities = Stream.of(authorities)
    .map(SimpleGrantedAuthority::new)  // this is shortcut of a -> new SimpleGrantedAuthority()
    .collect(Collectors.toSet(); // the passed in value is converted as set here
}
  @Override 
  public Collection<? extends GrantedAuthority> getAuthotities (){
  // since we have set these values in the constructor we now send these
     return this.authorities;
  }
  
  @Override
  public String getPassword() {
    return this.password;
    }
    
  @Override
  public boolean isActive(){
    return this.active;
   }
   //other override variables 
}
```
##### Audit logs - to see what is going on the system. Who is authenticating the system example.
  - using spring boot actuators, just add the dependencies 
  - in order to expose the endpoint, we can configure it in application.properites
     - update `management.endpoints.web.exposure.include=*` ; * - is everything exposed
     - Note: since we inclded spring security, we need to have username password to access the endpoint.
  - once application is up, hit `http://localhost:8080/actuator` to view the list of endpoints exposed.
      - `http://localhost:8080/actuator/auditevents` endpoint provides information about who is authenticated etc. Tip: use jq util to format the json pay load.
      - events are displayed successful and unsuccessfule authentications.
