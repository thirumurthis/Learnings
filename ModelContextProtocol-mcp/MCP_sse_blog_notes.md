## Spring AI MCP Client Server communication with SSE transport 

In this blog have tried to use Spring AI MCP with SSE transport to manage local KinD Kubernetes cluster. This is a follow up of previous blog [Spring AI MCP Client Server using STDIO transport](https://www.linkedin.com/pulse/mcp-client-server-use-local-llm-thirumurthi-s-5uc5c/). 

The Spring AI MCP Server code shown below includes set of functionality annotated with `@Tool` in service layer. These functionality will use Kubernetes Java client to manage the local KinD cluster resources. Like in previous blog have used the Ollama service running in docker with llama-3.2 model. And accessible in `http://localhost:11434`.

Pre-requisites:
  - Docker Desktop
  - Kind CLI
      - KinD cluster created using `kind create cluster --name sample`
  - Java IDE (InteliJ Idea community edition)

### Overview

With `STDIO` transport when running the MCP client the server is configured to run with java command, but in case of `SSE` transport the MCP server runs as standalone application (or accessed using HTTP). Spring provides OAuth configuration to secure the MCP server which is not explained here. For `SSE` Spring AI by default exposes the endpoint at `/sse`.

<img width="1140" height="651" alt="image" src="https://github.com/user-attachments/assets/97970942-1bd5-4b8d-834e-1434c7909da4" />

Info:
  - With KinD CLI, the kube config file will be updated and placed in default .kube folder. This kube config file is reqired by the Kubernetes Java client to connect and manage the resources. When running the MCP Server from IDE like IntelliJ Idea set the kube config path in environment variable `KUBECONFIG`. Also note, if there are more than one KinD cluster set appropriate context for the server to access. Refer the kubernetes documentation for this specific details. 
  - In my local machine, the docker daemon is configured in WSL2 with Ubuntu-24.04 distro. So the environment configured looks like `KUBECONFIG=\\wsl.localhost\Ubuntu-24.04\home\<user-name>\.kube\config`. When runnign the Spring Application class in InteliJ IDE set the environment variables using `Modify Run configuration`.

<img width="300" height="332" alt="image" src="https://github.com/user-attachments/assets/92c8d463-be72-4759-b3be-6bc7cf71a560" />

<img width="300" height="749" alt="image" src="https://github.com/user-attachments/assets/3dfc36c0-cb3b-465c-9614-6619af6546ef" />


#### Server code 

The `pom.xml` for the project is listed below, the `spring-ai-starter-mcp-server-webmvc` dependency is added. Please refer the Spring documentation for production grade implementations.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.3</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.k8s.mcp</groupId>
	<artifactId>k8s</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>k8s</name>
	<description>Demo project for Spring Boot</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>24</java.version>
		<spring-ai.version>1.0.0</spring-ai.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
		</dependency>
		<dependency>
			<groupId>io.kubernetes</groupId>
			<artifactId>client-java</artifactId>
			<version>24.0.0</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.ai</groupId>
				<artifactId>spring-ai-bom</artifactId>
				<version>${spring-ai.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
	<repositories>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>
	<pluginRepositories>
		<pluginRepository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</pluginRepository>
	</pluginRepositories>

</project>
```

- The code with functionality annotated with `@Tool` in Service layer. The code logic is self-explanatory. As mentioned in the previous blog, the Tools annotation requires `name` and `description` which is used by the MCP to detetmine the context when using the local LLM in this case.

```java
//# filename: K8sService.java 
package com.k8s.mcp.k8s.service;

import com.k8s.mcp.k8s.data.K8sNamespaceInfo;
import com.k8s.mcp.k8s.data.K8sPodInfo;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class K8sService {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sService.class);

    ApiClient client;
    CoreV1Api api;
    public K8sService() {
        try {
            client = Config.defaultClient(); // for local use
        } catch (IOException e) {
            LOGGER.error("exception occurred accessing client ",e);
            throw new RuntimeException(e);
        }
        Configuration.setDefaultApiClient(client);
       api = new CoreV1Api();
    }

    @Tool(name="get_pods_from_given_namespace",
            description = "This function will return the list of pods for a given namespace " +
                    "from the kubernetes cluster")
    public K8sPodInfo getPods(String namespace){


        K8sPodInfo podInfo;
        if (namespace == null || namespace.isBlank()){
            namespace = "default";
        }
        try {
            V1PodList podList = api.listNamespacedPod(namespace).execute();
            List<String> podNames = podList.getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .toList();
            podInfo = new K8sPodInfo(namespace,podNames);
        } catch (ApiException e) {
            LOGGER.error("Exception listing pod",e);
            throw new RuntimeException(e);
        }

        return podInfo;
    }
    @Tool(name="get_all_pods_in_cluster",
            description = "This function will list all the pods from the cluster")
    public List<K8sPodInfo> getAllPodsFromCluster(){

        List<K8sPodInfo> allPodInfo = new ArrayList<>();
        try{
            Map<String,List<String>> nsPodNames = new HashMap<>();

            V1PodList podList = api.listPodForAllNamespaces().execute();
            podList.getItems().forEach(pod -> {
                                String podName = pod.getMetadata().getName();
                                String ns = pod.getMetadata().getNamespace();
                                nsPodNames.putIfAbsent(ns, new ArrayList<String>());
                                nsPodNames.get(ns).add(podName);
                            }
                    );


           nsPodNames.forEach((ns,pods)->{
               allPodInfo.add(new K8sPodInfo(ns,pods));

           });

        } catch (ApiException e) {
            LOGGER.error("Exception accessing all pods from namespace", e);
            throw new RuntimeException(e);
        }
        return allPodInfo;
    }

    @Tool(name="get_all_namespace",
    description = "This function will return list of the namespaces from the kubernetes cluster")
    public K8sNamespaceInfo getNamespaces(){
        K8sNamespaceInfo k8sNamespaceInfo;
        try {
            V1NamespaceList namespaceList = api.listNamespace().execute();
            List<String> nameSpaces = namespaceList.getItems().stream()
                    .filter(ns -> ns.getMetadata() != null)
                    .map( ns -> ns.getMetadata().getName())
                    .toList();
            k8sNamespaceInfo = new K8sNamespaceInfo(nameSpaces);
        } catch (ApiException e) {
            LOGGER.error("Exception fetching namespace ",e);
            throw new RuntimeException(e);
        }
        return k8sNamespaceInfo;
    }


    @Tool(name="create_namespace",
    description = "This function will create a namespace in the kubernetes cluster" +
            " and return the list of namespace available in the cluster as response with created namespace")
    public K8sNamespaceInfo K8sCreateNamespace(String namespace){

        LOGGER.info("Creating namespace named : {}",namespace);

        if(namespace == null || namespace.isBlank()){
            throw new RuntimeException("namespace can't be empty or blank");
        }
        V1Namespace nsObject = new V1Namespace();
        V1ObjectMeta nsMetadata = new V1ObjectMeta();
        nsMetadata.setName(namespace);
        nsObject.setMetadata(nsMetadata);
        CoreV1Api.APIcreateNamespaceRequest nsRequest =  api.createNamespace(nsObject);
        try {
            nsRequest.execute();
        } catch (ApiException e) {
            LOGGER.error("error creating namespace ",e);
            throw new RuntimeException(e);
        }

        return getNamespaces();
    }
}
```

- Spring Bean configuration to registering the service to the spring context.

```java
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
```

- Pojo classes used to hold the resource information form the cluster which will be sent part of response

```java
//# filename: K8sNamespaceInfo.java
package com.k8s.mcp.k8s.data;

import java.util.List;

public record K8sNamespaceInfo(List<String> namespaces) {}
```

```java
//#filename: K8sPodInfo
package com.k8s.mcp.k8s.data;

import java.util.List;

public record K8sPodInfo(String namespace, List<String> podNames) {}
```
- Entry point Spring Application

```java
package com.k8s.mcp.k8s;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class K8sApplication {

	public static void main(String[] args) {
		SpringApplication.run(K8sApplication.class, args);
	}
}
```

- `application.yaml` file of server. The properties in below commented default can be removed for the code to work 9it is optional).

```yaml
spring:
  application:
    name: k8s-mcp-server
  main:
    #web-application-type: none
    banner-mode: off
  ai:
    mcp:
      server:
        enabled: true
        name: k8s-mcp-server
        version: 1.0.0
        resource-change-notification: true # default
        tool-change-notification: true     # default
        prompt-change-notification: true   # default
        type: sync                         # default
        capabilities:                      # default
          completion: true                 # default
          prompt: true                     # default
          resource: true                   # default
          tool: true                       # default

logging:
  level:
    io.modelcontextprotocol: TRACE
    org.springframework.ai.mcp: TRACE
```

### Testing the server

#### Curl command

- Once the server application is up and running, we can access using cURL command 

 - execute the curl command below to check the sse endpoint 

```sh
$ curl http://localhost:8080/sse
id:f087df6d-795d-472c-8313-80f41ed76c56
event:endpoint
data:/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56
```

The lifecycle of MCP connection can be found in [Model Context protocol documentation](https://modelcontextprotocol.io/specification/2025-03-26/basic/lifecycle)

- To connect to the server with curl command, we use the below url, which is part of the response from the above
  - Open another git bash instance to initialize the transaction
```sh
curl -XPOST http://localhost:8080/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56   -H "Content-Type: application/json" -d '{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-03-26",
    "capabilities": {
      "roots": {
        "listChanged": true
      },
      "sampling": {}
    },
    "clientInfo": {
      "name": "ExampleClient",
      "version": "1.0.0"
    }
  }
}'
```

- Upon executing above intialize method on the endpoint, we could see repsone on the sse endpoint prompt like below few lines

```sh
$ curl http://localhost:8080/sse
id:f087df6d-795d-472c-8313-80f41ed76c56
event:endpoint
data:/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56

