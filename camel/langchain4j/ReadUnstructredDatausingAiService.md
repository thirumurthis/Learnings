## Extract data from unstructured data using AI service with Camel, Langchain4j and JBang 

In this blog we use AI service to extract data from the unstructured data provided. This is inspired from the Camel blog - [Extraction from unstructured data with Apache Camel and Langchan4j](https://camel.apache.org/blog/2024/07/data-extraction-first-experiment/).

### Pre-requisites

To understand and execute the code below would be required

 - Docker desktop to run the ollama-codellama image
 - Jbang CLI - For installation refer [Jbang installation documentation](https://www.jbang.dev/documentation/guide/latest/installation.html)
 - Basic understanding of Camel and usage of Langchain4j 

### Summary

In this blog we use CURL to send the JSON payload with POST endpoint the data will be routed by camel to running AI service and based on the user message configured in Langchain4j inerface the data would be extracted and a custom POJO object would be created as defined.

The unstructured input data in this case would be transcript between a customer and representative. The data includes Name of the customer, Date of Birth and conversation, AI service will infer the data based on the configured usermessage and summarize the unstructured data. 

#### Run Ollama codellama service

To run the docker service use below command. Make sure to run the service before executing below Java code with JBang. 

```sh
docker run -p 11434:11434 langchain4j/ollama-codellama:latest
```

Note, once the docker service is up to make if the service is accessible use below command to get repsonse
```sh
curl -X POST http://localhost:11434/api/generate -d '{
  "model": "codellama",
  "prompt": "Write me a function that outputs the fibonacci sequence"
}'
```

#### Code

- `Jbang` is used here since it doesn't require the usual java project structure and we can execute java code as script.

-  The code below uses Camel routes, the data is sent to the Camel rest route which exposes an POST endpoint using the Camel Jetty server component. The data in the camel exchange is routed to AI service endpoint and the response is printed and sent as response.

- The `CustomPojoExtractor` interface defined includes the `@UserMessage` annotation is configured with set of message. AI service uses this message and passed data to extract the necessary details. The response from the AI service is set to `CustomPojo` object. 

-  The `extractorService()` method in the `RestAiRouteConfig` class uses the `AiServices` of Langchain4j to create the bean used in the camel route. The AiService uses the `ChatLanguageModel` and `CustomPojoExtractor` interface object to create a bean called `extractionService` which is  registered to the Camel context. This registered bean is used in the routes which will invoke the `extractFromText()` method of the interface when data is received.

 - The `OllamaChatModel` builder is used to create the `ChatLanguageModel` which uses the URL of the AI docker service and model name. Refer the Langchain4j documentation for more understanding. 

- The `CustomPojo` is a simple Java record and the `toString()` is overridden in here to return the object as JSON string.


```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS dev.langchain4j:langchain4j:1.0.0-alpha1
//DEPS dev.langchain4j:langchain4j-ollama:1.0.0-alpha1

//DEPS org.slf4j:slf4j-simple:2.1.0-alpha1
//DEPS commons-io:commons-io:2.18.0
//DEPS org.apache.camel:camel-bom:4.9.0@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-direct
//DEPS org.apache.camel:camel-rest
//DEPS org.apache.camel:camel-jetty
//DEPS org.apache.camel:camel-http
//DEPS com.fasterxml.jackson.core:jackson-core:2.18.2
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2
//DEPS com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2

import org.apache.camel.main.Main;
import org.apache.camel.builder.RouteBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.time.Duration;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import static java.lang.System.*;


public class CamelLangChain4jAIExtractor {

    public static final String AI_SERVICE_BASE_URL = "http://localhost:11434";
    public static final String MODEL_NAME = "codellama";

    public static final String SERVERCOMPONENT = "jetty";
    public static final String LOCALHOST = "localhost";
    public static final int PORT =  9090;
   
    public static void main(String[] args) {

        out.println("Starting camel route...");
        try {
            Main main = new Main();

            main.configure().addRoutesBuilder(new RestAiRouteConfig());
            out.println("camel service started localhost 9090");
            main.run();
        } catch (Exception exe) {
            err.println("exception thrown ");
            exe.printStackTrace();
        }
    }

    public static class RestAiRouteConfig extends RouteBuilder {

        @Override
        public void configure() {

            CustomPojoExtractor extractionService = extractorService();
            getContext().getRegistry().bind("extractionService", extractionService);

            restConfiguration()
                    .component(SERVERCOMPONENT)
                    .host(LOCALHOST)
                    .port(PORT)
                    .bindingMode("auto");

            rest()
                    .post("/transcript")
                    .consumes("application/json")
                    .produces("application/json")
                    .to("direct:processTranscript");

            from("direct:processTranscript")
                    .log("incoming message - ${body}")
                    .bean(extractionService)
                    .log("output message - \n${body}");

        }

        CustomPojoExtractor extractorService() {
            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl(AI_SERVICE_BASE_URL)
                    .modelName(MODEL_NAME)
                    .temperature(0.0)
                    // .format("json")
                    .timeout(Duration.ofMinutes(1L))
                    .build();

            CustomPojoExtractor extractionService = AiServices.create(CustomPojoExtractor.class, model);
            return extractionService;
        }
    }

    public record CustomPojo(
            boolean customerSatisfied,
            String customerName,
            LocalDate customerBirthday,
            String summary) {

        @Override
        public String toString() {
            try {

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                return mapper.writeValueAsString(this);
            } catch (Exception e) {
                return "Error converting to JSON: " + e.getMessage();
            }
        }
    }

    interface CustomPojoExtractor {
        @UserMessage(
          "Extract information about a customer from the text delimited by triple backticks: ```{{text}}```." +
          "The customerBirthday field should be formatted as YYYY-MM-DD." +
          "The summary field should concisely relate the customer main ask."
        )
        CustomPojo extractFromText(@V("text") String text);
      }
}

```

#### Run the code:

Save the above code to file `CamelLangChain4jAIExtractor.java`, then issue below command

```sh
jbang CamelLangChain4jAIExtractor.java
```

##### Input Output

Once the application is up, use Git Base to send the data using curl command.

- Sample Input
```
curl -XPOST http://localhost:9090/transcript \
-H 'Content-Type: application/json' \
-d '{ "data":"customer: hello. \
operator: how can I help you. \
customer: I need to return one of the product I purchased from your platform. \
operator: ok. is there an issue. \
customer: yes. it does not work as expected and need a replacement. \
operator: can you provide your name and dob? \
customer: John I Doe and born on Labor day 30 years ago. \
operator: ok. you will receive the shipping label soon. \
operator: can you provide feed back on the product please? \
customer: I am not satisfied with the product wouldn\''t recommend the product."}'

```
Output

```
{"customerSatisfied":false,"customerName":"John I Doe","customerBirthday":"1997-09-04","summary":"Customer is not satisfied with the product and will not recommend it."}
```

- Sample Input 


```
curl -XPOST http://localhost:9090/transcript \
-H 'Content-Type: application/json' \
-d '{ "data":"customer: hello. \
operator: how can I help you. \
customer: I would like to provide a review for one of the recently purchased product. \
operator: Sure. can you provide your name and birthdate info? \
customer: Jonny II Doe and born independence day 45 years ago. \
operator: how do you like the product? \
customer: the product is useful and would recommend to any one. \
operator: Is there anything i can help with. \
customer: no thanks."}'
```

output
```
{"customerSatisfied":true,"customerName":"Jonny II Doe","customerBirthday":"1978-07-04","summary":"Customer provided a review for a recently purchased product and would recommend it to others."}
```

![image](https://github.com/user-attachments/assets/91ac3fdb-33ee-433b-b480-f488f2e6cbc4)


