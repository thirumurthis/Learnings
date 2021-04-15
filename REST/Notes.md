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
----------

### Versioning 
  - Based on the requirement

#### Option 1:
  - indicating version in the URI path, like https://exampleweb.com/api/v2/Invoices
     - Pros: very clear to understand
     - cons: when the version changes, the client needs to change the uri path.
     - Not recommended.

#### Option 2:
  - Using query string like https://exampleweb.com/api/Invoices?v=2.0
    - Pros: version is optionally included (we can always default to specific version)
    - Cons: Too easy for clients to miss the needed version

#### Option 3:
   - Versioning with Headers (using X-Version, as below )
   ```
    GET /api/Invoices HTTP/1.1
    Host: localhost:8080
    Content-Type: application/json
    X-Version: 2.0
   ```
     - Pros: Separates versioning from REST API
             version is highly decoupled as the version can be added by an interceptor that is handling the API has to put this version in header.
     - Cons: Requires to additional efforts to add Headers to add request or manipulate the headers.
    
 #### Option 4:
   - Versioning using Accept Headers
   ```
    GET /api/Invoices HTTP/1.1
    Host: localhost:8080
    Content-Type: application/json
    Accept: application/json; version=2.0
   ```
      - Pros: No need to create own custom headers. 
              The response can include the version along with the content-Type header.
      - Cons: Less discoverable than query strings
#### Option 5:
  - versioning using content-type
 ```
    GET /api/Invoices HTTP/1.1
    Host: localhost:8080
    Content-Type: application/vnd.yourwebapp.camp.v1+json
    Accept: application/vnd.yourwebapp.camp.v1+json
 ```
 - Note: we have a Custom content-type per the spec, we it allows to start with "vnd" and .application name as content.
   - example: `Cotent-Type: application/vnd.website.com.v1+json`, `Content-Type : application/vnd.testvalue.cont.v1.0+json`
 - In the above example, we have adding a version.
 - This is used for log lived application. (say there is a list of customer in an API today and two weeks latter there was an update on a customer - This will tell the version of API haven't changed, since the content-Type and the Accept header usually are married with the conten that is stored. Should tell what version of content that recived from the API. 
 - The Content-Type and Accept header Version tell 
    - Pros: Can version the payload and the API itself.
    - Cons: requires lots of development maturity, example: github
----------------

### Security:
   - Cross Domain Security 
     - By default the browser doesn't allow this access data of other domain. (this prevents by running malicious code from running on the browser accessing different API)
     - For Public API consider allow cross domains.
     - For Private API, consider partner domains.
  
  - The Cross Domain Security is done usign Cross Origin Resource Sharing (CORS)
     - Allow access fine grained control over which domain can access, what resources has access to domain.
     - The `CORS is limited to the browser`, in other words if you are using an mobile/desktop app to access the API then CORS doesn't matter in accessing API.
     - Most API will be support since most of the time it is accessed from browser. Considering this part of design is a best practice.

- How CORS work:
  - Some other website request API call to say a API you built.
  - Step:1
    - The browser sends a request, as below
    ```
    OPTIONS /api/movies HTTP/1.1
    Origin: http://moviesite.com
    Access-Control-Request-Method: POST
    Host: localhost:8080
    ```
  - Step:2 
    - The server sends a response, as below (stating the methods allows and the Domains that are allowed)
    ```
    Access-Control-Allow-Methods: GET, POST, OPTIONS
    Acess-Control-Allow-Origin: http://moviesite.com
    Content-Length: 0
    ```
  - Step:3
    - The Browser then issues a CORS header
    ```
    POST /api/movies HTTP/1.1
    Origin: http://moviesite.com
    Access-Control-Request-Method: POST
    Host: localhost:8080
    ```
#### Authorization and Authentication:
  - Authentication => the person, information to determine identity, credentials/claims
  - Authorization => what can you do, Rules about rights (roles, etc)
 
 - Authentication type:
  - `App authentication` - Identifying an app for accessing your API.
    -  Example: in case of a Banking app, once the login is performed with username and password, the API not only validated the credientials also the APP.
    - This is authenticating the developer itself
    - This can be implemented using AppId + key, certificates.
 
  - `User Authentication` - typical user authentication
    - Identifying the users, usually using credentials, claims, oauth (third party authorization)
