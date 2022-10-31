### Kafka Exception handling in SpringBoot

- The handling is done via `@KafkaListener`

There are other operations to exclude certain excepton during message consumption like using the schema registry mostly supports Avro based schema handling.

- controller to send message to the kafka broker

```java

package com.kafka.example.kafkademo.code;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class InputController {

    @Autowired
    private ProducerService producerService;

    @Value("${test-start-stop-topic:demo-topic}")
    private String startStopTopicName;

    @GetMapping("/send/{message}")
    public void sendMessage(@PathVariable String message){
        producerService.sendMessage(startStopTopicName,message);
    }
}
```
- Controller to start and stop the consumer 
  - Note the topicId is the `id` value provided in the `@KafkaListener` in the consumer 
  
```java
package com.kafka.example.kafkademo.restart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KafkaContainerHandler {

    //Below bean is autowired, since the KafakListenerfactory
    //is used to create the consumer and we need to find the 
    //container based on the KafkaListener Id and either start or stop it.
    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @GetMapping("/start/{topicId}")
    public String startTopic(@PathVariable("topicId") String topicId){
        endpointRegistry.getListenerContainer(topicId).start();
        return "started kafka topic "+topicId;
    }

    @GetMapping("/stop/{topicId}")
    public String stopTopic(@PathVariable("topicId") String topicId){
        endpointRegistry.getListenerContainer(topicId).start();
        return "stopped kafka topic "+topicId;
    }
}
```

- Consumer configuration 
```java
package com.kafka.example.kafkademo.restart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class kafkaListener {

    //the id value is used to start and stop the consumers
    @KafkaListener(topics="${test-start-stop-topic}",
                   groupId="id-1",
                   errorHandler = "errorHandler",
                   id="KID-01")
    public void consumer(String msg, 
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){

        //below message will be printed for every message consumed
        log.info("topic-name : {}, payLoad : {}",topicName,msg);
        
        //below is used to simulate exception. 
        // when we pass the message as one below will throw and exception
        int x = Integer.parseInt(msg);
        
        log.info("consumed the message -  {}",x);
    }
}
```
- 
package com.kafka.example.kafkademo.restart;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerLevelErrorHandler implements ConsumerAwareListenerErrorHandler {

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException e, Consumer<?, ?> consumer) {
        log.error("Exception occurred message {} ",message.getPayload().toString());
        log.error("message info on exception .. {}",message);
        log.error("listener execution message: {}",e.getMessage());
        
        //Explicitly stopping the consumer container in case
        //of exception. by passing the id from kafkaListener
        endpointRegistry.getListenerContainer("KID-01").stop();

        return null;
    }
}
```
- Register the above error handler so spring can identify it

```java
package com.kafka.example.kafkademo.restart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorHandlerConfig {

    @Bean("errorHandler")
    public  ConsumerLevelErrorHandler consumerLevelErrorHandler(){
        return new ConsumerLevelErrorHandler();
    }
}
```
- Application.properties value

```
#start-stop topic demo based on exception
test-start-stop-topic=demo-topic

spring.kafka.consumer.bootstrap-servers= localhost:9092
spring.kafka.consumer.group-id= consume-test-id
spring.kafka.consumer.auto-offset-reset= earliest
spring.kafka.consumer.key-deserializer= org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer= org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.producer.bootstrap-servers= localhost:9092
spring.kafka.producer.key-serializer= org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer= org.apache.kafka.common.serialization.StringSerializer
```
- Main springboot application entry point

```java
package com.kafka.example.kafkademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(KafkaDemoApplication.class, args);
	}
}
```

### Output
 - Validating the exception 
 - below command will work fine 
 ```
 curl http://localhost:8080/api/send/1
 ```
 
 - below command will throw exception
   - The log message also indicates the consumer stopped consuming message after exception.
   
```
curl http://localhost:8080/api/send/one

//output in the console will be displaying the message 
 
2022-10-30 20:17:16.213  INFO 5532 --- [nio-8080-exec-5] c.k.e.kafkademo.code.ProducerService     : Sending message from producer - one
2022-10-30 20:17:16.230  INFO 5532 --- [   KID-01-0-C-1] c.k.e.kafkademo.restart.kafkaListener    : topic-name : demo-topic, payLoad : one
2022-10-30 20:17:16.232 ERROR 5532 --- [   KID-01-0-C-1] c.k.e.k.r.ConsumerLevelErrorHandler      : Exception occurred message one 
2022-10-30 20:17:16.232 ERROR 5532 --- [   KID-01-0-C-1] c.k.e.k.r.ConsumerLevelErrorHandler      : message info on exception .. GenericMessage [payload=one, headers={kafka_offset=2, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@1f8a8285, kafka_timestampType=CREATE_TIME, kafka_receivedPartitionId=0, kafka_receivedTopic=demo-topic, kafka_receivedTimestamp=1667186236213, kafka_groupId=id-1}]
2022-10-30 20:17:16.232 ERROR 5532 --- [   KID-01-0-C-1] c.k.e.k.r.ConsumerLevelErrorHandler      : listener execution message: Listener method 'public void com.kafka.example.kafkademo.restart.kafkaListener.consumer(java.lang.String,java.lang.String)' threw exception; nested exception is java.lang.NumberFormatException: For input string: "one"
2022-10-30 20:17:26.259  INFO 5532 --- [   KID-01-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-id-1-1, groupId=id-1] Revoke previously assigned partitions demo-topic-0
2022-10-30 20:17:26.259  INFO 5532 --- [   KID-01-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : id-1: partitions revoked: [demo-topic-0]
2022-10-30 20:17:26.259  INFO 5532 --- [   KID-01-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-id-1-1, groupId=id-1] Member consumer-id-1-1-18e5a9e6-a7e6-4594-9f46-a1d195c938bb sending LeaveGroup request to coordinator thirumurthi-HP:9092 (id: 2147483647 rack: null) due to the consumer unsubscribed from all topics
2022-10-30 20:17:26.261  INFO 5532 --- [   KID-01-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-id-1-1, groupId=id-1] Resetting generation due to: consumer pro-actively leaving the group
```
 
 - To start the consumer group use below command 
```
curl http://localhost:8080/api/start/KID-01
```
