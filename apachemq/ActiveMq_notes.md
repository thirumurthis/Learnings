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
  - Refer below for more details on messages.
        
`JMS message Headers`:
 - Headers are set automatically by the clients `send()` method.
 - Two types of headers which differ semantically
    - 1. `standard list of headers and methods to work with them`. 
    - 2. `message properties to facilitate custom headers based on primitive Java types`.
          
`JMS domains` 
  - Two domain, **point-to-point** and **Publish/subscribe**.
  - Refer below for more information
  
`Administered objects` 
  - Preconfigured JMS object, contains provider specific configuration data for use by client. These object are typically accessed by client using JNDI.
  
`Connection factory` 
  - clients created connection to JMS provider using connection factory.

`Desitnation` 
  - Object to which messages are addressed and sent. Also from which the messages are received.
  
  ------------
  
##### JMS Message Header 
  `Standard Header`
   - `JMSDestination`
     - The destination to which the message is being sent.
     - Valuable for clients who consumer messages from more than one destination.
     
   - `JMSDeliveryMode` 
     - Delivery mode is set on the producer and is applied to all the messages sent from that producer. This can be overriden for specific messages too.
     - JMS supports two type of delivery modes for messages
       - Persistent (default mode)
          - Advices JMS provider to persist the message so it is not lost in case of provider failure.
          - Also JMS provider should deliver the persistent mesasge once and only once.
       - Non-persistent
           - JMS provider will not persist the message, if the JMS Provider fails the message may get lost, but won't be delivered twice.
   
   - `JMSExpiration`
      - Default time to live is set to 0, which means the message will not expire.
      - This value can be set using `setTimeToLive()` method globally on the all the messages sent by the producer.
      - JMS providers shoudn't deliver messages that have expired, the JMS client should be developed in a way not to process expire message.
  
   - `JMSMessageID` 
      - String uniquely identifies a message that is assigned by the JMS provider.( __must begin with ID__)
      - messageID can be used for meessage processing or for historical purposes in message storage mechanism. 
      - the producer can advise JMS provider that the JMS application doesn't depend on the value of this header using `MessageProducer.setDisableMessageID()`, the JMS provider accepting this advice should set the message ID to null. But some JMS provider might ignore this and assign a message ID anyway.
      
  - `JMSPriority`
      - assigns level of importance to a message.
      - this header is also set on the message producer. If the priority is set on the producer, it applies to all messages sent from that producer. 
      - the priority can be overridden for individual messages.
      - Priority ranges 0 to 9, 0 being lowest and 9 the highest.
          - Priorites (0-4) => finer granularities of the normal priority.
          - Priorities (5-9) => finer granularities of expediated priorty.
       - JMS providers aren't required to implement message ordering, but higher-priority messages should be delivered before lower-priority messages.
      
 - `JMSTimeStamp`
     - The header denotes the time the message was sent by the producer to the JMS provider.
     - Producer can advice to disabe this using `Producer.setDisableMessageTimeStamp()` method. In this case the JMS provider will set this value to 0.
     
##### Optional headers
 
   - `JMSCorrelationID `
      - used to associate the current message with a previous message. 
      - commonly used to associate a response message with a request message.
         - Provider-specific messageID (beings with ID)
         - Application-specific String ( this must not start with ID)
         - Provider-native byte[] value ( client assigns a specific value to match that expected by non-JMS clients) 
        
 - `JMSReplyTo`
    - used to specify a destination where a reply should be sent.
    - This header is commonly used for `request/reply style` of messaging.
    - Messages with this header populated typically expects a response, but it's actually optional.
    - Client must make the decision to respond or not.
    
 - `JMSType`
     - Semanitcally identify message type, used by vendors not to anything with the payload of the JMS message.
     
  - `JMSRedelivered`
     - Used to indicate that a message was previously delivered but not acknowledged.
     - This happens if a consumer fails to acknowledge delivery or JMS provider is not notified due to some exception thorwn that prevent the acknowledegment reaching the provider.

##### JMS Message Properties
  - this are additional header that can be specifed on mesage.
  - this can be used to set `custom headers` using generic methods (working with java primitive types like Boolean, byte, short, int, etc.).
  - `propertyExits()` method is for testing whether the given property exists on a messge.
  - `getPropertiesNames()` method returns an Enumeration of all the properties on a given message. To easily iterate through all of them.
  
  ```java  
  public interface Message{
     boolean getBooleanProperty(String name) throws JMSException;
     ...
  ```
  
##### `Headers` and `properties` are important for filtering the mesage received by a client subscribed to a destination. 

__` Message Selectors`__

