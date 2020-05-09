
ActiveMq Artemis is a next generation MQ service.

Below is a sample architecture of executing the Artemis in Kubernetes.

```
                                           |-------------------------------------|
                                           |          k8s cluster                |
   --------------                          |---------                            |
   | Http client | ----------------------->| Sender  |      -------------------  |
   --------------- <-----------------------| service |----> |                  | |
                                           |--------- AMQP  |                  | |
                                           |                |  Artemis service | |
                                           |                |                  | |
                                           |            |<--|      Topic       | |
                                           |            |     ------------------ |
   --------------                          |--------    |                        |
   | Http client | ----------------------->|reciver |<--| AMQP (protocol)        |
   --------------- <-----------------------|sercice |                            |
                                           |--------                             |  
                                           |-------------------------------------|
  
```

 - K8s external access in most case is performed using HTTP (easier to perform)
 - Still the AMQP can be exposed directly. (option)
 
Form the above flow diagram, insider the K8s cluster all the communication is will be using messaging. 

The `sender service` running within the k8s cluster.
   - incoming request implemented using `JAX-RS` API
   - outgoing side of this service we use using `JMS` API ( this will communicate with Artimes broker, inside this broker is a topic which is where the message goes.)
   
 The `receiver service` 
   - Is going to consume the messages using JMS API
   - Serves up that using JAX-RX API
 
 Sample sender service code
 ```java
 
 @Post
 @Path("/input")
 @Cosumes("text/plain")
 @Produces("text/plain")
 
 public String sendInput(String inMsg){
  //the context is autowired 
    Topic topic = jmsContext.createTopic("inputMsg/messages");
    
    JMSProducer producer = jmsContext.createProducer():
    
    //async opertion this will not block the send handler
    // for completion of the acknowlegement ( this option based on
    // requirements) using asyn no blocking happens.
    producer.setAsync(new CompletionListener(){
    // do what it needs to do
    });
    
    producer.sen(topic,inMsg);
    return "OK\n"; // response to sender
 }
 ```

Sample reciever 

```java

private Queue<String> inMsg = new ConcurrentLinkedQueue<>();

public Receiver(){
  Topic topic = jmsContext.createTopic("inputMsg/messages");
  
  JMSConsumer consumer = jmsConntext.createConsumer(topic);
  
  // the lambda code cleans up the payload and sets to an 
  // simple inmemory variable. inMsg
  consumer.setMessageListener( (msg) ->{
     String str = message.getBody(String.class);
     inMsg.add(str);
     });
  }
   //rest using JAX-RS 
  @Post
  @Path("/output")
  @Produces("text/plain")
  public String recieveOutput(){
    return inMsg.poll();
 }
```

Sample Docker:
```docker
FROM <project-image-location-url>/<project-name>

RUN microdnf --nodocs install java-1.8.0-openjdk \
    && microdnf clean all
    
COPY target/<app-fat>.jar  /app/app.jar
ENV HOME=/app
WORKDIR /app

# to execute the jar and app as user not root.
RUN chown -R 1001:0 /app && chmod -R 775 /app
USER 1001

Expose 8080

CMD ["java", "-jar", "/app/application.jar"]
```


Few tips on running in K8s
```

$ minikube init

## deployment happens to k8s cluster
$ kubectl run broker --image docker.io/ssorj/activemq-artemis

# expose port within the cluster 
# 5672 is MQP port and we are using it.
$ kubectl expose deployment/broker --port 5672


# navigate to the maven based sender service project
$ mvn package

# once success then create docker image for the sender service
$ docker build -t sender .


# the sender service has a k8s deployment yaml execute it.
# sender is the image name created in docker above
# in order not to pull from the remote repoistory
# use the option --image-pull-policy Never
# configuation is passed using -env since both takes environment argument
$ kubectl run sender --image sender --image-pull-policy Never -env MESSAGING_SERVICE_HOST=broker


# expose the port of the sender service outside the cluster
# to expose outside we use --type NodePort, in this case a port will opened.
# this port can be accessed outside the cluster.
# deployment/sender was the deployment got created in privious command

$ kubectl expose deployment/sender --port 8080 --type NodePort

// once send service is setup we had to build reciver service

# navigate to reciver service code
$ mvn package

# docuer build image
$ docker build -t reciver .


# kubectl perform deployment of receiver
$ kubectl reciver --image receiver --image-pull-policy Never --env MESSAGEING_SERVICE_HOST=broker

# expose port
$ kubectl expose deployment/receiver --port 8080 --type NodePort

# check if the pods are running
$ kubectl get pod

# check the service running 
$ kubectl get service
  // this will list the port info that are exposed internally and externally.
  // under the output check the ports(s) column


# minikue provides a command to get the service url
$ send_url=$(minikube service sender --url) // sender is the deployment service name.


## in linux use curl command
$ curl -X POST -H "content-type: text/plain" -d msg1 $send_url/<url-of-rest>
// The request now sent to sender servive, the sender service will send it 
// immediately to topic since the user of jms producer.

```
