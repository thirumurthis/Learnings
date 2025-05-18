package org.example.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;

        Server server = ServerBuilder.forPort(port)
                //to register the implemented idl
                // we use .addService and add the new impl class
                .addService(new GreetingServerImpl())
                .build();

        server.start();
        System.out.println("Server started...");
        System.out.println("Listening on port: "+port);

        // this is used to stop the server when user clicks cntrl + c
        // Select Settings -> search Gradle under the build, execution ->
        // select the Build and Run using and Run testing options to Intellij Idea on both
        // Add an application by selecting the Run/Debug configuration
        // select java version in module, select the package
        // select the main class from which the server to run
        Runtime.getRuntime().addShutdownHook(new Thread( ()->
        {
            System.out.println("Received Shutdown request");
            server.shutdown();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
