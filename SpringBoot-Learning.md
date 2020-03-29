### Use [spring intitializer](https://start.spring.io) to create the spring boot project with web.

  - Select apporpriate options (Maven, Java jdk 11).
  - Download the zip file, and extract to setup with appropriate IDE (Intellij CE IDE)
  
Spring is opnionated framework, which has default configuration to start the application.

## Profile:

` application.properties` is a the default properties file present within the `/main/resources/`.

```
server.port=9090
```

### Using specific `application.properties` as profile.

##### Multiple `application.properties` file for different environments.

For example, we have below files under the resource directory of spring-boot project

`application-dev.properties` file content
```
server.port=9091
```

`application.properties` file content
```
server.port=9090
```

To make the application to use the dev properties in above case, when starting the application use -Dspring.profiles.active=dev

##### using maven to tell spring to use specific profile (the application-dev.properties) file will be used
```
$ mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#####  using java as (JVM arguments using -D)
```
$ java -jar -Dspring.profiles.active=prod application.jar
```

##### using java parameter (also using the location of the config file if it is external of the jar file)
```
$ java jar application.jar --spring.profiles.active=dev --spring.config.location=c:\config
```

#### Defining profile using `@profile` annotation

 - Use the `@profile('dev')` annotation on the servce that is needs to be used for specific environment.
 - In `application.properties` specify the active profile usign `spring.profiles.active=dev`.
 
 The application will use specific profile objects within the application code.

**`Note`**: 
  - In case of application.properties if there is specific property file defined any properties value missed in the specific property will be identifed by Spring from the applicaion properties itself.
 
### @ComponentScan

##### when the controller is in different package from the package where the SprinBoot main application is present, then use `@ComponentScan("com.pacakge.name.*")` to help spring context to discover the controller or service.

### `@RequestParam` and `@RequestBody` Example

##### EmployeeController.java
```java
package com.restdemo.app.controller;

import com.restdemo.app.Data.Employee;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {

    @RequestMapping("/employee")
    public Employee getEmployee(@RequestParam(value = "employeeId",defaultValue = "0",required = false) long employeeId,
                          @RequestParam(value= "employeeName", defaultValue = "default empName", required = false) String employeeName){
        return new Employee(employeeId,employeeName);
    }
    /*Note: the @RequestMapping annotation property is value not name.*/
    @RequestMapping(value="/register/employee",method= RequestMethod.POST)
    public String postEmployee(@RequestBody Employee employee){
        return "Employee info "+employee.getEmployeeId()+" && " + employee.getEmployeeName() +" received at server side.";
    }

}

```
```
Output:
  - once the application is deployed
  - use Postman or java code to connect to the API at 
       - GET : http://localhost:8080/employee
       - POST: http://localhost:8080/register/employee (update the Body with json content, content type application/json)
```
### `@ConfigurationProperties`
- A way to group the properties .
- These can be made type-safe configuration properties.
- These properties can be inject in code, avoiding the need for using `@value` annotation everywhere.
- .properties and .yaml is supported

##### How to achive it.
- using `@component` tagged with `@ConfigurationProperties`
- The `@CongigurationProperties` should use the prefix, and the `@component` class properties should have the same name as the one in application properties.
- Within the `@Component` validation can be performed as needed like `@Notnull`, etc. 

```properties
server.port=9090

default.employee.employeeId=1
default.employee.employeeName=default one

custom.employee.employeeId=-1
custom.employee.employeeName=minus one
custom.employee.greetings=hello from custom properties
```

```java
package com.restdemo.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//Default property of @configurationpropertiees is prefix
@ConfigurationProperties("custom.employee")
public class ConfigurePropertiesDemo {
    //The name of the value should match the one present in properties file
    private long employeeId;
    private String employeeName;
    private String greetings;

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getGreetings() {
        return greetings;
    }

    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }
}
```

```java
package com.restdemo.app.controller;

import com.restdemo.app.Data.Employee;
import com.restdemo.app.config.ConfigurePropertiesDemo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {

    @Value("${default.employee.employeeName}")
    private String empName;
    @Value("${default.employee.employeeId}")
    private long empId;

    @Autowired
    private ConfigurePropertiesDemo configuration;

    @RequestMapping("/employee/custom")
    public Employee customEmployee(@RequestParam(value = "employeeId",defaultValue = "0",required = false) long employeeId,
                                @RequestParam(value= "employeeName", defaultValue = "default empName", required = false) String employeeName){
        System.out.println(configuration.getGreetings());
        //The configuration object is created using @Component and @ConfigurationProperties
        //so the required properties can be grouped as needed.
        return new Employee(configuration.getEmployeeId(),configuration.getEmployeeName());
    }

    @RequestMapping("/employee/default")
    public Employee defaultEmployee(@RequestParam(value = "employeeId",defaultValue = "0",required = false) long employeeId,
                                   @RequestParam(value= "employeeName", defaultValue = "default empName", required = false) String employeeName){
        //The values of the employee object is from default properties group
        //defined in application.properties using @Value annotation
        return new Employee(empId,empName);
    }
}
```

### using `yaml` file

 - Spring application can automatically identify the `application.yml` file.
 - The resource file is expected to have only `application.yml` file.
 
 In the old project structure remove the application.proeprties and use the appplication.yml file.
 
 There is no need for any code change in the controller class.

Converting the application.properties to application.yml from the above example.
```yaml

server:
  port=9090

default:
  employee:
    employeeId: 1
    employeeName: default one

custom:
  employee: 
    employeeId: -1
    employeeName: minus one
    greetings: hello from custom properties
```
