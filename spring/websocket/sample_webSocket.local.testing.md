


#### Below is the test case for websocket


```java
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

class WebSocketConfigTestCase {

	@Test
	void testRegisterStompEndpoints() {
		try {
			WebSocketConfig wsc = new WebSocketConfig();
			StompEndpointRegistry seprg = mock(StompEndpointRegistry.class);
			StompWebSocketEndpointRegistration wser = mock(StompWebSocketEndpointRegistration.class);
			when(seprg.addEndpoint("/my-project-socket")).thenReturn(wser);
			when(wser.setAllowedOrigins("*")).thenReturn(wser);
			wsc.registerStompEndpoints(rg);
	
		} catch (Exception e) {
			fail(e.getMessage());	
		}
		
	}

}
```

#### Controller class which used swagger as well

## refer [link](https://stackoverflow.com/questions/52999004/subscribemapping-vs-messagemapping)

```java
public class ProjectController{
 
  public static String TOPIC_URL = "/topic/my-app/{modules}";
  
  //Other controller methods
  
   @GetMapping(value = "/my-project-module/all/{itemid}", produces = "application/json")
   public Items getAllItemss(@PathVariable String itemId) {
     
     //use the service to fetch the data from DB
     return items;  // items list of items

   }
  
   // Below will push the info to the subscribed client.
    @SubscribeMapping(ProjectController.TOPIC_URL)
  // If we have path variable part of the url, then we can asl
    public String onSubscribe(@DestinationVariable String[] modules, PreAuthenticatedAuthenticationToken token) {
        CustomUser usr = (CustomUser) token.getPrincipal(); // fetched from the spring security  
        
        if (!usr.getAccessLevel().containsAll(Arrays.asList(modules))) {
            throw new AccessDeniedException(usr.getName()+" user don't have access");
        }
        
        System.format(" user info : {} for {}", user.getName(), modules);
        return "";
    }
  
}
```

##### web configuration
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
        registry.addEndpoint("/my-project-socket").setAllowedOrigins("*");
    }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    ThreadPoolTaskScheduler tpts = new ThreadPoolTaskScheduler();
    tpts.setPoolSize(1);
    tpts.setThreadNamePrefix("wss-hb-thrd-");
    tpts.initialize();

    registry.enableSimpleBroker("/topic")
        .setHeartbeatValue(new long[] { 25000, 25000 })
        .setTaskScheduler(tpts);
  }
}

```
