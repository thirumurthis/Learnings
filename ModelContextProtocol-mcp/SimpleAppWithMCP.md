### Application with MCP protocol with local LLM

With the AI being popular, recently came accross the Model Context Protocol (MCP) in this blog have documented my learning with a sample application.

### What is MCP?

Model Context Protocol (MCP) is an open standard developed by Anthropic. MCP is an open protocol that standardizes how applications provide context to LLMs. MCP helps you build agents and complex workflows on top of LLMs.
MCP standardize how AI applications, particularly large language models (LLMs), access and utilize external tools, data, and resources. For more details refer [MCP documentation](https://modelcontextprotocol.io/introduction).


### Summary of the local application

The MCP Server and Client is build using Spring AI and communicates with STDIO. MCP supports different transport like STDIO, SSE (Server-Sent Event).

`STDIO` - Standard input and output (stdio) is the simplest and most universal transport for MCP
`SSE` - Server-sent events (SSE) provide a persistent connection for server-to-client streaming, while client-to-server messages are sent using HTTP POST.

The functionality of the server is exposed using `Tools`. With the `spring-ai-starter-mcp-server` supports `@Tool` annotation which includes `name` and `description`. The LLM uses the `description` to understand the functionality. The `spring-ai-starter-mcp-server` supports simple STDIO transport. 

`Tools` - Tools are a powerful primitive in the Model Context Protocol (MCP) that enable servers to expose executable functionality to clients. Through tools, LLMs can interact with external systems, perform computations, and take actions in the real world. 


##### Local Olama LLM in docker container
 
For my learning, the Ollama LLM runs local docker container, with llama3.2 model.

- To run the Ollama container in local docker use below command

```sh
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
```

- Once the Ollama docker container starts running, we exec into the container and issue below command to run the llama3.2 model. Below command will start downloading the model to the container and starts it, once it started a prompt will be displayed. To quit the prompt type `/bye`. If jsut need execut the command drop the `-it` option from the command.

```sh
docker exec -it ollama ollama run llama3.2
```

##### Application info

For learning, the MCP server expose a service layer which uses set of functionality that manages the in-memory list of item. The Item object is a Java record, with name and quantity fields.
There service includes functionality to get the list of items from the in-memory list, another service to add one item and get only one item.

The server code looks like below, we can use the `start.spring.io`, to include `MCP server` dependency and configure the generated zip file to any IDE.

The Server code details

- Pom.xml - Includes a maven plugin to copy the generated jar to a different path. This is done because in the MCP Client configuration for STDIO we will configure the jar to start the server. We could see that in the MCP client code application yaml, were the mcp-server-config.json will be included. 

```xml
<!-- file name: pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.4-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.mcp.demo</groupId>
	<artifactId>mcp-server</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>mcp-server</name>
	<description>Sample project MCP server</description>
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
			<artifactId>spring-ai-starter-mcp-server</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-autoconfigure</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
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
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>3.8.1</version>
				<executions>
					<execution>
						<id>copy</id>
						<phase>package</phase>
						<goals>
							<goal>copy</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<artifactItems>
						<artifactItem>
							<groupId>${project.groupId}</groupId>
							<artifactId>${project.artifactId}</artifactId>
							<version>${project.version}</version>
							<type>${project.packaging}</type>
						</artifactItem>
					</artifactItems>
					<outputDirectory>C:\\AI-mcp\\jar</outputDirectory>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

- The `resources/application.yaml` looks like below. The `loggging.pattern.console` is set to empty since when the MCP Client communicates to the MCP Server with STDIO transport we don't wan't the log message being logged in the console. 
- Note with the `logging.patter.console` empty, when the MCP server application is started to run on the IDE, should see below message but the application is running as expected. 

```
ERROR in ch.qos.logback.classic.PatternLayout("") - Empty or null pattern.
```

```yaml
# file-name: resources/application.yaml
spring:
   application:
     name: item-mcp-server
   main:
      web-application-type: none
      banner-mode: off
   ai:
     mcp:
        server:
           name: item-mcp-server
           version: 1.0.0

logging:
   pattern:
      console:
```

- The Item record looks like below, we use AtomicInteger to allocate Id

```java
package com.mcp.demo.server.data;

import java.util.concurrent.atomic.AtomicInteger;

public record Item(String name, int quantity, int id) {
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Item {
        if (id ==0 ){
            id = counter.incrementAndGet();
        }
    }
    public Item(String name, int quantity){
        this(name,quantity,counter.incrementAndGet());
    }
}
```

The Core service code that manages the Items in in-memory list

```java
package com.mcp.demo.server.service;

import com.mcp.demo.server.data.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    public static final Logger log = LoggerFactory.getLogger(ItemService.class);

    public List<Item> items = new ArrayList<>();

    @Tool(name="t_get_all_items",description = "This method will get all the items stored in-memory list in this application")
    public List<Item> getItems (){
        return items;
    }

    @Tool(name="t_get_item_by_name",description = "This method will fetch one item based on the input name from the in-memory item lists, the name shouldn't be null")
    public Item getItem(String name) throws IllegalArgumentException {
        if(name == null || name.isEmpty()){
            log.error("Name can't be empty");
            throw new IllegalArgumentException("Name can't be empty for this request - t_get_item_by_name service");
        }
        return items.stream()
                .filter(car ->  car.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Tool(name="t_add_item_to_list",description = "This method will add a single item to the in-memory list. " +
            "The inputItem argument in the method should be an Item object which includes name and quantity field." +
            "Before accessing this tool functionality from the client create the Item object with name and quantity" +
            "and this function will add it to the in-memory list")
    public String addItem(Item inputItem) throws IllegalArgumentException{
        if(inputItem == null || inputItem.name() == null || inputItem.name().isEmpty()){
            log.error("input Item name can't be empty");
            throw new IllegalArgumentException("Input Item name can't be empty");
        }
        items.add(inputItem);
        return inputItem.toString();
    }

    @PostConstruct
    public void init(){
       List<Item> carList = List.of(
                new Item("Table",156),
               new Item("Chair",510),
               new Item("Cups",500),
               new Item("Bottle",43),
               new Item("Box",600)
        );
        items.addAll(carList);
    }
}
```

- Below code is the how to register the service with tools functionality to the ToolCallBackProvider. So client can easily list the tools.

```java
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
```

- Entry point of the server application, this is typical code generated by the spring starter io.

```java
package com.mcp.demo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}
}
```

#### Testing the Server code with MCP Inspector

- Already some of the REST clients like Postman supportws MCP, but in this case Antropic community has inspector tool which requires node js to be installed in local.
- We can use `Cluade`, `Cursor`, etc. desktop tool to connect to the MCP server as well.
- Make sure to generate the server Jar generated, as we are using maven it would be `mvn clean install`, the generated jar will be moved to the path specified in the pom.xml maven plugin.

- To start the application we can use below command

```sh
 npx @modelcontextprotocol/inspector
