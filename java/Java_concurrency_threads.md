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
  
#### `ExecutorService` - is a framwork where we define a pool of threads to perform the tasks.

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

#### Callable - used when the thread has to return any data. 
   - Java provides a Future<> object as a place holder. using Future obeject `get()` is a blocking operation.

```java
// Simple implemention 

public static void main(String ... args){
   ExecutorService service = ExecutorService.newFixedThreadPool(10);
   
   Future<Integer> future = service.submit(new SimpleTask()); // in here we get a Future object, upon sumitting the thread
                                                              // In this case the thread is created and provides a place holder which is Future object
                                                              // use the future object get() method to retrive the return value of the thread.
   try {
      Integer result = future.get();           // This blocks the main thread. 
      System.out.println("Result" + result);    
   } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
   }
}

static class Simpletask implements Callable{
   @Override
   public Integer call (){   // mock with Thread.sleep(2);
    return new Random().nextInt();
   }
}
```

```
  ## callable visualization (without executor service) - Future object get method blocks the main thread
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
  
```
## Callable visualization with Executor service 
main
|
| create pool      _________________________________
|-----------------| fixed number of threads         | uses blocking queue -> stores the tasks submitted, 
| service.submit()  t0 t1 t2 ..................t9                         -> and all the 10 threads will 1. fetch the next task from queue and 2. executes it
| Future = blank     |  |  |  | ............... |
|                    |  |  |  | ............... |      say if t0 thread processed the simple task, then it will start process next simple task
 blocked Future.get()
                  |_________________________________|
| Future = result
|
x ends
```

#### `CompletableFuture` - Is used for possible asynchoronous (non-blocking) computation and trigger dependent computation (which could also be asynchronous)
 - Say if we have a process which depends on sequence of step's:  step1 -> step2 -> step3 -> step4 
  - Note: if we use the Future object for perform the dependent tasks. In the below case if we need to perform in multiple thread only one tread will be performed.
  ```
   Future<Step> step1future = service.submit(performStep1());
   Step step1 = step1future.get();                           /// blocking.
   Future<Step> step2future = service.submit(performStep2(step1));
   Step step2 = step2future.get();                           /// blocking.
   ....
  ```
  - The above is a blocking operation for other threads in case if we using for loop this can be viewed, though these task can be executed in parallel thread.

- The `CompletableFuture` helps in this situation

```
// what we need is each in its own thread
main thread
 |
 |  --------------------
 |        t0        t1  ....... tn
 |         |        | 
 |      step1   step1
 |         |        |
 |      step2   step2
 |         |        |
 |       step3   step3
 |        .       .
 |        .       . 
```

```java
## using completablefuture example
for(int i=0; i< 10; i++){
   CompletableFuture.supplyAsync(()->performStep1())
                    .thenApply((step1) -> performStep2(step1))
                    .thenAppy((step2) -> performStep3(step2)) // the parameter can be any value step2 or s2
                    .thenAccept((s3) -> performStep4(s3));
}

// since we didn't provide custom service pool for the above case, the default used is FORKJOINPOOL.

/*** WE Can use the ExecutorService as a parameter as well ***/

ExecutorService ioTaskService = ExecutorService.newFixedThreadPool(10);
ExecutorService cpuTaskService = ExecutorService.newCachedThreadPool(); // no arguments
for(int i=0; i< 10; i++){
   CompletableFuture.supplyAsync(()->performStep1(), ioTaskService)
                    .thenApplyAsync((step1) -> performStep2(step1), cpuTaskService)  // not to use thenApplyAsync() with executor service, thenApply throws compilation error
                    .thenAppyAsync((step2) -> performStep3(step2), ioTaskService) // the parameter can be any value step2 or s2
                    .thenAccept((s3) -> performStep4(s3));
 
}

/********* HANDLING Exception in the flow *************************/
   CompletableFuture.supplyAsync(()->performStep1())
                    .thenApplyAsync((step1) -> performStep2(step1))
                    .exceptionally( e -> new FailedStepException())  // using exceptionally to throw exception
                                                                     // exceptionally is like a catch block, if failed perfom the new failed object step
                    .thenAppyAsync((step2) -> performStep3(step2)) // the parameter can be any value step2 or s2
                    .thenAcceptAsync((s3) -> performStep4(s3));

/* using thenApplyAsync on second consecutive steps, is we need to consecutive steps to be asynchronous.
   CompletableFuture.supplyAsync(()->performStep1())
                    .thenApplyAsync((step1) -> performStep2(step1))
                    .thenAppyAsync((step2) -> performStep3(step2)) // the parameter can be any value step2 or s2
                    .thenAcceptAsync((s3) -> performStep4(s3));
*/
```

#### For more easy concurrent handling, check Rx java library
