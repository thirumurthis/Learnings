package com.spring.grpc.server.service;

import com.spring.grpc.hello.HelloReply;
import com.spring.grpc.hello.HelloRequest;
import com.spring.grpc.hello.SimpleGrpc;
import io.grpc.stub.StreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl extends SimpleGrpc.SimpleImplBase {

    private static final Log log = LogFactory.getLog(HelloServiceImpl.class);

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        log.info("Hello " + req.getName());
        if (req.getName().startsWith("error")) {
            throw new IllegalArgumentException("Bad name: " + req.getName());
        }
        if (req.getName().startsWith("internal")) {
            throw new RuntimeException();
        }
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello ==> " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void streamHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        log.info("Hello " + req.getName());
        int count = 0;
        while (count < 10) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello(" + count + ") ==> " + req.getName()).build();
            responseObserver.onNext(reply);
            count++;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e);
                return;
            }
        }
        responseObserver.onCompleted();
    }
}
