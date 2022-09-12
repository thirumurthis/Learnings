### Apache kafka

- What is kafka
- Producer/consumer
- Topics - Parition, Offset
- Kafka CLI - installing binaries provides a list of CLI to start the cluster local
- Kafka SDK - Java or any language code.

#### Terminology used in Kafka:

**Topic :**
  - Analogus to table in the database world, it streams data of particular type
  - The **data** needs to written to the **Kafka broker** in the **Kafka cluster** it is written in a **partition** within the topic.
      - The data in the topic can't be changed latter (immutable), this is different from the relational database world.
     - By default the data is kept in the Broker for a limited period (default is 7 days, this is configurable)
  - There can be many number of parition within the Topic.
  
A **Kafka producer** writes the data to partition in the topic
     - The producer decides to which parition the data to be written in the topic
     - Kafka producer decides to which partition the data to be stored using key value.
         - The key value is not null, this key is used to determine the hash value to which the partition the data needs to be written. If the same key is associated with the key all the data will be writted to the same partition in the topic. Kafka used murmur2 hashing technique to determine which parition the data to be written.
         - If the key value is null, then the data is writted to the parition in round-robin fashion determined by the producer.

Kafka message structure created by the producer:
  - Below message is then sent to the Kafka broker
  - Note, this message are sent as bytes to the Kafka broker, so we need to serialize it.
  - There are different serializer (one for key and value) of the data itself.
     - Say if the key is Integer, then we can use IntegerSerializer
     - Say if the value object is String, then we can use String Serialzier
  
```
       _________________________________________________
      |Key (binary value)    |   Value - binary         |
      |(holds null)          |   (holds null)           |
      |-------------------------------------------------|
      | Compression Type (gzip, snappy, none, lz4, etc  |
      |-------------------------------------------------|
      | Headers (optional)[Key1: value1, key2: value2]  |
      |-------------------------------------------------|
      |  Parition + Offset                              |
      |-------------------------------------------------|
      | Timestamp of system or user set                 |
      |_________________________________________________|      
```

### Producer Acknowledgement (acks):
  - Producer can be configured to receive acknowledgement from leader broker of data writes
    - acks=0 => producer won't wait for acknowledgement after writing (possible data loss)
    - acks=1 => producer will wait for acknowledgement(limited data loss)
    - acks=all => produce will wait till the data is written in leader broker and replicas as well (no data loss)
 Above is more like, data consistency. Where in Comso DB we have different consistency level like eventual consistency, Strong consistency, Session Consistency, etc. Same like, eventual consistency in Cassandra. Refer CAP theorem.

Note:- Refer the topic replication for leader broker details, below.

Kafka **consumer** is used to read the data from partition in the topic
 - The consumer reads the data from offser low to higher value
 - We need to de-serialize the data from the partition, so the consumer needs to know in advance what is the format of the messages being read.
   - There are IntegerDeSerializer, StringDeSerializer, JsonDeserlizer, etc.
 
 When we need to scale the kafka, we can use consumer groupe.
 - A **consumer group** contains more than one consumers
 - For example, If there is a topic-01 with partition-01, parition-02, parition-03
     - Say, the consumer_group-01 contains two consumers consumer-01, consumer-02. Note: the consumer will connect to all the partition. consumer-01 connect to partion-01, consumer-02 connects to partition-02 and partition-03
     - Say, the consumer_group-02 contains three consumers consumer-01,consumer-02, consumer-03. Note: each consumer will connect to each partition in this case
     - Say, the consumer_group-03 contains four consumers consumer-01, consumer-02, consumer-03, consumer-04. Note:- the fourth consumer will be inactive since there are only 3 parition to connect.

  - The data is written to the topic in the partition, and the data can be accessed using the **offset**. The offset is an incremental pointer managed by the kafka cluster.
  
