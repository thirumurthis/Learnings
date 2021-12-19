### Rate Limiter pattern:
  - makes service highly available by limiting the number of request made during specific window.
  - Implemented using `resilient4j`

Checkout [link](https://www.vinsguru.com/rate-limiter-pattern/)

#### Rate Limiter vs Circuit Breaker:
  - Rate Limiter - helps protect the server from over loading by controlling input.
  - Circuit Breaker - helps client functional when the target server failed /unresponsive.

### Circuit Breaker Pattern
   - When request Service B is un-responsive, there is no point to send request from Service A to Service B continuously.

### Timeout Pattern

### Retry Pattern

### Bulkhead pattern
