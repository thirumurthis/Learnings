Adder and Accumulator:

- Say for an use case where we need to keep track of number of task or thread is being used for a process. In this case we can use AtomicLong,AtomicInteger, etc.
- The issue with the AtomicInteger, etc. is at the Core level the local cache is flushed often when there are many threads using that variable.
- To overcome the contention issue we can use `LongAdder`. In this case each thread will have a local variable and increment its localvariable. There won't be any contention. At the end we use sum (instead of AtomicLong.get()). The sum operation will be the only synchoronous operation. The throughput in LongAdder is high.
- Accumulator is more like the adder, we use LongAccumulater
```
LongAccumulater counter  = new LongAccumulater((x,y)-> x+y,0);
// takes a lambda, more like reduce function
// if we need to add we use counter.accumulate(1);
// we use the .get() method to fetch the value.
```
when to use Accumulator or adder:
- Only applied to a stateless function.
- Best suited for write heavy operation.
- Order of the Accumlate operation is not gauranted.
- when ever to perform counter like 

