
- simple AggregationStrategy
```java


public class CustomAggregate implements AggregationStrategy{

   @Override
   public Exchange aggregate(Exchange actual, Exchange source){
      
      OrderStatus orderBody = actual.getIn().getBody(OrderStatus.class);
      
      actual.getIn().setHeader("orderStatus",orderBody);
      
      actual.getIn.setBody(source.getIn().getBody(String.class));
      
      return actual;
   }
}
```

- ORDER 
```java

@JsonRootName(value="order")
@JsonFilter("customFilter")
@JsoneProertyOrder({"correlationKey","order_id"})
@JsonInclude(Include.NON_NULL)
@JsonIgnorePorperties(value={"description"})
public class OrderStatus{

   @JsonPropoerty("status")
   private String status;
      
   public enum orderProcessStatus{
        SUCCESS,
        FAILED;
   }
   
   public String getStatus(){
       return this.status;
   }
   // other @JsonProperties
   
}
```

- RouteBuilder

```java

@Component
public class CustomRouteBuider extends RouteBuilder{
   
   public String contentType = "application/json";
   
   @Override
   public void configure(){
      rest("/order)
        .id("Order-REST-Endpoint")
        .put("/product").id("PUT-product-EP").consumes(contentType)
        .to("direct:orderRoute")
        .put("/payment").id("PUT-payment-EP").consumes(contentType)
        .to("direct:paymentRoute");
        
       from("direct:orderRoute?exchangePattern=InOnly") //no need for acknowldege
         .id("order-process")
         .autoStartup(true) // we can configure this to flow from the properties file 
         .unmarshall().json(JsonLibrary.Jackson, OrderStatus.class, true)
         .choice()
           .when().simple("${body.getStatus()} == null || "${body.getOrderId()} == null})
             .removeHeader("CamelFileName")
           .otherwise()
             .setHeader("CamelFileName").simple(${body.getOrderId()}.${body.getStatus()})
             .log(LoggingLevel.INfo, "logged - ${header(OderStatus.OrderId)}")
            .end()
          .choice()
            .when(header("CamelFileName").isNotNull())
                .setHeader("OrderStatus.Status").simple("${body.getStatus()}")
                .setHeader(Exchange.HTTP_METHOD).constant(HttpMethods.GET) // spring class
                .pollEnrich()
                .simple("${body.getStatus()")
                .aggregationStrategy(new CustomAggregate())
                .log("${header[CamelFileName]}")
             .choice()
                .when()
                    .simple("${header[OrderStatus.status]} =~ 'SUCCESS'")
                    .to("file:{{order.location}}/success")  //order.location -> is set in the application.properties of the spring boot application
                .otherwise()
                   .to("file:{{order.location}}/failed")
                .end()
           .end();   
   }
}
```
