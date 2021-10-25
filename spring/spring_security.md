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
   
How to configure Authentication?

```java

@EnableWebSecurity  //this tells this is web security (another type is method level security different then this)
public class SecurityConfig externds WebSecurityConfigurerAdaptor{
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
   
   //Since we are handling password here, we need to encrypt or hast it to store it. 
   //This can be done here by creating a PasswordEncoder bean
   
   // This is a requirement
   @Bean
   public PasswordEncoder getPasswordEncoder(){
      return NoOpPasswordEncoder.getInstance(); //don't use this in production system.
   }
}
```
-------------------

How to configure Authorization?
  - Let's say we have a simple security app there are mulitple user (in-memory) with two different role.
  - So some of the API to be accessed by one role, and another to be accessed by another role.

Note: Adding a spring-security starter to app, the spring security automatically adds the authentication screen.

- Say, we are having "/", "/user", "/admin" api's accessed by all, user & admin, only admin respectively.

How to configure the Authorization specific to API paths?
   - This can be done using `HttpSecurity` from `WebSecurityConfigurerAdaptor`.
   - To get hold of the `HttpSecurity` object is to extend `WebSecurityConfigurerAdaptor` and override `configure(HttpSecurity)` method.


```java

@EnableWebSecurity  //this tells this is web security (another type is method level security different then this)
public class SecurityConfig externds WebSecurityConfigurerAdaptor{
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
   
   //Since we are handling password here, we need to encrypt or hast it to store it. 
   //This can be done here by creating a PasswordEncoder bean
   
   // This is a requirement
   @Bean
   public PasswordEncoder getPasswordEncoder(){
      return NoOpPassowrdEncoder.getInstance(); //don't use this in production system.
   }
   
   @Override 
   public configure(HttpSecurity http) throws Exception{
      http.authorizeRequests()
           .antMatchers("/admin").hasRole("ADMIN")
           .antMatchers("/user").hasAnyRole("USER","ADMIN") // hasAnyRole() for more than one role usage
           .antMatchers("/").permitAll()  //permitAll the root api can be viewed by all user
           .and()
           .formLogin();  // formLogin is a default and this will provides a username/password form.
           
           //Note: The least accessed url should be at the top, most accessed to be at the bottom
   }
}
```
#### Represetnation of the flow and terminology:

