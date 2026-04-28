///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import java.util.Random;

import static java.lang.System.out;
import static java.lang.System.setProperty;

class DemoPublisher{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        Random random = new Random();
        Main main = new Main();

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("timer:Msg?period=60000")
               .process(exchange -> exchange.getIn().setBody(random.nextInt(1000)))
               .setBody(simple("msg from publisher = ${body}"))
               .to("kafka:test-topic-1?brokers=localhost:31092")
               .to("stream:out");;
            }
         });
        main.run();
    }
}