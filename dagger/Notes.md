- Installing from the `winget`

```
winget install --id=Dagger.Cli -e
```
-- Created a spring boot project with starter io, used below command from the WSL2 instance. install the dagger in wsl2 insta

```
dagger run mvnw exec:java

in WSL2 doens't had Java installed in it, so got an JAVA_HOME not found and reqired message
```
In WSL the `mvnw exec:java` command didn't work reporting java_home not set. requires the java_home to set.

-- When installing the java in wsl2, had issue updating below was teh exception message. To fix this had to issue ` sudo apt-get update --fix-missing`.

```
E: Failed to fetch http://security.ubuntu.com/ubuntu/pool/main/o/openjdk-21/openjdk-21-jre-headless_21.0.5%2b11-1ubuntu1%7e24.04_amd64.deb  404  Not Found [IP: 91.189.91.81 80]
E: Failed to fetch http://security.ubuntu.com/ubuntu/pool/main/o/openjdk-21/openjdk-21-jre_21.0.5%2b11-1ubuntu1%7e24.04_amd64.deb  404  Not Found [IP: 91.189.91.81 80]
```
- mvn clean install didn't work as expected, had to install the jdk version since javac is not recognized as command.

  ```
   sudo apt-get install openjdk-21-jdk-headless
  ```
#### Finally with the above configuration
  - install java to wsl2, remove the java from windows
  - make sure the docker is accessible in wsl2
  - make sure the javac is accessible in wsl2

then issu the command `dagger run ./mvnw spring-boot:run` to view the application running
  With the above command the spring boot build got successful
