## Maven

#### Repository
   - Local => repostiory on local file system $USER/.m2/
   - Central => public repostiory hosted by Maven community - https://repo1.maven.org/maven2
   - Remote => other location which are private or public

The workflow of maven, first looks into the local directory `.m2/` folder if not present locally then looks to the remote or central based on the configuration in the settings.xml.

With the artifacts that are `-SNAPSHOTS`, maven will look in local though it finds the artifact in local repository it will look for new copy of it from the remote or central repo by default. We can configure the SNAPSHOT artifacts to be cached but by default it is configurable ofr 1 day.

If the artifact is not `-SNAPSHOTS`, once downloaded to local it will not look for it from the remote or central repository.

#### Maven wagon
 - Maven wagon is an unified maven API.
 - This is a transport abstraction for artifactid and repository handling code.
 - Allows different providers to be used for communications with Maven repository.
 - Most of the time http/https is used, no additional configuration is needed.
 - In coporate environment we might need to configure Wagon for Proxy settings.

 Provider like File, http, ftp, ssh/scp, WebDAV, SCM

 The build lifecycle `site:deploy`  deploy site via wagon

 #### Project Object Model (pom)
 - The pom.xml is a XML document which describes a maven project.


 #### Dependency
  - Transitive Dependency
     -  A dependency of an artifact which your project depends on.
        - Can go many layers
        - Cyclic dependencies are not supoorted
  - Exculding dependency - exclude specific dependency
  - Optional dependency - makes the dependency optional (exclude by default for downstream projedcts)

###### Dependency scope
   - Compile => Default, available on all classpaths of project. Also, probagated to downstream project.
   - Provided => Like compile, but expected to be provided by JDK or container at runtime.
   - Runtime - Not required for compile, but needed for runtime. On runtime and test classpaths, not compile.
   - Test - Only available on test classpath, not transitive.
   - System - similar to provided, but JAR is added to system explicitly. (via file path)
   - Import - imports dependency of pom.

- Dependency are managed by dependency plugin
- Goals:
    - dependency:tree => shows the dependency tree. Useful for resolving conflicts 
    - dependency:go-offline => resolve all, prepare to go offline
    - dependency:purge-local-repository => clear artifacts from local repository
    - dependency:sources => get sources for all depedencies

- By default the maven standard folder structure will be created.
  - main/src, test/src, corresponding resources directory


### Maven build lifecycles
  - A lifecycle is a pre-defined group of build steps called phases
  - Each phase can be bound to one or more plugin goals
  - All the work is done by maven is using maven plugins

- Maven has three pre-defined life-cycle
  - clean => Cleans the project, removes all build artifacts from working directory. Defined with build plugin bindings.
  - default => does the build and deployment of project. This does not have any pluhin bindings by default. Default lifecycle defines phases and bindings are defined for each packaging type of pom like jar,war,pom, etc.
  - site => creates a website for the porject. Defined with the plugin bindings.


The clean lifecycle, include below phases
  - pre-clean
  - clean
  - post-clean 

There is no plugin bind pre-clean and post-clean. For clean phase we have a plugin bind called `mvn clean`. 

default lifecycle :
  - validate - verify project 
  - compile - compile source code
  - test - test compiled files
  - package - package compiled files
  - verify - run integration test
  - install - install to local maven repo
  - deploy - deploy to shared maven repo

Jar packaging:
- Phase: process-resources, Plugin: - maven-resources-plugin:resources
- Phase: compile - Plugin: - maven-compiler-plugin:compile
- Phase: process-test-resources, Plugin: maven-resources-plugin:testResources
- Phase: test-compile, Plugin: maven-compiler-plugin:testCompile
- Phase: test, Plugin: maven-surefire-plugin:test
- Phase: pacakge, Plugin: maven-jar-plugin:jar
- Phase: install, Plugin: maven-install-plugin:install
- Phase: deploy, Plugin: maven-deploy-plugin:deploy

