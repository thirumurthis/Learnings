## Local SMTP mail development using FakeSMTP 

In this blog will demonstrate how we can use FakeSMTP library to develop mail functionality like sending mail to server.

In order to demonstrate the functionality
- We need to setup and start the FakeSMTP server in local.
- Write a simple SpringBoot REST GET Endpoint to trigger an email.

### Setting up the FakeSMTP server in local

  - This is very easy all we need to do is download the jar from [FakeSMTP Website](http://nilhcem.com/FakeSMTP/download.html).
  - Extract it to any location, from command prompt start the service using below command. The fakeSMPT.jar is extracted from the download.
  
  ```
  > java -jar fakeSMTP.jar -o ./ouput
  ```
  
  - Above command will open up a simple UI, where you need to click the `Start Server` to start the SMTP server.
  
  > **Note**
  > - I am using the default configuration, check the documentation for customization like port, host, etc.
  
  - FakeSMTP UI will look as in below snapshot

![image](https://user-images.githubusercontent.com/6425536/177018447-6c2d3fb6-074a-4c32-915c-c317458f3121.png)

  
### Springboot REST endpoint to trigger sendMail

- We will develop a simple SpringBoot REST app with Java Mail dependency to send mail.
 
- Below is the pom.xml with the required dependency.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.1</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.mail.demo</groupId>
	<artifactId>mail</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>mail</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-mail</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

- SpringBoot application entry point Java class.

```java
package com.mail.demo.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailApplication.class, args);
	}
}
```

- Controller code
 
```
package com.mail.demo.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailController {
    
    @Autowired
    MailService service;

    @Value("${mail.id}")
    String mailId;

    @GetMapping(value="sendMail/{msg}")
    public String getMethodName(@PathVariable("msg") String msg) {

        String subject = "subject - this is a test ";
        String body = "Body of the message Data from Rest - "+msg;
        service.sendSimpleMessage(mailId, subject, body);
        //Using java new feature of python style string declaration
        var response = 
            """
            {
                "status" : "Mail delivered"
            }                   
            """;
        return response;
    }
}
```

- Config bean 

```java
package com.mail.demo.mail;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class ConfigMail {
    
    @Value("${mail.host}")
    String mailHost;

    @Value("${mail.port}")
    Integer mailPort;

    @Value("${mail.id}")
    String mailId;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        return mailSender;
    }
}
```
- Service to send mail

```java
package com.mail.demo.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailService {
    
    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setFrom("example@company.com");
        message.setTo(to); 
        message.setSubject(subject); 
        message.setText(text);
        emailSender.send(message);
    }
}
```

- Properties file 

```properties

### The host and port where the FakeSMTP is running
### This can be later changed with specific provider like Gmail, SendMail Services, etc.

mail.host=localhost
mail.port=25

spring.mail.host=smtp
spring.mail.port=25
```

### Output 
  - On executing the SpringBoot application, invoking `http://localhost:8080/sendMail/test` will send the mail to the FakeSMTP server that is running.
  
- Below is the snapshot of the mail sent by the REST endpoint
 
![image](https://user-images.githubusercontent.com/6425536/177018385-946d32e0-db79-40bd-89b0-6cd714cf4e26.png)

- Below is the body of the mail

![image](https://user-images.githubusercontent.com/6425536/177018424-ec131f2f-e759-4d41-b0bf-98cbbe51fc01.png)


  
