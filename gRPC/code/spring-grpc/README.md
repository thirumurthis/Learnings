# Spring grpc example

- Multi module maven project 
- uses Spring grpc library.
- Start the server and client
- For development, the grpc-proto has the proto idl file, this is generated into jar
- The grpc-proto is added as dependency to server and client


 Once the application is started use http://localhost:8080/api/greet
 and http://localhost:8080/api/greetings
 First is simple unary type implementation
 Second is Server Streaming type implementation
 
Also the grpc-proto, pom.xml has commented code which generates jar plugin 
using maven plugin extension from generated source. This is another approach
for generating jar.
Make sure to clean install the grpc-proto before making any changes.