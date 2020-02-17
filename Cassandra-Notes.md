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
  - No `ACID` guarantee, Atomic:Consistent:Isolated:Durable
  - Eventual consistency
  ```
  Commodity Hardware is a device or component that is relatively inexpensive, 
  widely available and more or less interchangeable with other hardware of its type.
  ```
  - No `Master-slave` architecture invoved, it is `peer-peer` type.
  
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
  `Consistency` - All nodes see the same data at the same time
  `Availability` - A guarantee that every client request recives a success/failed response.
  - `Partition Tolerance` - The system continues to operate despite of arbitary partitioning in case of network failures.
  
  ![image](https://user-images.githubusercontent.com/6425536/74592288-a77efd80-4fd4-11ea-8468-c07916a10d2e.png)

  Assume when there are three data center in different geological location like US, Europe and Asia, data replication happens asynchronously. Which leads to `replication lag`, where the data is updated in one data-center and it takes some time to sync up the other data center due to network or other limitations. 
  
  This is one situation where it is difficut to achive consistency over the data. Also one of the reason Cassandra considers Availability over Consistency.

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
     - 1. The write request data is written to the `commit log`. Every write includes `timestamp`.
         - Commit log is an append only data structure, which performs sequential IO, which makes it fast. 
     - 2. Then the data is merged to an in-memory representation called `memtable`.
     - 3. Responds back to the Client, that the data is written.
  - Every data that is being written in Cassandra gets a timestamp associated to it.
     - 4. `memtable` memory runs out, Cassandra flushes the data to the disk behind the scenes asynchornously (sequential IO operation which is fast, taking the in-memory data and serializing it to disk). The disk is called `SSTable`.
  
  ![image](https://user-images.githubusercontent.com/6425536/74626761-76eab100-5105-11ea-8228-93c19c14bc9d.png)
  
 ##### Cassandra doesn't do any updates or deletes.
 ##### SSTable are immutable.
 ##### when deleting a data, Cassandra writes a **`tombstone`**, a special type of record. A marker to say there is no data here, for this column anymore. `Tombstone` like other records also gets timestamp.
 
 ### What are SSTables?
   - What happens when there are many SSTables?
     - SSTables are immutable files on disk (data file for row storage). 
     - When SSTables are flushed they are written as smaller files, as an optimization there is process called **`compaction`** does merge the smaller SSTables to bigger ones. As merged, only the latest timestamp is kept.
        ```
         Row1 Time1
         Row1 Time2
          only latest time2/latest time is kept part of compaction process.
          this makes the Cassandra fast, also this makes backup extremely trivial
        ```
     - Partition is spread accross multiple SSTables.
     - Same column can be in multiple SSTables.
   - Once the SSTables are written to a disk, copy it to another server and it should work. 
  
  **`Compaction`** (optimization) : A process where the smaller SSTable are merged into a bigger one.

### How READS work in Cassandra?
  - Reads are similar to writes, the node recived the read request is called `coordinator` node.
  - At individual node level, Cassandra looks for data in multiple SSTables (as compaction is running in background process) and in a scenario where this process is still running,
    - Cassandra pulls the data from multiple SSTables to memory, and merge them together using the latest timestamp.
    - Also pulls the unflushed data in the `memtable` it also gets merged
      The data is then sent to client.
 
 In case of read, the choice of Disk type has an impact like using SSD, etc.
 
 If Cassandra has to read on lesser file, then the read will be very fast. 
 
 - When the consistency level is < ALL (ONE or QUORAM), Cassandra performs read repair in background which can set using configuration called `read_repair_chance`. 
 - Since the Cassandra is Eventually consistent system, time to time the nodes disagree about the value (one node might not have the latest updated data). 
 `read_repair_chance` configuration tries to talk to all the replica in order to make all data to be consistent, the default value is `10%`.
 
 ### Data Modeling in Cassandra
 
 Cassandra is a `column store`. The `key space` in Cassandra is a container for tables, indexes, etc.
 
 The `primary key` is composed of two parts 
   - `Partition key` - tells Cassandra in which server/node teh data is present, (check hashing function output to store data)
   - `Cluster key` - tells Cassandra how to order the data when it is returned.
 
 ##### Sorting using Cassandra query is not achievable, but when creating the table we can use classes to sort the data when storing to table.
 
 ##### Data Types 
  - `Single` - Int, Text, Float, UUID, Blob
  - `Collection` - List, Map, Set.
