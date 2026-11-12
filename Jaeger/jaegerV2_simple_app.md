- The kind cluster with config

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30317
    hostPort: 8317
  - containerPort: 30318
    hostPort: 8318
  - containerPort: 30990
    hostPort: 9090
  - containerPort: 30300
    hostPort: 3000
  - containerPort: 31686
    hostPort: 8088
- role: worker
- role: worker
- role: worker
```

-- to install the kind cluster
```
kind create cluster --config kind-config.yaml --name dev0
```

-- to install the jaeger default in-memory
```
helm upgrade -i -n jaeger jaeger jaegertracing/jaeger --create-namespace
```
- Simple service override after installting

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jager-nodeport-svc
spec:
  type: NodePort
  selector:
    app.kubernetes.io/component: all-in-one
    app.kubernetes.io/instance: jaeger
    app.kubernetes.io/name: jaeger
  ports:
    - protocol: TCP
      name: jaeger-ui
      port: 16686     
      targetPort: 16686  
      nodePort: 30300
    - protocol: TCP
      name: collector-1
      port: 4317
      targetPort: 4317
      nodePort: 30317
    - protocol: TCP
      name: collector-2
      port: 4318
      targetPort: 4318
      nodePort: 30318

```

kubectl -n jaeger apply -f <above-file-name>.yaml

Spring app

app/SimpleApp.java

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25

//DEPS org.springframework.boot:spring-boot-dependencies:4.1.0@pom
//DEPS org.springframework.boot:spring-boot-starter-web
//DEPS org.springframework.boot:spring-boot-starter-opentelemetry
//FILES application.yaml

package com.example.demo;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SimpleApp {

    private final Tracer tracer;

    public SimpleApp(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("com.example.demo", "1.0.0");
    }

    public static void main(String[] args) {
        SpringApplication.run(SimpleApp.class, args);
    }

    @GetMapping("/order")
    public String createOrder(@RequestParam(defaultValue = "ORD-101") String id) throws InterruptedException {
        Span.current().setAttribute("order.id", id);

        Span dbSpan = tracer.spanBuilder("save-order-to-db")
            .setAttribute("db.system", "postgresql")
            .setAttribute("db.operation", "INSERT")
            .startSpan();

        try (Scope scope = dbSpan.makeCurrent()) {
            dbSpan.addEvent("Executing insert query");
            Thread.sleep(75);
            dbSpan.setStatus(StatusCode.OK);
        } catch (Exception e) {
            dbSpan.recordException(e);
            dbSpan.setStatus(StatusCode.ERROR, "DB write failed");
            throw e;
        } finally {
            dbSpan.end();
        }

        return "Processed order " + id;
    }
}

```

app/application.yaml

```yaml
spring:
  application:
    name: jbang-springboot-service

management:
  tracing:
    sampling:
      probability: 1.0 # Required in Spring Boot 4 (defaults to 0.1 / 10%)
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: http://localhost:8318/v1/traces
          #endpoint: http://localhost:4318/v1/traces

  otlp:
    metrics:
      export:
        enabled: false # Disable metrics export if using Jaeger alone
```

http://localhost:3000/ for viewing the jaeger

