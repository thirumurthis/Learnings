##### Different load balancing options
   - Nginx, HAProxy - Software based
   - F5, Citrix - Hardware based  (costly, since designed specific for load balancing)


##### Routing methods:
   - Round Robin:
      - Simplest type of routing
      - might result in uneven traffic 

   - Least Connections: 
      - Route based on the number of client connection to server
      - useful in chat or streaming applicaiton

   - Least Response time:
      - Routes based on how quickly the server responds
   
   - IP Hash
      - Routes client to server based on IP 
      - This is useful for stateful sessions (in case we need to maintain session)
        - In certain applicaton the client needs data stored in particular server, in that case we hash the IP address so when the user/client sends request from that IP address it will routed this that server. Example, use case Shopping cart application, for example if the user/client refresh the page and wanted to render the data in server.

##### Type of Load balancer
  - Layer 4
    - only has access to TCP & UPD data
    - simpler since doesn't have access to data itself
    - Faster 
    - It sometimes lead to `uneven traffic` since this doesn't have access to the data within the request.
    
  Note: 
    - Layer 4 load balancer can be setup at the edge of the datacenter or network, since it can look into the request IP address and prevent DoS attack. Rather passing these request to server.
    - So in all the data center, will first transmit the incoming traffic to the Layer 4 load balancer, before allowing it to application server.
    
  - Layer 7
    - It has full access to HTTP protocol and data of the request.
    - SSL termination - it can decrypt the traffic.
    - check for authentication - Say if the use send request without logged in to access certain page instead of sending the request to the application server, we can redirect to authenication page.
    - lots of smarter routing options since we have access to the request data
  
  Note: 
   - These are CPU intensive, with the drop in cost of hardware this impact is mimimal.

##### Redundant LoadBalancer (LB) setup
  - Consider single point of failure, when implementing the load balancer.
  - In production, we should think having an active - passive load balancer setup
      - The Active one will route the traffic
      - The Passive, will keep in sync with the Active LB. In case of failure the Passive LB will take the incoming traffic, till the Active one is fixed.
 