Example:
  - JMS client is subscribed to a given destination, but it may want to filter the types of message it recives. This is exactly where the headers and properties can be used.
  - If a consumer registered to receive messages from a queue and only interested in messages about particular stock symbol. The JMS client (consumer) can utlize JMS message selectors to inform the JMS provider that it only wants to receive mesasages containing particular values in a particular property.
  
  - Message selector allows JMS client to specify which messages it want to receive from the destination based on values in message header.
  - `Selectors are conditional expression` passed as string arguments defined using a subset of SQL92.
  - Using Boolean logic, message selector use headers and prperties as criteria for simple boolean evaluation, if messages not maching these expression then those are not delieverd to the client (consumer).
  - `Message selector can't reference a message payload`, only the message headers and properties.
  
  - When creating a session the selector conditional expression defined as string can be passed as an argument `javax.jms.Session` object method.
  
  ```
  # JMS selector syntax.
  Item => values
  Literals => TRUE/FALSE; numbers 5,-4,+8,+1.021,2E8
  Identifiers => header or property field
  Operators => AND, OR, BETWEEN, LIKE, = <> , <,>, IS NULL, IS NOT NULL, +, *, -, =>, <=
  ```
  
  Sample Message selector code in the Producer/consumer
  
  ```java
 //Producer  
  public void sendMessage(Session session, MessageProducer producer, Desitnation destination, String load, String selectorSymbol, double price) throws JMSException {
    
    TextMessage textMessage= session.createTextMessage();
    textMessage.setText(load);
    textMessage.setStringProperty("SYMBOL", selectorSymbol);
    textMessage.setStringProperty("PRICE", price);
    producer.send(destination, textMessage);
    }
   
  // consumer example with message selector
  // MSFT is the stock symbol for mcirosoft and below consumer get only those info
  // the selector is directly added as string value
   MessageConsumer consumer = session.createConsumer(destination, "MSFT");
  
  // expression where the price is provided already to this object
  // The stock matching microsoft and price greater than previous price
  String conditionSelector = "SYMBOL = 'MSFT' AND PRICE > " + getPreviousPrice();
  MessageConsumer consumer = session.createConsumer(destination, conditionSelector);
  
  
  // more selector example 
  // PE (price to earning ratio)
  
  String conditionalSelector= "SYMBOL IN ('MSFT','BA') AND PRICE >"
              + getPreviousPrice() +" AND PE_RATIO < "
              + getCurrentPERationAvg();
  MessageConsumer consumer = session.createConsumer(destination, conditionalSelector);
  
  ```
  
#### MESSAGE Body (`Payload`)
  
   - JMS define 6 Java types for message body
      - `Message` - base message type. Used to send a message with no payload, only headers and proeprties. Typically used for event notification.
      - `TextMessage` - Message whose payload is a String. commonly used to send texts, XML data.
      - `MapMessage` - Uses a set of name/value pairs as payload. Names are String and Values are a Java primitive type.
      - `BytesMessage` - contains an array of initerpreted bytes as the payload.
      - `StreamMessage` - A message with a payload containing a stream of primitive java types thats filled and read sequentially.
      - `ObjectMessage` - Used to hold a serializable java object. Usually complex java object. Also supports java collections.
      
##### JMS Domains
  - __point-to-point (PTP)__
     - uses destinations known as `queues`
     - messages sent and recieved either synchronously or asynchronously
     - each message received on the queue is delivered once and only once to single consumer.
     - multiple consumers can register on a single queue, but only one consumer will recive a given message and then it is upto the consumer to acknowledge the message.
     -JMS provider is distributing the messages in a round-robin style across registered consumers.
     
  - __Publish/Subscribe (pub/sub)__   
     - uses destination known as `topics`
     - publisher sends messages to the topic, subscribers register to receive messages from the topic.
     - any message sent to topic is deleivered automatically to _all consumers_. 
     - topics don't hold messages unless explicitly instructed. This can be achieved via the use of __`durable subscription`__.
     - using `durable subscription` when a subscriber disconnects from the JMS provide, its the responsibility of JMS provider to store messages for the subscriber. Upon reconnecting, the durable subsciber will recive all unexpired messages from the JMS provider.
     - `durable subscription` used in case if subscriber disconnection should not miss any messages.
     
     
 __`Message durablity and Message persistence` are different.__
 
 - `durable subscription` is infinite.
     - it's registered with the topic subscription to tell JMS provider to preserve subscription state in event that the subscriber disconnects.
     - the JMS provider holds all messages unilt that subscriber connects again or until the subscriber explicitly unsubscribes from the topic.
     
  - `nondurable subscription` is finite.
     -it's registered with the topic subscription to tell JMS provider to not preserve the subscription state in the event that subscriber disconnects.
 
 
 `Message Persistence` is independent of message domain.
   - Message persistence is a quaility of service used to indicate the JMS application ability to handle missing messages in the event of JMS provider failure.
   - this is specified using `setDeliverMode` method using one of `JMSDeiveryMode` class `PERSISTENT` or `NON-PERSISTENT` properties as argument.
 
 
##### Request/Reply messaging in JMS
  - JMS spec doen't define request/reply messaging as a formal messaging domain.
  - There are some message headers and couple of convenience classes for handling baskc request/reply messaging in an asynchronous back and forth conversational pattering in either PTP or pub/sub model.
  - the comination of JMSReplyTo sepcifies the destination and JMSCorrelationID in the reply mesage specifies the JMSMessageID of the requeste message. These headers are used to link the reply to original request message.
  - a Temporary destinations are those that are created only for the duration of a connection and only be consumed from, by the connection that created it.
  - Convenience class `QueueRequestor` and `TopicRequestor` provide request() method that sends a request message and waits for reply message.
  - These classes are useful only for basic form of request/reply, one reply per request. Not designed to handle complex cases of request/reply, in this case it would be development of new JMS application.
 
