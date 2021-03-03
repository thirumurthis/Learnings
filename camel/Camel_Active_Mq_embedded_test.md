### Below is the test case on how to set up embedded ActiveMq broker
 - Make sure to have the appropriate dependencies
```java
package com.camel.demo.CamelProj2;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ActiveMQEmbedBrokerTest extends CamelTestSupport{

	@EndpointInject("mock:result")
	MockEndpoint resultEndpoint;

	protected ConnectionFactory connectionFactory;

	@Override
	protected CamelContext createCamelContext() throws Exception {
		CamelContext camelContext = super.createCamelContext();

		connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		camelContext.addComponent("activemq", jmsComponentClientAcknowledge(connectionFactory));

		return camelContext;
	}


	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from("direct:input")
				.to("activemq:queue:example");
        
        // comment this consumer section for one of the test case to validate the message content method testSendQueueMessage()
				from("activemq:queue:example")
				.to("mock:result");
			}
		};
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
    
    //for now the mock end point is fetched from the context.
		// since we have override the context, we need to fetch from the context
		// rather using MockEndpoint mock = getMockEndpoint(“mock:end”); in the test case
		resultEndpoint = (MockEndpoint) context.getEndpoint("mock:result");
	}

	@Ignore
	@Test
	public void testSendQueueMessage() throws Exception {
		String m = "TEST";
		//in order to execute this test case where to check the message within the 
		// queue, comment out the consumer (last from and to of the routebuilder configuration)
		template.sendBody("direct:input", m);
    
		// this should be invoked after the message is sent
		Object message = consumer.receiveBody("activemq:queue:example");
		assertEquals(m, message);
	}

	@Test
	public void testSendQueueMessage1() throws Exception {
		String m = "TEST";
		resultEndpoint.expectedBodiesReceived(m);
     // another option to send header
		//template.sendBodyAndHeader("direct:input", m,"Header test");
		template.sendBody("direct:input",m);
		resultEndpoint.assertIsSatisfied();
		//assertMockEndpointsSatisfied();
	}
}
```
 - Validating Header of the message
```java
package com.camel.demo.CamelProj2;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.connection.JmsTransactionManager;
//import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;
import static org.apache.camel.component.jms.JmsComponent.jmsComponentClientAcknowledge;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ActiveMqEmbdBrokerTest extends CamelTestSupport {
    private static final String EXPECTED_BODY = "Hello there!"; 
    protected MockEndpoint resultEndpoint;
    protected String startEPUri = "activemq:queue:test.queue";

    @Test
    public void testJmsRouteWithTextMessage() throws Exception {
        resultEndpoint.expectedBodiesReceived(EXPECTED_BODY);
        resultEndpoint.message(0).header("testvalue").isEqualTo(123);
        sendExchange(EXPECTED_BODY);
        resultEndpoint.assertIsSatisfied();
    }

    protected void sendExchange(final Object expectedBody) {
        template.sendBodyAndHeader(startEPUri, expectedBody, "testvalue", 123);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        resultEndpoint = (MockEndpoint) context.getEndpoint("mock:result");
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
    		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		    camelContext.addComponent("activemq", jmsComponentClientAcknowledge(connectionFactory));
        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
               //this part is validated using queue test
                from(startEPUri).to("activemq:queue:test.queue");
                from("activemq:queue:test.queue").to("mock:result");

                JmsEndpoint endpoint1 = (JmsEndpoint) endpoint("activemq:topic:quote.MSFT");
                endpoint1.getConfiguration().setTransactionManager(new JmsTransactionManager());
                endpoint1.getConfiguration().setTransacted(true);
                from(endpoint1).to("mock:transactedClient");

                JmsEndpoint endpoint2 = (JmsEndpoint) endpoint("activemq:topic:quote.MSFT");
                endpoint2.getConfiguration().setTransacted(false);
                from(endpoint2).to("mock:nonTrasnactedClient");
            }
        };
    }
}
```
  - dependencies xml
```xml
 <!-- camel-version = 3.0.0;
      activemq-version = 5.15.3 -->
	<dependency>
			<groupId>javax.xml.bind</groupId>
			<artifactId>jaxb-api</artifactId>
			<version>2.3.1</version>
		</dependency>
		<dependency>
			<groupId>com.sun.xml.bind</groupId>
			<artifactId>jaxb-core</artifactId>
			<version>2.3.0.1</version>
		</dependency>
		<dependency>
			<groupId>com.sun.xml.bind</groupId>
			<artifactId>jaxb-impl</artifactId>
			<version>2.3.2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-core</artifactId>
			<version>${camel-version}</version>
		</dependency>
   <dependency>
			<groupId>org.apache.activemq</groupId>
			<artifactId>activemq-core</artifactId>
			<version>5.6.0</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.apache.activemq/activemq-camel -->
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
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-test-junit5</artifactId>
			<version>${camel-version}</version>
			<scope>test</scope>
		</dependency>
```
