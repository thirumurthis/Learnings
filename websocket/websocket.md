
#### Websocket
  - websocket used http/https to initial handshaking and then uses ws:// scheme for further communication
  - once the connection is established it is maintained till the connection is closed.

##### Simple example using websocket server:

`websocketd.com` provides a websocket server.

 - In linux environment, download the zip folder, using `wget` command.
 - support to windows is also available.
 
 - once downloaded use below command to start the server.
 
 ```
  $ websocketd --port 8080 --devconsole ./count.sh
  -- where count.sh contains simple script.
 ```
 
 check the tutorial at, [gitlab.com](https://github.com/joewalnes/websocketd/wiki/Ten-second-tutorial)
 
 #### Spring boot web-socket chat application can also be created.
   - mainly it requires the web-socket dependency of spring boot
   - javascript stomp.js, and jquery library. Spring boot packages it as `webjar`.
   - `MessageBroker` is one of the main component which is used to register the queue/topic.
   - Spring provide a websocket server which can be configured with an endpoint.
   - in the client side, 
      - the stomp js is used to create an websocket to connect to the endpoint.
      - then subscribe to the topics/queues in the endpoint.
      - `subscribe` method provides a call back function which can be used to handle the response.
      - `send` message to actual controller developed, which will perform the logic and pushes the message to the message broker internally.
   
