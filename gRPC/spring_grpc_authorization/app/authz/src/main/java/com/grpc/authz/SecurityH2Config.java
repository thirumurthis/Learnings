package com.grpc.authz;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class SecurityH2Config {

    /**
     * ROOT BYPASS: Tells Spring Security to completely ignore the H2 console.
     * This ensures the H2 login page, internal sub-pages, frames, and assets
     * (like default.css) load instantly without matching any security chains.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console());
    }
}

