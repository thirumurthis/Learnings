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

- To complie use
```shell
$ ./mvnw compile
```

- Using wrapper is recommended since it is self-containing and portable. 
