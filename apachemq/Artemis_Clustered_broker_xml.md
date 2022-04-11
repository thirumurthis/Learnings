### Below is a simple static connector configuration of Apache Artemis MQ in the same machine or VM.
 - ports used 
   - host(broker1) - (tcp,amqp) = (61616,5672)
   - host(broker2) - (tcp,amqp) = (61617,5673)

##### First we download the artemis tar and extract it.
##### Second navigate to the bin folder, executed the command in command prompt 
```
### from bin folder, use below commands

> artemis.cmd create brokers/broker1
> artemis.cmd create brokers/broker2
```

##### Update the hawtio or web console port on broker2 
  - We need to udpate bootstrap.xml where the hawtio or Artemis Web console port needs to be changes from default 8161 to 8162 on the broker2 (this file will be located at etc/ folder)

##### update the broker.xml on the broker1 and broker2
  - We need to update the etc/broker.xml configuration to tell Artemis to discover the brokers statically. 
  - Below are the different discovery options
    - Static
    - UPD
    - JGroups


#### broker1/etc/broker.xml
  - update the connector
  - update the cluster-connection
  - update the acceptors (by default all protocol based acceptors will be created, in our case we are only using tcp and amqp)

```xml
<acceptors>
    <acceptor name="artemis">tcp://0.0.0.0:61616?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;amqpMinLargeMessageSize=102400;protocols=CORE,AMQP,STOMP,HORNETQ,MQTT,OPENWIRE;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpDuplicateDetection=true;supportAdvisory=false;suppressInternalManagementObjects=false</acceptor>
    <!-- AMQP Acceptor.  Listens on default AMQP port for AMQP traffic.-->
    <acceptor name="amqp">tcp://0.0.0.0:5672?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpMinLargeMessageSize=102400;amqpDuplicateDetection=true</acceptor>
</acceptors>

<connectors>
   <connector name="netty-connector">tcp://localhost:61616</connector>
   <connector name="server1-connector">tcp://localhost:61617</connector>  <!-- cluster configuration of the other broker2 -->
</connectors>
 
<cluster-connections>
   <cluster-connection name="my-cluster">
      <connector-ref>netty-connector</connector-ref>
      <retry-interval>500</retry-interval>
      <use-duplicate-detection>true</use-duplicate-detection>
      <message-load-balancing>STRICT</message-load-balancing>
      <max-hops>1</max-hops>
      <static-connectors>
         <connector-ref>server1-connector</connector-ref>  <!-- configuration of the connected defined to connect to the second server-->
      </static-connectors>
   </cluster-connection>
</cluster-connections>
```
#### update the borker2/etc/broker.xml
```xml
<acceptors>
   <acceptor name="artemis">tcp://0.0.0.0:61617?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;amqpMinLargeMessageSize=102400;protocols=CORE,AMQP,STOMP,HORNETQ,MQTT,OPENWIRE;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpDuplicateDetection=true;supportAdvisory=false;suppressInternalManagementObjects=false</acceptor>
   <!-- AMQP Acceptor.  Listens on default AMQP port for AMQP traffic.-->
   <acceptor name="amqp">tcp://0.0.0.0:5673?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpMinLargeMessageSize=102400;amqpDuplicateDetection=true</acceptor>
</acceptors>

<connectors>
     <connector name="netty-connector">tcp://localhost:61617</connector>
     <connector name="server2-connector">tcp://localhost:61616</connector> <!-- Connecting to the broker1 server -->
</connectors>

<cluster-connections>
     <cluster-connection name="my-cluster">
        <connector-ref>netty-connector</connector-ref>
        <retry-interval>500</retry-interval>
        <use-duplicate-detection>true</use-duplicate-detection>
        <message-load-balancing>STRICT</message-load-balancing>
        <max-hops>1</max-hops>
        <static-connectors>
           <connector-ref>server2-connector</connector-ref>  <!--Refer to the connector of the other borkers -->
        </static-connectors>
     </cluster-connection>
</cluster-connections>
```

#### java client configuraton

