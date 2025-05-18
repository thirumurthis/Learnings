- pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>GrpcDemo</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <name>GrpcDemo</name>
  <url>http://maven.apache.org</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <grpc.version>1.56.0</grpc.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-netty-shaded</artifactId>
      <version>${grpc.version}</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-protobuf</artifactId>
      <version>${grpc.version}</version>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-stub</artifactId>
      <version>${grpc.version}</version>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-services</artifactId>
      <version>${grpc.version}</version>
    </dependency>

    <dependency> <!-- necessary for Java 9+ -->
      <groupId>org.apache.tomcat</groupId>
      <artifactId>annotations-api</artifactId>
      <version>6.0.53</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.28</version>
      <!--<scope>provided</scope>-->
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-testing</artifactId>
      <version>${grpc.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>2.0.7</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>2.0.7</version>
    </dependency>
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-slf4j-impl</artifactId>
      <version>2.18.0</version>
    </dependency>
<!--    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.4.5</version>
    </dependency>-->
  </dependencies>
  <build>
    <extensions>
      <extension>
        <groupId>kr.motd.maven</groupId>
        <artifactId>os-maven-plugin</artifactId>
        <version>1.7.1</version>
      </extension>
    </extensions>
    <plugins>
      <plugin>
        <groupId>org.xolstice.maven.plugins</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>0.6.1</version>
        <configuration>
          <protocArtifact>com.google.protobuf:protoc:3.22.3:exe:${os.detected.classifier}</protocArtifact>
          <pluginId>grpc-java</pluginId>
          <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.56.0:exe:${os.detected.classifier}</pluginArtifact>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
              <goal>compile-custom</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <!--
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <version>1.4.1</version>
        <executions>
          <execution>
            <id>enforce</id>
            <goals>
              <goal>enforce</goal>
            </goals>
            <configuration>
              <rules>
                <requireUpperBoundDeps/>
              </rules>
            </configuration>
          </execution>
        </executions>
      </plugin>
      -->
    </plugins>
  </build>
</project>
```

- greetings.proto

```proto
syntax = "proto3";

package com.grpc.app;

option java_multiple_files = true;

enum TimeOfDay {
    MORNING = 0;
    AFTERNOON = 1;
    EVENING = 2;
    NIGHT = 3;
}

message GreetingRequest {
    string name = 1;
}

message GreetingResponse {
    string message = 1;
    TimeOfDay dayOfTime = 2;
}

service GreetingService {
    rpc greeting(GreetingRequest) returns (GreetingResponse);
}
```

- GrpcServer.java

```java
package org.grpc.server;

import com.grpc.app.GreetingRequest;
import com.grpc.app.GreetingResponse;
import com.grpc.app.GreetingServiceGrpc;
import io.grpc.protobuf.services.ProtoReflectionService;
import com.grpc.app.TimeOfDay;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalTime;

public class GrpcServer {


    static public void main(String [] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(ProtoReflectionService.newInstance())
                .addService(new GreetingServiceImpl()).build();

        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started!");
        server.awaitTermination();
    }

    public static class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
        @Override
        public void greeting(GreetingRequest request,
                             StreamObserver<GreetingResponse> responseObserver) {
            System.out.println(request);

            LocalTime currentTime = LocalTime.now();

            int hour = currentTime.getHour();
            TimeOfDay time;
            if(hour >=6 && hour <12){
                time = TimeOfDay.MORNING;
            }else if( hour >=12 && hour <18){
                time = TimeOfDay.AFTERNOON;
            }else {
                time = TimeOfDay.EVENING;
            }

            String greeting = "Hello there, " + request.getName();

            GreetingResponse response = GreetingResponse
                    .newBuilder()
                    .setMessage(greeting)
                    .setDayOfTime(time)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
```

- Alternate server implementation

```java
package org.grpc.server;


import com.grpc.app.GreetingRequest;
import com.grpc.app.GreetingResponse;
import com.grpc.app.GreetingServiceGrpc;
import com.grpc.app.TimeOfDay;
import io.grpc.Context;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerImpl {

    private final int port;
    private final Server server;
    public ServerImpl(int port){
        this(ServerBuilder.forPort(port),port);
    }

    public ServerImpl(ServerBuilder serverBuilder, int port){
        this.port = port;
        server = serverBuilder
                .addService(new GreetServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build();
    }

    public void start() throws IOException{
        server.start();
        log.info("server start port {}",port);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                try{
                    ServerImpl.this.stop();
                } catch (InterruptedException e) {
                    log.error("exception occurred ",e);
                }
            }
        });
    }

    private void blockUntilShutdown() throws InterruptedException{
        if(server != null){
            server.awaitTermination();
        }
    }

    public void stop() throws InterruptedException{
        if (server != null){
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }
    public static class GreetServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
        @Override
        public void greeting(GreetingRequest request,
                             StreamObserver<GreetingResponse> responseObserver) {
            log.info("Request - {}",request);

            LocalTime currentTime = LocalTime.now();

            int hour = currentTime.getHour();
            TimeOfDay time;
            if(hour >=6 && hour <12){
                time = TimeOfDay.MORNING;
            }else if( hour >=12 && hour <18){
                time = TimeOfDay.AFTERNOON;
            }else {
                time = TimeOfDay.EVENING;
            }

            String greeting = "Hello there, " + request.getName();

            GreetingResponse response = GreetingResponse
                    .newBuilder()
                    .setMessage(greeting)
                    .setDayOfTime(time)
                    .build();

            if(Context.current().isCancelled()){
                log.info("request cancelled");
                responseObserver.onError(
                        Status.CANCELLED
                                .withDescription("Request is Cancelled")
                                .asRuntimeException()
                );
                return;
            }

            try {
                Thread.sleep(5000);
            }catch (InterruptedException e){
                log.error("sleep for a minute");
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public static void main(String ... args) throws IOException, InterruptedException {
        ServerImpl serverImpl = new ServerImpl(8091);
        serverImpl.start();
        serverImpl.blockUntilShutdown();
    }
}
```
