## JBang CLI

In this blog I have explained the basic usage of JBang CLI command.

### What is JBang?

- [JBang](https://www.jbang.dev/documentation/guide/latest/index.html) is a CLI tool which can run java code directly from a file.
- With JBang CLI we can execute java code from file by passing it as a argument. 
    - In order to execute the Java file by JBang CLI, the file shouls include special defintions within java comments, which is explained in the example.
- JBang can execute .jsh (java shell) file. The .jsh file is similar to the java file, in .jsh we don't need to define the class or main method.
    - The .jsh supports the code that are executed in JShell REPL editor.
- JBang can also be used to install JDK's and list JDK's. `jbang jdk list`. 

#### Install JBang CLI
- JBang CLI can be installed in different ways refer [installation](https://www.jbang.dev/documentation/guide/latest/installation.html) docs.
- In my Windows machine I used chocolatey manager. 

#### Simple hands-on to execute java code using JBang CLI

- JBang CLI requires the environment to be defined in very first line of the java file, like below.
- This is similar to shell shebang, where we use `#!`.

```
///usr/bin/env jbang "$0" "$@" ; exit $?
```

- Save below content to a file named `addTwoNumbers.java`

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

class addTwoNumbers {
    public static void main(String[] args) {
        if(args.length==0) {
            System.out.println("No arguments passed!!");
        } else {
            try{
              int num1 = Integer.parseInt(args[0]);
              int num2 = Integer.parseInt(args[1]);
              System.out.println("Sum of "+num1+" + "+num2+" = "+(num1+num2));
             } catch (NumberFormatException ex){
                 System.out.println("ERROR:- only whole number is supported");
             }
            
        }
    }
}
```

- To execute the file using JBang CLI use below command

```
jbang addTwoNumbers.java 5 5

# output of the execution will look like below
Sum of 5 + 5 = 10
```

#### Defining Maven dependencies for JBang execution

- JBang CLI can resolve Maven or Gradle dependencies from the file.
- The dependencies should be defined like below, where the line should start with `//DEPS`.

```
//DEPS groupId:artifactId:version
```

BOM dependencies can also be defined which will look like below. Note the suffix `@pom`, this should be specified in case of bom dependecy.
  - The bom should be sepcified at first, any other related dependencies of the bom doesn't require version number to be specified.
  - Only one bom can be defined in a file.

```
//DEPS groupId:artifactId:version@pom
```

- Example for [camel bom](https://mvnrepository.com/artifact/org.apache.camel/camel-bom/3.20.1) dependencies, not nee to specify the versions for the realted dependencies.

```
//DEPS org.apache.camel:camel-bom:3.20.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
```

## Sample code that uses external maven dependencies
- The code uses camel dependencies and generates random number every 2.5 seconds.

- Save the below code snipet in a file named `CamelDemo.java`.

```java
///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:3.20.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.slf4j:slf4j-nop:2.0.6
//DEPS org.slf4j:slf4j-api:2.0.6

import org.apache.camel.*;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import org.apache.camel.spi.*;
import static org.apache.camel.builder.PredicateBuilder.*;
import java.util.Random;

import static java.lang.System.*;

class CamelDemo{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        Random random = new Random();
        Main main = new Main();

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("timer:hello?period=2500")
               .process(exchange -> exchange.getIn().setBody(random.nextInt(500)))
               .setBody(simple("rNum: = ${body}"))
               .to("stream:out");
            }
         });
        main.run();
    }
}
```

- Run the above code using the command `jbang CamelDemo.java`
- Initial run will download the jars and output will be displayed like below,
- Additionally, if we need to print details we can use `jbang --verbose CamelDemo.java`

```
> jbang CamelDemo.java
[jbang] Resolving dependencies...
[jbang]    org.apache.camel:camel-bom:3.20.1@pom
[jbang]    org.apache.camel:camel-core:3.20.1
[jbang]    org.apache.camel:camel-main:3.20.1
[jbang]    org.apache.camel:camel-stream:3.20.1
[jbang]    org.slf4j:slf4j-nop:2.0.6
[jbang]    org.slf4j:slf4j-api:2.0.6
[jbang] Dependencies resolved
[jbang] Building jar...
Starting camel route...
rNum: = 482
rNum: = 289
```

![image](https://user-images.githubusercontent.com/6425536/215309948-953a1b90-7f88-4496-8258-7684ac98019d.png)

#### Executing the JBang in Docker container

```
# in windows when attaching the volume better to use the complete path of the folder where CamelDemo.java exists
docker run -v C:\\simple-camel\\:/ws -w/ws -ti jbangdev/jbang-action --verbose CamelDemo.java
```
> **Note:**
> Based on the JBang documentation, the command might looke like below, but note `pwd` in volume didn't work in windows
> ```
> docker run -v `pwd`:/ws -w/ws -ti jbangdev/jbang-action --verbose CamelDemo.java
> ```


#### Executing the Java code directly from internet
- We can use JBang CLI to execute the java code directly from the internet as well 

```
> jbang https://github.com/jbangdev/jbang-examples/blob/main/examples/helloworld.java
```

![image](https://user-images.githubusercontent.com/6425536/215311206-26a7b8b0-fb35-4203-9ce5-06326aa4edfe.png)


- There are certain limitation in using JBang CLI, like it can't be used for complex Java programs. 
- It can refer mulitple java file under pacakges, refer [documentation](https://www.jbang.dev/documentation/guide/latest/organizing.html)
- When using in production, we might need to consider only for small automation process, this community is still growing.



