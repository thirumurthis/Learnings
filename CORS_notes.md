#### To check or replicate the `CORS` - cross origin resource sharing.
  - open up any website in chrome browser, open up Developers tool.
  - in console type 
  ```js
    fetch(http://localhost:8080/stock/INTC).then(a=>a.text()).then(console.log);
    // http://localhost:8080/stock/INTC -> is the sample spring boot applicaton running on local host
  ```
  Output exception message since the CORS ploicy:
  ```
  Access to fetch at 'http://localhost:8080/stock/INTC' from origin 'https://www.google.com' has been blocked by 
  CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
  If an opaque response serves your needs, set the request's mode to 'no-cors' to fetch the resource with CORS disabled.
  ```
  
  #### In order to resolve the CORS error, in the localhost (spring-boot) application set the header key and value as `"Access-Contorl-Allow-Origin","http://www.google.com"`
  where www.google.com is the host from where the request is initiated.
  
  #### In our case if we need to allow the access from all the server domain, then set the header with "*" like below on the local host application
  ```
  "Access-Control-Allow-Orgin","*"
  ```
