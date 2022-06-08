Pre-requisites:
  - Basic understanding of Zookeeper and its Data model.
  
In this blog, we create  we will be using single node Zookeeper running in Docker Desktop.

#### Server setup using Docker

If the docker is installed, use below the command to run the single node ZooKeeper.

 ```
$ docker run --name zookeeper-srv1 --restart always -d -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080 zookeeper 
 ```
  - `-p` port is to exposing the container port, so server can be connected using java client which we will be seeing shortly.

#### Zookeeper CLI using from Docker
 
Lets now create a Zookeeper CLI using Docker instance, we can use the below command to connect to the above single node server.

```
$ docker run -it --rm --link zookeeper-srv1:zookeeper zookeeper zkCli.sh -server zookeeper
```

- Client code is example of how to we can perform distributed synchronized lock based processing.
    - The business logic is mocked using `Thread.sleep()`.
    - The method `DistributedLock.java` class lock() method using the `getChildrens()` method of Zookeeper java client.
          - The lock() method will use double synchronized block and use `Watch` events to lock until the lock is released.
 
#### Representation of flow

Below diagram depicts the flow:

 ![image](https://user-images.githubusercontent.com/6425536/172683653-e66ec7e0-04eb-4497-8b04-3ef16af22336.png)

Few other use cases refer [Zookeeper documentation](https://zookeeper.apache.org/doc/current/recipes.html)

#### Java client code

 ```java
 package com.demo;

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
package com.demo;

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

#### Brief overview of logic

  - In this case, executed the Java code as two process

     - The process-1 creates the znode path, Zookeeper will add a sequential string to the znode path ending with 001.
     - The process-2 creates the znode path, in this case Zookeeper will create the znode path ending with 002.
      - Both process-1 and process-2 creates the znode, since the Zookeeper server includes sequential number to the znode path (as we used EPHMERAL_SQUENTIAL option to create the znode).
      - In the client code we use the synchronized block, the `getChildren()` method gives list of children's  under the /locknode path. 
      - Sort the znode name children's list, if the very first znode name matches to that of znode created by that the process then perform the business logic. 
     - Once the business process is completed the znode is deleted.
     - When the znode is deleted this triggers the Watch event, and the process-2 will perfrom the above logic and start process further.
  
 
#### Reference

This article is also inspired by the [blog](https://dzone.com/articles/distributed-lock-using)

Few Notes, from the above link:

- The node created is EPHEMERAL which means if our process dies for some reason, its lock or request for the lock with automatically disappear thanks to ZooKeeper's node management, so we do not have worry about timing out nodes or cleaning up stale nodes.
- The nested synchronization structure is used to ensure that the DistributedLock is able to process every update it gets from ZooKeeper and does not "lose" an update if two or more updates come from ZooKeeper in quick succession.
-  Since the Watcher callback is in a synchronized block keyed to the same Java lock object as the outer synchronized block, it means that the update from ZooKeeper cannot be processed until the contents of the outer synchronized block is finished.
      - When an update comes in from ZooKeeper, it fires a notifyAll() which wakes up the loop in the lock() method. That lock method gets the updated children and sets a new Watcher. 
