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
 
 
 ### Association
   - URI Navigation on sub-objects
```
## Example
/api/customers/123/Invoices  -> specifies the invoices of the customer 123
/api/movies/superman/ratings
/api/invoices/2020-01-24/payments
```
 If the `/api/invoices` and `/api/customers/123/invoices` should return the same format of the result, first might be List for all the customer, latter just invoiced of specific customer.
  - details for specific customer with mulitple associaton.
 ```
 /api/customers/123/invoices
 /api/customers/123/payments
 /api/customers/123/shipments
 ```
 Don't use `query parameter` for the association. 
 If we are performing any serach then use `query parameter`, like `/api/customer?id=123`
 
 ### Paging 
  - use the `query parameter or query string` for paging
```
 /api/sites?page=1&page_size=25
  Note: page=1 should be sufficient but the page_size per page should have default count.
```
 The result in this case can wrap the result with next and privious as below
```json
{
  totalResults : 100,
  nextPage: "/api/sites?page=5"
  prevPage: "/api/sites?page=3"
  result : [
   "key1" : "value1:,
   ]
}
```
 ### Error Handling
   - In some case the REST can send Status code 400,404, etc.
   - return the failure for security resason, for example in login page don't say password incorrect rather say invalid credentials.
   - based on the requirement provide the error response in detail.
  ```
  400 BAD request
  { "error" : "descrption missing" }
  ```

### Caching - required to be truly REST. 
 - Not all the API requires it to be cache.
 - HTTP Cache, using E-Tags
 
 Client: 
    - Sends a GET request for some response.
    - The response will set the version in Etag in header.
    
 - When the client sends the **GET** request again with the Etag version, the server will check and send response with status 304 Not Modified. 
 
 - If the Client want to update the data in a way of concurency, it can send a **PUT** request with `If-Match=version` (this is the version present in Client side that was received at some point of time), it is like saying the server, that client has this version and update data if the version is same as in the server. Else send `412 Precondition failed` if the version is not found.
 
 ```
 Strong caching 
 Weak Caching
 ```
 -  The Etag is set in header of the response with a unique identifier. 
 -  To indicate if the caching is weak Caching, we can add Etag with W/<unique identifier> in response.

##### In order to check if the value is already cached 
 - For GET indicate with 304
   - The GET request can be sent with header `If-None-Match : "012312301231" ` (If-None-Match - holds the unique identifier already received).
   - In the server side if that version identifier matches, then send `304 - NOT MODIFED` or `it's cached` status 
   
 - For PUT/DELETE indicate with 412 (the data to update/modify not same).
    - The PUT request is sent with the header `If-Match : "01231111122" ` (if-match holds the unique version identifier already received).
    - If we need to update the record in the server to udpate the field if match the version,
      - if the version identifier in the Server is not the same, then we respond with `412 - Precondition Failed`.

- Note: The browser doesn't send/set `no-cache` header, but tools like postman sets this value.

#### Functional API
  - API's are not restful.
  - In case if we have some request to recalculate the total or some call to the server, etc. the one of operational API support.
  - This is most of performing operational activities. Always document the side effects.

