Types of API in gRPC
  1. Unary
  2. Server Streaming
  3. Client Streaming
  4. Bi-directional Streaming 

** Unary:**
  - The client will send 1 request and server sends 1 response.

** Server Streaming:**
  - The client will send 1 request and server can send 1 or more response.
  - example, client want get something in real time

** Client Streaming:**
  - The client sends 1 or multiple request and server sends 1 response.
  - example can be used uploading or updating info

** Bi-directional streaming:** 
  - The client can sends multiple request and server can send multiple response
  - The response can be in any order received in the client side.

```protobuf
service GreetingService{

    //Unary
    rpc Greet(GreetRequest) returns (GreetResponse) {};

    //server streaming
    rpc GreetManyTimes(GreetRequest) returns (stream GreetResponse) {};

    //client streaming
    rpc LongGreet(GreetRequest) returns (GreetResponse) {};

    //Bi-directional streaming
    rpc GreetEveryone(stream GreetRequest) returns (stream GreetResponse) {};
}
```

Scalability:
gRPC server side is Async, the main thread is not blocked so it can answer as many requests in parallel.
In the client side we have option to select Async or sync (Blocking).

Secruity:
- The schema based serialization provides some secrutiy.
- We need to also encrypt with SSL. 
- We can include TLS configuration between client server
- We can add interceptor for Auth to the APIs

---

Channels:
  - Channels are object that creates TCP connection between client and server.
  - To create this channel we can use the code library


In Intellij Idea we create a simple gradle project with java.

1. Add the protobuf dependencies - check the grpc/grpc-java git repo read me
2. Add the plugins for protobuf from the same in build.gradle under the existing plugin with id.
3. Add id with idea plugin with which we can use shutdown hook refer the project

- Create a directory under the created project called `proto` with a package `greeting`
- add a file greeting.proto and add the protobuf version with required config and definition

- Create a greeting.server package and create a simple code for server
- Create a greeting.client package with a simple code for client.
- The Client will use channel.

- In the intellij IDEA after adding the plugin and dependencies info, the gradle opion on right IDE includes generateProtobuff option under the others.
- Clicking it will generate the code under the build/ folder.
- The stub code will be created.

Steps:
  - Define the protobuf
  - Generate the stub code
  - Server side 
     - Create the Server side implementation with the stubcode
     - register that implemntation to the server using addService
  - Client side 
     - The arguments of the main method will be used in the client to send info to server and get response
     - For sync call, use the GreetingServiceGrpc.GreetingServiceBlockingStub
     - For async call, use the GreetingServiceGrpc.GreetingServiceFutureStub
     - The above two stub is more like calling the function directly on the server side, code looks like below
     ```
     GreetingServiceGrpc.GreetingServiceBlockingStub stub =
                 GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("User").build());
     ```


### Unary

- After updating the proto buf and the generated code the server implementation looks like below

```java
//server implementation

@Override
public void sum(SumRequest request, StreamObserver<SumResponse> streamObserver){

    int result = request.getFirstNumber()+request.getSecondNumber();
    streamObserver.onNext(SumResponse.newBuilder().setResult(result).build());
    streamObserver.onCompleted();
}
```

```java
// client implementation
private static void doAdd(ManagedChannel channel, int num1, int num2){

    SumRequest addRequest = SumRequest.newBuilder().setFirstNumber(num1).setSecondNumber(num2).build();
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =CalculatorServiceGrpc.newBlockingStub(channel);
    SumResponse response = stub.sum(addRequest);
    System.out.println(response.getResult());
}
```


### Sever Streaming

```java
//server implementation
@Override
public void prime(PrimeRequest request, StreamObserver<PrimeResponse> responseStreamObserver){

    int primeNumber = request.getInputNumber();
    int factor=2 ;
    while ( primeNumber > 1){
        if (primeNumber % factor == 0){
            responseStreamObserver.onNext(PrimeResponse.newBuilder().setResult(k).build());
            primeNumber = primeNumber/factor;
        }else{
            factor = factor+1;
        }
    }
    responseStreamObserver.onCompleted();
}

```

```java
// java implementation
private static void doPrimeFactor(ManagedChannel channel, int inputPrime) throws InterruptedException {

    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
    stub.prime(PrimeRequest.newBuilder().setInputNumber(inputPrime).build())
        .forEachRemaining(primeResponse -> { System.out.println(primeResponse.getResult()); });
}
```

### Client Streaming 

```java
//server implementation
@Override
public StreamObserver<AverageRequest> average(StreamObserver<AverageResponse> responseObserver) {

  return  new StreamObserver<AverageRequest>() {
      int sum=0;
      int numOfInput=0;
      @Override
      public void onNext(AverageRequest averageRequest) {
           sum = sum + averageRequest.getInputNumber();
           ++numOfInput;
      }
      @Override
      public void onError(Throwable throwable) {
          responseObserver.onError(throwable);
      }
      @Override
      public void onCompleted() {
          responseObserver.onNext(AverageResponse.newBuilder().setResult( (double) sum /numOfInput).build());
          responseObserver.onCompleted();
      }
  };
}
```

```java
//client implementation
private static void doAverage(ManagedChannel channel, String[] numbers) throws InterruptedException {

    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<AverageRequest> stream = stub.average(new StreamObserver<AverageResponse>() {
      @Override
      public void onNext(AverageResponse averageResponse) {
          System.out.println(averageResponse.getResult());
      }
      @Override
      public void onError(Throwable throwable) {
      }
      @Override
      public void onCompleted() {
          latch.countDown();
      }
    });

    for(int i =1; i< numbers.length; i++){
      stream.onNext(AverageRequest.newBuilder().setInputNumber(Integer.parseInt(numbers[i])).build());
    }

    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);

}

```

