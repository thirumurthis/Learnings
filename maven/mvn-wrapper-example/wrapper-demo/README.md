### Maven wrapper

```shell
$ mvn -N io.takari:maven:wrapper -Dmaven=3.9.9
```
- 
- To run the wrapper

```shell
$ ./mvnw --version
```

- With the wrapper uploaded to the git repo, now when this repo is cloned. 
- The wrapper can use the mvn wrapper instead of mvn cli. This should work even without internet just java to be installed.

- To compile use
```shell
$ ./mvnw compile
```

- Using wrapper is recommended since it is self-containing and portable. 

#### To execute the integration test with failsafe plugin use below
 - Below command will run the test case that follow the pattern *IT.java or *ITcase.java

```shell
$ mvn failsafe:integration-test
```

#### To list goals under phase use below 

```shell
$ mvn help:describe -Dcmd=verify
```

> IMPORTANT!!
> - In order for jacoco plugin to generate reports by default on `mvn verify`
> - move the plugin from `<pluginManagement>` to `<plugins>.