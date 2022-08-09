### Check `jackson balckbird` a library that tunes the perfomance of the jackson libraries.

### check the `Apache commons package` - for IO operation

### `MapStruct` java 

### `SocksProxy`

### check the `Apache commons pacakage` - for easy handling of arguments unix style for the program, for exampe `java Main -h` etc

### check `Fractions - Thorntail` packaging uberjar 

Thorntail decomposed.
  - compose throntail around your application
  - just enough enterprise java for microservice package in uber jar
  - with additional functionality outside java ee like netflix oss libraries etc
Uberjar
  - bunder application with all dependencies.
  - the fraction is required to support it, maven repo with dependencies, 
Fractions:
  - Thorntail compositional units providing specfic runtimes capablitites and means configure them

For microservice, leverage spring-cloud-kubernetes projecr (Istio in the feature for service mesh)

`vert.x`  -> is a toolkit to build distributed and reaction application on top of the JVM using an asynchronous nonblocking development model.
    - Responsive: fast, is able to handle large number of events/connections
    - Elastic : scale up and down by just starting and stopping nodes, round-robin
    - Resilient: failure as first-class citizen, self-healing
    - Asynchoronous nonlbocking development model.


```
                reactive ( software responding to events/stimuli )
               /                  |                      \      
             /                    |                       \
      reactive                  Reactive                reactive 
      system                    stream                  programming
         |                        |                         |
      responsive              back-pressure             Asynchronous
      distributed               protocols               development model
      systems
```

#### Quarkus
  - Supersonic; Subatomic; java

- Kubernetes navtive java framework tailered for GraalVM and OpenJDK, crafted from best java libarries that include Eclipse MicroProfile, JPA/Hibernate JAX-RS/RESTEasy, Eclipse vert.x, Netty, etc.
- Fast startup allows automatic scaling up and down of microservice on conatainers and Kubernetes as well as FaaS on-the-spot execution.
- Low memory utlization optimizes container density in microservices architecture deployments requiring multiple containers.


#### In-Memory distributed data, 
  - Distributed in-memory data management system for application data, synchronizing data accross multiple systems providing fast access to data



Openshift service mesh/ISTIO
  - jaeger
  - Prometheus
  - Graphana

FUSE - Redhat suppported version of Apache camel.

- Fuse and Spring boot in containers:
 
 ```
 
                                 ---- camel route (Auto detects routes)
                              /  
 Container  -> Application  {  ----- camel spring boot starter (auto-configure camelcontext, register camel components)
                             \
                               ------ configuration (Auto-wires commpnonets tomcat, JDBC , messageing via applicaiton.properties/yml file
 ```
 
 ### kubernets operator:
 
   - Software that run continously and automates tasks
   - Driven by resource defintion (CRDs) and object state
   - Typically implemented as a controller in Kubernetes
   - Managed by Operator lifecycle Manager (OLM)

Types:
  - Service Operator - example 3scale, Fues online, AMQ online
  - Feature Operator - example: Camle-K, AMQ Streams, 3scale, AMQ online


Redhat containerized developer tooling for openshift (eco system)

- Application runtimes
    - Redhat openshift
    - Redhat datagrid
    - Redhat AMQ
    - OpenJDK
- Integration
    - Redhat fuse
    - Redhat AMQ 
    - Redhat 3scale API management
- Process Automation
   - Redhat Process Automation manager
   - Redhat decision manager


- Application serverices
- Service Mesh
- Enterprise Kubernetes
- Redhat openshift

### Azure SignalR 
##### When exposing application from the Azure DMZ, to internet which is built with WebSocket protocol.
 - In order to set the keepalive configuration continuously in Azure we can use Azure SignalR service.

 The user is authorized to access the web application via internal proxy, then the function client calls are authenticated using JWT token , which negotiates the connection with SignalR and produces signal specific token that the client then uses to connect directly to SingalR to open the websocket connections.
 SignalR is managed websocket orgchestration tool.
 if the internal proxy perimeter doesn't has the support for websocket type connection.
 
 -------
 
 JPA `@DiscriminatorColumn()` Read [Link](https://vladmihalcea.com/the-best-way-to-map-the-discriminatorcolumn-with-jpa-and-hibernate/)
 
 
 Anguar - .prettierignore, add a pre- commit hook in the .git/hook
 Spring java -  prettier-java maven plugin (check google java style guide) this also can be set as precommit git hook
             - Editorconfig (.editorconfig) check the specfic website for usng this plugin in favourite browsers.
             
