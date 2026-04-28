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

class AvroProducer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out"); 
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        setProperty("org.slf4j.simpleLogger.log.org.apache.camel", "info");       

        Main main = new Main();

        List<String> inputString = new ArrayList<>();

        String str1 = """
        {"name":"User1","address":{"string":"11,somewhere,country"}}
        """;
        String str2="""
        {"name":"User2","address":{"string":"12,somewhere,country"}}
        """;
        String str3="""
        {"name":"User3","address":{"string":"13,somewhere,country"}}
        """;
        String str4="""
        {"name":"User4","address":{"string":"15,somewhere,country"}}
        """;
        inputString.add(str1);
        inputString.add(str2);
        inputString.add(str3);
        inputString.add(str4);

        AvroProducer producer = new AvroProducer();
        Schema schema = producer.getSchema();

        GenericRecord record1 = producer.getRecord(schema,"User1","11,somewhere,country");
        GenericRecord record2 = producer.getRecord(schema,"User2","12,somewhere,country");
        GenericRecord record3 = producer.getRecord(schema,"User3","13,somewhere,country");
        GenericRecord record4 = producer.getRecord(schema,"User4","15,somewhere,country");
        GenericRecord record5 = producer.getRecord(schema,"User5","15,somewhere,country");    
        
        List<GenericRecord> inputRecords = new ArrayList<>();

        inputRecords.add(record1);
        inputRecords.add(record2);
        inputRecords.add(record3);
        inputRecords.add(record4);
        inputRecords.add(record5);

        List<ByteArrayOutputStream> inputJsonStream = new ArrayList<>();
        List<byte[]> inputJsonByteArray = new ArrayList<>();
    
        ByteArrayOutputStream stream1 =  producer.getByteStream(schema, record1,false,true);
        ByteArrayOutputStream stream2 =  producer.getByteStream(schema, record2,false,true);
        ByteArrayOutputStream stream3 =  producer.getByteStream(schema, record3,false,true);
        ByteArrayOutputStream stream4 =  producer.getByteStream(schema, record4,false,true);
        ByteArrayOutputStream stream5 =  producer.getByteStream(schema, record5,false,true);

        inputJsonStream.add(stream1);
        inputJsonStream.add(stream2);
        inputJsonStream.add(stream3);
        inputJsonStream.add(stream4);
        inputJsonStream.add(stream5);

        inputJsonByteArray.add(stream1.toByteArray());
        inputJsonByteArray.add(stream2.toByteArray());
        inputJsonByteArray.add(stream3.toByteArray());
        inputJsonByteArray.add(stream4.toByteArray());
        inputJsonByteArray.add(stream5.toByteArray());
       
        
        System.out.println("number of input "+inputJsonStream.size());
    
        System.out.println("print jsonstream values");
        inputJsonStream.forEach(System.out::println);
        System.out.println("print jsonbytearray values");
        inputJsonByteArray.forEach(System.out::println);

        ByteArrayOutputStream streamb1 =  producer.getByteStream(schema, record1,false,true);
        ByteArrayOutputStream streamb2 =  producer.getByteStream(schema, record2,false,true);
        ByteArrayOutputStream streamb3 =  producer.getByteStream(schema, record3,false,true);
        ByteArrayOutputStream streamb4 =  producer.getByteStream(schema, record4,false,true);
        ByteArrayOutputStream streamb5 =  producer.getByteStream(schema, record5,false,true);


        List<ByteArrayOutputStream> inputBinaryStream = new ArrayList<>();
        inputBinaryStream.add(streamb1);
        inputBinaryStream.add(streamb2);
        inputBinaryStream.add(streamb3);
        inputBinaryStream.add(streamb4);
        inputBinaryStream.add(streamb5);

        AvroDataFormat avroDataFormat = new AvroDataFormat(producer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{

                        
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
               .process(exchange -> {System.out.println("unmarshall data:- "+exchange.getIn().getBody());})
               .log("publishing msg: ${body}")
               .marshal(avroDataFormat)
               .to("kafka:test-topic-1?brokers=localhost:31092"
                   +"&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer"
                )
               .to("stream:out");

            }
         });
        main.start();
        // create producer template 
        ProducerTemplate template = main.getCamelContext().createProducerTemplate();


        template.sendBody("direct:start", inputRecords);

        Thread.sleep(5000);
        main.stop();
        //main.run();
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

       // Schema.Parser parser = new Schema.Parser();
        return new Schema.Parser().parse(userSchema);
        //return schema;
    }

    public GenericRecord getRecord(Schema schema, String name, String address){

        GenericRecord record = new GenericData.Record(schema);
        record.put("name", name);
        record.put("address", address);

        return record;
    }

    public ByteArrayOutputStream getByteStream(Schema schema, GenericRecord record, boolean binaryEncoder, boolean jsonEncoder) throws IOException{
      ByteArrayOutputStream output = new ByteArrayOutputStream();


      DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());

      try {
        if(binaryEncoder){
            BinaryEncoder binaryAvroEncoder = EncoderFactory.get().binaryEncoder(output, null);
            writer.write(record, binaryAvroEncoder);
            binaryAvroEncoder.flush();
        }
        if (jsonEncoder){
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