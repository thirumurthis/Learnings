### Install Jaeger Tracing in KIND cluster

- In this blog will explain how to install the Jaeger tracing using Jaeger operator in Docker Desktop KIND cluster.

- We will use the Spring boot service to send traces using OpenTracing (note OpenTracing is now deprecated). Latest version of uses OpenTelementry.


#### KIND configuration
- Kind cluster configuration with the multiple ports export
- In below cluster configuration exposes most of the port, this can be updated to expose only minimal ports mostly 16686, 14268
- Save the file as `jaeger_kind_cluster.yaml`

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
networking:
  ipFamily: ipv4
  apiServerAddress: 127.0.0.1
  apiServerPort: 6443
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 6831
    hostPort: 6831
    listenAddress: "127.0.0.1"
    protocol: UDP
  - containerPort: 6832
    hostPort: 6832
    listenAddress: "127.0.0.1"
    protocol: UDP
  - containerPort: 16686
    hostPort: 16686
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 16685
    hostPort: 16685
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 9411
    hostPort: 9411
    listenAddress: "127.0.0.1"
  - containerPort: 4317
    hostPort: 4317
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 4318
    hostPort: 4318
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 5778
    hostPort: 5778
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 14250
    hostPort: 14250
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 14268
    hostPort: 14268
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 14269
    hostPort: 14269
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
```

### Below shell script used to automate the process

- Store the `cert-manager.yaml` from [GitHub](https://github.com/cert-manager/cert-manager/releases/download/v1.6.3/cert-manager.yaml)

- Store the the `jaeger-opertor.yaml` from (GitHub](https://github.com/jaegertracing/jaeger-operator/releases/download/v1.41.0/jaeger-operator.yaml)

- Jaeger instance configuration to be installed in KIND cluster

```yaml
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: simplest
```

- Shell script to install the jeager operator

```sh
#! /bin/sh

# Create kind cluster in Docker desktop
kind create cluster --name jaeger-cluster --config jaeger_kind_cluster.yaml

# install the cert manager 
kubectl apply -f cert-manager.yaml

# create namespace trace
kubectl create ns trace

# sleep for 10 sec
sleep 10

# install the jaeger operator
kubectl -n trace apply -f jaeger-operator.yaml

# pause for 100 seconds
sleep 100

# install the simple cluster
kubectl -n trace apply -f simple-jaeger.yaml
```

- Though the port are exposed in the Docker process, we need to expose the ports from the cluster.

```
# get the pods name of the jaeger
kubectl -n trace get pods 

# output 
NAME                               READY   STATUS    RESTARTS       AGE
jaeger-operator-6787f4df85-ww7mx   2/2     Running   7 (147m ago)   10d
simplest-6b8dbb67c5-kpz5j          1/1     Running   0              146m
```

```
kubectl -n trace get svc

# output sample
NAME                            TYPE        CLUSTER-IP   PORT(S)                                                 
jaeger-operator-metrics         ClusterIP   10.96.230.32 8443/TCP                                                
jaeger-operator-webhook-service ClusterIP   10.96.137.93 443/TCP                                                 
simplest-agent                  ClusterIP   None         5775/UDP,5778/TCP,6831/UDP,6832/UDP                     
simplest-collector              ClusterIP   10.96.135.45 9411/TCP,14250/TCP,14267/TCP,14268/TCP,4317/TCP,4318/TCP
simplest-collector-headless     ClusterIP   None         9411/TCP,14250/TCP,14267/TCP,14268/TCP,4317/TCP,4318/TCP
simplest-query                  ClusterIP   10.96.212.68 16686/TCP,16685/TCP                                     
```

- Command to port forward the jeager pod and service

```
# port forward the jaeger pod (the pod name can be fetched from above command)
kubectl -n trace port-forward pod/simplest-6b8dbb67c5-kpz5j 16686:16686

# port forward the jaeger service 
kubectl -n trace port-forward svc/simplest-collector 14268:14268
``` 

- Simple SpringBoot application with the version 2.7.X, expose an API. We will use the same application in different port.

> **Note:**
> - The spring boot 3.0.0, with OpenTracing is not supported.

- pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.7</version>
		<relativePath/>
	</parent>
	<groupId>com.example</groupId>
	<artifactId>app</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>app</name>
	<description>application</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>io.opentracing.contrib</groupId>
			<artifactId>opentracing-spring-jaeger-cloud-starter</artifactId>
			<version>3.3.1</version>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
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

- java application 

```java
package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class JaegerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);
	}

    // Used by the Jeager dependencies to intercept http header
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder){
		return builder.build();
	}
}
```

- Controller

```java
package com.example.kafka;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@RestController
@RequestMapping("/api")
@Slf4j
public class AppController {

   private RestTemplate restTemplate;

   public AppController(RestTemplate restTemplate){
       this.restTemplate = restTemplate;
   }
    Random rand = new Random();
    @GetMapping("/generate")
    public long calculate(){
        long number = rand.nextInt(1000);
        log.info("api /generate invoked {}",number);
        return number;
    }

    @Value("${app2.port:8090}")
    private String appPort;

    @Value("${app2.host:localhost}")
    private String hostName;

    @GetMapping("/fetch")
    public ResponseEntity getNum(){
        String url = "http://"+hostName+":"+appPort+"/api/generate";
        String resp = restTemplate.getForObject(url, String.class);
        log.info("api/fetch invoked - {} = {}",url,resp);
        return ResponseEntity.ok("random num : "+resp);
    }
}
```

- application.yml file, application name will be passed during java command

```
  opentracing:
    jaeger:
      http-sender:
        url: http://localhost:14268/api/traces
```

- With the above Spring boot application, using `mvn clean install` to create the jar file. 
- Use below commands to run the application in two different ports
 
```
java -jar .\target\app-0.0.1-SNAPSHOT.jar --spring.application.name=service-1 --server.port=8080 --debug

java -jar .\target\app-0.0.1-SNAPSHOT.jar --spring.application.name=service-2 --server.port=8090 --debug
```

- Now we can use curl command to access the API

```
curl -i http://localhost:8080/api/fetch

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 16
Date: Wed, 11 Jan 2023 02:09:58 GMT

random num : 771
```

- With the above access from [Jaeger UI](http://localhost:16686), we should be able to see spring boot application name as services in the UI, like below.
- The service name `jaeger-query` is the default, once accessing the REST endpoint API the traces will be shipped to the Jaeger UI under services, with span id.

Service name details:

![image](https://user-images.githubusercontent.com/6425536/211721720-4cc25cc7-4817-42b4-9ebc-26d1e2cffe17.png)


![image](https://user-images.githubusercontent.com/6425536/211721864-0fd6b612-f36e-4e17-9f91-a9063e1caf0e.png)
