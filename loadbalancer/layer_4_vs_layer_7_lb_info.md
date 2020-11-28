#### Layer4 vs layer 7 load balancing:

#### Load Balancer ( fault tolerance)

##### Type of Load Balancer :
 - Layer 4 LB
 - Layer 7 LB

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

  ##### pros:
    - simple load balancing
    - faster and efficient (no data lookup), since doesn't look into the data to make decision.
    - The data is still encrypted in this layer, so in a way secure.
    - only one TCP connection established. (router take the repsonsiblity for forwarding the TCP connection, but for client it is one connection only)
    - uses NAT (statefulness)
  
  ##### Cons:
    - Not a smart load balancing
      - the data is not read cookies,etc. so no need to add headers or redirect based on it.
    - Not suitable for Microservice. (not applicable)
       - Since, the ingress protocol can use the content to forward different service based on the path. like using REST endpoint like /image which will we navigate service dedicated to media and knows to cache. the path /message this will navigate to different services.
    - when we forward using LB, which makes a TCP connection there is a maximum limit 1500 bytes. If the GET request size is 1MB, then this needs to be broken to multiple TCP segments, that is one packet multiple segment. The LB better forward all those segments to the same destination. We cannot forward part of the segment in the packet to one destination and some to another destination.
    - no cache, since at this layer the data is not looked up or read.

#### Simple HA proxy implementation to support Layer 4 LB

Below is a simple `express` node js application. 

Application listens to which input port eg. 5000.
When user visits the application /* the application will tell or return which port is server listening to.

```js
const app = require("express")();

const port = process.env.PORT || 3000

app.get("/*",(req,res)=> {
  //any string within `` is considered template ECMA6
   res.send(`sent by ${port}`);
});

app.listen(port,() => console.log(`listening on ${port}`));
```
--------
##### To execute the above application
```
// in WINDOWs machine open command prompt using cmd. issue below command
> set PORT=5000
> node loadBalancer_example.js
Listening on 5000
```
---------
##### Now spin up the loadbalancer using  HA proxy, then listen to different port say, 8080
##### Then forward the load between those two using load balancer using TCP.
- install HA proxy.


  - Layer 7 LB
   - if there is a https connection, the LB should have certificate and need to looks the data to make decision. so if this is compromised the data is exposed.
   -
   
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