id:f087df6d-795d-472c-8313-80f41ed76c56
event:message
data:{"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2024-11-05","capabilities":{"completions":{},"logging":{},"prompts":{"listChanged":true},"resources":{"subscribe":false,"listChanged":true},"tools":{"listChanged":true}},"serverInfo":{"name":"k8s-mcp-server","version":"1.0.0"}}}
```

- Upon executing below comand, since below is a notification to the cluster there won't be any repsonse just the 200 OK

```sh
curl -XPOST http://localhost:8080/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56 -H "Content-Type: application/json" -d '{
  "jsonrpc": "2.0",
  "method": "notifications/initialized"
}'
```

- with the curl command we can try to list the tools

```sh
curl -XPOST http://localhost:8080/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56 -H "Content-Type: application/json" -d '{
  "jsonrpc": "2.0",
  "method": "tools/list",
  "id": 1
}'
```

- Execution above tools list method command we could see the list of tools in the sse endpoint prompt, refer the last few lines with response

```sh
$ curl http://localhost:8080/sse
id:f087df6d-795d-472c-8313-80f41ed76c56
event:endpoint
data:/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56

id:f087df6d-795d-472c-8313-80f41ed76c56
event:message
data:{"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2024-11-05","capabilities":{"completions":{},"logging":{},"prompts":{"listChanged":true},"resources":{"subscribe":false,"listChanged":true},"tools":{"listChanged":true}},"serverInfo":{"name":"k8s-mcp-server","version":"1.0.0"}}}

