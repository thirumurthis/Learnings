
## Maven Lifecylce 
 - Maven by default defines three lifecycle - `default`, `clean` and `site`.
 - Maven command accepts only `goal` or `phase`.
 - Maven lifecycle phases itseld doesn't have any capabilities to accomplish anything and they rely on plugins to do any work.
  - Lifecycle phases don't come with any functionality
     - To carry out a task they rely on plugins. For example, the `compile` phase doesn't actually compile the Java source, instead it delegates the job to compile goal of `maven-compiler-plugin` which invokes the `javac` to compile the project.
     - when using `mvn package` the plugins that are plugged into phases are executed.
	     - `compile` phase binds with the `mavne-compiler-plugin`
		 - `test` phase binds with the `maven-surefire-plugin`

- Below command list the lifecycle phases and also the bounded goal and plugin version

```
$ mvn help:describe -Dcmd=<phase>
``` 

- Sample 
```
$ mvn help:describe -Dcmd=clean
$ mvn help:describe -Dcmd=deploy
```

### Phases

 - Each lifecycle consists of `predefined phases`
 - When we use `mvn clean` the `clean` is a phase in the lifecycle.
   - `clean` includes three phases - `pre-clean`, `clean`, `post-clean`.
   - Maven loads clean lifecycle and executes the three phase in pre-clean, clean and post-clean order.
- Default lifecycle includes ~21 phases, out of which only few is used frequently like `compile`,`test`,`install`,`deploy`. Below are few other phases
   - `process-resources`- this copies and process the resources into the destination directory, ready for packaging.
   - `process-test-resources` - this copy and process the resources into destination directory
   - `test-compile` - compile the test code into the test destination directory.
   - `compile` - compile the source code of project.
   - `install` - install the package into local repository, for use as dependency in other project locally
   - `deploy` - copies the final package to the remote repository for sharing with other developer and projects.

### Maven plugins

- Lifecycle phases are just steps without any capability to carryout some task and they delegate the actual work to Maven Plugins.
 
> Important!!!
> - The Maven core is capable of parsing XML files such as pom.xml and manage lifecycle and phases. By itself, Maven doesn't know how to compile the code or even to make a JAR file. It is designed to delegate most of the functionality to Maven Plugins. 
> - In fact, the first time we ran something like mvn install with a new Maven installation, it retrieves most of the core Maven plugins from the Central Maven Repository. Maven is - at its heart - a plugin execution framework; all work is done by plugins

### Maven goals

- Maven plugin is a collection of one or more goals which do some task or job. It is also known as Mojo - Maven Plain Old Java Object.
- Plugins can be grouped as `build plugins` and `reporting plugins`
- Maven plugin is a collection of one or more goals which do some task or job. It is also known as Mojo - Maven Plain Old Java Object
- `maven-surefire-plugin`, to run tests it has two goals `surefile:test` and `surefire:help`.

- To run plugin goal
```
$ mvn [options] [<goal(s)>] [<phase(s)>]
```

- To run a goal with maven, use the format `<plugin prefix>:<goal>`
- The compiler is plugin prefix of `maven-compiler-plugin` and `compile` is the goal name.
- The `mvn test` executs all dependent goals in the lifecycle phase test.

- Sample

```
$ mvn compiler:compile 
$ mvn surefire:test
$ mvn compiler:testCompile
```

#### Configuring plugin

- To configure plugins, we use project build element in pom.xml which looks like below.

```xml
<project>
...
  <build>
    ...
    <plugins>
      <plugin>
        <groupId>...</groupId>
        <artifactId>...</artifactId>
        <version>...</version>
        <configuration>...</configuration>        
        <executions>
           <execution>...</executions>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```
- Elements

- `build` => defines the project build.
- `plugins` => parent element for one or more `<plugin>` elements.
- `plugin` => the plugin to configure.
- `groupId`, `artifactId`, `version` => coordinates of the plugin to configure.
- `configuration` => holds the parameters and properties to be passed to the plugin.
`executions` => parent element for one or more `<execution>` element.
`execution` => configures the execution of a goal of the plugin.

- With <configuration> we can set plugin parameters. Below is the example,

```xml
<build>
	<plugins>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-compiler-plugin</artifactId>
			<version>3.1</version>
			<configuration>
					<source>1.8</source>
					<target>1.8</target>
			</configuration>
		</plugin>
	</plugins>
</build>
```

