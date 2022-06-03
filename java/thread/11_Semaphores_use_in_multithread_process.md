#### Semaphores:
  - Create a Semaphore object
  - A Semaphore is an object that maintains the count.
  - we can get available permits
-Note: since the `acquire()` and `release()` method wait for the permit to be available in a thread safe way this is explained below.

```java

public class MainApp{
  
  public static void main(String args[]) throws Exception{
  
  // an object maintains the count 
  // the count is the number of availabe permits
  Semaphore sem = new Semaphore(1); //1 is the count 
  
  //release() in semaphore will increment the count
  sem.release();
  
  //acaquire() in semaphore will decrement the count
  sem.acquire(); // the acquire will wait until the permit is available 
  
  //we can use the semaphore like a lock
  // if the semaphore with count 1 permit, is like lock and unlock.
  // in lock we needto unlock from the same thread we locked from, in case of Semaphore we don't have such requirement.
  // we can use Semaphore to access some resources.
  
  
  //getting the available permits 
  System.out.println("available permit " +sem.abailablePermits());
    
  }

}
```
  - Note: if the 
  ```
  Semaphore sem = new Semaphore(0);
  sem.acquire(); // this will wait till the permit is available. in this case waits for indefinitely
  ```
  
- Example of using Semaphore
  - say we want to limit the number of connection at given point of time we can use semaphore
    - When we make connection we acquire the permit
    - once connection is finished or releasing the connection we release the permit 

```
public class Connection {


 private static Connection instance = new Connection();
 
 private int connections= 0;

// say we want 10 connection at time.
private Semaphore sem = new Semaphore(10);

 
 // singleton pattern
 private Connection(){
 }
 
 public static Connection getInstance(){
   return instance;
  }
  
  public void connect(){

  try{
   sem.acquire();
   }catch(InterruptedException e){
     //exception handler
   }

   try{ 
     //even if the code throws exception here
     // the finally block will release the semaphore
     doConnect();
    }finally{
      sem.release();
    }
  }
  
  public void doConnect(){
  
    synchronized (this){
      connections++;
      System.out.println("Current connections "+ connections);
    }
    
    try{
    Thread.sleep(2000);
    }catch(InterruptedException e){
      // exception handler
    }
    
    synchronized(this){
      connections--;
    }
  }
 }
 
 
 public class MainApp{
   
   public static void main(){
   
  
   // cahced thread pool, when we submit 200 times 
   // creates the thread and reused
   ExecutorService exec = Executors.newCachedThreadPool();
   
   for(int i=0;i < 200; i++){
      executor.submit(new Runnable(){
         public void run(){
             Connection.getInstance().connect();
         }
      });
   
      exec.shutdown();
      exec.awaitTermination(1,TimeUnit.DAYS);
    }
   }
 
 }
```
  - Note:
    - The Semaphore constructor has fairness parameter, by setting that to true 
    - it means that which ever thread called the sem.acquire() will get the permit first when that is available. Sort of the order is followed.
    ```
    Semaphore sem = new Semaphore(10, true);
    ```
    - false, means that is not garuenteed, this is performance benefit
