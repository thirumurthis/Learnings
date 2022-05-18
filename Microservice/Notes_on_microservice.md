Micro service

   Distribution tax - Network latency
   can be addressed by using Reactive approach.

Communication between service in leveraged using REST over Http way.
Other methods like Event based communication, but note inter-service calls are REST HTTP.

Service Documentation
    Developing documentation of services is critical.

In a Microservice size is not critical as operation.
    **- one set of related function, with little or no cross domain operations.

Concept:
   > 'Domain Dirven Design'

Comparing analogy:

OOPs
Class is build to handle one
type of thing and provide the operation for that type.

Micro-services
> Service operates on well defined domain. Operation aren't defined
as on the data objects or business objects instead a domain as whole.

> very low level data focus service, often this service exposes domain
specific CRUD operations.
> That domain can span multiple data objects.
> Can built service for set of related business operation that can span one
or more domain.

Service Design Anti-Patterns
     * Too fine grained (usually this is most implementation and
     cases distribution tax - latency issue)
     * Not fine grained enough

Size of the services causes this.
Small size : build faster, few test scenario, deploy and start much faster.
mistakes can be fixed quicker.

-------------

In Microservice architechture the communication between services is through
HTTP using REST.
This allows for use of any coding language or framework that supports RestFul serices.

Each service can consume any other service over the same communication startegy.
This strategy is called, protocol-Aware Heterogeneous interoperability.

Protocol-aware heterogeneous interoperability, explains that the
services are bound to a protocol, in this case HTTP, and execute communication over
that protocol in a way that works in a mixed, or heterogeneous, environment.

Problem is network calls.

Orchestration is key, each service must maintain certain level of passivity in the API.
or there is a system failure, since there is no delineation on which service is calling
which service, there must be solid versioning  or perfect passive API.
So the calling system will not fail.

Calling system from failing when releasing new version of API.

*** Strong versioning startegy along strong passivity rules. ***
--------------------------
Communication pattern:
    Distribution on a single datacenter is not a mandate or requirement.

Distribution and scalability is the advantage of microservice
If an service providing cusotmer data is being used by the other service
more often, the load can be distributed just by creating multiple instance
using API proxy layer, there is no change to the code.

I.e. individual service can be scaled individually.
----------------------------

Service invocation in microservices architecture is over remote network call,
like connection setup, tear down, and wire latency on every single call.

Latency is relatively insignificant for a single call.
Eventually the single call becomes many as the code path
grows, which in addition increases latency.
If the service is on load, the risk of latency is increase in response time.

GridLock:
> Latency can exponentially exaggerated in a model where every call is remote.
At certain point this latency can develop into a gridlock of the system as a
whole. While calls are waiting for response, delays can be unexceptable leading to
failure.

> another path to gridlock is due to circular calls. Any service can be call
and othe service in microservice world. The calling service is subsequently
called multiple times by down stream service, which is circular call stack.
The latency can be a problem, when a multiple service can depend on service involved
in cicular call stack.

How to overcome gridlock and latency issue?
 > To control this negative reacton to gridlock and latency, one such pattern is to use
 a ***circuit breaker***.
 > in circuit breaker, has a standard behaviour when latency occurs time-out occurs.
 then trip the curcuit and began the default behaviour (basic or reduced functions).
     -There is possible sufferring of reduced function.
     -When the service are back to normal, circuit closes and behaves normal
Example:
 Netflix Hystrix: supports the circuit breaker pattern.
 > Strong time-out logic, can limit the use of circuit breaker.

Key for better microservices:
   > strong time-out logic
   > global distribution of all service offering
   > scalable individual load
   > usage of circuit breaker pattern

   ------------------
   Sizing the microservice:
      > leverage Domain driven design strategy
      > focus on bounded context when decomposing large multi-domain system into
      individual service.

   Design pattern used to decompose Domain Driven Design pattern.
     * Investigate working system
     * Determine the domains
     * Break services up accordingly (boundaries of system)

How to determine the bounded context of domain for splitting the granularity?
    << Don't focus on just splitting the service bases on data domain. This might
    some time lead to latency issue.>>

   Analyze the traffic patterns based on real world use cases.
    > reduce the cross-domain calls.

Example:
Customer service & login service
As determine the traffic pattern,
when the login service is hit multiple times the customer service/domain is called.
- There might be a suggestion to put the two domains as one domain.
- we need to determine the boundary, the data of customer domain might be saved in a
different manner, etc. Reason to have it as a separate domain.

There is no specific reason to do this, but building strong bounded context will
reduce the number of calls made.

Another advantage to have better well defined context:
   > for strong contract
   > self-discovery as a whole system

Consumer should be able to locate the correct location to discover the service.

  ** bounded context => boundary definition
--------------

  Data access layer (data service layer):
      In data layer we need to consider more into account other than
      bounded context, we need to deal with data transaction also.

  Removing the transactional boundaries in the existing database is challenging.
  >>> Don't try to leverage the distributed transaction for microservice.

Building low level data domains is the hardest part.
challenge is to decompose the monolith database into smaller individual system.

2 Ways to decompose monolith database to smaller database:
   > Break the database and break it as a small database.
           - this yields a  quicker path
           - Migrating the data in a live system is difficult.
   > building a API layer. (recommended pattern building data domain)
          - start with the services instead of db, all the service will be connect to monolith database.
          - start to see the domains are well defined.
          - start modelling the data domain itself.
          - overall objective is to minimize the cross domain calls.
          - enforcing the transaction boundaries
--------------------

distributed transaction is difficult to manage.
    Traditional system adds for ACID concept.

in Microservice we relay on >>> BASE model <<< rather than ACID.
and strive for eventual consistency across highly available distributed systems.

we aim for situation where assuming the data isn't modified again.
we eventually achieve the end state in all nodes of the distributed system.

In microservice architecture we need to identify where we need ACID transactional
and define service boundaries around those operation.

Aim for eventual consistency.

----------------------
API LAYER:
    The api shouldn't execute logic.
    >> API in Microservice is an aggregated proxy of all our operation.

In case of making the services being accessed directly the host by the consumer,
it will be a complicated when there are more number of consumer.

The API layer could leverage the proxy and the development will be easy.

A passive API can handle versioning.
--------------

Asynchronous communication:
    Leveraging of event-driven Asynchronous communication reduces Latency
    To achieve this is not easy.

    > Event-Driven using Message Queue (Asynchronous).
    > Stream data platform, the events are written to the center message systems
    or broker, this events triggers listener operator. this is highly useful in
    distributed environment.

Dead letter queue - should be monitor
-----------------

Logging and tracing :
        plan for unified logging strategy against all platform.

Logging in microservice is noisier.
     Large volume of artifacts
     Agile nature
     Different team different logging Strategy.

log aggregators:
      should be used in case of distributed.
       this is uniform
Tracing:
      Create a unique token, use it for all the logging event for all the services
      involved, each service uses the token and passes it.
-------------------

continuous delivery:
     Agility

Routing technique
or blue green technique used for production deployment

-----------------

Hybrid architecture: Hierarchy and service based services:

Hierarchy:
    Models an n-tier hierarchy modelling {expose data as service, business process
    as services, edge services}
    use this only for transition

---------------

Design Consideration:
    > 1. CI/CD implementation should be first even starting the coding.
    > 2. Design on logging and tracing should be considered. (use log aggregator
    and search feature.)
    > 3. Evaluate and consider non-blocking code. Standardize stack when possible.
    > 4. Try to implement the calls Asynchronous activity - reduces latency.

------------
Mark Richard and Martin Fowler.

Dichotomy, issues of complexity