![image](https://user-images.githubusercontent.com/6425536/130565632-79709a9d-aa2e-4273-872c-1722e2a62fe1.png)

---------------------

##### Creating the spring security project with spring starter io
- Add following dependecies and create the project.
   - `spring web starter`
   - `spring security`
   - `H2  database`
   - `jdbc api`
- Create a RestController with different api's "/", "/user", "/admin"

```java

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

// we need to tell spring about the jdbc source
@Autowired
DataSource dataSource;  // right now we have H2 database, this can be external datasource like Mysql db as well.

// Note: since we have H2 embedded database is used, the spring will auto-configure the H2 schema. 
//This is because spring has default opinion, like spring will create the default 
// If he clean database is provide, the Spring can create the default schema with authority and user tables. (refer the  withDefaultSchema() below
// Also write the user info in an sql file, when application is loaded the info will be autmatically loaded to the H2 db

@Override
public configure(AuthenticateManagerBuilder auth) throws Exception {
   auth.jdbcAuthentication() // this is the type of authentication we need to use. earlier we used in-memory now jdbc
   
   .dataSource(dataSource)  // create the schema and populate the following data
   .withDefaultSchema()
   .withUser(
       User.withUsername("username")
       .password("password")
       .roles("USER")
     )
     .withUser(
       User.withUsername("admin")
       .password("admin")
       .roles("ADMIN")
     );
}

@Bean
public PasswordEncoder getPasswordEnconder(){
  return NoOpPasswordEncoder.getInstance();
}

@Overrider
public void configure(HttpSecurity http) throws Exception {
      http.authorizeRequests()
           .antMatchers("/admin").hasRole("ADMIN")
           .antMatchers("/user").hasAnyRole("USER","ADMIN") // hasAnyRole() for more than one role usage
           .antMatchers("/").permitAll()  //permitAll the root api can be viewed by all user
           .and()
           .formLogin();
}
```
 - Note: Ideal way looks like below

```
@Override
public configure(AuthenticateManagerBuilder auth) throws Exception {
   auth.jdbcAuthentication()    
   .dataSource(dataSource);
 }
```
 - The default schema information is available in Spring documentation.
 - just using the `schema.sql` file with create tables (users, authorities) [Note this is not for production case.]
 - Create another file `data.sql` with the insert or DML queries.
 - Add this file to the resources to the spring boot app, when the spring starts this file will be loaded.
 - Using `application.properties`, h2 properties the h2 database web console can be accessed.

--------- 
- If we have our own database for user information and need to query that table instead of default tables, spring provides ways to do it. using `usersByUsernameQuery` and `authoritiesByUsernameQuery`.
- For different data source update the `application.properties`.
```
//securityconfig.java class extends WebSecurityConfigurerAdaptor
@Override
public configure(AuthenticateManagerBuilder auth) throws Exception {
   auth.jdbcAuthentication()   
   .dataSource(dataSource)
   .usersByUsernameQuery("select username,password,enabled from users "
      +"where username = ?")
   .authoritiesByUsernameQuery("select username,authority "
      +" from authorities where username =? ");
```
---------

##### Implementing spring security using JPA or external datasource

- In this case the Security configuration class extending the class `WebSecurityCondigurerAdaptor` doesn't requires a datasoruce, instead a service can be injected to retrive the user details info.

```java
public class SecurityConfig extends WebSecurityConfiguratorAdaptor{
//inject the service which will fetch info from DB
@Autowired
UserDetailsService userDetailsService;

@Override
protected configure(AuthenticationManagerBuilder auth) throws Exception{
  auth.userDetailsService(userDetailsService);
}
// use the same configure(HttpSecurity) and PasswordEncoder as in above code
}
```
 - Create a class `CustomUserDetailsService.java` implementing the `UserDetailsService` of Spring security.
 ```java
 
 @Service
 public CustomUserDetailsService implements UserDetailsService{
 
 @Override
 public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
   // This method is going to return UserDetails to security
   // We need to create the UserDetails class implementing UserDetails
   // Ideally fetch user info from DB and set this object.
   
   //For testing purpose the CustomUserDetails implemented from spring UserDetails will take a name.
    return new CustomUserDetails(username); // since we created this class below
   }
 }
 ```
 - Implement the UserDetails class 
 ```java
 public CustomUserDetail implement UserDetails{
   //There are list of method that should override.
   
   private String username;
   //Creating a constructor with empty and taking username as args
   public CustomUserDetail(String username){
       this.username = username;
   }
   public CustomUserDetail(){};
   
   @Override
   public String password(){ return "somepasssword";}
   
   @Override
   public String getUserName() { return username;}
   
   @Override
   public Collection<? extends GrantAuthority> getAuthority(){
      return Arrays.asList(new SimpleGranterAuthority("ROLE_USER");
   }
   //There are other methods to override, which we can make to return true.
 }
 ```
- Note: This UserDetail implementation is not hitting the Database, this is just to show how just hard coding the password, it any user name only with the hardcoded somepassword.

- The `loadUserByUsername()` method can also connect to the external database and provide back the `UserDetals` object back

- To hit the database for userDetails info, we can update the CustomUserDetails class

 ```java
 
 @Service
 public CustomUserDetailsService implements UserDetailsService{
 
 @Autowirded
 UserRepository userRepo;
 
 @Override
 public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
    // the repo is invked to fetch info from DB
    Optional<User> user= userRepo.findByUserName(username);
    
    user.orElseThrow(()-> new UsernameNotFound("user Not Found : " +username));
    
    return user.map(CustomUserDeatils::new).get(); // if there are not user object, we need to perform check exception.
   }
 }
 ```
 - Create a Entity class for JPA
```java 
@Entity
@Table(name="User")
public class User{
  
  @Id
  @GenerateValue(strategy = GenerationType.AUTO)
  private int id;
  private String userName;
  // other variables lke userName,password,active,roles
  //use lombok and tag as data as well since we need gettersetter
}
```

- Create a repo for User
```java
//Spring data - the datasource is defined in the application.properties file.
public interface UserRepository extends JpaRepository<User, Integer>{
    Opitional<User> findByUserName(String userName); //userName defined as in User class
}
// note the data source can also be stored in Vault and retrieved from there.
```
- Update the `CustomUserDetails` class

```java
public class CustomUserDetails implements UserDetails{
// In the earlier class remove all the content of the class
// new variables are declared.
  privae String username;
  private String password;
  private boolean active;
  // this value is comming from the Database
  private List<GrantedAutority> authorities;
  public CustomUserDetails(User user){
     this.userName = user.getUserName();
     this.password=user.getPassword();
     this.active = user.isActive();
     this.authorities = Arrays.stream(user.getRole().split(",")
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());                       
  }
  //override only those are required
  // getPassword, getGaurenteedAuthorities, getUserName, getPassword, isEnabled.  
}
```
 - Note: If we use external database, use `@EnableJpaRepositories(basePackagclasses = UserRepository.class)` in the spring applicaiton class.
 - The datasource can be configured in the application.properties.

----------------

#### using `JWT` (json web tokens):
  - JWT is commenly used for managing autorization
  - This is used for communication security.
  - Open industry specifiction `RFC 7519` explains how JWT be defined and used.

- Types of Authorization strategies:
   - Session Token
   - JSON Web Token

- HTTP protocol is a stateless protocol:
    - In order to make it a bit of state, we use cookies or session.
    - If we didn't think of session, then everytime the user accessing the application, each API or pages should be authenticated.
    - The token approach can be used to make the HTTP to be state.
    - Security needs to be managed, but expiring the session, setting session timeout.

- When the browser request to view the application, the authentication information is sent to server. The authentication is validated and server generates a session id, and sends this info to user.
- The session id is stored in cookie (this is the most common approach), so when the browser sends the request the cookies is added to the request  header.

- The disadvantage is this is applicable only for monolitic application, or we need to implement loadbalancer with sticky sessions. This apporach is difficult to scalable.

- JWT is easy to implement within the mico-services.

  - The idea of JWT is when the user/browser requests info, the server will create the JWT token (`JWT token is encoded userName or other non important properties`).

- Structure of JWT:
   - Sample JWT contans `<content1>.<content2>.<content3>` where content's are encoded strings.
   - content1 - represent `Header` (tells how this JWT is signed, with the algorithm used to verify the signature)
   - content2 - represent `Payload` ( this is the data with base64, this can be any non critical data)
   - content3 - represent `Signature` (this is created using the algorithm mentioned in header, with a secret key. this secret key is only available in server.)

- All the JWT informaiton can be decoded using base64 and view the information.
- 
 - `jwt.io` can encode the jwt token except the signature.

##### Note: JWT is mostly used for Authorization, the authentication is already done.
```

1. USER  -----> sends credentials to application -------------> SERVER
2. USER  <---- send JWT to user <------------------------------ SERVER
3. USER  ------> send JWT token back for subsequent request ---> SERVER

JWT token is passed SERVER, in request Header like below
`Authorization : Bearer <JWT-tocken>`

```
##### how to implement the Spring security using JWT

- Create an new spring boot project with `spring web` and `spring security` dependencies.
- Create a simple API for "/welcome"
- Create a class to extend the `WebSecurityConfigurerAdaptor` class
```java
@EnableWebSecurity
public SecurityConfig extends WebSecurityConfigurerAdaptor {

@Autowired
private CustomUserDetailService customUserDeatilService;
// the service will be implementing the UserDetailsService, which overrides the 
// loadUserByUsername() method, to return the user object.
  @Override
  protected void configure(AuthenticationBuilderManager auth) throws Exception {
    return auth.userDetailsService(customUserDeatilsService);
  }
}
```
- Create the service CustomUserDetailService
```java
@Service
public CustomUserDetailService implements UserDetailsService{

   @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
      return new User("user","pass", new ArrayList<>());
   }
}
```
- Above is only uses generic approach of Authentication, now to update the JWT authorization.
    - Add following dependencies to pom.xml file
    ```xml
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>  <!-- note: jjwt -->
        <version>0.9.1</version>
    </dependency>
    <dependency> <!-- below is sine we are using java 11 -->
       <groupId>javax.xml.bind</groupId>
       <artifactId>jaxb-api</artifactId>
       <version>2.3.0</version>
    </dependency>
    ```
- Create a class to extract the JWT generation part, this will be a Util class, which will create the token, etc.

```java

@Service
public class JWTManagerUtil{
   private String SECRET_KEY ="verySecretKey";
   
   public String extractUserName(String token){
     return extractclaim(token, Claims::getSubject);
     }
   
   public Date extractExpiration(String token){
     return extractClaim(token, Claims::getExpiration);
   }
   
   public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
     final Claims claims = extractAllClaims(token);
     return claimResolver.apply(claims);
   }
   
   private Claims extractAllclaims(String token){
      return Jwt.parser().setSigningKey(SECRET_KEY).parseClaims(token).getBody();
   }
   
   private Boolean isTokenExpired(String tockent){
     reurn extractExipration(token).before(new Date());
   }
   
   public String generateToken(UserDetails userDetails){
      Map<String, object> claims = new HashMap<>(); / for testing this is empty
      return createToken(claims,userDetails.getUserName());
   }
   
   private String createToken(Map<String,Object> claims, String subject){
      return JWts.builder().setClaims(claims).setSubject(subject)
                 .setIssuedAt(new Data(System.currentTimeMillis()))
                 .setExpiration(new Date(System.currentTimeMillis()+ 1000*60*60))  // the token will expire in 1 hour
                 .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
   }
   
   public Boolean validateToken(String token, UserDetails userDetails){
      final String userName = extractUsername(token);
      return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
   }
}
```
 - The spring boot already provides default formlogin. 
 - There is no need for form login, since we don't need any username or password.
    - We can create an `/authenticate` endpoint API which returns the JWT token in response. This API is not required to redirect to any other API's.
        - The user will send username/password in post request to the /authenticate endpoint
    - The client/user should be sending this token for subsequent request.
    - The code need to validate this token and authorize the request

- Handling JWT token generation 
   - Create a class to hold the username and password
```java
 public class AuthenticationRequest{
    private string username;
    private string password; 
    //define constructors, getter setters
 }
```
   - Create another class to hold the response
```java
public class AuthenticationResponse{
   private final String jwtToken;
   //corresponding getter setter.
}
```
   - Create the `/authentication` endpoint
```java
  @RestController
  publi class Authenticate{
    @RequestMapping("/welcome")
    public String welcome() { return "Welcome";}
    
    @Autowired 
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtManagerUtil jwtManagerUtil;
    
    //This is the authentication manager handle, which we can invoke authenticate() method
    @Autowired
    private AuthenticationManager authentication<anager;
    
    @RequestMapping(value="/authenticate", method=RequestMethod.POST)
    public ResponseEntity<?> generateAuthenticationToken(@RequestBody AuthenticationRequest authenticateRequest) throws Exception{
    
    try{
    //if below authentication we need to catch and throw exception
      authenticationManager.authenticate(
         new UsernamePasswordAuthorizationToken(authenticationRequest.getUserName(),authenticationRequest.getPassword()));
         } catch (BadCredentialsException e){
            throw new Exception ("Invalid username and password");
         }
        // since now validated we need to get the username and generate JWT
        // autowire the service
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final jwt = jwtManagerUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
  }
```
 - Till this point, the exposed `/authenticate` endpoint is not authenticated. Remember, the above details and code where `configure(HttpSecurity)` is not overrided yet. Check SecurityConfig class.
 - Update the SecurityConfig

```java
//SecurityConig extends WebSecurityConfigurerAdaptor {...
@Override
protected void configure(HttpSecurity http) throws Exception {
// disables and authorize for /authenticate endpoint other 
  http.csrf().disable()
      .authorizeRequests()
      .antMatchers("/authenticate").permitAll()
      .anyRequest().authenticated();
}

// also create a bean for AuthenticationManager, starting spring 2+ this needs to be overrided
@Override
@Bean
public AuthenticationManager authenticationManagerBean() throws Exception{
  return super.authenticationManagerBean();
}
```
  - Now running the aobve application accepts the username and password, returns the JWT token
  - now is postman, using a POST request to /authenticate, should return the token.

- Till now the JWT token is not being used from the incoming request on the code.
- We need to intercept all the incoming request and validate the jwt, to extract username and set to execution context. This can be achived by using fitlers `OncePerRequestFilter`.

```java
public class JwtRequestFilter extends OncePerRequestFilter {
  
  @Autowired
  private UserDetailsService userDetailsService;
  
  @Autowired
  private JwtManagerUtil jwtManager;
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse respone, Filter chain) throws ServletException, IOexception {
  
  // this is where we exampne the jwt in header, and validate it.
  
  //get the jwt from request header Authorization key
  final String authorizationHeader = request.getHeader("Authorization");
  
  //if the header contains valid jwt, extract the username
  String username = null;
  String jwt = null;
  
  // additionally validate the expiration, active etc.
  if( authorizationHeader != null && authorization.sartsWith("Bearer ")){
     jwt = authorizationHeader.substring(7);
     username = jwtManager.extractUsername(jwt);
  }
  
  //incase the token didn't had the username, fetch it and validate, set to context
  
  if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username));
    
    // since the user details is fetched, check if the jwt is valid and not expired
    if (jwtManger.validateToken(jwt,userDetails)){
    // below step will do this automatically, but sine we need to perform this
    // only when the jwt is validated.
       UsernamePasswordAuthenticationToken usernamePasswordAuthencationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
       usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
       
// this executes only when the authentication is null       securityCotnextHolder.getContext().setAuthtentication(usernamePassworduthenticationToken);
     }
  }
 }
}
```
  - Since we added filter and implemented, we need to add this to `WebSecurityConfigurerAdaptor` extended class.

```java
// SecurityConfig java

@Autowired
private JwtRequestFilter jwtRequestFilter;

@Override
protected configure(HttpSecurity http) throws Exception {
   http.csrf().disable()
   .authorizeRequests().antMatchers("/authenticate").permitAll()
   .anyrequest().authenticated()
   .and().sessionManagement()  //adding session management, saying spring not to create session.
   .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
   // this stateless makes spring not to creat session
   // add the new fiter
   http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
}
```
 - To Test the JWT implementaion above, use Postman
    - Hit the `/authenticate` endpoint with post body of username and password
    - The JWT token will be sent.
    - Add this to Header `Authorization : Bearer <jwt-token>` access `/welcome` end point. this should return welcome.
    - If the token is not provided, the access denied message will be displaed, the session is not managed.
