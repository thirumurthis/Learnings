#### how to create a Thread in java 
- Basic methods
  1. Extending the Thread class
  2. Implementing Runnable interface
- Other options
  3. ThreadPool service

```java

class Runner extends Thread {
   public void run(){
     for(int i=0; i<10; i++){
       System.out.println("Hello "+i);
       
       try{
         Thread.sleep(100); // sleep for 100 ms
       }catch (InterruptedException e){
         e.printStackTrace();
       }
     }
   }
 }

public class MainApp{

    Runner thread1 = new Runner();
    thread1.start(); // note we shouldn't call the run, since if do so the application runs in the main thread of the application
    // calling the start() of the thread class we are running as seprate thread
    
    Runner thread2 = new Runner();
    thread2.start(); // if we execute this class we will see two thread running concurrently.
}
```

- Creating the thread using the Runnable interface.

```java

class Runner implements Runnable{

   public void run(){
     for(int i=0; i<10; i++){
       System.out.println("Hello "+i);
       
       try{
         Thread.sleep(100); // sleep for 100 ms
       }catch (InterruptedException e){
         e.printStackTrace();
       }
     }
   }
}

public class MainApp{
   public static void main(String .. args){
      Thread t1 = new Thread (new Runner());
      Thread t2 = new Thread(new Runner());
      
      t1.start();
      t2.start();
   }
}
```

- Creating thread using anonymous class

```java
public class MainApp{

 public static void main (String ... args){
    Thread t1 = new Thread (new Runnable(){
     
      @Override
      public void run(){
       for(int i=0; i<10; i++){
       System.out.println("Hello "+i);
       
       try{
         Thread.sleep(100); // sleep for 100 ms
       }catch (InterruptedException e){
         e.printStackTrace();
       }
     }
    }) //override method ending
  }
  
  t1.start();
}
```
