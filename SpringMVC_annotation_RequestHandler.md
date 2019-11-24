In a Client - Server based application, the application used a angular with data table component.

The data table component was usign ajax call to send data as GET query parameter which exceeds the httpmaxHeader config limits.

Handling the client side request in Spring controller to automatically bind to object. (custom type converter)


Create annoation

Create annotation resolver, which implements spring HandlerMethodArgumentResolver class.
  override **resolveArgument()** and **supportParameter ()**
  supportParameter should return parameter.hasParameterAnnotation(Employee.class); or true.
  The code block can also be used to set new request body.
  
  
  To get the http request body content 
  ```
  // NativeWebRequest webRequest 
  HttpServletREquest servletReq = webRequest.getNativeRequest(HttpServlet.class);
  String bodyJson = (String) webrequest.getAttribute("JSON_REQUEST_BODY" , NativeWebRequest.SCOPE_REQUEST);
  if(bodyJson == null){
    String bodyContent = IOUtils.toString(servletRequest.getInputStream()); // apache common io package.
    //servletReq.setAttribute("JSON_REQUEST_BODY",body); // this will send the json pay load to serverside.
    return bodyContent;
    }
```
