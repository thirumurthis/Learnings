### Project uses Spring gRPC 

This uses `spring 4.x` version

The `authz` module defines the authorization part
  - the authz application.yaml configuration defines the authorization information
The `service` module defines the api to using grpc
The `client` module will access the service api, configured to use the authorization server


The hosts file to be included with below values for each module.

The authorization server might not work as expected when using the loopback ip address

```sh
127.0.0.1 authz.local service.local client.local client-http.local service-http.local client-grpc.local service-grpc.local
```

Start the authz and use the curl to get the jwt token

```sh
curl -vvi -X POST http://authz.local:7070/oauth2/token   -u "spring:secret"   -d "grant_type=client_credentials"   -d "scope=openid profile"
```

start all the application (the authz uses Postgres) but that is not used since there is inmemory userdetail passed

The authz includes two set of client info, make sure the redirect-url in the authz is including correct spring authorization provider section name from the client

For example in the below url in the authz application.yaml the latest section is the registration name on the client config
```sh
"http://client-grpc.local:8080/login/oauth2/code/spring-grpc"
```

```yaml
    registration:
      spring-grpc:
        provider: "spring-grpc-provider"
        client-id: "spring-grpc"
        client-secret: "grpc-secret"
```

Start all the application
For HTTP API service access, from browser use http://client-http.local:8083, this will get the api from the service-http exposed in 8086 port http://service-http:8086/

For gRPC service access, from browser use http://client-grpc.local:8080, this will redirect to the login page use the admin user info and should redirect to the appropriate server in thsi case http://service-grpc:8085/