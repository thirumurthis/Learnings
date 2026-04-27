
--to install cert manager

helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.20.2 \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true

-- apisix deployment 
 
helm upgrade --install --create-namespace -n apisix apisix-cp apisix/apisix \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=control_plane \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=selfsigned-apisix-cert-secret \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set apisix.admin.allow.ipList[0]=0.0.0.0/0 \
  --set etcd.replicaCount=3 \
  --set etcd.enabled=true \
  --wait
 
--additional - 
#  --set apisix.ssl.fallbackSNI=apisix.demo.com \
 
-- used this for data_plane 
 
helm upgrade  --install apisix-dp \
  --namespace apisix \
  --create-namespace \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=data_plane \
  --set apisix.nginx.logs.enableAccessLog=true \
  --set apisix.nginx.logs.errorLogLevel=warn \
  --set apisix.admin.enabled=false \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=selfsigned-apisix-cert-secret \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set service.type=NodePort \
  --set service.http.enabled=true \
  --set service.http.servicePort=80 \
  --set service.http.containerPort=9080 \
  --set service.http.nodePort=30080 \
  --set service.tls.servicePort=443 \
  --set service.tls.nodePort=30443 \
  --set ingress-controller.enabled=true \
  --set ingress-controller.apisix.adminService.namespace=apisix-dp \
  --set ingress-controller.gatewayProxy.createDefault=true \
  --set ingress-controller.gatewayProxy.provider.controlPlane.service.name=apisix-cp-admin \
  --set externalEtcd.user="" \
  --set externalEtcd.host[0]=http://apisix-cp-etcd.apisix.svc.cluster.local:2379 \
  --set etcd.enabled=false \
  --set ingress-controller.gatewayProxy.provider.controlPlane.auth.adminKey.value=edd1c9f034335f136f87ad84b625c8f1 \
  apisix/apisix \
  --wait

#  --set apisix.ssl.fallbackSNI=apisix.demo.com \

port forward 
curl -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1" http://localhost:9999/apisix/admin/ssls

  
kubectl -n apisix apply -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/apisix_https.yaml

after deploying try to use http: url http://apisix.demo.com/ui


-- below is not requiired since the configuration added under ssl chart
use admin to load the cert fetched from secret

cd /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/tmp_cert

curl http://127.0.0.1:9999/apisix/admin/ssls/1 \
-H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1" -X PUT -d '
{
     "cert" : "'"$(cat tls.crt)"'",
     "key": "'"$(cat tls.key)"'",
     "snis": ["apisix.demo.com"]
}'

------------

apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: apicurio-ui
spec:
  ingressClassName: apisix 
  http:
    - name: apicurio-ui-http
      match:
        hosts:
          - registry.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: sample-ui-service
          servicePort: 8080
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: apicurio-app
spec:
  ingressClassName: apisix 
  http:
    - name: apicurio-app-http
      match:
        hosts:
          - registry.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: sample-app-service
          servicePort: 8080
---

kubectl apply -n apicurio-registry -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/apicurio_route.yaml
---


apicurio config


#https://www.apicur.io/registry/docs/apicurio-registry/3.2.x/getting-started/assembly-operator-config-reference.html#operator-ingress-reference_registry
apiVersion: registry.apicur.io/v1
kind: ApicurioRegistry3
metadata:
  name: sample
spec:
  app:
    env:
    - name: QUARKUS_HTTP_CORS_ORIGINS
      value: "http://localhost:8888,http://localhost:8080,http://127.0.0.1:8888,http://127.0.0.1:8080"
    - name: QUARKUS_HTTP_CORS
      value: "true"
    - name: QUARKUS_LOG_CONSOLE_JSON
      value: "true"
    ingress:
      enabled: false
    #  host: simple-app.apps.cluster.example:8080 #simple-app-svc.apicurio-registry.svc.cluster-domain.example #simple-ui-service.
  ui:
   env:
    - name: REGISTRY_API_URL
      #value: "sample-app-service.apicurio-registry:8080/apis/registry/v3" #localhost:8099 #sample-app-service.apicurio-registry.svc:8080
      value: "http://localhost:8080/apis/registry/v3"
    - name: QUARKUS_HTTP_CORS_ORIGINS
      value: "http://localhost:8888,http://localhost:8080,http://127.0.0.1:8888,http://127.0.0.1:8080"
    - name: QUARKUS_HTTP_CORS
      value: "true"
    #- name: QUARKUS_LOG_CONSOLE_JSON
    #  value: "true"
   ingress:
    enabled: false
    #host: simple-ui.apps.cluster.example



