## Voltaile keyword - is mostly used not to cache the variable in the memory during thread process.

#### use of `volitile` keyword

  - Note the above code the Processor class has the variable running, in some cases, the java internally cache the running and never notice the change when used in threads.
  - Basically the main thread and the process thread are using the variable, only when the main thread is changing the state of the another thread.
  - Sometime part of optimization there might be odd behaviour.
  - using `volatile` keyword ensure to prevent threads from caching variables so garuentees latest state is available.

```java

class Processor extends Thread{

  // private boolean running = true; 
  
     private volatile boolean running = true; 
  // we are overriding the run method
   public void run(){
   
     // we are basically creating a infinite loop and stop using the flat
     while(running){
        System.out.println("output");
        try{
         Thread.sleep(100); // sleep for 100 ms
       }catch (InterruptedException e){
         e.printStackTrace();
       }
      }
   }
   
   public void shutdown(){
     running = false;
   }
}


public class MainApp{
   public static void main(String args[]){
   
     Processor process1 = new Processor();
     
     process1.start();
     
     // we get the input from the user using scanner
     
     System.out.print("press enter to stop");
     Scanner scan = new Scanner(System.in);
     scanner.nextLine();
     
     process1.shutdown();
   }
}
```

#### Using the synchronized keyword
  - Below code without the synchronize the output of count value will be different for each run
  - when we do count++ in the thread t1, t2, there are three steps, 1. reading 2. incrementing 3. storing it. 
  - if we don't use the synchronize there is possiblity that when one thread is incrementing another thread might be reading it. so there will be inconsistent output.

Solution:
  - to use a synchronized operation
  - another option is to use AtomicInteger

Note:- we can think to use voltaile mke this work but it will not fix since the underying problem is not caching here, random access between the thread.
  
```java

public class MainApp{
  
  //variable at the class which will be incremented by the thread.
  
  private int count =0;
  
  public static void main(String args[]){
  
  
  }
  
   public void performWork(){
   
    Thread t1 = new Thread(new Runnable(){
      @Override
      public void run(){
        for(int i=0;i <1000000l;i++){
        count+;
        }
      
      }
    });
    Thread t2 = new Thread(new Runnable(){
      @Override
      public void run(){
        for(int i=0;i <1000000l;i++){
        count+;
        }
      
      }
    });
    
    t1.start();
    t2.start();
    
    //If we don't use the below code once the main thread is closed the t1 and t2 will be closed. 
    //So we need to wait
    
    try{
        t1.join();
        t2.join();
    } catch(InterruptedException e){
       e.printStackTrace();
    }
    
    System.out.println("count is: "+count);
   }
}
```
  - How to convert to synchronized.
   - Use an function to increment the variable.
```java

  public synchronized increment(){
     count++;
  }
 // and in each thread instead of using count++; simply call the above increment() method
```

 Tips: 
   - Every object in Java has an intrensic lock or monitor lock (sometimes called as mutex)
   - If we call the synchronized method of that object the java acquires the intrensic lock and only one thread acquires the intrensic lock. 
   - If the object is under intrensic lock, another thread can't access it simply it should wait.

   
