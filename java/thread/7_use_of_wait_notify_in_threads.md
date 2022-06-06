#### Wait and Notify:

```java

public class Processor{

  public void produce() throws InterruptedException{

     //using intrensic lock of the processor object itself
     synchronized (this){ 
       System.out.println("producer thread running..");
       wait(); // every object in java has a wait method it is method of Object parent itself
       //use the timeout without any timeout the thread will wait indefintiely 
       // wait only called within the synchronized block.
       //wait will wait without consuming lots of resources, we can use in while loop that wanted 
       //to check change of the flag we can to wait and check for change of any flag.
       //wait() actually hands over the control of the lock that synchronize block locked on, 
       //this block will lose the locks, this thread will not resume until below two things
       // 1. it must be possible for this thread to regain the control of the lock in order to resume.
       // 2. i can span another thread that locked on the same object and call the method notify();
     
     }
  }
  
  public void consume() throws InterruptedException{
  
      Scanner scanner = new Scanner(System.in);
      
     // this makes sure the produce starts first
     Thread.sleep(2000);
     
     //not the lock is on the same processor object
     synchronized(this){
        System.out.println("press return");
        scanner.nextLine(); // wait for user input
        System.out.println("enter pressed");
        // Since the same processor object used in the synchronized block and is in wait state in the produce ()
        //method calling the notify() in this thread will notify and wake up the other thread

        notify();
        // notifyAll(); // notifies all the wait thread
     }
  }
}

// separate file
public class MainApp(){

  public static void main(String args[]){
  
  final Processor processor = new Processor();
  Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.produce();
       }catch(InterruptedException e){
         e.printStackTrace();
       }
     }
  });
  
    Thread t2 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.consume();  ///consumer part
       }catch(InterruptedException e){
         e.printStackTrace();
       }
     }
  });

    t1.start();
    t2.start();
    
    try{
      t1.join();
      t2.join();
    }catch(InterruptedException e){
      e.printStackTrace();
    }
  }
}
```
- Note: 
  - immediately after the notify() the wait() method on the produce thread is not invoked.
  if we add a thread.sleep(5000) after the notify(), we will see since the consumer already 
  had lock on that object it will wait till the synchronize block on consume() method to complete.
