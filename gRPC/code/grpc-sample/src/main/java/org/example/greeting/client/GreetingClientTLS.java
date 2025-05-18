package org.example.greeting.client;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.ChannelCredentials;
import io.grpc.Deadline;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClientTLS {

    //this is the client code.
    public static void main(String[] args) throws InterruptedException, IOException {

        if (args.length == 0){
            System.out.println("Requires one argument to work..");
            return;
        }
        ChannelCredentials creds = TlsChannelCredentials
                .newBuilder()
                .trustManager(new File("ssl/ca.crt"))
                .build();
        //create a channel
        ManagedChannel channel = Grpc
                .newChannelBuilderForAddress("localhost",50051, creds)
                .build();

        // call server using the hello service
        switch (args[0]){
            //step 1
            case "greet" -> doGreet(channel);
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
}
