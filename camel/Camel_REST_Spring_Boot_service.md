  - Below is the code or details for using Camel-REST and Spring boot to expose a Micro-service
  
  - Create a maven project with spring web, camel depedency.
     - The service can be exposed using `servlet` or `restlet` component.
     - for servlet, use the camel-servlet-starter dependency
     - for restlet, use the camel-restlet depdenecy. Note, when using restlet, no need to include the spring-web-starter depdencies.
     
   
   pom.xml
 ```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.3.5.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.camel.service</groupId>
	<artifactId>camel-rest</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>camel-rest-service</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>11</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
         <dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-spring-boot-starter</artifactId>
			<version>2.24.0</version>
		</dependency>
		 <dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-servlet-starter</artifactId>
			<version>2.24.0</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-jackson</artifactId>
			<version>2.24.0</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
 ```
 
 MainApplication, where @SpringbootApplication is annotated.
 ```java
 package com.camel.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CamelRestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelRestServiceApplication.class, args);
	}

}
 ```

Using RouteBuilder in a component class so Rest configuration can be exposed.
```java
package com.camel.service;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class SimpleRouter extends RouteBuilder{

	@Override
	public void configure() throws Exception {
    
    //restconfiguration component used here is servlet, we can use restlet too
    // for using restlet component, not need for a spring web dependency, add restlet dependency
    // below code should work as such.
		restConfiguration().component("servlet")
		.port(9090).host("localhost").bindingMode(RestBindingMode.auto);

    //if we didn't use the produces as json, the brower has difficulties in displayig content
    // as we used the RestBindingMode.auto above
		rest().get("/hello").produces("application/json")
		.route().setBody(constant("Welcome to camel java dsl "));
	}
}
```
 - application.properties, only for the servlet component (not required for restlet component)
 - we need to setup the context path 
 ```properties
 # context-path is required only for servlet component.
 camel.component.servlet.mapping.context-path=/*
 server.port=9090
 ```
 
 - After the above configuration is set in the code, run the MainApplication
 - in browser, issue http://localhost:9090/hello, a response should be displayed.
 