To acces the apicurio registry https create the keystore
-- from browser exporte the certificate 

keytool -import -file browserCertExport.crt -alias root-ca -keystore truststore.jks -storepass secret -trustcacerts -noprompt


additionalProperties (common)

Sets additional properties for either kafka consumer or kafka producer in case they can’t be set directly on the camel configurations 
(e.g.: new Kafka properties that are not reflected yet in Camel configurations), 
the properties have to be prefixed with additionalProperties..,
 e.g.: additionalProperties.transactional.id=12345&additionalProperties.schema.registry.url=http://localhost:8811/avro. 
 If the properties are set in the application.properties file, 
 they must be prefixed with camel.component.kafka.additional-properties and the property enclosed in square brackets,
 like this example: camel.component.kafka.additional-propertiesdelivery.timeout.ms=15000.
 This is a multi-value option with prefix: additionalProperties.

------------------
java camel code with Apiregistry deployed
Note the pem certificate ca.crt fomr the secret needs to be copied and update the ca cert

### producer

///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS org.apache.avro:avro-compiler:1.12.1
//DEPS org.apache.avro:avro-maven-plugin:1.12.1
//  ##### / / DEPS io.apicurio:apicurio-registry:3.2.1@pom
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.1
//  ##### / / DEPS io.apicurio:apicurio-registry-serdes-avro-serde:3.0.0.M4
// ##### / / DEPS io.apicurio:apicurio-registry-utils-serde:2.4.12.Final

/* $$$$$$$ / /DEPS io.quarkus:quarkus-avro:3.23.0   */

import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.*;
import org.apache.camel.main.Main;
import java.util.Random;
import org.apache.camel.component.kafka.KafkaComponent;

//import javax.net.ssl.KeyStoreBuilderParameters;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericDatumWriter;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;


import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
// Standard Java Security and IO
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

// Apache Camel JSSE (JSSE Utility API)
//import org.apache.camel.support.jsse.KeyStoreParameters;
//import org.apache.camel.support.jsse.SSLContextParameters;
//import org.apache.camel.support.jsse.TrustManagersParameters;

//---don't use this format ---import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.camel.dataformat.avro.AvroDataFormat; 
import org.apache.camel.model.dataformat.AvroLibrary;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.component.jackson.avro.JacksonAvroDataFormat;
import org.apache.camel.component.kafka.KafkaConstants;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.camel.CamelExecutionException;

import static java.lang.System.*;

