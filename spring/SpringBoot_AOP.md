## Validate input JSON payload with SpringBoot AOP

In this blog will demonstrate an use case to use SpringBoot AOP to perform input validation in the `@Controller` or `@Service` layer level.

### Use case - Validate payload in controller or service layer

 - When building REST based application at circumstance we might need to perform customized validation on the JSON payload before passing request to the repository layer or further next layer.

> **Info:-**
> The validation can be performed using the `hibernate-validator`, with built-in annotation `@NotNull`, `@NotEmpty`, etc. defined in the entity class.

- Create SpringBoot project using `start.spring.io` with `spring web` and `lombok` dependencies.
- Add the Spring starter AOP dependencies in the `pom.xml`, like below

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```


- In this blog have included few additional points,

  - How to easily create builder pattern on POJO using `@Build` annotation from Lombok project 
  - Extending RuntimeException to create custom exception used to handle Input validation in this case
  - Custom Spring annotation creation and registering it in AOP `@Around` advice which will be invoked and used to intercept the method calls
  - `java.utils.Objects` to validate null. Objects contains few other methods which is not detailed here
  - Java feature to perform `instanceof` check and directly store the converted data to variable
    ```java
     // Code where the Object instanceof Customer and the converted data stored in customers
     if ( requestObject.length >0  && requestObject[0] instanceof Customer customers ){
            // first object in the arguments
            if (Objects.isNull(customers)){
                throw new InvalidInputException("Input invalid - Cannot be null");
            }
      //.....
    ```

> **Info:-**
>  SpringBoot AOP only used to intercept methods. 
>  For advanced feature we need to use AspectJ, for example to check if fields have changed.

### Code

#### Create Spring Annotation
- Create Spring Annotation, which will be configured in the Aspect `@Around` advice class.

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

#### Create Aspect to intercept the method calls

- Below code defines the Aspect using `@Aspect` annotation (note, this aspect needs to be regiestered as a bean using `@Component`).
- The custom annotation `@ValidateInputRequest` in the previous code snippet is registered in the `@Around` advice.
- The Aspect class `validateInput()` method will be invoked if the custom annotation `@ValidateInputRequest` is annotated on the method defined in `@Controller` or `@Service` layer.

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

    //Using the Around advice, with the Annotation created and place the annotation 
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

#### Custom validation logic to validate input JSON payload

- Class where we perform the basic validation, to demonstrate basic validation
   - Where we check if the input Customer JSON payload received contains name attribute and should NOT be empty.
   - Also checking if the address attributes should be available.

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

#### Customer and Address POJO class

- Simple POJO class defining Customer related info

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

#### Controller class

- A simple controller object

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

#### Service class

- Service layer where we have included the custom annotation `@ValidateInputRequest`
- Have used List of items to load when the application is successfully started

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

#### Custom Exception 

- Creating custom Exception extending RuntimeException, used to throw this exception when validating the input JSON payload.

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

### Project structure snapshot

![image](https://user-images.githubusercontent.com/6425536/197376393-af8db8c9-d9e8-4aa5-8598-c66cba5b8320.png)


### Output 

#### Curl command to send an JSON payload with empty Customer name

- The output with the below response will trigger an exception in output
- 
```
curl -X POST -H "Content-Type:application/json" http://localhost:8080/api/customer -d '{"name":"","id":0,"department":null,"addresses":[{"addressLine1":"work adddress of customer3","addressLine2":null,"aptNo":null,"city":"city1","state":null,"zipcode":0}]}'
```

- Exception message

```
{"timestamp":"2022-10-23T04:42:35.931+00:00","status":500,"error":"Internal Server Error","trace":"com.app.demo.demo.InvalidInputException: Invalid input - name cannot be null\r\n\tat com.app.demo.demo.config.ValidateInput.validateRequest(ValidateInput.java:19)
com.app.demo.demo.config.ValidationAspectConfig.validateInput(ValidationAspectConfig.java:38)
java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
java.base/java.lang.reflect.Method.invoke(Method.java:568)
org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:634)
org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:624)
org.springframework.aop.aspectj.AspectJAroundAdvice.invoke(AspectJAroundAdvice.java:72)
org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175)
org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)
org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97)
org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763)
org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:708)
com.app.demo.demo.service.CustomerService$$EnhancerBySpringCGLIB$$e764f31c.addCustomer(<generated>)
com.app.demo.demo.controller.CustomerController.addCustomer(CustomerController.java:29)\r\n\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
java.base/java.lang.reflect.Method.invoke(Method.java:568) 
........
org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1789)
org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)
org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)
org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\r\n\tat java.base/java.lang.Thread.run(Thread.java:833)\r\n","message":"Invalid input - name cannot be null","path":"/api/customer"}
```
