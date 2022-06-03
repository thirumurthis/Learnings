#### Using Low-level synchronization

```java

pulic class Processor{

 // Shared data variable between the thread
 
 private LinkedList<Integer> list = new LinkedList<Integer>();
 
 //limit the list with specific value
 private final int LIMIT = 10;
 
 //using internsic lock using object
 private Object lock = new Object();
 
// t1 runs this method
//producer add item to store
  public void produce() throws InterruptedException{
    int value =0;
    while(true){
    
    synchronized(lock){
    
     // check the size 
     
     while(list.size() == LIMIT){
        lock.wait(); // we need to apply wait on the object
      }
       // incrementing the value.
         list.add(value++); 
         
         //we notify() the object to wake
         lock.notify();
      }
    }
   }
  }
 
// t2 runs this method 
// consumer removed item from the store
  public void consume() throws InterruptedException{
    
    Random rand = new Random();
    while(true){
    synchronized(lock){
    
     while(list.size() == 0){
        lock.wait();
     }

      System.out.println("list size"+list.size());
      //more like queue FIFO, remove first item
      int val = list.removeFirst();
      System.out.println("removed value "+value);
      lock.notify();
       }
       // the while loop will pause for random amount time
       Thread.sleep(rand.nextInt(1000));
     }
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
 - Executing the above code the limit of the list will be within the 10 and process it.
 - use the object intrensic lock. Object.wait(); else using wait() directly will through error
