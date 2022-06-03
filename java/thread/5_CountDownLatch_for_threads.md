
#### Countdown Latches Multi threading
 - Java library provides lots of thread-safe classes to work with multi thread environment, `CountdownLatch` is one such class.
  
 -  `CountdownLatch` - let us countdown based on the value passed.
 - One or more thread can count down the latch and when it is equal to 0, then one or more thread waiting over the latch can proceed (or start processing)
 

```java

class Processor implements Runnable{
   //pass the variable countdownlatch
   private CountdownLatch latch;
   // create constructor to set latch
   
   public Processor(CountdownLatch latch){
     this.latch = latch;
   }
   
   public void run(){
     System.out.println("Started ");
     
     // we perform some activity
     //we mock this using Thread.sleep
     
     try{
        Thread.sleep(3000);
     }catch(InterruptedException e){
       e.printStackTrace();
     }
     
     // Note the latch variable is not set with synchronized keyword since 
     // this is already thread-safe
     // we can call the count down anywhere during the process
     latch.countDown(); // this will count down by 1
     
   }
}

public class MainApp{
  public static void main(String str[]){
   //span 3 process using Executors
  
    //CountdownLatch in the main thread
    CountdownLatch latch = new CountdownLatch(3);

  
ExecutorService executor  = Executors.newFixedThreadPool(3);

//we can create 100 process using the for loop. 
// we can count down from 100 to 0; (by passing 100 in the countdownlatch)
for(int i=0; i<3; i++){
   // the latch created from this class
   executor.submit(new Processor(latch));
  }
  // This will wait till the count down latch to 0
  // we are specifiying this latch wait in main thread.
  // we are not limited to add this await in the main thread, we can add this in any number of tread as well.
  try{
  latch.await(); // it also takes timeout
  } catch(InterruptedException e){
    e.printStackTrace();
  }  
  
   System.out.println("completed");
}
```
  - When the above code is executed, all the threads will be started, each thread will call countdown after 3 seconds. each countdown decrements latch by 1.
  - when it reached 0, await finally return and the completed printed
  
