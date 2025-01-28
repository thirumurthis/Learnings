- Below code executes with Jbang
- make sure to run the ollama in docker. Check - https://camel.apache.org/blog/2024/07/data-extraction-first-experiment/

```java
///usr/bin/env jbang "$0" "$0" : exit $?

/* 
 / / SOURCE CustomPojo.java 
 / / SOURCE CamelCustomPojoExtractor.java
*/

//DEPS dev.langchain4j:langchain4j:1.0.0-alpha1
//DEPS dev.langchain4j:langchain4j-ollama:1.0.0-alpha1

//DEPS org.projectlombok:lombok:1.18.36
//DEPS org.slf4j:slf4j-simple:2.1.0-alpha1
//DEPS commons-io:commons-io:2.18.0
//DEPS org.apache.camel:camel-bom:4.9.0@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.slf4j:slf4j-nop:2.1.0-alpha1
//DEPS org.slf4j:slf4j-api:2.1.0-alpha1
/* //DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2 */
import org.apache.camel.*;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import org.apache.camel.spi.*;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.apache.camel.ProducerTemplate;

import lombok.Data;

//import org.apache.camel.model.dataformat.JsonLibrary;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.apache.camel.builder.PredicateBuilder.*;
import static java.lang.System.*;

public class CamelJsonAI{

   public static void main(String[] args) {

        if(args.length==0) {
            System.out.println("no arguments");
        } else {
            System.out.println("args :- " + args);
        }

        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        CamelContext camelContext = new DefaultCamelContext();
        //ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try{
            //Main main = new Main();
     
            ChatLanguageModel model = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("codellama")
            .temperature(0.0)
            .format("json")
            .timeout(Duration.ofMinutes(1L))
            .build();

            CamelCustomPojoExtractor extractionService = AiServices.create(CamelCustomPojoExtractor.class, model);
            camelContext.getRegistry().bind("extractionService", extractionService);
            //camelContext.getRegistry().bind("prettyPrintCustomPojo",CustomPojo.class);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String outFormat ="{\"customerSatisfied\": \"%s\",\"customerName\": \"%s\",\"customerBirthday\": \"%s\",\"summary\": \"%s\"}";

             camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                   // from("timer:hello?period=2500")
                    from("direct:start")
                    .bean(extractionService)
                    //.marshal().json(JsonLibrary.Jackson, objectMapper)
                    .transform(simple("${body}"))
                    .process( exchange -> {
                           CustomPojo customObj = exchange.getIn().getBody(CustomPojo.class);
                           //create a bean
                           String output = "";
                           if(customObj == null){
                             System.out.println("empty response");
                           }else{
                            output = String.format(outFormat,
                             customObj.getCustomerSatisfied(),
                             customObj.getCustomerName(),
                             customObj.getCustomerBirthDay().format(dateFormatter),
                             customObj.getSummary());
                             System.out.println("response "+ output);
                           }
                           exchange.getIn().setBody(output);
                      })
                    //.bean(prettyPrintCustomPojo)
                    .log("${body}");
                    //.to("stream:out");
                };
            });

            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            String transcript ="""
                    Operator: Hello, how may I help you?
                    Customer: Hello, I am currently at the police station because I have got an accident. The police would need a proof that I have an insurance. Could you please help me?
                    Operator: Sure, could you please remind me your name and birth date?
                    Customer: Of course, my name is Kate Hart and I was born on August the thirteen in the year nineteen ninety nine.
                    Operator: I am sorry Kate, but we don't have any contract in our records.
                    Customer: Oh, I am sorry that I have made a mistake. Actually, my last name is not Hart, but Boss. It changed since I am married.
                    Operator: Indeed, I have now found your contract and everything looks good. Shall I send the proof of insurance to the police station?
                    Customer: Oh, if possible, my husband will go directly to your office in order to get it.
                    Operator: Yes, that is possible. I will let the paper at the entrance. Your beloved could just ask it to the front desk.
                    Customer: Many thanks. That is so helpful. I am a bit more relieved now.
                    Operator: Sure, you are welcome Kate. Please come back to us any time in case more information is needed. Bye.
                    Customer: Bye.
                    """;
            template.sendBody("direct:start",transcript);
 
            //template.sendBodyAndHeader("direct:start", "Sam Joined",
            //        "departments", "direct:account,direct:hr,direct:manager");
            //main.configure().addRoutesBuilder(new RouteBuilder(){
           
            //public void configure() throws Exception{
            //    from("timer:hello?period=2500")
            //    .bean(extractionService)
                //.bean(prettyPrintCustomPojo);
               //.process(exchange -> exchange.getIn().setBody(random.nextInt(500)))
               //.setBody(simple("rNum: = ${body}"))
            //   .to("stream:out");
            // }
         //});
        //main.run();
        }catch(Exception exe){
            System.err.println("exception thrown ");
            exe.printStackTrace();
    }finally{
        camelContext.stop();
    }
    }

}

@Data
class CustomPojo {
    private boolean customerSatisfied;
    private String customerName;
    private LocalDate customerBirthday;
    private String summary;
//* 
    public void setCustomerSatisfied(boolean customerSatisfied){
        this.customerSatisfied = customerSatisfied;
    }
    public boolean getCustomerSatisfied(){
        return this.customerSatisfied;
    }

    public void setCustomerName(String customerName){
        this.customerName = customerName;
    }

    public String getCustomerName(){
        return this.customerName;
    }

    public void setCustomerBirthday(LocalDate customerBirthday){
        this.customerBirthday = customerBirthday;
    }

    public LocalDate getCustomerBirthDay(){
        return this.customerBirthday;
    }

    public void setSummary(String summary){
        this.summary = summary;
    }

    public String getSummary(){
        return this.summary;
    }
//*/
  }

interface CamelCustomPojoExtractor {
    @UserMessage(
      "Extract information about a customer from the text delimited by triple backticks: ```{{text}}```." +
      "The customerBirthday field should be formatted as YYYY-MM-DD." +
      "The summary field should concisely relate the customer main ask."
    )
    CustomPojo extractFromText(@V("text") String text);
  }
```
