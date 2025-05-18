package blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.greeting.server.GreetingServerImpl;

import java.io.IOException;

public class BlogServer {

    /*
    We have already added the mangodb-driver-sync dependency
    We need to add a connection to the mango db
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;

        //mango client
        MongoClient client = MongoClients.create("mongodb://root:root@localhost:27017/");

        Server server = ServerBuilder.forPort(port)
                //to register the implemented idl
                // we use .addService and add the new impl class
                .addService(new BlogServiceImpl(client))
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
            client.close();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
