package com.k8s.mcp.k8s.config;

import com.k8s.mcp.k8s.service.K8sService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerToolConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(K8sService k8sService) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(k8sService)
                .build();
    }
}
