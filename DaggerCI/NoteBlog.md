### Dagger CI workflow with Java SDK

In this blog have details how we can create Dagger module and functions using Dagger Java SDK on a simple Java Spring boot project. With Dagger CLI we can run the CI process in our local machine. With the Dagger CLI new modules can be created or use the modules built by community from DaggerVerse. In this example, we initialize the Dagger module with functions to build environment on a container, build jar, test the build and publish the build. With the Dagger CLI we can run the functions in local machine. There is an option to run the Dagger module in Github actions or Jenkins.

### What is Dagger?

Dagger is an opensource code that enables to define CI/CD workflows as code. Dagger SDK is available in different languages like Go, Python, Typescript, etc. to develop code. In local the Dagger CLI uses the Dagger enging running in Docker to run the functions. For Dagger architecture refer the [dagger.io](https://dagger.io/) documentation.

#### Key terminologies

 - `Module` - Module is collection of Dagger functions.
 - `Functions` - Functions is fundamental unit of computing written in regular code in preferred language using available Dagger SDK. 
 

#### Prerequisites:
- Docker desktop
- Dagger CLI installed. Note, when installing in Windows use Powershell 7+. Dagger CLI will use Docker to run the dagger engine.

#### Initialize dagger in the project

- We have a simple SpringBoot Java project and first initialize the dagger by executing below command. This will create `dagger.json` file updates dagger engine version and few other metadata.

```shell
dagger init 
```

### Dagger module creation

#### Parent module

We can create modules using Dagger CLI, and implement our functions to perform CI/CD operations. To Dagger module in the project we use Java SDK. By executing below command we will create a dagger module. This will create a `.dagger` folder and simple java class as it uses the maven code generation. Since the name of the java project was `sample-app`, the java code generated under `.dagger/src/main/java/io/dagger/modules/sample-app/SampleApp.java`

```shell
dagger develop --sdk=java
```

Note, we will be creating a module under the `.dagger` module, called `backend`. This will be helpful to manage if the same repo has both backend and frontend code and CI process builds them differently. We could have created the functions directly in `.dagger/src/main/java/io/dagger/modules/sample-app/SampleApp.java` class in `.dagger` folder but we will look how to create modules under the `.dagger` and re-use the function from child module in parent module.

#### Backend module (child module)

Lets create the backend module, the process to create other modules under the parent module is the same.

Create a `backend` folder under the `.dagger` directory, and issue below command to initialize the dagger module from `backend` directory.

```shell
cd .dagger/backend
dagger init --name=backend
dagger develop --sdk=java --source .
```

The project structure will look like below after creating the backend modules. We could see that the `dagger.json` at the root of the SpringBoot project folder structure and the modules created under the the `.dagger` folder. Note, when we use Dagger CLI to run the functions, the maven code generator will create target folders under the `.dagger` and `backend` which is not included below.

```
sample-app/
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

Till now we have created two modules, parent module under the `.dagger` folder. Another module under `backend`. Since Dagger CLI generated the module folder structure now we can create functions using the Dagger library. The functions below are self explanatory since it is similar to Dockerfile definition.

Below is the backend module with simple functions will helps to setup environment on the container, build jar artifact, test, run and publish.

> With the functions added, Dagger CLI can list the functions defined in this module. The function arguments name will be used as parameters when we call the function using Dagger CLI. Next we will see how to use Dagger CLI to list the functions.

```java
package io.dagger.modules.backend;

import io.dagger.client.CacheVolume;
import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.File;
import io.dagger.client.Service;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.dagger.client.Dagger.dag;

/** Backend main object */
@Object
public class Backend {

  /** Create environment for build **/
  @Function
  public Container buildEnv(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {
    CacheVolume mavenCache = dag().cacheVolume("maven-cache");
    return dag().container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedCache("root/.m2",mavenCache)
            .withMountedDirectory("/app/backend",source
                    .withoutDirectory(".idea")
                    .withoutDirectory(".dagger"))
            .withWorkdir("/app/backend");
  }

  /** Build jar artifacts*/
  @Function
  public File build(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return buildEnv(source)
            .withExec(List.of("mvn","-DskipTests","clean","install"))
            .file("target/sample-app-0.0.1-SNAPSHOT.jar");

  }

  /** Run test*/
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
    return dag().container()
            .from("openjdk:21-jdk-slim")
            .withFile("/app/sample-app.jar",build(source))
            .withExec(List.of("chmod","777","/app/sample-app.jar"))
            .withEntrypoint(List.of("java","-jar","/app/sample-app.jar"))
            .withExposedPort(8080)
            //.publish("localhost:5000/simple-app:1.0.0")
            //.publish("localhost:5000/simple-app-%d".formatted((int) (Math.random() * 10000000)))
            .publish("ttl.sh/simple-dagger-app-%d".formatted((int) (Math.random() * 10000000)))
            ;
  }

  /** Runs as service*/
  @Function
  public Service run(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag().container()
            .from("openjdk:21-jdk-slim")
            .withFile("/app/sample-app.jar",build(source))
            .withExec(List.of("chmod","777","/app/sample-app.jar"))
            .withEntrypoint(List.of("java","-jar","/app/sample-app.jar"))
            .withExposedPort(8080)
            .asService()
            .withHostname("localhost")
            //.asService(new Container.AsServiceArguments().withArgs(List.of("")))
            //.start()
            //.
            ;
    //.withExec(List.of("java","-jar","/app/sample-app.jar")) -- is not working when use as service
  }

  /** simple test container */
  @Function
  public Service testRun()
          throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag().container()
            .from("nginx:latest")
            .withExposedPort(80)
            .asService()
            .withHostname("localhost")
            .start()
            ;
  }

  /** Build helper returns container*/
  @Function
  public Container buildHelper(Directory directoryArg)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag().container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedDirectory("/src",directoryArg)
            .withWorkdir("/src")
            //.withExec(List.of("ls","-lrt"))
            //.withExec(List.of("mvn","--version"))
            //.withExec(List.of("mvn","install"))
            //.stdout();
            //.withExec(List.of("mvn","-DskipTests","clean","install"))
            //.file("/src/target/sample-app-0.0.1-SNAPSHOT.jar")
            ;
  }

  /** Build the jar file*/
  @Function
  public File buildJar(Directory directoryArg)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag().container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedDirectory("/src",directoryArg)
            .withWorkdir("/src")
            //.withExec(List.of("ls","-lrt"))
            //.withExec(List.of("mvn","--version"))
            .withExec(List.of("mvn","-DskipTests","clean","install"))
            //.stdout();
            //.withExec(List.of("mvn","-DskipTests","clean","install"))
            .file("target/sample-app-0.0.1-SNAPSHOT.jar")
            ;
  }
}

