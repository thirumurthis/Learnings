## Resiliency in microservice:

### Rate Limiter pattern:
  - makes service highly available by limiting the number of request made during specific window.
  - Implemented using `resilient4j`

Checkout [link](https://www.vinsguru.com/rate-limiter-pattern/)

#### Rate Limiter vs Circuit Breaker:
  - Rate Limiter - helps protect the server from over loading by controlling input.
  - Circuit Breaker - helps client functional when the target server failed /unresponsive.
  - Use `@RateLimiter` annotation with configuration in application.yaml

### Circuit Breaker Pattern
   - When request Service B is un-responsive, there is no point to send request from Service A to Service B continuously.
   - The circuit breaker simple skips the call and uses the fall back method/ default values for certain duration.
   - The Circuit breaker pattern retries after some time
   - State: `Based on the failure rate threshold configured`
     - CLOSED - If service is up, request are allowed.
     - OPEN - Service is not available.
     - HALF_OPEN - Once the state becomes OPEN, wait for sometime in OPEN state.
   - Use `@CircuitBreaker` annotation with configuration in application.yaml

### Timeout Pattern
  -  Assume there are multiple services (1,2,3,4), one service (1) might depend on the another service (2) which in turn might depend on 3 and so on. There are possiblities for  some network issue and Service 4 might not respond as expected.
  - This slowness could affect the downstream services – all the way up to Service 1 by blocking the threads in the individual services.
  - It is better to take this service slowness/unavailability issues into consideration while designing your Microservices by setting a timeout for any network call.
  - Use `@TimeLimiter` annotation with configuration in application.yaml
     - Pros: we don''t block the threads indefinitely.
     - Cons: still threads are blocked for short duration
     
### Retry Pattern
  - When running multiple instances of same Service for high availability and load balancing. If one of the instances has issue and it does not respond properly to our request, If we retry the request, the load balancer could send the request to a healthy node and get the response properly. So with Retry option, we have more chance for getting the proper response.
    - Use `@Retry` annotation with configuration in application.yaml

### Bulkhead pattern
  - A ship is split into small multiple compartments using Bulkheads. Bulkheads are used to seal parts of the ship to prevent entire ship from sinking in case of flood.
  - The application should be split into multiple components and resources should be isolated in such a way that failure of one component is not affecting the other.
  - Bulkhead Pattern helps us to allocate limit to resources which can be used for specific services. So that resource exhaustion can be reduced.
  - Use `@Bulkhead` annotation with configuration in application.yaml
