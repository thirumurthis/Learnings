#### Using `multiple locks` using synchronized code blocks


  - When the below code with any lock or synchronized block is executed multiple times, the output will not be consistent.
  - the list count will be different for each run 
  
```java
public class Worker{

   private Random rand = new Random();
   
   private List<Integer> list1 = new ArrayList<>();
   
   private List<Integer> list2 = new ArrayList<>();
   
   public void stageOne(){
      //uses list1 
      try{
       Thread.sleep(1);
       }catch(InterruptedException e){
         e.printStackTrace();
       }
       
       list1.add(rand.nextInt(100));
       
   }
   
   public void stageTwo(){
     //uses list2 
      try{
       Thread.sleep(1);
       }catch(InterruptedException e){
         e.printStackTrace();
       }
       
       list2.add(rand.nextInt(100));
   }
   
    public void process(){
      for (int i=0; i<1000; i++){
         stageOne();
         stageTwo();
      }
    
    }

  // this is not the psvm of the class
  // custom main () method
  public void main(){
     System.out.println("starting.."); 
     long start = System.currentTimeMilis();
     
     //call process - with this if execute the code (without any thread) then the process will take approximately  2 seconds(since we are sleeping 1 ms )
     // process();
     
     // Say we want to perform this process concurrently we can use threads
     // with this thread it will take 2 seconds.
     
     Thread t1 = new Thread(new Runnable(){
       @Override
       public void run(){
         process();
       }
     });
      t1.start();
      // the main thread should wait for t1 to finish
      
           Thread t1 = new Thread(new Runnable(){
       @Override
       public void run(){
         process();
       }
     });
      t2.start();
      
      try{
      t1.join();
      t2.join();
      }catch(InterruptedException e){
        e.printStackTrace();
      }
     
     
     long end = System.currentTimeMilis();
     
     System.out.println("complete " + (end-start));
     System.out.println("list1 "+list1.size() +" list2: "+ list2.size());
     
   }
}

// different class file.
public class MainApp{

  public static void main(String args[]){
    
    Worker worker = new Worker();
     worker.main();
  }

}
```
 -  Options to address the above issue is to add synchronize method
   - Option 1:
      - Make the stageOne() and stageTwo() as synchronized block 
      - adding synchronized block on the methods will make the process to run 4 seconds
      - With this apporach the time take is doubled.
      - This is because the object acquires intrensic lock and only one thread can access it at one time.
      - the output will be consistent
      
```java
public class Worker{

   private Random rand = new Random();
   
   private List<Integer> list1 = new ArrayList<>();
   
   private List<Integer> list2 = new ArrayList<>();
   
   public synchronized void stageOne(){
      //uses list1 
      try{
       Thread.sleep(1);
       }catch(InterruptedException e){
         e.printStackTrace();
       }
       
       list1.add(rand.nextInt(100));
       
   }
   
   public synchronized void stageTwo(){
     //uses list2 
      try{
       Thread.sleep(1);
       }catch(InterruptedException e){
         e.printStackTrace();
       }
       
       list2.add(rand.nextInt(100));
   }
   
    public void process(){
      for (int i=0; i<1000; i++){
         stageOne();
         stageTwo();
      }
    
    }

  // this is not the psvm of the class
  // custom main () method
  public void main(){
     System.out.println("starting.."); 
     long start = System.currentTimeMilis();
     
     //call process - with this if execute the code (without any thread) then the process will take approximately  2 seconds(since we are sleeping 1 ms )
     // process();
     
     // Say we want to perform this process concurrently we can use threads
     // with this thread it will take 2 seconds.
     
     Thread t1 = new Thread(new Runnable(){
       @Override
       public void run(){
         process();
       }
     });
      t1.start();
      // the main thread should wait for t1 to finish
      
           Thread t1 = new Thread(new Runnable(){
       @Override
       public void run(){
         process();
       }
     });
      t2.start();
      
      try{
      t1.join();
      t2.join();
      }catch(InterruptedException e){
        e.printStackTrace();
      }
     
     
     long end = System.currentTimeMilis();
     
     System.out.println("complete " + (end-start));
     System.out.println("list1 "+list1.size() +" list2: "+ list2.size());
     
   }
}

// different class file.
public class MainApp{

  public static void main(String args[]){
    
    Worker worker = new Worker();
     worker.main();
  }
}
```
  - Option 2:
    - use multiple locks by creating different object
    - what we need is since stageOne() and stageTwo() are not writing to the same variable, we can make each method to run parallel which can done by creating locks.
    - create object, and use that in the synchronized block for each code block.
    - so now the thread has locks on different block using different object both can run concurrently.
    - no two thread can run the both stageOne() and stageTwo() at the same time. so in this case the time take 2 second.
    - It is a good practice to use separate locks. We can lock the List variable itself which is not recommended.
    
```java
public class Worker{

   //ojbect to create lock
   
   private Object lock1 = new Object();
   private Object lock2 = new Object();
   

   private Random rand = new Random();
   
   private List<Integer> list1 = new ArrayList<>();
   
   private List<Integer> list2 = new ArrayList<>();
   
   public void stageOne(){
    // we create synchronized block
    // using the lock1 object
    synchronized(lock1){
      //uses list1 
      try{
       Thread.sleep(1);
       }catch(InterruptedException e){
         e.printStackTrace();
       }
       
       list1.add(rand.nextInt(100));
     }
   }
   
   public void stageTwo(){
    // we create synchronized block
    // using the lock2 object
    synchronized(lock2){

     //uses list2 
      try{
       Thread.sleep(1);
       }catch(InterruptedException e){
         e.printStackTrace();
       }
       
       list2.add(rand.nextInt(100));
     }
   }
   
    public void process(){
      for (int i=0; i<1000; i++){
         stageOne();
         stageTwo();
      }
    
    }

  // this is not the psvm of the class
  // custom main () method
  public void main(){
     System.out.println("starting.."); 
     long start = System.currentTimeMilis();
     
     //call process - with this if execute the code (without any thread) then the process will take approximately  2 seconds(since we are sleeping 1 ms )
     // process();
     
     // Say we want to perform this process concurrently we can use threads
     // with this thread it will take 2 seconds.
     
     Thread t1 = new Thread(new Runnable(){
       @Override
       public void run(){
         process();
       }
     });
      t1.start();
      // the main thread should wait for t1 to finish
      
           Thread t1 = new Thread(new Runnable(){
       @Override
       public void run(){
         process();
       }
     });
      t2.start();
      
      try{
      t1.join();
      t2.join();
      }catch(InterruptedException e){
        e.printStackTrace();
      }
     
     
     long end = System.currentTimeMilis();
     
     System.out.println("complete " + (end-start));
     System.out.println("list1 "+list1.size() +" list2: "+ list2.size());
     
   }
}

// different class file.
public class MainApp{

  public static void main(String args[]){
    
    Worker worker = new Worker();
     worker.main();
  }
}
```
