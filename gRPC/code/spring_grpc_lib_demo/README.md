### The example is to use the protobuf generated code
The server implementation added from another library dependency
If the logic of the server logic are same, this approach will benifit
to use multiple servers and client

- The library dependency is in different repo spring-grpc/grpc-srv-lib
- The changes to be built there using mvn clean install and the liberary will
- be updated and used.

The code has a schedule based invocation example but commented
