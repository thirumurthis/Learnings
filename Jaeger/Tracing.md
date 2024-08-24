Observability - Create and trace spans between applications


This blog demostrate 
In this blog we have two Spring Boot apps where first app invokes the second app. 
The tracing will be tracked from the first app to the next app.

![image](https://github.com/user-attachments/assets/589948c3-6365-4cce-a33d-bdf6adf19937)


We use the micrometer dependency to create the traces and spans.

The invoker app, the pom.xml uses spring boot web, actuator, Lombok and micrometer dependencies.

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-parent</artifactId>
	    <version>3.3.2</version>
	    <relativePath/>
	</parent>
	<groupId>com.trace</groupId>
	<artifactId>invoker</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>invoker</name>
	<description>Demo project for Spring Boot</description>
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
		<java.version>22</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
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
			<artifactId>micrometer-core</artifactId>
		</dependency>
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing-bridge-brave</artifactId>
		</dependency>
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing</artifactId>
		</dependency>
		<dependency>
			<groupId>io.zipkin.brave</groupId>
			<artifactId>brave-instrumentation-okhttp3</artifactId>
		</dependency>
		<dependency>
			<groupId>io.zipkin.reporter2</groupId>
			<artifactId>zipkin-sender-urlconnection</artifactId>
		</dependency>
		<dependency>
			<groupId>io.zipkin.reporter2</groupId>
			<artifactId>zipkin-reporter-brave</artifactId>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.micrometer</groupId>
				<artifactId>micrometer-tracing-bom</artifactId>
				<version>${micrometer-tracing.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>io.zipkin.brave</groupId>
				<artifactId>brave-bom</artifactId>
				<version>${brave.version}</version>
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

</project>
```

Controller with the REST API

```java
package com.trace.invoker;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v2/")
@Slf4j
public class AppInvokerController {

    private final Tracer tracer;

    AppInvokerController(Tracer tracer){
        this.tracer = tracer;
    }


    @GetMapping("/execute")
    public String invokeTask() {

        //works
        //Span span = tracer.currentSpan().name("api/v2");

        // new span
        Span span1 = tracer.nextSpan().name("invoker-api-parent").start();
        String APPURL = "http://localhost:8082/app/v1/run?traceId=%s&spanId=%s";
        String traceId = tracer.currentSpan().context().traceId();
        String spanId = span1.context().spanId();
        String url = String.format(APPURL,traceId,spanId);

        RestTemplate clientTemplate = new RestTemplate();
        log.info("tracer invoked app-1 invoked");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> request = new HttpEntity<>("",httpHeaders);

        try (Tracer.SpanInScope spanInScope = tracer.withSpan(span1.name("invoke-api").start())) {
            span1.tag("appName","invokeApi");
            span1.event("invoked from invoker api");
            ResponseEntity<String> response =
                    clientTemplate.postForEntity(url,null,String.class);
            log.info("response {}",response.getBody());

        }finally {
            span1.end();
        }

        Span span2 = tracer.nextSpan().name("last step").start();
        span2.event("completed");
        span2.tag("app","invoker");
        try{
            Thread.sleep(700);
        } catch (InterruptedException e) {
            log.warn("Interrupted exception");
        }
        span2.end();
        return "completed";
    }

}
```

App config

```java
package com.trace.invoker;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class AppConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

}
```

- The app-1 spring boot 

- The same pom.xml dependencies are same as invoke-app Spring boot app above.


- controller app 
```java
package com.trace.app.one;


import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/app/v1")
@Slf4j
public class AppController {

    private final Tracer tracer;

    Random random = new Random();
    AppController(Tracer tracer){
        this.tracer = tracer;
    }


    @PostMapping("/run")
    public String process(@RequestParam String traceId,
                          @RequestParam String spanId){

        if(!Objects.isNull(traceId) ) {
            log.info("traceId included - {}", traceId);
            //log.info("traceId from the passed span {}", span.context().traceId());
            //Span span = tracer.currentSpan().name("app-1-tracing");
            var contextWithCustomTraceId = tracer.traceContextBuilder()
                    .traceId(traceId)
                    .spanId(spanId)
                    .sampled(true)
                    .build();

            try (var sc = tracer.currentTraceContext().newScope(contextWithCustomTraceId)) {
               var span =  tracer.spanBuilder().name("app-one-tracing").start();
                try(Tracer.SpanInScope spanInScope= tracer.withSpan(span)) {
                    span.tag("app", "run invoked");
                    log.info("tracing logs...");
                    sleep(random.nextInt(750));
                    sampleThreadInvocation();
                    additionalProcess("process-1");
                    additionalProcess("process-2");
                }finally {
                    span.end();
                }
            }
            //Span newSpan = tracer.nextSpan().name("app-1-span");
            //try (Tracer.SpanInScope ws = tracer.withSpan(newSpan.start())) {
            /*
            try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
                log.info("spanId - {}",span.context().spanId());
                log.info("traceId - {}",span.context().traceId());
                // ...
                // You can tag a span - put a key value pair on it for better debugging
                //newSpan.tag("app", "run invoked");
                span.tag("app","run invoked from invoker");

            } finally {
                // Once done remember to end the span. This will allow collecting
                // the span to send it to a distributed tracing system e.g. Zipkin
                //newSpan.end();
                span.end();
            }
             */
        }
        return "completed invocation";
    }

    void additionalProcess(String name){
        Span span = tracer.nextSpan(tracer.currentSpan());

        try(Tracer.SpanInScope spanInScope= tracer.withSpan(span.name(name).start())){
            SpanHelper spanHelper = new SpanHelper();
            Span innerSpan = spanHelper.createSpan(tracer,span,"ap-"+name,"proceed");
            sleep(random.nextInt(1100));
            spanHelper.endSpan(innerSpan);
            spanHelper.endSpan(span);
        }

    }
    void sampleThreadInvocation(){

        log.info("api invoked..");
        try(ExecutorService executor = Executors.newFixedThreadPool(5)) {

            Span span = tracer.currentSpan();//.name("api-parent").start();//currentSpan();

            for (int i = 0; i < 10; i++) {
                try (Tracer.SpanInScope spanInScope = tracer.withSpan(span)) {
                    Callable<Void> caller = new Task(tracer, span, "task" + i, random.nextInt(750), true);
                    executor.submit(caller);
                }
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException interruptedException) {
                executor.shutdownNow();
            }
        }
        log.info("completed");
    }

    void sleep(int duration){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            log.warn("sleep interrupted");
        }
    }
}