class AvroProducer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");        

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

        String caCert = """
-----BEGIN CERTIFICATE-----
MIIDEDCCAfigAwIBAgIUN2QC7KftnYYFMq0QbDJZLIHuy88wDQYJKoZIhvcNAQEL
BQAwIDEeMBwGA1UEAxMVYXBpY3VyaW8uYXBwLmRlbW8uY29tMB4XDTI2MDQyNjIw
MDQyM1oXDTI2MDcyNTIwMDQyM1owIDEeMBwGA1UEAxMVYXBpY3VyaW8uYXBwLmRl
bW8uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7M2Jhw7CjwfV
uVSyISicjfEh8tMl0qZvr3nD1H4q0d1mAB2JJ+meulvsout8rsHIzgHgvRxKs6gU
FZljTb+kHAXSQqNytoZ0vZoWA01s6CLcTbIphTStQZ9jkinQLGju2RMrEecLPiW6
+JiGP66hD+NT//AmJE27+KzB5bC17YzRRf4sFsZmIntWwdZUdL2RTTjjkm2Jj08p
97DyNby0bXBOpg18h0qrlDJfY17jlbwBA9ItqObR4FOqmwE+rYTXhCHfXKj0comO
Pc2UrcGxaJ5mly8g+fAp+GDBo15UZkhNbzOop1iAj1K5zpZsDwczZSrhSBHv8Dv3
vBgt0YNKSQIDAQABo0IwQDAOBgNVHQ8BAf8EBAMCBaAwDAYDVR0TAQH/BAIwADAg
BgNVHREEGTAXghVhcGljdXJpby5hcHAuZGVtby5jb20wDQYJKoZIhvcNAQELBQAD
ggEBAKk7HJCyIsRjNMPvfXpzpQkoerudvLmcncRpTWyituAIsaTYQ2EU57CBuFpO
jlO+zTuOJKI3S/o8GOo+DNAAb2XaRQsgnXq/9YOCOsv3BC3uuEiJ5afemN4MxfrP
pY4GXXCj+jkDd0fMSZ+sQJUUkR0b/bRhwdQwLqLmBFgdZXXglfYkrMQ3gSq95wyy
ZGEND7QN0JWCQknGvCkI7v3h9lwnC5xN7KzS/+FlFANV6Z/eIKXKRMTgjje/+1aM
uPj0v5Y+el1uMAGkePjyWzDs3s35CINW4dEV5T++t5/QsT+M4hmvsPUCdAfI2CgE
48zKhAn+6un8kpt08BBlSodA81A=
-----END CERTIFICATE-----        
""";


       Path caPath = Files.writeString(Files.createTempFile("ca", ".pem"), caCert);

       System.out.println("------------- "+caPath.toAbsolutePath());


        List<ByteArrayOutputStream> inputBinaryStream = new ArrayList<>();
        inputBinaryStream.add(streamb1);
        inputBinaryStream.add(streamb2);
        inputBinaryStream.add(streamb3);
        inputBinaryStream.add(streamb4);
        inputBinaryStream.add(streamb5);

        AvroDataFormat avroDataFormat = new AvroDataFormat(producer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());
   

        //AvroMapper avroMapper = new AvroMapper();
        //JacksonAvroDataFormat jacksonAvro = new JacksonAvroDataFormat();
        //AvroSchema jacksonAvroSchema = new AvroSchema(schema);
        //jacksonAvro.setSchema(producer.getSchema());
        //jacksonAvro.setSchemaResolver(exchange -> jacksonAvroSchema);
        
         /*
        from("kafka:my-topic?brokers=localhost:9092"
        + "&valueDeserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer"
        + "&additionalProperties.schema.registry.url=http://localhost:8081"
        + "&additionalProperties.specific.avro.reader=true")
        .log("Consumed Avro message: ${body}");
        */

         //SSLContextParameters scp = createSslContext(caCert); 
        // 3. Add to Registry (if using Spring, mark as @Bean)
        //main.bind("mySSLConfig", scp);
        
        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                /* 
                onException(Exception.class)
                .process(exchange -> {
                    // Trigger a graceful shutdown of the context
                    exchange.getContext().stop();
                    // Optional: Force JVM exit if needed
                    // System.exit(1); 
                }); 
                //*/
                //from("timer:Msg?period=60000")
               //.process(exchange -> exchange.getIn().setBody(random.nextInt(1000)))

                //KafkaComponent kafka = new KafkaComponent();

                //kafka.getConfiguration().getAdditionalProperties().put("schema.registry.url", "https://apicurio.app.demo.com/apis/registry/v3");
                //kafka.getConfiguration().getAdditionalProperties().put("ssl.truststore.type", "PEM");
               //kafka.getConfiguration().getAdditionalProperties().put("ssl.truststore.location", "./caCert.crt");
               // kafka.getConfiguration().getAdditionalProperties().put("ssl.keystore.type", "PEM");
               // kafka.getConfiguration().getAdditionalProperties().put("ssl.keystore.location", "./caCert.crt");
                // kafka.getConfiguration().getAdditionalProperties().put("ssl.key.password", "password"); // if needed

                // Add to registry
                //getContext().addComponent("kafka", kafka);


               onException(Exception.class)
               .process(exchange -> {
                Exception cause = exchange.getException();
                System.out.println("%%%%%%%%%%%%%%%%%%%%%");
                //if(cause != null){
                   System.out.println(cause.getMessage());
                   cause.printStackTrace();
                //}

                System.out.println("%%%%%%%%%%%%%%%%%%%%%");
               })
               .handled(true)
               .stop();
               //.continued(false);

               from("direct:start")
               .split(body())
               /* 
               .process(exchange -> {
                 ByteArrayOutputStream output = exchange.getIn().getBody(ByteArrayOutputStream.class);
                 System.out.println("BEFORE ----- message from direct: "+output); 
                 exchange.getIn().setBody(output.toString(StandardCharsets.UTF_8));
                 System.out.println("AFTER ------ message from direct toString: "+output.toString(StandardCharsets.UTF_8)); 
                })
                //*/
               // .unmarshal().json(JsonLibrary.Jackson) 
                .process(exchange -> {System.out.println("unmarshall data:- "+exchange.getIn().getBody());})
               //.setBody(simple("msg from publisher = ${body}"))
               //.to("log:com.jbang.apachecamellogging?level=INFO")
               .log("publishing msg: ${body}")
               //.setHeader(KafkaConstants.KEY, simple("${body.id}"))
               //.convertBodyTo(byte[].class)
               //.process(exchange -> {
               //  ByteArrayOutputStream output = exchange.getIn().getBody(ByteArrayOutputStream.class);
               //  System.out.println("info from exchange... - "+output);
               //  exchange.getIn().setBody(output.toByteArray()); 
               // })
               //.marshal(jacksonAvro)
               //.marshal(avroDataFormat)
               //.avro()
               
                 //.avro(AvroLibrary.ApacheAvro,avroDataFormat)    
                 //.to("mock:result")
                //check - https://github.com/Apicurio/apicurio-registry/blob/main/schema-resolver/src/main/java/io/apicurio/registry/resolver/config/SchemaResolverConfig.java
               .to("kafka:demo-topic-1?brokers=localhost:31092"
                    + "&valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
                    + "&additionalProperties[apicurio.registry.url]=https://apicurio.app.demo.com/apis/registry/v3"
                    + "&additionalProperties[apicurio.registry.auto-register]=true"
                    //+ "&sslTruststoreLocation=./caCert.crt"
                    //+ "&sslTruststoreType=PEM"
                    //+ "&sslKeystoreType=PEM"
                    //+ "&sslKeystoreLocation=./caCert.crt"
                    //+ "&sslEnabledProtocols=TLSv1.3"
                    //+ "&sslProtocol=TLSV1.3"
                    //+ "&sslContextParameters=#mySSLConfig"
                    //+ "&additionalProperties[apicurio.registry.ssl.truststore.type]=PEM"
                    //+ "&additionalProperties[apicurio.registry.ssl.truststore.location]=./caCert.crt"
                    + "&additionalProperties[apicurio.registry.tls.truststore.type]=PEM"
                    + "&additionalProperties[apicurio.registry.tls.truststore.location]="+ caPath.toAbsolutePath()
                    //+ "&additionalProperties[apicurio.registry.tls.truststore.certificates]=" + caCert
                    //+ "&additionalProperties[apicurio.registry.request.ssl.truststore.password]=secret"
                    //+"&additionalProperties[apicurio.registry.rest.artifact.download.skipSSLValidation]=true"
                    //+ "&additionalProperties[apicurio.registry.request.ssl.skipServerVerification]=false"
                    //+ "&additionalProperties[apicurio.registry.request.ssl.keystore.type]=PEM"
                    //+ "&additionalProperties[apicurio.registry.request.ssl.keystore.location]="+ caPath.toAbsolutePath() //./caCert.crt"
                   // +"valueSerializer=io.confluent.kafka.serializers.KafkaAvroSerializer&"
                   //+"&valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
                   //+"&keySerializer=org.apache.kafka.common.serialization.StringSerializer"
                )
               .to("stream:out");

      //additionalProperties.schema.registry.url=https://schema-registry:8081&
      //additionalProperties.schema.registry.ssl.truststore.location=/path/to/registry.truststore.jks&
      //additionalProperties.schema.registry.ssl.truststore.password=trustpass&
      //additionalProperties.schema.registry.ssl.keystore.location=/path/to/registry.keystore.jks&
      //additionalProperties.schema.registry.ssl.keystore.password=keypass

            }
         });
        main.start();
        // create producer template 
        ProducerTemplate template = main.getCamelContext().createProducerTemplate();



        template.sendBody("direct:start", inputString);

        Thread.sleep(3000);
        main.stop();
        //main.run();
    }

