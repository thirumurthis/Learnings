///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.kafka:kafka-clients:4.2.0
//-- //nop dependency to skip log
//-- //DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

import static java.lang.System.out;
import static java.lang.System.setProperty;

class DemoConsumer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");

        Main main = new Main();

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("kafka:test-topic-1?brokers=localhost:31092&"+
                "maxPollRecords=1000&consumersCount=1&seekTo=BEGINNING&"+
                "groupId=kafkaGroup")
               .log("consumed: ${body}")
               .to("stream:out");
            }
         });
        main.run();
    }
}