## Connecting to WebSocket endpoint created using websocketd

In this blog will provide a brief overview of Websocket and how to create Websocket endpoint using Websocketd.

### What is Websocket?

 - From Wikipedia, 
    - WebSocket is a computer communications protocol, providing full-duplex communication channels over a single TCP connection. 
    - The WebSocket protocol enables interaction between a web browser (or other client application) and a web server with lower overhead than half-duplex alternatives such as HTTP polling, facilitating real-time data transfer from and to the server.

> Info:
>
> In conventional Client-Server communication, mostly the Client send the request and the Server sends back the response, this is interaction is more stateless. Server uses Cookies to make this communication look like stateful over TCP/HTTP.
>

    - To establish a WebSocket connection, the client sends a WebSocket handshake request, for which the server returns a WebSocket handshake response
    - The handshake starts with an HTTP request/response, allowing servers to handle HTTP connections as well as WebSocket connections on the same port. Once the connection is established, communication switches to a bidirectional binary protocol which does not conform to the HTTP protocol. 

Recommend to understand the Websocket communication protocol by referring to the (wiki)[https://en.wikipedia.org/wiki/WebSocket] and other sources.

### What is websocketd?

 - Websocketd is websocket daemon service with which we can run Websocket server to create WebSocket endpoint.
 - Any program that can be executed from command line, we can expose the output as a WebSocket endpoint.
 - If a program/process prints the message contents with new line char to STDOUT, the output can be sent as a WebSocket message to browser using `websocketd`.
 
#### Installing websocketd

 - Download the executable from the link http://websocketd.com/

In below demonstration, the `websocketd` is used to run the shell script `command.sh`.
  - The `command.sh` script access the API (https://www.randomnumberapi.com/) using cURL command. This API generates a random number. 
  - This script executes the cURL command in while loop and pause for a second. 
  - The API response is enclosed with `[]`, inorder to remove these brackets `sed` command is used.

#### smoothie.js library for time-series chart

 - The `smoothie.js` library placed under js, this is a Javascript library used to plot time series plot, which will look like real-time chart.
 - The `smoothie.js` script needs to be included as a script in the `index.html`, so we can plot the time-series chart. 

#### websocketd exposes both websocket endpoint and http endpoint

 - When we execute the `websocketd` command the server will be able to serve the `index.html` file with the http url endpoint.

> Note:
>
> Download the smoothie.js from this link http://smoothiecharts.org/
> In this example, if the `index.html` and `command.sh` is created under say `wsd-example`, then place the `smoothie.js` under `wsd-example/js/` folder.
>

#### Representation of program execution

![image](https://user-images.githubusercontent.com/6425536/185732937-4ad10327-b5ee-4537-b7bf-8618bfad97c1.png)

The `command.sh` shell script content

```sh
#!/bin/sh

# For development purpose provided count as 99, this can be infinite as well
count=99
incr=0
while [ $incr -lt $count ]; do
   curl -s "http://www.randomnumberapi.com/api/v1.0/random?min=100&max=1000&count=1" | sed -e 's/\[//g' -e 's/\]//g'
   incr=$(expr $incr + 1)
   sleep 1
   echo ""
done 
```

- The `index.html` file content.
  - In order to connect to the Websocket endpoint, we use the Javascript Websocket object in the script section.
  - Once the websocket connection established, we can handle the events with callbacks.
  - In below case have used `onmessage` event the callback uses the data from the shell script STDOUT command. 
  - This output is passed to the smoothie library object to plot the data.

```html
<!DOCTYPE html>
<html>
  <head>
    <title>websocketd count example</title>
    <style>
      #count {
        font: bold 150px arial;
        margin: auto;
        padding: 10px;
        text-align: center;
      }
    </style>
  </head>
  <body>

    <canvas id="mycanvas" width="400" height="100"></canvas>
    <script type="text/javascript" src="js/smoothie.js"></script>

    <script>
      var ws = new WebSocket('ws://localhost:8084/');
      var smoothie = new SmoothieChart();
      
      /* Below commented code is to add color to the plot */
      /*
      var smoothie = new SmoothieChart({
           grid: { strokeStyle:'rgb(125, 0, 0)', fillStyle:'rgb(60, 0, 0)',
             lineWidth: 1, millisPerLine: 250, verticalSections: 6, },
           labels: { fillStyle:'rgb(60, 0, 0)' }
       });
       */

      smoothie.streamTo(document.getElementById("mycanvas"));
      var line1 = new TimeSeries();
      ws.onopen = function() {
        document.body.style.backgroundColor = '#cfc';
      };
      ws.onerror = function (error){
        console.log("errror:- "+error)
      }
      ws.onclose = function() {
        document.body.style.backgroundColor = null;
      };
      ws.onmessage = function(event) {
        setInterval(function() {
        line1.append(new Date().getTime(), event.data);
        }, 1000);
        smoothie.addTimeSeries(line1);
       //Below adds color to the smoothie chart
       //smoothie.addTimeSeries(line1,{ strokeStyle:'rgb(0, 255, 0)', fillStyle:'rgba(0, 255, 0, 0.4)', lineWidth:2 });
      };
    </script>
    
  </body>
</html>
```

### Output of websocketd process

- Make sure to navigate to the directory where the `index.html` and `command.sh` file exists.
- Issue below command.
  
```
$ websocketd --port 8084 --staticdir=. sh command.sh
```

#### Accessing the exposed HTTP endpoint using Browser

Below is the `websocketd` output, which displays the endpoint url that can be used from browser to access

![image](https://user-images.githubusercontent.com/6425536/185732980-88324066-1262-4dcc-b71c-093234af99e4.png)

Now we should be able to connect to index.html using `http://localhost:8084/index.html`

- Output when connected to http URL using browser

![image](https://user-images.githubusercontent.com/6425536/185733066-15e082e7-fb43-47e9-a777-d1a1336d877b.png)


![browser-output](https://user-images.githubusercontent.com/6425536/185775648-8b338270-9b30-4abb-aa4c-9d3286d8513e.gif)


#### Accessing the Websocket (WS) endpoing using Nodejs websocket client

##### Connecting using Nodejs websocket client

Prerequisites:
   - NodeJs already installed and able to use `npm install` command in the system.
   
- Use below command to install websocket library

```
# create a folder and issue below command
$ npm install websocket
```

> **NOTE**
>
> The above command will generate the package.json, if not you need to use `npm init` first to initialize a nodejs application
> 
> The package.json will look like below,
> 
>  ```json
>  {
>    "dependencies": {
>    "websocket": "^1.0.34"
>    }
>  }
>  ```
> 

- Creating the Javascript websocket client code and connect to Websocket endpoint.

```js
const WebSocketClient = require('websocket').client;

var client = new WebSocketClient();

client.on('connectFailed', function(error) {
    console.log('Connect Error: ' + error.toString());
});

client.on('connect', function(connection) {
    console.log('WebSocket Client Connected');
    connection.on('error', function(error) {
        console.log("Connection Error: " + error.toString());
    });
    connection.on('close', function() {
        console.log('echo-protocol Connection Closed');
    });
    
    /* Once connection established below console log will be printed */
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            console.log("Received: '" + message.utf8Data + "'");
        }
    });
   /* Ignore the below functon this is not tested for this demonstration */ 
   function sendNumber() {
       if (connection.connected) {
           var number = Math.round(Math.random() * 0xFFFFFF);
           connection.sendUTF(number.toString());
           setTimeout(sendNumber, 1000);
        }
    }
    sendNumber();
});
// when using the protocols like echo-protocol - got exception 
// Exceptec a Sec-WebSocket-Protocol header 
// solution https://github.com/theturtle32/WebSocket-Node/issues/114
// https://stackoverflow.com/questions/14933235/access-golang-websocket-server-with-nodejs-client
client.connect('ws://localhost:8084/');
```

- To execute the client code, save it as wsd.js file and issue below command

```
$ node ./wsd.js
```

The output will be displayed like below

![image](https://user-images.githubusercontent.com/6425536/185733445-c469cc68-8393-4d1a-a954-dff54a74547e.png)