- Easiest way to know the available parameters for a goal is to run plugin help goal.

```
$ mvn compiler:help -Dgoal=compile -Ddetail
```

- To know the default value of the paramters, run below command

```
$ mvn help:describe -Dplugin=compiler -Dmojo=compile -Ddetail
# Note that maven-help-plugins uses -Dmojo for goal, instead of -Dgoal
```

#### Working with plugin configuration
- Maven Clean Plugin deletes the target directory by default and we may configure it to delete additional directories and files.

```xml
...
  <build>
   <plugins>
    <plugin>
      <artifactId>maven-clean-plugin</artifactId>
      <configuration>
        <filesets>
        <filesets>
          <fileset>
            <directory>src/main/generated</directory>
            <followSymlinks>false</followSymlinks>
            <useDefaultExcludes>true</useDefaultExcludes>
            <includes>
              <include>*.java</include>
            </includes>
            <excludes>
              <exclude>Template*</exclude>
            </excludes>
          </fileset>
        </filesets>
      </configuration>
    </plugin>
   </plugins>
  </build>
```

- The element `<executions>/<execution>` allows you to configure the execution of a plugin goal. With it, you can accomplish the following things.
  - bind a plugin goal to a lifecycle phase.
  - configure plugin parameters of a specific goal.
  - configure plugin parameters of a specific goal such as `compiler:compile`, `surefire:test`, etc., that are by default binds to a lifecycle phase

- Below example uses `maven-antrun-plugin` built with echo to print the property.

