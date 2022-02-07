Apache Kafka:

Terminology:
  - Producer
  - Consumer
  - Message
  - Topic
  - Partition
    - Offset/index
  - Consumer group
  

- The `producer` push the message to Topic, Consumer reads the message from the Kafka broker/Server.

- Kafka provides capability to partition he large message, like sharding. But partition needs to be configured its not out of the box
- When consuming a partitioned message the consumer must need to know the topic, partition and the position/offset of the message.
- Partition is used for distirbuted events.

- `Consumer Group`: Used for process message in parallel on partitions. This mask the need for developer to know the parition details to access the message by the consumer.
   - Consumer group - removes the awareness of the partition from the consumer.
   - Create a consmer group and add consumer to that group. 
   - If there is only one consumer in the consumer group that one consumer is responsible for consumring message from all the partition. 
   - That is, that one consumer in consumre group subscribes to the topic and if that topic has multiple partition. This consumer recives message from all partitions.
- Best practice is one consumer consuming messages from one partition.
- So if the topic has 2 parition and two consumer are present in the consumer group each parition will be consumed by two consumer from consumer group, if the third consumer wanted to join the consumer group what happens?- research?

- By default, with two partition in topic and two consumer in consumer group, one consumer consuming messages from one partition will read messages like queue, from position 0,1,2,3,.. etc. by default.
- The consumer in consumer group can be modified to read the position again. 
- The second consumer in the consumer group will be responsible for parition 2 and cannot read partition1.
- This is how the Queue option is achived in Kafka using consumer group

For Kafka to execute as pub/sub model create different consumer group and add consumer in each unique group.
The same parition can be accessed by different consumer in different consumer group.

In Distributed System:
   - Master - Slave broker (leader-follower).
   - Copy the partition to the follower broker.
  In Kafka, we can tell it to one broker to be leader for the partition 1. Similarly for partition 2 as well.
  
  - How does the producer know and where is the rule stored saying Partition 1 is leader of broker 1 and Paritition 2 of broker 2. 
  - The is information is stored in ZooKeeper.
  - The producer can write to only the leader, the follower can only be used to read.
  - The ZooKeeper has a Gossip system (like Cassandra) to communicate to determine the leader and write it.
 
 Even with the one broker we need ZooKeeper.
   - Start zookeeper
   - Sping up kafka cluster (for single need)
   - We can use docker image.
 
 ```
 docker run -n zookeeper-server -p 8181:8181 zookeeper
 ```
Environment variables:

`KAFKA_ZOOKEEPER_CONNECT=thiru:8181`, the thiru:8180 is the localhost or hostname (thiru) and port where the zookeeper is accessible.
`KAFKA_ADVERTISED_LISTENERS=` can be SSL or Plaintext or TLS.

Kafka spins 3 instances, we need to set as 1 KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR.

```
docker run -n -n kafka-server -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=thiru:8181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://thiru:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 confluentinc/cp-kafka
```

- We can use use nodejs (kafkajs)
  - producer 
  - consumer  
  
```
npm install kafkajs
```
- Admin connection 
```js
const {Kafka} = require("kafkajs")
// note {} is the unpacking the package and setting to variable. This is equivalent to 
//const kafka = require('kafkajs').Kafka
execute ();

async function execute(){
   try {
    // 1. we require a admin connection
      const adminKafkaConnector = new Kafka({
             "clientId" : "thirusapp",
             "brokers" : ["thiru:9092"]  // there can be more than one instance provided so 
             // client can connect.
      })
      const admin = adminKafkaConnector.admin();
      // since promise returned we wait
      await admin.connect();
      
      // creating topics
      await admin.createTopics({
        "topics" : [{ "topic" : "Users",
                      "numParititions": 2
                   }]
        })
        await admin.disconnect();
   } catch (exception){
      console.error(`Error ${exception}`)
   }
   finally{
       // exist the process 
       process.exit();  //node js 
   }
}
```

- producer

```js
const {Kafka} = require("kafkajs")
// note {} is the unpacking the package and setting to variable. This is equivalent to 
//const kafka = require('kafkajs').Kafka

// To fetch arguments from the user pass in when executing this js in node.
// Passed user input.
const userInput = process.argv[2]; 
// argv[0] - node; argv[1]- filename; argv[2] - input value 

execute();

async function execute(){
   try {
    // 1. we require a admin connection
      const adminKafkaConnector = new Kafka({
             "clientId" : "thirusapp",
             "brokers" : ["thiru:9092"]  // there can be more than one instance provided so 
             // client can connect.
      })
      
      // we use producer instead of admin
      const producer = adminKafkaConnector.producer();
      // since promise returned we wait
      await producer.connect();

      // the parition created will be based on the first char
      // in user name provided here. A-M partition 1, N-Z parition-2.
      const partition = userInput[0] < "N" ? 0 : 1;
      //above
      const result = await producer.send({
          "topic" : "Users",
          // messages array to send message.
          "messages" :  [
                {
                  "value" : userInput,
                  "partition" : partition
                }
          ]
      })
      console.log("result - ${JSON.stringfy(result)})
      // the response on producer will have parition,
      // offset, topic name, etc info.
      await producer.disconnect();

   } catch (exception){
      console.error(`Error ${exception}`)
   }
   finally{
       // exist the process process.exit();
   }
}
```
Store the file in input.js, then in this case to run the app use `node input.js tom` which will produce the tom to parition

- Consumer:
  - we had to specify which group it should belong.

```js
const {Kafka} = require("kafkajs")
// note {} is the unpacking the package and setting to variable. This is equivalent to 
//const kafka = require('kafkajs').Kafka

execute();

async function execute(){
   try {
    // 1. we require a admin connection
      const adminKafkaConnector = new Kafka({
             "clientId" : "thirusapp",
             "brokers" : ["thiru:9092"]  // there can be more than one instance provided so 
             // client can connect.
      })
      
      // we use consumre instead of admin
      const consumer = adminKafkaConnector.consumer({"groupId":"test"});
      // since promise returned we wait
      await consumer.connect();
      
      // Subscribing to the topic
      const result = await consumer.subscribe({
         "topic" : "Users", // to which topic to subscribe
         "fromBeginning" : true // Do we need to read from begninning?
      })

     // run the consumer  and poll for the message
     
     await consumer.run({
         "eachMessage": async result => {
            console.log(`consumering msg ${result.message.value`)
         }
     })

   } catch (exception){
      console.error(`Error ${exception}`)
   }
   finally{
       // exist the process process.exit();
       // comment out the process.exit() since this should conitously running to poll message.
   }
}
```

Kafka 
 - pros: 
   - Append only commit log. (more like update the message at the end, which is very fast)
   - fast performance
   - Distributed
   - long polling  (consumer waits and polls for message)
   - Event driver, Pub-Sub (brodcast message) and also Queue. 
     Storing the events in Kafka, say an file has beeing uploaded, the consumer can read that parition and work on it.
   - Scale - add new broker and zookeeper will take care of it.
   - Parallel processing available since data is sharded

  - Cons:
    - ZooKeeper - not easy to manitain 
    - Producer need to know the parition, which can lead to problem. This is more like knowing the table name in the Releational database. this complicates the clients. [Vitess is software abstract the sharding]
    - complex to install, configure and manage    
