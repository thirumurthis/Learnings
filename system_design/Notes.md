### Scaling:
  - Vertical:
     - Adding more Disk, RAM, etc.
     
  - Horizontal:
     - Adding more machines itself.


### Load Balancing:
  Check [link](https://github.com/thirumurthis/Learnings/blob/master/loadbalancer/properties_routing_methods.md)


### Caching:
  - Improve performance of application
  - cost effective

##### Peformance and speed of using cache:
   - Reading from memory is much faster than disk
   - Can serve same amount of traffic with fewer resource
   - Pre-calculate some data that needs to be cached. (for example, twitter does with the time line, so this pre-compute the last 200 tweets and push it to cache, so served faster)
   - Most apps have more reads than writes, perfect for caching. 

##### Caching Layers
   - DNS
   - Content Distribution Network (CDN)  (Netflix use open connect)
   - Application
   - Database
