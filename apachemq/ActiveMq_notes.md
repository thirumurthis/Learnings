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
  
  #####JMS Message Header 
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
     
 #####Optional headers
 
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

#####JMS Message Properties
  - this are additional header that can be specifed on mesage.
  - this can be used to set `custom headers` using generic methods (working with java primitive types like Boolean, byte, short, int, etc.).
  - `propertyExits()` method is for testing whether the given property exists on a messge.
  - `getPropertiesNames()` method returns an Enumeration of all the properties on a given message. To easily iterate through all of them.
  
  ```java  
  public interface Message{
     boolean getBooleanProperty(String name) throws JMSException;
     ...
  ```
  
#####`Headers` and `properties` are important for filtering the mesage received by a client subscribed to a destination. 

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
  
####MESSAGE Body (`Payload`)
  
   - JMS define 6 Java types for message body
      - `Message` - base message type. Used to send a message with no payload, only headers and proeprties. Typically used for event notification.
      - `TextMessage` - Message whose payload is a String. commonly used to send texts, XML data.
      - `MapMessage` - Uses a set of name/value pairs as payload. Names are String and Values are a Java primitive type.
      - `BytesMessage` - contains an array of initerpreted bytes as the payload.
      - `StreamMessage` - A message with a payload containing a stream of primitive java types thats filled and read sequentially.
      - `ObjectMessage` - Used to hold a serializable java object. Usually complex java object. Also supports java collections.
      
#####JMS Domains
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
 
 
#####Request/Reply messaging in JMS
  - JMS spec doen't define request/reply messaging as a formal messaging domain.
  - There are some message headers and couple of convenience classes for handling baskc request/reply messaging in an asynchronous back and forth conversational pattering in either PTP or pub/sub model.
  - the comination of JMSReplyTo sepcifies the destination and JMSCorrelationID in the reply mesage specifies the JMSMessageID of the requeste message. These headers are used to link the reply to original request message.
  - a Temporary destinations are those that are created only for the duration of a connection and only be consumed from, by the connection that created it.
  - Convenience class `QueueRequestor` and `TopicRequestor` provide request() method that sends a request message and waits for reply message.
  - These classes are useful only for basic form of request/reply, one reply per request. Not designed to handle complex cases of request/reply, in this case it would be development of new JMS application.
 
#### Administered objects
  - used to hide provider-specific details from the client and to abstract the JMS provider administration tasks.
  - It's common to look up these objects via JNDI, but not required.
  - Types of administered objects
      - ConnectionFactory (JMS clients use the Connection factory to create connection to a JMS provider, connection typically represent an open TCP socket.)
      - Destination ( This object encapsulates the provider specific address to which messages are sent and from which messages are consumed. Destination are created using `Session` object, lifetime matches the connection from thwich the session was created.
          - Temporary destinations are unique to that connection that ws used to created them and live as long as the connection that created them, only the connection that created them can create consumers from them. This is commonly used for request/reply messaging. 

#####JMS Application
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
