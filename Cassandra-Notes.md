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
  - No `Master-slave` or `leader-follower` architecture invoved, it is `peer-peer` type.
     - When using `Leader-follower` leader will coordinate with the follower node
     - Follower node are copy of leader node.
     - `sharding` - data spread over multiple node
     - If the leader node goes down, new leader to be elected and delays.
   - `Peer-peer` is used in Cassandra
      - No node is leader no node is follower, all are the same.
      - `Coordinator` takes the data from client and writes the data async to corresponding node. This also sends data correct replica nodes.
      - What happens if the cluster splits down, Cassandra handles the split automatically. 
        - Each node still online that can be seen by client can write the data. 
        - This is configurable, consistency level.
  
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
 - `Consistency` - All nodes see the same data at the same time
 - `Availability` - A guarantee that every client request recives a success/failed response.
 - `Partition Tolerance` - The system continues to operate despite of arbitary partitioning in case of network failures.
  
      ![image](https://user-images.githubusercontent.com/6425536/74592288-a77efd80-4fd4-11ea-8468-c07916a10d2e.png)

Assume when there are three data center in different geological location like US, Europe and Asia, data replication happens asynchronously, which leads to `replication lag`. The data is updated in one data-center and it takes some time to sync up the other data center due to network or other limitations. 
  
This is one situation where it is difficut to achive consistency over the data. Also one of the reason Cassandra considers `Availability` over `Consistency`.

##### Fault tolerance

Developer can take control the avialablity and performance, using the correct configuration.

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

### Setting Apache Cassandra locally
##### Installing Cassandra in Virtual box, and using the steps detailed in this [Link](http://cassandra.apache.org/download/)
##### To install the ubuntu box, in windows 10  follow the [Link](https://github.com/thirumurthis/Learnings/blob/master/Ubuntu19.10-virtualbox.md)
 
 Inside the Ubuntu box once the process is started
 ```unix
   // incase of systemd with older version
   $ service cassandra status 
   
   // in case of centos 7+/8+
   $ systemctl status cassandra
 ```
 ![image](https://user-images.githubusercontent.com/6425536/74711403-c64de180-51d8-11ea-8685-33bde7a0688c.png)

```unix
## To create the table space use cqlsh

$ cqlsh
```
 
 ### Data Modeling in Cassandra
 
 Cassandra is a `column store`. The `key space` in Cassandra is a container for tables, indexes, etc.
 
 The `primary key` is composed of two parts 
   - `Partition key` - tells Cassandra in which server/node the data is present, (this value is passed to hashing function and based on the output value data is store within the node that falls within the range.)
     `We need to know the partition key at the query time.`
   - `Cluster key` - tells Cassandra how to store the row data in catogrized order. The arrangment of data is based on the columns, invokved in the clustering key. This arrangment well order to recover data using clustering key. 

![image](https://user-images.githubusercontent.com/6425536/75085512-ff3ecc80-54de-11ea-9eca-0329d7efd6f6.png)

![image](https://user-images.githubusercontent.com/6425536/75085419-db2ebb80-54dd-11ea-8a89-2b09c592e73b.png)

![image](https://user-images.githubusercontent.com/6425536/75085470-8770a200-54de-11ea-9029-2bd2830b2b9a.png)

![image](https://user-images.githubusercontent.com/6425536/75085488-c9014d00-54de-11ea-9404-a04d4040fb25.png)

Images abover are refered from @[link](https://www.slideshare.net/planetcassandra/cassandra-summit-2014-cql-under-the-hood-39445761)
 
 ##### Sorting using Cassandra query is not achievable, but when creating the table we can use classes to sort the data when storing to table.
 
 ##### Data Types 
  - `Single` 
     - Int, 
     - Text, 
     - Float, 
     - Timestamp, 
     - Date, 
     - UUID, 
     - Blob
  - `Collection` 
     - List - collecton of one or more elements with no duplicates and order matters
     - Map - group of key value pair 
     - Set - collection of one or more elements where the values are Unique and order doesn't matters
     
  ##### `Keyspace` are logical container for Tables, idexes, primary keys, etc. data structure
  When defining keyspace we also define replication stratergy, replicas are high availablity feature. 
  The replication staergy types, 
     - `Simple replication` - Sets number of replicas when using within a single data center.  
     - `Network topology repplication` - Used to set number of replicas when using multiple data center.
  
  Create keyspace query structure:
  ```sql
  CREATE KEYSPACE <Keyspace-name>
  WITH
    replication = {'class':'SimpleStrategy',
                    'replication_factor' : 3};
  ```
 
 ##### Create `table` once the `keyspace` is created.
 
 ```sql
   CREATE TABLE (
      field1 int,
      field2 text,
      field3 text,
      PRIMARY KEY (field1))
  ```
  
  Simple create table with map
  
  ```sql
    CREATE TABLE (
      field1 text,
      field2 int,
      field3 map<text,int>,
      PRIMARY KEY (field1));
  ```
  ##### Selecting `primary key`, this key uniquely identifies the row. 
    
  ```sql
    CREATE TABLE applicationInfo (
      application_name varchar,
      host_id int,
      process_id int,
      cpu_time int,
      os_cpu_time map<text,int>,
      PRIMARY KEY (host_id,process_id));
   ```
   Note:
      host_id => is used as `partition key`, which determines which node the data is stored on.
      porcess_id => `Clustering  key`, which determines how to data is ordered on the disk. 

 Cassandra expects `partition key` to be unique, since if we push data over and over it will be upserted. That is the reason we include clustring key aloing wiht partition key in case there is no unique value. 
 For example, for application if we are grouping based on states like CA, TX, WA. When creating table the primary key would include state and unique id.

`Clustering key` is mostly used for ordering the record, and also for uniqueness.

 When selecting the data from the table we need to use primary keys. We need to specify the whole primary key.
 The below query uses both the partition key and clustering key, which returns data.
```sql
  SELECT * FROM 
    applicationInfo
  where 
    host_id = 'Webserver'
  AND
    process_id = '100';
```
Cassandra also provides option to sort order when data is stored in disk.

```sql
    CREATE TABLE applicationInfo (
      application_name text,
      host_id int,
      process_id int,
      cpu_time int,
      PRIMARY KEY (host_id,process_id)
      ) 
      WITH CLUSTERING ORDER BY (process_id);
```
 Cassandra doesn't provide any mechanism to sort the query results at query time `consider sort order during table creation time`.
 
 ##### Creating secondary `index`
 ```sql
 // create index <index-name> on Table(column);
 //from cqlsh, use describe index-name to view the information
 
 $ create index host_id_index on applicationInfo(host_id); 
 ```

### Kinds of `Numeric datatypes` :
 - INT * (32 bit signed integers)
 - BIGINT * (64 bit signed integers)
 - TINYINT * (1 bit numbers 0 to 255 / 127 to -127)
 - VARINT * ( to store arbitary precision integers with variable length encoding. like varchar)
 - Decimal (with decimal precision)
 - Float ( 32 bit IEEE 754 )
 - Double ( 64 bit IEEE 754 )
 * for integers 

### Kinds of `String datatypes` :
 - ASCII (stores US ASCII char strings / 127 chars - most of keyboard chars) 
 - VARCHAR (stores UTF -8 encoded string / 128000 chars - special characters included)
 - Text ( is an alias of VARCHAR)
 
Note: 
   - ASCII chars is a subset of UTF-8 encoded characters.
   - VARCHAR and Text can be used interchangably.
   
 ### Kinds of `Time datatype`:
   - Timestamp - is date + time (enocded as 8 bits since unix epoch (01/01/1970), number of millisecond since epoch) 
       - Eg: yyyy-mm-dd HH:mm, yyyy-mm-dd HH:mm:ssZ with Z is used to specify 4 digit time zone.
   - TimesUUID - Universly unique identifier based on time and the MAC address of device generating the identifier 
       - Includes Time and sorting on time, this helps to identify order of UUID generated 
  
  ### UUID 
  Universly unique identifier based on time and the MAC address of device generating the identifier
  
  ### Collection (non-atomic value store)
   - List - order preserved, contains duplicate
   - Sets - order free, without duplicate. `It is recommended to use Sets, than List`
   - Map - Sets of key value pair.
  Note: Collection can hold upto 64K items in collection, best practice to keep this small

 ### Tuple 
   Used to create structured collections.
   Tuple order lists of attributes with fixed structure.
   Tuples is different from list and sets which can have varing length.
  Example:
  ```sql
     Create TABLE item_location (
       product_id UUID,
       product_name text,
      location <tuple<decimal, decminal, int>>,
      primary key (product_id));
      
      //Location stores lattitude, longitude and altitude. in the same order.
  ```
  Insert query for tuple data
  ```sql 
      insert into item_location(product_id, product_name, location) 
      values ('4asdada-asdeee-asdada-ereasda','kids toy', (122.40505,-124.0000,60)) 
  ``` 
  
  Note: 
    Used when logically group serveral attribute, and no new attributes cannot be added latter.
    
 ### Designing Cassandra Tables
   - Queries drives the design of Tables in cassandra, not the entities.
   - Cassandra doesn't have `joins` and lots of duplication of data. This is unlike the relational database, since joins are expensive in cassandra so the data duplication happens.
      - Duplication data doesn't mean we are wasting the disk space, we need to choose the data types to minimize the data usage.
      - Avoid using data to store many narrow rows, usage of one wide row boosts performance.
      
 ### Denormalizing (instead of joining and sorting)
   - We use redundant copy of data.
   - Collection to store multiple values, instead of using spearate table.
   
   For example, we are tracking server performance metrics and wanted to build tables.
   We need the metrics to list the servers by average cpu utlization, we need a query and build table accordingly.
   ```sql
   create table avg_cpu_utlization(
   server_name text,
   avg_cpu_utlization int,
   disk_storage int,
   measure_time datetime,
   .., //other fields
   ..,
   primary key ((server_name), avg_cpu_utlization, measure_time))
   with clustering order by (avg_cpu_utlization desc);
  ```
  
  In above query, the primary key includes two parts, 
      partition key - in this case it is servername which tells cassandra in which node it should store the data based on the hash function.
      clustering key - using clustering class.
  
  Assume we need another query which serves the same data in different order (say servers by disk utlization) , we need to create another table here for this scenario.
  Cassandra query doesn't support order by or sort.
  Below is mostly likely same data, but different table.
  ```sql
   create table avg_cpu_utlization(
   server_name text,
   avg_disk_utlization int,
   disk_storage int,
   measure_time datetime,
   .., //other fields
   ..,
   primary key ((server_name), avg_disk_utlization, measure_time))
   with clustering order by (avg_disk_utlization desc);
  ```
  
  Duplication is a denormalizing technique to server faster query.
  
  Using collection is another denormalzing techinque.
  
  Updating data to the column which used collection.
  
  ```sql
  Create table avg_cpu_utlization(
  server_name text,
  user_name text,
  ip_address set<text>,
  ...
  primary key(....
  );
  
  -- to update the values in the ip address
  
  update avg_cpu_utilization 
  set ip_address = ip_address + ['0.1.0.1']
  where server_name = 'webserver1';
  ```
 Note:
  In the above case we don't want to duplicate the entire data, just use the collection to store multiple values, in this case the ip address.
  If you want to don't wan't to preserver order in which the ipaddress are being added, then use set in the field instead of list.
  
  ##### Optimizing the query:
  
  ```
  Create table product_sold(
  id uuid,
  product_name text,
  location map<text,text>,
  manufactured_date date
  ) primary key ((id),manufactured_date);
  
  -- using select query as below will NOT work
  select * from product_sold 
  where manufactured_date < '2020-02-20';
  ```
  Note: 
   The `select` query has the where clause which doesn't reference the partition key. This will generate the error since, Cassandra will try to query all the node.
    
   In order to address this situation, we can include the manufactured_year in the table, so Cassandra will not need to look for data in all the nodes.
  ```sql
  -- include manufacture year to address the above issue.
  Create table product_sold(
  id uuid,
  product_name text,
  location map<text,text>,
  manufactured_date date,
  manufactured_year int
  ) primary key ((id),manufactured_year, manufactured_date);
  
  -- using select query as below will NOT work
  select * from product_sold 
  where manufactured_date < '2020-02-20' and manufactured_year < 2020;
  ```
   
  ### Modeling time series data
   Use wide row for time series data
   
   ```sql 
   --Creating table
     Create Table heat_sensor (
     sensor_name text,
     measure_time timestamp,
     temperature int,
     Primary key (sensor_name, measure_time));
   
   --Inserting row
   Insert into heat_sensor (sensor_name,measure_time, temperature) values ('heat sync','2020-02-20 07:00:00',70);
   ```
  When storing large volume of time series data we could end up in with large number timestamp and measures in row. 
  Row here means data associated with the partition key, Wide values by partition key. 
  
  If we are gathering the temprature data every milliseconds which is much more frequently, then sotring these data in single partition leads to poor query performance.
  
  Solving this problem, using fine grained partition.
    - partition by server and date, like below
 
   ```sql 
   --Creating table
   Create Table heat_sensor (
     sensor_name text,
     measuer_date date,
     measure_time timestamp,
     temperature int,
     Primary key (sensor_name, measure_date, measure_time));
   ```
  
##### Time to live (TTL) feature
      - When insert data we can tell how long the data to be live in the database. 
      - After the TTL period is reached the data is marked for deletion.
   
   We can use the same Create Table query as we did for the heat_sensor table.
   Insert query with the TTL values is as follows, TTL specified in seconds. 
   ```sql
      -- keep the data for one day, TTL is specified in seconds.
      Insert into heat_sensor (sensor_name,measure_time, temperature) values ('heat sync','2020-02-20 07:00:00',70) USING TTL 86440;
   ```
   Note: In this case where the data is expiring, the data can be inserted in DESCENDIND order.
   
   ```sql
   Create Table heat_sensor (
     sensor_name text,
     measuer_date date,
     measure_time timestamp,
     temperature int,
     Primary key (sensor_name, measure_date, measure_time))
     WITH CLUSTERING ORDER BY (measure_time DSEC)
   ```
   ```
   select measure_time, TTL(measure_time) from heat_sensor;
   -- the response will provide number of time to live, how many seconds to live.
   ```
   
 ### When to use `Secondary Index`
  Mostly we design Cassandra tables to address single query.
  We can also design multipe query to fetch similar data from the same table, using index.
  Data duplication is common practice is cassandra.
  
  In the above heat_sensor table, we wanted to capture the manufacture info. we can add the field in the same table, but we will not be able to query using Where clause.
  
  In this case we can create an Index on manufacturer and then fire query.
  
  Secondary indexes are indexes on columns, that allows us to specify that column in the where clause of select query.
  
  When are secondary Index useful, since creating index also has side effects to query performance.
   - There are many rows that have indexed values.
   - Tables don't have counters, an autoincrement feature of Cassandra.
   - The column is not frequently updated.
  
  What happens when we have few rows, with a particular indexed value. This is referred to as `high cardinality index` which doesn't perform well in these cases and should be avoided.
    
 ### When to use `Materialized views`
  We can manage multiple tables when we need to denormalize, each time we add a row to one table we have to add to the other table.
  We need to perform operation to sync the data synchronized.
  
  Cassandra provides a feature called `Materialized view` to address this scenarios. 
  
  Materialized view is table managed by Cassandra. Cassandra will keep data in sync between the tables and materialized views.
  
  ```
  Create table devices (
    id uuid,
    device_name text,
    serial_number int,
    ...
    primary key (id)
  );
  
  CREATE MATERIALIZED VIEW device_by_serial_num
  As
    select serial_number,device_name, manufacturer
    from 
     devices 
     where serial_number is not null and id is not null
     primary key (serial_number, id);    
     
  // Not null is used since the primary key doesn't allow null values
 ```
  
  Materialized views help reduce the overhead of managing denormalized tables.
  Limitation in using materialized view:
   - The primary key of the base table must be in the primary key of the materialized view.
   - Only one column can be added to the materialized view primary key.
   
  Overhead/trade-offs  of materialized view:
   - They introduce additional steps to update operation and data replication
   There will be performance penality about 10% on WRITE operation, READ is not impacted.
   
  Read [link](http://www.datastax.com/dev/blog/materialized-view-performance-in-cassandra-3-x)
  
 ### using `uuid()` function of cassandra to generate uuid value.
   `uuid()` - cassandra provides a function to create a uuid values.
  ```sql
   Create table devices (
    id uuid,
    device_name text,
    serial_number int,
    primary key (id)
  );
  
  Insert into devices ( id, device_name, serial_number) values (uuis(),'sample deice','x1231231');
  ```
  
  Using Set, Map in insert query
  ```sql
  Create Table devices(
  id uuid,
  device_name varchar,
  ip_address set<text>,
  location map<text,text>
  primary key (id));
  
  Insert into devices(id,device_name,location) values (uuid(), 'devicename1', {'192.164.0.1'}, {'data center':'city1','rack':'rack1'});
  ```
  ### How to delete a column from row and delete single row from table   
  Deleting a column in specific row, demonstrating with the same devices table above.
  
  ```sql
  delete device_name from devices where id = 6324adsa-23423asda-323424a-daasd;  
  
  --output:
  The device_name column will be null for the id 6324adsa-23423asda-323424a-daasd
  ```
  
  Delete entire row
  ```sql
  delete from devices where id = 78887ada-asdasd-9899adsa-232;
  
  --output
  The complete row will be deleted.
  
  ```
  ----
  
### Estimating data size
 
Once we have the physical model the size of the data can be estimated, based on below building blocks,
 - Column data = Column name + cloumn value (data type size) + Overhead value (15 bytes or 23 if using TTL); varchar roughly length of string
 - Row data = sum (stored columns size) + 23 bytes
 - Indexes 

Column size based on data type 
  
Estimate % of rows that will have this column (column_usage_percent)
  
Estimate number of rows in the table (row_count)

```
Expected Column storage = (coulumn size) * column_usage_percent 
  
Expected Row storage = sum of columns in expected column storage (+ 23 bytes overhead).
  
Table size = row storage * row count;

Index size also needs to be considered. 
  Size of the columns in the primary key determines size of index.
  Index reqiuires 32 bytes overhead.

Table size + index size
```  
Demo:

```sql
 -- For a table:
  Create Table product(
  id uuid,
  product_name varchar,
  year_manufactured int,
  serial_num text,
  primary key (id));
```

column name | data type | column size | column_usage (%) |Expected column storage(col. size * % col. usage) |
|--------|--------|--------|----|-------|
|id | uuid | 16 | 100 | 16 |
|product_name | varchar | 25 (avg length) | 100 | 16 |
|year_manufactured | int | 4 | 60  (some data misses) | 2.4 |
|serial_num | text | 12 (avg length) | 50 | 6 | 

Expected_row_storage = 16 + 16 + 2.4 + 6 = 40.4 bytes

Row overhead = 23 bytes

Expected row storage with overhead = 63.4 bytes

For 10,000 number of rows, then 634,000 bytes.

--------------

##### Ring/ Cluster

When we need to handle more load/capacity, we can add more node/servers.

Data coming to the cluster, is recieved by any node in the cluster. This node that recives the data to write is called as `coordinator` node. 

Job of the coordiator node is to send the data to the node that handles that range of partition. 

Each node is responsible for a range of data (partition hash), this is known as `token range`. 
Knowing the token range, the coordiantor node sends the data to that corresponding node. That particular node writes the data and sends acknowldgement/response to the client.

Range of the token/partition hash: 2^63-1 to -2^63 
This range is distributed accross the ring/cluster.

`Partitioner` - determines how the partition hash/ data is distributed among the ring/cluster.
If partitioner didn't work as expected the range will be distributed unevenly, where some nodes are overloaded when others doesn't have issue.

Use `Murmur3` or `MD5` partitioner, which will evenly distribute data in random fashion. 

##### when a node joins cluster
- There is no downtime when the node joins ring.
- Gossips to the seed nodes. (seed node list among the possible nodes from cassandra.yaml)
- The other nodes calculate where the new node to fit within the ring. (this can be manual/automatic)
- The other node streams the data to the new node.
- Four stages to each the node:
     - JOINING (During this phase the node is still recieving data, but not fully joined and not ready for reads) 
     - LEAVING
     - UP
     - DOWN
- During joining phase of node, when the driver connects to cassandra cluster it participates in exchange of the data. When driver connects it understands the token ranges and will store the informaton locally. The dirver is aware of the token ranges belongs to which node and which replicas. This is called `tokenAwarePolicy` of driver.

Different types of policy:
    - `TokenAwarePolicy` - driver chooses the node which contains the data
    - `RoundRobinPolicy` - driver will perform round robin the ring, relys on `coordinator`
    - `DCAwareRoundRobinPolicy` - round robin the data center (data stays within local dc)

In `tokenAwarePolicy` when data comes to the driver, it knows to which node holds the data needs to be sent thus eleminating the need for `coordinator` node involvement. 

##### Scaling
 - Single node has the disadvantage for downtime and replication of data.
 - More node more capacity, no downtime.
 - Horizontal vs Vertical scaling:
      - `Vertical scaling`: Needs 1 large expensive machine
      - `Horizontal scaling`: Needs multiple less-expensive commodity hardware
    
 ##### Scaling down nodes
  - Decommission the nodes.
     - Drain or flush the data from commit log, memtable. (`nodetool drain`)
     - Execute the decommission process. (`nodetool decommission`)
        - During decommission, `nodetool status` will display LEAVING Status.

### VNODES (virtual nodes)
  - Uneven token range distribution in a ring/cluster. A good partioner will evenly distribute the token range.
  - Adding and removing (recommission and decommissioning) nodes will have the nodes in the ring to have unbalanced token range for a short duration.
  - Assume int this case where one of the Node is over-loaded with the partition hash range and a new node is added to the ring, the data will get streamed from the overloaded node to newly added node, which is again a overload to the same node with overloaded token range.
  - Cassandras VNode feature is used to address this overloading issue.
      - Vnode feature makes each physical node act more like several smaller VNodes.
      - With VNode each physical node is responsible for smaller slices of ring of smaller size instead of single slice. With consistent hash all the nodes will roughly has same amount of data.
      - When a new noded added with the VNode feature, instead of the new node taking over the token range from one specific node, the new node with Vnode feature will take part of the token range from each other node. Thus data streamed to the new node in more balanced way.
      - VNode feature automatically manges the token range assignement, instead of administrator managing it.
      - By default, each node has 128 Vnodes.
      
##### Configuring VNode
   - using `num_tokens` in the cassandra.yaml file.
   - value greater than 1 will turn on vnodes.

```
# comment the initial_token value from cassandra.yaml file
# add num_tokens = 128 
# restart the cassandra service ( $ systemclt start cassandra)
$ nodetool ring

# after exeucting the ring command, could see each node responsible for smaller sections. 
```

### Gossip 
  - Broadcast protocol for disseminating data
  - There is NO centeralized server that holds information, instead peer shares the information among themselves, maintaing only latest information automatically.
  - Information spreads out to the cluster/ring in polynomial fashion
  - Nodes can gossip with any numbe of nodes, there is no specifi order in which the nodes gossip.
       - Node picks the node to gossip based on specifi criteria
       - Seed nodes gets more probability of being choosen for gossip by any nodes.
       - Each node initates gossip round every second.
       - Chooses 1 to 3 random nodes to gossip with.
       - No Tracking available on which nodes where gossiped prior.
       - Gossip information spreads quickly, in reliable and efficient way.
  - Gossip spreads Only node metadata.
       - Each node has a datastructure called `Endpoint state`, this stores all the gossip state information of the node.
       - `Endpoint state` contains another data structure 
           - `Heartbeat state`, which tracks two values 
               - generation - timestamp of when the node bootstraped.
               - version - integer value each node increments this value every second.
           - `Heartbeat state` increments when spearding throught the cluster
           - `Application state` which tracks 
               - `STATUS` (STATUS=> NORMAL, LEAVING/LEFT - decommission, REMOVE/ING - nodes that cannot be accessed.).
               - `DC=west` (nodes datacenter)
               - `RACK=rack1` (rack in dc)
               - `SCHEMA=abece...` (schema that mutated over time)
               - `LOAD=100.0`  (disk space usage)
  - Gossip is simple message protocol
    
           
       
  
   
