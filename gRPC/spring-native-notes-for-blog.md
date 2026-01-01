# Spring Native gRPC 

In this article have demonstrated the use of native Spring gRPC to create a simple Client Server application with Spring Boot. There are different third-party libraries but in here the application uses the native Spring gRPC starter. For more details on Spring gRPC refer the [Spring documentation](https://docs.spring.io/spring-grpc/reference).

gRPC is efficient and high performant framework, enables transparent communication between client server using HTTP/2. It is schema based, uses protobuf IDL so achieves fast and compact data serialization. This article doesn't explain the gRPC protocol in details basic understanding of gRPC protocol would be helpful to follow the code. For more info refer the [gRPC introduction and overview](https://grpc.io/docs/what-is-grpc/introduction). 

gRPC supports different APIs patterns which are listed below,

1. Unary - For single client request server sends single response. 
2. Server Streaming - For single client request server send one or more response. 
3. Client Streaming - For one or more client request server sends single response. 
4. Bi-directional Streaming - Client sends more request server responds with multiple responses.

Below is the example of how the APIs service is representated in a protobuf 

```
service GreetService{

    //Unary
    rpc Hello(GreetRequest) returns (HelloResponse) {};

    //server streaming
    rpc HelloManyTimes(GreetRequest) returns (stream HelloResponse) {};

    //client streaming
    rpc LongHello(GreetRequest) returns (GreetResponse) {};

    //Bi-directional streaming
    rpc HelloEveryone(stream GreetRequest) returns (stream GreetResponse) {};
}
```

### Full Source Code

The complete code to the Spring application with gRPC link [grpc-app git repo](https://github.com/thirumurthis/projects/tree/main/grpc-app).


### About The Client Server Application

- The Client Server application uses the native Spring gRPC starter and doesn't utilize third-party libraries.
- The project is a multi-module maven project, all the dependency versions are grouped in the properties section of parent pom.xml. Below are the list of child modules
    1. proto-idl - This module includes protobuf files with the service defined which will be used by the server and client. This sub-module will be packaged to the jar when the project is built with maven command.
    2. grpc-server - This module includes the implementation of generated stubs to handle the client requests. The proto-idl jar is added as dependency in the pom.xml. The class that implements the server stub is annotated with `@GrpcService` this will be scanned by the Spring auto-configuration and service will be registered to Spring context when the application starts.
    3. grpc-client-one - This module creates the clients using the stubs, created a blocking or synchronized client. The proto-idl jar dependency is added in the pom.xml. The client is configured with retrypolicy configuration defined in application.yaml.
 
### Modules in the project
- proto-idl module:
    - This service defined in the protobuf file creates an order, update the order, streams the order status and also includes a method to simulate netowrk delay and random exception.
    - The maven build `mvn clean install` will package the generated code to jar file. This generated code includes stubs to be implemented by server/client.
    - Along with the protobug generated code, a java AppConstants class is also packaged in the jar and used in client and server module.

- grpc-server module:
    - The server uses H2 database to store the order state. The H2 database is configured to store the state in file once the application starts the file will be created under `data` folder of the project root. The database schema and ddl script with sample data is placed under the `resource` folder. When the application starts the sample data can be used for testing the server response. The database script is idempotent, so the application be restarted multiple times.
    - The server starts in default gRPC port 9090. The H2 console can be enabled and UI can be accessed in 8080 port.
    - The DTO layer under the `com.spring.grpc.dto` defines the entity for order and status, it also includes simple builder pattern for easy usage.
    - The `OrderHandler` component class has the necessary service to access the database and it is used in the service implementation.
    - The generated stub service code is implemented in `com.spring.grpc.service.OrderService`. Since this class is annotated with `@GrpcService` this service will be registered to the Spring context by Spring auto-configuration on startup. The service defined in the protobuf is generated as stub and the method is override in the service like in below code snippet.
      
     ```java
       @GrpcService
       public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

         @Override
         public void createOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
          //... implementation goes here...
         }

         @Override
         public void updateOrder(OrderRequest request, StreamObserver<com.proto.app.OrderStatus> responseObserver) {
         //... implementation goes here...
         }
     
         //.. override other service and implement if required
       }
     ``` 

- grpc-client-one application:
    - The client application includes REST endpoints to access the server. Each endpoint utilizes the gRPC client created from the stub to get the server response.
    - The server url is defined in the `application.yaml` file `spring.grpc.client.channels.local.address: 0.0.0.0:9090` and `spring.grpc.server.enabled: false`. Also the client application starts in 8085 port which is defined in `server.port: 8085` property.
    - The controller `com.spring.grpc.client.OrderController` class defines GET, POST and PUT mapping to access the server endpoint respectively.
    - The client bean is defined in `com.spring.grpc.client.OrderClientConfig` class using `ManagedChannelBuilder` to create a channel. The ManagedChannelBuilder is used to include custom configuration to the client channel like loadbalancer type, retryPolicy, etc. The java bean definition code snippet will look like below.

     ```java
      @Bean
      OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channelFactory) {

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forTarget(targetServerAddress)
                .keepAliveTime(10, TimeUnit.SECONDS);;

        if(retryClient.negotiationType().equalsIgnoreCase("PLAINTEXT")){
            channelBuilder.usePlaintext();
        }
        // configure the retry config from helper method
        if(retryClient.enabled()){
            Map<String, Object> config = this.buildServiceConfig();
            log.info("configuration: {}",config.toString());
            channelBuilder.defaultServiceConfig(config);
            channelBuilder.enableRetry();
            channelBuilder.maxRetryAttempts(5);
        }

        ManagedChannel channel = channelBuilder.build();
        return OrderServiceGrpc.newBlockingStub(channel);
      }
     ```
      
    - If we don't want to customize the configuration and use the default config the gRPC client `@Bean` configuration will like below code snippet. The `local` is defined in the `application.yaml`, the local in the property `spring.grpc.client.channels.local.address` were the gRPC server url is defined.

      ```java
          @Bean
          OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channels) {
              return OrderServiceGrpc.newBlockingStub(channels.createChannel("local"));
         }
      ```

    - The `ChannelBuilderClient.java` class is standalone java example of gRPC client also uses  `ManagedChannelBuilder` to create channel to access the server.
    - Since the stub client is accessed via REST, there are additional bean configuration added to support protobuf de-serialization. Refer the client code snippet below.
 

The project also includes `camel-client` folder which uses JBang and Apache Camel gRPC to connect to the Spring gRPC server. The sample code access the status endpoint of the server and prints the streamed status response for the request.

During development we can use tools like `Postman`, `gRPCUI` or other clients to connect to the server. In here have used `grpcui`, refer below for details with snapshot.

### proto-idl module

- Code snippet below shows the rpc service defined in proto file that are used to manage the order. In the below snippet we could see The protobuf file is split into different files if needed we can use import statement to import protobuf file, this helps to group messages based on domain or functionality.

app.proto file snippet

```
syntax = "proto3";

package app;

option java_package = "com.proto.app";
option java_multiple_files = true;

import "app/order.proto";
import "app/sim.proto";

service OrderService {

  rpc createOrder(OrderRequest) returns (OrderResponse) {};
  rpc updateOrder(OrderRequest) returns (OrderStatus) {};
  rpc getOrderStatus(OrderKey) returns (stream OrderStatus) {};
  rpc specialCaseSimulator(SimRequest) returns (SimResponse){};
}
```

sim.proto file snippet

```
syntax = "proto3";

package app;

option java_package = "com.proto.app";
option java_multiple_files = true;

import "app/order.proto";

message SimRequest{
  map<string,string> simulatorRequest = 1;
}

message SimResponse{
  string simulatorResponse = 1;
}
```

The `mvn clean install` command with the full source code will package the stub code to the jar file. Since this jar file is available in local maven `.m2` directory, the server and client can identify it since respective sub-modules includes this dependency.

### grpc-server

 - Code snippet below is the server implementation to create and update order. The generated code from the protobuf module is extend and necessary service method is override with the implementation, below we could see the `createOrder` and `updateOrder` implementation. For full source code refer the git repo mentioned above.

```java
@GrpcService
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

   //.... supporting methods are skipped in this class for complete code refer the git repo
   // https://github.com/thirumurthis/projects
   // grpc-app folder

    public OrderService(OrderHandler orderHandler)
        this.orderHandler = orderHandler;
    }

   @Override
    public void createOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        //super.createOrderByUser(request, responseObserver);
        if(validateInput(request)){
            log.info("order request received from user ...");
            //build order info to store to db
            OrderInfo orderInfo = buildOrderInfo(request, request.getUserType()!=null?request.getUserType():"by_user");
            //insert to the db
            OrderInfo savedOrderInfo = orderHandler.addOrderInfo(orderInfo);
            OrderDetails orderDetails = buildOrderDetails(savedOrderInfo);
            //construct the status to be saved to db
            OrderStatus status = buildOrderStatus(savedOrderInfo, AppConstants.RECEIVED);
            OrderStatus savedOrderStatus = orderHandler.addOrderStatus(status);
            if(savedOrderStatus != null){
                log.info("[by user] Order status - [ orderId: {} | status: {} |" +
                         " updatedBy: {} | userName: {} | updatedAt: {} ] ",
                        savedOrderStatus.getOrderId(), savedOrderStatus.getStatus(),
                        savedOrderStatus.getUpdatedBy(), savedOrderStatus.getUserName(),
                        savedOrderStatus.getEventTime());
            } else{
                log.info("Order Status not updated - [{}]",request.getUserType());
            }
            OrderResponse response = OrderResponse.newBuilder()
                    .addOrderResponse(orderDetails)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            log.error("User Name and Item Name are mandatory");
            responseObserver.onError(new OrderException("user name and item name can't be empty!!"));
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateOrder(OrderRequest request, StreamObserver<com.proto.app.OrderStatus> responseObserver) {
        try {
             if(!request.getUserName().isEmpty()) {
                log.info("UserName : {}",request.getUserName());
             }
             if(request.getOrderId() > 0){
                 log.info("OrderId: {}",request.getOrderId());
             }
             log.info("order update request received ...");
             //build order info to store to db
             OrderInfo orderInfo = buildOrderInfo(request,
                     request.getUserType()!=null?request.getUserType():"by_user");

             OrderInfo savedOrderInfo = orderHandler
                     .findOrderInfoByUserNameAndOrderId(request.getUserName(), request.getOrderId());
             //insert to the db if not present
             if (savedOrderInfo == null) {
                 log.info("order NOT found saving to database ...");
                 savedOrderInfo = orderHandler.addOrderInfo(orderInfo);
             } else {
                 log.info("order found updating in database ... username: {}, orderId: {}",
                         savedOrderInfo.getUserName(),savedOrderInfo.getOrderId());
                 orderInfo.setOrderId(savedOrderInfo.getOrderId());
                 mergeOrderInfoDetails(orderInfo, savedOrderInfo);
                 savedOrderInfo = orderHandler.updateOrderInfo(savedOrderInfo);
                }

              //construct the status to be saved to db
              com.proto.app.OrderStatus statusCode = statusTransition(request.getStatus().name());
              //build status
              OrderStatus status = buildOrderStatus(savedOrderInfo, statusCode.getStatusCode().name());
              OrderStatus savedOrderStatus = orderHandler.addOrderStatus(status);
              if (savedOrderStatus != null) {
                 log.info("[by user] Order status - [ orderId: {} | status: {} |" +
                     " updatedBy: {} | userName: {} | updatedAt: {} ] ",
                     savedOrderStatus.getOrderId(), savedOrderStatus.getStatus(),
                     savedOrderStatus.getUpdatedBy(), savedOrderStatus.getUserName(),
                     savedOrderStatus.getEventTime());
              } else {
                 log.info("[by user] Order Status not updated");
              }
              com.proto.app.OrderStatus statusResponse = com.proto.app.OrderStatus.newBuilder()
                   .setStatusCode(getStatusCode(savedOrderStatus.getStatus()))
                   .setUpdatedBy(savedOrderStatus.getUpdatedBy())
                   .setOrderId(savedOrderStatus.getOrderId())
                   .setEventTime(savedOrderStatus.getEventTime().getTime())
                   .build();
              responseObserver.onNext(statusResponse);
              responseObserver.onCompleted();
        }catch (Exception e){
            log.error("Error occurred",e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("[Update Order] failed with exception")
                .withCause(e)
                .asException());
            responseObserver.onCompleted();
        }
    }
}
```

### grpc-client-one

Code snippet below are client side code. 

OrderClientConfig.java - This class actually creates the blocking client with the stub, the `ManagedChannelBuilder` is used set custom client configuration like retryPolicy, etc. This class is defined as configuration and the bean will be registered in the Spring context when the client starts.

```java
@Configuration
public class OrderClientConfig {

    public OrderClientConfig(GrpcServerConfig retryClient) {
        this.retryClient = retryClient;
    }

   @Bean
    OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channelFactory) {

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forTarget(targetServerAddress)
                .keepAliveTime(10, TimeUnit.SECONDS);;

        if(retryClient.negotiationType().equalsIgnoreCase("PLAINTEXT")){
            channelBuilder.usePlaintext();
        }
        if(retryClient.enabled()){
            Map<String, Object> config = this.buildServiceConfig();
            log.info("configuration: {}",config.toString());
            channelBuilder.defaultServiceConfig(config);
            channelBuilder.enableRetry();
            channelBuilder.maxRetryAttempts(5);
        }
        ManagedChannel channel = channelBuilder.build();
        return OrderServiceGrpc.newBlockingStub(channel);
    }
}
```

Additionally, the message converter configuration is included this is added since the REST endpoint uses gRPC client under the hood it requires message converter to be configured to support protobuf de-serialize otherwise will report exception when rendering the server response.

```java
@Configuration
public class WebProtoConfig extends WebMvcConfigurationSupport {

    //Below configuration is used to convert the protobuf to json
    @Bean
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        ProtobufHttpMessageConverter protobufHttpMessageConverter = new ProtobufHttpMessageConverter();
        List<MediaType> converterList = new ArrayList<>();
        converterList.add(MediaType.APPLICATION_JSON);
        converterList.add(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=ISO-8859-1"));
        protobufHttpMessageConverter.setSupportedMediaTypes(converterList);
        return protobufHttpMessageConverter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, protobufHttpMessageConverter());
    }
}
```

The controller code snippet below maps the HTTP request to gRPC client stub to fetch the server response. Note the `getStatuses()` method receives the streamed gRPC response and displays that in HTTP response.

```java
@RestController
@RequestMapping("/api")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderServiceGrpc.OrderServiceBlockingStub clientBlockingStub;
    OrderController(OrderServiceGrpc.OrderServiceBlockingStub clientBlockingStub) {
        this.clientBlockingStub = clientBlockingStub;
    }

    @PostMapping(path = "/order",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse submitOrder(@RequestBody OrderRequest orderRequest){

        logger.info("order request received...");
        if(orderRequest.getUserName() == null){
            OrderResponse response = OrderResponse
                    .newBuilder()
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST)
                    .getBody();
        }
        com.proto.app.OrderRequest req = com.proto.app.OrderRequest
                .newBuilder()
                .setDescription(orderRequest.getDescription()==null?"":orderRequest.getDescription())
                .setQuantity(orderRequest.getQuantity())
                .setItemName(orderRequest.getItemName()==null?"":orderRequest.getItemName())
                .setStatus(getStatusCode(orderRequest.getOrderStatus()==null?"RECEIVED":orderRequest.getOrderStatus()))
                .setUserName(orderRequest.getUserName())
                .setUserType(orderRequest.getUserType()==null?"by_user":orderRequest.getUserType())
                .build();

       OrderResponse response = clientBlockingStub.createOrder(req);

        return new ResponseEntity<>(response, HttpStatus.CREATED).getBody();
    }

    @PutMapping("/update")
    public com.proto.app.OrderStatus updateOrder(@RequestBody OrderRequest orderRequest){

        logger.info("order update request received...");
        if(orderRequest.getUserName() == null){
            com.proto.app.OrderStatus response = com.proto.app.OrderStatus
                    .newBuilder()
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST)
                    .getBody();
        }
        com.proto.app.OrderRequest req = com.proto.app.OrderRequest
                .newBuilder()
                .setDescription(orderRequest.getDescription()==null?"":orderRequest.getDescription())
                .setQuantity(orderRequest.getQuantity())
                .setOrderId(orderRequest.getOrderId())
                .setItemName(orderRequest.getItemName()==null?"":orderRequest.getItemName())
                .setStatus(getStatusCode(orderRequest.getOrderStatus()==null?"RECEIVED":orderRequest.getOrderStatus()))
                .setUserName(orderRequest.getUserName())
                .setUserType(orderRequest.getUserType()==null?"by_user":orderRequest.getUserType())
                .build();

        com.proto.app.OrderStatus resStatus = clientBlockingStub.updateOrder(req);

        return new ResponseEntity<>(resStatus, HttpStatus.CREATED).getBody();
    }

    /**
     * The Optional is used to handle if the requestParameter are not sent
     */
    @GetMapping("/status")
    public ResponseEntity<StreamingResponseBody> getStatuses(
            @RequestParam(name="userName") Optional<String> userName ,
            @RequestParam(name="orderId") Optional<Long> orderId){

        com.proto.app.OrderKey orderSearchKey = com.proto.app.OrderKey.newBuilder()
                .setUserName(userName.orElse(""))
                .setOrderId(orderId.orElse(0L))
                .build();

        StreamingResponseBody responseBody = statusResponse -> {
             Iterator<OrderStatus> statuses = clientBlockingStub.getOrderStatus(orderSearchKey);
             try {
                 while(statuses.hasNext()) {
                     OrderStatus status = statuses.next();
                     statusResponse.write(status.toString().getBytes(StandardCharsets.UTF_8));
                     statusResponse.flush();
                 }
             }catch (IOException e){
                 logger.error("Error exception ",e);
             }
        };
        return  ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL,"no-cache")
                .body(responseBody);
    }
//.. some of the code skipped for readability
}
```

The Client application starts at 8085 port, following are request and response info with the data rendered from gRPC server 

Below is the example to get the status for the order created by demo1 user this data is loaded during the start of server application. The server will stream the response.

```sh
curl  "http://localhost:8085/api/status?userName=demo1"
```

The output would look like below 

<img width="1049" height="832" alt="image" src="https://github.com/user-attachments/assets/9f968f2d-883c-4c64-aeec-5ebb7d64fa6c" />

To create the order 

```sh
curl -XPOST "http://localhost:8085/api/order" -d '{"userName": "test02",  "itemName": "item99",  "quantity": "99"}' -H "Content-Type: application/json"
```

The output would look like below

<img width="2462" height="392" alt="image" src="https://github.com/user-attachments/assets/e3e6a74a-0ccb-451a-b245-9fbe6f9d33b1" />

To update the order

```sh
 curl -XPUT "http://localhost:8085/api/update" -d '{ "userName": "test02", "orderId": "1378", "userType": "by_user", "status": "IN_PROGRESS", "itemName": "pencil", "quantity": "25" }' -H "Content-Type: application/json"
```

The output would look like below

<img width="2839" height="267" alt="image" src="https://github.com/user-attachments/assets/98cb7944-2369-4d04-9b5c-42fbafcde467" />


### ManagedChannelBuilder client

Code snippet below to connect to the gRPC server with java. The code below uses the executor service to call the server simulator service multiple times.

```java
public class ChannelBuilderClient {
    private static final Logger logger = LoggerFactory.getLogger(ChannelBuilderClient.class);
    public static void main(String ... args){
        String config = """
                {
                  "methodConfig": [
                    {"name": [{
                          "service": "com.proto.app.OrderService",
                          "method": "specialCaseSimulator"
                        }],
                      "retryPolicy": {
                        "maxAttempts": 4,
                        "initialBackoff": "0.1s",
                        "maxBackoff": "1s",
                        "backoffMultiplier": 2,
                        "retryableStatusCodes": ["UNAVAILABLE","DEADLINE_EXCEEDED"]
                      }
                    }
                  ]
                }
                """;

        Gson gson = new Gson();
        Map<String,?> serviceConfig = gson.fromJson(config, Map.class);

        logger.info("print retry config: {}",serviceConfig);
        // Build the channel with retry policy
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .disableServiceConfigLookUp()
                .defaultServiceConfig(serviceConfig)
                .enableRetry()
                .keepAliveTime(30,TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();

        OrderServiceGrpc.OrderServiceBlockingStub stub = OrderServiceGrpc.newBlockingStub(channel);

        logger.info("simulate server network based retry");

        try(ForkJoinPool executor = new ForkJoinPool()) {
            for (int i = 0; i < 5; i++) {
                executor.execute(() -> {
                    try {Thread.sleep(4_000);
                        }catch (InterruptedException e) {throw new RuntimeException(e);}
                    Map<String, String> reqMap = new HashMap<>();
                    reqMap.put("simType", "serverException");
                    SimRequest request = SimRequest.newBuilder().putAllSimulatorRequest(reqMap).build();
                    SimResponse response = stub.specialCaseSimulator(request);
                    logger.info("Server Response :- {}", response);
                    });
            }
            executor.shutdown();
            try {
                channel.shutdown().awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```


### JBang - Apache Camel gRPC client

Code snippet below is an example of Camel gRPC client with JBang that requests the response from the server for status of order which will stream the response.

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

package app;

//JAVA 25

//DEPS org.apache.camel:camel-bom:4.14.2@pom
//DEPS org.apache.camel:camel-grpc
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.slf4j:slf4j-api:2.0.17
//DEPS com.grpc:proto-idl:1.0.0-SNAPSHOT
//DEPS com.google.protobuf:protobuf-java:4.33.0

import org.apache.camel.*;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import org.apache.camel.spi.*;
import static org.apache.camel.builder.PredicateBuilder.*;
import com.proto.app.OrderKey;
import com.proto.app.OrderStatus;
import java.util.Date;
import java.text.SimpleDateFormat;

import static java.lang.System.*;

public class GrpcCamelClient{

    public static void main(String ... args) throws Exception{
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        out.println("Starting camel route...");
        Main main = new Main();

        String outputFormat = "[OrderId: %s, StatusCode: %s, UserName: %s, UpdatedBy: %s, EventTime: %s]";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
              //creating a request for fetching the status stream
              OrderKey orderKey = com.proto.app.OrderKey.newBuilder().setUserName("demo1").setOrderId(0L).build();

              from("timer:test?repeatCount=1&period=2500")
                  .setBody(constant(orderKey))
                  .to("grpc://localhost:9090/com.proto.app.OrderService?method=getOrderStatus&synchronous=true")
                  .split(body())
                  .log(LoggingLevel.INFO,"Recieved response : ${body}")
                  .process(exchange -> {
                    OrderStatus status = exchange.getIn().getBody(OrderStatus.class);
                      if(status != null){
                          System.out.println("RESPONSE: "+ String.format(outputFormat, status.getOrderId(), status.getStatusCode(),
                              status.getUserName(), status.getUpdatedBy(), dateFormat.format(new Date(status.getEventTime()))));
                      }
                  });
            }
        });
        main.run();
    }
}
```

To run the Camel gRPC client code, install [JBang](https://www.jbang.dev/) and use below command

```sh
jbang run camel-client/app/GrpcCamelClient.java
```

Output would look like below

<img width="2649" height="1255" alt="image" src="https://github.com/user-attachments/assets/e5b82360-59e2-4e34-a89d-c29777929849" />

### grpcui client

To install the gRPC UI follow the instruction from the [gRPCUI git repo](https://github.com/fullstorydev/grpcui). with the gRPCUI executable we can use below command to connect to the server. 

```sh
grpcui --plaintext localhost:9090
```

Once the client is connected the UI looks like below listing the service.

<img width="1642" height="1609" alt="image" src="https://github.com/user-attachments/assets/7b2bc50d-b43b-4f67-a331-26cc4ee90138" />

To fetch the statuses for the sample user

<img width="1381" height="1566" alt="image" src="https://github.com/user-attachments/assets/5b34c809-007c-42ae-9a57-b90e9bfea44b" />

The response from the server

<img width="1103" height="1626" alt="image" src="https://github.com/user-attachments/assets/c99028e9-e61e-40d4-9639-e7da33518632" />

   