id:f087df6d-795d-472c-8313-80f41ed76c56
event:message
data:{"jsonrpc":"2.0","id":1,"result":{"tools":[{"name":"get_all_namespace","description":"This function will return list of the namespaces from the kubernetes cluster","inputSchema":{"type":"object","properties":{},"required":[],"additionalProperties":false}},{"name":"get_all_pods_in_cluster","description":"This function will list all the pods from the cluster","inputSchema":{"type":"object","properties":{},"required":[],"additionalProperties":false}},{"name":"create_namespace","description":"This function will create a namespace in the kubernetes cluster and return the list of namespace available in the cluster as response with created namespace","inputSchema":{"type":"object","properties":{"namespace":{"type":"string"}},"required":["namespace"],"additionalProperties":false}},{"name":"get_pods_from_given_namespace","description":"This function will return the list of pods for a given namespace from the kubernetes cluster","inputSchema":{"type":"object","properties":{"namespace":{"type":"string"}},"required":["namespace"],"additionalProperties":false}}]}}
```

- To call the functionality we can execute below command in different git bash

```sh
curl -XPOST http://localhost:8080/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56 -H "Content-Type: application/json" -d '{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "get_pods_from_given_namespace",
    "arguments": {
      "namespace": "apisix"
    }
  },
  "id": 2
}'
```

- Executing the command above we could see repsonse on the bash where we executed sse endpint

```sh
$ curl http://localhost:8080/sse
id:f087df6d-795d-472c-8313-80f41ed76c56
event:endpoint
data:/mcp/message?sessionId=f087df6d-795d-472c-8313-80f41ed76c56

id:f087df6d-795d-472c-8313-80f41ed76c56
event:message
data:{"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2024-11-05","capabilities":{"completions":{},"logging":{},"prompts":{"listChanged":true},"resources":{"subscribe":false,"listChanged":true},"tools":{"listChanged":true}},"serverInfo":{"name":"k8s-mcp-server","version":"1.0.0"}}}

