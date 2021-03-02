### Code to test Artemis embedded version code.
 - check the [documentation on configuring embedded Artemis 1.0.0](https://activemq.apache.org/components/artemis/documentation/1.0.0/embedding-activemq.html).
 - check the latest [Artemis embedded changes](https://activemq.apache.org/components/artemis/documentation/latest/embedding-activemq.html)
 -  Github camel repo, test example [github link](https://github.com/apache/camel/blob/master/components/camel-amqp/src/test/java/org/apache/camel/component/amqp/artemis/AMQPEmbeddedBrokerTest.java)
 - Referreing [artemis unit test doc](https://activemq.apache.org/components/artemis/documentation/latest/unit-testing.html)
  
- Create a maven project, with the below code in the test folder.
- The dependency jars are placed in pom.xml

- Below example for testing topic and queue using Camel route.

```java
package com.test;

import static org.apache.camel.component.amqp.AMQPConnectionDetails.AMQP_PORT;
import static org.apache.camel.component.amqp.AMQPConnectionDetails.AMQP_SET_TOPIC_PREFIX;
import static org.apache.camel.component.amqp.AMQPConnectionDetails.discoverAMQP;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ArtemisEmbeddedTest extends CamelTestSupport{
	
	 static int amqpPort = AvailablePortFinder.getNextAvailable(); //This used beanutils package. we can use an unused port directly in the configuration

	    static EmbeddedActiveMQ server = new EmbeddedActiveMQ();

	    @EndpointInject("mock:result")
	    MockEndpoint resultEndpoint;

	    String expectedBody = "Hello there!";

	    @BeforeAll
	    public static void beforeClass() throws Exception {
	        Configuration config = new ConfigurationImpl();
	        AddressSettings addressSettings = new AddressSettings();
	        // Disable auto create address to make sure that topic name is correct without prefix
	        addressSettings.setAutoCreateAddresses(false);
	        config.addAcceptorConfiguration("amqp", "tcp://0.0.0.0:" + amqpPort
	                                                + "?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpMinCredits=300");
	        config.setPersistenceEnabled(false);
	        config.addAddressesSetting("#", addressSettings);
	        config.setSecurityEnabled(false);

	        // Set explicit topic name
	        CoreAddressConfiguration pingTopicConfig = new CoreAddressConfiguration();
	        pingTopicConfig.setName("topic.ping");
	        pingTopicConfig.addRoutingType(RoutingType.MULTICAST);
	        
          // included the address configuration via java for queue configuration.
	        CoreAddressConfiguration queueConfig = new CoreAddressConfiguration();
	        queueConfig.setName("queue.example");
	        queueConfig.addRoutingType(RoutingType.ANYCAST);

	        config.addAddressConfiguration(pingTopicConfig).addAddressConfiguration(queueConfig);

	        server.setConfiguration(config);
	        server.start();
	        System.setProperty(AMQP_PORT, amqpPort + "");
	        System.setProperty(AMQP_SET_TOPIC_PREFIX, "false");
	    }

	    @AfterAll
	    public static void afterClass() throws Exception {
	        server.stop();
	    }

	    @Test
	    public void testTopicWithoutPrefix() throws Exception {
	        resultEndpoint.expectedMessageCount(1);
	        resultEndpoint.message(0).body().contains("Hello there!");
	        template.sendBody("direct:send-topic", expectedBody);
	        //resultEndpoint.assertIsSatisfied();
	        assertMockEndpointsSatisfied();
	    }
	    
	    @Test
	    public void testQueue() throws Exception {
	        resultEndpoint.expectedMessageCount(1);
	        //resultEndpoint.message(0).body().contains("Hello there!");
	        template.sendBody("direct:send-queue", expectedBody);
	        Object reply = consumer.receiveBody("amqp-customized:queue:queue.example",10000);
	        //resultEndpoint.assertIsSatisfied();
	        //assertMockEndpointsSatisfied();
	        assertNotNull("Shouldn't be null",reply); //This will work only if the routebuilder didn't had any consumer
	        assertEquals("Hello there!", reply); // verifying the content sent.
	    }

      //override camelcontext
	    @Override
	    protected CamelContext createCamelContext() throws Exception {
	        CamelContext camelContext = super.createCamelContext();
	        camelContext.getRegistry().bind("amqpConnection", discoverAMQP(camelContext));
          //Camel way to add additional compoent, in this case we use the AMQPComponent.
	        camelContext.addComponent("amqp-customized", new AMQPComponent());
	        return camelContext;
	    }

	    @Override
	    protected RouteBuilder createRouteBuilder() {
	        return new RouteBuilder() {
	            public void configure() {
	                from("direct:send-topic")
	                        .to("amqp-customized:topic:topic.ping");

	                from("amqp-customized:topic:topic.ping")
	                        .to("log:routing")
	                        .to("mock:result");
	                
	                from("direct:send-queue")
                    .to("amqp-customized:queue:queue.example");
	                
                  //Comment out this part, so the message will stay in Queue and not be consumed.
                  // the test case to read the message and validate the message content can be achieved. 
                  //check the test case
                  
                  //if we uncomment this code, the test case above needs to be commented out since it might fail.
	                //from("amqp-customized:queue:queue.example")
                    //.to("mock:result");
	            }
	        };
	    }
}
```
- Dependency pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.learn.camel</groupId>
	<artifactId>camel-artemis</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<activemq-version>5.15.3</activemq-version>
		<camel-version>3.0.0</camel-version>
		<hawtio-version>1.4.63</hawtio-version>
		<junit-version>4.11</junit-version>
		<log4j-version>1.2.17</log4j-version>
		<slf4j-version>1.7.25</slf4j-version>
		<xbean-version>3.18</xbean-version>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<javax.activation.version>1.2.0</javax.activation.version>
		<jaxb.api.version>2.3.0</jaxb.api.version>
		<activemq-artemis-version>2.16.0</activemq-artemis-version>
		<qpid-jms-client-version>0.56.0</qpid-jms-client-version>
		<qpid-proton-j-version>0.33.8</qpid-proton-j-version>
		<netty-version>4.1.59.Final</netty-version>
		<commons-beanutils-version>1.9.4</commons-beanutils-version>
		<common-beanutils-core-version>1.8.3</common-beanutils-core-version>
		<acitvemq-core.version>5.6.0</acitvemq-core.version>

	</properties>
	<dependencies>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-core</artifactId>
			<version>${camel-version}</version>
		</dependency>
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
			<groupId>javax.enterprise</groupId>
			<artifactId>cdi-api</artifactId>
			<version>2.0</version>
			<!-- <scope>provided</scope> -->
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
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-xpath</artifactId>
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
			<!-- <scope>test</scope> -->
		</dependency>

		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-stream</artifactId>
			<version>${camel-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-test-spring</artifactId>
			<version>${camel-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-core</artifactId>
			<version>${acitvemq-core.version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-camel</artifactId>
			<version>${activemq-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-kahadb-store</artifactId>
			<version>${activemq-version}</version>
			<scope>test</scope>
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
			<groupId>commons-beanutils</groupId>
			<artifactId>commons-beanutils</artifactId>
			<version>${commons-beanutils-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-pool2</artifactId>
			<version>2.4.2</version>
		</dependency>
		<dependency>
			<groupId>commons-beanutils</groupId>
			<artifactId>commons-beanutils-core</artifactId>
			<version>${common-beanutils-core-version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-configuration2</artifactId>
			<version>2.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-amqp</artifactId>
			<version>${camel-version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-broker</artifactId>
			<version>${activemq-version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>artemis-server</artifactId>
			<version>${activemq-artemis-version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>artemis-amqp-protocol</artifactId>
			<version>${activemq-artemis-version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-amqp</artifactId>
			<version>${activemq-version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-test-junit5</artifactId>
			<version>${camel-version}</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>artemis-junit</artifactId>
			<!-- replace this for the version you are using -->
			<version>2.5.0</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.geronimo.specs</groupId>
			<artifactId>geronimo-jms_1.1_spec</artifactId>
			<version>1.0</version>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.qpid</groupId>
			<artifactId>qpid-jms-client</artifactId>
			<version>${qpid-jms-client-version}</version>
		</dependency>
		<dependency>
			<groupId>org.apache.qpid</groupId>
			<artifactId>proton-j</artifactId>
			<version>${qpid-proton-j-version}</version>
		</dependency>
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-buffer</artifactId>
			<version>${netty-version}</version>
		</dependency>
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-common</artifactId>
			<version>${netty-version}</version>
		</dependency>
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-handler</artifactId>
			<version>${netty-version}</version>
		</dependency>
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-transport</artifactId>
			<version>${netty-version}</version>
		</dependency>
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-codec-http</artifactId>
			<version>${netty-version}</version>
		</dependency>
	</dependencies>
	<build>
		<pluginManagement>
			<plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-compiler-plugin</artifactId>
					<version>2.5</version>
					<configuration>
						<source>1.11</source>
						<target>1.11</target>
						<maxmem>256M</maxmem>
						<showDeprecation>true</showDeprecation>
					</configuration>
				</plugin>
			</plugins>
		</pluginManagement>
	</build>
</project>
```
