### JMX (Java Management Extension)

 - JMX is used to manage Java applications remotely by using the JMX API, with remote management agent like JConsole.

 - JMX provides a standard way of dynamically monitoring and managing application resources.

 - Using JMX MBean server we can also update the values of the variable during runtime.

The Java code at the bottom will demonstrate how to update the variable during runtime, using JConsole with help of JMX MBean server.

### Use Case of JMX
Applications like Apache Cassandra, Apache Artemis provides JMX service for management and monitoring the process.

Refer my [Stackoverflow link](https://stackoverflow.com/questions/63162424/activemq-artemis-and-logstash-jmx-input) for a implementing monitor logic for queue count from Apache Artemis using JMX and Elastic Search.

The Java code below is an example of how to configure an application with JMX MBean server.

 - In the example the Monitor java bean, will return the duration from when the JVM started in seconds.

![image](https://user-images.githubusercontent.com/6425536/158005143-c0cdfaf3-d160-41c9-9765-8b14732c94da.png)

- We can update the Counter variable using operations of JConsole like in below snapshot. By providing a value in the text box and clicking the method name will update the variable in runtime.

![image](https://user-images.githubusercontent.com/6425536/158005504-0cf2c02e-aa61-478c-94fa-095d9f872e09.png)

- Once updated the counter variable will be updated refer Attribute section. In this case 12 was provided as input and updated as 12.

![image](https://user-images.githubusercontent.com/6425536/158005177-ca8e5729-b1a7-4bac-a75b-2c9ae9c9357b.png)

Upon invoking the `externalStopFlag` with value of true, the while loop will be stop so as the program will stop.
 
 Note: In the below code, only when using system.out.println to print that externalFlag from the monitor class I observed consistent shutdowns using JConsole.
 
![image](https://user-images.githubusercontent.com/6425536/158005237-117b93a9-c7a4-4668-8e68-09617402064e.png)

- In order to enable the JMX, we have to start the Java application with below JVM arguments

```
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false
```

Note:
  - When using `-Dcom.sun.management.jmxremote.port=0`, per openjdk documentation it is an undocumented feature which will cause the jvm to ask the OS to assign an ephemeral port. This will guarantee an unused port.

- Open Jconsole using `jconsole` command, if the Java process is running in the local VM, then the process id will be listed when selecting the connection.
- If we use the above JVM argument to start the process, we can use `localhost:9090` to connect.

Note: Using jconsole to connect services remotely we can use any of the one options and URL looks like,   
 - `service:jmx:rmi:///jndi/rmi://<host-name>:<port>/server`  - for Apache Artemis 
 - `service:jmx:rmi:///jndi/rmi://<host-name>:<port>/jmxrmi` - for Cassandra (if the node has a username password use that to connect using JConsole)

```
Additional tip:
  - In case of Cassandra, under Mbeans we can expand 
  org.apache.cassandra.net
  - FailureDetector
     - Attributes
        - SimpleStates   
        <to display the current status  (UP/DOWN) of the node connected> 
```

![image](https://user-images.githubusercontent.com/6425536/158005344-6ada0383-ceeb-4c4c-aa16-2cbcea0c6588.png)

If we want to connect using JMX client code below is how the sample code for client looks, but for this demonstration I only used JConsole.

```java
 MBeanServer mbs = MBeanServerFactory.createMBeanServer(); 
JMXServiceURL url = 
  new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server"); 
JMXConnectorServer cs = 
  JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs); 
cs.start(); 
//wait for some duration
cs.stop();
```

### Below is the Java code example for using JMX 

- Defining MBean bean, based on the design the Java class name should be end with MBean.

```java
package jmxdemo.jmxdemo;

public interface MonitorMBean {

   //Attribute will display the time in second from when the process started
	String getProcessDuration();
   //Flag used to stop the process when updated to true form Jconsole
	void exteralStopFlag(boolean stopFlag);

   // stored the counter value when input provided from Jconsole
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

- Helper class to register the bean to MBean server

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
           // another alternate param for ObjectName jmxdemo.jmxdemo:type=basic,mame=monitor
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

public class App {

	public static void main(String[] args) {
        //MBean instantiation 
		Monitor monitor = new Monitor();
        //Helper class to register the MBean with JMX agent
		Helper helper = new Helper();
		helper.createAndRegisterInJMX(monitor);
		boolean stopProcess = false;
		System.out.println("Process started...");
        // while loop to demonstrate the flow 
		while(!stopProcess) {
			status = monitor.checkStopFlag();

   //Note: 
   // For some reason, when we don't use the system.out within this block, the loop is not breaking.
   // when updating the variable to true from jconsole, by printing the return value of monitor class 
  // .checkStopFlag() the values are reflected and while loop breaks to stop the process 
   System.out.println(status);

		}
		System.out.println("shutting down..");
	}
}
```
