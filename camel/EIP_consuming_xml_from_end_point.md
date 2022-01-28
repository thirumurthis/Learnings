1. Create a simple spring boot application to render xml content.

- pom.xml
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>rest-xml</groupId>
  <artifactId>simple-xml</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  
   <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.5.RELEASE</version>
    </parent>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- xml dependency-->
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
        </dependency>
        <!-- xml dependency end-->
    </dependencies>
    <properties>
        <java.version>1.8</java.version>
    </properties>
</project>
```

- Spring main java class
```java
package com.simple.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@SpringBootApplication
public class SpringBootApp {
    public static void main(String[] args) {

        SpringApplication.run(SpringBootApp.class, args);

    }
}

@RestController
class XmlStringConverter {

    @GetMapping(path= "/content",produces =  { "application/xml", "text/xml" })
    public String articleInfo() throws JsonParseException, JsonMappingException, IOException {

    	
     
    	 File xmlFile = new File("/path/of/xml/file/resources/my-articles.xml");
    	 
    	 String xml2String ="";
    	 try (BufferedReader bufReader = new BufferedReader(new FileReader(xmlFile))){ 
           StringBuilder sb = new StringBuilder(); 
           String line = bufReader.readLine(); 
           while( line != null){ sb.append(line).append("\n"); 
           line = bufReader.readLine(); } 
           xml2String= sb.toString(); 
    	 }
    	
       //Below code can be used to convert the XML content to a JSON string and rendered as repsonse
       // When enabling this block, update the produces to application/json or remove the attribute from GET mapping annotation
       /*
    	 if(false) {
              InputStream is = JacksonXmlModule.class.getResourceAsStream("/articles.xml");

              XmlMapper xmlMapper = new XmlMapper();
              xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
              Object obj = xmlMapper.readValue(is, Object.class);

              return obj.toString();
    	 }
       */
    	 return xml2String;
    }
}
```
- my-articles.xml
```
<note>
<to>X</to>
<from>Y</from>
<heading>Reminder</heading>
<body>Message to X from Y!</body>
</note>
```
- Now in the browser if we use `https://localhost:8080/content` the xml content should be rendered.
![image](https://user-images.githubusercontent.com/6425536/151495625-893bd593-62c0-4489-8022-a53ede8e14b9.png)


- In below camel context (using 2.23.0+) version, start the Aretmis broker service to be running.
- The camel will hit the http endpoint to download the content and push it to queue

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:camel="http://camel.apache.org/schema/spring"
	xmlns:util="http://www.springframework.org/schema/util"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://camel.apache.org/schema/spring
		http://camel.apache.org/schema/spring/camel-spring.xsd
		http://www.springframework.org/schema/util
		http://www.springframework.org/schema/util/spring-util.xsd">

  
    <!-- Below can be used if we are going to hit the https end point. 
         using camel-http4 lib, and https4 scheme. the keystore should be generted using cert java keytool
         {{}} in the camel context will be resolved from the properties file
          The poperties file should be passed as parameter to the been
           <bean id="properties" class="org.apache.camel.spring.spi.BridgePropertyPlaceholderConfigurer">
                <property name="locations"><list value="file://path/to/properties-file" /></property>
           </bean>
   -->
     <!--
     <camel:sslContextParameters id="sslContextParams">
       <camel:keyManagers keyPassword="{{password-from-congiguration-file}}">
                <camel:keyStore resource="{{key-store-path}}" password="{{key-store-password}}"/>
       </camel:keyManagers>
   </camel:sslContextParameters>

    -->
  

  <!-- below bean configuration is used when we include aretmis jms client dependecies since this uses JMS client -->
  <!--
    <bean id="jmsConnectionFactory" class="org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory">
         <constructor-arg index="0" value="tcp://localhost:61616?wireFormat.maxInactivityDuration=500000"/>
    </bean>
  --> 
  <!-- When using the artemis-jms client then instead of AMQPComponent, we need to use JmsComponent -->
   <!--   
    <bean id="jms" class="org.apache.camel.component.jms.JmsComponent">
      <property name="configuration" ref="jmsConfig" />
    </bean> 
   -->
	
     <!-- use below when using Qpid client -->
     <bean id="jmsConnectionFactory" class="org.apache.qpid.jms.JmsConnectionFactory">
        <property name="remoteURI" value="amqp://localhost:5672" /> <!-- broker.xml of artmies should enable the amqp port-->
        <!--  <property name="remoteURI" value="tcp://localhost:61616" /> -->
    </bean>
	
    <bean id="jmsPooledConnectionFactory" class="org.messaginghub.pooled.jms.JmsPoolConnectionFactory" init-method="start" destroy-method="stop">
      <property name="maxConnections" value="5" />
      <property name="connectionFactory" ref="jmsConnectionFactory" />
    </bean>

    <bean id="jmsConfig" class="org.apache.camel.component.jms.JmsConfiguration">
      <property name="connectionFactory" ref="jmsPooledConnectionFactory" />
      <property name="concurrentConsumers" value="5" />
    </bean>
    <!-- uses the  AMQP component -->
    <bean id="jms" class="org.apache.camel.component.amqp.AMQPComponent">
      <property name="configuration" ref="jmsConfig" />
    </bean>
    
     <!-- If we wanted to run this job on a scheduled basis we can use Quartz Scheduler 
          Below is the bean defintion, but inorder to create a bean we need camel-quartz2 dependency included -->
     <!--     <bean id="quartz2" class="org.apache.camel.component.quartz2.QuartzComponent" /> -->

	<camelContext id="XMLContentProcessing" xmlns="http://camel.apache.org/schema/spring" streamCache="true">
      
          <endpoint id="demoQueue" uri="jms:queue:content_queue" />
          
       <!--   Below is different only for example where we can us timer and file components --> 
        <!--
          <camel:route id="sendXml" autoStartup="true"> 
               <camel:from uri="timer://foo?fixedRate=true&amp;period=60000" /> 
              <camel:from uri="file://path/for/camel_input_file?delay=60000&amp;move=.processed&amp;moveFailed=.error" />
              <camel:to uri="direct:sendXMlTo"/>
          </camel:route>
        -->
       <camel:route id="contentProcessRote" autoStartup="true">
               
                <!-- If the proess needs quartz2 schdeuler below is an example of using it. -->
               <!-- 
                  <camel:from uri="quartz2://wsFTDTimer717?cron=0+0/5+*+*+*+?"/> 
              -->
              <camel:from uri="timer://foo?fixedRate=true&amp;period=60000"/> 
                <camel:to uri="http4://localhost:8080/content"/> <!--  we need to include the camel-http4 dependency -->
                                                                 <!-- For https,we can use SSLparameters within the context-->
                <camel:setHeader headerName="Endpoint1XMLContent">
	                <camel:constant>ENDPOINT1</camel:constant>
                </camel:setHeader>
                <camel:to uri="direct:sendXMLToRoute"/>
        </camel:route>

        <camel:route id="sendMessageToQueue" >
                <camel:from uri="direct:sendXMLToRoute"/>
                 <choice>
                	<when>
                    	<simple>${body.length} == 0</simple>
                       	<camel:log logName="com.rest.xml.log" loggingLevel="INFO" message="${header.location}, breadcrumbId = ${header.breadcrumbId} MESSAGE DROPPED. "/>
                        <stop></stop>
                    </when>
                    <otherwise>
                        <camel:log logName="com.rest.xml.log" loggingLevel="INFO" message="${header.location}, breadcrumbId = ${header.breadcrumbId} MESSAGE PROCESED. "/>
                        <camel:wireTap uri="file:///paht/to/location/for/message/backup"/>
                        <camel:setHeader headerName="MessageType">
     	                   <camel:constant>XML</camel:constant>
                        </camel:setHeader>
                        <camel:convertBodyTo type="java.lang.byte[]"></camel:convertBodyTo>
                        <camel:to uri="ref:demoQueue" />
                    </otherwise>
                </choice>
        </camel:route>
	</camelContext>
</beans>
```

- Camel project pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.demo.artemis</groupId>
	<artifactId>artemis-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<name>artemis-demo</name>
	<description>A simple artemis-demo.</description>
	<!-- FIXME change it to the project's website -->
	<url>http://www.example.com</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<maven.compiler.source>1.8</maven.compiler.source>
		<maven.compiler.target>1.8</maven.compiler.target>
		<apache.camel.version>2.23.0</apache.camel.version>
		<apache.artemis.version>2.12.0</apache.artemis.version>
		<pooled-jms-version>1.1.1</pooled-jms-version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.11</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-core</artifactId>
			<version>${apache.camel.version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-jms</artifactId>
			<version>${apache.camel.version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-spring</artifactId>
			<version>${apache.camel.version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-context</artifactId>
			<version>${apache.camel.version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>artemis-jms-client</artifactId>
			<version>${apache.artemis.version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-api</artifactId>
			<version>2.17.1</version>
		</dependency>
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-core</artifactId>
			<version>2.17.1</version>
		</dependency>

		<!-- SLF4J Bridge -->
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-slf4j-impl</artifactId>
			<version>2.17.1</version>
		</dependency>
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<version>1.7.33</version>
		</dependency>
		<dependency>
			<groupId>org.messaginghub</groupId>
			<artifactId>pooled-jms</artifactId>
			<version>${pooled-jms-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.qpid</groupId>
			<artifactId>qpid-jms-client</artifactId>
			<version>0.54.0</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-amqp</artifactId>
			<version>${apache.camel.version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-http4</artifactId>
			<version>${apache.camel.version}</version>
			<scope>runtime</scope>
			<!-- use the same version as your Camel core version -->
		</dependency>

	</dependencies>
  
	<build>
		<pluginManagement>
			<plugins>
				<plugin>
					<artifactId>maven-clean-plugin</artifactId>
					<version>3.1.0</version>
				</plugin>
				<plugin>
					<artifactId>maven-site-plugin</artifactId>
					<version>3.7.1</version>
				</plugin>
				<plugin>
					<artifactId>maven-project-info-reports-plugin</artifactId>
					<version>3.0.0</version>
				</plugin>
				<plugin>
					<artifactId>maven-resources-plugin</artifactId>
					<version>3.0.2</version>
				</plugin>
				<plugin>
					<artifactId>maven-compiler-plugin</artifactId>
					<version>3.8.0</version>
				</plugin>
				<plugin>
					<artifactId>maven-surefire-plugin</artifactId>
					<version>2.22.1</version>
				</plugin>
				<plugin>
					<artifactId>maven-jar-plugin</artifactId>
					<version>3.0.2</version>
				</plugin>
				<plugin>
					<artifactId>maven-install-plugin</artifactId>
					<version>2.5.2</version>
				</plugin>
				<plugin>
					<artifactId>maven-deploy-plugin</artifactId>
					<version>2.8.2</version>
				</plugin>
			</plugins>
		</pluginManagement>
	</build>
</project>

```
- LOG4J2 configuration to display in console
```properties

status = debug
name = PropertiesConfig
 
filters = threshold
 
filter.threshold.type = ThresholdFilter
filter.threshold.level = debug
 
appenders = console
 
appender.console.type = Console
appender.console.name = STDOUT
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
 
rootLogger.level = debug
rootLogger.appenderRefs = stdout
rootLogger.appenderRef.stdout.ref = STDOUT
```

![image](https://user-images.githubusercontent.com/6425536/151497916-fae32e15-c19a-43fe-9d10-1091907831a4.png)

![image](https://user-images.githubusercontent.com/6425536/151498093-164952f2-f862-4059-8aad-a03a8239d088.png)

Note:
I was using Artemis 2.14.0 version, to note the queue info was not displayed in the Queue tab. Only after having client.

Sample Aretmis java based jms client:
```java
package com.demo.artemis.clients;

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

//import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
//import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class ArtemisClient_broker2 
{


	   public static void main(final String[] args) throws Exception {
		   
		   //new ArtemisClient_broker2().runProducer(true, false);
		   new ArtemisClient_broker2().runProducer(false, true);
	   }
	   
	   public boolean runProducer(boolean produceMesage, boolean consumeMessage) throws Exception{

		      Connection connection = null;
		      InitialContext initalContext = null;
		         int i = 20000;
		         try {

		        	 Properties jndiProp = new Properties();

	        		 i=0;
		        	 jndiProp.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
		        	 jndiProp.put("connectionFactory.ConnectionFactory", "tcp://localhost:61616?producerMaxRate=50");
		        	
		        	 // for checking ampq ftd added below, else use the above commented one
		        	 jndiProp.put("queue.queue/content_queue","content_queue");

		        	 initalContext = new InitialContext(jndiProp);
		        	 // Step 2. Perfom a lookup on the queue
		        	 //Queue queue = ActiveMQJMSClient.createQueue("exampleQueue");
		        	 Queue queue = (Queue) initalContext.lookup("queue/content_queue");
				 
		        	 // Step 3. Perform a lookup on the Connection Factory
		        	 ConnectionFactory cf = (ConnectionFactory)initalContext.lookup("ConnectionFactory");

		        	 // Step 4. Create a JMS Connection
		        	 connection = cf.createConnection("admin","admin");

		        	 // Step 5. Create a JMS Session
		        	 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		        	 if(produceMesage) {
		        		 // Step 6. Create a JMS Message Producer
		        		 MessageProducer producer = session.createProducer(queue);

		        		 System.out.println("Will now send as many messages as we can in few seconds...");

		        		 // Step 7. Send as many messages as we can in 10 seconds

		        		 final long duration = 60000;


		        		 long start = System.currentTimeMillis();

		        		 while (System.currentTimeMillis() - start <= duration) {
		        			 TextMessage message = session.createTextMessage("This is text message BROKER 61617: " + i++);

		        			 Thread.sleep(1500);
		        			 producer.send(message);
		        		 }

		        		 long end = System.currentTimeMillis();

		        		 double rate = 1000 * (double) i / (end - start);

		        		 System.out.println("We sent " + i + " messages in " + (end - start) + " milliseconds");

		        		 System.out.println("Actual send rate was " + rate + " messages per second");

		        		 // Step 8. For good measure we consumer the messages we produced.
		        	 }

		        	 if(consumeMessage) {
		        		 MessageConsumer messageConsumer = session.createConsumer(queue);

		        		 connection.start();

		        		 System.out.println("Now consuming the messages...");

		        		 i = 0;
		        		 int count= 0;
		        		 while (true && count <= 100) {
		        			 BytesMessage messageReceived = (BytesMessage)messageConsumer.receive(5000);
		        			 System.out.println("inside consuming counting part");
		        			 if(messageReceived!=null)
		        			 System.out.println(messageReceived.getBodyLength());
		        			 if (messageReceived == null) {
		        				 break;
		        			 }

		        			 i++;
		        			 count++;
		        			 Thread.sleep(1500);
		        		 }

		        		 System.out.println("Received " + i + " messages");
		        	 }
		        	 return true;
		         } finally {
		         // Step 9. Be sure to close our resources!
		         if (connection != null) {
		            connection.close();
		         }
		      }
	   }
}

```
- Artemis broker.xml
```xml


      <connectors>
         <connector name="netty-connector">tcp://localhost:61616</connector>
         <connector name="broker2-connector"> tcp://localhost:61617</connector>
      </connectors>

     <!-- how often we are looking for how many bytes are being used on the disk in ms -->
      <disk-scan-period>5000</disk-scan-period>

      <!-- once the disk hits this limit the system will block, or close the connection in certain protocols
           that won't support flow control. -->
      <max-disk-usage>90</max-disk-usage>

      <!-- should the broker detect dead locks and other issues -->
      <critical-analyzer>true</critical-analyzer>

      <critical-analyzer-timeout>120000</critical-analyzer-timeout>

      <critical-analyzer-check-period>60000</critical-analyzer-check-period>

      <critical-analyzer-policy>HALT</critical-analyzer-policy>

      
      <page-sync-timeout>4076000</page-sync-timeout>
  <acceptors>
           <acceptor name="artemis">tcp://0.0.0.0:61616?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;amqpMinLargeMessageSize=102400;protocols=CORE,AMQP,STOMP,HORNETQ,MQTT,OPENWIRE;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpDuplicateDetection=true;supportAdvisory=false</acceptor>

            <!-- AMQP Acceptor.  Listens on default AMQP port for AMQP traffic.-->
            <acceptor name="amqp">tcp://0.0.0.0:5672?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpMinLargeMessageSize=102400;amqpDuplicateDetection=true</acceptor>

      </acceptors>
      <address-settings>
         <!-- if you define auto-create on certain queues, management has to be auto-create -->
         <address-setting match="activemq.management#">
            <dead-letter-address>DLQ</dead-letter-address>
            <expiry-address>ExpiryQueue</expiry-address>
            <redelivery-delay>0</redelivery-delay>
            <!-- with -1 only the global-max-size is in use for limiting -->
            <max-size-bytes>-1</max-size-bytes>
            <message-counter-history-day-limit>10</message-counter-history-day-limit>
            <address-full-policy>PAGE</address-full-policy>
            <auto-create-queues>true</auto-create-queues>
            <auto-create-addresses>true</auto-create-addresses>
            <auto-create-jms-queues>true</auto-create-jms-queues>
            <auto-create-jms-topics>true</auto-create-jms-topics>
         </address-setting>

...
```