/*
        private static SSLContextParameters createSslContext(String caCertPem) throws Exception {
        // Create the KeyStore in memory
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);

        // Parse the PEM string (handles multiple certs in the chain)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends java.security.cert.Certificate> certs = cf.generateCertificates(
            new ByteArrayInputStream(caCertPem.getBytes(StandardCharsets.UTF_8))
        );

        int i = 0;
        for (java.security.cert.Certificate cert : certs) {
            trustStore.setCertificateEntry("ca-cert-" + i++, cert);
        }

        // Wrap for Camel
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setKeyStore(trustStore);

        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(ksp);

        SSLContextParameters scp = new SSLContextParameters();
        scp.setTrustManagers(tmp);
        
        return scp;
    }
 */
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
            Encoder jsonAvroEncoder = EncoderFactory.get().jsonEncoder(record.getSchema(), output);
            writer.write(record, jsonAvroEncoder);
            jsonAvroEncoder.flush();
        }
       } catch (IOException e) {
         System.err.println("Serialization error:" + e.getMessage());
      }

      return output;

    }
}
---------------------

### consumer
///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.apache.kafka:kafka-clients:4.2.0
// to enable printing the logs use simple not nop
// ---- / / DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.2
// ********* / / DEPS org.apache.avro:avro-compiler:1.21.1
// ********* / / DEPS org.apache.avro:avro-maven-plugin:1.12.1

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import org.apache.camel.dataformat.avro.AvroDataFormat; 
// ------ not use this import org.apache.camel.model.dataformat.AvroDataFormat;