```

- Below is the Dagger CLI command that lists the backend module functions. The function name `buildEnv` is displayed as `build-env` by Dagger CLI.

```shell
dagger -m .dagger/backend functions
```

- Output will look like below

```
dagger -m .dagger/backend functions
✔ connect 0.7s
✔ load module 14.9s

Name           Description
build             Build jar artifacts
build-env     Create environment for build
publish         Publish the artifact
run                Runs as service
test               Run test
```

To view the arguments info of the functions we can call the function and pass the `--help` like below

```
 dagger -m .dagger/backend call build --help
```

- Output of the build function displayed by Dagger CLI. Note, the java comments above the function method will be used as function description by Dagger CLI. The method parameter name will be used as argument name.

```
✔ connect 0.6s
✔ load module 4.6s
✔ parsing command line arguments 0.0s

Build jar artifacts

USAGE
  dagger call build [arguments] <function>

FUNCTIONS
  contents          Retrieves the contents of the file.
  digest            Return the file's digest. The format of the digest is not guaranteed to be stable between releases of Dagger. It is guaranteed
                    to be stable between invocations of the same Dagger engine.
  export            Writes the file to a file path on the host.
  name              Retrieves the name of the file.
  size              Retrieves the size of the file, in bytes.
  sync              Force evaluation in the engine.
  with-name         Retrieves this file with its name set to the given name.
  with-timestamps   Retrieves this file with its created/modified timestamps set to the given time.

