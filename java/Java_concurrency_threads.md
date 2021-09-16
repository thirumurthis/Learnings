 - Threads: (Runnable & Callable)
 
   - 1 Java thread is 1 OS thread, and creating on its own is an expensive operation
   - we need a fixed number of threads to be created as a pool, and we submit the task to it.
       - if we need 100 threads to run certain task, we can create a thread pool with 10 fixed threads, and submit the tasks to it
       - so at any point of time 10 threads will be processed by the thread pool, once it one thread is freed up, another task will be processed by another thread
   
  #### Runnable - doesn't return any data 

```java
//## simple implmentation using threads Runnable

public static void main(String ... args){

 for(int i =0; i<10;i++){ //running the thread as 10 instance.
    Thread th  = new Thread( new SimpleTask()); // passing the runnable class 
     th.start(); //start the thread 
   }
   System.out.println("Thread : " + Thread.currentThread().getName()); // main thread prints main.
}
static class SimpleTask implements Runnable{
   public void run () { //override
      System.out.println(Thread.currentThread().getName();
   }
}
```

```
  ## Runnable representation
    main thread
    |
    | th.start()
    |------------- thread0------ thread1......
    |             |              |
    |             |              |
    |             |              | 
    |             x  ends        x ends
    x ends
  ```

  #### Callable - used when the thread has to return any data. 
   - Java provides a Future<> object as a place holder. using Future obeject `get()` is a blocking operation.

```java
// Simple 
```

```
  ## callable - Future object get method
    main thread
    |
    |------------> thread1
    |             |
Future.get()      |
   <blocked>      |
                  |
      <-----------|
    |
    x ends
 ```
 
 
 - `ExecutorService` - is a framwork where we define a pool of threads to perform the tasks.

```java

public static void main(String... args){
    ExecutorService service = ExecutorService.newFixedThreadPool(10);
    for (int i=0; i< 10; i++){
       serice.execute(new SimpleTask());   // here we execute the tasks
    }
    System.out.println(Thread.currentThread().getName());
}

static class SimpleTask implements Runnable{
   public void run () { //override
      System.out.println(Thread.currentThread().getName();
   }
}
```

```
## Executor service visualization
main
|
| create pool      _________________________________
|-----------------| fixed number of threads         | uses blocking queue -> stores the tasks submitted, 
| for(0 to 100)     t0 t1 t2 ..................t9                         -> and all the 10 threads will 1. fetch the next task from queue and 2. executes it
| service.start      |  |  |  | ............... |
|                    |  |  |  | ............... |      say if t0 thread processed the simple task, then it will start process next simple task
|                 |_________________________________|
|
x ends
```

###### Choosing number of threads of Thread pool.
  - For CPU intense operation, say if there are 4 cores then best to use 4. 
     -  Since at any point of time only 4 thread can perform CPU intense operation since time slice for each thread by os.
     ```
     // simple way to get the number of cores
     int coreCount = Runtime.getRuntime().availableProcessors();
     ExecutorService service = ExecutorService.newFixedThreadPool(coreCount);
     ```
  - For IO intense operation (netowrk calls - http/https calls, Database calls) the thread will wait for response, most of the threads will wait for response.
     - In this case, we can provide more number of threads, like 20 or 30, even 100.

