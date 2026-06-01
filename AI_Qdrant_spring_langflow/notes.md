### RAG system in Spring AI and Langflow using Qdrant Vector Storage

The motivation of the article is use AI with my financial statements as context and ask questions around those. In order to build everything locally had to use RAG approach. The statements in this case used a text file with the transaction history for few months and injected to the Qdrant Vector store with mxbai-embed-large model is used.

To deploy the models in local have used Ollama in docker container. The embedding model and the llama3.2 LLM model is running in the Ollama container, which can is accessed via http://localhost:11434

RAG system with Spring AI

This is a simple Spring Boot application with the Spring AI dependencies included with  the Qdrant vector store auto configuration. The Qdrant vector store access configuration is configured in the application.yaml. The text file with statement is placed under the resources/docs folder. These files are ingested to Qdrant vector store on startup. The REST endpoint is exposed to take user input in this case we use curl with -d option, which is passed to Ollamachat model configured to use the vector store. The response will be printed in the screen. There is no UI for user inputs for this application.

Langflow

Langflow is a visual way to build AI workflows, this is literally low-to-no code approach, not intended for production environment. In this article the Langflow is deployed in Docker container, and different components are used to create the RAG system to connect to local vector storage and LLM model. The data to vector store is loaded under collections, we could see this in the Qdrant UI, the Langflow injects read file component reads the text file and loads to a new collection. The Qdrant component properties should be configured differently since using local applications, by default it tries to use SSL transport. The Qdrant component in Langflow should use url which uses the Qdrant Docker container IP itself.   

This article doesn't details the Langflow, refer the documentation for more info. 

Take away

Using local models is very slow and the response accuracy is very less

## Deploying the required components

### Deploy Ollama container

In Docker container use below command to deploy the ollama. The environment variable is to keep the model in memory not to offload when not in use.

```sh
docker run -e OLLAMA_KEEP_ALIVE=-1 -v ollama:/root/.ollama -d -p 11434:11434 --name ollama ollama/ollama
```

Once the docker container started, exec to the contaner with below command and pull the embedding model and llama3.2

To exec to the docker container

```sh
docker exec -it ollama sh
```

Use below command after exec into the docker container 

```sh
# ollama pull  mxbai-embed-large
# ollama run llama3.2
``` 

### Deploy Qdrant vector data base-url

Below command will deploy the Qdrant in docker container, uses mount volume from local to persist the data.

The port 6334 is used for gRPC transport

```sh
docker run --name qdrant -d -p 6333:6333 -p 6334:6334 \
-v $(pwd)/qdrant:/qdrant/storage \
qdrant/qdrant
```

Once container is ready, we can verify by accessing the endpoint `http://localhost:6333`.
The qdrant UI can be accessed from `http://localhost:6333/dashboard`, and metrics `http://localhost:6333/metrics` if need to configure Grafana dashboards.


Spring AI:

The spring boot application is very simple with the qdrant auto configuration dependency.

Below is the spring configuration 

```yaml
spring:
  application:
    name: rag
  ai:
    ollama:
      init:
        pull-model-strategy: when_missing
        timeout: 5m
        embedding:
		  # loaded in the Ollama
          additional-models:
            - mxbai-embed-large
          options:
		    # used for embbedding
            model: mxbai-embed-large
            keep_alive: 30m
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2
    # reference https://docs.spring.io/spring-ai/reference/api/embeddings/ollama-embeddings.html
    model:
      embedding: ollama
    vectorstore:
      qdrant:
        host: localhost
        port: 6334
        use-tls: false
        initialize-schema: true
        collection-name: custom-collection

server:
  port: 8095
```

The pom.xml with dependency

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.0.6</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>ai.app</groupId>
	<artifactId>qdrantrag</artifactId>
	<version>1.0.1-SNAPSHOT</version>
	<name>qdrantrag</name>
	<description>Rag example document</description>
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
		<java.version>25</java.version>
		<spring-ai.version>1.1.7</spring-ai.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-advisors-vector-store</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-pdf-document-reader</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-vector-store-qdrant</artifactId>
		</dependency>

		<dependency>
			<groupId>io.grpc</groupId>
			<artifactId>grpc-api</artifactId>
			<version>1.81.0</version>
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
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.14.1</version>
			</plugin>
		</plugins>
	</build>

</project>

```

- The entry point of spring application

```java
package ai.app.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RagApplication {

	static void main(String[] args) {
		SpringApplication.run(RagApplication.class, args);
	}

}

