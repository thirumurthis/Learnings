## using SpringBoot AOP to validate input

In this blog will demostrate using SpringBoot AOP to perform input validation. 

When building REST based application in certain circumstence we might need to validate the JSON payload before passing request to the repository layer.


To validate the input, we do have other options like using the hibernate-validator,  using the `@NotNull`, `@NotEmpty`, etc. in the data entity object.


- Use `start.spring.io` to create the SpringBoot application with 
   - `spring web` and `lombok`

- Add the AOP dependencies in the pom.xml

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

- Below code also includes
  - Using Lombok data to use builder pattern using `@Builder`
  - Creating custom exception extending RuntimeException
  - Using annotation on the AOP `@Around` advice to intercept the method calls.
  - Java 17 feature of assigning the variable directly when validating using `instanceof` keyword
  - Using `java.utils.Objects` to validate null.

The SpringBoot AOP, can only be used for intercepting the method. For advanced we can use AspectJ in the application.

- Create Spring Annotation, which is configured in the Aspect around advice.

```java
package com.app.demo.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateInputRequest {
}
```

- Below code defines the Aspect and register the annotation in around advice.
- Now, using the annotation in `@Controller` or `@Service` layer on any method the `Around` aspect will be invoked and intercepted.

```java 
package com.app.demo.config;

import com.app.demo.InvalidInputException;
import com.app.demo.dto.Customer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
@Slf4j
public class ValidationAspectConfig {

    //Using the Around advice, with the Annotation we created
    //once the annotation is created placing the annotation 
    //over the method will invoke this call
    
    @Around("@annotation(ValidateInputRequest)")
    public Object validateInput(ProceedingJoinPoint  pjp) throws Throwable {

        log.info("AOP intercept for method - "+pjp.getSignature());
        
        Object[] requestObject = pjp.getArgs();
        if ( requestObject.length >0  &&requestObject[0] instanceof Customer customers ){
        
            // first object in the arguments
            if (Objects.isNull(customers)){
                throw new InvalidInputException("Input invalid - Cannot be null");
            }
            
            //Performing validation for the input request intercepted and received
            ValidateInput.validateRequest(customers);
            
        }else{
            log.info("input object is not of customer instance");
        }
        
        //just obtain the obj from the ProceedingJoinPoint
        Object obj = pjp.proceed();

        log.info("Completed executing the AOP intercept");
        return obj;
    }
}
```


- Class to validate the input Pojo, in this case Customer Pojo

```java
package com.app.demo.config;

import com.app.demo.InvalidInputException;
import com.app.demo.dto.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ValidateInput {

    public static void validateRequest(Customer customer){

       if( Objects.isNull(customer.getName()) || customer.getName().isEmpty()){
           throw new InvalidInputException("Invalid input - name cannot be null");
       }
       if( Objects.isNull(customer.getAddresses())
               || (Objects.nonNull(customer.getAddresses()) && customer.getAddresses().isEmpty())){
           throw new InvalidInputException("Invalid input - address cannot be null");
       }
    }
}
```

- Pojo class

```java 
package com.app.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    
    public String addressLine1;
    public String addressLine2;
    public String aptNo;
    public String city;
    public String state;
    public long zipcode;

}
```
```java
package com.app.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    public String name;
    public long id;
    public String department;
    public List<Address> addresses;
}
```

- Controller 

```java
package com.app.demo.controller;

import java.util.List;

import com.app.demo.config.ValidateInputRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.demo.dto.Customer;
import com.app.demo.service.CustomerService;

@RestController
@RequestMapping("/api")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;

    @GetMapping("/customer")
    // the annotation can be used at controller method, commented for now
    //@ValidateInputRequest
    public ResponseEntity<List<Customer>> getCustomers(){
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PostMapping("/customer")
    //@ValidateInputRequest
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer){
        return ResponseEntity.ok(customerService.addCustomer(customer));
    }
}
```

- Service

```java
package com.app.demo.service;

import com.app.demo.config.ValidateInputRequest;
import com.app.demo.dto.Address;
import com.app.demo.dto.Customer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    static List<Customer> inMemoryDb = new ArrayList<>();

    public List<Customer> getAllCustomers() {
        return fetchCustomers();
    }

    //The annotation which will be intercepted by AOP
    @ValidateInputRequest
    public Customer addCustomer(Customer customer) {
        inMemoryDb.add(customer);
        return customer;
    }

    protected List<Customer> fetchCustomers() {

        return inMemoryDb;
    }

    static {
        Address address1 = Address.builder()
                .addressLine1("work adddress of customer1")
                .city("city1").build();
        Customer customer1 = Customer.builder()
                .name("user1")
                .addresses(List.of(address1))
                .build();

        Address address20 = Address.builder()
                .addressLine1("work address customer2")
                .city("city1").build();
        Address address21 = Address.builder()
                .addressLine1("home address customer2")
                .city("city1").build();

        Customer customer2 = Customer.builder()
                .name("user2")
                .addresses(List.of(address20, address21))
                .build();

        inMemoryDb.add(customer1);
        inMemoryDb.add(customer2);
    }
}
```

- Exception 

```java
package com.app.demo;

public class InvalidInputException extends RuntimeException{

    public InvalidInputException(String message){
        super(message);
    }
}
```

- SpringBoot Application entry point

```java
package com.app.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
public class DemoApplication {

   public static void main(String[] args) {
      SpringApplication.run(DemoApplication.class, args);
   }
}
```

The project structure looks like below,

![image](https://user-images.githubusercontent.com/6425536/197376393-af8db8c9-d9e8-4aa5-8598-c66cba5b8320.png)

- The output with the below response will trigger an exception output
- 
```
curl -X POST -H "Content-Type:application/json" http://localhost:8080/api/customer -d '{"name":"","id":0,"department":null,"addresses":[{"addressLine1":"work adddress of customer3","addressLine2":null,"aptNo":null,"city":"city1","state":null,"zipcode":0}]}'
```

- Exception message

```
{"timestamp":"2022-10-23T04:42:35.931+00:00","status":500,"error":"Internal Server Error","trace":"com.app.demo.demo.InvalidInputException: Invalid input - name cannot be null\r\n\tat com.app.demo.demo.config.ValidateInput.validateRequest(ValidateInput.java:19)\r\n\tat com.app.demo.demo.config.ValidationAspectConfig.validateInput(ValidationAspectConfig.java:38)\r\n\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)\r\n\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n\tat java.base/java.lang.reflect.Method.invoke(Method.java:568)\r\n\tat org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:634)\r\n\tat org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:624)\r\n\tat org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:72)\r\n\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)\r\n\tat org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)\r\n\tat org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97)\r\n\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)\r\n\tat org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)\r\n\tat org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:708)\r\n\tat com.app.demo.demo.service.CustomerService$$EnhancerBySpringCGLIB$$e764f31c.addCustomer(<generated>)\r\n\tat com.app.demo.demo.controller.CustomerController.addCustomer(CustomerController.java:29)\r\n\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)\r\n\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\r\n\tat java.base/java.lang.reflect.Method.invoke(Method.java:568)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:150)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:895)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808)\r\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1071)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:964)\r\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)\r\n\tat org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909)\r\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:696)\r\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)\r\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:779)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:227)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\r\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\r\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\r\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\r\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:117)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\r\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:197)\r\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97)\r\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:541)\r\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:135)\r\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92)\r\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78)\r\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:360)\r\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:399)\r\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65)\r\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:893)\r\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1789)\r\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)\r\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\r\n\tat java.base/java.lang.Thread.run(Thread.java:833)\r\n","message":"Invalid input - name cannot be null","path":"/api/customer"}
```
