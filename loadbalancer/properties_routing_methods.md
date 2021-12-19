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
      - This is useful for stateful sessions
