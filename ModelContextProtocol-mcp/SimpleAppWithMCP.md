### Application with MCP protocol with local LLM

With the AI being buzz word in the IT industry recently came across the work Model Context Protocol (MCP) and wanted to learn and understand about the protocol. This blog doesn’t details much about the MCP, please refer the MCP documentation. This would like a quick start with MCP using Spring AI and captures my learnings of using different tools.

#### Pre-requisites:
- Docker Desktop
- Java
- Any IDE for Java development

### What is MCP?

Model Context Protocol (MCP) is an open standard developed by Anthropic. MCP is an open protocol that standardizes how applications provide context to LLMs. MCP helps you build agents and complex workflows on top of LLMs. MCP standardize how AI applications, particularly large language models (LLMs), access and utilize external tools, data, and resources. For more details refer MCP documentation.

### Sample Application

Have build a simple MCP Server and Client using Spring AI. There are different transports provided by MCP but have used STDIO transport since it is simple to start with. MCP supports different transport like STDIO, SSE (Server-Sent Event).

STDIO - Standard input and output (stdio) is the simplest and most universal transport for MCP 

SSE - Server-sent events (SSE)- provide a persistent connection for server-to-client streaming, while client-to-server messages are sent using HTTP POST.

The MCP server code includes a service layer which has bunch of functionality to manage an in-memory Item list. The methods in the service layer are annotated with @Tools. For Spring AI this annotation is imported from  spring-ai-starter-mcp-server dependency. The tools annotation includes name and description field. With the description LLM will be able to set context and understand the methods functionality.

Tools - Tools are a powerful primitive in the Model Context Protocol (MCP) that enable servers to expose executable functionality to clients. Through tools, LLMs can interact with external systems, perform computations, and take actions in the real world.


### Running Ollama LLM in local docker container
 
- To run the Ollama container in local docker use below command

```sh
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
```

- Once the Ollama docker container starts running in detached mode, we execute below command which will exec to the container and issue below command. The command will run the llama3.2 model. The command will download the model, note the volume in the above docker command the model will be downloaded to local storage at that path. After downloading the model will starts display the prompt. You can quit out of the prompt by typing /bye. By dropping the -it option in below command there won’t be any prompt displayed when running the model in ollama container.

```sh
docker exec -it ollama ollama run llama3.2
```

### Overall flow

<img width="1439" height="1075" alt="image" src="https://github.com/user-attachments/assets/abb2b633-c5c5-4106-a672-50c38d3606d2" />

The MCP server include bunch of methods to manage the in-memory item list in the service layer. The Item is defined as Java record, with id, name and quantity fields. There service layer methods will list all the items from in-memory list, add and find item by name. Note, after deploying MCP Client noticed when adding the item, the LLM requires explicit instruction to create the Item object with name and quantity fields explicitly else get text to json conversion error. Refer the Output section below.

For initial code generation have used the start.spring.io, and included only MCP server dependency.

### Server code details

- pom.xml
The pom.xml also includes maven plugin to copy the generated jar to a different path. This is optional, I just wanted to keep the server jar that will be executed by the client mcp-server configuration in different folder. In the MCP Client code application.yaml we could see the classpath reference to the mcp-server-config.json which includes the java command to start the server. This is done as we are using STDIO transport

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

The resources/application.yaml looks like below. The loggging.pattern.console is set to empty since when the MCP Client communicates to the MCP Server with STDIO transport we don't wan't the log message being logged in the console.

> Note:
> With the logging.patter.console empty, when starting the MCP server application in the IDE, you might see below message. This is fine the actual application is running as expected.

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

Refer the @Tools annotation in the service layer, the description includes information about the methods functionality. If the description is detailed enough with example, the LLM could better associate with the context.

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

- Below code shows how to register the item service with tools functionality to the ToolCallBackProvider. The MCP Client would be able to list the tools in the client side.

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

There are different options to test the MCP server code. 

Like using Postman with MCP, Claude, Cursor, etc. In here have details testing using MCP Inspector which is from Anthropic community. To run the MCP Inspector we need node js to be installed in local machine.

Make sure to generate the server Jar, before testing with the tool, use mvn clean install to generate the jar. Jar will be copied to the path specified in the pom.xml maven plugin.

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

> Note:
> Java should be accessible and update the UI with below command and arguments info

```
Transport Type: STDIO
Command: java
Arguments "-Dspring.ai.mcp.server.stdio=true" "-jar" "C:\\AI-mcp\\jar\\mcp-server-0.0.1-SNAPSHOT.jar"
```

<img width="1822" height="1470" alt="image" src="https://github.com/user-attachments/assets/4d3f93b8-8cb7-4bff-aa77-39f14d08f966" />

Click the Connect button which should display the screen as seen below

<img width="2643" height="1569" alt="image" src="https://github.com/user-attachments/assets/50c024ea-1e3a-4676-a100-955e63180d5a" />

Select the Tools tab and click the list Tools, which would list the service tool list
<img width="2467" height="1561" alt="image" src="https://github.com/user-attachments/assets/950421c8-4d8d-4da5-a961-707bbf84eb4e" />

`Claude` desktop to connect to the server 

<img width="1996" height="973" alt="image" src="https://github.com/user-attachments/assets/d5002cf9-5850-4722-a6cb-e6d604822721" />

- The Claude config looks like below, the server configuration below it also includes a filesystem mcp server which is optional.
  
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

From Claude, when we request to list all the items from the in-memory list, it will prompt to allow access, which looks like below

<img width="1950" height="1285" alt="image" src="https://github.com/user-attachments/assets/5e944e76-d34d-4e2c-b3b6-62dba41377e8" />

The response will look like below 
<img width="1420" height="1144" alt="image" src="https://github.com/user-attachments/assets/0080c818-29f1-416a-ac7d-8e954df10d51" />

### MCP Client Code details


The MCP Client code is also generated using Spring Starter io and with the MCP Client dependency.

The Client code includes below dependencies

 - The MCP Client java dependency spring-ai-starter-mcp-client
 - Ollama spring dependency spring-ai-starter-model-ollama
 - spring-boot-starter-web dependency expose an POST endpoint to send message using cURL

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