ARGUMENTS
      --source Directory   [required]

Use "dagger call build [command] --help" for more information about a command.
```

#### Invoke build-env function from the backend module

Now lets call the `buildEnv` function from `backend` module using Dagger CLI. Since `buildEnv` function returns a Container we can use `terminal` option so Dagger CLI will run the command and will exec into the container to working directory path with the copied source code. 

```
dagger -m .dagger/backend call build-env --source . terminal
```

![image](https://github.com/user-attachments/assets/3e2e8315-1a4a-412e-a46e-59736c0c0fc2)


### Install and reuse the backend module functions in parent module

Till now we created function in the backend module and executed using Dagger CLI with `-m` option. During development for IDE to use the `backend` module from `.dagger` parent module we need to install the backend module. 

To install the `backend` module issue below command from the root directiry of Java project root in this case `sample-app`. This command will update the `dagger.json` at the `sample-app` project level. Along with install we need to issue another command perform code gen with the installed module.

```shell
dagger install .dagger/backend
```

After installing the `backend` module with above command, from the root directory of java project `sample-app` issue execute below command which will run code generate and update the parent module. Now in IDE we will be able to use the function from the `backend` module.

```shell
dagger develop
```

The `backend` functions can now be used in `.dagger/src/main/java/io/dagger/modules/sample-app/SampleApp.java` class to build backend, if we have created frontend functions we can reuse those function to build both codes.

- The `.dagger/src/main/java/io/dagger/modules/sample-app/SampleApp.java` includes function and reuse the backend module functions.

```java
package io.dagger.modules.sampleapp;

import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.Service;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.dagger.client.Dagger.dag;

/** SampleApp main object */
@Object
public class SampleApp {

  /** publish image of the apps */
  @Function
  public String publish(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag().backend().publish(source);
  }

  /** Run the application as service*/
  @Function
  public Service run(Directory source){
    return dag().backend().run(source);
  }
}

```

Since the parent module is updated, by issuing below command we could see those functions listed like in the output of the command below.

```shell
dagger functions
```

- Output of above command with list of function from the main module looks like below

```
dagger functions
✔ connect 0.7s
✔ load module 13.9s

Name             Description
publish          publish image of the apps
run              Run the application as service
```

### Call the run function from main module using Dagger CLI

Since the main module is updated now, we can use Dagger CLI just to call the functions instead of referring the modules with `-m` option.

```shell
dagger call run --source . up --ports 8081:8080
```

![image](https://github.com/user-attachments/assets/3e2e8315-1a4a-412e-a46e-59736c0c0fc2)

Since the run command returns Service, the `--ports 8081:8080` option in Dagger CLI will create a network tunneling and the application can be accessed using `http://localhost:8081/api/check` from another terminal. Below is the output of the accessing the endpoint of application running in dagger engine.

```
$ curl localhost:8081/api/check && echo
[{"check": "ok"},{"time": "2025-03-09 17:31:53"}]
```

### Call the publish function from main module using Dagger CLI

```
dagger call publish --source .
```

![image](https://github.com/user-attachments/assets/c814fdfc-2e1b-484b-8950-ca3066dffafb)

#### Using custom dagger engine

In enterprise private repository will be used and behind proxy in this case we might require customization of the dagger images with enterprise certs, security, etc. Dagger CLI can be configured to use the custom dagger engine image from docker by configuring the image name in environment variable `_EXPERIMENTAL_DAGGER_RUNNER_HOST`. 
Refer [Dagger.io](https://docs.dagger.io/configuration/custom-runner#connection-interface) documentation for more details.

##### Source code

The code can be found in [github repo](https://github.com/thirumurthis/Learnings/tree/master/DaggerCI/sample-app). Note, the `.dagger` folder is renamed to dagger since it is treated as hidden folder in git. This source currently can't be cloned and used directly, just for reference only.