import static java.lang.System.*;

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

                String caCert = """
-----BEGIN CERTIFICATE-----
MIIDEDCCAfigAwIBAgIUN2QC7KftnYYFMq0QbDJZLIHuy88wDQYJKoZIhvcNAQEL
BQAwIDEeMBwGA1UEAxMVYXBpY3VyaW8uYXBwLmRlbW8uY29tMB4XDTI2MDQyNjIw
MDQyM1oXDTI2MDcyNTIwMDQyM1owIDEeMBwGA1UEAxMVYXBpY3VyaW8uYXBwLmRl
bW8uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7M2Jhw7CjwfV
uVSyISicjfEh8tMl0qZvr3nD1H4q0d1mAB2JJ+meulvsout8rsHIzgHgvRxKs6gU
FZljTb+kHAXSQqNytoZ0vZoWA01s6CLcTbIphTStQZ9jkinQLGju2RMrEecLPiW6
+JiGP66hD+NT//AmJE27+KzB5bC17YzRRf4sFsZmIntWwdZUdL2RTTjjkm2Jj08p
97DyNby0bXBOpg18h0qrlDJfY17jlbwBA9ItqObR4FOqmwE+rYTXhCHfXKj0comO
Pc2UrcGxaJ5mly8g+fAp+GDBo15UZkhNbzOop1iAj1K5zpZsDwczZSrhSBHv8Dv3
vBgt0YNKSQIDAQABo0IwQDAOBgNVHQ8BAf8EBAMCBaAwDAYDVR0TAQH/BAIwADAg
BgNVHREEGTAXghVhcGljdXJpby5hcHAuZGVtby5jb20wDQYJKoZIhvcNAQELBQAD
ggEBAKk7HJCyIsRjNMPvfXpzpQkoerudvLmcncRpTWyituAIsaTYQ2EU57CBuFpO
jlO+zTuOJKI3S/o8GOo+DNAAb2XaRQsgnXq/9YOCOsv3BC3uuEiJ5afemN4MxfrP
pY4GXXCj+jkDd0fMSZ+sQJUUkR0b/bRhwdQwLqLmBFgdZXXglfYkrMQ3gSq95wyy
ZGEND7QN0JWCQknGvCkI7v3h9lwnC5xN7KzS/+FlFANV6Z/eIKXKRMTgjje/+1aM
uPj0v5Y+el1uMAGkePjyWzDs3s35CINW4dEV5T++t5/QsT+M4hmvsPUCdAfI2CgE
48zKhAn+6un8kpt08BBlSodA81A=
-----END CERTIFICATE-----        
""";

       Path caPath = Files.writeString(Files.createTempFile("ca", ".pem"), caCert);
       System.out.println("certificate path "+caPath.toAbsolutePath());

        /*
        from("direct:start")
       .to("kafka:my-topic?brokers=localhost:9092"
        + "&valueSerializer=io.confluent.kafka.serializers.KafkaAvroSerializer"
        + "&additionalProperties.schema.registry.url=http://localhost:8081");
         */

       /*
       from("kafka:my-topic?" +
        "brokers=localhost:9092&" +
        "valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer&" +
        "additionalProperties[apicurio.registry.url]=http://localhost:8080/apis/registry/v2&" +
        "additionalProperties[apicurio.registry.use-specific-avro-reader]=true")
        .log("Message received: ${body}");
       */

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{

                               onException(Exception.class)
               .process(exchange -> {
                Exception cause = exchange.getException();
                System.out.println("%%%%%%%%%%%%%%%%%%%%%");
                //if(cause != null){
                   System.out.println(cause.getMessage());
                   cause.printStackTrace();
                //}

                System.out.println("%%%%%%%%%%%%%%%%%%%%%");
               })
               .handled(true)
               .stop();

                from("kafka:demo-topic-1?brokers=localhost:31092" 
                //+"&maxPollRecords=1000&consumersCount=1"
                +"&seekTo=BEGINNING"
                +"&groupId=${bean:java.util.UUID?method=randomUUID}" 
                +"&valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer" 
                + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
                +"&additionalProperties[apicurio.registry.url]=https://apicurio.app.demo.com/apis/registry/v3" 
                +"&additionalProperties[apicurio.registry.auto-register]=true" 
                //+ "&additionalProperties[apicurio.registry.request.ssl.truststore.type]=PEM"
                //+ "&additionalProperties[apicurio.registry.request.ssl.truststore.location]=" + caPath.toAbsolutePath() 
                +"&additionalProperties[apicurio.registry.tls.truststore.type]=PEM"
                +"&additionalProperties[apicurio.registry.tls.truststore.location]="+ caPath.toAbsolutePath() 
                //+"&additionalProperties[apicurio.registry.avro-datum-provider]=io.apicurio.registry.serde.avro.DefaultAvroDatumProvider"
                )
                .process(exchange -> {
                    //byte[] data = exchange.getIn().getBody(byte[].class);
                    System.out.println("inside the consumer"+exchange.getIn().getBody());
                    //exchange.getIn().setBody(data);
                })
                .log("Consumed Avro Message: ${body}")
                .to("stream:out");
                /* 
                from("kafka:demo-topic-1?brokers=localhost:31092&"
                +"maxPollRecords=1000&consumersCount=1&seekTo=BEGINNING"
                //+"&groupId=kafkaGroup"
                //+"&valueDeserializer=org.apache.kafka.common.serializer.ByteArrayDeserializer"
                )
                .unmarshal(avroDataFormat)
                //.unmarshal().avro()
                //.setHeader("CamelKafkaGroupId", simple("${random(1000,9999)}"))
                .process(exchange -> {
                    //byte[] data = exchange.getIn().getBody(byte[].class);
                    System.out.println("inside the consumer"+exchange.getIn().getBody());
                    //exchange.getIn().setBody(data);
                })
               .log("consumed: ${body}")
               .to("stream:out");
               */
            }
         });
        //main.run();
        Thread.sleep(3000);
        main.start();
        Thread.sleep(10000);
        main.stop();
    }

    /*
    public dataDecoder(){
         DatumReader<GenericRecord> reader
                = new SpecificDatumReader<>(Vehicle.class);
        Decoder decoder = null;
        try {
            decoder = DecoderFactory.get().jsonDecoder(
                    Vehicle.getClassSchema(), new String(data));
            return reader.read(null, decoder);
    }
 */
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
}
--------------------
#####
####  Avro consumer 
######

