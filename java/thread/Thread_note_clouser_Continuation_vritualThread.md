
Parallel - performing multiple task in parallel. 
  Different task continue to evolve at same time, but at any one point of time (or slice of instance) there are more than one task is being done.

Concurrent - 
  Given certain amount of time there might be more than one task will running concurrently and in parallel, but at any given point of time ( or slice of intstance) only one task is done. 
  
Asynchronous - 
  - javascript is Asynchronous.
  - non-blocking, which utlizies the resource better way.
  - in C++ the multi threading is not same for all platform, like Windows, Linux, etc libraries are different. Java has a standard libraries

CGI - The sytem will start an process when a request comes in, most case starting a process is expensive. Since the resource usage is high, we tried to use the threads.

How many threads can we create?
  - Creating more than one thread might crash, based on the memory or cpu.
  
Number of threads selection or to choose it formulae
 
```
                         # of cores
Number of threads =  -------------------
                        1- blocking factor

- blocking factor = is the amount of time a thread is spent being blocked without using the CPU (mostly for IO operation)
  0 <= blocking factor <1 

1.  If the task or process are computation instensive ( i.e CPU intense) then 
# of threads <= # of cores

2. If the task or process is IO intensive 
   - if the blocking factor is 0.5, # of threads ~ 2 * # of cores 
   - if the blocking factor is 0.9, # of threads ~ 10 * # of cores
```


**Subroutines**: - 
  - Is a function call and we get back a response.
  
**Cooroutines** :-
  - It is co-operating routines, where two routines running at the same time and communicating to each other.
  - Its like normal conversation, where we talk to each other responding by yeilding.
  
  - How does the co-routine remembers the data if the threads could be switched around?
     - We have a datastucture called `continuation`, this can remember the conversational state (or previous call) and conitue from where it left off.
     - Continuation are datastucture useful to carry the converstational state between the coroutines.

```java
package com.parallel;

import java.util.function.Function;

public class DemoThread {

    public static Function<Integer, Integer> generate(int n) {
        // add the try-finally to print the flow status
        try {
            int tmp = n * 10;
            return item -> item + tmp;
        } finally {
            System.out.println("end of function execution.");
        }
    }

    public static void main(String[] args) {
        // with the below flow will print the finally and result
        var func = generate(9);
        System.out.println("Call from main");
        System.out.println(func.apply(9));
    }
}
## Note the output, where the function call is done first
  - this is because of the closures.
## output  
end of function execution.
Call from main
99
```

- Closure, captures the lexical scope (immutable state) and carries it over.
- Closure logically is like a datastructure that carries it over.
- `Continuation` are like closure, which can carry state from one thread to another thread.

- We never use the continuation in the code, the library use it. This is where the project loom is using it.
- Continuation says, the thread state can be parked and then come back and resume.
this is called virtual threads in java.
- Virtual therad is super light wieght, these doesn't take memory. 
- Virtual threads are created by the JVM not by the Operating system. 
- We can create many many virtual therads, more like wait and resume.
- In traditional threads when it is per task per thread model, where if the task is blocked the thread is not used.
- In vitrtual threads, when the task is blocked the thread is used to perform other task. When the task is ready to run, the thread can pick it up and resume.
- In virtual threads the task is going to wait not the underlying threads.

```
package com.parallel;

public class LoomThread {

    public static void main(String[] args) {
        int THREADMAX = 1000;
        for(int i =0;i< THREADMAX; i++){
            Thread.startVirtualThread(LoomThread::calcuate);
        }
    }

    public static void calculate(){
        try{Thread.sleep(5000);}catch (Exception e){}
    }
}
```

- Note: For virutal threads we don't need any Threadpool to be configured.
- uses ForkJoinPool
