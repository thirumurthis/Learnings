#### WebSocket to send heart beats to subscribed clients

 - when using spring bean with thread pool

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/my-app-wsocket").setAllowedOrigins("*");
    }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    ThreadPoolTaskScheduler tpts = new ThreadPoolTaskScheduler();
    tpts.setPoolSize(1);
    tpts.setThreadNamePrefix("wss-hb-thread-");
    tpts.initialize();

    /*
     * Below enables sending heartbeats to / from web clients that are subscribed to topic.
     * Using  30 seconds for both sides, other wise the StompJS on the client side will
     * timeout out connections.
     */
    registry.enableSimpleBroker("/topic")
        .setHeartbeatValue(new long[] { 30000, 30000 })  //30 second rep
        .setTaskScheduler(tpts); // pass in the taskschduler created.
  }
}
```

#### how to send message to the endpoint using the SimpMessagingTemplate convertAndSend() method.

```java
// in the specific controller class
import org.springframework.messaging.simp.SimpleMessagingTemplate;


...
private SimpMessagingTemplate wsTemplate = null;
//getter 
public SimpMessagingTemplate getWsTemplate(){
  return this.wsTemplate;
}

//setter injection

@Autowired
public void setWsTemplate(SimpMessagingTemplate wsTemplate){
   this.wsTemplate = wsTemplate;
}

// if want to push the info on schdeuled basis

@Scheduled(fixedRate = 25000)
public void sendMessage(){
 // fetch the info from the Database
 // iterate the object and use template to send to subscriber
 
 //for each 
    wsTemplate.convertAndSend(URL);
 
 }

```