```

- The output of the command looks like below
```sh
Need to install the following packages:
@modelcontextprotocol/inspector@0.16.1
Ok to proceed? (y) y

Starting MCP inspector...
⚙️ Proxy server listening on localhost:6277
🔑 Session token: 85b6c70dfebf439a3737efe462470fadad8f790bcc0c3d0ac56e2ffe99f07552
   Use this token to authenticate requests or set DANGEROUSLY_OMIT_AUTH=true to disable auth

🚀 MCP Inspector is up and running at:
   http://localhost:6274/?MCP_PROXY_AUTH_TOKEN=85b6c70dfebf439a3737efe462470fadad8f790bcc0c3d0ac56e2ffe99f07552

🌐 Opening browser...
```

The browser will look like below to start with.

<img width="2471" height="1591" alt="image" src="https://github.com/user-attachments/assets/eeaaf827-f590-411b-9caa-19d44b9495a1" />

In the UI, we could select and update below configuration 
 - Note, the java should be accessible in the path, and the arguments we pass the path of the mce-server jar. Make sure

```
Transport Type: STDIO
Command: java
Arguments "-Dspring.ai.mcp.server.stdio=true" "-jar" "C:\\AI-mcp\\jar\\mcp-server-0.0.1-SNAPSHOT.jar"
```

<img width="1822" height="1470" alt="image" src="https://github.com/user-attachments/assets/4d3f93b8-8cb7-4bff-aa77-39f14d08f966" />

Clicking the Connect should see the screen like below 

<img width="2643" height="1569" alt="image" src="https://github.com/user-attachments/assets/50c024ea-1e3a-4676-a100-955e63180d5a" />

Select the Tools tab and click the list resources, which would list the service tool list 
<img width="2467" height="1561" alt="image" src="https://github.com/user-attachments/assets/950421c8-4d8d-4da5-a961-707bbf84eb4e" />

`Claude` desktop to connect to the server 

<img width="1996" height="973" alt="image" src="https://github.com/user-attachments/assets/d5002cf9-5850-4722-a6cb-e6d604822721" />

- The Claude config looks like below, the server configuration below aslo includes a filesystem mcp server. 
  
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "C:\\thiru\\edu\\AI-mcp\\data"
      ]
    },
    "item-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\thiru\\edu\\AI-mcp\\jar\\mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

From Claude, when request to list all the items, will prompt to allow access, which looks like below

<img width="1950" height="1285" alt="image" src="https://github.com/user-attachments/assets/5e944e76-d34d-4e2c-b3b6-62dba41377e8" />

The response will look like below 
<img width="1420" height="1144" alt="image" src="https://github.com/user-attachments/assets/0080c818-29f1-416a-ac7d-8e954df10d51" />

### MCP Client Code

The MCP Client code is also generated using Spring Starter io, with the MCP client dependency.

Below lists the code for MCP Client
 - The MCP Client java dependency `spring-ai-starter-mcp-client`
 - Since we use Ollama we add the spring dependency `spring-ai-starter-model-ollama`
 - `spring-boot-starter-web` dependency is added to expose an POST endpoint to get use prompt

```xml
<!-- file-name: pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.5.4-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.mcp.demo</groupId>
	<artifactId>mcp-client</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>mcp-client</name>
	<description>Sample project MCP server</description>
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

