///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.apache.kafka:kafka-clients:4.2.0
//-- below nop dependency can be used to skip log
//--/ / DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

// ------ not use this import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.camel.dataformat.avro.AvroDataFormat; 

import static java.lang.System.out;
import static java.lang.System.setProperty;;

class AvroConsumer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        setProperty("org.slf4j.simpleLogger.log.org.apache.camel", "info");

        Main main = new Main();

        AvroConsumer consumer = new AvroConsumer();
        AvroDataFormat avroDataFormat = new AvroDataFormat(consumer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("kafka:test-topic-1?brokers=localhost:31092&"
                +"maxPollRecords=1000&consumersCount=1&seekTo=BEGINNING"
                )
                .unmarshal(avroDataFormat)
                //process to print the message from exchange
                //.process(exchange -> System.out.println("inside the consumer"+exchange.getIn().getBody()))
               .log("consumed unmarshalled message: ${body}")
               .to("stream:out");
            }
         });
        //main.run();
        main.start();
        Thread.sleep(10000);
        main.stop();
    }

        public Schema getSchema(){
        String userSchema = """
        {"type": "record",
        "name": "employee",
        "fields":[
            {"name": "name","type":"string"},
            {"name": "address", "type": ["string", "null"]}
            ]}
        """;

        return new Schema.Parser().parse(userSchema);
    }
}