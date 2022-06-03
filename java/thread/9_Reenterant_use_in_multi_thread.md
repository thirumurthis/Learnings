### Re-entrant locks
  - This is an alternate to use of synchronized keyword
  
 - Note if we use the code without surrounding the increment() method with try finally block, that is not a best code.
 - what happens if the increment() method throws exception. we need to handle reentrant lock unlock() in finally block surrounding the increment() with try.

```java

public class Runner {

private int count =0;

//using the re-enterant lock
// this impelments lock interface
// once the thread acquires the lock, it can keep counts of the number of time and we need to unlock that many times.

private Lock lock = new ReenterantLock();

private void increment(){
   for(int i=0; i<10000;i++){
      count++;
   }
}
  public void firstThread() throws InterruptedException{
  
      // locked to lock
      lock.lock();
      try{
      increment();
      }finally{
         // unlock the lock
         lock.unlock():
      }
  }
  
  public void secondThread() throws InterruptedException{
     lock.lock();  

     try{
            increment();
     }finally{
         // unlock the lock
         lock.unlock():
      }
  }
  
  public void finished(){
     System.out.println("Count is "+count);
  }
}

// separate file
public class MainApp(){

  public static void main(String args[]){
  
  final Runnter processor = new Processor();
  Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.firstThread();
       }catch(InterruptedException e){
         e.printStackTrace();
       }
     }
  });
  
    Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.secondThread();
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
    
    process.finished();
  }
}
```
- Wait and notify/notifyAll equivalent in Re-entrant lock are await() and signal() /singnalAll() respectively, whoever wrote the code had to rename it that way.

- Note, the wait and notify are the methods of the Object class and every object has it.

- await() and signal() are not methods of the reentrant lock itself, they are methods of condition.

- await() and signal() can be invoked only after the reentrant lock object is locked
  
public class Runner {

private int count =0;

//using the re-enterant lock
// this impelments lock interface
// once the thread acquires the lock, it can keep counts of the number of time and we need to unlock that many times.

private Lock lock = new ReenterantLock();

// condition for using await and signal
private Condition cond = lock.newCondition();


private void increment(){
   for(int i=0; i<10000;i++){
      count++;
   }
}
  public void firstThread() throws InterruptedException{
  
      // locked to lock
      lock.lock();
      
      // Also we can only call the await or signal only after locking the lock
      
      // similar to wait(); method in sync block
      cond.await(); 
      
      System.out.println("Wake up ..");
      
      try{
      increment();
      }finally{
         // unlock the lock
         lock.unlock():
      }
  }
  
  public void secondThread() throws InterruptedException{
     Thread.sleep(1000);
     lock.lock();  
     System.out.println("press enter");
     new Scanner(System.in).nextLine();
     System.out.println("Entered");
     
     //once this signal is reached the firstThread() await() will be woke up and prints the next statment.
     // the await() will need to re acquire lock, so only when the secondThread lock is unlocked the firstThread will start proceeding
     
     cond.singal();
     
     try{
     increment();
     }finally{
         // unlock the lock
         lock.unlock():
      }
  }
  
  public void finished(){
     System.out.println("Count is "+count);
  }
}
```
