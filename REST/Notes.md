### REST - REpresentation State Transfer
  - Concept includes:
    - Seperation of client and server
    - Server request are stateless
    - Cacheable request
    - Uniform Interface
  
  Note: Introduced by Roy Fieldings doctoral  dissertion.
  
#### History of distributed API's:
- Browser to Server communitcation 
   - XMLHTTP
   - REST
   - SOAP (came from Microsoft camp, solves some problems like REST does)
   - GraphQL - recent approach 
   - gRPC 
 
#### The most common approach is REST in the enterprise industries. The `GraphQL` and `gRPC` address specific issues.
 
### How does HTTP works?
- The browser makes a request to some remote servers, the browser sends a request.
- The request is actually a text document which contains three parts
   - Verb (Example: POST)
   - Header  (Example: Content Length: 10)
   - content (optional) This is the content that we want to post. (Example: Message From server)
- The server gets the request, it will recognize the request or rejects it. It sends response if it recognizes it.
- The request and response are individual calls, tehre is no connection between the client and server, these are short lived connection.
   - The response is like a text document,that contains
     - Status (did the request succeed, failed)
     - Header (tells the body of the content type : text)
     - Content (response text from server)

#### HTTP Reqeuest and response is stateless, short lived.
Request:
 Verbs:
    - GET
    - POST
    - PUT
    - PATCH - used to update partial resource
    - DELETE

 Headers - are set of name - value pair, the metadata about the request. Like what type of data is the content.
    - Content Type - the type of content data
    - Content size - the size of the content passed
    - Authorization - who makes the call
    - Accept - What type can be accepted
    - Cookies - this is also a data, that is sent along with the request and response, which is used to create state (from stateless)


Content:
  - For web page, this would be HTML,CSS, javascript, etc.
  - content is not valid in some verbs, like GET doesn't has content body.
  - So for GET the content should be in the URI or Headers.

Response 
  Status:
   - 100 - 199 Informational
   - 200 - 299 Success
   - 300 - 399 Redirection 
   - 400 - 499 Client errors
   - 500 - 599 Server Errors

  Header: 
    - Content type
    - Content length
    - Expires - the browser cache, can set to expire
    - Cookies - cookies can also be set by server as well

  Content:
    - content sent back by Server.


To view the request content:
 ```
  $ curl <url>
  $ curl web.com/css/site.css ## this will retrive the css file if the access allowed on the site.
 ```
 
 To view the response details
 ```
 $ curl someweb.com -I
 ## the response data will be viewed in the screen without Body.
 
 $ curl someweb.com -i
 ## -i will list the details of the response and the content from server as well
 ```
 
 ##### Postman - can also be used to achive what `curl` command displayed.
  - check the github.com, where the api is designed to view pulbic info, URL: "https://api.github.com/users/thirumurthis"
 
