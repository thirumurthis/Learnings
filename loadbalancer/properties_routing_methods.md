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


