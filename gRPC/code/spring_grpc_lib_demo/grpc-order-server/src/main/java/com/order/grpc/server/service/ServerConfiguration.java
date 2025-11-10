package com.order.grpc.server.service;

import com.grpc.order.lib.service.OrderProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {

    @Bean
    OrderProcessor orderExecutor(){
        return new OrderProcessorImpl();
    }
}