### Bi-directional streaming 

```java
//server implementation
 @Override
 public StreamObserver<MaxRequest> max(StreamObserver<MaxResponse> responseObserver) {
     return new StreamObserver<MaxRequest>() {
         int previousMax =Integer.MIN_VALUE;
         @Override
         public void onNext(MaxRequest maxRequest) {
           if (maxRequest.getInputNumber() > previousMax) {
               previousMax = maxRequest.getInputNumber();
               responseObserver.onNext(MaxResponse.newBuilder().setResult(previousMax).build());
           }
         }

         @Override
         public void onError(Throwable throwable) {
             responseObserver.onError(throwable);

         }

         @Override
         public void onCompleted() {
             responseObserver.onCompleted();
         }
     };
 }
```

```java
//client implementation
  private static void doMax(ManagedChannel channel,String [] numbers) throws InterruptedException {
      CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
      CountDownLatch latch = new CountDownLatch(1);
      StreamObserver<MaxRequest> stream = stub.max(new StreamObserver<MaxResponse>() {
          @Override
          public void onNext(MaxResponse maxResponse) {
              System.out.println(maxResponse.getResult());
          }
          @Override
          public void onError(Throwable throwable) {
          }
          @Override
          public void onCompleted() {
              latch.countDown();
          }
      });
      Arrays.stream(numbers).skip(1).forEach(itm ->{
          stream.onNext(MaxRequest.newBuilder().setInputNumber(Integer.parseInt(itm)).build());
      });
      stream.onCompleted(); //if this value is not added it will wait for latch to be released 3 sec
      latch.await(3,TimeUnit.SECONDS);
  }
```

### Handling error

```java
//server implemenation
@Override
public void sqrt(SqrtRequest request, StreamObserver<SqrtResponse> responseObserver) {
    int inputNumber = request.getInputNumber();
    if (inputNumber < 0){
        responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Number can't be negative")
                .augmentDescription("input number: "+inputNumber)
                .asRuntimeException());
    }
    responseObserver.onNext(SqrtResponse.newBuilder().setResult(Math.sqrt(inputNumber)).build());
    responseObserver.onCompleted();
}
```

```java
//client implementation
private static void dpSqrt(ManagedChannel channel,String[] args){
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
   SqrtResponse response = stub.sqrt(SqrtRequest.newBuilder().setInputNumber(Integer.parseInt(args[1])).build());
    System.out.println(response.getResult());
    try{
        response = stub.sqrt(SqrtRequest.newBuilder().setInputNumber(-1).build());
        System.out.println(response.getResult());
    }catch (RuntimeException e){
        System.out.println("error occured");
        e.printStackTrace();
    }
}
```

### Using Deadline context

Note the protobuf itself is different service

```java
//server implementation
@Override
public void greetWithDeadline(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
    Context context = Context.current();
    try {
        for (int i = 0; i < 3; i++) {
            if (context.isCancelled()) {
                return;
            }
            Thread.sleep(100);
            responseObserver.onNext(GreetingResponse.newBuilder().setResult("hello "+request.getFirstName()).build());
            responseObserver.onCompleted();
        }
    }catch (InterruptedException e){
        responseObserver.onError(e);
    }
}
```

```java
//client implementation
private static void doGreetWithDeadline(ManagedChannel channel){
    GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
   GreetingResponse response = stub.withDeadline(Deadline.after(3,TimeUnit.SECONDS)).greetWithDeadline(GreetingRequest.newBuilder().setFirstName("user1").build());
    System.out.println(response.getResult());
    try {
        response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadline(GreetingRequest.newBuilder().setFirstName("user2").build());
        System.out.println(response.getResult());
    }catch (StatusRuntimeException e){
        if(e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED){
            System.out.println("deadline exceeded");
        }else {
            System.out.println("exception occurred invoking greetwithdeadline");
            e.printStackTrace();
        }
    }
}
```

### Secure grpc communication with SSL
 The SSL certificates are available

 ```java
 //server defintion
 Server server = ServerBuilder.forPort(port)
                .useTransportSecurity(
                        new File("ssl/server.crt"), //private crt
                        new File("ssl/server.pem")) //private key
                //to register the implemented idl
                // we use .addService and add the new impl class
                .addService(new GreetingServerImpl())
                .build();

// consider using the existing service
 ```

 ```java
 //client Channel creation

 ChannelCredentials creds = TlsChannelCredentials
        .newBuilder()
        .trustManager(new File("ssl/ca.crt")) // public client crt
        .build();
//create a channel
ManagedChannel channel = Grpc
        .newChannelBuilderForAddress("localhost",50051, creds)
        .build();
        
 ```

 ### server reflection
check link https://github.com/grpc/grpc-java/blob/master/documentation/server-reflection-tutorial.md

With the below configuration change we can use grpcCurl or evans cli to connect to grpc server

Add the dependency in (below is for gradle, same applicable for maven as well)

```groovy
    implementation "io.grpc:grpc-services:${grpcVersion}" // added for reflection
```

In the server builder add `.addService(ProtoReflectionServiceV1.newInstance())` like below 

```java
Server server = ServerBuilder.forPort(port)
                .addService(ProtoReflectionServiceV1.newInstance()) // add for reflection
                .addService(new GreetingServerImpl())
                .build();
```

### Blog example

- The docker compose is copied, configure the Intellij to talk with the WSL2 docker deamon (optional), check the Learnings/WSL2/ directory for notes.

- Create a protobuf package named blog, add the services for the blog to perform CRUD operation. Use the Gradle tool to generateProtobuf code
- Create a blog pacakge in the src/main/java path, with two client and server package
- Create a Server class `BlogServer.java` and Service implementation `BlogServiceImpl.java` class. 
- The server class includes Server configuration, which also requires mongodb connections