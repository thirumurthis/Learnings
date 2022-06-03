#### Producer-Consumer
  - Blocking queues 
  - no synchronize keyword used,
```java
import java.util.concurrent.ArrayBlockingQueue;


public class MainApp{
   //below is a data strcuture, to which we can add the item and remove FIFO order
   //These are thread-safe
   private static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10); //max size


    public static void main(String args[]){
       Thread t1 = new Thread(new Runnable(){
         try{
            producer();
         }catch(InterruptedException e){
            e.printStackTrace();
          }
       });
       
       Thread t2 = new Thread(new Runnable(){
         try{
            consumer();
         }catch(InterruptedException e){
            e.printStackTrace();
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
    
    //produce things or data
    private static void producer()throws InterruptedException{
       Random rand = new Random();
       
       //infinte loop, to generate random int and push to queue
       while(true){
         //throws InterruptedException
         // since 10 item is configured, only 10 items can be added and till the queue is less than the 10 it will add items.
         //put waits till that size is reduced.
         queue.put(rand.nextInt(100));
       }
    }
    
     private static void consumer() throws InterruptedException{
     
       //consumes the message from the queue
       
       // using to fetch the random fashion
       Random rand = new Random();
       while(true){
         Thread.sleep(100);
         
         if(rand.nextInt(10)==0){
           // if there is no item in the queue take() will wait until some item is added to the queue
           // while waiting it doesn't consume too many resources
           
           Integer value = queue.take();
           System.out.println("consumed value " + value+ " queue size: "+ queue.size());
         }
       }
     }
}
```
