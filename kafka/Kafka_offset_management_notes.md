TOC:
  - Rebalance parition 
     Note: When consumer join or leave the consumer group rebalancing happens
        - Different stratergy are discussed.
  - Static Group membership
  - Offset management

 
### Consumer groups and Partition rebalancing:
  - When a consumer is joining or leaving the consumer group, the partition will be moved. This is called rebalance.
     - Moving parition between consumer is called rebalancing
     - Reassignment of paritions happenes when a consumer leaves or joins a consumer group
     - Rebalancing also occurs when a new parition is added to a topic

```
# If there are 2 consumer with the 3 parition topic

Parition 0 -----> consumer 0

Parition 2 ------> consumer 1
                 /
Parition 3 -----/

# Now adding the 3rd consumer, the partition 3 will be assinged to consumer 2

Parition 0 -----> consumer 0

Parition 2 -----> consumer 1

Parition 3 -----> consumer 2
```
Different types of rebalance:

 - **Eager rebalance** => All the consumer stops, and then start processing messages (leaving up the membership to that parition)
   - Note: There is a breif moment where no consumer is going to process messages.(stop-the-world-events)
   - Also there is no gaurentees the same consumer will be assinged to the same parition

#### Below is the way to set the Eager rebalance.
  - We need to use the `partition.assignement.stratergy` configuration in Kafka Consumer
      - RangeAssignor :- In older version of kafak the default value used to be `RangeAssignor` (assign parition per-topic basis) leads to imbalance.
      - RoundRobin :- assigns parition accross all topic in round-robin, optimal balance.
      - StickyAssignor :- balances like round-robin, then minimises parition movement (rebalance) when consumer leaving/joining the group.

In most real world we don't want to stop the messages from being consumed and also we don't want different consumer processing from different partition.

 - **Cooperative Rebalance (Incremental Rebalance)**
    - In this stratergy, not all the partition are reassigned between the consumer, only a small subset is impacted.
    - Also the other consumer don't needs to be reassinged so they can process messages/events uninterruptedly.
    - This internally goes several iteration to find a stable assginment of partition.

#### How to set the `cooperative rebalance` stratergy?      
   - CooperativeStickyAssignor :- rebalance is similar to the StickyAssignor, supports cooperative rebalances and therefore consumer can keep consuming from the topic
   
#### Starting Kafka 3, the default Rebalance stratergy is 
   - RangeAssignor, CooperativeStickyAssignor :- Use the RangeAssignor by default, but if we remove the RangeAssignor it will use the CooperativeStickyAssignor (with just a single rolling bounce).
 
#### Kafka Connect is enabled with CooperativeStickyAssignor by default
#### Kafka stream the CooperativeStickyAssignor is turned by default using StreamsParitionAssignor

What is **Static Group Membership**:
   - The default behviour when consumer leaving the consumer group is the parition is revoked and reassigned.
   - If the consumer joins again it will have the new member id and new parition is reassinged.
 
 We can configure to say when a consumer leaves then don't reassing use the same parition.
   - The configuration is done by specifying a `group.instance.id` in the consumer, this makes the consumer static member.

```
# note consumer 1,2,3 are part of same consumer group

Partition 0 -----> Consumer 1 /Id 1 

Partition 1 -----> Consumer 2/ Id 2

Partition 2 ------> Consumer 3/ Id 3

# So if the Consumer 3 /Id 3 leaves the consumer group,
# the parition 2 will not be reassinged to any other since it is static member
```
  - Important notes:
     - There are few other details, if the cosumer joins back within the specified session time out specified by `session.timeout.ms`, the parition is reassinged automatically without triggering rebalance. 
     - If the consumer didn't join back within the specified session.timeout.ms, then rebalance will be triggered.
     - This is a feature that we can use, mostly helpful when we have a use case to maintain local state and cache (helps to avoid re-bulding cache)
     - With kubernetes environment, this can be more helpful

#### In code to use the configuration CooperativeStickyAssignor

```
# Checking the consumer logs in console on the displayed configuration
 properties.setProperty(ConsumerConfig.PARTITION_ASSIGNEMENT_STRATEGRY_CONFIG,CooperativeStickyAssignor.class.getName())
```

### Kafka offset management
 - Kafka manages two types of offset
    - Current offset
    - Committed offset 

When does the offset gets commited?

- we can commit the offset automatically or manually:

### Automatic commit:
     - In the Java consumer API the offsets are regularly committed. (this is done when we invoke the poll() method regularly)
     - The `at-least once` read scenario is enabled by default with below configuration the offset will be commited 
        - When the poll() method is called and below configuration
        - `auto.commit.interval.ms = N ` has elapsed (default is 5 seconds)
        - `enable.auto.commit = true`  (this is true by default)
      - Say, if the poll method is not invoked for N milliseconds (from the above) the offset will be committed every N milliseconds.
      
   
   **Important**:
     - We need to make sure all the messages are processed successfully, before calling the next poll().
     - Sometime in Auto commit of offset we fase reprocessing the same message. This might be because of rebalancing happening before commits. To solve this we can use manual commit.

Kafak maintains two types of offset current and committed offset.

##### Current Offset:
  - Lets assume there are 100 messages in the partition, when the poll() method is called to consume messages, lets say Kafka sends 20 messages then the current offset will be set to 20. This is crucial because the consumer won't receive duplicate messages again. The current offset will be moved forward during next set.

