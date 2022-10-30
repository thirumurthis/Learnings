## Using Awaitality in integration test Kafka Broker

In this blog will briefly explain how to can use `Awaitility` library to test Kafka in integration test. 

### Why use Awaitility dependency

- Awaitility library can be used for testing external services like Kafka, RabbitMQ, etc. mostly this can be used when application handling asynchronized calls and wait for responses.

- Awaitility has different method to support application built in asynchronized way, for more details about Awaitlity refer the [documentation](https://github.com/awaitility/awaitility)

- In below have created a simple SpringBoot application with a Producer to send message to Kafka Broker and Consumer to consumes the message. The Listener in the Consumer, will wait for message s to be received from the topic.

- The Consumer has to wait till the message is received and this mostly happens in asynchronous application. The application has to wait for a response from external service which might be immediate or delayed after few seconds in such case we can use Awaitility library to perform integration test. 

- Note, there are cases where we can mock the external service like in this case we use embedded Kafka broker, this might not be always possible since some projects might have dedicated environment. In such scenarios we can use Awaitlity library.

- For integration testing in this example have used the embedded Kafka Broker to test the  ConsumerService.

### Code

- Create SpringBoot application with `lombok` and `kafka` dependency form `start.spring.io` or IDE.

#### Required maven dependencies

- Include `awaitility` dependency in `pom.xml`

```xml
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <scope>test</scope>
    </dependency>
```
- Kafka testing dependency should already be included if not add it to pom.xml

```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
```

#### ProducerService Code that sends message to broker 

- Simple producer code, where the Kafka broker configuration are defined in `application.properties`, SpringBoot will use it to create the KafkaTemplate

```java
package com.kafka.example.kafkademo.code;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProducerService{

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    public String sendMessage(String topic, String message){
        log.info("Sending message from producer - {}",message);
        kafkaTemplate.send(topic,message);
        return MessageFormat.format("Message Sent from Producer - {0}",message);
    }
}
```

#### ConsumerService Code that consumes message from broker 

- Simple Consumer code which will use the `@KafakListener` configuration defined in the `application.properties`.

```java
package com.kafka.example.kafkademo.code;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerService {

    @Getter
    @Setter
    private String payload;

    @KafkaListener(topics="${test.app.topic}")
    public void consumeMessageAppTopic(ConsumerRecord<?,?> consumerRecord){
        log.info("payload consuming {}",consumerRecord.toString());
        payload = consumerRecord.value().toString();
    }
}
```

#### Kafka broker configuration details 

- `application.properties` file with the Kafka broker configuration
  - Using 9094 as Kafka port instead of default 9092.

```
# topic name
test.app.topic=test-topic

# Consumer configuration
spring.kafka.consumer.bootstrap-servers= localhost:9094
spring.kafka.consumer.group-id= consume-test-id
spring.kafka.consumer.auto-offset-reset= earliest
spring.kafka.consumer.key-deserializer= org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer= org.apache.kafka.common.serialization.StringDeserializer

# Producer configuration
spring.kafka.producer.bootstrap-servers= localhost:9094
spring.kafka.producer.key-serializer= org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer= org.apache.kafka.common.serialization.StringSerializer
```

#### Integration test case code using Awaitality library

  - Below code should be placed in the test folder in the SpringBoot project structure

```java
package com.kafka.example.kafkademo.code;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@DirtiesContext
/*
properties to the EmbeddedKafka broker
partitions – # of partitions to be used per topic, default 2.
brokerProperties – Kafka broker configuration, using plain text listener.
*/
@EmbeddedKafka(partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9094", "port=9094" })
class ConsumerServiceTest {

    @Autowired
    private ConsumerService consumer;

    @Autowired
    private ProducerService producer;

    @Value("${test.app.topic}")
    private String topic;

    @Test
    void testUsingAwaitility_messageConsumed(){
        String expectedData = "message data";

        producer.sendMessage(topic,expectedData);
        
        /*
          Awaitality which will wait for 15 seconds to receive the 
          message, if message is received before 15 seconds
          the test case will be passed and assertion gets validated
          
          until() requires a callable to return the Boolean here,
          where the payLoad is obtained
        */

        Awaitility.await().atMost(Duration.ofSeconds(15))
                .until(()->Objects.nonNull(consumer.getPayload()));
    }

    @Test
    void testUsingAwaitility_messageEquality(){
        String expectedData = "message data";

        producer.sendMessage(topic,expectedData);

        /*
          The Awaitality until() method callable to validate the 
          expected message with the consumed message
        */
        Awaitility.await().atMost(Duration.ofSeconds(15))
                .until(()->Objects.equals(expectedData,consumer.getPayload()));
    }
}
```

### Alternate approach to test Kafka

#### Integration test case using CountDownLatch

##### ConsumerService code changes to use CountDownLatch

- Alternatively we can use `CountDownLatch` to wait till the consumer listener recieves the message. Only the ConsumerService code will change in this case, the ProducerService remains the same.

- The consumer code looks like below when using CountDownLatch 

```java
package com.kafka.example.kafkademo.code;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Slf4j
public class ConsumerService {

    @Getter
    @Setter
    private String payload;

    @Getter
    @Setter
    private CountDownLatch latch = new CountDownLatch(1);
    
    @KafkaListener(topics="${test.app.topic}")
    public void consumeMessageAppTopic(ConsumerRecord<?,?> consumerRecord){
    
        log.info("payload consuming {}",consumerRecord.toString());
        payload = consumerRecord.value().toString();
        
        //once the message is consumed we call the countdown latch to 
        // decrement the count
        
        latch.countDown();
    }

    public void resetLatch(){
        latch = new CountDownLatch(1);
    }
}
```
##### Test case code using CountDownLatch
 
- The `consumer.getLatch().await()` in test case will wait the thread till  the message is consumed by the ConsumerService class consumerMessageAppTopic() method, since this method invokes `countDown()` after the message is received from the Listener releasing the thread to proceed further.

```java
package com.kafka.example.kafkademo.code;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9094", "port=9094" })
class ConsumerServiceTest {

    @Autowired
    private ConsumerService consumer;

    @Autowired
    private ProducerService producer;

    @Value("${test.app.topic}")
    private String topic;

    @Test
    public void testConsumeMessage_countDownLatch() throws Exception {
    
        String expectedData = "message data";
        producer.sendMessage(topic,expectedData);

        // CountDownLatch await() method will wait for 10 seconds or till the
        // ConsumerService consumeMessageAppTopic() invokes countDown() method.
        
        boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
        
        assertTrue(messageConsumed);        
        assertTrue(expectedData.equals(consumer.getPayload()));
    }
}
```

### Output

- Running the test cases should succeed

![image](https://user-images.githubusercontent.com/6425536/198904572-db399126-69db-4aa0-bbde-b461e1e81553.png)
