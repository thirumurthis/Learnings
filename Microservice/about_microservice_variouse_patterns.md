Microservice:
   - A small unit that has one responsiblity or similar logic to solve problem.
   - small and independent service works together to build highly automated, polygot 
   
Pros:
   - Framework independent 
   - Language independent
   - Independent deployment, development, versioning
   - Easy to scale
   - Better fault isolation (other service can work even on failure.)
   - zero downtime upgraders
   - CI/CD support
   
Monolithic vs N-layer Vs SOA - Microservice

Microservice principles & Design consideration:
  - Modelled around business domain (catolog as domain, order management as a domain)
  - Automation cluture 
  - Hide implementation details
  - Decentralise/decouple all things
  - Deployment can be done independently
  - Isolated Failure, one service failure shouldn't impact other service (desing consideration)
  - Highly observable - each microservice should be able to trace the logs, at a single place.

When to use microservice Architecture?
  - Large application require high release velocity.
  - complex application needs to be highly scalable.
  - Application with rich domains and sub domains
  - Small development team.
  
Microservice challenges:
  - complex
  - Development and testing (building and testing a service relies on other service needs domain understanding and refactoring which can be difficult)
  - Lack of governance - Decentralized approach where different service build with different language can be overhead for maintenance.
  - Network congestion and latency - Each service communicates over the network which can result additional latency because of interservice communication and long chaing of service dependecies. Consider API design to use asynchronous communication patterns.
  - Data integrity - using one service one database pattern has its own persistence, the data will be stored in multiple database as a result data consitency can be challenge.
  - Devops management - automating the build process.
  - Versioning - updating the new feature in the service, careful design and backward or forward compatibility needs to be considered
  
Different pattern used in microservice
 - Decomposition patterns  (making the application to smaller service, to achieve loosely coupled service)
    - Decompose by business capability
    - Decompose by Subdomain
    - Strangler Pattern
 - Integration patterns  (Need for differet format of response, handling different protocols, )
    - API Gateway Pattern - provide unified endpoint
    - Aggregator Pattern - in case more than one service to be aggregated we can use this pattern
    - Client-Side UI Composition Pattern 
    
 - Database patterns - (service needs to be scaled indepedently)
    - Database per Service
    - Shared Database per service
    - CQRS pattern (if some service wants to only read only query segregate those seperately.)
    - Saga pattern
    
 - Communication Patterns 
    - Request/Response pattern - Https/Http are synchronos protocol
    - Messaging Pattern - the service will communicate with each other directly
    - Event Driven Pattern - there will be a common message structure, communication between service happens via events that individual service produce and consuming service react to the event occurrence
    
 - Cross-cutting Concern Patterns (in case of kubernetes, the helm chart had the configuration which was added as configMap during deployment. using the configuration in key vault)
    - Extrenalize Configuration 
    - Service Discovery pattern - Service discovery using kubernetes gave that capability.
    - Circuit Breaker Pattern - configuration within service if no response received by the other service we configure default callback based on failure rate.
    
 - Obeservability patterns - troubleshooting the problems
    - Log Aggregation - collect the logs in common area, using logback elastic appender to Elastic and query it latter from kibana
    - Performance Metrics
    - Distributed tracking
    - Health check - within the service have API endpoints (Http) returns health check (actuator in case of spring boot)
    - Audit logging 
    
 - Deployment Patterns
    - Multiple Service instance per host - deploy multipe instance in same VM 
    - Service instance per Host  - for one service there will one VM or container
    - Serverless deployment - Azure function or AWS Lambda
    - Service Deployment Platform 
    
what is Twelve-Factor app?
  - Created by Adam Wiggins (engineer at Heroku in 2011)
    - This is a fundamental guidelines for any cloud ready application.
    - Proven practices around version control, environment configuration, dependecy isolation, executing apps as stateless resources, working with backing services like database , queue, etc.

Factors are:
   - Codebase - one codebase can be deployed in different environment
   - Dependencies - Explicitly declare and isolate (nodejs for javascript, maven for java, etc )
   - Config  - Store config in environment (create sperate env for each environment)
   - Backing service - consider backing service as attached resources (database, email services to be attached)
   - Build, Release and run - sperate build and run stages 
   - Process - execue the app as one or more stateless process 
   - Port binding - export services via port binding (exposing the port for example in kubernetes)
   - concurrency  - scale out via the process model (how to scale the service)
   - Disposability -  Maximize robustness with fast startup and graceful shutdown (graalVM)
   - Dev/prod parity - keep dev environment, staging and prod similar possible
   - Logs - consider logs as event streams like ELK
   - Admin process - Admin and mangement process should be sperate
   
Advantage:
  - Minimize the time and cost for new developer setting up the project. decleartive format of setup automation.
  - suitable for modern cloud platforms, removing the need for servers and sys admin.
  - limits difference between devlopment and prod,enabling continuous deployment for max agility
  - scale up without major changes to tooling, architecture or development practices so performance will be the priroity
