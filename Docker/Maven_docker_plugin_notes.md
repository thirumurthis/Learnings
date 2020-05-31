### Below notes are used to automatically build the image and push it to docker hub automatically.

There are multiple maven plugins to perform docorization,
   - JIB
      - This doesn't require the Dockerfile to be used, most of the docker command is included within the xml using plugins.
   - Fabric8
   - Spotify (`dockerfile-maven-plugin`)
      - The Dockerfile will be used to build and push the image in this case.
   
 In the project update the pom.xml with the plugin information.
 
 ```xml
 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>sample.demo</groupId>
  <artifactId>HelloFromDocker</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <name>HelloFromDocker</name>
  <url>http://maven.apache.org</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.target>1.8</maven.compiler.target>
    <maven.compiler.source>1.8</maven.compiler.source>
    <dockerfile-maven-version>1.4.13</dockerfile-maven-version> <!-- this the version of the spotify plugin -->
  </properties>

  <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>2.2.2.RELEASE</version>
  </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
        
 <build>
	<plugins>
		<plugin>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-maven-plugin</artifactId>
		</plugin>
     	<plugin>
		<groupId>com.spotify</groupId>
		<artifactId>dockerfile-maven-plugin</artifactId>
		<version>${dockerfile-maven-version}</version>
		<executions>
			<execution>
				<id>default</id>
				<phase>install</phase> <!-- when using the $mvn install, will perform the corresponding goals.-->
				<goals>
					<goal>build</goal>
					<goal>push</goal>  <!--For development, this can be commented in case develpment -->
				</goals>
			</execution>
		</executions>
		<configuration>
			<repository><docker-username>/hw-k8s-sl</repository> <!-- the hub.docker.com, user account and -->
			<tag>${project.version}</tag> <!-- The value resloved and evaluated to <version> value of this project -->
			<tag>latest</tag>
			<buildArgs>
				<JAR_FILE>${project.build.finalName}.jar</JAR_FILE>  <!-- This is the argument value 
			</buildArgs>
		</configuration>
	</plugin>	
  </plugins>
 </build>
 
 </project>
```

##### The `Dockerfile` content.
```
FROM openjdk:8-jre-alpine

# JAR_FILE is an argument value from the maven representation.
ARG JAR_FILE
# copy JAR into image
COPY target/${JAR_FILE} /app.jar 

EXPOSE 8800
# run application with this command line 
CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/app.jar"]
```

##### To execute build the project:
If the `Docker Toolbox`, then start that and login 
 - After login use `$ mvn install` will build and push the image.
