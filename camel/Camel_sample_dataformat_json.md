

```xml

<bean id="processInput" class="demo.CustomProcess" />

<camel:camelContext id="sample1"
		xmlns="http://camel.apache.org/schema/spring"
		typeConverterStatisticsEnabled="true">

	
		<onException>
			<exception>com.fasterxml.jackson.core.JsonParseException</exception>
			<handled>
				<constant>true</constant>
			</handled>
			<process ref="parseExceptionResponse" />
		</onException>

		<!-- to validate whether camel polls the directory continouesly -->
		<route>

			<!-- <from uri="scheduler://delayedScheduler?initialDelay=1m&amp;delay=1m30s"/> -->
			<from uri="file://c:/input/camel_data/inbound" />
			<log
				message="MSG RECEIVER - APP | $simple{in.header[CamelFileName]}"
				loggingLevel="INFO"></log>
			<unmarshal>
          			<json id="inputMsg" library="Jackson"
				unmarshalTypeName="demo.test.OrderInfo" />
	
      </unmarshal>
			<process ref="processInput" /> <!-- process is a class which uses exchange to fetch -->
		</route>

		<!-- <route> 
        <from uri="file://c:/tim/camel_data/outbound1"/> 
         <process ref="printProcess" /> 
         <to uri="file://c:/ouput/camel_data/outbound" /> 
     </route> -->
	</camel:camelContext>
```

```java

//sample my json pojo use tools to generate which looks like 

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "version",
    "order",
    "date"
})
public class OrderInfo {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JsonFormat
    (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ssZ")
    private Date orderDate;
    
    //....
    }

```

```java
public class CustomProcess implements Processor{

    @Override
    public void process(Exchange exchange) throws Exception {
            OrderInfo msg = exchange.getIn().getBody(OrderInfo.class);
        }
}
```
