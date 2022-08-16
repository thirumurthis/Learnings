
#### Websocket
  - websocket uses http/https to initially handshaking and then upgrades to use ws:// scheme for further communication
  - once the connection is established it is maintained till the connection is closed, at the client side.

#### WebSocket: provide full duplex (two way) communication channel over single TCP connection

#### Traditional Http:
  - Client makes a request.
  - Server interprets request .
  - Server requests info from Database (if non-static content).
  - Database sends reponse back to server.
  - Server formats and sends response to client

Reference for below [IETF](https://tools.ietf.org/id/draft-ietf-hybi-thewebsocketprotocol-09.html)

#### WebSocket Handshake
  - Sample Websocket Client request:
    - Origin, version are the rquired..
  ```
    GET /chat HTTP/1.1
    Host: <server domain name: eg: server.com>
    Upgrade: websocket
    Connection: Upgrade
    Sec-WebSocket-Key: <encrypter key using multiple hash algo>
    Origin: http://client.com
    Sec-WebSocket-Protocol: chat,superchat
    Sec-WebSocket-Version: 14
  ```

#### WebSocket Response
 - Sample WebSocket response:
    - 101 status code is sent.
 ```
 HTTP/1.1 101 Switching Protocols
 Upgrade: websocket
 Connection: Upgrade
 Sec-WebSocket-Accept: <encrytped hashed key>
 Sec-WebSocket-Protocol: chat
 ```
 - WebSocket key is formed by concatenating the websocket key from request to GUID then  encoded to an SHA1 hash then base-64 encoded.
 - When the client recieves this key, it know the frame is direct connection from the server.
 
 ##### Webscoket data limit per frame.
   - WebSocket protocol is based on the TCP protocol, and breaks data down into frames, which are further broken down into binary bits. Each bit has signigicance in the protocol.
      - Frames are specific format and can support 64k 
  ```
     0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-------+-+-------------+-------------------------------+
  |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
  |I|S|S|S|  (4)  |A|     (7)     |             (16/63)           |
  |N|V|V|V|       |S|             |   (if payload len==126/127)   |
  | |1|2|3|       |K|             |                               |
  +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
  |     Extended payload length continued, if payload len == 127  |
  + - - - - - - - - - - - - - - - +-------------------------------+
  |                               |Masking-key, if MASK set to 1  |
  +-------------------------------+-------------------------------+
  | Masking-key (continued)       |          Payload Data         |
  +-------------------------------- - - - - - - - - - - - - - - - +
  :                     Payload Data continued ...                :
  + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
  |                     Payload Data continued ...                |
  +---------------------------------------------------------------+
  ```

##### Simple example using websocket server:

`websocketd.com` provides a websocket server.

 - In linux environment, download the zip folder, using `wget` command.
 - support to windows is also available.
 
 - once downloaded use below command to start the server.
 
 ```
  $ websocketd --port 8080 --devconsole ./count.sh
  -- where count.sh contains simple script.
 ```
 - count.sh file
 ```sh
 #!/bin/bash

for COUNT in $(seq 1 20); do
   echo $COUNT
   sleep 1 
done
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
   