id:f087df6d-795d-472c-8313-80f41ed76c56
event:message
data:{"jsonrpc":"2.0","id":1,"result":{"tools":[{"name":"get_all_namespace","description":"This function will return list of the namespaces from the kubernetes cluster","inputSchema":{"type":"object","properties":{},"required":[],"additionalProperties":false}},{"name":"get_all_pods_in_cluster","description":"This function will list all the pods from the cluster","inputSchema":{"type":"object","properties":{},"required":[],"additionalProperties":false}},{"name":"create_namespace","description":"This function will create a namespace in the kubernetes cluster and return the list of namespace available in the cluster as response with created namespace","inputSchema":{"type":"object","properties":{"namespace":{"type":"string"}},"required":["namespace"],"additionalProperties":false}},{"name":"get_pods_from_given_namespace","description":"This function will return the list of pods for a given namespace from the kubernetes cluster","inputSchema":{"type":"object","properties":{"namespace":{"type":"string"}},"required":["namespace"],"additionalProperties":false}}]}}

id:f087df6d-795d-472c-8313-80f41ed76c56
event:message
data:{"jsonrpc":"2.0","id":2,"result":{"content":[{"type":"text","text":"{\"namespace\":\"apisix\",\"podNames\":[\"apisix-85fcc8d665-xrx2w\",\"apisix-dashboard-68b7748c98-sbcqv\",\"apisix-etcd-0\",\"apisix-etcd-1\",\"apisix-etcd-2\",\"apisix-ingress-controller-f6fb8548-bgdnn\"]}"}],"isError":false}}
```

#### MCP inspector

To start the MCP inspector use below command, this will open up the browser

```
 npx @modelcontextprotocol/inspector
```

- From the browser select below configuration values

```
Transport type: SSE
URL: http://localhost:8080/sse
```

<img width="2651" height="1264" alt="image" src="https://github.com/user-attachments/assets/527283c6-0464-4823-b495-ab41f159717f" />

Select Tools, and list the tools, if the curl command to sse is enabled terminate that connection else you might receive timeout message in case of sync connection.

<img width="2684" height="1442" alt="image" src="https://github.com/user-attachments/assets/4e4fb757-8187-4020-8ee3-0cb7a91a0747" />

### MCP client

- pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.3</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.k8s.client</groupId>
	<artifactId>k8s-client</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>k8s-client</name>
	<description>Demo project for Spring Boot</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>24</java.version>
		<spring-ai.version>1.0.0</spring-ai.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-mcp-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-model-ollama</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.ai</groupId>
				<artifactId>spring-ai-bom</artifactId>
				<version>${spring-ai.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
```
- The controller class

```java
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
```

- Entrypoint of the class

```java
package com.k8s.client.k8s_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class K8sClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(K8sClientApplication.class, args);
	}
}
```

- Resource configuration

```yaml
#application.yaml
spring:
   application:
     name: k8s-client
   ai:
     mcp:
       client:
         enabled: true
         toolcallback:
           enabled: true
         name: k8s-mcp-client
         version: 1.0.0
         request-timeout: 30s
         type: SYNC  # or ASYNC for reactive applications
         sse:
           connections:
             server1:
               url: http://localhost:8080
     ollama:
       base-url: http://localhost:11434
       chat:
         options:
           model: llama3.2
logging:
  level:
    io:
      modelcontextprotocol:
        client: trace
        spec: trace

server:
   port: 8085
```

#### Output 

- List of pods from the kind cluster `kubectl get pods -A`
<img width="450" height="1132" alt="image" src="https://github.com/user-attachments/assets/0b9e41e7-452a-4097-91e3-6d9695f09ca1" />

- List of namespace from the kind cluster `kubectl get namespace`
<img width="250" height="399" alt="image" src="https://github.com/user-attachments/assets/354e2e36-e28c-48e8-bb23-2fa26280e0fd" />

- With `curl http://localhost:8085/input/in -d 'get me the list of pods from all namespace'` the output snapshot looks like below
<img width="450" height="1621" alt="image" src="https://github.com/user-attachments/assets/da88faa4-54ed-4f4a-b2ea-c57c709736e1" />

- with `curl http://localhost:8085/input/in -d 'get me the pods from apisix namespace'` the output snapshot looks like below
<img width="500" height="335" alt="image" src="https://github.com/user-attachments/assets/a47b402d-268c-4d30-b233-e87ff4c92faf" />

- with `curl http://localhost:8085/input/in -d 'create a new namespace named test-k8s-mcp in the cluster'` the output looks like below
  Note, the response from the functionality is list of namespace after created but the client displays a message only. This probably the description can be more explicitly defined.
<img width="550" height="80" alt="image" src="https://github.com/user-attachments/assets/98d25ead-017a-47b4-8bc1-19934369e69d" />

