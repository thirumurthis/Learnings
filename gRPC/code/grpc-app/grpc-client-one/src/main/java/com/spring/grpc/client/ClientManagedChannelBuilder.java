
package com.spring.grpc.client;

import com.google.gson.Gson;
import com.proto.app.OrderServiceGrpc;
import com.proto.app.SimRequest;
import com.proto.app.SimResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ClientManagedChannelBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ClientManagedChannelBuilder.class);
    public static void main(String ... args){

        String config = """
                {
                  "methodConfig": [
                    {
                      "name": [
                        {
                          "service": "grpcretryexample.UserService",
                          "method": "GetUser"
                        }
                      ],
                      "retryPolicy": {
                        "maxAttempts": 4,
                        "initialBackoff": "0.1s",
                        "maxBackoff": "1s",
                        "backoffMultiplier": 2,
                        "retryableStatusCodes": [
                          "UNAVAILABLE",
                          "DEADLINE_EXCEEDED"
                        ]
                      }
                    }
                  ]
                }
                """;

        Gson gson = new Gson();
        Map<String, ?> serviceConfig = gson.fromJson(config, Map.class);

        logger.info("config: {}",serviceConfig);
        // Build the channel with retry policy
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .disableServiceConfigLookUp()
                //.defaultServiceConfig(buildServiceConfig())
                .defaultServiceConfig(serviceConfig)
                .enableRetry()
                .keepAliveTime(30,TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();

        OrderServiceGrpc.OrderServiceBlockingStub stub =
                OrderServiceGrpc.newBlockingStub(channel);

        logger.info("simulate server network based retry");


        SimResponse resp = SimResponse.newBuilder().setSimulatorResponse("").build();

        ForkJoinPool executor = new ForkJoinPool();


        for (int i = 0; i < 1; i++) {
            executor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(4_000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Map<String, String> reqMap = new HashMap<>();
                            reqMap.put("simType","serverException");
                            reqMap.put("num","2");
                            SimRequest req = SimRequest.newBuilder().putAllSimulatorRequest(reqMap).build();
                            SimResponse resp = stub.specialCaseSimulator(req);
                            logger.info("response :- {}",resp);
                        }
                    });
        }
        executor.awaitQuiescence(100, TimeUnit.SECONDS);
        executor.shutdown();
        try {
            channel.shutdown().awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


/*
        try {
            resp = stub.specialCaseSimulator(req);
            logger.info("Response: {}",resp.getSimulatorResponse());
        } catch(Exception exe) {
            logger.error("Exception throw during api access", exe);
        } finally {
            channel.shutdown();
        }
 */
    }

    public static Map<String, Object> buildServiceConfig() {

        //https://stackoverflow.com/questions/73172112/configuring-retry-policy-for-grpc-request
        Map<String, Object> svcConfig = Map.of("loadBalancingConfig",
                List.of(Map.of("weighted_round_robin", Map.of()),
                        Map.of("round_robin", Map.of()),
                        Map.of("pick_first", Map.of("shuffleAddressList", true))),
                "methodConfig", List.of(
                        Map.of("name", List.of(
                                        Map.of("service", "com.proto.app.OrderService",
                                                "method", "specialCaseSimulator"
                                        )
                                ),
                                "waitForReady", true,
                                "retryPolicy",
                                Map.of(
                                        "maxAttempts",4.0,
                                        "initialBackoff", "0.1s",
                                        "backoffMultiplier",2.0,
                                        "maxBackoff", "1s",
                                        "retryableStatusCodes", List.of("UNAVAILABLE","DEADLINE_EXCEEDED")
                                )
                        )
                )
        );

        logger.info("config map {}",svcConfig);
        return svcConfig;
    }
}

/*
public class ClientManagedChannelBuilder {

    static final String PLAIN_TEXT = "PLAINTEXT";
    private final ManagedChannelBuilder<?> channelBuilder;

    private static final Logger logger = LoggerFactory.getLogger(ClientManagedChannelBuilder.class.getName());

    public ClientManagedChannelBuilder(ManagedChannelBuilder<?> channelBuilder, GrpcProperties properties) {
        this.channelBuilder = channelBuilderConfiguration(channelBuilder,properties);
    }

    private ManagedChannelBuilder<?> channelBuilderConfiguration(final ManagedChannelBuilder<?> channelBuilder,
                                                                 final GrpcProperties properties){

        if(properties != null) {
            channelBuilder.defaultServiceConfig(getPropertiesToMap(properties));
            if (properties.getNegotiationType() != null &&
                    properties.getNegotiationType().equalsIgnoreCase("PLAIN_TEXT")) {
                channelBuilder.usePlaintext();
            }
        }
        return channelBuilder;
    }

    public static ClientManagedChannelBuilder target(String target, GrpcProperties properties){
        return new ClientManagedChannelBuilder(ManagedChannelBuilder.forTarget(target),properties);
    }

    public static ClientManagedChannelBuilder address(String name, int port, GrpcProperties properties){
        return new ClientManagedChannelBuilder(ManagedChannelBuilder.forAddress(name,port), properties);
    }

    private Map<String,Object> getPropertiesToMap(GrpcProperties properties){
        Map<String,Object> map = new HashMap<>();
        map.put("retry",properties.isEnableRetry());

        return map;
    }

    public ManagedChannel build(){
        return channelBuilder.build();
    }
}
*/
