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
- Now in the browser if we use `https://localhost:8080/content` the xml content should be rendered.

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
  
 <!-- use below when using Qpid client -->
   <bean id="jmsConnectionFactory" class="org.apache.qpid.jms.JmsConnectionFactory">
    <property name="remoteURI" value="amqp://localhost:5672" /> <!-- broker.xml of artmies should enable the amqp port-->
    <!--  <property name="remoteURI" value="tcp://localhost:61616" /> -->
  </bean>

  <!-- below bean configuration is used when we include aretmis jms client dependecies since this uses JMS client -->
  <!--
    <bean id="jmsConnectionFactory" class="org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory">
         <constructor-arg index="0" value="tcp://localhost:61616?wireFormat.maxInactivityDuration=500000"/>
    </bean>
   -->
  

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
               <!--  <camel:from uri="direct:sendXMlTo"/> -->
                <!-- <camel:to uri="file:/home/tsanthanakrishnan@DAA.LOCAL/tim/camel_for_ftd"/> -->
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