///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.apache.kafka:kafka-clients:4.2.0
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
// ********* / / DEPS org.apache.avro:avro-compiler:1.21.1
// ********* / / DEPS org.apache.avro:avro-maven-plugin:1.12.1

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import org.apache.camel.dataformat.avro.AvroDataFormat; 
// ------ not use this import org.apache.camel.model.dataformat.AvroDataFormat;

import static java.lang.System.*;

class AvroConsumer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        Main main = new Main();

        AvroConsumer consumer = new AvroConsumer();
        AvroDataFormat avroDataFormat = new AvroDataFormat(consumer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());

        /*
        from("direct:start")
       .to("kafka:my-topic?brokers=localhost:9092"
        + "&valueSerializer=io.confluent.kafka.serializers.KafkaAvroSerializer"
        + "&additionalProperties.schema.registry.url=http://localhost:8081");
         */

       /*
       from("kafka:my-topic?" +
        "brokers=localhost:9092&" +
        "valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer&" +
        "additionalProperties[apicurio.registry.url]=http://localhost:8080/apis/registry/v2&" +
        "additionalProperties[apicurio.registry.use-specific-avro-reader]=true")
        .log("Message received: ${body}");
       */

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("kafka:demo-topic-1?brokers=localhost:31092&"
                +"maxPollRecords=1000&consumersCount=1&seekTo=BEGINNING"
                //+"&groupId=kafkaGroup"
                //+"&valueDeserializer=org.apache.kafka.common.serializer.ByteArrayDeserializer"
                )
                .unmarshal(avroDataFormat)
                //.unmarshal().avro()
                //.setHeader("CamelKafkaGroupId", simple("${random(1000,9999)}"))
                .process(exchange -> {
                    //byte[] data = exchange.getIn().getBody(byte[].class);
                    System.out.println("inside the consumer"+exchange.getIn().getBody());
                    //exchange.getIn().setBody(data);
                })
               .log("consumed: ${body}")
               .to("stream:out");
            }
         });
        //main.run();
        main.start();
        Thread.sleep(10000);
        main.stop();
    }

    /*
    public dataDecoder(){
         DatumReader<GenericRecord> reader
                = new SpecificDatumReader<>(Vehicle.class);
        Decoder decoder = null;
        try {
            decoder = DecoderFactory.get().jsonDecoder(
                    Vehicle.getClassSchema(), new String(data));
            return reader.read(null, decoder);
    }
 */
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
}

