## Chain of Responsibility pattern

![image](https://user-images.githubusercontent.com/6425536/196061951-d562e3c5-0668-4151-8984-3559edf241a2.png)

If we need to process a request, but we don't know how to handle till the runtime, then the chain of responsibility pattern is ideal in that ara.

The request will be passed to the handler if that handler is NOT able to process it will pass the request to next handler,
until the specific handler that is able to handle the request. This is chain of response.

Example: Handling Authentication of different type like SAML, OAuth, etc...
