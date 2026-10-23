### Project uses Spring gRPC 

This uses `spring 4.x` version

The `authz` module defines the authorization part
  - the authz application.yaml configuration defines the authorization information
The `service` module defines the api to using grpc
The `client` module will access the service api, configured to use the authorization server


The hosts file to be included with below values for each module.

The authorization server might not work as expected when using the loopback ip address

```sh
127.0.0.1 authz.local service.local client.local
```

Start the authz and use the curl to get the jwt token

```sh
curl -vvi -X POST http://authz.local:7070/oauth2/token   -u "spring:secret"   -d "grant_type=client_credentials"   -d "scope=openid profile"
```

start all the application (the authz uses posrtgres) but that is not used since there is inmemory userdetail passed