---
## Avro producer 

///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS org.apache.avro:avro-compiler:1.12.1
//DEPS org.apache.avro:avro-maven-plugin:1.12.1
//  ##### / / DEPS io.apicurio:apicurio-registry:3.2.1@pom
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.1
//  ##### / / DEPS io.apicurio:apicurio-registry-serdes-avro-serde:3.0.0.M4
// ##### / / DEPS io.apicurio:apicurio-registry-utils-serde:2.4.12.Final

/* $$$$$$$ / /DEPS io.quarkus:quarkus-avro:3.23.0   */

import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import java.util.Random;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericDatumWriter;
//import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;

import io.vertx.ext.auth.User;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
//---don't use this format ---import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.camel.dataformat.avro.AvroDataFormat; 
import org.apache.camel.model.dataformat.AvroLibrary;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.component.jackson.avro.JacksonAvroDataFormat;
import org.apache.camel.component.kafka.KafkaConstants;
import java.util.List;
import java.util.ArrayList;

import static java.lang.System.*;

class AvroProducer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");        

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
   

        //AvroMapper avroMapper = new AvroMapper();
        //JacksonAvroDataFormat jacksonAvro = new JacksonAvroDataFormat();
        //AvroSchema jacksonAvroSchema = new AvroSchema(schema);
        //jacksonAvro.setSchema(producer.getSchema());
        //jacksonAvro.setSchemaResolver(exchange -> jacksonAvroSchema);
        
         /*
        from("kafka:my-topic?brokers=localhost:9092"
        + "&valueDeserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer"
        + "&additionalProperties.schema.registry.url=http://localhost:8081"
        + "&additionalProperties.specific.avro.reader=true")
        .log("Consumed Avro message: ${body}");
        */

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                /* 
                onException(Exception.class)
                .process(exchange -> {
                    // Trigger a graceful shutdown of the context
                    exchange.getContext().stop();
                    // Optional: Force JVM exit if needed
                    // System.exit(1); 
                }); 
                //*/
                //from("timer:Msg?period=60000")
               //.process(exchange -> exchange.getIn().setBody(random.nextInt(1000)))
               from("direct:start")
               .split(body())
               /* 
               .process(exchange -> {
                 ByteArrayOutputStream output = exchange.getIn().getBody(ByteArrayOutputStream.class);
                 System.out.println("BEFORE ----- message from direct: "+output); 
                 exchange.getIn().setBody(output.toString(StandardCharsets.UTF_8));
                 System.out.println("AFTER ------ message from direct toString: "+output.toString(StandardCharsets.UTF_8)); 
                })
                //*/
               // .unmarshal().json(JsonLibrary.Jackson) 
                .process(exchange -> {System.out.println("unmarshall data:- "+exchange.getIn().getBody());})
               //.setBody(simple("msg from publisher = ${body}"))
               //.to("log:com.jbang.apachecamellogging?level=INFO")
               .log("publishing msg: ${body}")
               //.setHeader(KafkaConstants.KEY, simple("${body.id}"))
               //.convertBodyTo(byte[].class)
               //.process(exchange -> {
               //  ByteArrayOutputStream output = exchange.getIn().getBody(ByteArrayOutputStream.class);
               //  System.out.println("info from exchange... - "+output);
               //  exchange.getIn().setBody(output.toByteArray()); 
               // })
               //.marshal(jacksonAvro)
               .marshal(avroDataFormat)
               //.avro()
               
                 //.avro(AvroLibrary.ApacheAvro,avroDataFormat)    
                 //.to("mock:result")
           
               .to("kafka:demo-topic-1?brokers=localhost:31092"
                   +"&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer"
                   // +"valueSerializer=io.confluent.kafka.serializers.KafkaAvroSerializer&"
                   //+"&valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
                   //+"&keySerializer=org.apache.kafka.common.serialization.StringSerializer"
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
            Encoder jsonAvroEncoder = EncoderFactory.get().jsonEncoder(record.getSchema(), output);
            writer.write(record, jsonAvroEncoder);
            jsonAvroEncoder.flush();
        }
       } catch (IOException e) {
         System.err.println("Serialization error:" + e.getMessage());
      }

      return output;

    }
}
------------------ 