##### Administered objects
  - used to hide provider-specific details from the client and to abstract the JMS provider administration tasks.
  - It's common to look up these objects via JNDI, but not required.
  - Types of administered objects
      - ConnectionFactory (JMS clients use the Connection factory to create connection to a JMS provider, connection typically represent an open TCP socket.)
      - Destination ( This object encapsulates the provider specific address to which messages are sent and from which messages are consumed. Destination are created using `Session` object, lifetime matches the connection from thwich the session was created.
          - Temporary destinations are unique to that connection that ws used to created them and live as long as the connection that created them, only the connection that created them can create consumers from them. This is commonly used for request/reply messaging. 

##### JMS Application
  - 1. Acquire JMS connection factory
  - 2. Create JMS Connection using connection factory
  - 3. Start JMS connection
  - 4. Create JMS session from connection
  - 5. Acquire a JMS destination
  - 6. A*) Create JMS producer (create JMS message and address it to a destination)
  - 6. B*) Create JMS consumer ( if needed, register a JMS message Listener)
  - 7. Send or receive JMS messages
  - 8. Close all JMS resources (connection, session, etc.)
  
```java
  // Sample for sending message
    ConnectionFactory connectionFactory;
    Connection connection;
    Session session;
    Destination destination;
    MessageProducer producer;
    Message message;
    boolean useTransaction = false;
    try {
        Context ctx = new InitialContext();
        connectionFactory =
        (ConnectionFactory) ctx.lookup("ConnectionFactoryName");
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(useTransaction,
        Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("TEST.QUEUE");
        producer = session.createProducer(destination);
        message = session.createTextMessage("this is a test");
        producer.send(message);
    // catch exception
    //finally close 
    finally{
       producer.close();
       session.close();
       connection.close();
       }
    
```

Similar to send message, below is example of reveive message `synchronous` way
```java

// Synchronous way of sending message.
    ...
    ConnectionFactory connectionFactory;
    Connection connection;
    Session session;
    Destination destination;
    MessageConsumer consumer;
    Message message;
    boolean useTransaction = false;
    try {
    Context ctx = new InitialContext();
    connectionFactory =
    (ConnectionFactory) ctx.lookup("ConnectionFactoryName");
    connection = connectionFactory.createConnection();
    connection.start();
    session = connection.createSession(useTransaction,
    Session.AUTO_ACKNOWLEDGE);
    destination = session.createQueue("TEST.QUEUE");
    consumer = session.createConsumer(destination);
    message = (TextMessage) consumer.receive(1000);
    System.out.println("Received message: " + message);
```
Note:
  - There is no timing consideration needed to make sure that the producer is sending messsage at the same time the consumer is available.
  - All mediation and temporary storage of the message is the job of the JMS provider implementation.
  - the consume must poll for messages over and over again in a loop.
  
 
 Example of consumer receiving message.
 ```java
 
 public class AsyncExampleMessageConsumer implements MessageListener {

      ConnectionFactory connectionFactory;
      Connection connection;
      Session session;
      Destination destination;
      MessageProducer producer;
      Message message;
      boolean useTransaction = false;
      try {
          Context ctx = new InitialContext();
          connectionFactory =
          (ConnectionFactory) ctx.lookup("ConnectionFactoryName");
          connection = connectionFactory.createConnection();
          connection.start();
          session = connection.createSession(useTransaction,
          Session.AUTO_ACKNOWLEDGE);
          destination = session.createQueue("TEST.QUEUE");
          consumer = session.createConsumer(destination);
          consumer.setMessageListener(this);
      } catch (JMSException exe) {
            System.Out.println("Exception occured "+ exe.getMessage());
      } finally {
            producer.close();
            session.close();
            connection.close();
      }
      
      // implement the onMessage method from the MessageListener
      public void onMessage(Message message) {
          if (message instanceof TextMessage) {
             System.out.println("Received message: " + message);
           }
      }
  }
 ```
Note:
   - `onMessage` method override and implemented.
   - Now `consumer` no need to poll for messages repeatedly, instead the MessageListener implementation is registered with JMS Provider. The message will be delivered automatically to the `onMessage()` method in an asynchronous manner.

`ConnectionFactory`,`Connection` and `Destination` object supports concurrent access.

`Session`,`MessageProducer` and `Messageconsumer` objects don't support concurrent access and shouldn't be shared across threads in java application.


