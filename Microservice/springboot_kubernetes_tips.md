
- application.properites providing profile based support, YAML file used provide that already
   - in order to achieve it
   - pass the -Dspring.config.active.on-cloud-platform=kubernetes when starting the spring boot app so the pofile specific info between #--- #--- will be applied.    
 ```properties
 
 # this will e
 server.port=8080
 
 #---
 spring.config.active.on-cloud-platform=kubernetes
 server.shutdown=graceful                    # another option is immediate
 info.message = Application from k8s         # This message will be displayed in actuator endpoint info.
 
 # to read the config from a file
 spring.config.import=configtree:${HOME}/config    # Say if we have a kubernetes configmap which is mounted to file
 #---

```
 - To create an container image, we need to create yaml etc.
   - The `buildpacks.io` are specification that are part of CNCF.
   - This will read the application artifacts and turn it into a container.
 - The implementation of `buildpacks.io` is `paketo.io`
 - `buildpacks.io` was part of what `heroku` was doing. CloudFoundary and Heroku donated this to CNCF.
 - There is nothing to know about the paketo, add it to the CI/CD pipeline, it will automatically take the jar and creates the image.
 -  we can use the `pack` cli to build the image (manual)
 -  but spring-boot has support out of box, mvn clean spring-boot:build-image 
    - The spring native dependency added will used graal VM native image builder
    - Graal VM is open jdk distribution with extra benefits. It contains, 
       - native image builder - which perform static analysis of the application at compile time and throws away, that is not being used statically.
       - the left out bytecode is used to build the binary, which will be much smaller in size
       - the image will start very quickly
       - The JSON configuration needs to provided, so the static analysis will not throw out necessary part of application
       - The configuration is provided by the spring boot, maven plugin.
  
  The pom.xml with paketo looks like below
  ```xml
  <plugin>
    <groupId>org.spring-boot-maven-plugin</groupId>
    <artifactId>spring-boob-maven-plugin</artifactId>
    <configuration>
        <image>
          <builder>paketobuilderpacks/builder:tiny</builder>
          <env>
            <BP_NATIVE_IMAGE>true</BP_NATIVE_IMAGE>  <!-- the native image is create by compiling the class, all files. this will take several minutes to compile. -->
          </env>
      </image>
    </configuration>
</plugin>
  ```
