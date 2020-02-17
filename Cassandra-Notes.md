### Apcahe Cassandra:
  - Is a `distributed database` (fast)
  - Is bulit for `high availablity` 
  - `linear scalability`
  - Stable and predicatable `performance`
  - No Single point of Failure `SPOF`
  - Supports `Multi Data Center` option out of the box open source.
     - Cassandra can withstand the failure of entire Data center.
  - Cassandra can be deployed in `Commodity Hardware`, cheaper hardware
     - Vertical scaling is costly, Cassandra can deployed in commodity hardware
  - Easy to manange Cassandra operations
  
  ```
  Note: Commodity Hardware: is a device or device component that is relatively inexpensive, 
  widely available and more or less interchangeable with other hardware of its type.
  ```
  - No Master-slave architecture invoved, it is `peer-peer` type.
  
  ### What is Cassandra?
  
  Conceptually Cassandra can be thought of like a Hash ring where are the nodes/vm in cluster are equal.
  
  Assume each node owns a range of hashes, like a bucket.
  
  When defining data model, creating table we specify the partition key part of the primary key.
  
  When we insert data to the Cassandra the partition key value is passed run thorugh hashing function and depending on the output value we can figure out which bucket or range of hash output fits to and thus which node Cassandra will talk to distribute the data around the cluster. Refer below image.
  
  ![image](https://user-images.githubusercontent.com/6425536/74592135-25daa000-4fd3-11ea-9ef8-8d87aaa08809.png)

  The data is replicated in multiple servers. Since all the nodes are equal any node in the cluster can service an read/write request in the cluster. 
  
  ### Trade-offs `CAP Theorm`
  
  Impossible to be both consistent and highly available during network partition
  Latency between data centers also adds upto impractical consistency
  ```diff
  - Cassandra chooses `Availablity and partition tolerance` over consistency.
  ```
  
  ![image](https://user-images.githubusercontent.com/6425536/74592288-a77efd80-4fd4-11ea-8468-c07916a10d2e.png)

  Assume when there are three data center in different geological location like US, Europe and Asia, data replication happens asynchronously. Which leads to `replication lag`, where the data is updated in one data-center and it takes some time to sync up the other data center due to network or other limitations. This is one situation where it is difficut to achive consistency over the data.

This is one of the reason that Cassandra considers Availability over Consistency.

##### Fault tolerance

As Developer can take control the avialablity and performance.

##### Replication (Replication Factor)
 - Data is replicated automatically 
 - Replication factor is 3, three nodes get the copy
 - Data is always replicated to each replica
 - `hinted handoff` - when a machine is down missing data is replayed via hinted handoff
    - When a machine is down while replication is performed, the node Cassandra talking to will save something called `hint`.
    - Cassandra uses `hinted handoff` to replay the wirtes that where been missed when Cassandra node is up and running and rejoins the cluster ring.
 
 Note:
 ```diff
 - Replication factor is set when we create a keyspace (analogus to schema in relational database)
 - Replication happens asynchronously.
 ```
 
 ##### Consistency level
  How many replicas the Cassandra needs to hear from before serving the read/write request to the client. (how many replicas for query to respond OK)
 
 Different types 
  - ALL
  - ONE * 
  - QUORAM * 
 * - Popularly used are ONE and QUORAM.
 
`ONE` - Cassandra just need to hear from One replica, and serves the client.
      - In this case if two replicas is not available, the client is served with just one node.
      
`QUORAM` - Cassandra has to hear form majority (for replication factor 3, Cassandra needs to hear at least is 2 replicas) 51% or higher.
 
 Points:
   - Consistency level, set at each read/write request. (per query)
 
 ##### How fast read and write data in Cassandra depends on the Consistency level.
 
 Lower the consistency level (with replication factor RF= 3, even if two nodes down the client is served) , high availablity.
 Higher the consistency level (need to hear from more nodes, more nodes to be online which is less tolerant to node going down), low availablity.
 
 ##### Multi-DC 
 For single Datacenter, the consistency level will be ONE or QUORAM.
 For Multi Datacenter, the consistency level to use is LOCAL ONE or LOCAL QUORAM. The data is asyncroniously replicated to other data center. We can specify the replica factor per Key space.
 
 ### Working of Cassandra data READ & WRITE.
 
##### Write request:
  - When a write request is sent to Cassandra cluster, any node within the cluster can serve that request. 
  - The node that serves the request is called `coordinator` node. This node will coordinate with other nodes in the cluster behind the scenes on behalf of query.
  (Note: All node in the cluster are equal, there is no master-slave architecture)
  
  - Once the write request hits the individual (`coordinator`) nodes below happens,
     - 1. The write request data is written to the `commit log`. 
         - Commit log is an append only data structure, which performs sequential IO, which makes it fast. 
     - 2. Then the data is merged to an in-memory representation called `memtable`.
     - 3. Responds back to the Client, that the data is written.
  - Every data that is being written in Cassandra gets a timestamp associated to it.
     - 4. `memtable` memory runs out, Cassandra flushes the data to the disk behind the scenes asynchornously (sequential IO operation which is fast, taking the in-memory data and serializing it to disk). The disk is called `SSTable`.
     
  
