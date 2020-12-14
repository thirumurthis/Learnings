#### How to debug Camel routes

Step 1:
 - Start the camel process with java `jolokia` agent. (in Eclipse use it in vm arguments)
 - This jar will set up an url locally that can be connected using hawtio 
 ```
   -javaagent:"C:\thiru\learn\hawtio_usage_demo\jolokia-jvm-1.6.2-agent.jar"
 ```
 
 ![image](https://user-images.githubusercontent.com/6425536/102028243-109ee100-3d5e-11eb-90cb-203b75d2ee59.png)

Step 2:

 - Download the hawtio jar and start the service locally
 ```
  java -jar hawtio-app-x.y.z.jar
 ```
 
 ![image](https://user-images.githubusercontent.com/6425536/102028302-5491e600-3d5e-11eb-8a9d-c73f71247221.png)

Step 3:
  - once the hawtio is up, issue `http://localhost.1:8080/hawtio` to open up the web module.
  - add connect and since the java application is already started with jolokia, use the instance `http://127.0.0.1:8778/jolokia`

![image](https://user-images.githubusercontent.com/6425536/102028381-c2d6a880-3d5e-11eb-909a-0ec9dce17ef7.png)


 - Camel sample Code which actually sets a counter and routes based on random number using choice

```java
package com.learning.camel.sendMsg;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;

public class SendMsgToEndPoint {
	public static void main(String[] args) throws Exception {
		
		CamelContext context = new DefaultCamelContext();
		
		try {
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				//from("timer:SendOut?repeatCount=5000&delay=2000")//
				from("direct:input")
				.setHeader("count",method(CountIt.class)) // sets a counter and adds to the header of the message
				.setBody(simple("${headers.count}"))
				.process(new RandomNumberGenerator())
				.choice().when().simple("${body} >= 5").setBody(simple("greater than 5 - ${body}")).to("stream:out")
				.otherwise().setBody(simple("less than 5 - ${body}")).to("stream:out");
			}
		});
		context.start();
		ProducerTemplate template = new DefaultProducerTemplate(context);
		template.start();
		for(int i=0;i<=100000; i++) {
		template.sendBody("direct:input", i);
		  Thread.sleep(5000);
		}
		Thread.sleep(600*1000);
		}finally {
			context.stop();
		}
	}
}


package com.learning.camel.sendMsg;

public class CountIt {

    private int counter = 0;

    public int count() {
        return counter++;
    }
}

package com.learning.camel.sendMsg;

import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class RandomNumberGenerator implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Random rand = new Random();
		int random = rand.nextInt(9);
		exchange.getIn().setBody(random);
	}
}
```
