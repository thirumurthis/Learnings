### Log Component:
  - Used for logging the the Cmael message using standard format at certain points. 
  - Simple Java DSL, to log the incoming file message.
 
 ```java
   from("file://inbox/input")
   .to("log:input")
   .to("jms:queue:example");
 ```
- `By default, the Log component will log the message body and its type at INFO logging level.`
- More customization of the log
```
  log:incoming?showBodyType=false&maxChars=40
```
#### Log to print the human readable message, like below using Log EIP.
  - below is based on the Spring expression language.
```
from("file://inbox/input")
.log("Message to be logged: ${file:name} containing: ${body}")
 
 //To override the log level
.log(LoggingLevel.DEBUG, "Log", "Log message on incoming file ${file:name} with body: ${body}")
```
  - Spring XML DSL, representation
 ```
   <log message="Log message on incoming file ${file:name} with body: ${body}" logName="Incoming" loggingLevel="DEBUG"/>
 ```

#### Using `Correaltion` Ids
 - When logging messages the message being processed can easily get interleaved, which means the log lines will be interleaved (blank) as well.
 - we can correlate this message using the Exchange ids (exchange.getExchangeId()).
 - we can configure log component to display this message by saying 
 ```
 showExchangeId=true
 ```
 - In Log EIP pattern, we can use `${id}` directly.


#### Using `Tracer`
  - Tracer's role is to trace when and how the messages are routed in camel.
  - This needs to be enabled. To enabled it in Java DSL and Spring xml DSL we need to perform below
 
 ```
 //java DSL
 public void configure() throws Exception {
     context.setTracing(true);
     ...
  }
 ```
 ```
 // Spring xml DSL
 <camelContext id="camel" trace="true" xmlns="http://camel.apache.org/schema/spring">
 ```
  - We can use Delay interceptor to delay the process so we can follow to debug. Check Delay interceptor documentation.

 ##### Customizing the Tracer
  - by creating a bean and registring in the registry
 ```xml
 <bean id="traceFormatter" class="org.apache.camel.processor.interceptor.DefaultTraceFormatter">
       <property name="showProperties" value="false"/>
       <property name="showHeaders" value="false"/>
</bean>
 ```
 
  - We can customize tracer at the route level on the camel context.
```xml
<camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
  <route trace="true">
      <from uri="file://inbox/input"/>
      <wireTap uri="direct:track"/>
      <bean beanType="HelloBean"/>
      <to uri="jms:queue:example"/>
  </route>
  <route>
      <from uri="direct:track"/>
      <bean beanType="PrintService"/>
  </route>
</camelContext>
```
 - Java DSL
```java
public void configure() throws Exception {
    from("file://inbox/input")
    .tracing()  //Tracking enabled 
    .wireTap("direct:track")
    .bean(HelloBean.class)
    .to("jms:queue:example");
    
    from("direct:track")
    .bean(PrintService.class);
}
```
 - Tracing can be managed using JMX as well, but it takes a bit of time to reflect.

### Using Notification
  - Camel management modules offers notifiers for handling internal notifications. 
  - This notification are generted when specific event occur inside Camel, such as 
     - When instance starts or stops
     - When an exception being caught
     - When message is created or completed
  - The `notifiers` subscribe to these events as listeners and they react when an event is received.
  - Different Notifier:
     - LoggingEventNotifier - event using Apache common logging framework
     - PublishEventNotifier - Notifier for dispatching the event to any kind of Camel Endpoint.
     - JmxNotificationEventNotifier - A notifier for broadcasting the events as JMX notification.
   
  - Event loader potentially add second load to the system, and that is the reason it is not enabled by default. 
  - When setting up notifier add filters to filter unwanted notifications.
   
   - Setting up Log notifier using Java DSL
```java
   // Below example, is enabling only the logs events we are intereseted in
  // create a loggingEventNotifier
  LoggingEventNotifier notifier = new LoggingEventNotifier();
  notifier.setLogName("example.EventLog");
  notifier.setIgnoreCamelContextEvents(true);
  notifier.setIgnoreRouteEvents(true);
  notifier.setIgnoreServiceEvents(true);
  // with help of Management Strategy add the notifier
  context.getManagementStrategy().setEventNotifier(notifier);
```
  - Setting up Log notifier using Spring xml DSL
```xml
<bean id="eventLogger" class="org.apache.camel.management.LoggingEventNotifier">
  <property name="logName" value="rider.EventLog"/>
  <property name="ignoreCamelContextEvents" value="true"/>
  <property name="ignoreRouteEvents" value="true"/>
  <property name="ignoreServiceEvents" value="true"/>
</bean>
```
 
