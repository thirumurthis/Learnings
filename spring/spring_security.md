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
      return NoOpPassowrdEncoder.getInstance(); //don't use this in production system.
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
public configure(HttpSecurity http) throws Exception {
      http.authorizeRequests()
           .antMatchers("/admin").hasRole("ADMIN")
           .antMatchers("/user").hasAnyRole("USER","ADMIN") // hasAnyRole() for more than one role usage
           .antMatchers("/").permitAll()  //permitAll the root api can be viewed by all user
           .and()
           .formLogin();
}
```
