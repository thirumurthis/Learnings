package com.spring.grpc.client;

import com.proto.app.OrderServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class OrderClientConfig {

    private static Logger log = LoggerFactory.getLogger(OrderClientConfig.class.getName());

    @Bean
    OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channels) {
        return OrderServiceGrpc.newBlockingStub(channels.createChannel("local"));
    }

    //private final OrderHandlerGrpc.OrderHandlerStub stub;
    //private final OrderHandlerGrpc.OrderHandlerBlockingStub blockingStub;

    /*
    public OrderClientConfig(OrderHandlerGrpc.OrderHandlerStub stub, OrderHandlerGrpc.OrderHandlerBlockingStub blockingStub) {
        log.info("Constructing autowiring of stubs stub.IsNull? - {}; blockingStStub.IsNull? {}",
                stub==null, blockingStub==null);
        this.stub = stub;
        this.blockingStub = blockingStub;
    }
    */

    /*
    @Bean
    OrderGrpcClient client(GrpcProperties properties){
        return new OrderGrpcClientImpl("0.0.0.0:9095",properties);
    }

    @Bean
    @Primary
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufJsonFormatHttpMessageConverter();
    }
     */
    //@Bean
    //OrderGrpcClient client(GrpcProperties properties){
    //    return  new OrderGrpcClientImpl(stub,blockingStub,properties);
    //}

}
