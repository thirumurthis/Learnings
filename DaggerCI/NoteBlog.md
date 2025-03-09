### Dagger CI workflow

In this blog have used Dagger Java SDK to create CI workflow as code. In a simple java project we need to initalize using Dagger CLI. We can create modules and functions using the CLI. The functions used to use container, set environment, build jar and publish image to artifactory.

What is Dagger?
Dagger is an opensource code that enables to define CI/CD workflows as code.
Dagger SDK is available in different languages like Go, Python, Typescript, etc. to develop code. 

Key terminologies: 

 - `Module` - Module is collection of Dagger functions.
 - `Functions` - Functions is fundamental unit of computing written in regular code in preferred language using available Dagger SDK. 
 
For Dagger architecture refer the [dagger.io](https://dagger.io/). 

#### Prerequisites:
- Docker desktop
- Dagger CLI installed. Note, when installing in Windows use Powershell 7+.

#### Implementing
- Simple Java Spring boot application is added with Dagger module with functions to set the environment, build and publish the image from local.
- The build process will be executed in local machine.

##### Initialize dagger in the project

- From the Java project issue below command which will create `dagger.json` file updates dagger engine version and few metadata

```shell
dagger init 
```

#### Dagger module creation

- We create a Dagger module in the project in this case we use Java SDK. Issue below command from the project directory
- Below command will create a `.dagger` folder with sample java code generated.

```shell
dagger develop --sdk=java
```

Under the `.dagger` folder we create a folder `backend` and initialize the dagger module using `dagger init` from this folder.
We could have created the functions to build and publish in `.dagger/src/main/java/io/dagger/modules/sampleapp/SampleApp.java` class,
instead we create those function under in the `backend` folder after creating module there.

If the same project includes frontend code, by having two modules backend and frontend we can isolate those modules. 
These function can be installed to the parent module under `.dagger` folder. The `SampleApp.java` under `.dagger` folder uses both module to build and publish artifacts. 

From the `backend` folder issue below command.

```shell
dagger init --name=backend
dagger develop --sdk=java --source .
```

After issuing above command, the java project folder structure looks like below.

```
.
├── .dagger
│     ├── .gitattributes
│     ├── .gitignore
│     ├── backend
│     │     ├── .gitattributes
│     │     ├── .gitignore
│     │     ├── LICENSE
│     │     ├── dagger.json
│     │     ├── pom.xml
│     │     └── src
│     │           └── main
│     │               └── java
│     │                   └── io
│     │                       └── dagger
│     │                           └── modules
│     │                               └── backend
│     │                                   ├── Backend.java
│     │                                   └── package-info.java
│     ├── pom.xml
│     └── src
│           └── main
│               └── java
│                   └── io
│                       └── dagger
│                           └── modules
│                               └── sampleapp
│                                   ├── SampleApp.java
│                                   └── package-info.java
├── dagger.json
├── pom.xml
└── src
      ├── main
      │     ├── java
      │     │     └── com
      │     │         └── demo
      │     │             └── sample_app
      │     │                 ├── HealthController.java
      │     │                 └── SampleAppApplication.java
      │     └── resources
      │         ├── application.properties
      │         ├── static
      │         └── templates
      └── test
          └── java
              └── com
                  └── demo
                      └── sample_app
                          └── SampleAppApplicationTests.java
```

Now with the module created, the sdk code has some sample function created, we can list those function using below command.

The backend class with the few functions that will build, publish, test, etc.

```java
package io.dagger.modules.backend;

import io.dagger.client.CacheVolume;
import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.File;
import io.dagger.client.Service;
import io.dagger.module.AbstractModule;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

import java.util.List;
import java.util.concurrent.ExecutionException;

/** Backend main object */
@Object
public class Backend extends AbstractModule {
    
  /** Create environment for build **/
  @Function
  public Container buildEnv(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {
    CacheVolume mavenCache = dag.cacheVolume("maven-cache");
    return dag.container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedCache("root/.m2",mavenCache)
            .withMountedDirectory("/app/backend",source
                                                 .withoutDirectory(".idea")
                                                 .withoutDirectory(".dagger"))
            .withWorkdir("/app/backend");
  }

  /** Build the jar artifacts*/
  @Function
  public File build(Directory source)
    throws InterruptedException, ExecutionException, DaggerQueryException {

    return buildEnv(source)
            .withExec(List.of("mvn","-DskipTests","clean","install"))
            .file("target/sample-app-0.0.1-SNAPSHOT.jar");
  }

  /** Run test */
  @Function
  public String test(Directory source)
    throws InterruptedException, ExecutionException, DaggerQueryException {

    return buildEnv(source)
            .withExec(List.of("mvn","test"))
            .stdout();
  }

  /** Publish the artifact */
  @Function
  public String publish(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    String dockerhub_url = "docker.io/thirumurthi/sample-app:latest";
    String dockerhub_token = "your_dockerhub_personal_access_token";
    return dag.container()
            .from("openjdk:21-jdk-slim")
            .withFile("/app/sample-app.jar",build(source))
            .withExec(List.of("chmod","777","/app/sample-app.jar"))
            .withEntrypoint(List.of("java","-jar","/app/sample-app.jar"))
            .withExposedPort(8080)
            // publishing to ttl.sh repo
            .publish("ttl.sh/simple-dagger-app-%d".formatted((int) (Math.random() * 10000000)))
            ;
  }

  /** Runs as service*/
  @Function
  public Service run(Directory source)
    throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag.container()
            .from("openjdk:21-jdk-slim")
            .withFile("/app/sample-app.jar",build(source))
            .withExec(List.of("chmod","777","/app/sample-app.jar"))
            .withEntrypoint(List.of("java","-jar","/app/sample-app.jar"))
            .withExposedPort(8080)
            .asService()
            .withHostname("localhost");
  }
  
  /** Build the jar file*/
  @Function
  public File buildJar(Directory directoryArg)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag.container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedDirectory("/src",directoryArg)
            .withWorkdir("/src")
            .withExec(List.of("mvn","-DskipTests","clean","install"))
            .file("target/sample-app-0.0.1-SNAPSHOT.jar");
  }
}
```

```shell
dagger -m .dagger/backend functions
```
- Output looks like below

```
dagger -m .dagger/backend functions
✔ connect 0.7s
✔ load module 14.9s

Name           Description
build          Build jar artifacts
build-env      Create environment for build
build-jar      Build the jar file
publish        Publish the artifact
run            Runs as service
test           Run test
```

To install the backend module to the main dagger module, we can use below command from the java project directory.
This command will update the dagger.json at the project level and preform code generation in the .dagger folder.

```shell
dagger install .dagger/backend
```

After we install the `backend` module with above command, from the project level below command will generator code and update the backend class function.

```shell
dagger develop
```

Now we can update the `.dagger/src/main/java/io/dagger/modules/sampleapp/SampleApp.java` class to build backend.
If we have frontend module we can install it and after installation, the functions should be available to parent dagger module.

- The java class under the `.dagger` folder (say, main module), now will be able to use the functions from the `backend` module

```java
package io.dagger.modules.sampleapp;

import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.Service;
import io.dagger.module.AbstractModule;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

import java.util.List;
import java.util.concurrent.ExecutionException;

/** SampleApp main object */
@Object
public class SampleApp extends AbstractModule {

  /** Publish image to repo */
  @Function
  public String publish(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {
     return dag.backend().publish(source);
  }

  /** Run the app as service */
  @Function
  public Service run(Directory source){
    return dag.backend().run(source);
  }
}
```

From the project directory issue below command

```shell
dagger functions
```
- Outputs the function from the main module looks like below

```
dagger functions
✔ connect 0.7s
✔ load module 13.9s

Name             Description
publish          publish image of the apps
run              Run the application as service
```

#### Build and run as a service

```shell
dagger call run --source . up --ports 8081:8080
```

- Output would look like below 

![img](https://github.com/user-attachments/assets/ba5542ec-d36f-4104-8b38-2d8cdbe2bafe)


#### Invoking the publish function

- Output looks like below with verbose level 2

![image](https://github.com/user-attachments/assets/c814fdfc-2e1b-484b-8950-ca3066dffafb)

#### Debug options

If the function returns container, we can use terminal command in Dagger CLI to investigate

````shell
dagger -m .dagger/backend call build-env --source . terminal
````

- The output would look like below

![img_1](https://github.com/user-attachments/assets/0b1c56c8-8913-4d28-9256-b54bfe9dbc8d)

Dagger does the tunneling and the application can be accessed using `http://localhost:8081/api/check`.


#### Using custom dagger engine image
- In enterprise environment if the system is behind proxy, possibly we might need to customize the dagger images with enterprise certs, etc.
- If we have custom dagger engine image we can use that image.
- Create the image and set the image name in an environment variable `_EXPERIMENTAL_DAGGER_RUNNER_HOST`. Refer [Dagger.io](https://docs.dagger.io/configuration/custom-runner#connection-interface) documentation.

The code can be found in [github repo](https://github.com/thirumurthis/Learnings/tree/master/DaggerCI/sample-app). Note, the `.dagger` folder is renamed to dagger since it is treated as hidden folder in git.
