### JMX (Java Management Extension)
 - We can manage Java applications remotely by using the JMX API, using remote management agent such as JConsole.
 - JMX provides a standard way of dynamically monitoring and managing application resources.
 - Using JMX MBean server we can update the variable during runtime.

Applications like Apache Cassandra, Apache Artemis provides JMX service to monitor the process using JMX.
Refer my Stackoverflow for a implemention to monitor the queue count from Apache Artemis using JMX
https://stackoverflow.com/questions/63162424/activemq-artemis-and-logstash-jmx-input

Below is an example of how to configure an applicaiton using JMX.
 - The Monitor bean, will return the duration from the start of the process in seconds.
![image](https://user-images.githubusercontent.com/6425536/158005143-c0cdfaf3-d160-41c9-9765-8b14732c94da.png)

 - We update a counter varliable, below snapshot 
![image](https://user-images.githubusercontent.com/6425536/158005163-536d1ec3-39ac-4117-9ade-93b6efe3f090.png)
- After updating the counter attribute is set with user provided value. In this case 12.
![image](https://user-images.githubusercontent.com/6425536/158005177-ca8e5729-b1a7-4bac-a75b-2c9ae9c9357b.png)

Upon invoking the externalStopFlag with true, the while loop will be stopped. 
 Note: only when print that externalFlag value using system.out.println() was consistently able to shutdown on update.
![image](https://user-images.githubusercontent.com/6425536/158005237-117b93a9-c7a4-4668-8e68-09617402064e.png)

- Start the application with below JVM arguments
```
-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999  -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

- Open Jconsole using `jconsole` command, if the Java process is running in the local VM, then the process id will be listed when selecting the connection.
- If we use the above JVM argument to start the process, we can use `localhost:9090` to connect.
![image](https://user-images.githubusercontent.com/6425536/158005344-6ada0383-ceeb-4c4c-aa16-2cbcea0c6588.png)

If we want to connect using JMX client code below is how the same code for client looks

```java
JMXServiceURL url = 
  new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server"); 
JMXConnectorServer cs = 
  JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs); 
cs.start(); 
//wait for some duration
cs.stop();
```

- Lets define an MBean bean, based on the design the bean should be named with MBean.
```java
package jmxdemo.jmxdemo;

public interface MonitorMBean {

	String getProcessDuration();
	void exteralStopFlag(boolean stopFlag);
	void externalCounter(int i);
	int getCounter();
	boolean getStopFlag();
}
```
- Implement the MBean which will can be updated remotely.

```java
package jmxdemo.jmxdemo;

public class Monitor implements MonitorMBean{

	static long startUpTime;
	static {
	 startUpTime = System.currentTimeMillis();	
	}
	
	boolean stopFlag =false;
	int externalInput=0;
	
	@Override
	public String getProcessDuration() {
		long currentTime = System.currentTimeMillis();
  	return (currentTime-startUpTime)/(60*60) +" seconds";
	}

	@Override
	public boolean getStopFlag() {
		return stopFlag;
	}

	@Override
	public void exteralStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	@Override
	public void externalCounter(int i) {
		externalInput +=i;
	}
	
	@Override
	public int getCounter() {
		return this.externalInput;
	}
	
	public boolean checkStopFlag() {
		return this.stopFlag;
	}
}
```

- Helper class to register the bean to MBserver
```java
package jmxdemo.jmxdemo;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class Helper {

	void createAndRegisterInJMX(Monitor monitor){
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName objectName = new ObjectName("jmxdemo.jmxdemo:type=monitor");
			mBeanServer.registerMBean(monitor, objectName);
		}
		catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
			System.err.println("Error occurred during registring MBean");
			e.printStackTrace();
		}
	}
}
```

- Main class

```java
package jmxdemo.jmxdemo;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class App {

	public static void main(String[] args) {
		Monitor monitor = new Monitor();
		Helper helper = new Helper();
		helper.createAndRegisterInJMX(monitor);
		boolean stopProcess = false;
		System.out.println("Process started...");
		boolean status = false;;
		while(!stopProcess) {
			status = monitor.checkStopFlag();

   //Note: Below loop is based on the idea to stop the process when the variable is updated from Jconsole 
   // But for some reason, when we didn't use the system.out within this block, the loop is not breaking.
   // When system.out starts printing either the value from the checkStopFlag() directly or the status
   // upon updating the value in the Jconsole the process stops
   System.out.println(status);
			if(status||stopProcess) {
				System.out.println("external input");
				stopProcess=true;
			}else{
				stopProcess=false;
			};
		}
		System.out.println("shutting down..");
	}
}
```