##### Committed Offset:

  - After receiving the set of messages, we will process it. Once processed in most cases we need to persist the record. The committed offset is the indicator saying what was the last offset that was commited to the disk.
  - Committed offset is a pointer to partition to the last record that a consumer has successfully processed.
  - The `Committed offset` is very ciritcal in rebalancing partition.
  - Committed offset is used to avoid resending a processed message again, if a new consumer joins the consumer group.

### Manually committing the offset
To perform manual commit, (Advanced topic)
  - Disable the `enable.auto.commit = false` in the consumer.
  - From different thread to call the, below options
        - commitSync() - This is a blocking method, the thread will wait till the offset is committed
        - commitAsync() - This is non-blocking method.

What is difference between commitSync() and commitAsync():
  - commitSync(), is blocking method (block the call until completing the commit operation), reliable method, it will also performs retries if there recoverable errors.
  - commitAsync() - will send signale to commit and continue (fire-and-forget), though in this method there won't be any retries. Say if the commit signal is sent to commit offset 10, and it failed for some reason, the next signal to commit offset 20 is sent and that succeeds. Since the higher offset is already committed we don't want to retry the older signal.
 
- Implementing commitAsync
   - Below method also has edge case where the process might be uncommitted.
   - So the option in that case is to commit specific offset.

```java

String topic="first-java-demo";
// Properties are set
// refer the consumer code for more info
Properties config = new Properties();

//... configuration..

KafkaConsumer<String,Supplier> consumer = null;

try {
   consumer = new KafkaConsumer<>(config);
   consumer.subscribe(Arrays.asList(topic));
   
   while(true){
      ConsumerRecords<String, Supplier> records = consumer.poll(100);
      
      for (ConsumerRecord<String,Supplier> item : records){
         //process for demo printing
         System.out.println(record.value());
      }
      // call to commit
      consumer.commitAsync();
   }catch (Exception exe){
      exe.printStackTrace();
   }finally{
       // Make sure to call the sync commit in finally block
       // else there will be duplicate processing
       consumer.commitSync();
       consumer.close();
   }
}
```

The edge case in the above code is, before the commit a rebalance occurred. 

  - Delay in next poll. (say we are processing the records for more than 5 seconds, in this case the co-ordiantor will trigger a rebalance) 
  - The rebalance triggered for some reason.

In both cases above, we need to commit the offset before any new consumer starts joining consumer group and start processing the messages. The new consumer joined the consumer group needs to start from the committed offset. So, to gracefully commit the offset we need to know below
   - How to commit a particular offset, instead of committing the offset at the end of processing?
   - How to know that a rebalance is triggered?

The answer is `RebalanceListener`.
In the `commitSync` and `commitAsync` the commit signal is sent at the after calling the poll() method and processing the messages, we don't want this. We need to perform intermidiate commits based on the reblance trigger.

- To identify whether the Rebalance is triggered we have the `ConsumerRebalanceListener` which has two method to be overriden, **onPartitionsRevoked**, and **onPartitionsAssigned**.

 - The consumer API will call the onPartitionsRevoked method before removing the parition. This is one area we can commit the offset.
 - The consumer API will call the onPartitionsAssigned method after the parition is assinged and before start consuming the messages. In this case we don't need to commit at this method call. But this is good to know if we need to handle in some cases.
 
- Code to handle that onPartitionsAssigned

```java
String topic = "first-demo-topic";
String groupName= "customer-grp-1";

KafkaConsumer<String, String> consumer = null;

Properties config = new Properties();
// other consumerconfigurations...
config.put("enable.auto.commit","false");

consumer = new KafkaConsumer<>(config);

//Listener - pass consumer object
RebalanceListener rebalanceListener = new RebalanceListener(consumer); 

//pass the rebalance listener 
// by passing listener to the subscribe 
// the consuemr will invoke onPartitionsRevoked when parition is revoked
consumer.subscribe(Arrays.asList(topic),rebalanceListener);

try{

  while(true){
     ConsumerRecords<String,String> records = consumer.poll(100);
     
     for(ConsumerRecord<String,String> item: records){
      // process the records
      System.out.println(""+item.topic());
      
      // To commit the offset on  the listener below code will do it
      // The below method will maiting the list of offset to be committed
      // till the listener is onPartitionsRevoked is invoked.
      // note below will not invoke every time.
      rebalanceListener.addOffset(item.topic(),item.partition(),item.offset());
     }
     // we need to issue the commit signal - after processing every record
     consumer.commitAsync(rebalanceListener.getCurrentOffsets());
  }
}catch(Exception e){
  e.printStackTrace();
}finally{
  consumer.close();
}
```
- The code for implementing the `ConsumerRebalanceListener`

```java
import java.util.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;


public class RebalanceListener implements ConsumerRebalanceListener {
  
  private KafkaConsumer consumer;
  
  //To hold the list of the offset
  private Map<TopicParition, OffsetAndMetaData> currentOffsets = new HashMap<>();
  
  //constructor
  public RebalanceListener(KafkaConsumer consumer){
     this.consumer = consumer;
  }
  
  public void addOffset(String topic, int parition, long offset ){
    currentOffsets.put(new TopicParition(topic,parition), new OffsetAndMetaData(offset,"Commit"));
  }
  
  public void onPartitionsAssigned(Collection<TopicParition> paritions){
    // we can print for now not doing anything
  }
  
  public void onPartitionsRevoked(Collection<TopicParition> paritions){
    for(TopicParition parition: paritions)
      System.out.println(parition.parition());
      
     
     // parition that are committed
     for(TopicParition tp : currentOffsets.keySet())
       System.out.println(tp.partition())
     consumer.commitSync(currentOffsets);
      curretOffsets.clear();
  }
}
```
