package org.example.greeting.client;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    //this is the client code.
    public static void main(String[] args) throws InterruptedException {

        if (args.length == 0){
            System.out.println("Requires one argument to work..");
            return;
        }
        //create a channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext() //not using SSL/TLS now
                .build();

        // call server using the hello service
        switch (args[0]){
            //step 1
            case "greet" -> doGreet(channel);
            //step 2
            case "greet_many_times" -> doGreetManyTimes(channel);
            //step 3
            case "long_greet" -> doLongGreet(channel);
            //step 4
            case "greet_everyone" -> doGreetEveryone(channel);
            case "greet_with_deadline" -> doGreetWithDeadline(channel);
            default -> System.out.println("invalid argument: "+ args[0]);
        }

        //close the channel
        System.out.println("client shutdown channel");
        channel.shutdown();
    }



    //Step 1
    private static void doGreet(ManagedChannel channel){
        System.out.println("In doGreet block");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub =
                 GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("T!!").build());

        System.out.println("Greetings "+response.getResult());

    }

    //Step 2
    private static void doGreetManyTimes(ManagedChannel channel){
        System.out.println("In doGreetManyTimes block");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub =
                GreetingServiceGrpc.newBlockingStub(channel);
        // we are reading from the stream
        stub.greetManyTimes(GreetingRequest.newBuilder().setFirstName("T!!").build())
                .forEachRemaining(response -> {
                    System.out.println(response.getResult());
                });
    }

    private static void doLongGreet(ManagedChannel channel) throws InterruptedException {
        System.out.println("In doLongGreet block");
        //since this is a streaming, we need to use Async call

        GreetingServiceGrpc.GreetingServiceStub stub =
                GreetingServiceGrpc.newStub(channel);

        List<String> inputs = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(inputs,"user1","user2","user3");

        StreamObserver<GreetingRequest> stream = stub.longGreet(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse response) {
                System.out.println(response.getResult());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();

            }
        });

        for (String input : inputs ){
            stream.onNext(GreetingRequest.newBuilder().setFirstName(input).build());
        }

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetEveryone(ManagedChannel channel) throws InterruptedException {
        System.out.println("in doGreetEveryone block");

        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetingRequest> stream = stub.greetEveryone(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(GreetingResponse greetingResponse) {
                System.out.println(greetingResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.asList("user1","user2","user3").forEach(input -> {
            stream.onNext(GreetingRequest.newBuilder().setFirstName(input).build());
        });
        stream.onCompleted();
        latch.await(3,TimeUnit.SECONDS);
    }

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
}
