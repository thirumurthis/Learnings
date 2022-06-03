#### Callable and Future
  - These two classes enable to get return values from threads and allow to throws exception.
  
```java

public class MainApp{

 public static void main(String args[]){
   ExecutorService executor = Executors.newCachedThreadPool();
   
   //Callable is used to get the return value
   //Callable is a parameteric class,which takes the 
   // return type of the thread
   //Callable<Integer> - expects Integer type to be returned
   
   //Future is also parameterized like Callable and the return type should be same for both future and callable.
   
   // we can store the future in arraylist when there are many threads returning value
   Future<Integer> future =  executor.submit(new Callable<Intger>(){
        
        //Callable has method call which needs to be overrided
        
        // Note since the callable declared with parameter Integer, call() should return Integer.
        @Override
        public Integer call() throws Exception {
        
        Random random = new Random();
        int duration = random.nextInt(5000);
        
        System.out.println("Starting..");
        //mocking the business logic using thread.sleep() for convience
        try{
           Thread.sleep(duration);
        }catch(InterruptedException e){
           e.printStackTrace();
        }
           return duration;
        }
      
      }
   
   });
    executor.shutdown();
    
    //wait 
    exceutor.awaitTermination(1,TimeUnit.DAYS);
    
    try{
    System.out.println("Returned from the thread : "+future.get());
    } catch(InterruptedException | ExecutionException e){
      System.out.println("exception");
    }
 }
}
```

- we used a callable method to perform logic, and the return value of the callable can be fetched using the future.
- The executor.submit(), returns a future object
- Note: If the future.get() in the sysout above is called without using the exceutor.awaitTermination(), might block the thread holding the future value until that thread is terminated - This can't be seen visually since it runs veryfast.

 - How to throw exception from the Callable
 
```java

public class MainApp{

 public static void main(String args[]){
   ExecutorService executor = Executors.newCachedThreadPool();
   
   //Callable is used to get the return value
   //Callable is a parameteric class,which takes the 
   // return type of the thread
   //Callable<Integer> - expects Integer type to be returned
   
   //Future is also parameterized like Callable and the return type should be same for both future and callable.
   
   // we can store the future in arraylist when there are many threads returning value
   Future<Integer> future =  executor.submit(new Callable<Intger>(){
        
        //Callable has method call which needs to be overrided
        
        // Note since the callable declared with parameter Integer, call() should return Integer.
        @Override
        public Integer call() throws Exception {
        
        Random random = new Random();
        int duration = random.nextInt(5000);
        
        //mocking
        if(duration > 2000){
          throw new IOException("greater duration");
        }
        
        System.out.println("Starting..");
        //mocking the business logic using thread.sleep() for convience
        try{
           Thread.sleep(duration);
        }catch(InterruptedException e){
           e.printStackTrace();
        }
           return duration;
        }
      
      }
   
   });
    executor.shutdown();
    
    //wait 
    exceutor.awaitTermination(1,TimeUnit.DAYS);
    
    try{
    // when the Callable, throws that IOException, the future.get() will throws it as ExecutionException.
    
    System.out.println("Returned from the thread : "+future.get());
    } catch(InterruptedException | ExecutionException e){
      System.out.println("exception");
    }
 }
}
```

- Note: 
  - Any exception thrown during the Callable process, the future.get() will catch it as ExecutionException.
  - using ExecutionException e) e.getMessage(); will have the same message.
  - to fetch the IOException back, use 
    IOException ex = (IOException)e.getCause();
 
- Useful methods in the Future.
   - future.isDone() - tells whether the thread 
   - future.cancel() - cancel the thread
 
- If we don't want the Callable to return any value then in the future use ? as parameter, and Void in callable.

```

Future<?> future = executor.submit(new Callable<Void>(){

 public Void call() throws Exception {
 
    // perfom business logic
     return null;
 }
});
```
