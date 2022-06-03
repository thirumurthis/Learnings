#### Dead lock - 

 - this can be avoided using TryReenterantLock.

```java

public class Runner {

private Account acc1 = new Account();
private Account acc2 = new Account();
//total balance to be 20000
// without any locking then the total balance won't be 20000 

private Lock lock1 = new ReenterantLock();
private Lock lock2 = new ReenterantLock();

  public void firstThread() throws InterruptedException{

   Random ran = new Random();
   
   for (int i=0; i< 10000;i++){
   
      lock1.lock();
      lock2.lock();
      try{
      Account.transfer(acc1,acc2,ran.nextInt(100));
      }finally{
        lock1.unlock();
        lock2.unlock();
      }
   }
  }
  
  public void secondThread() throws InterruptedException{
  
     Random ran = new Random();
   
   for (int i=0; i< 10000;i++){
      lock1.lock();
      lock2.lock();
      try{
       Account.transfer(acc1,acc2,ran.nextInt(100));
       } finally{
           lock1.unlock();
           lock2.unlock();
       }
     }
  }
  
  public void finished(){
     System.out.println("acc1 balance is "+ acc1.getBalance());
     System.out.println("acc2 balance is "+ acc2.getBalance());
     System.out.println("total balance is "+ (acc1.getBalance()+acc2.getBalance());
  }
}

class Account{

private int balance = 10000;

public void deposit(int amount){
 balance += amount;
 }
 
public void withdraw(int amount ){
  balance -= amount;
}

public int getBalance(){
  return balance;
}

public static void transfer(Account ac1, Account ac2, int amount){
   ac1.withdraw(amount);
   ac2.deposit(amount);
}

}
// separate file
public class MainApp(){

  public static void main(String args[]){
  
  final Runnter processor = new Processor();
  Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.firstThread();
       }catch(InterruptedException e){
         e.printStackTrace();
       }
     }
  });
  
    Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.secondThread();
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
    
    process.finished();
  }
}
```
- when using the ReenterantLock the order of lock acquired is mandatory, else we will enter to a dead lock situation.
  - for example in the above, in the second thread, if we first issue lock2.lock() and then lock1.lock(), and the unlock is in different order, this will be end up in dead lock.
 
- Is there a way to lock in any order and unlock the lock of ReenterantLock in any order. so the dead lock can not happen.

- Dead lock can happen with nested synchronized block as well


solution:
  - 1. always lock the lock in the same order and unlock the lock in the same order.
  - 2. some kind of method in any order and don't cause any dead lock using `tryLock()` (refer below code). This way there won't be any dead lock at all.
  
```java

public class Runner {

private Account acc1 = new Account();
private Account acc2 = new Account();
//total balance to be 20000
// without any locking then the total balance won't be 20000 

private Lock lock1 = new ReenterantLock();
private Lock lock2 = new ReenterantLock();

private vod acquireLocks(Lock firstlock, Lock secondLock) throws InterruptedException{
  while(true){
     // acquire locks 
     boolean getFirstLock = false;
     boolean getSecondLock =false;
     try{
     // tryLock will return true immediatly if the lock is acquired
        getFirstLock = firstLocl.tryLock();
        getSecondLock = secondLock.tryLock();
     }finally{
     //if both the lock has acquired lock we return simply
       if(getFirstLock && getSecondLock){
         return ;
       }
       // if the firstLock is already locked we unlock them
       if(getFirstLock){
          firstLock.unlock();
       }
       // if the secondLock is already locked we unlock them
       if(getSecondLock){
          secondLock.unlock();
       }
     }
     // once locks not acquired
     Thread.sleep(1);
  
  }

}

  public void firstThread() throws InterruptedException{

   Random ran = new Random();
   
   for (int i=0; i< 10000;i++){
   
      acquireLocks(lock1, lock2);

      try{
      Account.transfer(acc1,acc2,ran.nextInt(100));
      }finally{
        lock1.unlock();
        lock2.unlock();
      }
   }
  }
  
  public void secondThread() throws InterruptedException{
  
     Random ran = new Random();
   
   for (int i=0; i< 10000;i++){
      acquireLocks(lock2,lock1);
      try{
       Account.transfer(acc1,acc2,ran.nextInt(100));
       } finally{
           lock1.unlock();
           lock2.unlock();
       }
     }
  }
  
  public void finished(){
     System.out.println("acc1 balance is "+ acc1.getBalance());
     System.out.println("acc2 balance is "+ acc2.getBalance());
     System.out.println("total balance is "+ (acc1.getBalance()+acc2.getBalance());
  }
}

class Account{

private int balance = 10000;

public void deposit(int amount){
 balance += amount;
 }
 
public void withdraw(int amount ){
  balance -= amount;
}

public int getBalance(){
  return balance;
}

public static void transfer(Account ac1, Account ac2, int amount){
   ac1.withdraw(amount);
   ac2.deposit(amount);
}

}
// separate file
public class MainApp(){

  public static void main(String args[]){
  
  final Runnter processor = new Processor();
  Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.firstThread();
       }catch(InterruptedException e){
         e.printStackTrace();
       }
     }
  });
  
    Thread t1 = new Thread(new Runnable(){
     @Override
     public void run(){
       try{
          processor.secondThread();
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
    
    process.finished();
  }
}
```
