package com.spring.grpc.client;

import com.grpc.order.lib.OrderHandlerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class OrderClientConfig {

    private static Logger log = LoggerFactory.getLogger(OrderClientConfig.class.getName());

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

    @Bean
    OrderGrpcClient client(GrpcProperties properties){
        return new OrderGrpcClientImpl("0.0.0.0:9095",properties);
    }

    @Bean
    @Primary
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufJsonFormatHttpMessageConverter();
    }
    //@Bean
    //OrderGrpcClient client(GrpcProperties properties){
    //    return  new OrderGrpcClientImpl(stub,blockingStub,properties);
    //}

}