```
- Task class

```java
package com.trace.app.one;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class Task implements Callable<Void> {

    private final Tracer tracer;
    SpanHelper spanHelper = new SpanHelper();
    private final Span span;
    private final String name;
    private final int timeOut;
    private final boolean useSpan;

    public Task(Tracer tracer, Span span, String name, int timeOut, boolean useSpan){
        this.tracer =tracer;
        this.span = span;
        this.name = name;
        this.timeOut = timeOut;
        this.useSpan = useSpan;
    }

    @Override
    public Void call() throws Exception{
        Span childSpan;

        if(useSpan){
            childSpan = spanHelper.createSpan(tracer,span,Thread.currentThread().getName(),name);
        }else{
            childSpan = spanHelper.createSpan(tracer,Thread.currentThread().getName(), name);
        }
        log.info("Running : {}",Thread.currentThread().getName());
        Thread.sleep(timeOut);
        spanHelper.endSpan(childSpan);

        return null;
    }

}
```

- Span helper

```java
package com.trace.app.one;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpanHelper {

    Span createSpan(Tracer tracer, String spanName, String tags){

        log.info("create new span..");

        Span span = tracer.nextSpan(tracer.currentSpan());
        span.name(spanName+"-"+tags);
        span.event("starting "+spanName);
        span.start();
        return span;
    }

    Span createSpan(Tracer tracer, Span parentSpan, String spanName, String tags){
        log.info("create new child span..");

        Span span = tracer.nextSpan(parentSpan);
        span.name(spanName+"-"+tags);
        span.event("starting "+spanName);
        span.start();
        return span;
    }

    void endSpan(Span span){
        if(span != null){
            log.info("closing the span {}",span);
            span.end();
        }
    }
}
```
- application app

```java
package com.trace.app.one;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppOneApplication.class, args);
	}
}
```
