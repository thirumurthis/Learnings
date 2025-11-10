package com.spring.grpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.grpc")
public class GrpcProperties {

    private static Logger log = LoggerFactory.getLogger(GrpcProperties.class.getName());

    private boolean enableRetry;

    //PLAIN_TEXT or TLS/SSL
    private String negotiationType;


    public boolean isEnableRetry() {
        return enableRetry;
    }

    public void setEnableRetry(boolean enableRetry) {
        this.enableRetry = enableRetry;
    }

    public String getNegotiationType() {
        log.info("property resolution.. getNegotiationType {}",negotiationType);
        return negotiationType;
    }

    public void setNegotiationType(String negotiationType) {

        this.negotiationType = negotiationType;
    }


}