Site lifecycle phases:

- Phase: Pre-site, Plugin: none
- Phase: Site, Plugin: maven-site-plugin:site
- Phase: Post-site, Plugin: none
- Phase: Site-Deploy, Plugin: maven-site-plugin:deploy

### Maven wrappers
- When there are no wrapper assinged to the project, when using the mvn CLI command it uses either the mvn installed in the command line or IDE.

The purpose of the wrapper is to make the build portable, so we can checkout the source code and everything the needs to be run can be done with a shell script.

The wrapper is a wrapper around maven the allows us to work with maven without maven being installed in the machine.

To create a wrapper.

From the project working directory, issue below command

```shell
mvn -N io.takari:maven:wrapper
```

- The above command will create a `.mvn` folder, `mvnw` file and wr 

- Once the above command is executed. Issue below command we should see the maven version

```shell
./mvnw --version
```

If we need to use the latest maven within the `cd .mvn/wrapper/maven-wrapper.properties` which includes the version being used by the wrapper.

- To specify the specific version of maven to be wrapped we can use below command

```shell
mvn -N io.takari:maven:wrapper -Dmaven=3.9.9
```

#### Site plugin

`site:site` - generatte site for project
`site:deploy` - deploy site via wagon
`site:run` - run site locally using Jetty as web server
`site:stage` - generate site to a local staging directory
`site:stage-deploy` - deploy site to remote staging location


#### Overriding the default plugins in the project pom.xml

- Say, we create a simple maven project if we use the `mvn package` goal more than once, unless there are changes in the code it will not rebuild the jar.

- Lets override the default `maven-clean-plugin` in pom.xml in the `<build>` like below, where we bind the `clean` goal to `initailize`.

The override configuration looks like below

```xml
<build>
  <plugin>
    <artifactId>maven-clean-plugin</artifactId>
    <version>3.4.1</version>
    <executions>
      <execution>
        <id>auto-clean</id>
        <phase>initialize</phase>
        <goals>
          <goal>clean</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
</build>
```

> NOTE:
> - Just by adding above configuration when using `mvn package` we see that the resource are cleaned before package, we don't want to issue `mvn clean package`.

We can use IntelliJ IDEA, we can use the maven tool, expand the Plugins -> Compiler > Compiler:help

The maven plugin in IntelliJ IDEA, list the maven default LifeCycle phase in the tool. So when we execute the `compile` goal in the default lifecycle phase it will look into the plugin defined in the `<build>` property and runs it.

### Maven jar plugin to create Executable jar

- To make the jar executable refer the maven documentation, we will add the jar plugin and add the configuration about the manifest with main class.

```xml
<build>
  <plugins>
    <plugin>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.4.2</version>
          <configuration>
            <archive>
              <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>com.demo.wrapper.App</mainClass>
              </manifest>
            </archive>
          </configuration>
        </plugin>
      </plugins>
</build>
```

- command to run after `mvn package` and jar creation

```
$ java -jar target/wrapper-demo-1.0-SNAPSHOT.jar
```

### To add groovy along with junit5 in testing 

- Below is the complete pom.xml defintion with dependency for groovy to work

