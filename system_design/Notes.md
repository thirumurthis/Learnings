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
-------------

