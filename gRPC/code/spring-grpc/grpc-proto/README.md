# grpc-proto

The project is created as simple java and upgraded to generate the protobuf
generated class as jar file and use it in server and client.

The pom.xml is similar to the client and server using the spring config
Note, created a `proto` directory under `src/main`, added this direct as root source directory
- in pom.xml, under the build section added below
 `<sourceDirectory>${project.basedir}/src/main/proto</sourceDirectory>`
- With the above added from intellij with the maven plugin, grpc-proto -> install the jar generated under target folder

The dependency is added in the server and client