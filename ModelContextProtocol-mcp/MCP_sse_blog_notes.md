## Spring AI MCP Client Server communication with SSE transport 

In this blog have created a Spring AI MCP server which will expose simple functionality to manage a kubernetes cluster. This is with reference to my previous [blog Spring AI MCP Client Server using STDIO transport](https://www.linkedin.com/pulse/mcp-client-server-use-local-llm-thirumurthi-s-5uc5c/) which manages an in-memory list of items. 

The MCP Server includes a set of tools to manage the local kind cluster using the Kuberentes Java client API. The tool functionality creates list and creates few resources in the kind cluster for demonstration purpose. The MCP Client uses the local Ollama service running in docker with llama-3.2 model.


Pre-requisites:
  - Docker Desktop
  - Kind CLI


Create a kind cluster using Kind cli using `kind create cluster --name sample`. 
Once the Kind cluster is running in Docker, the kube config of cluster needs to be configured in IDE so the MCP server code can connect to the cluster. 
In my case, running the docker in WSL2 with Ubuntu-24.04 distroand set the environment variable `KUBECONFIG` with the path, which looks like `KUBECONFIG=\\wsl.localhost\Ubuntu-24.04\home\<user>\.kube\config`. 
In IntelliJ it would be clicking the `Modify Run configuration` and setting the `Environment Variables`.


With the SSE transport, the MCP server runs as standalone application and the MCP client access to it using `/sse` path.

#### Server code 

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

- Service layer with set of functionality that commuincates to the KinD cluster

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

        if(namespace == null || namespace.isBlank()){
            throw new RuntimeException("namespace can't be empty or blank");
        }
        V1Namespace nsObject = new V1Namespace();
        V1ObjectMeta nsMetadata = new V1ObjectMeta();
        nsMetadata.setNamespace(namespace);
        nsObject.setMetadata(nsMetadata);
        api.createNamespace(nsObject);

        return getNamespaces();
    }
}
```

- Registering the service with the functionality to the spring context

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

- Pojo classes to hold the information part of response

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

