# Spring boot 3.4.3 with tracing config


-pom.xml
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
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
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
			<artifactId>micrometer-tracing-bridge-otel</artifactId>
		</dependency>
		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-exporter-zipkin</artifactId>
		</dependency>
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing</artifactId>
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

- The controller class

```java
package com.tracing;



import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/")
public class TracingController {

    private final Tracer tracer;

    TracingController(Tracer tracer){
        this.tracer = tracer;
    }

    @GetMapping("/execute")
    public String invokeTask() {

        // new span
        Span span1 = tracer.nextSpan().name("invoker-api-parent").start();
        // URL for second app app-1 application;
        System.out.println("tracer invoked app-1 invoked");


        try (Tracer.SpanInScope spanInScope = tracer.withSpan(span1.name("invoke-api").start())) {
            span1.tag("appName","invokeApi");
            span1.event("invoked from invoker api");

        }finally {
            span1.end();
        }
        // New span just for example to demonstrate the last step in invoker-app
        Span span2 = tracer.nextSpan().name("last step").start();
        span2.event("completed");
        span2.tag("app","invoker");
        try{
            Thread.sleep(700);
        } catch (InterruptedException e) {
            System.err.println("Interrupted exception..");
        }
        span2.end();
        return "completed";
    }
}
```

- command to start the Jaeger process

```
docker run --rm --name jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 5778:5778 \
  -p 9411:9411 \
  jaegertracing/jaeger:2.3.0
```
