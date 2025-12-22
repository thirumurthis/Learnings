package com.spring.grpc.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.grpc.client.method-config")
public record GrpcServerConfig(boolean enabled, List<Map<String,String>> name,
                               RetryPolicy retryPolicy, String negotiationType
) { }
