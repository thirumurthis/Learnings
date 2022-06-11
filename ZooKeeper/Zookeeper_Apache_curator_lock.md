- Distributed lock using Apache Curator client for Apache Zookeeper

 In this example we will see how to create distributed lock using Apache curator recipes. 
 
 - We can setup development environment using in docker using this [blog](https://thirumurthi.hashnode.dev/apache-zookeeper-creating-server-ensemble-using-docker-desktop)

#### Java Code using Apache Curator framework

 ```java
 package com.artemis.demo;

import java.util.Date;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryNTimes;

public class DistributedLockWithCuratorRecipe {

	
	 public static void main(String[] args) {
		
		 RetryPolicy retryPolicy = new RetryNTimes(3, 100);
		 CuratorFramework cf = CuratorFrameworkFactory.newClient("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183",
				  retryPolicy);
                  //alternate to retry
				  // new RetryForever(2000));
		 cf.start();
		 try {
			cf.blockUntilConnected();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		 InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(cf, "/curator/node");
		 
		 try {
			mutex.acquire(); // throws exception
			
			// Perform the business logic - mocking using Thread sleep
			System.err.print("D1 is acquired the lock");
			Date dt = new Date();
			System.out.println(dt.toString());
			Thread.sleep(30000);
			mutex.release();	
			System.err.print("D1 is released the lock");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			cf.close();
		}
	}
}
```

#### Adding maven dependency

```xml
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.8.0</version>
</dependency>

<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>5.2.1</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>5.2.1</version>
</dependency>
```

#### Execution
  - If the above java class is executed as multiple process, we could see that when one process acquired the lock, the next process waits for lock to be released.
  - This code is using the Curator `InterProcessSemaphoreMutex` shared lock, to make distributed process synchronized.