```java
package com.artemis.demo;


import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.activemq.artemis.api.core.client.FailoverEventListener;
import org.apache.activemq.artemis.api.core.client.FailoverEventType;
import org.apache.activemq.artemis.jms.client.ActiveMQConnection;
  
public class ArtemisStaticClusterDiscoveryClient  
{
   public static void main(final String[] args) throws Exception
   {
     new ArtemisStaticClusterDiscoveryClient().runExample(true,false);
      Thread.sleep(10000);
      //new ArtemisClientExample().runExample(false,true);
   }
 
  
   public boolean runExample(boolean produce, boolean consume) throws Exception
   {
      Connection connection = null;
      InitialContext initialContext = null;
      try
      {
         // Step 1. Create an initial context to perform the JNDI lookup.
         Properties p = new Properties();
         p.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
         // Define the connection property or broker url 
         p.put("connectionFactory.ConnectionFactory", "(tcp://localhost:61616,localhost:61617)");
         p.put("queue.queue/exampleQueue", "exampleQueue");
          
         initialContext = new InitialContext(p);
         // lookup on the queue
         Queue queue = (Queue)initialContext.lookup("queue/exampleQueue");
 
         // lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("ConnectionFactory");
 
         // Create an authenticated JMS Connection
        //connection = cf.createConnection("admin1","admin");
         connection = cf.createConnection("artemis","artemis");
        ((ActiveMQConnection) connection).setFailoverListener(new FailoverListenerImpl());
        // connection = cf.createConnection();
         // Create a JMS Session
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
         if(produce) {
         // Create a JMS Message Producer
         MessageProducer producer = session.createProducer(queue);
 
         // Create a Text Message
				/*
				 * TextMessage message = session.createTextMessage("This is a text message");
				 * System.out.println("Sent message: " + message.getText());
				 */ 
         long i = 0;
         //configure the value to run for N minutes 60K = 1 min
         final long duration = 60000;
         long start = System.currentTimeMillis();

         while (System.currentTimeMillis() - start <= duration) {
            TextMessage message = session.createTextMessage("SAMPLE MESSAGE" + i++);

            producer.send(message);
         }

         long end = System.currentTimeMillis();

         double rate = 1000 * (double) i / (end - start);

         System.out.println("We sent " + i + " messages in " + (end - start) + " milliseconds");

         // Send the Message
//         producer.send(message);
         }
         if(consume) {
         // Create a JMS Message Consumer
         MessageConsumer messageConsumer = session.createConsumer(queue);
 
         // Start the Connection
         connection.start();
 
         // Receive the message
         TextMessage messageReceived = (TextMessage)messageConsumer.receive(5000);
 
         System.out.println("Received message: " + messageReceived.getText());
         }
         return true;
      }
      finally
      {
         // Be sure to close our JMS resources!
         if (initialContext != null)
         {
            initialContext.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
   }
   
   private static class FailoverListenerImpl implements FailoverEventListener {

	      public void failoverEvent(FailoverEventType eventType) {
	         System.out.println("Failover event triggered :" + eventType.toString());
	      }
	   }
}
```

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.sample</groupId>
	<artifactId>LearnMq</artifactId>
	<version>0.0.1-SNAPSHOT</version>
 <properties><camel.version>2.24.2</camel.version>
 </properties>
	<dependencies>
	
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>
	
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-core</artifactId>
			<version>${camel.version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-spring</artifactId>
			<version>${camel.version}</version>
		</dependency>

		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<version>1.7.5</version>
		</dependency>
		<dependency>
		   <groupId>org.apache.logging.log4j</groupId>
		   <artifactId>log4j-slf4j-impl</artifactId>
		   <version>2.16.0</version>
		</dependency>	
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.16.0</version>
</dependency>

		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-jms</artifactId>
			<version>${camel.version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-camel</artifactId>
			<version>5.15.10</version>
		</dependency>


		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-broker</artifactId>
			<version>5.15.10</version>
		</dependency>
		
   <dependency>
         <groupId>org.apache.activemq</groupId>
         <artifactId>artemis-jms-client</artifactId>
         <version>2.12.0</version>
      </dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.8.5</version>
</dependency>

		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-client</artifactId>
			<version>5.15.10</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-pool</artifactId>
			<version>5.10.1</version>
		</dependency>
	</dependencies>
</project>
```
Note: Place log4j2.properties under resource folder, any default configuration should work.

#### With the above client running, the messages will be delived to the brokers evenly.

Messages in broker1
![image](https://user-images.githubusercontent.com/6425536/162648398-15cfd058-e3a5-4340-acaa-7f2d5f7ad375.png)

Messages in broker2
![image](https://user-images.githubusercontent.com/6425536/162648493-48b2cc14-6f17-4fcd-8636-6fc6b1958393.png)

#### Starting the broker should see below messages in console (if started as standalone process and not as service)
```
2022-04-10 17:26:51,536 INFO  [org.apache.activemq.artemis.core.server] AMQ221027: Bridge ClusterConnectionBridge@10312362 [name=$.artemis.internal.sf.my-cluster.88306672-b88f-11ec-8641-0a0027000009, queue=QueueImpl[name=$.artemis.internal.sf.my-cluster.88306672-b88f-11ec-8641-0a0027000009, postOffice=PostOfficeImpl [server=ActiveMQServerImpl::name=0.0.0.0], temp=false]@18a8c051 targetConnector=ServerLocatorImpl (identity=(Cluster-connection-bridge::ClusterConnectionBridge@10312362 [name=$.artemis.internal.sf.my-cluster.88306672-b88f-11ec-8641-0a0027000009, queue=QueueImpl[name=$.artemis.internal.sf.my-cluster.88306672-b88f-11ec-8641-0a0027000009, postOffice=PostOfficeImpl [server=ActiveMQServerImpl::name=0.0.0.0], temp=false]@18a8c051 targetConnector=ServerLocatorImpl [initialConnectors=[TransportConfiguration(name=netty-connector, factory=org-apache-activemq-artemis-core-remoting-impl-netty-NettyConnectorFactory) ?port=61616&host=localhost], discoveryGroupConfiguration=null]]::ClusterConnectionImpl@1604020967[nodeUUID=ac345e4d-b88f-11ec-a2b2-0a0027000013, connector=TransportConfiguration(name=netty-connector, factory=org-apache-activemq-artemis-core-remoting-impl-netty-NettyConnectorFactory) ?port=61617&host=localhost, address=, server=ActiveMQServerImpl::name=0.0.0.0])) [initialConnectors=[TransportConfiguration(name=netty-connector, factory=org-apache-activemq-artemis-core-remoting-impl-netty-NettyConnectorFactory) ?port=61616&host=localhost], discoveryGroupConfiguration=null]] is connected
2022-04-10 17:26:52,258 INFO  [org.apache.activemq.hawtio.branding.PluginContextListener] Initialized activemq-branding plugin
2022-04-10 17:26:52,932 INFO  [org.apache.activemq.hawtio.plugin.PluginContextListener] Initialized artemis-plugin plugin
2022-04-10 17:26:56,043 INFO  [io.hawt.HawtioContextListener] Initialising hawtio services
```

#### From the artemis web console, we can also check the server are connected as live nodes (cluster info displays 2 live connection)
![image](https://user-images.githubusercontent.com/6425536/162648774-d03337c2-4c7d-4816-99c7-74ecdb6a7fe2.png)


