#### Custom traces and span in spring boot application

- The Jaeger to run in local docker

  ```sh
  docker run --rm --name jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 5778:5778 \
  -p 9411:9411 \
  jaegertracing/jaeger:2.4.0
  ```

or 

```sh
docker run --rm --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.67.0
```

- The pom.xml for the spring boot simple application

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.4.3</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.tracing</groupId>
	<artifactId>tracing</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>tracing</name>
	<description>Tracing playground</description>
	<properties>
		<java.version>23</java.version>
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
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing-bridge-brave</artifactId>
		</dependency>
		<dependency>
			<groupId>io.zipkin.reporter2</groupId>
			<artifactId>zipkin-reporter-brave</artifactId>
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

- Starting point for application

```java
package com.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TracingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracingApplication.class, args);
	}	
}
```

- Controller java

```java
package com.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api")
public class TracingController {

    private static final Logger logger = LoggerFactory.getLogger(TracingController.class);

    private final Tracer tracer;

    //private final RestTemplate restTemplate;

    TracingController(Tracer tracer
            //, RestTemplateBuilder restTemplateBuilder
    ){
        this.tracer = tracer;
        //this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping("/execute")
    public String invokeTask() {

        Random random = new Random();
        // new span
        Span span1 = tracer.nextSpan().name("invoker-api-parent");//.start();

        //tracer.traceContextBuilder().traceId(UUID.randomUUID().toString());
        // URL for second app app-1 application;
        logger.info("tracer invoked app-1 invoked");

        try (Tracer.SpanInScope spanInScope = tracer.withSpan(span1.start())) {
        //try (Tracer.SpanInScope ws = (Tracer.SpanInScope) this.tracer.startScopedSpan("operation")){
            span1.tag("appName","invokeApi");
            span1.tag("value",random.nextInt(2000));
            span1.event("invoked from invoker api");
                Span span0 = tracer.nextSpan(span1);
                span0.start();
                span0.event("baggage span");
                span0.tag("baggage0","test");
                try{

                    Thread.sleep(random.nextInt(2500));
                } catch (InterruptedException e) {
                     logger.error("Interrupted..");
                }
                span0.end();
         //}
        }finally {
            span1.end();
        }

        // New span just for example to demonstrate the last step in invoker-app
        Span span2 = tracer.nextSpan().name("last step").start();
        span2.event("completed");
        span2.tag("app","invoker");
        try{
            Thread.sleep(random.nextInt(1500));
        } catch (InterruptedException e) {
           logger.error("Interrupted exception..");
            //System.out.println("interrupted exception..");
        }
        span2.end();
        return """
            {"status":"completed"}
            """;
    }
}
```

- output

```sh
curl -iv http://localhost:8080/api/execute
```

- jaeger output

```sh
http://localhost:16686
```

