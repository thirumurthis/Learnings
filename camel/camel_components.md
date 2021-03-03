### Camel Components
#### camel stream
  - Stream the data to console. Example route
```java
    // RouteBuilder info
    @Override
    public void configure() {
    from("file:data/inbox?noop=true").to("stream:out");  //noop don't delete the original file from input directory
    }
```
  - Stream to prompt input from user and print to a file
```java
    public void configure() {
      from("stream:in?promptMessage=Enter something:").to("file:data/outbox"); //The file name is not specified in here so a UID format file name will be created
    }
```
  - To specify the file, use `fileName` attribute in the to route. Since the file will get overwritten everytime, we can append date to the filename as below
```java
// in this case teh filename will be overritten when executing the route again
public void configure() {
   from("stream:in?promptMessage=Enter something:").to("file:data/outbox?fileName=userprompt.txt");
}

// below is to use date as filename
public void configure() {
from("stream:in?promptMessage=Enter something:").to("file:data/outbox?fileName=${date:now:yyyyMMdd-hh:mm:ss}.txt"); //filename will be the format 
                                                                               // date format can be any format supported by the java.txtSimpleDateFormat.
}
```
#### camel ftp
   - `ftp` component inherits all the feature of the `file` component with few more options
   - The ftp component is not part of the camel-core package, add below dependency
```xml
<dependency>
  <groupId>org.apache.camel</groupId>
  <artifactId>camel-ftp</artifactId>
  <version>${camel-version}</version>
</dependency>

 <!-- also add common-net utils depedency for ftp to work-->
<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>3.6</version>
</dependency>

```
- Sample demonstration of ftp component, we are using spring based DSL, since spring provides easy hook to start and stop the embedded FTP server.
```xml
 <!-- Save below in the filename stream-ftp-context.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd       ">
    <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
        <route>
            <from uri="stream:in?promptMessage=Enter Something:" />
            <!-- <transform><simple>${body}</simple></transform> -->
            <to uri="ftp://thiru:secret@localhost:21000/data/outbox"/>
        </route>
    </camelContext>
</beans>
```
 - Java part using Spring context to run, per 3+ camel version document, use one camel context per context.
```java
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InvokeMainCamelFTPSpring {

	public static void main(String[] args) {
		
		ApplicationContext appContext = new ClassPathXmlApplicationContext("stream-ftp-context.xml");
		SpringCamelContext  context = (SpringCamelContext)appContext.getBean("camel"); //camel context referenced within 
		try {
     		context.start();
     		Thread.sleep(120000);
		}catch(Exception exe) {
			 // nothing for now.
		}finally {
			context.stop();
		}
	}
}
```
 - To spin up an embedded FTP server, [check the link](https://cwiki.apache.org/confluence/display/FTPSERVER/Embedding+FtpServer+in+5+minutes)

#### To spin up an embedded server use below code
 - also check this link for [more info](https://www.programmersought.com/article/8525219396/).
```java
package com.ftp.server.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class FtpServerEmbedded {
  public static void main(String[] args) throws FtpException {
	
	  FtpServerFactory serverFactory = new FtpServerFactory();
      
	  ListenerFactory factory = new ListenerFactory();
	           
	  // set the port of the listener
	  factory.setPort(21000);
	  	   
	  // replace the default listener
	  serverFactory.addListener("default", factory.createListener());
	  
	  PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    //Create this file in resources with blank content.
	  userManagerFactory.setFile(new File("C:\\thiru\\Java9_learning\\learn2\\workspace\\CamelProj1\\resources\\myuser.properties"));
	  
	  UserManager um = userManagerFactory.createUserManager();
      
      // this will be the base user with write permission
      BaseUser user = new BaseUser();
      user.setName("thiru");
      user.setPassword("secret");
      //Use the existing directory in windows.
      user.setHomeDirectory("C:/thiru/learn/ftproot/");
      List<Authority> authorities = new ArrayList<Authority>();
      authorities.add(new WritePermission());
      //authorities.add(new ConcurrentLoginPermission(2, Integer.MAX_VALUE));
      user.setAuthorities(authorities);
      
      um.save(user);
	  //serverFactory.setUserManager(userManagerFactory.createUserManager());
      serverFactory.setUserManager(um);                  
	  // start the server
	  FtpServer server = serverFactory.createServer();
	  server.start();
   }
}
```

#### camel file

