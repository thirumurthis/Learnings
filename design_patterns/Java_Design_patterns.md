## Notes Using Java `Optional`

 - Don't return null, instead of empty object 
  - Say if we are about to return a collection of objects, then return empty list like below
    - We can iterate using empty collection like 
    ```
    public List<Customer> getCustomer(long id){
       // if customer for id not present in database 
       // return empty object like 
       
       return List.of();
    }
    ```

  - how to manage in case if the method to return single value, then return Optional
    ```
    public Optional<String> getUserName(long id){
      // if the name is not found then 
      return Optional.empty();
      // don't return null
    }
    ```
    - When we obtain the value from the Optional then use
       - Don't use the optional.get(), since it will sometime return null
       ```
         System.out.println(Optional.orElse("value not found"));
       ```
  - If a function always return a single value, and it always exists DON'T use Optional.
  - If a method may NOT return a single value then use Optional. 
  - Also if a function returns a collection, then DON'T return Optional. Since for collection we can send empty collection.
  - Don't use Optional<T> as parameter to functions, mostly use overloading
      - Also say if a function is going to perform a default action if the passed argument is null, then don't use optional.
  
      ```
      // don't use Optional like below
      public void setName(Optional<String> name){
         if(name.isPresent()){
            // handle different logic
         }else{
           // perform default operation
         }
      }
      // The usage would be,and every time the client code should use optional wrapped around 
      
      setName(Optional.empty());
      setName(Optional.of("userName"));
      
      ```
      - Handle the above case like below
      ```
      // to handle default action use below method
      public void setName(){
       // do something
      }
      
      public void setName(String name){
       // do something
      }
      ```
  - Optional can be used to field
  
  
  

