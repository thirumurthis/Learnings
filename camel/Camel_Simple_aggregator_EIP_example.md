
```java
package org.simpleapp;

import org.apache.camel.main.Main;

public class MainApp {

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new Aggregator());
        main.run(args);
    }
}
```

```java
package org.simpleapp;

import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
public class Aggregator extends RouteBuilder {

    //Correlation_id
    final String CORRELATION_ID = "correlationId";
    final List<String> deviceList = Arrays.asList("device1","device2","device3");
    private Random random = new Random();
    @Override
    public void configure() throws Exception {

        from("timer:example?period=1000")
                //.process(exchange -> exchange.getIn().setBody(random.nextInt(15)+""))
                .process(exchange -> {
                    Message msg = exchange.getMessage();
                    String device = deviceList.stream()
                            .map(index -> random.nextInt(deviceList.size()-1))
                            .map(item -> deviceList.get(item))
                            .findFirst().orElse("UNKNOWNDEVICE");
                     msg.setBody(new Date()+"");
                     msg.setHeader(CORRELATION_ID,device);}
                   )
                .log(LoggingLevel.INFO, "${header."+CORRELATION_ID+"} - ${body}")
                .aggregate(header(CORRELATION_ID),new CustomAggregator())
                .completionSize(3) //this can be size or time etc.
                .log(LoggingLevel.INFO,"${body}");
    }
}
```

```java
package org.simpleapp;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.Objects;

public class CustomAggregator implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        if(oldExchange == null){
            return newExchange;
        }
        String oldExchangeMessage = oldExchange.getIn().getBody(String.class);
        String newExchangeMessage = newExchange.getIn().getBody(String.class);

        // Example to concatenate the messages from old nad new exchange

        String newMessage = oldExchangeMessage +" : "+newExchangeMessage;
        //set the message in any of the exchange message

        newExchange.getIn().setBody(newMessage);
        return newExchange;
    }
}
```