**__consumer_offsets** :- 
  - This is a special topic and internal in Kafka. This topic is used to stores the offshores at which consumer group consumers last read offset values. The main advantage of this is in case if the consumer fails, it can restart from where it last stopped. The offset is recored in this topic.
  - When a consumer in consumer group processed data received form kafka, it should be periodically committing the offsets. Note, the kafka broker will write this information to the __consumer_offsets topic not the consumer group.
 
 Delivery semantics for the conumers
   - Three delivery semantics:
     - 1. At least once (preferred one)
        - Offset are committed after the message is processed
        - If the processing ends in exception, the same message will be read again
        - This causes duplicate message processing, we need to make sure the processing is idompotent.
        - Java consumers will autmoatically commit offsets (at least once)
     - 2. At most once
        - Offsets are commited as soon as messages are recived
        - If processing ends in exception, the consumer wil not be able to read the data. Some messages will be lost
     - 3. Exactly once
        - This is used to process message exactly once
        - When we read messages from Topic of one Kafka and write messages to another topic in Kafka. In this case we can use the tansanctional API supported by the Streams API
        - When we read messages from Kafka and process in external system, we need to idompotent consumer.

### Representation of the Kafka cluster

```
Cluster
   Broker-01 
       Topic-01:
           - Partition 0
                - offset 0,1,2,3,4...
      Topic-02
           - Partition 0 
                - offset 0,1,2,3,4,5....

   Broker-02 
       Topic-01:
           - Partition 1
                - offset 0,1,2,3,4...
       Topic-02
           - Partition 1
                - offset 0,1,2,3,4,5....

   Broker-03 
       Topic-01:
           - Partition 2
                - offset 0,1,2,3,4...

Note: The partition in a topic can be spread out to different brokers
      In the above the data is distributed, which is horizontal scaling
      Brokers only have the data that needs to have.
```

  - Kafka cluster is a ensemble of multiple Kafka Brokers
  - Each broker container certain topic and partition
  - Kafka client when connected to the any broker (called a bootstrap broker), then will be connected to the entier cluster. This is handled within the Kafka client.

Client discovering the Kafka broker:
 -  All the broker is a bootstrap server
 - That is, when we connect to one broker in the cluster, the client will know to the details of the other brokers in the cluster.
 - Each broker has the metadata of the other borkers within the cluster. This is more like the Cassandra node aware of other node,using the Gossip porotocol.
 
Replication of Topic:
  - When configuring the Kafka cluster we can set this value between 2 and 3 for production.
  - For local development, we can use 1.
  
```
Say we are having 3 brokers, and configured the replication factor of 2

Broker-01
   - Topic-01 
       - Partition-01
       
Broker-02
   - Topic-01 
       - Partition-02
  - Topic-01*
       - Partition-01*
 
Broker-03
   - Topic-01*
       - Partition-02*

* - represents replication topic.

In the above case if we the Broker-3 is down in the cluster we can still access the data
```
  - Leader election in the parition in the replicated topic
     - At any point of time there can be only one leader among the replicated topic/partition
     - Producer can only send data to the broker that is leader of a partition.
     - The replica parition will be in sync replica (ISR). That is each partition has one leader and multiple ISR. Note, there are out of sync replicas as well.
     - Consumer by default will read from the leader broker for partition. 
     - In case if the leader broker goes down, the next leader broker will be elected and that will used by producer/consumer.

To install kafka in windows we need WSL2

