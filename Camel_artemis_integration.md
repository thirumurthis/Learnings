##### Sample Camel producer to send message to ActiveMq Artemis using Camel 3.3+ version

```java
package com.camelArtemis;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ClientProgram {

	public static void main(String[] args) {
		ProducerTemplate template = null;
		ApplicationContext appContext =null;
		SpringCamelContext camelContext = null;
		try {
			appContext = new ClassPathXmlApplicationContext("META-INF/spring/camel-producer1.xml");
			camelContext = (SpringCamelContext) appContext.getBean("camel");

     		camelContext.start();
			// To build Exchange in case if exchage is needed.	
      // ExchangeBuilder exchange = new ExchangeBuilder(camelContext);
	  	//	template=  exchange.build().getContext().createProducerTemplate();
      
      //producer template
				template = camelContext.createProducerTemplate();
             
			int i=0;
			String message ="";
			while (true) {
				message=""+i;
				template.sendBody("direct:toMyQueue", message);
				if (i==15000) {
						break;
				}
				i++;
			}
		}catch (Exception exe) {
			System.out.println("Something wrong... ");
			exe.printStackTrace();
			if(template!=null) {
				template.stop();
			}
		}finally {
			System.out.println("finally invoked....");
			if(template !=null) {
				System.out.println("template stopped..");
				template.stop();
			}
			if(camelContext!=null)
				System.out.println("camel context stopped...");
      			camelContext.stop();
			
  	}
	}
}
```

##### Sample camelcontext to send message with HA master slave config
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:jee="http://www.springframework.org/schema/jee"
	xmlns:tx="http://www.springframework.org/schema/tx"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/aop 
        http://www.springframework.org/schema/aop/spring-aop-3.1.xsd
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-3.1.xsd
        http://www.springframework.org/schema/jee
        http://www.springframework.org/schema/jee/spring-jee-3.1.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx-3.1.xsd
        http://camel.apache.org/schema/spring
        http://camel.apache.org/schema/spring/camel-spring.xsd">
        
	<bean id="jmsConnectionFactory"
		class="org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory">
		<constructor-arg index="0"
			value="(tcp://172.28.128.28:61616,tcp://172.28.128.100:61616)?ha=true;reconnectAttempts=-1;transferExchange=true;" />
	</bean>

	<bean id="jmsPooledConnectionFactory"
		class="org.messaginghub.pooled.jms.JmsPoolConnectionFactory"
		init-method="start" destroy-method="stop">
		<property name="maxConnections" value="10" />
		<property name="connectionFactory" ref="jmsConnectionFactory" />
	</bean>
  
	<!-- If needed to cache the connection factory this can be used.
  <bean id="cachingConnectionFactory" class="org.springframework.jms.connection.CachingConnectionFactory"> 
		<property name="reconnectOnException" value="true"/> 
    <property name="sessionCacheSize" value="20"/> 
    <property name="connectionFactory" ref="jmsConnectionFactory" /> 
    <property name="exceptionListener" ref="exceptionHandler"/> 
  </bean> 
    -->
	<bean id="exceptionHandler"
		class="com.camelArtemis.CustomExceptionHandler" />
	<bean id="jmsConfig"
		class="org.apache.camel.component.jms.JmsConfiguration">
		<property name="connectionFactory" ref="jmsPooledConnectionFactory" /> <!-- pooled or cachng connection factory pass it along -->
		<property name="concurrentConsumers" value="5" />
	</bean>

	<bean id="jms" class="org.apache.camel.component.jms.JmsComponent">
		<property name="configuration" ref="jmsConfig" />
	  <property name="streamMessageTypeEnabled" value="true" />
	</bean>

	<bean id="deadLetterErrorHandler"
		class="org.apache.camel.builder.DeadLetterChannelBuilder">
		<property name="deadLetterUri" value="log:dead" />  <!-- RedeliveryPolicy can be configered in this case too push the message -->
	</bean>

<!-- <bean id="customRedeliveryPlcyConfig" class="org.apache.camel.processor.RedeliveryPolicy"> 
		<property name="maximumRedeliveries" value="5"/> 
    <property name="redeiveryddelay"  value="6000"/> 
   </bean> -->
   
   <!--camel context value as per 3+ doc, only one camel context per spring application context -->
	<camelContext id="camel" xmlns="http://camel.apache.org/schema/spring"  autoStartup="true">
  
		<endpoint id="myqueue" uri="jms:queue:myExampleQueue" />

    <route errorHandlerRef="deadLetterErrorHandler">
			<from uri="direct:toMyQueue" />
			<transform>
				<simple>MSG FRM DIRECT TO MyExampleQueue : ${bodyAs(String)}
				</simple>
			</transform>
			<to uri="ref:myqueue" />
		</route>
	</camelContext>

</beans>
```

##### Log4j2 properties
```properties 
status = error
name = Config
 
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

##### Maven pom.xml:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>camelArtemis</groupId>
	<artifactId>camelArtemis</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>camelArtemis</name>
	<url>http://maven.apache.org</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<maven.compiler.source>1.11</maven.compiler.source>
		<maven.compiler.target>1.11</maven.compiler.target>
	</properties>

<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.apache.camel</groupId>
				<artifactId>camel-bom</artifactId>
				<version>3.3.0</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.11</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-core</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-jms</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-main</artifactId>
		</dependency>
		<dependency>
			<groupId>org.messaginghub</groupId>
			<artifactId>pooled-jms</artifactId>
			<version>1.1.1</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-spring</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>artemis-jms-client</artifactId>
			<version>2.13.0</version>
		</dependency>
    
			<!-- Included for cachingconnectionfactory
        <dependency> 
          <groupId>org.springframework</groupId> 
           <artifactId>spring-jms</artifactId> 
			     <version>5.1.9.RELEASE</version> 
        </dependency> -->

		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-slf4j-impl</artifactId>
			<version>2.13.3</version>
		</dependency>
			<dependency>
			    <groupId>org.slf4j</groupId>
			    <artifactId>slf4j-api</artifactId>
			    <version>1.7.30</version>
			</dependency>
      
      <!-- for log4j binding -->
			<dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.11.2</version>
    </dependency>

    <!-- log4j2 support -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.11.2</version>
    </dependency>
		
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-simple</artifactId>
			<version>1.7.30</version>
		</dependency>

	</dependencies>
</project>

```
