### Example to recieve response.

```java
package com.camel.demo.CamelProj;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;

public class PrintBeanCamelUsageInOUT {
	//components - HTTP, etc
	public static void main(String args[]) {
		
		// One way to register the bean in the camel context using simple registry
		SimpleRegistry registry = new SimpleRegistry();

    //using bind - binding to the registriy - THIS OPTION WORKS (comment resgistry.put() line to this work)
		//registry.bind("PrintBean", new PrintBean());

    //using map object - and adding to registry - THIS WORKS, Instead of binding twice, we can do only once (comment the above)
		Map<Class<?>,Object> map= new HashMap<>();
		map.put(PrintBean.class, new PrintBean());
		registry.put("PrintBean",map);
		
		CamelContext camelContext = new DefaultCamelContext(registry);
		
    //Below two lines is how to add the bean, to the Default context.
    // NOTE: here this is not component to use addComponent()
		//CamelContext  camelContext = new DefaultCamelContext();
		//camelContext.getRegistry().bind("PrintBean", PrintBean.class);
		
		try {
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
            // This response provides an response, InOut => used for recieving response
            from("direct:input").inOut("bean:PrintBean?method=sayHello()").to("stream:out");
				}
			});
	
			camelContext.start();
			ProducerTemplate template = camelContext.createProducerTemplate();
			template.start();
      // Producer template, to get response, when using InOut
			Object obj = template.requestBody("direct:input","First Message");
			System.out.println("inout -> "+obj.toString());
			Thread.sleep(2000);
			template.stop();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			camelContext.stop();
		}
	}
}
```
- Bean class
```java
package com.camel.demo.CamelProj1;

public class PrintBean {
	public String sayHello() {
		return "hello From Spring Bean";
	}
}
```

### Example of using bean using beanRef
```java
 	SimpleRegistry registry = new SimpleRegistry();
	registry.put("helloBean", new HelloBean());
	context = new DefaultCamelContext(registry);
	template = context.createProducerTemplate();
	context.addRoutes(new RouteBuilder() {
	       public void configure() throws Exception {
	           from("direct:hello").beanRef("helloBean");
                }
	    });
	context.start();
        Object reply = template.requestBody("direct:hello", "World");
	assertEquals("Hello World", reply);
	template.stop();
	context.stop();
     }
```
