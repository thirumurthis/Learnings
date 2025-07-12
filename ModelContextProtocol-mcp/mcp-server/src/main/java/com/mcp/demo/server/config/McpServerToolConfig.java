package com.mcp.demo.server.config;

import com.mcp.demo.server.service.ItemService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerToolConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(ItemService itemService) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(itemService)
                .build();
    }
}
