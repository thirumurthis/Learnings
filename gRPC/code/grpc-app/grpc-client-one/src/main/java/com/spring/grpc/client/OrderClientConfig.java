package com.spring.grpc.client;

import com.proto.app.OrderServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class OrderClientConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderClientConfig.class.getName());

    private final GrpcServerConfig retryClient;

    @Value("${spring.grpc.client.channels.local.address}")
    private String targetServerAddress;

    //@Value("${app.grpc.client.negotiationType}")
    //private String negotiationType;

    public OrderClientConfig(GrpcServerConfig retryClient) {
        this.retryClient = retryClient;
    }
    /*
    @Bean
    OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channels) {
        log.info("enable the client channel");
        return OrderServiceGrpc.newBlockingStub(channels.createChannel("local"));
    }
     //*/
//*
    @Bean
    OrderServiceGrpc.OrderServiceBlockingStub stub(GrpcChannelFactory channelFactory) {

        Map<String,Object> grpcProperties = new HashMap<>();

        log.info(">>>>> target server address {}",targetServerAddress);
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forTarget(targetServerAddress)
                .keepAliveTime(10, TimeUnit.SECONDS);;

        if(retryClient.negotiationType().equalsIgnoreCase("PLAINTEXT")){
            channelBuilder.usePlaintext();
        }
        if(retryClient.enabled()){
            Map<String, Object> config = this.buildServiceConfig();
            log.info("configuration: {}",config.toString());
            //channelBuilder.defaultServiceConfig(config);
            channelBuilder.enableRetry();
            channelBuilder.maxRetryAttempts(5);
        }

        ManagedChannel channel = channelBuilder.build();

        //ChannelBuilderOptions options = ChannelBuilderOptions.defaults()
        //        .withCustomizer(GrpcChannelBuilderCustomizer.)
        //        ;
        //ManagedChannel channel = channelFactory.createChannel("localhost", options);
        return OrderServiceGrpc.newBlockingStub(channel);
    } //*/

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
/*
    public Map<String, Object> buildServiceConfig() {

        //https://stackoverflow.com/questions/73172112/configuring-retry-policy-for-grpc-request
        Map<String, Object> svcConfig = Map.of("loadBalancingConfig",
                List.of(Map.of("weighted_round_robin", Map.of()),
                        Map.of("round_robin", Map.of()),
                        Map.of("pick_first", Map.of("shuffleAddressList", true))),
                "methodConfig", List.of(
                        Map.of("name", List.of(
                                              Map.of("service", retryClient.service(),
                                                     "method", retryClient.method()
                                              )
                                ),
                         "waitForReady", true,
                         "retryPolicy",
                          Map.of(
                             "maxAttempts",retryClient.maxAttempts(),
                             "initialBackoff", retryClient.initialBackoff(),
                             "backoffMultiplier",retryClient.backoffMultiplier(),
                             "maxBackoff", retryClient.maxBackoff(),
                             "retryableStatusCodes", retryClient.retryableStatusCodes()
                          )
                    )
                )
            );
        log.info("config map {}",svcConfig);
        return svcConfig;
    }

 */

    public Map<String, Object> buildServiceConfig() {

        //https://stackoverflow.com/questions/73172112/configuring-retry-policy-for-grpc-request
        Map<String, Object> svcConfig = Map.of("loadBalancingConfig",
                List.of(Map.of("weighted_round_robin", Map.of()),
                        Map.of("round_robin", Map.of()),
                        Map.of("pick_first", Map.of("shuffleAddressList", true))),
                "methodConfig", List.of(
                        Map.of("name", List.of(retryClient.name().stream().toList())),
                                "waitForReady", true,
                                "retryPolicy",
                                Map.of(
                                        "maxAttempts",retryClient.retryPolicy().maxAttempts(),
                                        "initialBackoff", retryClient.retryPolicy().initialBackoff(),
                                        "backoffMultiplier",retryClient.retryPolicy().backoffMultiplier(),
                                        "maxBackoff", retryClient.retryPolicy().maxBackoff(),
                                        "retryableStatusCodes", retryClient.retryPolicy().retryableStatusCodes()
                                )
                        )
                );
        log.info("config map {}",svcConfig);
        return svcConfig;
    }
}
