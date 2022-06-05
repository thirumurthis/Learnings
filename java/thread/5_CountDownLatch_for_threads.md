
#### Countdown Latches Multi threading
 - Java library provides lots of thread-safe classes to work with multi thread environment, `CountdownLatch` is one such class.
  
 -  `CountdownLatch` - let us countdown based on the value passed.
 - One or more thread can count down the latch and when it is equal to 0, then one or more thread waiting over the latch can proceed (or start processing)
 
 - `CountdownLatch` - is used to make sure that a task waits for other threads before it starts. To understand its application, let us consider a server where the main task can only start when all the required services have started.

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
  
#### Another example
 -once the 4 thread is completed the process starts after await()
```java

// Java Program to demonstrate how
// to use CountDownLatch, Its used
// when a thread needs to wait for other
// threads before starting its work.
import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo
{
	public static void main(String args[])
				throws InterruptedException
	{
		// Let us create task that is going to
		// wait for four threads before it starts
		CountDownLatch latch = new CountDownLatch(4);

		// Let us create four worker
		// threads and start them.
		Worker first = new Worker(1000, latch,
								"WORKER-1");
		Worker second = new Worker(2000, latch,
								"WORKER-2");
		Worker third = new Worker(3000, latch,
								"WORKER-3");
		Worker fourth = new Worker(4000, latch,
								"WORKER-4");
		first.start();
		second.start();
		third.start();
		fourth.start();

		// The main task waits for four threads
		latch.await();

		// Main thread has started
		System.out.println(Thread.currentThread().getName() +
						" has finished");
	}
}

// A class to represent threads for which
// the main thread waits.
class Worker extends Thread
{
	private int delay;
	private CountDownLatch latch;

	public Worker(int delay, CountDownLatch latch,
									String name)
	{
		super(name);
		this.delay = delay;
		this.latch = latch;
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep(delay);
			latch.countDown();
			System.out.println(Thread.currentThread().getName()
							+ " finished");
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

```
