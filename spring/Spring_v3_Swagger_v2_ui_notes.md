Spring Open API 

- Open API is a specification and Swagger is an implementation.

Spring boot 3.0.0

In order to include the OpenAPI in spring boot we need to add the dependencies

```xml
<dependency>
	<groupId>org.springdoc</groupId>
	<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
	<version>2.0.0-RC1</version>
</dependency>
```

- Other spring boot dependecies in pom.xml

```xml 
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.0.0-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.spring.demo</groupId>
	<artifactId>actuator-app</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>actuator-app</name>
	<description>Simple project</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi</artifactId>
			<version>2.0.0-RC1</version>
			<type>pom</type>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
	<repositories>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>
	<pluginRepositories>
		<pluginRepository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</pluginRepository>
		<pluginRepository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</pluginRepository>
	</pluginRepositories>
</project>
```

With the below controller we can access the endpoints
  - Note, with just adding the dependecies for openapi the basic 

```java
package com.spring.demo.actuatorapp;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AppController {

    List<Employee> employees;

    public AppController(){
        employees = new ArrayList<>();
        loadEmployees(employees);
    }

    private void loadEmployees(List<Employee> items) {
        items.add(Employee.builder().id(1).name("Tom").department("IT").build());
        items.add(Employee.builder().id(2).name("Jack").department("Accounts").build());
        items.add(Employee.builder().id(3).name("Adam").department("HR").build());
    }

    @GetMapping("/employee/{empName}")
    public Employee getEmployee(@PathVariable String empName){
        Optional<Employee> employee = this.employees.stream()
                         .filter(item -> item.name().equals(empName)).findFirst();
        return employee.orElseGet(()-> Employee.builder().build());
    }

    @GetMapping("/employees")
    public List<Employee> getEmployees(){
        return  this.employees;
    }
}

@Builder
record Employee(
        String name,
        String department,
        long id
        ){}
```

[openapi spec documentation](https://springdoc.org/v2/#Introduction)

Just by adding the openapi dependency we get the default Swagger ui at below endpoint

- The swagger ui 
http://localhost:8080/swagger-ui.html 
or 
http://localhost:8080/swagger-ui/index.html

- Raw openapi spec 
http://localhost:8080/v3/api-docs

In order to load the swagger-ui in a custom url we can use

```
# swagger-ui custom path
springdoc.swagger-ui.path=/openapi-spec.html
```

> **Note:**
> after making the changing the custom url, the swagger-ui threw exception in browser
> Failed to load the remote configuration
> looks like we need to Empty browser cache and do hard refresh", When I used incognito browser it worked.

To display the actuator endpoint in the Swagger ui, use below properties in application.properties file

```
springdoc.show-actuator=true
```

### We can expose the Swagger UI in a different port other than the application port.
- The actuator endpoint can be displayed in different port other than the actual application port. This is done using the management properties.

- we can configure the Swagger UI to this Manangement port where Actuator is exposed
- in order to achieve that we need to use below configuration in application.properties

```
# using below properties, expose the actuator in this port
management.server.port=9090

springdoc.use-management-port=true
# This property enables the openapi and swagger-ui endpoints to be exposed beneath the actuator base path.
management.endpoints.web.exposure.include=openapi, swagger-ui
```

- Adding the above configuration, hitting http://localhost:9090/actuator lists all the endpoint.

```json
{"_links":
 {
  "self":{"href":"http://localhost:9090/actuator","templated":false},
  "openapi":{"href":"http://localhost:9090/actuator/openapi","templated":false},
  "swagger-ui":{"href":"http://localhost:9090/actuator/swagger-ui","templated":false}
 }
}
```

- in production if we don't need the swagger endpoint to be exposed we can use below configuration
```
# Disabling the swagger-ui
springdoc.swagger-ui.enabled=false
```
- Below will disable the springdoc-openapi spec raw generation
```
# Disabling the /v3/api-docs endpoint
springdoc.api-docs.enabled=false
```

- Adding more documentation information over the endpoint
- The tags are used to group the documentation

```
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
//....
    @Operation(summary="Get Employees", description = "list of employees", tags = "Get")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Employee list",
            content = {@Content(mediaType="application/json", schema=@Schema(implementation=Employee.class))}),
            @ApiResponse(responseCode = "404",description = "Not found",
            content=@Content)
    })
    @GetMapping("/employees")
    @CrossOrigin  // This resolved CORS issue
    public List<Employee> getEmployees(){
        return  this.employees;
    }
//..
```