```

- Data injection service

```java
package ai.app.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DocumentIngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    @Value("classpath:docs/*")
    private Resource[] docsPath;


    @Override
    public void run(String... args) throws Exception {

        for(Resource file : docsPath ){
            loadTextToQdrant(file.getFile().getAbsolutePath());
        }
    }

    // reads the text file from and loads to the collection
    public void loadTextToQdrant(String filePath) {
   
        String resourcePath = "file:" + filePath;
        log.info("file name from the folder {}",resourcePath);
		
        // Read the text file
		
        TextReader textReader = new TextReader(new FileSystemResource(new File(filePath)));
        List<Document> documents = textReader.get();

        // Tokenize/Chunk the documents based on token counts
		// the splitter with parameter
		// TokenTextSplitter(chunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator, punctuationMarks)
		
        TokenTextSplitter textSplitter = new TokenTextSplitter(800, 200, 200, 200, true, List.of(',',' ',':'));

        List<Document> splitDocuments = textSplitter.apply(documents);

        // Generate chunks and their vectors automatically to Qdrant
        vectorStore.accept(splitDocuments);
    }
	
	// option to read from pdf file but not used in this application
	public void loadPdfToQdrant(String filePath) {
	
        // Read the PDF from the local file path
        String resourcePath = "file:" + filePath;
        log.info("file name from the folder {}",resourcePath);
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resourcePath);
        List<Document> rawDocuments = pdfReader.get();

        // Split text into smaller chunks for better embedding accuracy
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.apply(rawDocuments);

        // Write chunks and their vectors automatically to Qdrant
        vectorStore.accept(splitDocuments);
    }
}
```

REST controller exposing end point to get user input  

```java
package ai.app.rag.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);
    private final OllamaChatModel chatModel;
    private final VectorStore vectorStore;

    AppController(OllamaChatModel chatModel, VectorStore vectorStore){
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }
    @PostMapping("/input")
    public String chat(@RequestBody String input){

        log.info("input received - {}",input);

        return ChatClient.builder(chatModel)
                .build()
                .prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .user(input)
                .call()
                .content();
    }
}
```

The financial transaction data where placed under a folder called docs in text format. Still PDF can still be used.

Sample request and response 

```
$  curl http://localhost:8095/api/input -d 'summaries the overall transactions for each month in the table format with table format with header month,total payments and other credits, total purchases and adjestments, total. if possible could you plot a text simple graph for it againts month and total'

Based on the provided table format with transactions for each month, I will summarize the overall transactions for each month.

**Monthly Summary:**

| Month | Total Payments and Other Credits | Total Purchases and Adjustments |
| --- | --- | --- |
| December | -$102.44 | $90.07 |
| January | -$98.60 | $86.51 |
| February | -$98.60 | $86.51 |

**Simple Graph:**

Unfortunately, I'm a text-based AI and cannot directly plot a graph for you. However, I can describe the general trend of the data.

The total payments and other credits have been decreasing each month, indicating a potential reduction in expenses or an increase in refunds. On the other hand, the total purchases and adjustments have remained relatively stable, suggesting that there has been no significant change in spending habits.

If you need to visualize this data further, I recommend using a graphing tool like Google Sheets, Microsoft Excel, or a dedicated charting software to plot the monthly trends against each other.
```

With the same request sent again could see different response like below snapshot

<img width="2859" height="1583" alt="image" src="https://github.com/user-attachments/assets/97c7fd35-2582-4d86-9087-f68eb3330a45" />

## Deploy Langflow in local Docker container

Below command deploys the Langflow app, the environment variables usage is self explanatory. The Auto login has set since noticed when accessing the Langflow UI the application prompted for username and crendentials

```sh
docker run -d \
--name langflow \
-e DO_NOT_TRACK=true \
-e LANGFLOW_LOG_LEVEL=debug \
-e LANGFLOW_AUTO_LOGIN=true \
-e LANGFLOW_WORKER_TIMEOUT=1800 \
-e AUTH_ACCESS_TOKEN_EXPIRE_MINUTES=-1 \
-e AUTH_REFRESH_TOKEN_EXPIRE_DAYS=-1 \
-p 7860:7860 -v langflow-data:/app/langflow langflowai/langflow
```

To launch the application from browser use `http://localhost:7860`

The screen would look like below

<img width="2501" height="1524" alt="image" src="https://github.com/user-attachments/assets/cd712927-fd28-4f0a-9eac-eb5a68090850" />

The complete RAG system looks like below snapshot

<img width="2765" height="1546" alt="image" src="https://github.com/user-attachments/assets/5081806e-390a-4baf-80ad-96c321d6291c" />

The RAG part of the flow with the Qdrant component, the snapshot shows how other components are connected.
The Read file component reads the input files input files, the split text component will chunk the files and inputs to the Qdrant vector database.

Since we are using the Qdrant database running in local, we need to configure the properties to the component like below.
Leave the host, port and grpc port empty. And in the url property use the Docker Ip of the qdrant container.
To find the container Ip address use `docker network inspect bridge` where bridge is the default network. 

The Qdrant component properties in LangFlow looks like below 
<img width="1070" height="1336" alt="image" src="https://github.com/user-attachments/assets/2b8c1f4d-2fc4-4a7d-9199-a3241d1ff915" />

The Qdrant section zoomed version of components
<img width="2762" height="1544" alt="image" src="https://github.com/user-attachments/assets/484b330d-73d8-4fcc-a369-370d81a28a77" />


The ouput of the playground session
<img width="2537" height="1522" alt="image" src="https://github.com/user-attachments/assets/5e7ace6b-73ae-4c86-973b-dc016364a256" />

