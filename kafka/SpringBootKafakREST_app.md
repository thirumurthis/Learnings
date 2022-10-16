Setting up a Kafka and connecting using Spring boot

Pre-requsites: 
  - Basic understanding of how the Kafka works.
  - Install the Apache Kafka cluster and running.

I am using a Windows 10 machine and I downloaded the `Scala 2.13-3.3.1.tar.gz` binary from [Apache Kafka Website](https://kafka.apache.org/downloads)

Note:
  - At the time of writing this blog the version is `Scala 2.13-3.3.1`.
  - 3.3.1 per Release notes KIP-833 KRaft is marked Production Ready.
  - In Windows I was not able to successfully start the Kafak in KRaft mode, WSL2 ubuntu distro worked.
  
Brief overview of Kafka, 
  - Kafka is a distributed streaming platform very popular in desinging event based systems.
  - Kafka relies on Apache Zookeeper heavily for coordination, leader election of brokers, topic configuration.
  - For past few years Kafka community is trying to move away from Zookeeper, and working on KRaft (KIP500) a consensus protocol which makes use of the new quorum controller service. Refer [documentation](https://developer.confluent.io/learn/kraft/) for more details.
  
Info:
  - The extracted tar file has the windows bat files to run the Zookeeper and Kafak cluster. 
  
Spring boot code

- Create a springboot application with `Spring web`, `Lombok` and `Spring Kafak message` dependencies.

- Below code will create a spring boot REST application 
   - Has a Kafka producer service (which produces a message passed via REST endpoint using Kafka template)
   - Has a Kafak Consumer service (which uses `@kafkaListener`) to consumer message from the specified topic. (note, I have used Spring Expression languae to pass the values form application.properties)
   
- Once Application is up from Git Bash use curl command to send message

```
curl -X POST http://localhost:8080/api/message -H "Content-Type: application/text" -d "hello1"
```

- application.properties
```

spring.kafka.topic=input-topic
spring.kafka.consumer.bootstrap-servers: localhost:9092
spring.kafka.consumer.group-id: input-group-id
spring.kafka.consumer.auto-offset-reset: earliest
spring.kafka.consumer.key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.producer.bootstrap-servers: localhost:9092
spring.kafka.producer.key-serializer: org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

- Controller class
```java
package com.kafka.example.kafkademo.code;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class InputController {

    @Autowired
    private ProducerService producerService;

    @PostMapping("/message")
    public void handleMessage(@RequestBody String message){
        producerService.sendMessage(message);
    }
}
```

- Producer service 

```java
package com.kafka.example.kafkademo.code;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProducerService{


    @Autowired
    KafkaConstants kafakConstants;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    public String sendMessage(String message){
        log.info("Sending message from producer - {}",message);
        kafkaTemplate.send(kafakConstants.getTopicName(),message);
        return MessageFormat.format("Message Sent from Producer - {0}",message);
    }
}
```

- Consumer class
```java
package com.kafka.example.kafkademo.code;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

//Have annotated with Component not as Service.
@Component
@Slf4j
public class ConsumerService {
    
    // using Spring Expression language to read the topic from app.properties
    @KafkaListener(topics={"#{'${spring.kafka.topic}'}"},groupId="#{'${spring.kafka.consumer.group-id}'}")
    public void consumeMessage(String message){
        log.info("Consumed message - {}",message);
    }
}
```
- Constant class, this is not needed, if are use the `@Value` annotation in the Producer service for the input topic name.

```java
package com.kafka.example.kafkademo.code;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class KafkaConstants {

    @Value("${spring.kafka.topic}")
    @Getter
    @Setter
    private String topicName;

    @Value("${spring.kafka.consumer.group-id}")
    @Getter
    @Setter
    private String groupId;
}
```