- We include the input controller, note that Spring AI MCP Client autowires the MCP client, would recommend to refer the [spring documentation](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)

```java
package com.mcp.demo.client.controller;

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

        // This prints the tools list mostly used for debugging
        // This is optional
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

- The `resources/application.yaml` configuration looks below

```yaml
spring:
  application:
    name: item-mcp-client
  main:
    banner-mode: off
  ai:
    mcp:
      client:
        enabled: true
        name: item-mcp-client
        version: 1.0.0
        toolcallback:
          enabled: true
        stdio:
          servers-configuration: classpath:mcp-servers-config.json # mcp server config
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
```

- The `mcp-servers-config.json`, this includes the command to start the server jar

```json
{
  "mcpServers": {
    "item-mcp-client": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-jar",
        "C:\\thiru\\edu\\AI-mcp\\jar\\mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

### Output:

- With the MCP client and MCP server running, below output section shows using cURL command to access functionality we had created to manage the items.


```sh
$ curl http://localhost:8080/input/in -d'List all the items from the server' -H 'Content-Type: application/json'
Here is the list of items from the server:

* Table: 156 items, ID: 1
* Chair: 510 items, ID: 2
* Cups: 500 items, ID: 3
* Bottle: 43 items, ID: 4
* Box: 600 items, ID: 5
```

- Note: When we need to add an item to the list, with the provided simple instruction we could see we get error message repsonse. This is because, the LLM is not able to infer and Create the Item object before accessing the specific tools functionality. On the MCP client we could see below message in this case.

```
java.lang.IllegalStateException: Error calling tool: [TextContent[audience=null, priority=null, text=Conversion from JSON to com.mcp.demo.server.data.Item failed]]
```

```sh
$ curl http://localhost:8080/input/in -d'Please add an item with name Calculator and quantity 5 to the existing in-memory list' -H 'Content-Type: application/json'
{"timestamp":"2025-07-12T21:42:54.166+00:00","status":500,"error":"Internal Server Error","path":"/input/in"}
```

```sh
 curl http://localhost:8080/input/in -d'Please add an item with name Calculator and quantity 5 to the existing in-memory list, make sure to invoke the tools functionality with Item object with name and quantity field.' -H 'Content-Type: application/json'
The tool has added an item to the list with name 'Calculator' and quantity 5. The new item's ID is 6. Here's a formatted answer:

The item "Calculator" with quantity 5 has been successfully added to the list. The item's details are as follows:
Name: Calculator
Quantity: 5
ID: 6
```

```sh
$ curl http://localhost:8080/input/in -d'List all the items from the server' -H 'Content-Type: application/json'
Here is the list of items from the server:

1. Table - 156 units
2. Chair - 510 units
3. Cups - 500 units
4. Bottle - 43 units
5. Box - 600 units
6. Calculator - 5 units
```

Reference:

1. [MCP documentation](https://modelcontextprotocol.io/introduction)
2. [MCP Tools](https://modelcontextprotocol.io/docs/concepts/tools)
3. [Transport details](https://www.speakeasy.com/mcp/building-servers/protocol-reference/transports)
