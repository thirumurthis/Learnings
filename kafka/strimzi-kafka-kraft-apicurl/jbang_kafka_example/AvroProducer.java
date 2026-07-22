///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
// -- below nop dependency to skip the logs
//-- //DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS org.apache.avro:avro-compiler:1.12.1
//DEPS org.apache.avro:avro-maven-plugin:1.12.1
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.1

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.generic.GenericDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
//---don't use below format 
// ---import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.camel.dataformat.avro.AvroDataFormat;

import java.util.List;
import java.util.ArrayList;

import static java.lang.System.out;
import static java.lang.System.setProperty;;

class AvroProducer {

    public static void main(String... args) throws Exception {
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        setProperty("org.slf4j.simpleLogger.log.org.apache.camel", "info");

        Main main = new Main();

        AvroProducer producer = new AvroProducer();
        Schema schema = producer.getSchema();

        GenericRecord record1 = producer.getRecord(schema, "User1", "11,somewhere,country");
        GenericRecord record2 = producer.getRecord(schema, "User2", "12,somewhere,country");
        GenericRecord record3 = producer.getRecord(schema, "User3", "13,somewhere,country");
        GenericRecord record4 = producer.getRecord(schema, "User4", "15,somewhere,country");
        GenericRecord record5 = producer.getRecord(schema, "User5", "15,somewhere,country");

        List<GenericRecord> inputRecords = new ArrayList<>();

        inputRecords.add(record1);
        inputRecords.add(record2);
        inputRecords.add(record3);
        inputRecords.add(record4);
        inputRecords.add(record5);

        AvroDataFormat avroDataFormat = new AvroDataFormat(producer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());

        main.configure().addRoutesBuilder(new RouteBuilder() {
            public void configure() throws Exception {

                onException(Exception.class)
                        .process(exchange -> {
                            Exception cause = exchange.getException();
                            cause.printStackTrace();
                            System.out.println("---x[Exception occurred]x---");
                        })
                        .handled(true)
                        .stop();

                from("direct:start")
                        .split(body())
                        .process(exchange -> {
                            System.out.println("unmarshall data:- " + exchange.getIn().getBody());
                        })
                        .log("publishing msg: ${body}")
                        .marshal(avroDataFormat)
                        .to("kafka:test-topic-1?brokers=localhost:31092"
                                + "&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer")
                        .to("stream:out");

            }
        });
        main.start();
        // create producer template
        ProducerTemplate template = main.getCamelContext().createProducerTemplate();

        template.sendBody("direct:start", inputRecords);

        Thread.sleep(5000);
        main.stop();
        // main.run();
    }

    public Schema getSchema() {
        String userSchema = """
                {"type": "record",
                "name": "employee",
                "fields":[
                    {"name": "name","type":"string"},
                    {"name": "address", "type": ["string", "null"]}
                    ]}
                """;

        // Schema.Parser parser = new Schema.Parser();
        return new Schema.Parser().parse(userSchema);
        // return schema;
    }

    public GenericRecord getRecord(Schema schema, String name, String address) {

        GenericRecord record = new GenericData.Record(schema);
        record.put("name", name);
        record.put("address", address);

        return record;
    }

    public ByteArrayOutputStream getByteStream(Schema schema, GenericRecord record, boolean binaryEncoder,
            boolean jsonEncoder) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());

        try {
            if (binaryEncoder) {
                BinaryEncoder binaryAvroEncoder = EncoderFactory.get().binaryEncoder(output, null);
                writer.write(record, binaryAvroEncoder);
                binaryAvroEncoder.flush();
            }
            if (jsonEncoder) {
                JsonEncoder jsonAvroEncoder = EncoderFactory.get().jsonEncoder(record.getSchema(), output);
                writer.write(record, jsonAvroEncoder);
                jsonAvroEncoder.flush();
            }
        } catch (IOException e) {
            System.err.println("Serialization error:" + e.getMessage());
        }

        return output;

    }
}