```xml
<build>
    <plugins>
      <plugin>
        <artifactId>maven-antrun-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>run</goal>
            </goals>
            <phase>compile</phase>
            <configuration>
              <tasks>
                <echo>Build Dir: ${project.build.directory}</echo>
              </tasks>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

- Let's go through the above snippet to understand the execution. In the `<execution>` element, we indicate that it should be applied to `antrun:run` goal. We can apply an execution to one or more goals. 
- Next, we bind the execution to `compile` lifecycle phase and the `antrun:run` goal binds to compile phase. Finally, we add `<configuration>` to the execution. In `configuration`, we set `task` and `echo` parameters of `antrun:run` goal to echo the build directory name.
- In the above execution, we attached `antrun:run` goal to `compile` phase. But it can be any other phase and during execution of that phase ant will echo the output. 
- We Change the phase to test or verify phase and observe the output.

#### Maven properties

- Maven allows different types of properties like 
    - Environment variables, 
	- Project properties, 
	- properties defined in Settings files, 
	- Java System Properties and 
	- properties defined in POM

 - Maven properties are value placeholder and their values are accessible anywhere within a POM by using the notation ${X}, where X is the property.

#### To override an existing default plugin

-  Suppose, in the default jar we want to exclude certain packages and create a separate jar with those packages. We can use multiple execution declarations to achieve this.

 ```xml
   <plugin>
        <artifactId>maven-jar-plugin</artifactId>
        <executions>
            <execution>
                <id>default-jar</id>
                <configuration>
                    <excludes>
                        <exclude>**/extrapackage/*</exclude>
                    </excludes>
                </configuration>
            </execution>
            <execution>
                <id>special-jar</id>
                <phase>package</phase>
                <goals>
                    <goal>jar</goal>
                </goals>
                <configuration>
                    <includes>
                        <include>**/extrapackage/*</include>
                    </includes>
                    <classifier>extra</classifier>
                </configuration>
            </execution>
        </executions>
    </plugin>
    ...
 ```

### Elements in the pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- The Basics -->
  <groupId>...</groupId>
  <artifactId>...</artifactId>
  <version>...</version>
  <packaging>...</packaging>

  <dependencies>...</dependencies>
  <parent>...</parent>
  <dependencyManagement>...</dependencyManagement>
  <modules>...</modules>
  <properties>...</properties>

  <!-- Build Settings -->
  <build>...</build>
  <reporting>...</reporting>

  <!-- More Project Information -->
  <name>...</name>
  <description>...</description>
  <url>...</url>
  <inceptionYear>...</inceptionYear>
  <licenses>...</licenses>
  <organization>...</organization>
  <developers>...</developers>
  <contributors>...</contributors>

  <!-- Environment Settings -->
  <issueManagement>...</issueManagement>
  <ciManagement>...</ciManagement>
  <mailingLists>...</mailingLists>
  <scm>...</scm>
  <prerequisites>...</prerequisites>
  <repositories>...</repositories>
  <pluginRepositories>...</pluginRepositories>
  <distributionManagement>...</distributionManagement>
  <profiles>...</profiles>
</project>
```
### Super POM
> IMPORTANT!!!
> - Maven installation comes with a pom.xml which is known as Super POM. Which is like in Java, where every other class extends java.lang.Object, all Maven projects extends Super POM. The pom.xml we create in our project is child or decedent of the Super POM.
> - The Super POM declares location of Maven central repository, Plugins repository, build element which sets the standard locations of the directories, plugin configuration and execution, etc.

### Effective POM
> IMPORTANT!!!
> - At the start of every build, Maven internally merges project pomx.ml with `Super POM` and constructs a new POM which is known as `Effective POM`.
> - Earlier in Maven Lifecycle and Plugin Goals, we learned that Maven binds plugins goals to lifecycle phases. Actually, this magic happens in `effective POM` and it is highly instructive to go through the `effective POM` to know the what goes on under the hood.

 - To print the effective pom.xml to in console use below command

```
# below command prints the pom to console
$ mvn help:effective-pom 
```
> Note: 
> - To output effective pom to specific directory use 
```
mvn help:effective-pom -Dverbose=true -Doutput=target/effective-pom.xml
```

- Effective POM details
  - The effective POM starts with coordinates of our project and then goes on to define Maven central and plugin repositories. Next, in build section, it defines project and build directories.
  - In the plugins section, Maven binds default goals for lifecycle phases. Though we have not defined build section in the project pom.xml, Maven constructs the effective POM with all sections which are essential to build the project.

#### Resource plugin

- Resources are non source files of the project such as properties files, XML configuration files, shell scripts files and also, binary files like images etc.

Maven Resources Plugin (plugin prefix resources) copies resource files from source directories to output directory.
  - `resources:resources` => Copy resources for the main source code to the main output directory. Always uses the `project.build.resources` element to specify the resources to copy.  
  - `resources:testResources` => Copy resources for the test source code to the test output directory. Always uses the `project.build.testResources` element to specify the resources to copy.  
  - `resources:copy-resources` => Copy resources of the configured plugin attribute resources

> The goal resources:resources binds to process-resources phase and resources:testResources to process-test-resources.

##### Filters
- By default, the standard location of the source resources is `src/main/resources` and test resources is `src/test/resources`.

- Add configuration file config.xml to `src/main/resources` and add test resource file test-config.xml to `src/test/resources` with following contents

```xml
<!-- src/main/resources/config.xml -->
<config>
    <url>jdbc:hsqldb:mem:simpledb</url>
    <user>admin</user>
    <password>secret123</password>
</config>
```

```xml
<!-- src/test/resources/config.xml -->
<config>
    <url>jdbc:hsqldb:mem:testdb</url>
    <user>sa</user>
    <password></password>
</config>
```

- Lifecycle phase `process-resources` executes `resources:resources` goal and it copies `src/main/resources/config.xml` to output folder `target/classes`. - Similarly, lifecycle phase `process-test-resources` executes `resources:testResources` goal and it copies `src/test/resources/test-config.xml` to output folder `target/test-classes`.

- In `package` phase, the goal `jar:jar` includes the `classes/config.xml` in the `sample-app-1.0.jar`. By default, the test resources are used only to run tests and not added to the Jar.

- The Maven Resources Plugin also has features to apply filters to the resource files. Filters replaces the variables denoted by `${...}` in resource files with the actual value.

- Filtering is not enabled by default. If we want to centralize the database setting in a property file.

```xml
<!-- /src/main/resources/config.xml -->
<config>
    <url>${db.url}</url>
    <user>${db.user}</user>
    <password>${db.password}</password>
</config>
```

```xml
<!-- src/test/resources/test-config.xml -->
<config>
    <url>${db.test.url}</url>
    <user>${db.test.user}</user>
    <password>${db.test.password}</password>
</config>
```

- Create a new directory `src/main/filters` and to it, add a properties file `default.properties`.
- Create the property files `src/main/filters/default.properties`, this file contains main and test database properties as key-value pairs. 

```properties
db.url=jdbc:hsqldb:mem:mydb
db.username=admin
db.password=secret123

db.test.url=jdbc:hsqldb:mem:testdb
db.test.username=testuser
db.test.password=
```

- The filter file should be be mentioned in the pom.xml under `<build>/<filter>` element.

```xml
  <build>
    <filters>
        <filter>src/main/filters/default.properties</filter>
    </filters>
  </build>
```

- If we run `mvn clean package`, goal copies `config.xml` as it is without applying any filters. Because, by default filtering is disabled. To apply filter, we have to explicitly turn on the filtering in pom.xml.
- Below config turns on the filtering for all resources in the `src/main/resources` folder.

```xml
  <build>
    <filters>
         <filter>src/main/filters/default.properties</filter>
    </filters>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
  </build>
```

- We have not turned on filtering for test resources, the copied `test-config.xml` still retains the `${..}` variables. Modify pom.xml to enable filtering for it too. With below configuration both teh conif.xml will be copied to the jar file.

> Note:
> - The test resource should be `<testResources>`

```xml
 <build>
    <filters>
         <filter>src/main/filters/default.properties</filter>
    </filters>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
        <resource>
            <directory>src/test/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
   </build>
```

- Below is the configuration for config file to be copied to test/ path. 

```xml
  <build>
    <filters>
        <filter>src/main/filters/default.properties</filter>
    </filters>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
    <testResources>
        <testResource>
            <directory>src/test/resources</directory>
            <filtering>true</filtering>
        </testResource>
    </testResources>
  </build>
```

### Directories

- A set of directory elements live in the build element of `Super POM`, which defines the directories for source, test, scripts and build files etc.

```xml
  <build>  
    <directory>${project.basedir}/target</directory>  
    <outputDirectory>${project.build.directory}/classes</outputDirectory>  
    <testOutputDirectory>${project.build.directory}/test-classes</testOutputDirectory>  
    <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>  
    <scriptSourceDirectory>${project.basedir}/src/main/scripts</scriptSourceDirectory>  
    <testSourceDirectory>${project.basedir}/src/test/java</testSourceDirectory>  
   ...  
  </build> 
```

`directory` - sets the project build directory. The Maven property `${project.basedir}` defaults to the top level directory of the project, so the build directory defaults to the target directory in project base dir. It also sets the property `${project.build.directory}` to target directory and the next two output directories uses this property.
`outputDirectory` - directory to hold Java class files and it points to target/classes.
`testOutputDirectory` - directory to hold test class files and it points to target/test-classes.
`sourceDirectory` - project source code files folder and it points to src/main/java in project base dir.
`scriptSourceDirectory` - project script files folder and it points to src/main/scripts in project base dir.
`testSourceDirectory` - project test files folder and it points to src/test/java in project base dir.

> Note:
> We can override these elements in the project's pom.xml and force Maven to adapt to a different directory structure.


- We override default settings of `directory`, `outputDirectory` and `sourceDirectory` elements. With these settings, `src` is the source directory and `bin` is the project build and also, the output directory.

```xml
 <build>
    <directory>${project.basedir}/bin</directory>
    <outputDirectory>${project.build.directory}</outputDirectory>
    <sourceDirectory>${project.basedir}/src</sourceDirectory>
</build>
```

- Below example below we override the set of directory elements that are defined in Super POM for resources and test resources. This is just a demonstration, recommended to use the existing structure.

```xml
<build>
    <resources>
      <resource>
        <directory>${project.basedir}/src/main/resources</directory>
      </resource>
    </resources>
    <testResources>
      <testResource>
        <directory>${project.basedir}/src/test/resources</directory>
      </testResource>
    </testResources>
  </build>
```

### Archetype plugin
- An archetype is a template for a Maven project which is used by the Maven Archetype plugin to create new projects. Simply put, it generates skeleton for different types of projects.

### Distribution package

- `JAR` - it is the default packaging type and Maven binds `jar:jar` goal to package phase.
- `POM` - it is the simplest packaging type and the artifact that it generates contains the `pom.xml`. Maven binds `site:attach-descriptor` goal to package phase.
- `EJB` - Maven binds `ejb:ejb` goal to package phase.
- `WAR` - packages a web app and binds `war:war` goal to package phase.
- `Maven Plugin` - binds `jar:jar` and `plugin:addPluginArtifactMetadata` goals to package phase to package a Maven Plugin project.


- To push the artifacts to the repostiory that is ashared one accessed using ssh, below configuration can be used. Provided the ssh keys are configured to access the repo.
- The distributionManagement defines repository located at /var/maven/shared-repo in server with hostname maven-repo and it uses SSH protocol. To enable the use of SSH, it adds Maven extension library Apache Wagon.

```xml
<distributionManagement>
      <repository>
         <id>ssh-repository</id>
         <url>scpexe://maven-repo/var/maven/shared-repo</url>
      </repository>
  </distributionManagement>

  <build>
      <extensions>
         <!-- Enabling the use of SSH -->
         <extension>
              <groupId>org.apache.maven.wagon</groupId>
              <artifactId>wagon-ssh-external</artifactId>
              <version>1.0-beta-6</version>
          </extension>
      </extensions>
  </build>
```

### Assembly plugin

- When packing the project for distribution we require more control over the way package is done. The Maven Assembly Plugin allows finegrain control to assemble the project.
- The Maven Assembly Plugin allows users to package the project output with its dependencies, modules, site documentation, and other files into a single distributable archive.

- Assembly Plugin ships with four Pre-defined Descriptor Files.
  - `bin` - creates a binary distribution archive of the project.
  - `jar-with-dependencies` - creates a JAR which contains the binary output of the project, along its the unpacked dependencies.
  - `src` - creates source archives for the project.
  - `project` - creates archives containing the entire project, minus any build output that lands in the target directory.

- To create an assembly within the existing project, issue below command

```
$ mvn assembly:single -DdescriptorId=src
```

> Note:
> - assemblies can also be build as part of project build cycle by binding `assembly:single` goal to package phase.

To add the `pom.xml` with below config, and during `mvn package` along with regular jar creats source archives in different format.

```xml
 <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-assembly-plugin</artifactId>
        <version>2.5.4</version>
        <executions>
          <execution>
            <id>dist</id>
            <phase>package</phase>
            <goals>
              <goal>single</goal>
            </goals>
            <configuration>
              <descriptorRefs>
                 <descriptorRef>src</descriptorRef>
              </descriptorRefs>
            </configuration>
          </execution>
        </executions>
      </plugin>    
    </plugins>
  </build>
```

- Custom assembly descriptor can be configured to package javadoc, there is much easier alternative.
- As part of package phase, it produces source and javadoc jars along with the regular jar.

```xml
 <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <executions>
          <execution>
            <id>attach-sources</id>
            <goals>
              <goal>jar</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <executions>
          <execution>
            <id>attach-javadocs</id>
            <goals>
              <goal>jar</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

### Uber jar

- Maven documentation often uses a term uber-jar. Uber is a German word for above or over and an uber-jar contains not only the project packages and resources but also `all its dependencies in one single JAR` file.
- The assembly plugin provides only basic support for uber-jar and for more control, use Maven Shade Plugin.

### Profiles

- Maven Profiles allows to define multiple set of configurations and activate them based on certain conditions.
- Maven Profile is an alternative set of configurations which set or override default values. Profiles allow to customize a build for different environments. Profiles are configured in the pom.xml and each profile is identified with an identifier.

```xml
 <profiles> 
   <profile>
     <id>production</id>
      <build>
		<plugins>
		   <plugin>
			 <groupId>org.apache.maven.plugins</groupId>
			 <artifactId>maven-compiler-plugin</artifactId>
			 <configuration>
			   <debug>false</debug> 
			   <optimize>true</optimize>
			   </configuration>
		  </plugin>
		</plugins>
	</build>
   </profile>
 </profiles>
```

- To activate a specific profile use below command

```
$ mvn compile -P<profile-name>> -X
```

### Simple project execution

- Say, we have a simple java project with out any dependency once the application jar is generated in target folder, we can execute the code using below command

```
$ java -cp target/sample-app-1.0.jar com.demo.App
```

- Say, if the project uses a dependency and that is not packaged during `mvn install`, then when we execute the jar we need to pass in the dependency jar like below

```
java -cp target/sample-app-1.0.jar:$HOME/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar com.demo.App
```

- Instead of passing the dependent jar, we can use below mvn exec command

```
 $ mvn exec:java -Dexec.mainClass="com.demo.App"
``` 

- The command uses `exec-maven-plugin` to execute the class com.demo.App, the plugin builds the runtime classpath from the dependencies listed in pom.xml, by adding the location of dependent jar before executing the app.

- profile elements

```xml
<project>
    <profiles>
        <profile>
            <build>
                <defaultGoal>...</defaultGoal>
                <finalName>...</finalName>
                <resources>...</resources>
                <testResources>...</testResources>
                <plugins>...</plugins>
            </build>
            <reporting>...</reporting>
            <modules>...</modules>
            <dependencies>...</dependencies>
            <dependencyManagement>...</dependencyManagement>
            <distributionManagement>...</distributionManagement>
            <repositories>...</repositories>
            <pluginRepositories>...</pluginRepositories>
            <properties>...</properties>
        </profile>
    </profiles>
</project>
```

- profile activation with the activation property

```xml
 <profiles>
   <profile>
     <id>dev</id>
     <activation>
       <activeByDefault>true</activeByDefault>
     </activation>
   </profile>
 </profiles>
```

- profile activation if file exists and doesn't exists

```xml
  <profile>
     <activation>
        <file>
           <exists>target/generated-sources/foo</exists>
           <missing>target/generated-sources/bar</missing>
        </file>
     </activation>
    ...
   </profile>
```

#### Mavent surefire plugin:

  - The surefire plugin loads the `/src/main/java/*java` classes and `/src/main/resources` under `/target/classes`.
  - The `/src/test/java` test classes and `/src/test/resources` are loaded under `/target/test-classes`
  - For maven testing, the class path of the config is set to use the `/target/test-classes`. 
      - If any other folder path needs to be used under class-path for testing use `additionalClassPath` element. Check maven documentation.


Reference [Maven tutorial link](https://www.codetab.org/tutorial/apache-maven/plugins/maven-resources-plugin/)

-------------------------

Scenario:
  - The `applicationconfig.properties` file was manged under the `/src/main/resources` folder. 
  - The properties file under the /src/test/resource should not be used or removed.
  - The applicationconfig.properties was generated by infra code automatically in this case Chef client.
  
 Solution:
   - if any properties under /src/test/resource rename or remove it.
   - during build, in pre-interation-test phase copy the applicationconfig.properties from src/main/resources to target/test-classes using maven ant plugin.
   - maven test will automatically use target/test-classes in its class path.
   - so any reference in spring context.xml like "classpath:applicationconfig.properties" will refer to the copied content.
   
   maven content, so within the profile ` $ mvn install -P integration-test` will invokce the below pugins goal.
   ```xml
	<profiles>
		<profile>
			<id>integration-test</id>
			<properties>
				<build.profile.id>integration-test</build.profile.id>
				<skip.integration.tests>false</skip.integration.tests>
				<skip.unit.tests>true</skip.unit.tests>
			</properties>

			<build>
          		    <plugins>
				 <plugin>
				      <groupId>org.apache.maven.plugins</groupId>
				      <artifactId>maven-antrun-plugin</artifactId>
				      <version>3.0.0</version>
				      <executions>
					<execution>
						<id>copy-dependencies</id>
						<phase>pre-integration-test</phase>
						<goals>
							<goal>run</goal>
						</goals>
						<configuration>
							<target>
							   <echo>copy properties file in pre-integration-test phase ${basedir}</echo>
							   <copy todir="target/test-classes/">
						                <fileset dir="${basedir}/src/main/java/resources/">
						                     <include name="applicationconfig.properties"/>
						                </fileset>
						           </copy>
							</target>
						</configuration>
					  </execution>
					</executions>
				  </plugin> 
				  <plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-failsafe-plugin</artifactId>
					<version>2.22.1</version>
					<dependencies>
						<dependency>
							<groupId>org.apache.maven.surefire</groupId>
							<artifactId>surefire-junit47</artifactId>
							<version>2.22.1</version>
						</dependency>
					</dependencies>
					<executions>
						<execution>
							<id>integration-tests</id>
							<goals>
								<goal>integration-test</goal>
							</goals>
							<configuration>
							    <!-- Skips integration tests if the value of skip.integration.tests property is true -->
							    <skipTests>${skip.integration.tests}</skipTests>
							    <includes>
								<include>**/*.class</include>
						  	    </includes>
							     <!-- below is totally to use main/resources as classpath - but in our case this is not required -->
                                                             <!--
                                                             <additionalClasspathElements>
								<additionalClasspathElement>${basedir}/src/main/java/resources/</additionalClasspathElement>
							     </additionalClasspathElements> -->
							</configuration>
						  </execution>
					</executions>
				</plugin>
			</plugins>
		</build>
	</profile>
</profiles>
 ```
