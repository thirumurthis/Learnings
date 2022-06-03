#### Threadpool Executors in java

```java

class Processor implements Runnable{
  
  private int id;
  
  //constructor
  public Processor (int id){
    this.id = id;
   }
   
   public void run(){
     System.out.println("Starting ..."+id);
     
     //actually perform some process 
     
     try{
       //mocking some process taking time
       Thread.sleep(5000);
     }catch (InterruptedException e){
        e.printStackTrace();
     }
     System.out.println("completed "+id);
       
   }
}

public class MainApp{
  public static void main(String args[]){
    // we will run few threads of the processor
    ExecutorService executor = Executors.newFixedThreadPool(2);
    
    //submit the task to the executor service
    
    for(int i=0; i<5; i++){
       executor.submit(new Processor(i));
    }
    
     //executorservice has its own mangerial thread, 
      //to which we need to say after the submitted task is completed
      // to stop the service
      // this will not stop immediately but wait for the tasks are completed.
      executor.shutdown();
    
      System.out.println("All task sumbittted");
      
      //wait for the task to be completed in this case 5 task in for loop
      // we are saying wait for 1 day.
      try{
      executor.awaitTermination(1, TimeUnit.DAYS);
      }catch (InterruptedException e){
        e.printStackTrace();
      }
      
      System.out.println("All completed");
      // if the awaitTermination is passed with 10 seconds, if the tasks are not completed within the 10 second it will stop the threads
  }
}
```
