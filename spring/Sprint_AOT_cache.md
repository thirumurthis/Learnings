
To extract the jars from the SpringBoot uber jar we can use below command, this is useful for AOT (Ahead of Time) feature in java 24/25 to have high performance on application start up



```
java -Djarmode=tools -jar application.jar extract --destination extracted
```

The command extracts the jars and when we use tree we could see the lib contains list of jars from uber jar

We can run the spring extracted application, the extracted file is AOT compatible and CDS (Java Class Data Sharing) compatible.


- we can run the extracted jar itself and could see the application starts within seconds without aot cache, to start the application use below command

```
java -jar extracted/spring-petclinic-4.0.0-SNPASHOT.jar 
```

Way to do training run

Hard to do 
- Run integration tests/ mirror production traffic
    - Exercises many/all hot code paths used in producrtion
	- can't easily be done while building the image
	- collects profiling information
	- more involved setup

We can create the Aot cache from production run and use that for next run.


Easy to do 

Stop the application before context refresh (doesn't requires production setup)
  - easy setup
  - can be done while building the image
  - caches mostly class loading only
  - Doesn't collect profiling information
  - Doesn't excercise hot code paths in production

Stopping the application context before the refresh means the refresh is the concept in spring framework before it registers all the beans, during refresh the beans are created in the context.

The easy way:

```
  java -Dspring.context.exit=onRefresh -jar extracted/application.jar
```  
  
The above command will perfrom below life cycle operation

Start -->  Instantiate non-lazy singletons ---> afterPropertiesSet invoked ---> Exit on refresh


The full life cycle is not done, will stop after refresh rest of the stages of lifes cycle will not execute

Start -->  Instantiate non-lazy singletons ---> afterPropertiesSet invoked ---> Exit on refresh 
                                                                                          ||--> Lifecycle start invoked --> ContextRefreshedEvent published --> Running

Create the AOT Cache 
```
java -XX:AOTCacheOutput=app.aot -Dspring.context.exist=onRefresh -jar extracted/application.jar
```

- After the command exists we can check the file created for app.aot size would be in ~MB's

Run the application with the AOT cache created. Note remove the output on the command here when we run it 

```
java -XX:AOTCache=app.aot -jar extracted/application.jar
```

Is the app.aot cache is not correct then the application will not use. Possible option for that app.aot cache not being correct if the aot cache was created on different architecutre and different jvm where the application is being deployed.
For example, if the JVM uses java 25 then use it in the application deployment as well, etc.

The AOT cache also expects the Jar to be flat not under any directory.

### AOT Cache 

JVM feature developed via project leyden to improve efficency of the JVM. It supersedes CDS.

### Spring AOT 

Spring feature mandatory for GraalVM native images support. Can aslo be used on JVM to spped up the startup process and lower the memory consumption.
generates code ahead of time for the bean arrangement and other features, e.g. Spring Data reporsitories

below is the command to enable AOT and spring AOT on spring application. The memory is also less RSS memory

```
java -XX:AOTCache=extracted-aot/app.aot -DSpring.aot.enabled=true -jar extracted-aot/spring-petclinic-4.0.0-SNAPSHOT-aot.jar
```


### How to acheive this in production

work flow for creating the aot cache


Training Run  |   Assembly Phase  |          Deployment run (uses app.aot)
                                app.aot 
								             Deployment run (uses app.aot)
											 
								             Deployment run (uses app.aot)


We don't need to build the app.aot file when we scale the application, we use the same in containerized env.




Flows with docker 

```

 ----------          ---------------           ------------------                 -------------------                ---------------------
|  Dev/CI  |        | Runtime-Build |         | Final-Image (os) |               | Build image (tag)  |             | Containers (deployN)|
 ----------          ---------------           ------------------                 --------------------               ---------------------
                           |                          |                                  |                                    |
docker build               |                          |                                  |                                    |      
-------------------------->|                          |                                  |                                    |
ARG MODULES                |                          |                                  |                                    |
-------------------------->|                          |                                  |                                    |
jlink --add-modules        |  build minimal runtime   |                                  |                                    |
                           |------------------------> |                                  |                                    |
ARG JAR_FILE                                          |                                  |                                    |
----------------------------------------------------->|                                  |                                    |
                                                      |                                  |                                    |
RUN mkdir cache &&                                    |                                  |                                    |
    java -XX:AOTCacheOutput=cache/app.aot -jar app.jar| -------------- Training Run ---->|	                                  |
                                                      |                                  |                                    |
													  | -------------- Assembly Run ---->|                                    |
docker build result                                                                      |                                    |
-----------------------------------------------------------------------------------------|                                    |
                                                                                         |                                    |
docker run app:latest                                                                    |                                    |
-----------------------------------------------------------------------------------------|                                    |
                                                                   container CMD:                                             |
                                                                     java -XX:AOTCache=cache/app.aot -jar app.jar			  |													   
scale out                                                                                                                     |
------------------------------------------------------------------------------------------------------------------------------
                                                                                                                    each uses cache/app.aot
																													
```

Flow with Spring and docker 


```

 ----------          ---------------           ------------------                 -------------------                ---------------------
|  Dev/CI  |        | Runtime-Build |         | Final-Image (os) |               | Build image (tag)  |             | Containers (deployN)|
 ----------          ---------------           ------------------                 --------------------               ---------------------
                           |                          |                                  |                                    |
docker build               |                          |                                  |                                    |      
-------------------------->|                          |                                  |                                    |
ARG MODULES, JAR_FILE      |                          |                                  |                                    |
-------------------------->|                          |                                  |                                    |
jlink --add-modules        | build minimal runtime    |                                  |                                    |
                           |------------------------> |                                  |                                    |
                            RUN jarmode=tools  -----> |                                  |                                    |
                                                      |                                  |                                    |
RUN mkdir cache &&                                    |                                  |                                    |
    java -XX:AOTCacheOutput=cache/app.aot -jar app.jar| -------------- Training Run ---->|	                                  |
                                                      |                                  |                                    |
													  | -------------- Assembly Run ---->|                                    |
docker build result                                                                      |                                    |
-----------------------------------------------------------------------------------------|                                    |
                                                                                         |                                    |
docker run app:latest                                                                    |                                    |
-----------------------------------------------------------------------------------------|                                    |
                                                                   container CMD:                                             |
                                                                     java -XX:AOTCache=cache/app.aot -jar app.jar			  |													   
scale out                                                                                                                     |
------------------------------------------------------------------------------------------------------------------------------
                                                                                                                    each uses cache/app.aot
																													
```


Optimized a bit since there are cases the jre custom runtime which includes the modules in it.


Flow with Spring and docker 

```

 ----------          ---------------           ------------------                 -------------------                ---------------------
|  Dev/CI  |        | Runtime-Build |         | Final-Image (os) |               | Build image (tag)  |             | Containers (deployN)|
 ----------          ---------------           ------------------                 --------------------               ---------------------
                           |                          |                                  |                                    |
docker build               |                          |                                  |                                    |      
-------------------------->|                          |                                  |                                    |
ARG JAR_FILE               |                          |                                  |                                    |
-------------------------->| RUN jarmode=tools        |                                  |                                    |
                           |------------------------> |                                  |                                    |
                                                      |                                  |                                    |
RUN mkdir cache &&                                    |                                  |                                    |
    java -XX:AOTCacheOutput=cache/app.aot -jar app.jar| -------------- Training Run ---->|	                                  |
                                                      |                                  |                                    |
													  | -------------- Assembly Run ---->|                                    |
docker build result                                                                      |                                    |
-----------------------------------------------------------------------------------------|                                    |
                                                                                         |                                    |
docker run app:latest                                                                    |                                    |
-----------------------------------------------------------------------------------------|                                    |
                                                                   container CMD:                                             |
                                                                     java -XX:AOTCache=cache/app.aot -jar app.jar			  |													   
scale out                                                                                                                     |
------------------------------------------------------------------------------------------------------------------------------
                                                                                                                    each uses cache/app.aot
																													
```

Keep the same JDK for the each Docker stage.

Another approach is to use the aot cache generated and load to the image as volume mount. This gives an option to leaner image size.

docker build aotgen:tag 

docker run aotgen:tag --rm -e AOT_DIR=/cache -v "$PWD/cache:/cache"

docker build deploy:tag 

docker run deploy:tag -e AOT_DIR=/cache -v "PWD/cache:/cache"

scale out 
