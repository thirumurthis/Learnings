package com.grpc.order.lib.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("orderhandler")
@EnableScheduling
@AutoConfiguration
@ComponentScan(basePackageClasses = OrderServiceAutoConfig.class)
public class OrderServiceAutoConfig {

    private final static Logger log = LoggerFactory.getLogger(OrderServiceAutoConfig.class.getName());

    /*
    @Bean
    OrderServiceImpl getGrpcOrderService(OrderExecutor orderExecutor){

        log.info("order executor bean isNull? {}",orderExecutor==null);
        return new OrderServiceImpl(orderExecutor);
    }
    */

    /*
    @Bean
    OrderHandlerGrpc.OrderHandlerImplBase getOrderGrpcService(OrderExecutor orderExecutor){
        return new OrderServiceImpl(orderExecutor);
    }*/

    /*@ConditionalOnMissingBean
    @ConditionalOnBean(GrpcServerFactory.class)
    @Bean
    public GrpcServerLifecycle grpcServerLifecycle(
            final GrpcServerFactory factory,
            final GrpcServerProperties properties,
            final ApplicationEventPublisher eventPublisher) {
        return new GrpcServerLifecycle(factory, properties.getShutdownGracePeriod(), eventPublisher);
    }*/
}
