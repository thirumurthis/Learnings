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

