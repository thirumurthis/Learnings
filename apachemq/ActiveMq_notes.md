Apache MQ is `message oriented middleware (MOM)`

When to use Active MQ?
  - `Heterogeneous application itegration`
     - Active MQ broker is written in java, also provides client for other language.
  - As a replacement for RPC
     - Applicaton using RPC uses synchronous calls.
  - `loose coupling` between application
     - loose coupling, provides fewer dependencies.
     - utlize asynchronos communication (where the message is sent without waiting fro response - __`fire-and-forget`__
  - Backbone of an `event-driven architecture`
  

