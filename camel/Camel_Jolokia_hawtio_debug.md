#### How to debug Camel routes, works only when the routes are defiend in spring xml dsl

Step 1:
 - Start the camel process with java `jolokia` agent. (in Eclipse use it in vm arguments)
 - This jar will set up an url locally that can be connected using hawtio 
 
 ```
   -javaagent:"C:\thiru\learn\hawtio_usage_demo\jolokia-jvm-1.6.2-agent.jar" -Dhawtio.proxyAllowlist=localhost,127.0.0.1
 ```
 
 ![image](https://user-images.githubusercontent.com/6425536/102028243-109ee100-3d5e-11eb-90cb-203b75d2ee59.png)

Step 2:

 - Download the hawtio jar and start the service locally
 ```
  java -jar hawtio-app-x.y.z.jar
 ```
 
 ![image](https://user-images.githubusercontent.com/6425536/102028302-5491e600-3d5e-11eb-8a9d-c73f71247221.png)

Step 3:
  - once the hawtio is up, issue `http://localhost:8080/hawtio` to open up the web module.
  - add connection and since the java application is already started with jolokia, use the instance `http://127.0.0.1:8778/jolokia`

![image](https://user-images.githubusercontent.com/6425536/110230449-e88ace00-7ec5-11eb-9baa-cb80d29c1c00.png)

![image](https://user-images.githubusercontent.com/6425536/110230431-c42ef180-7ec5-11eb-9252-30aedfce13d0.png)

![image](https://user-images.githubusercontent.com/6425536/102028381-c2d6a880-3d5e-11eb-909a-0ec9dce17ef7.png)

Route representation.

![image](https://user-images.githubusercontent.com/6425536/102035498-5f0baa00-3d75-11eb-9484-1e5103388023.png)

Note: By default the JMX is disabled in camel 3.0, so add below depedency
```xml
<dependency>
  <groupId>org.apache.camel</groupId>
  <artifactId>camel-management</artifactId>
  <version>${camel.version}</version>
</dependency>
```

 - Below Spring code works
 ```java
 package com.learning.camel.examples.prog1;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
public class SimpleTransformation {
    public static void main(String[] args) throws Exception {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(
                "application-Context.xml");
        CamelContext camelContext = SpringCamelContext.springCamelContext(
                appContext, false);
        try {
            camelContext.start();          
            ProducerTemplate template = new DefaultProducerTemplate(camelContext);
            template.start();
            for(int i=0;i<=10000;i++) {
            template.sendBody("direct:start", "Hello");
            Thread.sleep(2000);
            }
        } finally {
            camelContext.stop();
        }
    }
}
```
 - application-context.xml placed in the resource classpath
 ```xml
<?xml version="1.0" encoding="UTF-8"?>
 
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd
       ">
    <camelContext xmlns="http://camel.apache.org/schema/spring">
        <route>
            <from uri="direct:start" />
            <transform><simple>${body}</simple></transform>
            <to uri="stream:out"/>
        </route>
    </camelContext>
</beans>
```
 - pom.xml
   - along with the camel core dependency add below xml.bind since java 11 doesn't include the modules
   - camel-stream and jolokia dependencies are used.
```xml
<properties>
   <javax.activation.version>1.2.0</javax.activation.version>
   <jaxb.api.version>2.3.0</jaxb.api.version>
</properties>
 ...
     <dependency>
            <groupId>com.sun.activation</groupId>
            <artifactId>javax.activation</artifactId>
            <version>${javax.activation.version}</version>
        </dependency>
        
        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
            <version>${jaxb.api.version}</version>
        </dependency>
 
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-core</artifactId>
            <version>${jaxb.api.version}</version>
        </dependency>
 
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
            <version>${jaxb.api.version}</version>
        </dependency>

	<dependency>
		<groupId>org.apache.camel</groupId>
		<artifactId>camel-stream</artifactId>
		<version>${camel-version}</version>
	</dependency>
	<dependency>
		<groupId>org.jolokia</groupId>
		<artifactId>jolokia-agent-jvm</artifactId>
		<version>2.0.0-M3</version>
	</dependency>

```
--------------- 
##### Below is code reference for understanding, doesn't work in hawtio since uses java routes, but the metadata are displayed.
--------------
 - (Below Java DSL based route doesn't work with the representation in hawtio) Camel sample Code which actually sets a counter and routes based on random number using choice

```java
package com.learning.camel.sendMsg;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;

public class SendMsgToEndPoint {

public static void main(String[] args) throws Exception {
	
	CamelContext context = new DefaultCamelContext();

	try {
		context.addRoutes(new RouteBuilder() {
		@Override
		public void configure() throws Exception {
			//from("timer:SendOut?repeatCount=5000&delay=2000")//
			from("direct:input")
			.setHeader("count",method(CountIt.class)) // sets a counter and adds to the header of the message
			.setBody(simple("${headers.count}"))
			.process(new RandomNumberGenerator())
			.choice().when().simple("${body} >= 5").setBody(simple("greater than 5 - ${body}")).to("stream:out")
			.otherwise().setBody(simple("less than 5 - ${body}")).to("stream:out");
		}
	});
	context.start();
	ProducerTemplate template = new DefaultProducerTemplate(context);
	template.start();
	for(int i=0;i<=100000; i++) {
		template.sendBody("direct:input", i);
		  Thread.sleep(5000);
	}
	Thread.sleep(600*1000);
	}finally {
		context.stop();
	}
}
}


package com.learning.camel.sendMsg;
public class CountIt {
    private int counter = 0;
    public int count() {
        return counter++;
    }
}

package com.learning.camel.sendMsg;
import java.util.Random;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class RandomNumberGenerator implements Processor{
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Random rand = new Random();
		int random = rand.nextInt(9);
		exchange.getIn().setBody(random);
	}
}
```

Note: `Jolokia` can also started with additional module, like jdk-attach and java.xml.bind part of java 9+. Since the java.xml.bind is different module now.
```
# download the necessary jar file
wget https://repo1.maven.org/maven2/jakarta/xml/bind/jakarta.xml.bind-api/2.3.3/jakarta.xml.bind-api-2.3.3.jar
wget https://repo1.maven.org/maven2/jakarta/activation/jakarta.activation-api/1.2.2/jakarta.activation-api-1.2.2.jar
java --module-path /path/of/jar --add-modules jdk.attach,java.xml.bind hawtio-app-.x.y.z.jar
```

Reference Link: 
[hawtio](https://hawt.io/docs/get-started/)

pom.xml used
```xml

<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.camel.demo</groupId>
	<artifactId>CamelProj1</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>CamelProj1</name>
	<url>http://maven.apache.org</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<activemq-version>5.15.3</activemq-version>
		<camel-version>2.15.6</camel-version>
		<cxf-version>3.0.6</cxf-version>
		<hawtio-version>1.4.63</hawtio-version>
		<hsqldb-version>1.8.0.10</hsqldb-version>
		<junit-version>4.11</junit-version>
		<log4j-version>1.2.17</log4j-version>
		<spring-version>4.1.7.RELEASE</spring-version>
		<slf4j-version>1.7.25</slf4j-version>
		<xbean-version>3.18</xbean-version>
		<bundle-plugin-version>2.3.7</bundle-plugin-version>
		<jetty-plugin-version>8.1.16.v20140903</jetty-plugin-version>
		<scala-version>2.11.5</scala-version>
		<scala-plugin-version>3.1.6</scala-plugin-version>
		<!-- use utf-8 encoding -->
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
	</properties>

	<dependencies>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>3.8.1</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-core</artifactId>
			<version>${camel-version}</version>
		</dependency>
		<dependency>
			<groupId>javax.xml.bind</groupId>
			<artifactId>jaxb-api</artifactId>
			<version>2.2.11</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-stream</artifactId>
			<version>${camel-version}</version>
			</dependency>
		<dependency>
			<groupId>org.jolokia</groupId>
			<artifactId>jolokia-agent-jvm</artifactId>
			<version>2.0.0-M3</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-jms</artifactId>
			<version>${camel-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-camel</artifactId>
			<version>${activemq-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-test</artifactId>
			<version>${camel-version}</version>
		</dependency>
               <dependency>
		    <groupId>org.apache.camel</groupId>
		    <artifactId>camel-stream</artifactId>
		    <version>2.15.1</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-core</artifactId>
			<version>5.6.0</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-camel</artifactId>
			<version>${activemq-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-client</artifactId>
			<version>${activemq-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-broker</artifactId>
			<version>${activemq-version}</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-api -->
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<version>${slf4j-version}</version>
		</dependency>
		<dependency>
		    <groupId>org.slf4j</groupId>
		    <artifactId>slf4j-log4j12</artifactId>
		    <version>${slf4j-version}</version>
		</dependency>
		<dependency>
			<groupId>io.javalin</groupId>
			<artifactId>javalin</artifactId>
			<version>2.5.0</version>
		</dependency>
	</dependencies>
...
```