- First it requires Java 11 to be installed in the WSL2 ubuntu
```
$ sudo apt-get update
$ sudo apt-get install openjdk-17-jre-headless
....
$ java --version
openjdk 17.0.4 2022-07-19
OpenJDK Runtime Environment (build 17.0.4+8-Ubuntu-122.04)
OpenJDK 64-Bit Server VM (build 17.0.4+8-Ubuntu-122.04, mixed mode, sharing)

$ wget https://downloads.apache.org/kafka/3.2.1/kafka_2.13-3.2.1.tgz
$ tar xzvf kafka_2.13-3.2.1.tgz
....
```
 - Set the shell script to the PATH variable in WSL2 ubuntu till the extracted binary bin folder 
 ```
 $ vi ~/.bashrc
 .. at the end of this file add below content, the path where the kafka ginary was extracted
 PATH="$PATH:~/kafka-2.13-3.2.1/bin/"

 # reload the updated bashrc to the current shell
 $ source ~/.bashrc 
```
- Before starting the kafka server in Kraft mode (without Zookeeper) we need to create the cluster and format the storage

Refer: https://www.conduktor.io/kafka/how-to-install-apache-kafka-on-windows-without-zookeeper-kraft-mode

```
# below command creates the cluster with randmon id
$ kafka-storage.sh random-uuid >> ./clusterId.txt

# format the storage before starting the cluster
$ kafka-storage.sh format -t $(cat clusterId.txt) -c ~/kafka_2.13-3.2.1/config/kraft/server.properties

# starting the cluster
kafka-server-start.sh ~/kafka_2.13-3.2.1/config/kraft/server.properties
```

### Using Kafka CLI for topic management

we are using Kafka Raft (KRaft) Check KIP500 specs for more info.

In the CLI, we will pass in the `--bootstrap-server host:port` with the commands to connect to Kafka cluster brokers.

#### Create a topic
```
# creates a topic with no replication or number of partition
# using _ or . in the topic name will display warning message since both are same wild cards
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic first-topic
```

#### Creating topic with partitions and replications

```
# since the created cluster has only one broker, the replication factor of 2 below displays an error message. we can use 1 to work in this case

$ kafka-topics.sh --bootstrap-server localhost:9092 --create --topic second-topic --partitions 3 --replication-factor 2
Error while executing topic command : Unable to replicate the partition 2 time(s): The target replication factor of 2 cannot be reached because only 1 broker(s) are registered.
[2022-09-05 12:04:23,722] ERROR org.apache.kafka.common.errors.InvalidReplicationFactorException: Unable to replicate the partition 2 time(s): The target replication factor of 2 cannot be reached because only 1 broker(s) are registered.
 (kafka.admin.TopicCommand$)
```

#### List the topic

```
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
first-topic
second-topic
```
#### Describe the topic or topics

```
# if we don't provide the --topic name, the describe command will list the number of partition, replications, etc.

$ kafka-topics.sh --bootstrap-server localhost:9092 --topic second-topic --describe
Topic: second-topic     TopicId: _1ANasT2TkalgRVOR0F74w PartitionCount: 3       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: second-topic     Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: second-topic     Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: second-topic     Partition: 2    Leader: 1       Replicas: 1     Isr: 1
```

```
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic first-topic --describe
Topic: first-topic      TopicId: tJeTYZQYQZW5GGQZr2gi4A PartitionCount: 1       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: first-topic      Partition: 0    Leader: 1       Replicas: 1     Isr: 1
```

#### Using CLI to produce data to the Kafka cluster using `kafka-console-producer`

##### with no key 

```
# below command will open up a terminal for user to prompt message, no key case

$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic second-topic
>this is demo
>i am trying to use the windows
>trying to make no key command
```

- We can add the `acks` config in the above command 

```
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic second-topic acks=all
```

Note:-
  - running the kafka-console-producer cli, with a topic that doesn't exits then the Topic will be created automatically. The console will display retriable exception until the Topic is created and the leader is elected.

##### with key

```
# we are using the property option, note if the seperator is not provided will throw exception

$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic third-topic --property parse.key=true --property key.separator=:
>user1:user name is u1
>user2:user name is u2
>user1:user can sing
>user2:user can dance
>user1
org.apache.kafka.common.KafkaException: No key separator found on line number 5: 'user1'
        at kafka.tools.ConsoleProducer$LineMessageReader.parse(ConsoleProducer.scala:374)
        at kafka.tools.ConsoleProducer$LineMessageReader.readMessage(ConsoleProducer.scala:349)
        at kafka.tools.ConsoleProducer$.main(ConsoleProducer.scala:50)
        at kafka.tools.ConsoleProducer.main(ConsoleProducer.scala)
```

