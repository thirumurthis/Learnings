## JBang

In this blog have explained the basic usage of JBang CLI command. With additional specification we will be able to run Java code without using any source code structure.

### What is JBang?

- [JBang](https://www.jbang.dev/documentation/guide/latest/index.html) is a CLI tool which can run java code directly from a file.
- With JBang CLI we can execute java code from file by passing it as a argument. 
    - In order to execute the Java file by JBang CLI, the file shouls include special defintions within java comments, which is explained in the example.
- JBang can also execute .jsh (java shell) file. The .jsh file is similar to .java file, but it doesn't require class definition or main method.
    - Any code that executes in JShell REPL editor, can be places in .jsh file.
- JBang can also be used to install JDK's and list installed JDK's, using command `jbang jdk list`. 

#### Install JBang
- JBang CLI can be installed in different ways refer [installation](https://www.jbang.dev/documentation/guide/latest/installation.html) docs.
- In my Windows machine I used chocolatey manager. 

#### Execute first Java code in JBang

- JBang CLI requires the environment to be defined in very first line of the java file, like below.
- This is similar to shell shebang, where we use `#!`.

```
///usr/bin/env jbang "$0" "$@" ; exit $?
```

- Save below code snippet as `addTwoNumbers.java`

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
##### JBang command to run Java file

```
> jbang addTwoNumbers.java 5 5
Sum of 5 + 5 = 10
```

#### JBang can resolve Maven dependencies

- JBang CLI can resolve Maven or Gradle dependencies from the Java file.
- JBang requires the dependencies to be specified in a specific format which should start with `//DEPS` and looks like below.

```
//DEPS groupId:artifactId:version
```

- Defining Maven `BOM` dependencies in the Java file. Note, the suffix `@pom`, is required when defining the bom dependencies.
     - Points to note when using  `bom` dependencies,
          - The bom definition should be specified before using the related dependencies.
          - Single Java file that is executed by JBang can only contain one `bom` definition.

```
//DEPS groupId:artifactId:version@pom
```

- Example for using [camel bom](https://mvnrepository.com/artifact/org.apache.camel/camel-bom/3.20.1) dependencies, not nee to specify the versions for the realted dependencies.

```
//DEPS org.apache.camel:camel-bom:3.20.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
```

#### Sample code defining maven dependencies for JBang

- The java code below uses camel dependencies. It simply generates random number every 2.5 seconds.

- Save below code snippet in `CamelDemo.java` file.

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
##### Output of JBang CLI execution

- To execute the java file using jbang use the command `jbang CamelDemo.java`
- Initial run will download the defined jar dependencies and output will look like below,
- We can use the `-verbose` switch to print additional logs of `jbang`like, `jbang --verbose CamelDemo.java`

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

#### Use JBang in Docker to run Java file

```
# in windows when attaching the volume better to use the complete path of the folder where CamelDemo.java exists
docker run -v C:\\simple-camel\\:/ws -w/ws -ti jbangdev/jbang-action --verbose CamelDemo.java
```

> **Note:**
> Based on the JBang documentation, the command might looke like below, but note `pwd` in volume didn't work in windows
> ```
> docker run -v `pwd`:/ws -w/ws -ti jbangdev/jbang-action --verbose CamelDemo.java
> ```

#### Use JBang to execute Java code directly using URL

- We can use JBang CLI to execute the java code directly from the internet as well 

```
> jbang https://github.com/jbangdev/jbang-examples/blob/main/examples/helloworld.java
```

![image](https://user-images.githubusercontent.com/6425536/215311206-26a7b8b0-fb35-4203-9ce5-06326aa4edfe.png)


#### Consideration

- There are certain limitation in using JBang CLI, like it can't be used for complex Java programs. 
- It can refer mulitple java file under pacakges, refer [documentation](https://www.jbang.dev/documentation/guide/latest/organizing.html)
- To use JBang in production totally depends on the usage, it can be used for small automation process but not for any complex code still this community is growing.
