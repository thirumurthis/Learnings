#### Layer4 vs layer 7 load balancing:

#### Load Balancer ( fault tolerance)

##### Type of Load Balancer :
  - Layer 4 LB
      - we only know only the `IP` and the `port`
      - we don't read the data in this TCP layer, the data is encrypted.
      - decision is based on the `Ip` address and `port` not based on the data.
      - based on the Load balancer algorithm, round-robin or random it will pass the request to server.
      - the layer 4 LB software will manage a table where the incoming ip address to lb ip address and pass the request to the server.
         - the LB will use NAT to change the Ip address under the hood, the source is change to LB ip. 
      - this totaly is only one TCP connection
      - this is a reverse proxy, where the client doesn't know the request is forwarded to
        - the server doesn't know the request is comming from, this is reverse proxy.

  - Layer 7 LB

OSI model 
```
  Layer 7 - Application - GET / ip port (Http headers, cookies, content-type)
  Layer 6 - Presentation
  Layer 5 - Session
  Layer 4 - Transport- TCP Host to Host, Flow of control
  Layer 3 - Network - (Packets, contains IP address, routing, subnet traffic control)
  Layer 2 - Data Link - (Frames, like envelopes, contains MAC address)
                         NIC card - switch - NIC card, Establishes & terminates the logical link between nodes.
  Layer 1 - Physical - (physcial structure, like cables, hub)
```


