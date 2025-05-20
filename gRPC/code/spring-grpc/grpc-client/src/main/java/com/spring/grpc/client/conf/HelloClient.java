package com.spring.grpc.client.conf;

import com.spring.grpc.hello.SimpleGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Component
public class HelloClient {


    @Bean
    SimpleGrpc.SimpleBlockingStub stub(GrpcChannelFactory channels) {
       // return SimpleGrpc.newBlockingStub(channels.createChannel("0.0.0.0:9090"));
        return SimpleGrpc.newBlockingStub(channels.createChannel("local"));
    }
}
