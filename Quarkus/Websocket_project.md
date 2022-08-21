### Below is the way to create websocket using the `Quarkus`.

1. Select websocket platform from the `https://code.quarkus.io`. This is similar to start.spring.io
2. Download the project.
3. Download and install Quarkus CLI. I used Chocolatey package manager `choco install quarkus`
4. Update the `StartWebSocket.java` with the below content.
5. Update the `index.html` with the below content
6. To run the project use `quarkus dev`, then connect the deployed application by connecting from browser using `http://localhost:8080/`

To build the jar, we need to use the Quarkus CLI `quarkus build --native`.

- StartWebSocket.java

```java
package org.acme;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import static java.util.Objects.requireNonNull;

@ServerEndpoint("/start-websocket/{username}")
@ApplicationScoped
public class StartWebSocket {

    Map<String, Session> sessions = new ConcurrentHashMap<>(); 

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
        broadcast("User " + username + " left");
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username);
        broadcast("User " + username + " left on error: " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        if (message.equalsIgnoreCase("_ready_")) {
            broadcast("User " + username + " joined");
        } else {
            broadcast(">> " + username + ": " + message);
        }
    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }
}
```

- index.html should be placed under the `resources\META-INF\resources`. Uses the jquery library.

```html
<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <title>Quarkus Chat!</title>
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/css/patternfly.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/css/patternfly-additions.min.css">

    <style>
        #chat {
          resize: none;
          overflow: hidden;
          min-height: 300px;
          max-height: 300px;
      }
    </style>
</head>

<body>
        <nav class="navbar navbar-default navbar-pf" role="navigation">
                <div class="navbar-header">                  
                  <a class="navbar-brand" href="/">
                   <p><strong>>> Quarkus Chat!</strong></p>
                  </a>
                </div>
        </nav>
    <div class="container">
      <br/>
      <div class="row">
          <input id="name" class="col-md-4" type="text" placeholder="your name">
          <button id="connect" class="col-md-1 btn btn-primary" type="button">connect</button>
          <br/>
          <br/>
      </div>
      <div class="row">
          <textarea class="col-md-8" id="chat"></textarea>
      </div>
      <div class="row">
          <input class="col-md-6" id="msg" type="text" placeholder="enter your message">
          <button class="col-md-1 btn btn-primary" id="send" type="button" disabled>send</button>
      </div>
      
      </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/js/patternfly.min.js"></script>

    <script type="text/javascript">
      var connected = false;
      var socket;

      $( document ).ready(function() {
          $("#connect").click(connect);
          $("#send").click(sendMessage);

          $("#name").keypress(function(event){
              if(event.keyCode == 13 || event.which == 13) {
                  connect();
              }
          });

          $("#msg").keypress(function(event) {
              if(event.keyCode == 13 || event.which == 13) {
                  sendMessage();
              }
          });

        $("#chat").change(function() {
            scrollToBottom();
          });

          $("#name").focus();
      });

      var connect = function() {
          if (! connected) {
              var name = $("#name").val();
              console.log("Val: " + name);
              socket = new WebSocket("ws://" + location.host + "/start-websocket/" + name);
              socket.onopen = function() {
                  connected = true;
                  console.log("Connected to the web socket");
                  $("#send").attr("disabled", false);
                  $("#connect").attr("disabled", true);
                  $("#name").attr("disabled", true);
                  $("#msg").focus();
              };
              socket.onmessage =function(m) {
                  console.log("Got message: " + m.data);
                  $("#chat").append(m.data + "\n");
                  scrollToBottom();
              };
          }
      };

      var sendMessage = function() {
          if (connected) {
              var value = $("#msg").val();
              console.log("Sending " + value);
              socket.send(value);
              $("#msg").val("");
          }
      };

      var scrollToBottom = function () {
        $('#chat').scrollTop($('#chat')[0].scrollHeight);
      };

    </script>
</body>
</html>
```

### Output on how to connect to the WebSocket connection

- Connect using the browser

Using the command `quarkus dev`

![image](https://user-images.githubusercontent.com/6425536/185776201-0e5f96b0-a029-4ec6-8321-60b6f042ab1c.png)

Opened two browser and add the user01 and user02 
![image](https://user-images.githubusercontent.com/6425536/185776254-83cb1ee2-56db-4da0-8773-eb3fc11feb6e.png)

After creating the user, then sending mesage will be broadcast when message received.
![image](https://user-images.githubusercontent.com/6425536/185776285-bc207a06-ec97-458b-8d2e-1450c977f6b3.png)

- Using the browser Dev Console we can use below command in the Console
  - Open the browser, type the `about:blank` in the address bar, and then use the Dev Console

```
let ws = new WebSocket("ws://localhost:8080/start-websocket/user03

ws.send("this message from user03 browser dev console")
```

![image](https://user-images.githubusercontent.com/6425536/185776341-c3db74a7-9f1b-479b-8ab2-9f7cd8083f4b.png)

