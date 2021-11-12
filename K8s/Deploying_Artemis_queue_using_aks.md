- instruction 

- Building Apache Artemis Image
    - Clone source code from apache-artemis [github](https://github.com/apache/activemq-artemis/tree/main/artemis-docker) project.
    - Build distribution locally using maven for version 2.17.0.
    - Build Docker image using locally generated artifact.
- Push Image to ACR
    - Tag the Docker images and push to Azure Container Registry (ACR)
- Running image in Kubernetes Cluster
    - Create a AKS cluster and namespace
    - Use deployment manifest file to run the image
    - Expose service of type load-balancer for deployment
- Build client to connect to Broker
    - Spring-boot client (producer/consumer) to connect to MQ Broker

- Development requires below software to be installed
    - Docker - 20.10
    - Kubectl - 1.20.4 (client)

### Instruction – To Build Docker Image

```
# Clone the project to specific directory
$ git clone https://github.com/apache/activemq-artemis

# fetch all tags 
$ git fetch --all --tags 

# checkout the tag to local branch
$ git checkout tags/2.17.0 –b localversion-2.17.0

# navigate and build 2.17.0 distrubiton
$ cd artemis-distribution
$ mvn  -Dskip.test=true install

// check the target directory for created file 
$ ls –lrht artemis-distribution/target/apache-artemis-2.17.0-bin/apache-artemis-2.17.0

// build the docker related files
$ ./prepare-docker.sh --from-local-dist --local-dist-path ../artemis-distribution/target/apache-artemis-2.17.0-bin/apache-artemis-2.17.0

// check docker/Dockerfile-centos file in ./activemq-artemis/artemis-distribution/target/apache-artemis-2.17.0-bin/apache-artemis-2.17.0 and use below
$ docker build -f ./docker/Dockerfile-centos -t artemis-centos . 

// Run container locally to see if the image is starting up correctly
$ docker run --rm –it –p 61616:61616 –p 8161:8161 artemis-centos 
```

### Instruction – To push image to ACR

```
# Login to Azure 
$ az login
$ az account set -s "<aks-subscription-name>“
$ az acr login --name <acr-name>

# verify the image locally
$ docker pull <acr-name>.azurecr.io/<project>/artemis/v1:latest
$ docker run --rm -it -p 61616:61616 -p 8161:8161 <acr-name>.azurecr.io/<project>/artemis/v1
```

###Instruction - To Create namespace and deploy manifest file

```
# Command to create  namespace
$ kubectl create namespace artemis-demo

# Add label to the namespace created
$ kubectl label namespace artemis-demo name=artemis-demo

# command used to change the default namespace is artemis-demo
$ kubectl config --current-context --namespace=artemis-demo

# navigate to the file location of artemis-deploy.yaml, issue below command
# the --namespace is not required if the first command was executed.
$ kubectl apply –f artemis-deploy.yaml --namepsace=artemis-demo
 
# the deployment and service status can be viewed with below command
$ kubectl get deployment,service -o wide

# to get the pod status and pod name
$ kubectl get pod

# the logs on the pods
$ kubectl logs pod/<pod-name-from-above-command>
```

### Instruction – Fetch service endpoint to access the Artemis console

```
# After deployment – use below command
$ kubectl get endpoint

# To display the service status, check if the external IP is created
$ kubect get service/artemis-server-lb

# Describe the service and fetch the LoadBalancer Ingress IP
$ kubectl describe service/artemis-server-lb | grep Load
```

```yaml
apiVersion: apps/v1 
kind: Deployment
metadata:
  name: artemis-broker-deployment
  labels:
     app: artemis-broker
     type: artemis
     environment: dev-poc
spec:
  template:
     metadata:
        name: artemis-broker
        labels:
           app: artemis-broker
           type: artemis
     spec:
        containers:
        - name: artemis-server
          image: <azureacrname>.azurecr.io/<project>/artemis/v1   #will use the latest version.
  replicas: 1
  selector:    # This specifies which pods to be replicated 
      matchLabels:
         type: artemis
---
apiVersion: v1
kind: Service
metadata:
  name: artemis-server-lb
  labels:
    app: artemis-server-lb
  annotations:
    service.beta.kubernetes.io/azure-load-balancer-internal: "true"
spec:
  type: LoadBalancer
  ports:
    - name: tcp-acceptor
      port: 61616
      targetPort: 61616
      nodePort: 30616
      protocol: TCP
    - name: amqp-acceptor
      port: 5672
      targetPort: 5672
      nodePort: 30567
      protocol: TCP
    - name: web-console
      port: 8161
      targetPort: 8161
      nodePort: 30816
      protocol: TCP
  selector:
    app: artemis-broker
    type: artemis
```

#### Spring code
 - application.properties
 
```properties
brokerUrl = amqp://<external-ip-fromservice>:5672
artemisUser = artemis
artemisPassword = artemis

connectionCount=3

server.port=8085

spring.h2.console.enabled=true
spring.datasource.platform=h2
spring.datasource.url=jdbc:h2:mem:tim
```
- Rest controller, to send message
- 
```java
package bootcamelamqp.Producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class ArtemisProducer {
    
    @Autowired
    JmsTemplate jmsTemplate;

    @PostMapping(value = "/artemis/sendMessage")
    public void sendMessage(
        @RequestParam(name = "message", required = true) final String[] message){
            jmsTemplate.convertAndSend("test.queue", message);
    }   
}
```
- configuration class
```java
package bootcamelamqp;

import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class AmqpJmsPoolConectionFactoryConfig {

	
	@Value("${artemisUser}")
	private String userName;
  
	@Value("${artemisPassword}")
	private String password;
	
	@Value("${brokerUrl}")
	private String brokerUrl;
	
	@Value("${connectionCount}")
	private int connectionCount;
	
	@Bean
	public JmsConnectionFactory jmsConnectionFactory() {
			JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(userName, password, brokerUrl);
			return jmsConnectionFactory;
	}

	@Bean(initMethod="start",destroyMethod="stop")
	public JmsPoolConnectionFactory jmsPoolConnectionFactory() {
		JmsPoolConnectionFactory jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
		jmsPoolConnectionFactory.setMaxConnections(connectionCount);
		jmsPoolConnectionFactory.setConnectionFactory(jmsConnectionFactory());
		return jmsPoolConnectionFactory;
	}
	
	@Bean
	public JmsConfiguration jmsConfiguration(@Value("${connectionCount}") int connectionCount){
		JmsConfiguration jmsConfiguration = new JmsConfiguration();
		jmsConfiguration.setConcurrentConsumers(connectionCount);
		jmsConfiguration.setConnectionFactory(jmsPoolConnectionFactory());
		return jmsConfiguration;
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		return new JmsTemplate(jmsConnectionFactory());
	}
}
```
- Entry point to boot application
```java
package bootcamelamqp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class CamelAmqpApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelAmqpApplication.class, args);
	}
}
```


