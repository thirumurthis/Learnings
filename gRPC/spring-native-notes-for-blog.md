# Spring Native gRPC 

In this article have created simple Client Server application with Spring boot and useing only native Spring gRPC.

As a pre-requsites recommend basic understanding of gRPC protocol to follow. For more info refer the [gRPC introduction and overview](https://grpc.io/docs/what-is-grpc/introduction). 

The rRPC supports different APIs which are listed below,

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

### About the application

- The Spring gRPC usage demonstrated in the project is an client server application which doesn't use any third-party gRPC library.
- The project is a multi-module maven project, it contains below child modules. The parent pom.xml properties includes the jar dependencies version.
    1. proto-idl - This module defines the service in the protobuf that will be uses by the server and client. The maven build will generate java stub code and generates the code as jar.
    2. grpc-server - This module implements the service stubs to handle the client requests. The proto-idl jar dependency addedin the pom.xml. The `@GrpcService` annotated class will register the services when server application starts.
    3. grpc-client-one - This module creates the clients using the stubs, created a blocking or synchronized client. The proto-idl jar dependency is added in the pom.xml. The client is configured with retrypolicy configuration defined in application.yaml.
 
#### Modules in the project
- proto-idl service:
    - This service defined in the protobuf file creates an order, update the order, streams the order status and also includes a method to simulate netowrk delay and random exception.
    - The maven build `mvn clean install` will package the generated code to jar file. This generated code includes stubs to be implemented by server/client.
    - Along with the protobug generated code, a java AppConstants class is also packaged in the jar and used in client and server module.

- grpc-server application:
    - The order state is stored in a H2 database which is configured to store the data in file under `data` folder in the project root. The database schema and ddl script with sample data is placed under the `resource` folder.
    - When the application starts the H2 db will be created if the database doesn't exist, and the script will load some sample data. The database script is idompotent.
    - The DTO layer under the `com.spring.grpc.server.dto` defines the entity for order and status which also includes simple builder pattern.
    - The `OrderHandler` component class has the necessary service to access the database and it is used in the service impmentation.
    - The service implementation is defined in `com.spring.grpc.server.service.OrderService`. This class is annotated with `@GrpcService`, which will load the service to the spring context on startup. The implementation looks like below
      
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
    - A REST API is used to access the client application, which under the hood uses the gRPC client created using the stub.
    - The `com.spring.grpc.client.OrderController` defines the entry point using the `@RestController`.
    - The class `com.spring.grpc.client.OrderClientConfig` creates client with `ManagedchannelBuilder` only to configure the retrypolicy.

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
      
    - To use the default gRPC client configuration provided by the Spring gRPC we can define the `@Bean` configuration like below

      ```java
          @Bean
          OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channels) {
              return OrderServiceGrpc.newBlockingStub(channels.createChannel("local"));
         }
      ```

    - The `ChannelBuilderClient.java` class is an example to use `ManagedChannelBuilder` client which is without using Spring gRPC.
    - There are additional bean configured to support the protobuf de-serialization.
- The project also includes a JBang based client which uses Apache Camel gRPC to connect to the server. The example to stream the order status is demonstrated in the code.
- Once the server application is started we can use `gRPCUI` as a client to connect to the server.

### Complete Code
TODO - Add URL link

### proto-idl app

- Below code snipet is the rpc service used to manage the order for the application. The protobug supports inheritance so the files is split into different protobuf file and the protobuf files are imported. This is an example shows how to group messages based on domain or functionality.

app.proto file

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

sim.proto file

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

#### grpc-server
 - Below is the server implementation for a create order and update order service using the generated stub

```java
@GrpcService
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {

    public OrderService(OrderHandler orderHandler) {
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
                //OrderDetails orderDetails = buildOrderDetails(savedOrderInfo);

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

//.... there are supporting methods in this class refer the git repo for complete implementation
}
```

#### grpc-client-one

Below is the code snipet of the client code 

```java

```

#### grpcui client
 
   
