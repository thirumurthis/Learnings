## Spring Boot NullKeySerializer - Handle Kafka payload with null keys

In this article detailed how the default Spring Boot Kafka JSON Serializer can be extended with NullKeySerializer implementation to handle payload when message with map contains null keys. The default JSON Serializer throws exception when payload message to Kafka topic with null keys.

### Pre-requisites:
  - Kafka cluster running in 9092 port 

In Message Oriented Middleware (MOM) based distributed application, the payload messages communicate between system over network. There are scenarios where the payload may contain maps with null key. As mentioned above default Spring Boot JSON serializer will throw exception and exception message looks like below.

```
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.apache.kafka.common.errors.SerializationException: Can't serialize data [MapInputData(messageInfo={null=the key is null explicitly, 1=the key is one non-null}, date=Sun Mar 26 14:40:29 PDT 2023)] for topic [input-topic]] with root cause
```

## Brief steps on how to handle null key in the payload:

 - First  the extend the default spring JSON serializer class `org.springframework.kafka.support.serializer.JsonSerializer` and implement the NullKeySerializer.
  - Next, the extended custom serializer class should be configured in the spring kafka producer `spring.kafka.producer.value-serializer` property.

### Code implementation

- The complete spring custom serialzier code that implements NullKeySeralizer:
    - In this class the Jackson ObjectMapper is created and configured the implemented NullKeySerializer. The ObjectMapper is also configured to load additional modules, to handle the Date in the payload.
   - The ObjectMapper is passed to the Spring Boot default JsonSerializer.
  
```java
package com.example.kafka;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class CustomKafkaJsonSerializer extends JsonSerializer<Object> {

    public CustomKafkaJsonSerializer() {
        super(customizedObjectMapper());
    }

    private static ObjectMapper customizedObjectMapper() {
        ObjectMapper objMapper= new ObjectMapper();
        //if the pay load include timestamps we need to use modules
        objMapper.findAndRegisterModules();

        objMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objMapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());
        return objMapper;
    }
    // Null key serializer
    static class NullKeySerializer extends StdSerializer<Object> {
        public NullKeySerializer() {
            this(null);
        }

        public NullKeySerializer(Class<Object> t) {
            super(t);
        }

        @Override
        public void serialize(Object nullKey, JsonGenerator generator, SerializerProvider unused)
                throws IOException {
            generator.writeFieldName("null");
        }
    }
}
```

- Spring Boot application entry point

```java
package com.example.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class KafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);
	}
}
```

- Controller code, which creates the payload message mocking Map with null key.

```java
package com.example.kafka;


import com.example.kafka.model.MapInputData;
import com.example.kafka.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class SimpleController {

    ProducerService producerService;
    public SimpleController(ProducerService producerService){
        this.producerService=producerService;
    }
    @GetMapping("/send")
    public void sendMessage(){
        MapInputData mapInputData = new MapInputData();
        Map<String,Object> input = new HashMap<>();
        input.put(null,"the key is null explicitly");
        input.put("1","the key is one non-null");
        mapInputData.setDate(new Date());

        mapInputData.setMessageInfo(input);

        producerService.sendMessage(mapInputData);
    }
}
```

- Payload POJO definition

```java
package com.example.kafka.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import java.util.Date;
import java.util.Map;

@Data
@JsonTypeInfo(
         use = JsonTypeInfo.Id.NAME,
        property = "type")
public class MapInputData {

    Map<String, Object> messageInfo;
    Date date;
}
```

- Simple Producer service, that sends the message to Kafka cluster

```java
package com.example.kafka.service;

import java.text.MessageFormat;

import com.example.kafka.model.MapInputData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProducerService{

    @Value("${spring.kafka.topic:demo-topic}")
    String topicName;

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public ProducerService(KafkaTemplate<String,Object> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }
    public String sendMessage(MapInputData messageModel){
        log.info("Sending message from producer - {}",messageModel);
        Message message = constructMessage(messageModel);
        kafkaTemplate.send(message);
        return MessageFormat.format("Message Sent from Producer - {0}",message);
    }

    private Message constructMessage(MapInputData messageModel) {

        return MessageBuilder.withPayload(messageModel)
                .setHeader(KafkaHeaders.TOPIC,topicName)
                .setHeader("reason","for-Local-validation")
                .build();
    }
}
```

- Application configuration 

```yaml
spring:
  application:
    name: service-1
  kafka:
    topic: input-topic
    consumer:
      bootstrap-servers: localhost:9092
      group-id: input-group-id
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      # The custom null serializer added to configuration
      value-serializer: com.example.kafka.CustomKafkaJsonSerial
```

With the above spring configuration the producer can serialize the payload with null key and send message over wire to Kafka broker.

- pom.xml with dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.0.0</version>
		<relativePath/>
	</parent>
	<groupId>com.example</groupId>
	<artifactId>kafka</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>kafka</name>
	<description>kafka</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka</artifactId>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
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
			</plugin>
		</plugins>
	</build>
</project>
```
