This blog will explain how to deploy the Jaeger all-in-one image in KIND cluster using the Jaeger operator.

**Pre-requesites**

* Basic understanding of Jaeger architecture
    
* Docker Desktop installed and running
    
* KIND CLI configured
    

#### About this article

* First, we will see instructions on how to deploy Jaeger instance in the Docker Desktop KIND cluster
    
* Next, we will run the SpringBoot application locally to send traces with OpenTracing.
    

#### Installing Jaeger

* Install KIND cluster with necessary ports exposed
    
* Install cert-manager, using cert-manager yaml
    
    * Based on the Jaeger Installation document it requires cert-manager to be installed in the Kubernetes cluster before installing the Jaeger operator.
        
* Create Namespace and Install Jaeger operator yaml
    
* Create Jaeger instance using CRD manifest
    
    * In this case, have used `all-in-one` mode Jaeger instance
        

> NOTE:
> 
> There are different modes the Jaeger can be deployed, the default mode is `all-in-one` mode where the trace data will not be persisted.
> 
> `all-in-one` mode is mostly used for local development and data is held in memory.
> 
> For production, it is better to use `production` mode. Refer Jaeger documentation for more detals.

#### Detail installation instruction

#### KIND configuration

* KIND cluster configuration below exposes multiple ports export, it is not necessary to expose all the ports, latter only ports 16686 and 14268 are forwarded.
    
* Save the YAML content as `jaeger_kind_cluster.yaml` file.
    

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

#### Required cert-manager and jaeger-operator yaml file reference

* Save the content from the [Cert-Manager/GitHub](https://github.com/cert-manager/cert-manager/releases/download/v1.6.3/cert-manager.yaml) file to `cert-manager.yaml`
    
* Save the content from the [Jaegertracing/GitHub](https://github.com/jaegertracing/jaeger-operator/releases/download/v1.41.0/jaeger-operator.yaml) file to `jaeger-opertor.yaml`
    
* Below is the Jaeger CRD manifest YAML, which deploys Jaeger instance in KIND cluster
    

```yaml
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: simplest
```

#### How to install the KIND cluster and Jaeger

* Once the yaml files are stored under a directory, we can use the below script from GitBash to install the Jaeger Instance in Docker Desktop
    
* Shell script that installs the Jaeger operator
    

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

* Once the shell script is executed, we can use below command to view the deployed Jaeger instance status and strategy.
    

```plaintext

$ kubectl -n trace get jaegers
NAME       STATUS    VERSION   STRATEGY   STORAGE   AGE
simplest   Running   1.40.0    allinone   memory    10d
```

* Below is the output of deployed Jaeger resources in the cluster
    

```plaintext
# get the pods name of the jaeger
$ kubectl -n trace get pods

# output 
NAME                               READY   STATUS    RESTARTS       AGE
jaeger-operator-6787f4df85-ww7mx   2/2     Running   7 (147m ago)   10d
simplest-6b8dbb67c5-kpz5j          1/1     Running   0              146m
```

```plaintext
# ge the service status
$ kubectl -n trace get svc

# output sample
NAME                            TYPE        CLUSTER-IP   PORT(S)                                                 
jaeger-operator-metrics         ClusterIP   10.96.230.32 8443/TCP                                                
jaeger-operator-webhook-service ClusterIP   10.96.137.93 443/TCP                                                 
simplest-agent                  ClusterIP   None         5775/UDP,5778/TCP,6831/UDP,6832/UDP                     
simplest-collector              ClusterIP   10.96.135.45 9411/TCP,14250/TCP,14267/TCP,14268/TCP,4317/TCP,4318/TCP
simplest-collector-headless     ClusterIP   None         9411/TCP,14250/TCP,14267/TCP,14268/TCP,4317/TCP,4318/TCP
simplest-query                  ClusterIP   10.96.212.68 16686/TCP,16685/TCP
```

#### Port forward the UI and the Collector service port

* In the spring application, we will send the traces to the 14268 port, which is the Jaeger collector service running in the cluster.
    
* Based on the Jaeger architecture, the collector service will collect the traces.
    

```plaintext
# port forward the jaeger pod (the pod name can be fetched from above command)
kubectl -n trace port-forward pod/simplest-6b8dbb67c5-kpz5j 16686:16686

# port forward the jaeger service 
kubectl -n trace port-forward svc/simplest-collector 14268:14268
```

> **NOTE**:
> 
> * SpringBoot application is developed using 2.7.7 version with OpenTracing dependencies.
>     
> * SpringBoot 3.0.0 doesn't support OpenTracing, it is deprecated in favour of OpenTelementry.
>     

#### SpringBoot Application to send traces

* Below is the pom.xml file with `OpenTracing` dependencies
    

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

* Entry point of springboot application
    

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

* Simple Spring controller, that expose two endpoint `/api/generate` and `api/fetch`
    

> **INFO:**
> 
> The same SpringBoot application will be running in different ports (8080 and 8090) on the host machine.
> 
> `http://localhost:8080/api/fetch` the endpoint will invoke the application running on `http:/localhost:8090/api/generate` and the traces will be sent to the Jaeger instance.

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

* Configuration `application.yml` file defining the `OpenTracing` Jaeger url configuraton
    
* The spring application name will be passed during runtime with `Java` command
    

```plaintext
  opentracing:
    jaeger:
      http-sender:
        url: http://localhost:14268/api/traces
```

#### Execute the spring boot application services

* Use `mvn clean install` command to create the jar file.
    
* Once jar file is generated, execute two instance of application with below commands
    

```plaintext
java -jar .\target\app-0.0.1-SNAPSHOT.jar --spring.application.name=service-1 --server.port=8080 --debug

java -jar .\target\app-0.0.1-SNAPSHOT.jar --spring.application.name=service-2 --server.port=8090 --debug
```

* After application is up and running, use `curl` command to access the REST end-point like below
    

```plaintext
curl -i http://localhost:8080/api/fetch

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 16
Date: Wed, 11 Jan 2023 02:09:58 GMT

random num : 771
```

* Once the `curl` command access the endpoing the traces are sent to Jaeger and we can view the spans from [Jaeger UI](http://localhost:16686) at `http://localhost:16686`
    
    > **INFO**
    > 
    > * In Jaeger UI the service name `jaeger-query` is the default.
    >     
    > * After accessing the REST endpoint and traces are collected the Jaeger UI spans can be viewed under service. This service is the SpringBoot application name. In this case, `service-1` and `service-2`.
    >     
    

#### Snapshot of the Jaeger UI after shipping the traces

![image](https://user-images.githubusercontent.com/6425536/211721720-4cc25cc7-4817-42b4-9ebc-26d1e2cffe17.png)

![image](https://user-images.githubusercontent.com/6425536/211721864-0fd6b612-f36e-4e17-9f91-a9063e1caf0e.png)
