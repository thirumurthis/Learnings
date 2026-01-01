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
    2. grpc-app-server - This module implements the service stubs to handle the client requests. The proto-idl jar dependency addedin the pom.xml. The `@GrpcService` annotated class will register the services when server application starts.
    3. grpc-client-one - This module creates the clients using the stubs, created a blocking or synchronized client. The proto-idl jar dependency is added in the pom.xml. The client is configured with retrypolicy configuration defined in application.yaml.
 
#### Modules in the project
- proto-idl service:
    - This service defined in the protobuf file creates an order, update the order, streams the order status and also includes a method to simulate netowrk delay and random exception.
    - The maven build `mvn clean install` will package the generated code to jar file. This generated code includes stubs to be implemented by server/client.

- grpc-app-server application:
    - The order state is stored in a H2 database which is configured to store the data in file under `data` folder in the project root. The database schema and ddl script with sample data is placed under the `resource` folder.
    - When the application starts the H2 db will be created if the database doesn't exist, and the script will load some sample data. The database script is idompotent.
    - The DTO layer under the `com.spring.grpc.data.dto` defines the entity for order and status which also includes simple builder pattern.
    - The service implementation is defined in `com.spring.grpc.server.service.OrderService`. This class is annotated with `@GrpcService`, which will load the service to the spring context on startup.

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

### proto-idl app
- The service to manage the order is defined in the protobuf and service looks like below. The supporting message are split into different protobuf and the parent proto is imported. This is an example how we could group the message based on the domain if needed.

```
syntax = "proto3";

package app;

option java_package = "com.proto.app";
option java_multiple_files = true;

import "app/order.proto";

service OrderService {

  rpc createOrder(OrderRequest) returns (OrderResponse) {};
  rpc updateOrder(OrderRequest) returns (OrderStatus) {};
  rpc getOrderStatus(OrderKey) returns (stream OrderStatus) {};
  rpc specialCaseSimulator(SimRequest) returns (SimResponse){};
}

message SimRequest{
  map<string,string> simulatorRequest = 1;
}

message SimResponse{
  string simulatorResponse = 1;
}
```

### grpc-app-server



### grpc-client-one

### grpcui client
 
   