```
name@name MINGW64 /c/sboot_app/eclipse-ws/HelloFromDocker
$ docker login                                                                                                                            Login with your Docker ID to push and pull images from Docker Hub. If you don't have a Docker ID, head over to https://hub.docker.com to create one.
Username (docker-user-name): docker-user-name
Password:
Login Succeeded

name@name MINGW64 /c/sboot_app/eclipse-ws/HelloFromDocker
$ mvn install                                                                                                                            [INFO] Scanning for projects...
[INFO]
[INFO] --------------------< sample.demo:HelloFromDocker >---------------------
[INFO] Building HelloFromDocker 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:3.1.0:resources (default-resources) @ HelloFromDocker ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 1 resource
[INFO] Copying 0 resource
[INFO]
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ HelloFromDocker ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-resources-plugin:3.1.0:testResources (default-testResources) @ HelloFromDocker ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory C:\sboot_app\eclipse-ws\HelloFromDocker\src\test\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ HelloFromDocker ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ HelloFromDocker ---
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running sample.demo.HelloFromDocker.AppTest
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.02 s - in sample.demo.HelloFromDocker.AppTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO]
[INFO] --- maven-jar-plugin:3.1.2:jar (default-jar) @ HelloFromDocker ---
[INFO] Building jar: C:\sboot_app\eclipse-ws\HelloFromDocker\target\HelloFromDocker-1.0.0.jar
[INFO]
[INFO] --- spring-boot-maven-plugin:2.2.2.RELEASE:repackage (repackage) @ HelloFromDocker ---
[INFO] Replacing main artifact with repackaged archive
[INFO]
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ HelloFromDocker ---
[INFO] Installing C:\sboot_app\eclipse-ws\HelloFromDocker\target\HelloFromDocker-1.0.0.jar to C:\Users\<username>\.m2\repository\sample\demo\HelloFromDocker\1.0.0\HelloFromDocker-1.0.0.jar
[INFO] Installing C:\sboot_app\eclipse-ws\HelloFromDocker\pom.xml to C:\Users\<username>\.m2\repository\sample\demo\HelloFromDocker\1.0.0\HelloFromDocker-1.0.0.pom
[INFO]
[INFO] --- dockerfile-maven-plugin:1.4.13:build (default) @ HelloFromDocker ---
[INFO] dockerfile: null
[INFO] contextDirectory: C:\sboot_app\eclipse-ws\HelloFromDocker
[INFO] Building Docker context C:\sboot_app\eclipse-ws\HelloFromDocker
[INFO] Path(dockerfile): null
[INFO] Path(contextDirectory): C:\sboot_app\eclipse-ws\HelloFromDocker
[INFO]
[INFO] Image will be built as <docker-user-name>/hw-k8s-sl:latest
[INFO]
[INFO] Step 1/5 : FROM openjdk:8-jre-alpine
[INFO]
[INFO] Pulling from library/openjdk
[INFO] Digest: sha256:f362b165b870ef129cbe730f29065ff37399c0aa8bcab3e44b51c302938c9193
[INFO] Status: Image is up to date for openjdk:8-jre-alpine
[INFO]  ---> f7a292bbb70c
[INFO] Step 2/5 : ARG JAR_FILE
[INFO]
[INFO]  ---> Using cache
[INFO]  ---> 4ffaf0f1e165
[INFO] Step 3/5 : COPY target/${JAR_FILE} /app.jar
[INFO]
[INFO]  ---> f4649b3f3545
[INFO] Step 4/5 : EXPOSE 8800
[INFO]
[INFO]  ---> Running in 368f68693573
[INFO] Removing intermediate container 368f68693573
[INFO]  ---> f1659f79f423
[INFO] Step 5/5 : CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=default", "/app.jar"]
[INFO]
[INFO]  ---> Running in d8444ddda896
[INFO] Removing intermediate container d8444ddda896
[INFO]  ---> 215822847bf7
[INFO] Successfully built 215822847bf7
[INFO] Successfully tagged <docker-user-name>/hw-k8s-sl:latest
[INFO]
[INFO] Detected build of image with id 215822847bf7
[INFO] Building jar: C:\sboot_app\eclipse-ws\HelloFromDocker\target\HelloFromDocker-1.0.0-docker-info.jar
[INFO] Successfully built <docker-user-name>/hw-k8s-sl:latest
[INFO]
[INFO] --- dockerfile-maven-plugin:1.4.13:push (default) @ HelloFromDocker ---
[INFO] The push refers to repository [docker.io/<docker-user-name>/hw-k8s-sl]
[INFO] Image 45355c2c525b: Preparing
[INFO] Image edd61588d126: Preparing
[INFO] Image 9b9b7f3d56a0: Preparing
[INFO] Image f1b5933fe4b5: Preparing
[INFO] Image f1b5933fe4b5: Layer already exists
[INFO] Image edd61588d126: Layer already exists
[INFO] Image 9b9b7f3d56a0: Layer already exists
[INFO] Image 45355c2c525b: Pushing
[INFO] Image 45355c2c525b: Pushed
[INFO] latest: digest: sha256:39bc89f9e104090f3afaeab4044427f02ec850f091ef0953463a1841ca9c5aa4 size: 1159
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  54.408 s
[INFO] Finished at: 2020-05-30T21:33:47-07:00
[INFO] ------------------------------------------------------------------------
```

 - Docker toolbox was installed in case of windows 10 home edition.

##### After successful build, use `$ docker images` to view the built images.
  - To run the docker container use command, `$ docker run -p 8000:8080 -d <container-id>`
  - To validate in case of docker toolbox, (Windows 10 home edition) version, use `$ docker-machine ip` to find the ip.
  - Then after container is up and running, use `$ curl http://<ip-address>:8000/` this will display the content.

##### This image can be used within the Kubernetes, to create deployment and service object.
 - The image is stateless, meaning there is no database.
 
 
