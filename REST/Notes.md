
History of distributed API's:
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
