- when using REST, 
   - In Spring we use RestTemplate
   - In Java HttpClient or URL connection
   
- In gRPC there are abstraction layer around the transport layer to which we can work on. 

- Create a sperate project other than the server code.

```java 
package com.grpc.example.client;

import io.grpc.ManagedChannelBuilder;


public class ExampleGrpcClient{

public static void main(String ... args){
    // In order not to deal with the low level transport layer
    // there is an abstraction which we can work with
    
    // in order to connect to the server channel we created 
    // we build a channel and connect to the service we created 
    // we use ManagedChannelBuilder 
    
    // we can use the SSL down here, but for demonstration purpose
    // using plaintext
    // .loadBalancerFactory() // is client side loadbalance strategy
    //     for this we need to specify the strategy like roundrobin, etc.
    // in order to know where the service are (i.e. its IP address)
    // we need to implement the .nameResolverFactory(), given the logical name
    //  .nameResolveFactory() - this will resolve this ip address
    // we can use Eureka if we use it for resolve service
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",8080)
    .usePlaintext(true)
    .build();
    
    // we need to generate and create the instance of the client stub
    // and pass the channel connection to it.
    // the client stub has option newBlockingStub(), newFutureStub(), newStub()
    // Note: everything on the server side is async, it is upto the client
    // to decide which one to use.
    // newStub() - is fully async stub, which listens to the fully async stream
    // newFutureStub() - get the future and wait for the result.
    // newBlockingStub() - sync stub.
    
    WishServiceGrcp.WishServiceBockingStub stub = WishServiceGrpc.newBlockingStub(channel);
    
    // no need to write the client library
    HelloResponse response = stub.wish(HelloRequest.newBuilder()
    .setUserName("Tom")
    .addWishes("Hello")
    .addWishes("welcome")
    .putItemsPicked("item1","coin")
    .build());
    
    //print the response
    System.out.println(response);
}
