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
    this.log.info("request time take " + diff);
 }
 
 @Override
 public void destroy () {
 }
```

