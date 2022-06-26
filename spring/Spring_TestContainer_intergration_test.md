### Using TestContainers for Integration testing in SpringBoot application

In this blog will demonstrate how to use TestContainers at basic level to perform integration testing.

- With reference to my [blog](https://thirumurthi.hashnode.dev/idea-of-building-spring-boot-app-and-deploying-in-heroku-platform) during local development I used H2 database.
- Heroku provided  PostgreSql option, so I decided to use it, i need to switch between H2 and PostgreSql between development and deployment.
- Recently came accross the TestContainer, which can be used for such situations.

- Here i will be creating an SpringBoot Library service and use Postgresql to persist the data.
- In the test case we will use WebTestClient to access the service
- Additionally, in `application.properties` have demonstrated how to enable all actuator endpoint. Not suitable for production.
- I am using Java JDK 17.0.1 and SpringBoot v2.7.1.

#### Create the SpringBoot Maven project with dependencies

  - Use [start.spring.io](https://start.spring.io) to create the maven based project with below dependencies
     - spring-boot-starter-web
     - spring-boot-starter-data-jdbc
     - spring-boot-starter-actuator
     - spring-boot-starter-webflux (WebTestClient uses this Reactive client library)
     - spring-boot-starter-test
     - flyway-core
     - testcontainers (this will be added as testcontainers-bom in pom.xml)

  - The maven pom.xml file looks like below.

```pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.1</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.library</groupId>
	<artifactId>example</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>example</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>17</java.version>
		<testcontainers.version>1.17.2</testcontainers.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jdbc</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webflux</artifactId>
		</dependency>
		<dependency>
			<groupId>org.flywaydb</groupId>
			<artifactId>flyway-core</artifactId>
		</dependency>

		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.testcontainers</groupId>
			<artifactId>junit-jupiter</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.testcontainers</groupId>
			<artifactId>postgresql</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.testcontainers</groupId>
				<artifactId>testcontainers-bom</artifactId>
				<version>${testcontainers.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

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

#### Create the service with RestController and Repository annotation

  - Below are self explinatory class, created under the same package (com.library.example)
  - The `record` class is a feature included after Java 14+, this is an immutable class
  
- Controller class

```java 
package com.library.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController{

    //constructor injection
	private final BookRepository bookRepo;

	BookController(BookRepository bookRepo){
		this.bookRepo = bookRepo;
	}

	@GetMapping()
	Iterable<Book> getAllBooks(){
		return this.bookRepo.findAll();
	}

	@PostMapping()
	Book addNewBook(@RequestBody Book book){
      return this.bookRepo.save(book);
	}
}
```

- Repository class

```java
package com.library.example;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CrudRepository<Book, Long>{}
```

```java
package com.library.example;

import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
//Immutable object creation
record Book(@Id Long id,String title) { }
```

- Main Application entry point

```java 
package com.library.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleApplication.class, args);
	}
}
```

Step 3: Update the `application.properties` with Database and Acutator configuration
   - Note i am exposing all the actuator endpoint using `*`, but in production this should be restricted.
   
```
spring.datasource.url = jdbc:postgresql://localhost:5432/book_catalog
spring.datasource.username = user
spring.datasource.password = password

# enable actuator endpoint - here enabling all the endpoint
# using actuator/flyway will list the migration we have done

#End points
# actuator/flyway
# actuator/prometheus  -we can use graphana to visualize metrics
# actuator/heapdump  - to get the memory statistics

management.endpoints.web.exposure.include=*
management.endpoint.health.show-components=always
management.endpoint.health.show-details=always

# When deploying this application in in kubernetes.
# by default the probes will be enabled.
# just demonstrating how to enable it specifically
management.endpoint.health.probes.enabled=true
```

Step 4: Create Integration test case

- We need to specify the spring to use random port for testing, by using `WebEnvironment` passed to `@SpringBootTest` annotation.

```
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

- We also override the PostgreSql database url specified in `application.properties` using `@TestPropertySource` annotation like below. 

```
  @TestPropertySource(properties = "spring.datasource.url = jdbc:tc:postgresql:15:///")
```
  - Already the TestContainer dependency is included in pom.xml, so by specifying the jdbc url in TestContainer specific format `jdbc:tc:`, executing this integration test, spring will autoamtically download, start and perform test using the docker image.
 
Note:
  - When running below test case, make sure to run the Docker Desktop if it is not already running.
 
```java
package com.library.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

//during integration we tell spring to use random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
				properties = "spring.main.lazy-initialization=true")

//Below annotation is used for enabling the Postgresql test container.
// a dynamic docker instance will be spinned up and tested
// we pass in the database url, which will be overidden from the applicaiton.properties
//the url convention can checked in the documentation, we use :tc: in the url.
//:15 is the version we are using 
@TestPropertySource(properties = "spring.datasource.url = jdbc:tc:postgresql:15:///")

//Need to add since the context is not loaded part of the webtestclient
@AutoConfigureWebTestClient(timeout = "PT30S") // we can also specift "30000" instead of PT30S

class ExampleApplicationTests {

	//using WebTestClient which can be used to call the 
	// book service client and check if the book is added correctly
    // Autowired the object directly in the test method

	@Test
	void addBook(@Autowired WebTestClient webTestClient) {
		var bookToCreate= new Book(null, "Test Title");

		webTestClient
		.post()
		.uri("/books")
		.bodyValue(bookToCreate)
		.exchange() //At this point the request is sent
		.expectStatus().is2xxSuccessful() //expect the status ok
		.expectBody(Book.class)
		.value(book -> {
			assertThat(book.id()).isNotNull();
			assertThat(book.title()).isEqualTo(bookToCreate.title());
		});
	}
}
```

#### Issues 

- Timeout exception while running the test case initally threw below exception

```
 java.lang.IllegalStateException: Timeout on blocking read for 5000000000 NANOSECONDS
```

Solution:
  - Add timeout of 30 seconds like `@AutoConfigureWebTestClient(timeout = "PT30S")` to resolve it.

#### To run the Library service in local, we can use the PostgreSql docker instance and Curl command to Post message.

- Below command will start an PostgreSql as a docker isntance and the application can connect to it.

```
docker run -it --rm --name postgres-dev -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_USER=user -e POSTGRES_DB=book_catalog postgres
```

- Once started should be able to see message like below.

```
PostgreSQL init process complete; ready for start up.

2022-06-26 16:49:07.200 UTC [1] LOG:  starting PostgreSQL 14.4 (Debian 14.4-1.pgdg110+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit
2022-06-26 16:49:07.201 UTC [1] LOG:  listening on IPv4 address "0.0.0.0", port 5432
2022-06-26 16:49:07.201 UTC [1] LOG:  listening on IPv6 address "::", port 5432
2022-06-26 16:49:07.227 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
2022-06-26 16:49:07.256 UTC [62] LOG:  database system was shut down at 2022-06-26 16:49:07 UTC
2022-06-26 16:49:07.274 UTC [1] LOG:  database system is ready to accept connections
```

- Start the Library service from IDE and using executable jar `java -jar <executable.jar>`, use below Curl command to POST a Book message. Use git bash terminal

```
$ curl -X POST http://localhost:8080/books -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{"title" : "from curl command"}'
```

- Output response of Curl command

```
{"id":1,"title":"from curl command"}
```

#### Output
![image](https://user-images.githubusercontent.com/6425536/175827507-9a8c82af-86c9-4f32-b85b-ebdf1eec5c74.png)
