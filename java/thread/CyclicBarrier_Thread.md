
- like countdownlatch, we can use cyclic barrier. Only advantage is the cyclic barrier is we can reuse the barrier.
- When calling the await(), the thread will wait till the number of parties intializied in the barrier is reached.

```java
package com.parallel;

import lombok.RequiredArgsConstructor;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierExample{

    //When number of parties/participants or threads wants to wait for
    //common point, this is similar to count down latch
    //In Countdownlatch each thread calls countdown, in CyclicBarrier the await()
    //method is called.
    // when the very last thread calls await() method, it signals that it has
    //reached the barrier and starts processing other thread.
    // In count down latch we can't reuse the countdownlatch once the count is reached to 0

    public static void main(String[] args) throws InterruptedException{

        CyclicBarrier cyclicBarrier = new CyclicBarrier(5);

        //int delay = 1000;
        SimpleThread t1 = new SimpleThread(cyclicBarrier,"t1");
        SimpleThread t2 = new SimpleThread(cyclicBarrier,"t2");
        SimpleThread t3 = new SimpleThread(cyclicBarrier,"t3");
        SimpleThread t4 = new SimpleThread(cyclicBarrier,"t4");
        SimpleThread t5 = new SimpleThread(cyclicBarrier,"t5");

        SimpleThread t01 = new SimpleThread(cyclicBarrier,"t01");
        SimpleThread t02 = new SimpleThread(cyclicBarrier,"t02");
        SimpleThread t03 = new SimpleThread(cyclicBarrier,"t03");
        SimpleThread t04 = new SimpleThread(cyclicBarrier,"t04");
        SimpleThread t05 = new SimpleThread(cyclicBarrier,"t05");

        System.out.println("Main thread started .. "+Thread.currentThread().getName());

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        //Thread.sleep(20000);
        t01.start();
        t02.start();
        t03.start();
        t04.start();
        t05.start();

        Thread.sleep(2000);// just waiting momentarily for other thread to complete
        //without above sleep, the main tread will be printing the below message at the start
        // this will not terminate the other threads
        System.out.println("Main thread completed .. "+Thread.currentThread().getName());

    }
}

class SimpleThread extends Thread{
    private final CyclicBarrier cyclicBarrier;
    private final String threadName;

    public  SimpleThread( CyclicBarrier cyclicBarrier, String threadName){
        super(threadName);
        this.threadName=threadName;
        this.cyclicBarrier=cyclicBarrier;
    }

    @Override
    public void run() {
        try{
            Random num = new Random();
            Thread.sleep(num.nextInt(1000,5000));
            // can perform any task before calling await
            System.out.println("current thread "+threadName);
            // The thread will wait till the cyclic barrier parties are reached
            // await will automatically decrement the initial count
            int parties = cyclicBarrier.await();

            if( parties == 0) {
                System.out.println("----------------\nWill be able to handle more thread..." + parties);
                Thread.sleep(10000);
            }else{
                System.out.println("still able to process few more threads "+parties);
            }
        }catch(InterruptedException | BrokenBarrierException e){
            e.printStackTrace();
        }
    }
}

/* OUTPUT - not a consistent output every time this varies

Main thread started .. main
current thread t2
current thread t03
current thread t01
Main thread completed .. main
current thread t5
current thread t3
----------------
Will be able to handle more thread...0
still able to process few more threads 4
still able to process few more threads 1
still able to process few more threads 3
still able to process few more threads 2
current thread t1
current thread t05
current thread t4
current thread t02
current thread t04
----------------
Will be able to handle more thread...0
still able to process few more threads 4
still able to process few more threads 2
still able to process few more threads 3
still able to process few more threads 1
*/
```

- using runnable and thread pool executor
- 
```java
package com.parallel;


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarrierThreadExample {

    public static void main(String[] args) throws InterruptedException{

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CyclicBarrier barrier = new CyclicBarrier(3);
        executor.submit(new Play("t1",barrier));
        executor.submit(new Play("t2",barrier));
        executor.submit(new Play("t3",barrier));
        Thread.sleep(3000);
    }
}

class Play implements Runnable {
    private CyclicBarrier barrier;
    private String threadName;

    public Play(String threadName, CyclicBarrier barrier) {
        this.barrier = barrier;
        this.threadName = threadName;
    }

    @Override
    public void run(){
        while (true){
            try{
                //any three threads can arrive at this point
                // once those three thread is reached, the main thread
                // will be resumed.
                System.out.println(" count "+barrier.await());

            }catch(InterruptedException|BrokenBarrierException e){
                e.printStackTrace();
            }
        }
    }
}

```
