
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
