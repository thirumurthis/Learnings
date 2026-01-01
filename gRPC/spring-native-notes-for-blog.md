# Spring Native gRPC 

In this article have created simple Client Server application with Spring boot and useing only native Spring gRPC.

As a pre-requsites recommend basic understanding of gRPC protocol to follow. For more info refer the [gRPC introduction and overview](https://grpc.io/docs/what-is-grpc/introduction). 


The different APIs supported by the gRPC protocol is listed below, we would use these to define the service in the protobuf file. 

1. Unary - For single client request server sends single response. 
2. Server Streaming - For single client request server send one or more response. 
3. Client Streaming - For one or more client request server sends single response. 
4. Bi-directional Streaming - Client sends more request server responds with multiple responses.

The service representation of each API looks like below 

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

### Key points aboutn the application

- The client server application in this example uses Spring Boot and Spring gRPC.
- The application is a maven multi-module project, which has three child modules
    1. proto-idl - This module defines the service in the protobuf that will be uses by the server and client. The pom.xml in this module is updated to generate java stub code and package to jar.
    2. grpc-app-server - This module implements the stubs for server. The proto-idl jar is added in the pom.xml as dependency. The @GrpcService is used to register the beans when server application starts.
    3. grpc-client-one - This module implements the stubs to create client. The proto-idl jar is also added in the pom.xml as dependency. The stubs are created used to client and configured with retrypolicy configuration.
 - proto-idl service:
    - The service defined in protobuf creates an order, update the order status, streams the status of an order and includes service to simulate netowrk delay and random exception.
 - grpc-app-server application:
    - The server code uses H2 in memory database (store the data as file), the schema and ddl configuration are under resource folder.
    - The H2 database will be created if the database doesn't exists, and the script is idompotent. Some sample data is loaded during the server application startup.
    - The dto defined for order and status also includes simple builder pattern.
 - grpc-client-one application:
    - The client code uses REST API wrapped around the gRPC stub client. The entry point is via OrderController.
    - The ChannelBuilderClient.java class is an example to use ManagedChannelBuilder based client which is without using Spring gRPC.
    - There are additional bean configured to support the protobuf de-serialization.
- The project also includes a JBang based client which uses Apache Camel gRPC to connect to the server. The example to stream the order status is demonstrated in the code.
- Once the server application is started we can use `gRPCUI` as a client to connect to the server.

### proto-idl app
 - Todo: add some key points

### grpc-app-server

### grpc-client-one

### grpcui client
 
   
