

### Camel Message Model

Model is made up of two interface:
 - Message Interface
 - Exchange Interface - additional, which wraps around the In-bound and out-bound message.
 - 
#### Message 
  - Header => Is constructed as a java map
    - key names are unique and case in-sensitive 
    - Values are stored as type Java Object (any type of message, metadata can be stored)
    - Additional construct used is `attachment` which is used in conjunction with the mail.
  - Body => holds the payload (is of type Java Object), when the message is routed some type of conversion happens on the body.
     - Camel Handles of type conversion
  - Fault => this indicates an unsuccessful outcome. (similar to SOAP service faluts)
     - Camel provides support to defining fault in the message
     - 
#### Exchange:
   - Camel passes messages part of routing.
   - the challenge is routes messages has a request, some of these requests require response.
      - has an option to seperate In-bound and Out-Bound Message, this is where the Exchange interface comes in.
   - Exchange is a container for the inbound and outbound messages.
   - The exchange contains metadata, these are the `properties` similar to the message `headers`. This last only for the lifetime of the exchange and contains global level METADATA.
      - one of this properties is the unique id for the exchange called exchange-id.
      - another is exception, that holds the exception happend during the routing.
   - key thing is of exchange, this pertains to the current request and response of the route. It is created when the consumer recieves a request.
   - Message Exchange Pattern (MEP)

     
 
