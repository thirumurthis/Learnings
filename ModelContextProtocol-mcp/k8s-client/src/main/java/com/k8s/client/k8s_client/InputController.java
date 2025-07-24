package com.k8s.client.k8s_client;

import io.modelcontextprotocol.client.McpSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/input")
public class InputController {

    private static final Logger log = LoggerFactory.getLogger(InputController.class);

    private final ChatClient chatClient;
    private final List<McpSyncClient> mcpSyncClients;

    public InputController(ChatClient.Builder chatClientBuilder,
                           ToolCallbackProvider toolCallbackProvider,
                           List<McpSyncClient> mcpSyncClients){
        this.chatClient = chatClientBuilder.build();
        this.mcpSyncClients = mcpSyncClients;

        printToolInfoFromServer(toolCallbackProvider);

    }

    @PostMapping("/in")
    public String input(@RequestBody String inputData){
        log.info("input data received - {}",inputData);
        return chatClient.prompt()
                .user(inputData)
                .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .call()
                .content();
    }

    private static void printToolInfoFromServer(ToolCallbackProvider toolCallbackProvider) {
        List<ToolCallback> toolCallbacks = List.of(toolCallbackProvider.getToolCallbacks());
        if(toolCallbacks.isEmpty()){
            log.warn("No tools found");
        } else {
            System.out.println("**************************************");
            for (ToolCallback toolCallback : toolCallbacks){
                ToolDefinition toolDefinition = toolCallback.getToolDefinition();
                System.out.println("Tool Name: "+toolDefinition.name());
                System.out.println(" |___ Description: "+toolDefinition.description());
                System.out.println(" |___ Input Schem: "+toolDefinition.inputSchema());
                System.out.println("__________________________________");
            }
            System.out.println("**************************************");
        }
    }
}
