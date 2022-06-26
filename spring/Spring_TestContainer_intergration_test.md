### Using TestContainers for Integration testing in SpringBoot application

In this blog will demonstrate how to use TestContainers at basic level to perform integration testing.

- With reference to my [blog](https://thirumurthi.hashnode.dev/idea-of-building-spring-boot-app-and-deploying-in-heroku-platform) during local development I used H2 database.
- Heroku provided  PostgreSql option, so I decided to use it, i need to switch between H2 and PostgreSql between development and deployment.
- Recently came accross the TestContainer, which can be used for such situations.

#### What to expect?

- Will be creating a SpringBoot service and use PostgreSql to persist the data.
- In the test case we will use WebTestClient to access the service for testing.
- Additionally, in `application.properties` have enabled all actuator endpoint. This is **not** suitable for production.
- Will beusing Java JDK 17.0.1 and SpringBoot v2.7.1.

#### Create SpringBoot Maven project with dependencies

  - Use [start.spring.io](https://start.spring.io) to create the maven based project with below dependencies> - spring-boot-starter-web
>      - spring-boot-starter-data-jdbc
>      - spring-boot-starter-actuator
>      - spring-boot-starter-webflux (WebTestClient uses this Reactive client library)
>      - spring-boot-starter-test
>      - flyway-core (Flyway is used to mange the Database version automatically) 
>      - testcontainers (This will be added as testcontainers-bom in pom.xml)

Note:
  - Flyway will create dependent tables within the PostgreSql, to manage history.
  - The fly script file name should follow the standard like `V1__init_schema.sql`, `V2__load_table.sql`, etc. which need to be placed under resources/db/migration

```sql
-- Flyway will create its own version control
CREATE TABLE book(
  id bigserial PRIMARY KEY,
  title VARCHAR(25)
)
```

The structure of the project looks like below

  ![image](https://user-images.githubusercontent.com/6425536/175827877-fb26a159-d75c-413a-8c0d-07d80ca7b1b0.png)

  - The maven `pom.xml` file looks like below

```xml
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

#### Create simple REST service 

  - Below are self explanatory class, created under the same package (com.library.example)
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

- Book record immutable object

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
   
```properties
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

#### Integration test case code

- We need to specify the spring to use random port for testing, by using `WebEnvironment` passed to `@SpringBootTest` annotation.

```
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

- We also override the PostgreSql database url specified in `application.properties` using `@TestPropertySource` annotation like below. 
     
         - Already the TestContainer dependency is included in pom.xml, so by specifying the jdbc url in TestContainer specific format `jdbc:tc:`, executing this integration test, spring will autoamtically download, start and perform test using the docker image.

```
  @TestPropertySource(properties = "spring.datasource.url = jdbc:tc:postgresql:15:///")
```
  
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

#### Issues faced during testing

- Timeout exception while running the test case initally threw below exception

```
 java.lang.IllegalStateException: Timeout on blocking read for 5000000000 NANOSECONDS
```

Solution:
  - Add timeout of 30 seconds like `@AutoConfigureWebTestClient(timeout = "PT30S")` to resolve it.

#### How to run the PostgreSql instance in Docker?

- To check the SpringBoot service, from local we need to start the application and run the Docker PostgreSql Instance running.

- Below command will start an PostgreSql as a docker instance, we are exposing the port 5432.

```
docker run -it --rm --name postgres-dev -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_USER=user -e POSTGRES_DB=book_catalog postgres
```

- Output of the docker command

```
...
PostgreSQL init process complete; ready for start up.

2022-06-26 16:49:07.200 UTC [1] LOG:  starting PostgreSQL 14.4 (Debian 14.4-1.pgdg110+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit
2022-06-26 16:49:07.201 UTC [1] LOG:  listening on IPv4 address "0.0.0.0", port 5432
2022-06-26 16:49:07.201 UTC [1] LOG:  listening on IPv6 address "::", port 5432
2022-06-26 16:49:07.227 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
2022-06-26 16:49:07.256 UTC [62] LOG:  database system was shut down at 2022-06-26 16:49:07 UTC
2022-06-26 16:49:07.274 UTC [1] LOG:  database system is ready to accept connections
```

- Start the SpringBoot service from IDE and using executable jar `java -jar <executable.jar>`, use below Curl command to POST a Book message. In this case I use git bash terminal.

```
$ curl -X POST http://localhost:8080/books -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{"title" : "from curl command"}'
```

- Output response of Curl command

```
{"id":1,"title":"from curl command"}
```

#### Output

- Test case output which starts the container and performs test

```
2022-06-26 10:57:02.539  INFO 11804 --- [           main] org.testcontainers.DockerClientFactory   : Connected to docker: 
  Server Version: 20.10.16
  API Version: 1.41
  Operating System: Docker Desktop
  Total Memory: 6175 MB
2022-06-26 10:57:03.310  INFO 11804 --- [           main] 🐳 [testcontainers/ryuk:0.3.3]           : Creating container for image: testcontainers/ryuk:0.3.3
2022-06-26 10:57:07.650  INFO 11804 --- [           main] 🐳 [testcontainers/ryuk:0.3.3]           : Container testcontainers/ryuk:0.3.3 is starting: 3b6fe78b89b13d76904e75de06aab6ef7ae1e91ee65b86051e43e9ea64d129fd
2022-06-26 10:57:12.637  INFO 11804 --- [           main] 🐳 [testcontainers/ryuk:0.3.3]           : Container testcontainers/ryuk:0.3.3 started in PT10.0372819S
2022-06-26 10:57:12.736  INFO 11804 --- [           main] o.t.utility.RyukResourceReaper           : Ryuk started - will monitor and terminate Testcontainers containers on JVM exit
2022-06-26 10:57:12.737  INFO 11804 --- [           main] org.testcontainers.DockerClientFactory   : Checking the system...
2022-06-26 10:57:12.739  INFO 11804 --- [           main] org.testcontainers.DockerClientFactory   : ✔︎ Docker server version should be at least 1.6.0
2022-06-26 10:57:12.742  INFO 11804 --- [           main] 🐳 [postgres:13]                         : Creating container for image: postgres:13
2022-06-26 10:57:13.386  INFO 11804 --- [           main] 🐳 [postgres:13]                         : Container postgres:13 is starting: 42a8e5d74a3a8f9d2b631b75153bf7b7414ab1561f7529a61a2cb0a2eae01af8
2022-06-26 10:57:53.269  INFO 11804 --- [           main] 🐳 [postgres:13]                         : Container postgres:13 started in PT40.5272774S
2022-06-26 10:57:54.255  INFO 11804 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2022-06-26 10:57:54.358  INFO 11804 --- [           main] o.f.c.i.database.base.BaseDatabaseType   : Database: jdbc:postgresql://localhost:50819/test (PostgreSQL 13.7)
2022-06-26 10:57:54.758  INFO 11804 --- [           main] o.f.core.internal.command.DbValidate     : Successfully validated 1 migration (execution time 00:00.151s)
2022-06-26 10:57:55.131  INFO 11804 --- [           main] o.f.c.i.s.JdbcTableSchemaHistory         : Creating Schema History table "public"."flyway_schema_history" ...
2022-06-26 10:57:55.374  INFO 11804 --- [           main] o.f.core.internal.command.DbMigrate      : Current version of schema "public": << Empty Schema >>
2022-06-26 10:57:55.424  INFO 11804 --- [           main] o.f.core.internal.command.DbMigrate      : Migrating schema "public" to version "1 - init schema"
2022-06-26 10:57:55.625  INFO 11804 --- [           main] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema "public", now at version v1 (execution time 00:00.307s)
2022-06-26 10:57:58.316  INFO 11804 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 50827 (http) with context path ''
2022-06-26 10:57:59.308  INFO 11804 --- [           main] c.l.example.ExampleApplicationTests      : Started ExampleApplicationTests in 105.885 seconds (JVM running for 116.971)
2022-06-26 10:58:07.836  INFO 11804 --- [o-auto-1-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2022-06-26 10:58:07.845  INFO 11804 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2022-06-26 10:58:12.499  INFO 11804 --- [o-auto-1-exec-1] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 14 endpoint(s) beneath base path '/actuator'
2022-06-26 10:58:14.691  INFO 11804 --- [o-auto-1-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 6845 ms
2022-06-26 10:58:17.327  INFO 11804 --- [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2022-06-26 10:58:19.097  INFO 11804 --- [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

Process finished with exit code 0
```

- Library service output, after using the Curl command mentioned above

![image](https://user-images.githubusercontent.com/6425536/175827507-9a8c82af-86c9-4f32-b85b-ebdf1eec5c74.png)
