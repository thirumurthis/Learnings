## Simple reactive application with SpringBoot, Apache Camel using Kafka Broker to stream the data.

- In this blog have demonstrated how to integrate Apache Camel with Apache Kafka and SpringBoot, by building a simple Reactive application.

- The Apache Camel components are used to generate random numbers every 2 seconds, this random number is sent to Kafka and consumed by different camel route finally connecting with SpringBoot Flux to stream the data via controller endpoint.

## About the application

- Apache camel route definition details are as follows
  - Route set 1:
      - from: timer component used to poll every 2 seconds in this case
      - processor (this will generate Random number between 0-500 and set in camel exchange)
      - to: direct endpoint (direct is specific to Apache Camel)
      
  - Route set 2:
      - from: direct endpoint 
      - to: kafka broker using the Camel kafka component

  - Route set 3:
    - This will be 
      - from: kafka broker consumes the message
      - to: send to direct endpoint 
  
  - Route set 4:
      - from: direct endpoint 
      - to: reactive-stream endpoint, named numbers

- To integrate the Apache Camel `reactive-stream` with the SpringBoot Flux, the publisher is retrieved from camel-context and subscribed using Flux. 

- With Camel [reactive-streams component](https://camel.apache.org/components/3.18.x/reactive-streams-component.html) it is easy to use Project Reactor or RxJava or other reactive framework. In this case Spring Flux is used to subscribe to the publisher.

### Code flow representation: 

![image](https://user-images.githubusercontent.com/6425536/201460847-9d270f86-c934-45a4-9955-dc65640874bd.png)

## Pre-requisites:

  - Kafka setup installed and running, accessible at `http://localhost:9092`
  - Basic understanding of Apache Camel

## Code details

### Dependencies 

- Create springboot project with Apache Camel and WebFlux dependencies, pom.xml details as follows
  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.5</version>
		<relativePath/>
	</parent>
	<groupId>com.camel.kafka</groupId>
	<artifactId>app</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>app</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webflux</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-spring-boot-starter</artifactId>
			<version>3.19.0</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-kafka-starter</artifactId>
			<version>3.19.0</version>
		</dependency>
		<dependency>
			<groupId>io.projectreactor</groupId>
			<artifactId>reactor-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-reactive-streams-starter</artifactId>
			<version>3.19.0</version>
		</dependency>
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-reactor-starter</artifactId>
			<version>3.19.0</version>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

### Routing configuration

- Below code shows the route configuration mentioned above, we define it by extending the `RouteBuilder` of Camel.

```java
package com.camel.kafka.app;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Component
public class AppCamelBasedProducerConsumer extends RouteBuilder {

    String kafkaProducerURI = "kafka:camel-demo?brokers=localhost:9092";
    String kafkaConsumerURI = kafkaProducerURI;

    @Override
    public void configure() throws Exception {
        //route set 1
        from("timer://demoapp?fixedRate=true&period=2000")
                .process(new RandomGenerationProcess())
                .to("direct:message");

        //route set 2
        from("direct:message")
                .setHeader(KafkaConstants.HEADERS, constant("FROM-CAMEL"))
                .to(kafkaProducerURI);

        //route set 3
        from(kafkaConsumerURI + "&groupId=app&autoOffsetReset=earliest&seekTo=BEGINNING")
                .log("message - ${body} from ${headers[kafka.TOPIC]}")
                .to("direct:outputStream");

        //route set 4
        from("direct:outputStream")
                .to("reactive-streams:numbers");
    }
}
```

### Camel Process configuration to generate random number

- Below is a implementation of Camel Processor which generates the random number.

- In Camel with processor we can transform the messages retrieved from one endpoint to another. 
    - For example, we can use file component to read the contents of the file from a directory and use processor to transform all into uppercase.
 
```java
package com.camel.kafka.app;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomGenerationProcess implements Processor {

    private Random random = new Random();
    @Override
    public void process(Exchange exchange) throws Exception {
        Integer rand = random.nextInt(500);

        //Set the random number to the Camel exchange in the body
        //This will be sent to the next endpoint
        exchange.getIn().setBody(rand);
    }
}
```

### Service layer to subscribe to the Camel Stream using Flux

- Below is the service layer where the Camel reactive-streams and the Spring Flux are chained.

```java
package com.camel.kafka.app;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class AppService{

    //Fetch the camel context from container
    @Autowired
    CamelContext camelContext;

    //Used to fetch the reactive stream publisher
    CamelReactiveStreamsService camel;

    public Flux<Integer> randomIntStream(){
        camel = CamelReactiveStreams.get(camelContext);
        Publisher<Integer> numbers = camel.fromStream("numbers", Integer.class);
        return Flux.from(numbers);
    }
}
```

### Controller code to create an event stream endpoint

- Below is a simple Controller, where we define the endpoint as a stream by defining a MediaType, so browsers can access the endpoint as stream of data

```java

package com.camel.kafka.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@Slf4j
public class AppController {

    @Autowired
    AppService appService;

    //Including the Media Type TEXT_EVENT_STREAM_VALUE enables browser 
    //to connect to the endpoint as event stream so data will be streamed continuously

    // accessing this endpoint with Chrome the data will be streamed
    // at this time when I tried with FireFox it downloads the stream as file
    
    @GetMapping(value="/stream",produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> getRandomIntegerStream(){
        log.info("invoked controller stream uri /stream");
          return appService.randomIntStream();
    }
}
```

## Output:

- Running the above code will throws exception message until an active subscriber is connected, in this case had to hit the `http://localhost:8080/api/stream` from a browser browser. The console output once connected using browser starts streaming the data to the subscriber.

```
2022-11-11 22:31:11.779  WARN 17004 --- [mer[camel-demo]] o.a.camel.component.kafka.KafkaConsumer  : Error during processing. Exchange[9D3C45E152C9A66-0000000000000437]. Caused by: [org.apache.camel.component.reactive.streams.ReactiveStreamsNoActiveSubscriptionsException - The stream has no active subscriptions]

org.apache.camel.component.reactive.streams.ReactiveStreamsNoActiveSubscriptionsException: The stream has no active subscriptions
	at org.apache.camel.component.reactive.streams.engine.CamelPublisher.publish(CamelPublisher.java:111) ~[camel-reactive-streams-3.19.0.jar:3.19.0]
	at org.apache.camel.component.reactive.streams.engine.DefaultCamelReactiveStreamsService.sendCamelExchange(DefaultCamelReactiveStreamsService.java:151) ~[camel-reactive-streams-3.19.0.jar:3.19.0]
	at org.apache.camel.component.reactive.streams.ReactiveStreamsProducer.process(ReactiveStreamsProducer.java:52) ~[camel-reactive-streams-3.19.0.jar:3.19.0]
	at org.apache.camel.processor.SendProcessor.process(SendProcessor.java:172) ~[camel-core-processor-3.19.0.jar:3.19.0]
	at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.run(RedeliveryErrorHandler.java:477) ~[camel-core-processor-3.19.0.jar:3.19.0]
	at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.schedule(DefaultReactiveExecutor.java:181) ~[camel-base-engine-3.19.0.jar:3.19.0]
	at org.apache.camel.impl.engine.DefaultReactiveExecutor.scheduleMain(DefaultReactiveExecutor.java:59) ~[camel-base-engine-3.19.0.jar:3.19.0]
	at org.apache.camel.processor.Pipeline.process(Pipeline.java:175) ~[camel-core-processor-3.19.0.jar:3.19.0]
	at org.apache.camel.impl.engine.CamelInternalProcessor.process(CamelInternalProcessor.java:392) ~[camel-base-engine-3.19.0.jar:3.19.0]
	at org.apache.camel.impl.engine.DefaultAsyncProcessorAwaitManager.process(DefaultAsyncProcessorAwaitManager.java:83) ~[camel-base-engine-3.19.0.jar:3.19.0]
	at org.apache.camel.support.AsyncProcessorSupport.process(AsyncProcessorSupport.java:41) ~[camel-support-3.19.0.jar:3.19.0]
	at org.apache.camel.component.kafka.consumer.support.KafkaRecordProcessor.processExchange(KafkaRecordProcessor.java:109) ~[camel-kafka-3.19.0.jar:3.19.0]
	at org.apache.camel.component.kafka.consumer.support.KafkaRecordProcessorFacade.processRecord(KafkaRecordProcessorFacade.java:124) ~[camel-kafka-3.19.0.jar:3.19.0]
	at org.apache.camel.component.kafka.consumer.support.KafkaRecordProcessorFacade.processPolledRecords(KafkaRecordProcessorFacade.java:77) ~[camel-kafka-3.19.0.jar:3.19.0]
	at org.apache.camel.component.kafka.KafkaFetchRecords.startPolling(KafkaFetchRecords.java:318) ~[camel-kafka-3.19.0.jar:3.19.0]
	at org.apache.camel.component.kafka.KafkaFetchRecords.run(KafkaFetchRecords.java:158) ~[camel-kafka-3.19.0.jar:3.19.0]
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539) ~[na:na]
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]
2022-11-11 22:31:12.708  INFO 17004 --- [ctor-http-nio-2] com.camel.kafka.app.AppController        : invoked controller stream uri /stream
2022-11-11 22:31:13.774  INFO 17004 --- [mer[camel-demo]] route3                                   : message - 442 from camel-demo
2022-11-11 22:31:15.783  INFO 17004 --- [mer[camel-demo]] route3                                   : message - 205 from camel-demo
2022-11-11 22:31:17.784  INFO 17004 --- [mer[camel-demo]] route3                                   : message - 53 from 
```

- From Chrome browser below is the output where the data will be streamed continously
 
![image](https://user-images.githubusercontent.com/6425536/201460960-9a407f96-b92a-45af-a948-9e81150fa6d1.png)
