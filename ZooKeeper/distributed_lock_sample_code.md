Pre-requesites:
  - Basic undestanding of Zookeeper.
  
In this blog, we create  we will be using single node Zookeeper running in Docker Desktop.

If the docker is installed, use below the command to run the single node ZooKeeper.
 ```
$ docker run --name zookeeper-srv1 --restart always -d -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080 zookeeper 
 ```
  - `-p` port is to exposing the container port, so server can be connected using java client which we will be seeing shortly.
 
Lets now create a Zookeeper CLI using Docker instance, we can use the below command to connect to the above single node server.

```
$ docker run -it --rm --link zookeeper-srv1:zookeeper zookeeper zkCli.sh -server zookeeper
```

## Below client code is used to perform distirbuted lock based processing.
 - We will be mocking the business logic using `Thread.sleep()`.
 - We will use lock() using the getChildrens() method of java client.
    - The lock() method will use dobule synchronized block and use Watch events to lock until the lock is released.
 
 
 ![image](https://user-images.githubusercontent.com/6425536/172683653-e66ec7e0-04eb-4497-8b04-3ef16af22336.png)


Few other usecases can be found in [Zookeeper documentation](https://zookeeper.apache.org/doc/current/recipes.html)

 ```java
 package com.artemis.demo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class DistributedLock {
	private final ZooKeeper zk;
	private final String lockBasePath;
	private final String lockName;
	private String lockPath;
	public DistributedLock(ZooKeeper zk, String lockBasePath, String lockName) {
		this.zk = zk;
		this.lockBasePath = lockBasePath;
		this.lockName = lockName;
	}
	public void lock() throws IOException {
		try {
			// lockPath will be different than (lockBasePath + "/" + lockName) because of the sequence number ZooKeeper appends
			lockPath = zk.create(lockBasePath + "/" + lockName, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			final Object lock = new Object();
			synchronized(lock) {
				while(true) {
					List<String> nodes = zk.getChildren(lockBasePath, new Watcher() {
						@Override
						public void process(WatchedEvent event) {
							synchronized (lock) {
								lock.notifyAll();
							}
						}
					});
					Collections.sort(nodes); // ZooKeeper node names can be sorted lexographically
					if (lockPath.endsWith(nodes.get(0))) {
						System.out.println("The lock path ends with the nodes name "+nodes.get(0));
						return;
					} else {
						System.out.println("Process is waiting .. for a while..");
						lock.wait();
					}
				}
			}
		} catch (KeeperException e) {
			throw new IOException (e);
		} catch (InterruptedException e) {
			throw new IOException (e);
		}
	}
	public void unlock() throws IOException {
		try {
			zk.delete(lockPath, -1);
			lockPath = null;
		} catch (KeeperException e) {
			throw new IOException (e);
		} catch (InterruptedException e) {
			throw new IOException (e);
		}
	}
}
```

```java
package com.artemis.demo;

import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZKLockingUsage {

	public static void main(String[] args) throws IOException {
		String client = "processA";
		if(args.length >=1) {
			client = args[0];
		}else {
			System.exit(1);
		}
		String hostPort = "localhost:2181";//for multiple servers use comman seperated values
		String zLockpath = "/locknode";
		int sessionTimeOut = 2000;
		ZKLockingUsage lock = new ZKLockingUsage();
		ZooKeeper zk = lock.connect(hostPort, sessionTimeOut, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				if(event.getType()==Watcher.Event.EventType.None && event.getState() == Watcher.Event.KeeperState.SyncConnected) {
					System.out.printf("\nEvent Received: %s", event.toString());
				}				
			}

		});


		DistributedLock dLock = new DistributedLock(zk,zLockpath,client );
		dLock.lock();
		//Mocking the business process
		try {
			System.out.println("This will be process one which will is working...");
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dLock.unlock();
	}


	public ZooKeeper connect(String hostPort,int timeout, Watcher watchEvent) throws IOException {

		ZooKeeper zk = new ZooKeeper(hostPort, timeout, watchEvent);
		return zk;
	}
}
```
  - The client code can be executed as two process, and we can notice when the first process creates a znode the other process will be in wait state until the Watch event is triggering the event from the Zookeeper server.
  
 
This article is also inspried by the blog https://dzone.com/articles/distributed-lock-using