```xml
<!-- check https://github.com/groovy/groovy-eclipse/wiki/groovy-eclipse-maven-plugin -->
 <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>23</maven.compiler.release>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.apache.groovy</groupId>
      <artifactId>groovy</artifactId>
      <version>4.0.26</version>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.platform</groupId>
      <artifactId>junit-platform-launcher</artifactId>
      <scope>runtime</scope>
    </dependency>
  </dependencies>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>5.12.2</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <pluginManagement>
      <!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.5.3</version>
        </plugin>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.4.1</version>
          <executions>
            <execution>
              <id>auto-clean</id>
              <phase>initialize</phase>
              <goals>
                <goal>clean</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.13.0</version><!-- 3.6.2 is the minimum -->
          <configuration>
            <compilerId>groovy-eclipse-compiler</compilerId>
            <compilerArguments>
              <indy/><!-- optional; supported by batch 2.4.12-04+ -->
              <!--<configScript>config.groovy</configScript>--><!-- optional; supported by batch 2.4.13-02+ -->
            </compilerArguments>
            <failOnWarning>true</failOnWarning><!-- optional; supported by batch 2.5.8-02+ -->
            <fork>true</fork><!-- optional; enables Parrot Parser by default for Groovy 3+ -->
          </configuration>
          <dependencies>
            <dependency>
              <groupId>org.codehaus.groovy</groupId>
              <artifactId>groovy-eclipse-compiler</artifactId>
              <version>3.7.0</version>
            </dependency>
            <dependency>
              <groupId>org.codehaus.groovy</groupId>
              <artifactId>groovy-eclipse-batch</artifactId>
              <version>4.0.26-01</version><!-- JDK 17+ -->
            </dependency>
          </dependencies>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.5.3</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.4.2</version>
          <configuration>
            <archive>
              <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>com.demo.wrapper.App</mainClass>
              </manifest>
            </archive>
          </configuration>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>

  <pluginRepositories>
    <!-- The groovy plugin is stored in different plugin repository -->
    <pluginRepository>
      <id>groovy-plugins-release</id>
      <url>https://groovy.jfrog.io/artifactory/plugins-release</url>
    </pluginRepository>
  </pluginRepositories>
```

- Then add the groovy file under the same `src/test/com/demo/wrapper` along with the java.

```groovy
package com.demo.wrapper

import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

import java.util.stream.Stream
import static org.junit.jupiter.api.Assertions.assertTrue;


class AppTestGroovy {
    @Test
    void streamSum() {
        assertTrue(Stream.of(1, 2, 3)
                .mapToInt(i -> i)
                .sum() > 5, () -> "Sum should be greater than 5")
    }

    @RepeatedTest(value=2, name = "{displayName} {currentRepetition}/{totalRepetitions}")
    void streamSumRepeated() {
        assert Stream.of(1, 2, 3).mapToInt(i -> i).sum() == 6
    }
}
```

- run the `mvn test` to validate the test is passing

### Adding SPOC to project

In the above groovy pom.xml we add below dependency under the `<dependencies>` property. Note the version of should match the groovy dependency used.

```xml
    <dependency>
      <groupId>org.spockframework</groupId>
      <artifactId>spock-core</artifactId>
      <version>2.4-M6-groovy-4.0</version>
      <scope>test</scope>
    </dependency>
```

- Create a folder `test/groovy` and package `com/demo/wrapper`. Create file named `AppGroovySpockTest.groovy` and add below content

```groovy
package com.demo.wrapper

import spock.lang.Specification

class AppGroovySpockTest extends Specification {

    def "getMessage from app instance"() {
        setup: "a new App instance is created"
        def app = new App()
        when: "getMessage"
        def result = app.getMessage()
        then: "result is as expected"
        result == "app message"
        println "result = ${result}"
    }
}
```

### Generating Test reports with maven surefire plugin

- Add below dependency to the pom.xml. Refer the project wrapper-demo project in git.
```xml
<project>
<!--... -->
<reporting>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-report-plugin</artifactId>
        <version>3.5.1</version>
      </plugin>
    </plugins>
  </reporting>
</project>
```
- Once updated run `mvn site` should render the site info. The `surefire.html` should have the test details.


### Maven failsafe plugin
- maven-failsafe-plugin is used to run integration tests. Refer the Maven documentation.
- For integration test it requires to load the context, other configuration, etc.
- This plugin will check for test case end with *it.
- After adding the dependency, issue `mvn verify` command to perform integration test.

### Maven Jacoco
 - Jacoco is used for code coverage
 - This is verstaile at the same time its raw.
 - Refer the documentation to configure the Jacoco