#### Interrupting Thread

```java

public class MainApp{
  
  public static void main(String args[]){
     System.out.println("starting..");
     
     Thread t1 = new Thread(new Runnable(){
       
       @Override
       public void run(){
         Random random = new Random();
         
         for(int i=0; i<1E8;i++){
         
          //we can check here to see if the thread is interrupted or not
          
          // this gives the curren thread info
          // at this point only one thread will be running.
          if(Thread.currentThread().isInterrupted()){
             System.out.println("Interrupted...");
             //break the loop
             break;
          }
           Math.sin(random.nextDouble());
         }
       }
     });
     //start the thread
     t1.start();
     
     //Sleep the thread
     Thread.sleep(500);
     
     //Actually this doesn't interrupt the thread
     // this will set the flag to the thread
     t1.interrupt();
     
     t1.join();
     
     System.out.println("Finished");
  }

}
```

- Note:
  - just adding the t1.interrupt(); will not stop the thread with interruption.
  - we need to actually check if that interruption flag is set in the thread run() method itself.
  - in the above code, the for loop takes few second to execute.
  - after the thred is started it will wait for 500 msecond, and set the t1.interrupt() which will interrupt the thread and if we stop it will break.
  
  - if we use the below approach for the interrupted
  - in the above run() method use the below code, the thread.sleep will detect the interrupt flag and throws the exception which we are using to stop it.
  ```java
  
  public void run() {
    Random ran = new Random();
    
    for(int i=0;i<1E8;i++){
      
       //sleep for each iteration
       try{
         Thread.sleep(1);
       }catch(InterruptedException e){
          System.out.println("Interrupted ");
          break;
       }
    }
  }
  ```
  
- ThreadPool has the cancel() method, which will cancel all the threads before they even run. They also set the interrupted flag, which we can use it.

- Future has a method which will used to cancel if that is interrupted.
