## Scaling:
  - Vertical:
     - Adding more Disk, RAM, etc.
     
  - Horizontal:
     - Adding more machines itself.

--------------------------
## Load Balancer:
  Check [link](https://github.com/thirumurthis/Learnings/blob/master/loadbalancer/properties_routing_methods.md)

------------------
## Caching:
  - Improve performance of application
  - cost effective

##### Peformance and speed of using cache:
   - Reading from memory is much faster than disk
   - Can serve same amount of traffic with fewer resource
   - Pre-calculate some data that needs to be cached. (for example, twitter does with the time line, so this pre-compute the last 200 tweets and push it to cache, so served faster)
   - Most apps have more reads than writes, perfect for caching. 

##### Caching Layers
   - DNS
   - Content Distribution Network (CDN)  
      - Pushing the static content to CDN and caching would serve content faster, not utlizing the server resources infrastructures.
      - Netflix use CDN for video streaming. check [Link](https://medium.com/swlh/a-design-analysis-of-cloud-based-microservices-architecture-at-netflix-98836b2da45f)
   - Application
   - Database
      -  Hibernate has Level 1/2 cache.

 Note: 
   - Redis, MemCache, etc. are key value. 
   - Read and write to the cache. Terminology - Cache hit, miss, evict.

#### Distributed cache
   - Works as the same way as traditional cache
   - This has built-in functionality to replicate data, shard data across servers and locate proper server to find the value for each key.
   - This is used for replication
   - Similar to data redundant store, using Active and Passive cache. Passive cache might be idle most of the time, having backups would save sudden load spikes in server.

#### Cache Eviction
  - This is performed to prevent stale data.
  - Caching only most valuable data to save cost and resources. For example it is not required to store most of the database data in cache we can pre calculate which needs to be cached based on requirement.

#### TTL (time to live) 
  - This is to set time period for cache to be deleted automatically. This is essentially how often the data to be resfreshed.
      - TTL time configured based on the data, for example some twitter like count can be cached with long duration since it is not required to be displayed immediately. This might not be the case with stock prices, the user require to see realtime in this case shorter TTL time can be used.
  - This is also used to prevent stale data.

#### LRU/ LFU
 - This are strategy used to keep the most requested data in the cache. (less to do to preventing stale data in the cache)
 - Types:
    - Least Recent Used (LRU)
      - Once cache is full, we can't add any more keys. we had to choose what to get rid of. 
      - In this strategy, the cache that hasn't been accessed ( or what last accessed latest) will be dropped.
    - Lease Frequently Used (LFU)
      - This will track the number of time the keys is accessed in the cache
      - Once the cache is full, the least accessed keys will be dropped.

 Note: 
   - The highly requeste data is stored in the cache, the least accessed data is requested from the database or application.
 
 Example for LRU/LFU:
   - For a twitter like application, say to keep the most recent tweet with million likes in the cache. For a few year old tweet with less like can be fetched from database.
 
 Insight: 
   - Check `Thundering herd problem`
    - Case study from facebook
       - Situation: When a popular post is uploaded, Facebook dumps from cache to refresh cache and if there are many simultaneous request coming in at the same time to view that post. The request might try to read from the cache in this case since cache is not available the read request is sent to database. This causes issues in the database due to spiked request.
       - Solution to issue at scale cache eviction was that to implement lease and have a backup cache and serve that old data.
 
 #### Caching strategies
   - Cache Aside  (this is the most commonly used)
   - Read Through
   - Write Through (used for write heavy application) 
      - In write through, to increase the amount of writes the database can handle the cache is updated before writting to database itself.
      - This allows to maintain high consistency between the cache it creates latency in writting the data to database.
   - Write Back (used for write heavy application)
      - In this case data is written directly to the cache, and latter to database. In this case if the cache fails the data will be lost.
      - If consistency isn't essential this can be used.
 
 #### Cache Consistency
  -  How to maintain the consitency between database and cache efficency.
  -  This mostly depends on the use case.

Note: The data written to the database should be immediately updated or rendered in case if that data is displayed to user.

-------------

### Database Scaling
  - Application servers are mostly stateless, so it can be horizontally scalled without much issues.
  - Scaling database itself has lots of challenges - replication, partition, etc.

- Most of the web apps are majority reads only. for example, in twitter or facebook the post are written once but read multiple times.
- The above is also a design consideration.

##### Basic scaling techniques:
   - Indexes 
     - Pros:
       - Create index based on the column that is frequently accessed 
         - creating index on most unique or high cardinality field will make lookup efficient
       - indexes speeds up read performance
     - Cons:
       - Writes and updates becomes slightly slower. This is due to the fact that the index also needs to be udpated.
       - Indexes occupies more storage.

   - Denormalization
      - This desinging the database based on the standard best practice.
        - Pros:
           - For example, Add redundant data to tables to reduce joins (with other table)
           - Boost read performance
        - Cons:
           - Slows down writes
          - Risk of inconsistent data across tables
          - Harder to write code, to update redundant data in different tables.

   - Connection pooling
     - Allow multiple application thread to use same DB connection 
     - Save overhead of independent DB connections. 

   - Caching
     - This is not implemented within the database.
     - Cache is setup in front of DB layer to handle request and serve content
     - Not all data can be cached.
     - for a request, if the data NOT exists in the cache a database request is made, for any subsequent request if the data is available in cache it will be served.
     - Example: Reids, Memcached
   - Vertical scaling:
      - Buy a bigger server with more memory, processor, disk.
      - Easiest solution, just buy high compute machine and steup the database and move the data to it.

   - Replicaton and Partitioning 
      - Replication and partitioning are continous scaling
       - Replication
          - Create replica servers to handle read request
          - Master server dedicated only to writes. 
            - In case of master-replica if connection between the master & replica is down it leads to consitency issue. Client request might render with stale data.
            - This provide fault tolerance.
       - Paritioning
          - 1. Sharing (Horizontal partitioning)
             - The schema of the table stays the same, but split accross multiple DB.
               - for example, Say if we are storing user name the user name starting with A-F will be stored in one database, G-M in another.
               - Now since we have the read replicas setup this can handle more writes.
             - Pros: 
                - Handles more traffic.
             - Cons: 
                -  Hot Keys, say if the username with A has more traffic, then that shared will be hit with more traffic leading to uneven traffic.
                -  No joins accross shards.
               Note:
                 - Instagram had an hot key issue with popular celebrity name had way more traffic than others. 
         - 2. Vertical Partitioning:
            - Dividing up the schema of database into seperate tables
            - This will be done by functionality, for example say we have one big data of user in analytics not certain amounts are not used together, so split them.
            - Most data in row isn't needed for most queries.

Selecting NoSQL:
  - The trade offs are known upfront.
  - CAP theorm. Consistency - Availabilty - Partitioning.
  - For transaction based system, consistency is required.
