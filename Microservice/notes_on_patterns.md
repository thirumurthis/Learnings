#### Microservices challenges

 - Transactional challanges
     - ACID within service
     - Eventual between services (using BASE)

CAP - Consitency, Availability, Partition. Only 2 can be applied either CP or AP. Most prefered is AP (availability partitioning)

Saga pattern, for distributed transaction.
- Transaction out-of-box pattern or Choregraphed
- Event Sourcing or Orechestration

Check links 
[1. Event Driven approach](https://eventuate.io/whyeventdriven.html)
[2. General Microservice blogs](https://microservices.io/adopt/index.html)
[3. Saga pattern ](https://microservices.io/patterns/data/saga.html)


Git links for saga details:
[1](https://github.com/eventuate-tram/eventuate-tram-sagas)
[2](https://github.com/eventuate-tram/eventuate-tram-sagas-examples-customers-and-orders)
