Apache MQ is `message oriented middleware (MOM)`

When to use Active MQ?
  - `Heterogeneous application itegration`
     - Active MQ broker is written in java, also provides client for other language.
  - As a replacement for RPC
     - Applicaton using RPC uses synchronous calls.
  - `loose coupling` between application
     - loose coupling, provides fewer dependencies.
     - utlize asynchronos communication (where the message is sent without waiting fro response - __`fire-and-forget`__
  - Backbone of an `event-driven architecture`
  
Active MQ Terminology:

![image](https://user-images.githubusercontent.com/6425536/81486471-de4a6580-9209-11ea-9e59-0151df27776c.png)


`JMS - JAva Message Service`
  - provide a standardized API to sennd and recive messages using the java programming language in  a vendor-netrual way.
  - JMS itself is not Message oriented Middleware, this is an API that abstracts the interaction between messageing cluent and MOM's.
  
 `JMS client` 
   - Application to send and recive message. 100% java
   - JMS client utlize the `MessageProducer` and `MessageConsumer` interface.
   - The JMs Provider should furnish an implementation of each of these interface.
   - Possible for JMS client to handle both sending and receiving messages.
     
 `Non-JMS client` 
   - Similar to JMS client, but wirtten using JMS provider's native client API.
   - Might offer additional features, for example utlilixing the CORBA IIOP protocol or different native protocol instead of Java RMI.
   - Many providers also provide a non-JMS client API.
  
 `JMS producer` 
   - Client application that created and sends JMS messages.
   - client uses MessageProducer class for sending messages to a destination.
   - Default destination is set when the producer is crated using `Session.createProducer()` method. This can be override for individual message using `MessageProducer.send() method`.
   - MessageProducer provides method to set various message headers like `JMSDeliveryMode`, `JMSPriority`, `JMSExpiration` (using get/setTimeToLive()).
      
`JMS consumer` 
  - Client application that recieved and process JMS messages.
  - client uses `MessageConsumer` class for consuming messages from destination.
  - MessageConsumer can consume message synchronously using one of receive() method.
  - MessageConsumer can also consume messages asynchronously by implementing `MessageListener`. The `MessageListener.onMessage()` method is invoked when messages arrived on the destination.
  - The destination is set when the consmer is created using `Session.createConsumer()` method. There is _no_ method available for setting the destination on MessageConsumer.
       
`JMS provider` 
  - Implementation of JMS API interface. (100% java)
  - Vendor sepecific MOM
  - The implementation provides access to the MOM via a standardized JMS API. (analogous to JDBC driver)
  
`JMS message` 
  - Fundamental block of MOM, send and received by clients.
  - JMS message allows anything to be send as part of the message like text and binary data. As well as information in the headers.
  - JMS message is of two parts,
  - Headers (JMSCorrelationID, JMSDeliveryMode, JMSDestination, JMSExpiration, JMSPriority, JMSMessageID, JMSRedeivered, JMSTimestamp, JMSType, etc.)
  - PayLoad (Text, binary etc)
  - Complexity of the JMS message resides in the headers.
          
`JMS message Headers`:
  - Two types of headers which differ semantically
    - 1. `standard list of headers and methods to work with them`. 
    - 2. `message properties to facilitate custom headers based on primitive Java types`.
          
`JMS domains` 
  - Two domain, **point-to-point** and **Publish/subscribe**.
  
`Administered objects` 
  - Preconfigured JMS object, contains provider specific configuration data for use by client. These object are typically accessed by client using JNDI.
  
`Connection factory` 
  - clients created connection to JMS provider using connection factory.

`Desitnation` 
  - Object to which messages are addressed and sent. Also from which the messages are received.
  
  
  