--------------------

###########
###
###  With no Api registry
#########

# consumer 

///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.kafka:kafka-clients:4.2.0
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13

import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import java.util.Random;


import static java.lang.System.*;

class DemoConsumer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");

        Main main = new Main();

        /*
        from("direct:start")
       .to("kafka:my-topic?brokers=localhost:9092"
        + "&valueSerializer=io.confluent.kafka.serializers.KafkaAvroSerializer"
        + "&additionalProperties.schema.registry.url=http://localhost:8081");
         */

       /*
       from("kafka:my-topic?" +
        "brokers=localhost:9092&" +
        "valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer&" +
        "additionalProperties[apicurio.registry.url]=http://localhost:8080/apis/registry/v2&" +
        "additionalProperties[apicurio.registry.use-specific-avro-reader]=true")
        .log("Message received: ${body}");
       */

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
-----------

## producer
///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import java.util.Random;

import static java.lang.System.*;

class DemoPublisher{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        Random random = new Random();
        Main main = new Main();

        /*
        from("kafka:my-topic?brokers=localhost:9092"
        + "&valueDeserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer"
        + "&additionalProperties.schema.registry.url=http://localhost:8081"
        + "&additionalProperties.specific.avro.reader=true")
        .log("Consumed Avro message: ${body}");
        */

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("timer:Msg?period=60000")
               .process(exchange -> exchange.getIn().setBody(random.nextInt(1000)))
               .setBody(simple("msg from publisher = ${body}"))
               //.to("log:com.jbang.apachecamellogging?level=INFO")
               //.log(LoggingLevel.INFO,"publishing msg: ${body}")
               .to("kafka:test-topic-1?brokers=localhost:31092")
               .to("stream:out");;
            }
         });
        main.run();
    }
}
---------------

For slf4j logs enable the simple jar  to print the logs
For certificate pikx error make sure the latest 3.2.1 deserialize rversion