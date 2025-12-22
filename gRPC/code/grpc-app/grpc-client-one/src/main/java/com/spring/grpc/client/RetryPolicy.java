package com.spring.grpc.client;


import java.util.List;

//@ConfigurationProperties(prefix = "app.grpc.client.method-config.retry-policy")
public record RetryPolicy(double maxAttempts, String initialBackoff,
      String maxBackoff, double  backoffMultiplier, List<String> retryableStatusCodes) {
}
