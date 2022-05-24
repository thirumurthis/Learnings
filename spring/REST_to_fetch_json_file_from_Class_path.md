### Below is the sample code on how to fetch and render the json using jackson library
  - Note: place the file in the classpath, it can be under the resource folder
  - make sure the jar file includes this json file as well

```java

@CrossOrigin
@RestController
@Validated
// for OpenApi - swagger
@Tag(name="Backend-order Service")
public class OrderController{

 
 @GetMapping(value="/app/predefinedaction", produces= "application/json")
 public Map<String, Object> getOrderAction(final HttpServletResponse response ){
   
   Map<String, Object> actionMap = new HashMap<>();
   
   try{
     actionMap = new ObjectMapper().readValue(new ClassPathResource("orderaction.json").getInputStream(),
                                              new TypeReference<Map<String,Object>>(){});
   }catch(Exception exe){
      //Error to be logged in case of exception - file not found
      System.out.err("exception"+exe.getMessage()); //use lomobk or log framework
   }
   
   //tell the browser to cache only for determiend duration.
   response.setHeader("Cache-Control", "private, max-age=10000, stale-while-revalidate=10000");
   return actionMap;
 
 }
}
```
- sample json  (src/main/resource/orderaction.json) 
```json
{
   "draft" : [
     "About to be approved",
     "validated"
   ],
   "pro-forma" :[
     "verified and ready to approve",
     "communication sent out"
   ]
}
```
