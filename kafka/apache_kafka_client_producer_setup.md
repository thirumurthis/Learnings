### Topic:
  - Kafka client dependencies
  - Kafka simple producer usage 
  - Kafka simple producer with `CallBack` usage

#### StickyParition from the producer
  - In the below kafka simple producer, if we use for loop to send message all the messages ends up in the same partition.
  - This is because the producer has the performance mechanism called stickyparition which when messages sent by producer with short window of time, all message ends up in the same parition, when the key is null in the producer record. 
  - If we introduce a delay `Thread.sleep()` in the loop, we would see the messages are sent to different partitions.
  - The configuration printed in the log will display the partition.class which lists the `DefaultParitioner`, the code itself a `StickyParitioner` in the code.

- The pom.xml that requires dependency
  - If we are setting up the multi-module maven project check the https://github.com/thirumurthis/learnings/maven/ path for instructions.
 
```xml
  <dependencies>
    <dependency>
      <groupId>org.apache.kafka</groupId>
      <artifactId>kafka-clients</artifactId>
      <version>3.2.1</version>
  </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.0</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>2.0.0</version>
  </dependency>
```

### Simple Kafka client 

- Note: 
   - When the WSL2 Kafka cluster is setup, the Windows might not be able to access the Kafka cluster running in WSL2
   - We need to identify the ip address and execute the command

```
> netsh interface portproxy add v4tov4 listenport=9092 listenaddress=0.0.0.0 connectport=9092 connectaddress=172.25.191.70
```

```java
package com.kafka.demo;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Below code uses Apacke Kafka client to connect to the local cluster
 * Note:- best practise to create the topic ahead of time, though the producer can create the topic automatically
 * 
 * Creating topic using below command, using kafka CLI (refer the github.com/thirumurthis/kafka/ notes for setting up cluster in WSL2)
 * `kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --topic first-demo-topic --partitions 3 --replication-factor 1`
 * 
 * Now: before running the below code lets create a console consumer using the CLI
 * ` kafka-console-consumer.sh --bootstrap.server 127.0.0.1:9092 --topic first-demo-topic` 
 *  - Above command will keep listening to the topic already
 * 
 * In order to access the Kafka cluster running in the WLS2 ubuntu distro
 *  - from ubuntu identify the ip address of the machine, use any of below command
 *  `ifconfig` or `ip addr` or `hostname -I`
 * 
 *  - from windows machine command prompt, use below command
 *  `netsh interface portproxy add v4tov4 listenport=9092 listenaddress=0.0.0.0 connectport=9092 connectaddress=172.25.191.70`
 *    - note: 172.25.191.70 was the IP address of my local WSL2 ubuntu distro
 * 
 * Now running the code, in the console will display the data "data-01"
 * 
 */

public class KafkaDemo {

    private static final Logger log = LoggerFactory.getLogger(KafkaDemo.class.getSimpleName());
    
    public static void main(String ... args){

        log.info("Producer is about to start...");
        //To Create a producer - we need to follow below steps

        //1. Create producer properties
        // 
        final String bootstrapServerUrl = "localhost:9092"; 
        Properties configs = new Properties();
        // we can use the string bootstrap.servers which passed in the CLI
        // Below Apache kafka client provides the configuration we are using the producerconfig
        // to set the urls and other config to avoid typos
        configs.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
        // we need to set the key serializer in the properties
        configs.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // We need to set the value serializer in the properties
        configs.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //2. create producer

        // create kafka producer and set the generics as string as key and value in this case is string
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        //3. Send data
        // to send data we need to create producer record.
        ProducerRecord<String,String> producerRecord = new ProducerRecord<String,String>("first-demo-topic", "data-01");

        // sending data 
        // below is the asynchoronus operation
        // this means that the below will send the record, the java will not wait till the step is complete
        // so we need to use the flush, similar to the Thread.join() method. wait for the thread to complete
        producer.send(producerRecord);

        //4. flush and close
        producer.flush(); // mostly this is done to make the java process wait till the data is sent to the brokers.
        producer.close();

    }
}
```

### Kafka client use Callback 
  - Below code is same as the above, the Producer.send() has a callback method which needs to be implemented to handle the onCompletion method.
  - use the RecordMetadata which has the response


```java
package com.kafka.demo;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Below code uses Apacke Kafka client to connect to the local cluster
 * Note:- best practise to create the topic ahead of time, though the producer can create the topic automatically
 * 
 * Creating topic using below command, using kafka CLI (refer the github.com/thirumurthis/kafka/ notes for setting up cluster in WSL2)
 * `kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --topic first-demo-topic --partitions 3 --replication-factor 1`
 * 
 * Now: before running the below code lets create a console consumer using the CLI
 * ` kafka-console-consumer.sh --bootstrap.server 127.0.0.1:9092 --topic first-demo-topic` 
 *  - Above command will keep listening to the topic already
 * 
 * In order to access the Kafka cluster running in the WLS2 ubuntu distro
 *  - from ubuntu identify the ip address of the machine, use any of below command
 *  `ifconfig` or `ip addr` or `hostname -I`
 * 
 *  - from windows machine command prompt, use below command
 *  `netsh interface portproxy add v4tov4 listenport=9092 listenaddress=0.0.0.0 connectport=9092 connectaddress=172.25.191.70`
 *    - note: 172.25.191.70 was the IP address of my local WSL2 ubuntu distro
 * 
 * Now running the code, in the console will display the data "data-01"
 * 
 */

public class KafkaDemowithCallBack {

    private static final Logger log = LoggerFactory.getLogger(KafkaDemo.class.getSimpleName());
    
    public static void main(String ... args){

        log.info("Producer with callback is about to start...");
        //To Create a producer - we need to follow below steps

        //1. Create producer properties
        // 
        final String bootstrapServerUrl = "localhost:9092"; 
        Properties configs = new Properties();
        // we can use the string bootstrap.servers which passed in the CLI
        // Below Apache kafka client provides the configuration we are using the producerconfig
        // to set the urls and other config to avoid typos
        configs.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
        // we need to set the key serializer in the properties
        configs.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // We need to set the value serializer in the properties
        configs.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //2. create producer

        // create kafka producer and set the generics as string as key and value in this case is string
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        //3. Send data
        // to send data we need to create producer record.
        ProducerRecord<String,String> producerRecord = new ProducerRecord<String,String>("first-demo-topic", "data-02");

        // sending data 
        // below is the asynchoronus operation
        // this means that the below will send the record, the java will not wait till the step is complete
        // so we need to use the flush, similar to the Thread.join() method. wait for the thread to complete
        producer.send(producerRecord,new Callback() {
            // This class is called as a callback
            @Override
            // this method is invoked when the message is successfully sent to broker
            // or exception is thrown 
            public void onCompletion(RecordMetadata metadata, Exception exception){

                //if exception is null then the message sent successfully
                if(exception == null){
                    log.info("Received new metadata "+
                    "\n topic:- "+metadata.topic()+
                    "\n Parition :- "+metadata.partition()+
                    "\n offset :- "+metadata.offset()+
                    "\n timestamp :- "+metadata.timestamp()
                );
                }else {
                    log.error("Exception occured during sedning message", exception);
                }

            }
        });

        //4. flush and close
        producer.flush(); // mostly this is done to make the java process wait till the data is sent to the brokers.
        producer.close();

    }
}
```
