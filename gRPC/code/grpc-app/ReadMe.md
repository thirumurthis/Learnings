### Application execution


- To run the client with different port with executable jar
- `cd /c/thiru/edu/gitsource/Learnings/gRPC/code/grpc-app/grpc-client-one/target`

```sh
java -Dserver.port=8086 -jar grpc-client-one-1.0.0-SNAPSHOT-exec.jar
```

- execute the example

- to get the status 

```sh
curl -XGET "http://localhost:8085/api/status?userName=demo1"
```

- to create new order

```sh
 curl -XPOST "http://localhost:8085/api/order" -d '{"userName": "test02",  "itemName": "item99",  "quantity": "99"}' -H "Content-Type: application/json"
```

