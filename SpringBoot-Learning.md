### Use [spring intitializer](https://start.spring.io) to create the spring boot project with web.

  - Select apporpriate options (Maven, Java jdk 11).
  - Download the zip file, and extract to setup with appropriate IDE (Intellij CE IDE)
  
Spring is opnionated framework, which has default configuration to start the application.

##### Profile:

` application.properties` is a the default properties file present within the `/main/resources/`.

```
server.port=9090
```

##### Multiple `application.properties` file for different environments.

For example, we have below files under the resource directory of spring-boot project
`application-dev.properties`
```
server.port=9091
```
`application.properties`
```
server.port=9090
```

To make the application to use the dev properties, when starting the application use -Dspring.profiles.active=dev

##### using maven
```
$ mvn spring-boot:run -Dspring.profiles.active=dev
```

#####  using java as (JVM arguments using -D)
```
$ java -jar -Dspring.profiles.active=prod application.jar
```

##### using java parameter (also using the location of the config file if it is external of the jar file)
```
$ java jar application.jar --spring.profiles.active=dev --spring.config.location=c:\config
```