#### Consume message using CLI

```
# note that consume below will read the data from the end of the broker
# we haven't specified offset value to read from or specified consumer to read from start

# so below command will start listening from the last position in the broker, if any message
# received at this point will be listed in the console

$ kafka-console-consumer.sh --bootstrap-server localhsot:9092 --topic second-topic
```

##### To consume the message from the start or the history message by the consumer

```
# use of from-beginning, but this seem to be display they are not in the order.
# this is because the data is written without the key and producer written in different parition
# in round-robin fashion. If we have 1 parition, then the output displayed will be in order

$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic second-topic --from-beginning
this is demo
may be we need to print the history
trying to make no key command
i am trying to use the windows
this is new one
```

##### To display the messages to with the key value and time stamp

```
# using the kafka.tools.DefaultMessageFormatter

$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic third-topic --from-beginning --form
atter kafka.tools.DefaultMessageFormatter --property print.timestamp=true --property print.key=true --property print.value=true
CreateTime:1662405740233        user1   user name is u1
CreateTime:1662405757246        user1   user can sing
CreateTime:1662405747099        user2   user name is u2
CreateTime:1662405764088        user2   user can dance
CreateTime:1662406602014        user3   user name is u3
CreateTime:1662406610869        user3   user can perform arts
```

#### Using CLI to create Consumer Group

- When we have 5 parition and 3 consumer.

```
# console 1 
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic four-topic --group first-consumer-group
```

```
# console 2
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic four-topic --group first-consumer-group
```

```
# console 3
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic four-topic
>one
>two
>three
>four
>five
>six
>seven
>eight
>nine
```
 - Oberservation of that the message from the producer is distributed between the consumer in the console 1 and console 2 in the above command.
 
 #### using consumer group CLI
 
 - to list the consumer group information

```
# lists the 

$ kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list 
first-consumer-group
``` 

 - describe the consumbe group
 
```
 # Note the lag is pending messages to be read by the consumer group
 # client id is the consoles opened with different consumer groups, in here i have opened 2 consumers in same consumer group
 
 $ kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group first-consumer-group

GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID
                 HOST            CLIENT-ID
first-consumer-group four-topic      0          4               4               0               console-consumer-61c5fc4a-5015-47af-adf0-83b5017b4311 /127.0.0.1      console-consumer
first-consumer-group four-topic      1          4               4               0               console-consumer-61c5fc4a-5015-47af-adf0-83b5017b4311 /127.0.0.1      console-consumer
first-consumer-group four-topic      2          4               4               0               console-consumer-61c5fc4a-5015-47af-adf0-83b5017b4311 /127.0.0.1      console-consumer
first-consumer-group four-topic      3          5               5               0               console-consumer-8517e9df-eb8b-4f9e-a2ed-6e9fbcf3ba29 /127.0.0.1      console-consumer
first-consumer-group four-topic      4          3               3               0               console-consumer-8517e9df-eb8b-4f9e-a2ed-6e9fbcf3ba29 /127.0.0.1      console-consumer
```

Note:-
  - When using consumer group we can't read from the beginning of the partition, since the __consumer_offset topics.
  

#### Resetting the offsets using with the consumer group command.

- If there is any consumer running, we cannot reset the offsets

```
# we need to use the execute and the specific topic or all topic.

$ kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group first-consumer-group --reset-offsets --to-earliest --execute --all-topic
```

- Shifiting by some number, below shift-by -2 and execute 

```
# we can go forward or backward 

$ kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group first-consumer-group --reset-offsets --shift-by -2 --execute --topic four-topic
```

