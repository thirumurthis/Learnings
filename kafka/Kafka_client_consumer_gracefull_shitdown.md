- Simple Apache kafka client which can use ggracefull shutdown with the shutdown hook.

```java
package com.kafka.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Below code uses Apacke Kafka client to connect to the local cluster
 * Note:- best practise to create the topic ahead of time, though the producer can create the topic automatically
 * 
 * consume the message 
 */

public class KafkaDemoConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaDemo.class.getSimpleName());
    
    public static void main(String ... args){

        log.info("Cosumer is about to start...");
        //To Create a producer - we need to follow below steps

        //1. Create configs using  properties for consumer
        // 
        final String bootstrapServerUrl = "localhost:9092"; 
        final String groupId = "first-consumer-groupId";
        final String topic = "first-demo-topic";

        Properties configs = new Properties();
        // we can use the string bootstrap.servers which passed in the CLI
        // Below Apache kafka client provides the configuration we are using the producerconfig
        // to set the urls and other config to avoid typos
        configs.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
        // we need to set the key Deserializer in the properties
        configs.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // We need to set the value Deserializer in the properties
        configs.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // we need to provide a consumer group id
        configs.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // we need to set the offset config, since we need to tell the consumer from where to
        // start reading form the topic
        configs.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest"); //none|earliest|latest
        // none = if no previous offset is found don't start
        // earliest = read from the very beginning of the topic
        // latest = read from the latest offset or now when the consumer was connected to the topic

        //2. create Consumer

        // create kafka producer and set the generics as string as key and value in this case is string
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);

                //We need to add graceful shutdown the consumer
        // Get the handle of the main thread

        final Thread mainThread = Thread.currentThread();

        //Add shutdown hook thread to the main thread
        Runtime.getRuntime().addShutdownHook(new Thread(){

            public void run(){
                log.warn("recevied shutdown signal, executing the wakeup()");
                consumer.wakeup(); // When the wakeup() method is invoked the poll() method will throw exception
                                   // the poll() method will throw wakeup exception, when invoked.
                // we need to wait for the main thread

                try{
                    mainThread.join();
                }catch(InterruptedException exe){
                    exe.printStackTrace();
                }
            }

        });

      
        // 3. consumer messages

        try{
            // subscribe to the topic to consumer
            //consumer.subscribe(Collections.singletonList(topic)); //This is to subscribe to single topic
            consumer.subscribe(Arrays.asList(topic)); // To subscribe to mulitple topic pass a list of topcis

            //To continously poll for the data - we will have infinite loop

            while(true){
                // the poll takes a timeout duration, 
                // poll - means poll kafka broker to consumer as many message as possible
                // if there are no message wait till the timeout duration in this case 100ms 
                // if there are no message even after 100ms then execute the next line of code
                // if ther are not records not available after timeout then returns empty collection
                ConsumerRecords<String, String> consumedRecords = consumer.poll(Duration.ofMillis(100));

                for(ConsumerRecord<String, String> record: consumedRecords){
                    log.info("key: "+ record.key() + " value: "+ record.value());
                    log.info("Partition:- "+record.partition()+
                            "\n Offset:- "+record.offset()+
                            "\n timestamp:- "+record.timestamp());
                }
            }
        }catch(WakeupException exception){
            log.warn("Wakeup exception occurred - ignore this ");
        }catch(Exception e){
            log.error("Unexpected exception - close this now");
        }finally{
            consumer.close();
            log.info("consumer closed gracefully..");
        }
    }
}

```
