### Apache Camel, Apache Proton Qpid, Apache ArtemisMq usage in EIP pattern.

 - Basics of Apache Camel is required to fully appreciate the use of it in EIP pattern. This blog is developed using Apache Camel 3.17.0 version.
   - Understanding of Camel Components, Routes, Exchange, etc. is required for this blog.
   - I have used File, AMPQ, JAXB, Marshall, Zip, Unmarshall, etc components here.

- Below code will demonstrate the Publish Consumer use Case as dipicted in the below image.

![image](https://user-images.githubusercontent.com/6425536/174726571-5ee625d7-ab61-4453-8c73-b31391aec90c.png)


- Publisher will read a xml file from folder/directory, compress and ship the message to Apache Artemis queue.
- Consumer will read the message from the queue, uncompress, unmarshal it and prints the message in the console.

- The project was created in Intellij Idea (Community Edition) as a maven project by choosing `Camel Java Archetype`. This generates the skeleton of the project.

Camel supports Java DSL, XML DSL and YAML DSL (Camel community recently started this support). 
In this demonstration I will be using Spring XML DSL for definting routes and components

Camel 3+ is modularized and in thsi case we will be using `camel-spring-main` and `camel-spring`, so the context file will be read by the Main.java of spring main package.
For this we need to create the spring context xml under `resources/META-INF/spring/*.xml`. Refer my [stack-overflow question](https://stackoverflow.com/questions/72655050/apache-camel-to-use-the-classic-xml-configuration-directly-in-camel-main-to-run).


To start with setup the Apache AretmisMQ broker in localhost, below route configuration uses AMPQ protocol.
Note: AretmisCloud.io, provide a docker image for Artemis I was able to setup and connect, but the Hawtio console is not accessible since the `jolokia-access.xml` is not editable.
Refer my [stack-overflow question](https://stackoverflow.com/questions/72672565/activemq-artemis-not-displaying-the-web-console-when-run-in-docker)

#### Publisher Code

- Camel route XML DSL defined in spring within spring context, this can be defined as java DSL as well (demonstrating this using XML DSL)

- The File idempotent bean, is used NOT to process the same file when provided as input. For example, if the filenamed input.xml is sent multiple times in the `data/input` directory this will not send the message to Queue.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:camel="http://camel.apache.org/schema/spring"
       xsi:schemaLocation="http://www.springframework.org/schema/util
        http://www.springframework.org/schema/util/spring-util.xsd
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://camel.apache.org/schema/spring
        http://camel.apache.org/schema/spring/camel-spring.xsd">

    <util:map id="test" key-type="String" value-type="Object"/>

    <bean id="fileIdempotentRef" class="java.io.File">
        <constructor-arg value="input/.simpleFileIdRepo"/>
    </bean>
    <bean id="testIdempotentRepo" class="org.apache.camel.support.processor.idempotent.FileIdempotentRepository">
        <constructor-arg ref="fileIdempotentRef"/>
        <constructor-arg ref="test"/>
    </bean>

   <bean id="jmsConnectionFactory" class="org.apache.qpid.jms.JmsConnectionFactory" >
        <property name="username" value="admin"/>
        <property name="password" value="secret"/>
       <property name="remoteURI" value="amqp://localhost:5672" /> 
       <!-- broker.xml of artmies should enable the amqp port-->
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
       <property name="connectionFactory" ref="jmsPooledConnectionFactory"/>
    </bean>

    <!-- <bean id="myRepo" class="org.apache.camel.processor.idempotent.MemoryIdempotentRepository"/> -->
    <camelContext id="testConsumer" xmlns="http://camel.apache.org/schema/spring" autoStartup="true"
                  allowUseOriginalMessage="false">

        <!-- We can use below if incase we are going to use direct scheme of camel endpoint
         <camel:endpoint id="directEndpoint" uri="direct:toMyQueue"/>
         -->
         
        <camel:endpoint id="inputFilePath" uri="file:data/input/"/>
        <camel:endpoint id="demoQueue" uri="jms:queue:content_queue" />

        <camel:route id="testproducer1" autoStartup="true">
            <camel:from uri="ref:inputFilePath"/>
            <camel:idempotentConsumer skipDuplicate="true" idempotentRepository="testIdempotentRepo">
                <camel:simple>${file:name}-${file:modified}</camel:simple>
                <camel:setHeader name="messageType">
                    <camel:constant>ORDER</camel:constant>
                </camel:setHeader>
                <camel:log logName="test.demo.log" loggingLevel="INFO" message="processing ${header.messageType}"/>
                <camel:marshal>
                    <camel:zipFile maxDecompressedSize="9"/>
                </camel:marshal>
                <camel:wireTap uri="file:data/input/archive/"/>
                <!-- We can use below for debugging so the output will be created is output
                   <camel:to uri="file:data/output/"/>
                -->
                <camel:to uri="ref:demoQueue"/>
            </camel:idempotentConsumer>
        </camel:route>
    </camelContext>

</beans>
```
- In case we need to fetch form the external SFTP system, we can use 

- MainApp.java - uses camel-spring-main for reading the Spring context from specific default location `resources/META-INF/spring/*.xml`

```java
package org.example;

//Package uses camel spring main
import org.apache.camel.spring.Main;

public class MainApp {

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.run(args);
    }
}
```

- pom.xml for the producer project.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>SimpleCamelDemo</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>

  <name>A Camel Route</name>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <log4j2-version>2.13.3</log4j2-version>
    <pooled-jms-version>2.0.5</pooled-jms-version> <!-- 3.0.0 uses jakarta version jms 2.0, but camel-ampq is not ready-->
    <qpid-jms-client-version>1.6.0</qpid-jms-client-version> <!-- 2.0.0 uses jakarata.xml which is no ready in Camel -->
  </properties>

  <dependencyManagement>
    <dependencies>
      <!-- Camel BOM -->
      <dependency>
        <groupId>org.apache.camel</groupId>
        <artifactId>camel-bom</artifactId>
        <version>3.17.0</version>
        <scope>import</scope>
        <type>pom</type>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-main</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-spring-main</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-spring</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-jms</artifactId>
    </dependency>
      <dependency>
          <groupId>org.apache.camel</groupId>
          <artifactId>camel-spring-xml</artifactId>
      </dependency>
      <dependency>
          <groupId>org.messaginghub</groupId>
          <artifactId>pooled-jms</artifactId>
          <version>${pooled-jms-version}</version> <!-- 3.0.0. use jakarta.xml new jms 2.0 version -->
      </dependency>
      <dependency>
          <groupId>org.apache.qpid</groupId>
          <artifactId>qpid-jms-client</artifactId>
          <version>${qpid-jms-client-version}</version>
      </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-xstream</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-amqp</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-zipfile</artifactId>
    </dependency>
    <!-- logging -->
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-slf4j-impl</artifactId>
      <scope>runtime</scope>
      <version>${log4j2-version}</version>
    </dependency>

    <!-- testing -->
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <defaultGoal>install</defaultGoal>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.10.1</version>
        <configuration>
          <release>11</release>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
          <encoding>UTF-8</encoding>
        </configuration>
      </plugin>

      <!-- Allows the example to be run via 'mvn camel:run' -->
      <plugin>
        <groupId>org.apache.camel</groupId>
        <artifactId>camel-maven-plugin</artifactId>
        <version>3.17.0</version>
        <configuration>
          <logClasspath>true</logClasspath>
          <mainClass>org.example.MainApp</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

#### Input xml structure
- Sample Xml input text file `simpleOrder.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<message id="12">
   <to>ORDER_DEP</to>
   <from>customernumber01</from>
   <status>APPROVED</status>
   <description>validated</description>
</message>
```

#### Consumer

- Camel spring context definition with the routing info
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://camel.apache.org/schema/spring
           http://camel.apache.org/schema/spring/camel-spring.xsd">

    <bean id="jmsConnectionFactory" class="org.apache.qpid.jms.JmsConnectionFactory" >
        <property name="username" value="admin"/>
        <property name="password" value="admin"/>
        <property name="remoteURI" value="amqp://localhost:5672" /> 
                 <!-- broker.xml of artmies should enable the amqp port-->
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
        <property name="connectionFactory" ref="jmsPooledConnectionFactory"/>
    </bean>

    <bean id="demoBean" class="org.example.Demo"/>
    <bean id="objectFactory" class="org.example.dataformatof.ObjectFactory"/>

    <camelContext id="testConsumer" xmlns="http://camel.apache.org/schema/spring" autoStartup="true" allowUseOriginalMessage="true">

        <route id="testroute" autoStartup="true">
            <from uri="jms:queue:content_queue?selector=messageType LIKE 'ORDER%'"/>
            <log loggingLevel="INFO" message="info ${header.fileName}"/>
            <unmarshal>
                <zipFile/>
            </unmarshal>
            <choice>
                <when>
                    <simple>${header.messageType} == 'ORDER' </simple>
                    <wireTap uri="file:data/output/consumed/"/> <!-- ${HOME} in file uses HOME env variable -->
                    <to uri="seda:processOrder"/>
                </when>
            </choice>
        </route>
        <route id="processFile" autoStartup="true">
            <from uri="seda:processOrder"/>
            
            <!-- Below can be used for debugging to see if the input text file is received at this path
             <to uri="file:data/output/processed"/>
             -->

            <unmarshal allowNullBody="true">
                <jaxb ignoreJAXBElement="true"  prettyPrint="true"  contextPath="org.example.dataformatof"
                      filterNonXmlChars="true"/>
            </unmarshal>
            <bean ref="org.example.Demo" method="processOrder"/>
        </route>
    </camelContext>
</beans>
```

- MainSpringContextApp.java which contains the main to run this context file

```java
package org.example;

import org.apache.camel.spring.Main;
public class MainSpringContextApp {

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.run(args);
    }
}
```

- Demo.java, to print the consumed and unmarshalled xml file.
- 
```java 
package org.example;
import org.example.dataformatof.MessageType;

public class Demo {
    public void processOrder(MessageType message){
        System.out.println("message value "+message.getId()+" "+message.getFrom());
    }
}
```

- pom.xml
  - The pom.xml on the consumer code, is a separate project. 
  - In which I added the `cfx-xjc` maven plugin configuration to generate the JAXB classes used by Camel for unmarshalling explained below.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>SimpleCamelConsumer</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>

  <name>A Camel Route</name>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <log4j2-version>2.13.3</log4j2-version>
    <pooled-jms-version>2.0.5</pooled-jms-version> <!-- 3.0.0 used now jakarta version jms 2.0 camel ampq is not ready-->
    <qpid-jms-client-version>1.6.0</qpid-jms-client-version> <!-- 2.0.0 used jakarata.xml which is no ready in Camel -->
    <startClass>org.example.MainSpringContextApp</startClass>
  </properties>

  <dependencyManagement>
    <dependencies>
      <!-- Camel BOM -->
      <dependency>
        <groupId>org.apache.camel</groupId>
        <artifactId>camel-bom</artifactId>
        <version>3.17.0</version>
        <scope>import</scope>
        <type>pom</type>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-main</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-spring-main</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-spring</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-jms</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-spring-xml</artifactId>
    </dependency>
    <dependency>
      <groupId>org.messaginghub</groupId>
      <artifactId>pooled-jms</artifactId>
      <version>${pooled-jms-version}</version> <!-- 3.0.0. use jakarta.xml new jms 2.0 version -->
    </dependency>
    <dependency>
      <groupId>org.apache.qpid</groupId>
      <artifactId>qpid-jms-client</artifactId>
      <version>${qpid-jms-client-version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-xstream</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-jaxb</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-amqp</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-zipfile</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-slf4j-impl</artifactId>
      <scope>runtime</scope>
      <version>${log4j2-version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.camel</groupId>
      <artifactId>camel-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <defaultGoal>install</defaultGoal>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.10.1</version>
        <configuration>
          <release>11</release>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
          <encoding>UTF-8</encoding>
        </configuration>
      </plugin>

      <!-- Allows the example to be run via 'mvn camel:run' -->
      <plugin>
        <groupId>org.apache.camel</groupId>
        <artifactId>camel-maven-plugin</artifactId>
        <version>3.17.0</version>
        <configuration>
          <logClasspath>true</logClasspath>
          <mainClass>org.example.MainSpringContextApp</mainClass>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.cxf</groupId>
        <artifactId>cxf-xjc-plugin</artifactId>
        <version>3.2.3</version>
        <configuration>
          <extensions>
            <extension>org.apache.cxf.xjcplugins:cxf-xjc-dv:3.2.3</extension>
          </extensions>
        </configuration>
        <executions>
          <execution>
            <id>generate-sources</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>xsdtojava</goal>
            </goals>
            <configuration>
              <sourceRoot>${basedir}/target/generated/src/main/java</sourceRoot>
              <xsdOptions>
                <xsdOption>
                  <xsd>${basedir}/schema/simpleOrder.xsd</xsd>
                </xsdOption>
              </xsdOptions>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

#### Output
 - I created the publisher and consumer as separate project, once both the project are started in IDE.
 - Use the simpleOrder.xml (input.xml) file, place it under Publisher project `data/input` directory
 - The consumer should print the info read from the input xml like below in the `message value 12 customernumber01`

```
[                          main] MainSupport                    INFO  Apache Camel (Main) 3.17.0 is starting
[                          main] JmsPoolConnectionFactory       INFO  Provided ConnectionFactory implementation is JMS 2.0+ capable.
[                          main] AbstractCamelContext           INFO  Apache Camel 3.17.0 (testConsumer) is starting
[r :(1):[amqp://localhost:5672]] JmsConnection                  INFO  Connection ID:27b151bb-f0cc-4ea8-b973-e148fddc4063:1 connected to server: amqp://localhost:5672
[                          main] AbstractCamelContext           INFO  Routes startup (total:2 started:2)
[                          main] AbstractCamelContext           INFO      Started testroute (jms://queue:content_queue)
[                          main] AbstractCamelContext           INFO      Started processFile (seda://processOrder)
[                          main] AbstractCamelContext           INFO  Apache Camel 3.17.0 (testConsumer) started in 8s870ms (build:281ms init:1s434ms start:7s155ms)
message value 12 customernumber01
```

##### Generate `jaxb` classes using `cxf-xjc` maven plugin.

 - To autmatically **unmarshalling** the xml we need to create JAXB classes, below are the steps I followed:
 
 1. we need to create the schem file for the input xml, for that I used the Intellij Idea Community edition Tools -> Generate Schema for Xml. Then renamed the xs:schema to xsd:schema (the `cfx-xjc` maven plugin requires the schema to be starting with xsd)
 
 2. Add the `cfx-xjc` maven plugin changes to the pom.xml (refer the above pom.xml of consumer section)
 
 3. Place the xsd, in this case the simpleOrder.xsd under the project base dir `schema/simpleOrder.xsd`
 
   - simpleOreder.xsd content 

 ```xml
 <?xml version="1.0" encoding="UTF-8"?>
<xsd:schema attributeFormDefault="unqualified" elementFormDefault="qualified" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <xsd:element name="message" type="messageType"/>
  <xsd:complexType name="messageType">
    <xsd:sequence>
      <xsd:element type="xsd:string" name="to"/>
      <xsd:element type="xsd:string" name="from"/>
      <xsd:element type="xsd:string" name="status"/>
      <xsd:element type="xsd:string" name="description"/>
    </xsd:sequence>
    <xsd:attribute type="xsd:string" name="id"/>
  </xsd:complexType>
</xsd:schema>
 ```
 4. Once maven project is update, issue `mvn:install` to generate java source
    - This will fail in some cases, just check if the source file generated. When I tried, it threw compilation error but the source file got generated under `target/generated/src/main/java`
 
 5. Copy paste the generated `MessageType.java` and `ObjectFactory.java` under `org.example.dataformatof.` package.
 
 6. Renamed the package to use `javax.xml`, since the xjc generates `jakarata.xml`.

Refer to my [Stack-overflow question link](https://stackoverflow.com/questions/72681775/camel-jaxb-not-able-to-unmarshall-the-xml-when-defined-using-spring-xml-dsl) 

 -  MesageType.java file content, removed commented code

```java
package org.example.dataformatof;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "messageType", propOrder = {
    "to",
    "from",
    "status",
    "description"
})
public class MessageType {

    @XmlElement(required = true)
    protected String to;
    @XmlElement(required = true)
    protected String from;
    @XmlElement(required = true)
    protected String status;
    @XmlElement(required = true)
    protected String description;
    @XmlAttribute(name = "id")
    protected String id;

    public String getTo() {
        return to;
    }
    public void setTo(String value) {
        this.to = value;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String value) {
        this.from = value;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String value) {
        this.status = value;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String value) {
        this.description = value;
    }
    public String getId() {
        return id;
    }
    public void setId(String value) {
        this.id = value;
    }
}
```
- `ObjectFactory.java` file content

```java
package org.example.dataformatof;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    private final static QName _Message_QNAME = new QName("", "message");

    public ObjectFactory() {
    }
    public MessageType createMessageType() {
        return new MessageType();
    }
    
    @XmlElementDecl(namespace = "", name = "message")
    public JAXBElement<MessageType> createMessage(MessageType value) {
        return new JAXBElement<MessageType>(_Message_QNAME, MessageType.class, null, value);
    }
}
```

In case if we need to fetch the file from SFTP linux based operating system we can use below endpoint within the camel context, and enable the endpoint to start based on the cron set using the quartz2 camel component.

```xml
<!--Below can be used to connect to SFTP if we need to fetch the info from there. -->
<!--
    <endpoint id="inputSFTP" uri="sftp://SFTP_SERVER_COM">
        <property key="username" value="<username"/>
        <property key="privateKetFile" value="path/to/.ssh/id_rsa"/>
        <property key="knownHostFile" value="/path/to/.ssh/know_hosts"/>
        <property key="include" value="input_file"/>
        <property key="scheduler" value="quartz2"/> <!-- inculde the quartz2 component -->
        <property key="scheduler.cron" value="*+*+1/3+?+*+*"/>
    </endpoint>
-->
```