![image](https://user-images.githubusercontent.com/6425536/81507140-1d32f680-92b0-11ea-993b-bc8372ecfe3c.png)

##### Connecting to ActiveMq
 - ActiveMQ provides `connector` a connectivity mechanism that provides `client-to-broker` (using `transport connectors`) and `broker-to-broker` communications (using `network connectors`).
 - ActiveMQ supports variety of protocols.
 
##### URIs as per the spec

```
 <scheme>:<scheme-specific-part>
 
 Example:
 
  mailto:mail@domain.com
  mailto - scheme
  mail@domain.com - email address uniquely identify both the service and particuar resources within that service.
  
  Hierarcihal URIs (known as URL Uniform resource Locator)
  
  <scheme>://<authority><path><?query>
  
  http://www.websitename.com/forum/hello.jsp?id=1111
  http - scheme
  query - used to specify additional parameters.
  
  ## ActiveMq uses the Hierarcihal type of uri
  tcp://localhost:61616
 ```
 
 - `tcp://localhost:61616` is a typical hierarchial URI used in ActiveMq which means `to create a TCP connection to localhost on port 61616`.
 - In ActiveMQ, using this kind of simple hierarchial URI pattern are referred to as __`low-level connectors`__
 - `tcp://localhost:61616?trace=true` - query => trace=true extends by telling broker to log all commands sent over this connector.
 - `failover` transport in ActiveMq supports automatic reconnecton as well as ability to connect to another borker in case the brokder to which a client is currently connected is unavailable. ActiveMQ makes this easy using __`composite URIs`__. 
    - ` static:(tcp://myhost1:61616,tcp://myhost2:61616)`, note there are no spaces, which is important to note.
        - static => scheme
        - tcp://myhost1:61616,tcp://myhost2:61616 => composite URI
           - two low-level connector.
 
##### How to configure transport connectors?
   - conf/activemq.xml
   ```
   <transportConnectors>
      <transportconnector name="openwire" uri="tcp://localhost:61616">
      <transportconnector name="stomp" uri="stomp://localhost:61616">
   <transportConnectors>   
   ```

From the client, the transport connector URI is used to create a ocnnection to the broker to send and recive messages.
```java
  ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
  Connection connection = factory.createConnection();
  connection.start();
  Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
```
The URI used above can be different for different protocols.

 - `tcp connector` - is default and most widely used, since provides optimal performance.
- `NIO connector` - which alos uses TCP network protocol underneath, but provides a bit better scalability than tcp connector.
- `UPD connector` - UDP protocol introduces some performance advantages but not reliablie comparted to TCP protocol. (check networking notes TCP/IP vs UPD)

|Protocol | Description
|--------|-------------|
|TCP| Default network protocol|
|NIO (New I/O API protocol)| Provide better scalability for connection from producer and consumer to the broker|
|UDP| Consider UPD protocol when need to deal with firewall between clients and broker.
|SSL| SSL when wanted to secure communication between clients and broker.
HTTP(S)| When wanted to deal with firewall between clients and broker.
|VM| This is not a network protocal per se, VM protocal is used when broker and clients communicate with a broker that is embedded in same JVM.

TCP connectors:
 - Before exchanging messages over the network, it needs to serialized to a suitable form. 
 - Messages must be serialized in and out of a byte sequence to be sent over the wire using what’s known as a `wire protocol`. The default wire protocol used in ActiveMQ is called `OpenWire`.
 - `OpenWire` isn't specific to TCP network standards.
 - TCP syntax,
    `tcp://hostname:port?key=value&key=value` -> query part (key=value) is optional.

```
<transportConnetors>
   <transportConnector name="tcp" uri="tcp://localhost:61616?trace=true"/>
</transportConnetors>
   
```

Like trace=true, TCP connector has many query properties, refer the documentation.

 _Note: After modifying the config/activemq.xml, the ActiveMQ needs to be restarted_
 
 `New I/O API protocol (NIO)`:
   - an alternative apporach to network progreamming to access to some low-level I/O operations of modern operating system.
   - Prominant feature of NIO are `selectors` and `non-blocking` I/O programming. Allowing developers to use the same resource to handle more network clients and heavier loads on servers.
   - NIO transport connector is same as TCP, only underlying implementation in NIO is performed using NIO API.
   
When to use NIO Connector?
   - when large number of clients want to connect to the broker.
      - Number of clients that can connect to a broker is limited by the number of threads supported by the OS, since NIO connector implementation starts few threads per client than the TCP connector we can use NIO when TCP doesn't meet the needs.
   - when there is a heavy network traffic to the broker.
      - Better performance (uses less resources on broker side)
 
Syntax of NIO connector (same as the TCP only the scheme and port changes)
 
 ```
 <transportConnectors>
    <transportConnector name="tcp" uri="tcp://localhost:61616?trace=true">
    <transportConnector name="nio" uri="nio://localhost:61618?trace=true">
 </transportConnectors>
 ```
 
 Producer can be using the NIO, the consumer can use TCP connector as depicted in the below image.

![image](https://user-images.githubusercontent.com/6425536/81514098-ed9ce200-92e1-11ea-9d4a-3fe29b91b42d.png)


#### UDP vs TCP

`User Datagram Protocol (UDP)` along with `TCP` make up the core of internet protocols.
The purpose of these two protocols is identical to send and receive data packets
(datagrams) over the network. 

Two main differences between them:

  - _`TCP is a stream-oriented protocol`_, which means that the `order of data packets is guaranteed`. There’s no chance for data packets to be duplicated or arrive out of order. _`UDP, on the other hand, doesn’t guarantee packet ordering`_, so a receiver can expect data packets to be duplicated or arrive out of order.

  - _`TCP also guarantees reliability of packet delivery`_, meaning that packets won’t be lost during the transport. This is ensured by maintaining an active connection between the sender and receiver. On the contrary, _`UDP is a connectionless protocol`_, so it can’t make such guarantees.
  
  Syntax example:
  ```
   udp://hostname:port?key=value
  ```
  
  Warnings:
  
When should you use the UDP transport instead of the TCP transport? 
 
Two such situations where the UDP transport offers an advantage:

  - The broker is located behind a firewall that you don’t control and you can access it only over UDP ports.
  - when using time-sensitive messages and you want to eliminate network transport delay as much as possible.
	
 Pitfalls regarding the UDP connector:
    
  - Since UDP is unreliable, end up losing some of the messages, application should know how to deal with this situation.
  - Network packets transmitted between clients and brokers aren't just messages, but can also contain so-called control commands. If some of these `control commands` are lost due to UDP unreliability, the JMS connection could be endangered.
  
##### Secure Socket Layer protocol (SSL)
   - Secure the data transfer
   - `SSL protocol` designed to transmit encrypted data over `TCP` network protocol.
      - It uses a pair of keys (private and public keys) to ensure secure communication channel.
   - ActiveMQ provides SSL Layer over the TCP communication channel between `client and broker`.
   - SSL involves keys and certificates which can be configured.
   - ActiveMQ uses `Java Secure Socket Extension (JSSE)` to implement its SSL functionality.
   
Syntax example:
```
ssl://hostname:port?key=value
```
Configuring the SSL in `activemq.xml`
```
<transportConnectors>
  <transportConnector name="ssl" uri="ssl://localhost:61617?trace=true"/>
</transportConnectors>
```
_Note:_
   - SSL transport needs SSL certificates, and other items to work properly.
   - JSSE defines two types of files for storing keys and certificates, `keystore` and `truststore`.
        - __`keystore`__ => holds your own private certificates with their corresponding private key.
	- __`truststore`__ => trusted certificates of other application (entitites) are stored in `truststores`.
    - The default `keystore` and `truststore` distributed with ActiveMQ are located under ${ACTIVEMQ_HOME}/conf. 
        - `borker.ks` - default broker certificate
	- `broker.ts` - hold trusted client certificates.

  For production usage, use custom generated keystore and truststore.
  
  Exeucting producer (client) with ssl scheme uri and not providing SSL certificates, will result in `SSLHandshakeException`.
  
  Broker log will have `Coult not accept connection: Recieved fatal alert : certiicate unkown.`.
  
  In order to pass the certificates to the Producer and consumer, we need to start the service passing the SSL parameters listed below.
    - `javax.net.ssl.keyStore` - keystore the client should use
    - `javax.net.ssl.keyStorePassword` - defines the password for keystore
    - `javax.net.ssl.trustStore` - defines an appropriate truststore client shoud use.
  
 ```
 // When starting the producer or consumer pass this values as JVM arguments.
 
  -Djavax.net.ssl.keyStore=${ACTIVEMQ_HOME}/conf/client.ks \
  -Djavax.net.ssl.keyStorePassword=password \
  -Djavax.net.ssl.trustStore=${ACTIVEMQ_HOME}/conf/client.ts \

 ```
 
 ##### Creating our own SSL resources
 
 ```
 ## create a keystore
 $ keytool -genkey -alias broker -keyalg RSA -keystore custombroker.ks
 
 ## Export this certificate from the keystore, so it can be shared with the brokers clients.
 
$ keytool -export -alias broker -keystore custombroker.ks -file custombroker_cert

"custombroker_cert" file is the broker certificate

similarly we need to create client keystore and certificate.
 
 $ keytool -genkey -alias client -keyalg RSA -keystore customclient.ks
 
 # Client truststore must be created and the broker certifcate must be imported into it <NOTE BELOW COMMAND IMPORT, not using EXPORT like in broker> 
 
 # the broker certificate will be used to create the trust store.
 $ keytool -import -alias broker -keystore customclient.ts -file custombroker_cert
 ```
 
 Different ways to use this files to start the broker
 ``
  1. Replace the existing keystore and truststore under ${ACTIVEMQ_HOME}/conf and restart the broker as $ /bin/activemq console
  
  2. Copy the stores and ceritficate to ${ACTIVEMQ_HOME}/conf and pass the certificate as input when starting the broker as
     $ /bin/activemq console  \
     -Djavax.net.ssl.keystore=${ACTIVEMQ_HOME}/conf/custombroker.ks \
     -Djavax.net.ssl.keystore=password-provided-when-creating-keystore.
     
  3. configuring sslContext in the activeMq.xml.
      <broker xmlns="http://activemq.apache.org/schema/core"  brokerName="localhost" dataDirectory="${activemq.base}/data">
	<sslContext>
	<sslContext keyStore="file:${activemq.base}/conf/custombroker.ks"
	keyStorePassword="password-used-when-creating-keystore"/>	
	</sslContext>
	<transportConnectors>
            <transportConnector name="ssl" uri="ssl://localhost:61617" />
	</transportConnectors>
	</broker>
	
     Start the activemq $ /bin/activemq console
 ```

##### Enabling and disabling SSL Ciphers
   - There are long list of cipher suites avialble defined by JSSE.
   - In a situation if we need to disable certain chipers, we can do it using the option `transport.enabledCipherSuites`
 
 Example:
 
 ```
 <transportConnectors>
    <transportConnector name="ssl" uri="ssl://localhost:61617?transport.enabledCipherSuites=SSL_RSA_WITH_RCA_128_SHA" />
 </transportConnectors>
 
## note that  the multiple Ciphers can be provided as comman seperated values
 
 Payment Card Industry (PCI) cipher is weak to leave them enabled.
 ```
 
 
 ##### HTTP/HTTPS
    - many environments has firewalls which allows only basic servies such as web access and email. 
    - in this situation, ActiveMQ can be used with HTTP transport connector.
    - HTTP transmit hypertext (HTML) pages over the web, it uses TCP as underlying network protocol and adds some additional logic for communication between browser and web servers.
    - ActiveMQ implements `HTTP transport connector` which provides for the exchange of xml-formatted messages with the broker.
    - This is what allows AciveMq to bypass strict firewall rules.
    
 Syntax:
 
 ```
For http,
  http://hostname:port?key=value
  
For https,
  https://hostname:port?key=value
 ```
 ```
 <transportConnectors>
  <transportConnector name="tcp" uri="tcp://localhost:61616?trace=true"/>
  <transportConnector name="http" uri="http://localhost:8080?trace=true"/>
</transportConnectors>
 ```
 
 Note:
    - in order to run the client using the HTTP protocol additional dependency is need `activemq-optional` module.
    
 ```xml
<dependency>
   <groupId>org.apache.activemq</groupId>
   <artifactId>activemq-optional</artifactId>
   <version>5.4.1</version>
</dependency>
 ```
If we don't need this module to be included part of the maven, include below jars in the classpath
```
$ACTIVEMQ_HOME/lib/optional/activemq-optional-<version>.jar
$ACTIVEMQ_HOME/lib/optional/commons-httpclient-<version>.jar
$ACTIVEMQ_HOME/lib/optional/xstream-<version>.jar
$ACTIVEMQ_HOME/lib/optional/xmlpull-<version>.jar
```

Note:

  - In HTTP transport, all broker-to-client communication is performed by sending XML messages. This type of communication can impact performance comparted to the TCP. Just find the workaround on firewall issues.
  

##### ActiveMQ inside the virtual machine (VM connector)
   - used by application to launch an `embedded broker` and connect to it.
   - in VM connector no network connection are created between clients and embedded broker. Communication is performed thorugh direct method invocation of broker objects.
   - no network stack, performance is improved significantly.
   - all subsequent VM transport connector from the same VM will connect to the same broker.
   - embedded broker doesn't lack any standard ActiveMq features.
   - When all the client that use VM transport to the broker close their connection, the broker will automatically shut down.
   
   URI Syntax:
   ```
   vm://brokerName?key=value
   
   // brokerName plays important role in VM transport connector URI which uniquely identifies the broker.
   // two different embedded brokers created by specifying different broker names.
```
  - Transport options are set using query prt of the URI.
  - These options are used to configure the broker.
  - option or query name prefixed with `broker` is used to tune the broker.
 ```
 vm://embeddedbroker1?marshal=false&broker.persistent=false
 
 // above example starts the broker with the persistent disabled.
 ```
    
Alernative URI syntax for VM connector
```
vm:broker:(transportURI,network:networkURI)/brokerName?key=value

Example:
  vm:broker:(tcp://localhost:6000)?brokerName=embeddedbroker&persistent=false
   
   // created a embedded borker named embeddedbroker, with tcp connector on port 6000 and persistence disabled.
 ```

So referring the above VM connector example, the embedded broker exposes tcp and client with the same JVM will connect using VM connector.

Java application outside will connect using tcp connector. Refer the representation below.

![image](https://user-images.githubusercontent.com/6425536/81521315-a15e9b80-92fb-11ea-81e3-c4cc7b732111.png)


##### Embedded broker with external configuration file
  - using transport option (query) `brokerconfig`
```
vm://localhost?brokerConfig=xbean:activemq.xml

// the activemq.xml file should be within the classpath, which will be identified using xbean: protocol.

// this provides the capablity to configure the embeddedbroker like the standalone ActiveMQ broker.
```

Sample code:
  - Where the publisher/producer will start the embedded broker without the need to start extenrally.
  
```java
 private static transient ConnectionFactory factory;
    private transient Connection connection;
    private transient Session session;
    private transient MessageProducer producer;
   
    factory = new ActiveMQConnectionFactory("vm://localhost");
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = session.createProducer(null);
   
```
   - Embedded broker are suited when one java application needs a single broker
   - when many java application uses embedded broker it creates maintenance problem when trying to consitently configure each broker. In this case create a small cluster of standalone broker.
   

##### How to make the ActiveMQ broker highly available and highly scalable?
   - This can be achieved using `network of brokers`
   - A network of brokers creates a cluster composed of multiple ActiveMQ isntances which are interconneted to meet more advanced scenarios.
   
   - Network connctors are channels that are configured between brokers so that those brokers can communicate with each other.
   - The `Network connector is` __`unidirectional`__ `channel by default`.
        - A given broker communincates in one direction by only forwarding messages it recives to the brokers on the other side. This setup is referred as __`forwarding bridge`__.
   - The network connector can also be set in `bidirectinal communication`.
        - A bidirectional channel between brokers - a channel that communitcates not only outward to the broker on the other side of the connection, but also recived message from other brokers on the same channel.
        - This is referred to as __`duplex connector`__.

Represenation of complex network of browkers:

![image](https://user-images.githubusercontent.com/6425536/81525469-aa566980-9309-11ea-8d7b-f2f949b3784f.png)

###### How to configure network connectors?
   - The same way we do for transport connectors, using the config/activemq.xml file.
   - Example configuration:
   ```
    <networkConnectors>
       <networkConnector name="default-nc" uri="multicast://default" />
    </networkConnectors>
   ```
   - __`discovery`__ - is a process of detecting remote broker services.
       - client usually want to discover all available brokers.
       - Brokers, usually want to find other avialable brokers so they can establish a network of brokers.
       - when we have the exact network address of each broker, then it is easy to configure the network statically and also connect client to predefined broker URIs. This is seen inr production, total control of all resources.
       
       - What happens if the client and broker don't know each other's network address, there must be some kind of discovery mechanism to dynamically locate the avilable brokers. This setup is often found in development environment, easy to setup & maintain.
       - The IP mutlicast is used by brokers to advertise their service and locate other avialable brokers using the multicast connector.
       - Clients use the `multicast connector` to discover brokers using a `discovery connectors`.
       - `peer connector` makes creating network of embedded brokers very simple.
       - `fanout connector` enables client to send messages to multiple brokers.
    
    
##### Static connector:
   - used to create a static configuration of multiple brokers in a network.
   
   Syntax example
  ```
   static:(uri1,uri2,..)?key=value
   
   // config/activeMq.xml
   
   <networkConnectors>
     <networkConnector name="custom network" uri="static://(tcp://host1:61616,tcp://host2:61616)" />
   </networkConnectors>  
    
    // This configuration is for the broker on localhost and brokers on hosts host1 and host2 which are up and running. (this could be seen the log message when the MQ starts)
    
    //The output, indicates in this case that the broker on localhost has successfully configured forwarding bridge with other brokers running on two remote hosts host1 and host2.
  ```


 Forward bridging example:
 
 BrokerA (seperate ActiveMQ instance)
 ```xml
 // activemq-brokerA.xml
<broker xmlns="http://activemq.apache.org/schema/core" brokerName="BrokerA" dataDirectory="${activemq.base}/data">
	<transportConnectors>
		<transportConnector name="openwire" uri="tcp://localhost:61616" />
	</transportConnectors>
	<networkConnectors>
		 <networkConnector uri="static:(tcp://localhost:61617)" />
	</networkConnectors>
</broker>
 ```
 
 Broker B (seperate ActiveMQ instance)
 ```xml
 // activemq-brokerA.xml
<broker xmlns="http://activemq.apache.org/schema/core" brokerName="BrokerB"  dataDirectory="${activemq.base}/data">
	<transportConnectors>
	       <transportConnector name="openwire" uri="tcp://localhost:61617" />
	</transportConnectors>
</broker>
 ```

Note:

  -  Start the ActiveMq instance
  
  ` ${ACTIVEMQ_HOME}/bin/activemq console xbean:src/main/resources/brokerA.xml`
  
  ` ${ACTIVEMQ_HOME}/bin/activemq console xbean:src/main/resources/brokerB.xml`
  
  Messages are published to BrokerA, these mesages are then forwarded to BrokerB where they are recived by consumer.

**The network address should be known when using with the static protocol.**

Scenario where the static protocol can be used and its advantage.  
```
  - Clients are in remote location and connecting to a specific location (Say service running at local laptop a home)
  - Depending on number of clients in each remote location, there might be way more network connections into the specific location.
  - The above might burden over the network.
  - To minimize the connection, one approach is to place a broker on each remote location, and allow static network connection between brocker in remote location and broker in specific loaction.
  - This will miminize the number of network connections between the remote location and hte specfic location, it allows the client applications to operate more efficiently.
  - thus less latency and less waiting for the client application.
```

#### Failover protocol:
   - Clients have been configured to connect to a specific broker. `What happens if it is not able to connect to specfic broker or connection fails due to some reason?`
   - The client either die gracefully or tries to connect to the same broker or other broker to resume work.
   - __`failover connector`__ implements automatic reconnection mechanism.
   
 URI Syntax:
 ```
 
 failover:(uri1,uri2....)?key=value
 
 or
 
 failover:uri1,uri2...
 ```
    - failover protocol uses random algorithm to choose one of the underlying connectors.
    - If connection fails, the transport will pick another URI and try to make connection.
    - default configuration also implements `reconnection delay logic`, that is delay of 10ms for first attempt and dobules the delay on further attempts till 30000ms.
    - reconnection logic tries to reconnect indefinitely.
    
```java
	factory = new ActiveMQConnectionFactory("failover:(tcp://host1:61616,tcp://host2:61616)");
    	connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
```

##### Example of Failover protocol and when it can be helpful?
Due to its reconnection capabilities, it’s highly advisable that you use the failover protocol for all clients, even if a client will only be connecting to a single broker. 

For example, the following URI will try to reestablish a connection to the same broker in the event that the broker shuts down for any reason:
```
failover:(tcp://localhost:61616)
```

The advantage of this is that clients don’t need to be manually restarted in the case of a broker failure (or maintenance, and so forth). As soon as the broker becomes available again the client will automatically reconnect. This means far more robustness for your applications by simply utilizing a feature of ActiveMQ. 

----------------

####### Dynamic networks:
  - Several mechanisms implemented in ActiveMq which can be used by brokers and clients to find each other and establish connection (when broker URI is not specified).
  
  - IP multicast is a network technique used for easy tranmission of data from one soruce to a group of recievers (1-to-Many communication) over an IP network.
  - The fundamental concept of IP multicast is `group address`.
  - The `group address` in an IP address in the range of 224.0.0.0 to 239.255.255.255 used by both soruce and recivers.
        - source use this address as destination for data
	- recivers use it to express interest in ata from the group
   
   - when IP multicast is configured, ActiveMQ broker use the multicast protocol to advertise their services and locate the services of other brokers.
   - Clients use multicast to locate brokers and establish a connection with them.
   
URI syntax example:
```
   multicast://ipaddress:port?key=value
```
   
Sample configuration:
```xml
   <broker xmlns="http://activemq.apache.org/schema/core" brokerName="multicast" 
dataDirectory="${activemq.base}/data">
<networkConnectors>
      <networkConnector name="default-nc" uri="multicast://default"/>
</networkConnectors>
<transportConnectors>
      <transportConnector name="openwire" uri="tcp://localhost:61616" discoveryUri="multicast://default"/>
</transportConnectors>
</broker>

//Group name used here is default, instead of specfic ip address

//transport connector discoveryUri attribute is used to advertise this transports URI on the default group.

//All clients interested in finding avialable broker would use this connector.

// uri on the network connector is used to serach for avialable brokers and create network with them. in this case, the broker acts like a client and uses multicast for lookup purpose.

 ```
##### Example use of the multicast protocol, where can it be used? 
   - Multicast protocol provides automatic discovery of other brokers (not like static list of brokers)
   - Multicast protocol is common where brokers are added and removed frequently and in case where brokers Ip address changes frequently.
   - In this case instead of reconfiguring each broker manually it is easier to utilize a discovery protocol.
   
 Disadvantage:
   - all the brokers are discovered automatically, in case if we don't want the broker to not discover automatically the initial configuration should be setup correctly.
   - Segmentation of broker networks is important, message might wind up in broker netowkr where they don't belong.
   - Excessively chatty over network.
   

##### change the broker name using config/activemq.xml
```
<broker xmlns="http://activemq.apache.org/schema/core" brokerName="custom-broker1" dataDirectory="${activemq.base}/data">

// helps when searching log file on specifi broker logs
```

##### Discovery Protocol
   - `discovery transport connector` is on client side of ActiveMq multicast functionality.
   - `discovery connector` is same as failover protocol in its behavior.
   - This connector will multicast to discvoer brokers and randomly choose one to connect.
   
 URI syntax:
 ```
  discovery:(discoveryAgentURI)?key=value
 ```
 
 
 ##### PEER protocol
    - `peer connector` allows to easity network embedded brokers.
    - utility that creates `peer-to-peer` netwokr of embedded brokers.
 
 URI syntax:
 ```
 peer://peergroup/brokerName?key=value
 
 //when embedded broker starts with peer connector, wil also configure the broker to establish network connectios to other brokers in the local network with the same group name.
 ```
 Sample code for producer
 ```java
 	factory = new ActiveMQConnectionFactory("peer://group1");
    	connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 ```
 
 Representation of Peer-to-Peer network on embedded broker
 ```
______________                                  ______________
| Publisher   |                                 | consumer    |
| application |                                 | application |
 ______________                                  _____________
    \                                           /
     \ vm:             N/W                     / vm:   
      \_________     connector    __________ /
      |  Broker |  <-----------> | Broker   |
       _________                  __________
 
 ```
 
 The configuration xml for the ActiveMq instance is started with 
 ` $ /bin/activemq console xbean:src/main/resource/activemq-multi.xml
 ```
 // store this in activemq-multi.xml (other content similar to default activemq.xml
 
  <networkConnectors>
          <networkConnector name="default-nc" uri="multicast://default"/>  
      </networkConnectors>

      <transportConnectors>
          <transportConnector name="openwire" uri="tcp://localhost:61616" discoveryUri="multicast://default"/>
      </transportConnectors>
 ```
