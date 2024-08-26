Observability - Create and trace spans between applications

Pre-requisites:
 - Jaeger installed and running (in this example deployed in kind cluster)
 - Understanding on traces and spans

This blog details on how to create spans manually in Spring Boot application. This is based on a requirement in a project, to track the flow between application using Jaeger. To create the traces and spans manually so it can visualized in Jaeger UI and helps the business process on how much time it takes.
 
In order to demonstrate we have two Spring Boot application, use the tracer object configured in Spring boot to create the spans. The spans set with name with additional tags which is key value pair which can be viewed in Jaeger UI. In the example, the application named `invoker-app` exposes a REST API (`/api/v2/execute`), when invoked calls the REST API (`/app/run?traceId=xxx&spanId=yyy`) of second application named `app-1`. The `app-1` application uses the traceId and spanId to create a trace context and adds to the current tracer.

The code snippet below is in app-1 application which creates traceContext with the traceId and spanId. The created traceContext is set to the configured tracer objects current Tracer Context scope. 

```java
var contextWithCustomTraceId = tracer.traceContextBuilder()
                    .traceId(traceId)
                    .spanId(spanId)
                    .sampled(true)
                    .build();
// use traceContext as a newScope
try (var sc = tracer.currentTraceContext().newScope(contextWithCustomTraceId)) {
 // create spans process any operation
}
```

Simple representation of the REST API invocation between invoker-app and app-1 with flow.

![image](https://github.com/user-attachments/assets/589948c3-6365-4cce-a33d-bdf6adf19937)

The expected flow from Jager UI looks like below, where the invoker-app span and app-1 span can be seen as child spans.

![image](https://github.com/user-attachments/assets/551914ef-659a-4464-bea2-c0604727ff20)


![image](https://github.com/user-attachments/assets/8e7ae5a0-9361-48e1-baaa-c82bb11d67d9)

### Code
#### Invoker app

Below is the pom.xml with dependencies. Note the dependencies are same for both invoker-app and app-1 only change is the name of the apps.
When using [Spring starter](https://start.spring.io) create two projects, just copy paste the dependencies section. 

```xml
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

- The application.yaml for the spring boot application, which includes the tracing endpoint url

```yaml
spring.application.name: invoker

server.port: 8080

management:
  server.port: 9145
  tracing:
    sampling:
      probability: 1.0

  zipkin:
    tracing:
      endpoint: 'http://localhost:9411/api/v2/spans'
```

##### Invoker app controller
 - The invoker app uses the RestTemplate to create the REST API GET request to access the app-1 app.
 - The traceId and spandId is obtained from the tracer object configured and created by the Spring boot application during deployment.
 - The use of Random class to induce additional random delay after the span is created just for demonstration to view it in Jaeger UI.
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

        // new span
        Span span1 = tracer.nextSpan().name("invoker-api-parent").start();
        // URL for second app app-1 application
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
        // New span- just for example to demonstrate the last step in invoker-app
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

- RestTemplate bean is used by the tracer object to publish the trace and span to Jaeger server.

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

- Application entry point usually class created by the spring starter itself.

```java
package com.trace.invoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InvokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvokerApplication.class, args);
	}
}
```

##### app-1 application

- The pom.xml with dependencies is same as the invoker-app dependencies. The only change would be the name and artifactId.

- The app-1 controller code,
  - With the provided traceId and spandId a new tracerContext is build and added to the current tracer context scope.
  - The `sampleThreadInvocation()` uses Task Callable class (note, we can use Runnable as well) to run set of thread in parallel and create child spans with provided parent span
  - The `additionalProcess()` method demonstrates creating the spans outside the thread.
 
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
        // traceId and spanId can't be null
        if(!Objects.isNull(traceId) && !Objects.isNull(spanId) ) {
            log.info("traceId included - {}", traceId);
           // create trace context with teh traceId and spanId
            var contextWithCustomTraceId = tracer.traceContextBuilder()
                    .traceId(traceId)
                    .spanId(spanId)
                    .sampled(true)
                    .build();
            // set the tracercontext to current trace context scope
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
- Below is Task class just used to demonstrate the use of parallel threads and span creation.

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

- SpanHelper class is a helper class used for creating the spans either with provides parent span or new span.

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
- The entry point of the app-1 application, usually generated by the spring boot starter

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

- application.yaml configuration for the app-1 application

```yaml
spring.application.name: app-1

server.port: 8082
management:
  zipkin:
    tracing:
      endpoint: 'http://localhost:9411/api/v2/spans'
```

#### Jaeger in Kind cluster
- Use the Kind cluster yaml configuration to create the cluster
- Deploy Jaeger to the cluster
- Port forward the necessary ports so spring boot application can send traces and spans.

- Save the below kind configuration yaml as `jaeger-cluster.yaml`.
- Use the command `kind create cluster --config jaeger-cluster.yaml` to create the cluster, make sure the docker desktop is running.

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: jaeger-cluster
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 31443
    hostPort: 7443
    protocol: TCP
```

- Use below set of command to deploy the Jaeger in Kind cluster, we use jaeger-all-in-one image which is not production ready.

- Download the cert-manager from `https://github.com/cert-manager/cert-manager/releases/download/v1.15.3/cert-manager.yaml` to `cert-manager-v1.15.3.yaml`. Use kubectl apply command below to deploy to kind cluster

```
kubectl apply -f cert-manager_v1.15.3.yaml
```

- Create the namespace with below command
```
kubectl create namespace observability
```

- Download the Jaeger operator yaml from `https://github.com/jaegertracing/jaeger-operator/releases/download/v1.60.0/jaeger-operator.yaml` and save it as `jaeger-operator.yaml`. Use below command to deploy to kind cluster

```
kubectl apply -f jaeger-operator.yaml
```

- With the below jaeger-all-in-one configuration create a yaml file named `jaeger_allinone.yaml`

```yaml
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: my-jaeger
spec:
  strategy: allInOne # <1>
  allInOne:
    image: jaegertracing/all-in-one:latest # <2>
    options: # <3>
      log-level: debug # <4>
  storage:
    type: memory # <5>
    options: # <6>
      memory: # <7>
        max-traces: 100000
  ingress:
    enabled: false # <8>
  agent:
    strategy: DaemonSet # <9>
  annotations:
    scheduler.alpha.kubernetes.io/critical-pod: "" # <10>

```

- Use below command to deploy the Jaeger all in one server.
```
kubectl apply -f jaeger_allinone.yaml
```

##### Port forward using below commands

```
# below ports will be used by the spring boot application to send traces and spans
kubectl port-forward svc/my-jaeger-collector 9411:9411 14250:14250 14267:14267 14269:14269 4317:4317 4318:4318

# below is to access the jaeger UI from local
kubectl port-forward svc/my-jaeger-query 16686:16686
```

#### Accessing the application
- With the spring boot applications running and Jaeger deployed, use `http://localhost:8080/api/v2/execute` to create traces.
- The Jaeger UI can be viewed using `http://localhost:16686/`


