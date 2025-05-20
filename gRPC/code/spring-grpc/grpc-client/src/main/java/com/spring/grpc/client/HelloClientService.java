package com.spring.grpc.client;


import com.spring.grpc.client.conf.HelloClient;
import com.spring.grpc.hello.HelloReply;
import com.spring.grpc.hello.HelloRequest;
import com.spring.grpc.hello.SimpleGrpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloClientService {

    private final SimpleGrpc.SimpleBlockingStub clientStub;

    HelloClientService(SimpleGrpc.SimpleBlockingStub  clientStub){
        this.clientStub = clientStub;
    }

    @GetMapping("/call")
    public String sayHello(){
       HelloReply reply = clientStub.sayHello(HelloRequest.newBuilder().setName("ClientUser1").build());
        return String.format("{ \"serverResponse\" : \"%s\"}",reply.getMessage() );
    }
}
