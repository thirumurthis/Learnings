package org.example.greeting.server;

import com.google.j2objc.annotations.ObjectiveCName;
import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.util.stream.Stream;

// The GreetingServiceImplBase is abstract class
// This implementation needs to be registered in the grpc server
public class GreetingServerImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    //Step 1
    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseStreamObserver){
        // the onNext() is simply sending response to the client
        responseStreamObserver
                .onNext(GreetingResponse.newBuilder().setResult("Hello "+ request.getFirstName()).build());

        // the onCompleted tells the communication between client and server is completed
        responseStreamObserver.onCompleted();
    }

    //Step 2
    @Override
    public void greetManyTimes(GreetingRequest request, StreamObserver<GreetingResponse> responseStreamObserver){
        GreetingResponse response = GreetingResponse.newBuilder()
                .setResult("Hello "+ request.getFirstName()).build();

        for(int i =0; i< 10; i++){
            responseStreamObserver.onNext(response);
        }
        responseStreamObserver.onCompleted();
    }

    //Step 3 - stream of request from a client will be concatenated and sent back with hello prefix
    // we need to return observer
    @Override
    public StreamObserver<GreetingRequest> longGreet(StreamObserver<GreetingResponse> responseStreamObserver){

        StringBuffer sb = new StringBuffer();

        return new StreamObserver<GreetingRequest>() {
            @Override
            public void onNext(GreetingRequest request) {
                sb.append("hello ");
                sb.append(request.getFirstName());
                sb.append("!\n");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occurred "+throwable.toString());
            }

            @Override
            public void onCompleted() {
                System.out.println("in onCompleted");
                responseStreamObserver.onNext(GreetingResponse.newBuilder().setResult(sb.toString()).build());
                responseStreamObserver.onCompleted();

            }
        };

    }

    //Step 4 - bidirectional stream
    @Override
    public StreamObserver<GreetingRequest> greetEveryone(StreamObserver<GreetingResponse> responseStreamObserver){


        return new StreamObserver<GreetingRequest>() {
            @Override
            public void onNext(GreetingRequest request) {
                System.out.println("bidirectional - server onNext invoked ");
                responseStreamObserver.onNext(GreetingResponse.newBuilder().setResult("Hello "+request.getFirstName()).build());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                System.out.println("bidirectional - server on complete invoked");
                responseStreamObserver.onCompleted();

            }
        };
    }

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
}
