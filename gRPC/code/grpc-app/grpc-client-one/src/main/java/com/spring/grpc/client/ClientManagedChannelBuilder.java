/*
package com.spring.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
