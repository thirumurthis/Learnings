- If we start the client code twice,
  - client 1 will elect that client as leader.
  - If the client 2 is running it will wait till the client 1 is stopped.
```java

public class ZookeeperLeaderElecton{

public static void main(String[] str) throws Exception{

  CuratorFramework cr = CuratorFrameworkFactory.newClient("localhost:2181,localhost:2182",2000,5000, new RetryForever(1000));
  
  cf.start();
  //curator will block until connected to server
  cf.blockUnitlConnected();
  LeaderLatch latch = new LeaderLatch(cf,"/node/latch");
  
  LeaderLatchListener latchListener = new LeaderLatchListener(){
    @Override
    public void isLeader(){
       System.out.println("Leader");
    }
    
    @Override
    public void notLeader(){
      System.out.println("NOT Leader");
    }
  }
  
  latch.addListener(latchListener);
  latch.start();
  Thread.sleep(30000);
  latch.close();
  cf.close();
 
 }
}
```
