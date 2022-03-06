### In this section we will see how to ship the logs directly to Elastic search from a spring boot applicaiton.
- We will be using the **logback**, **elastic search appender**. Reference [git repo](https://github.com/internetitem/logback-elasticsearch-appender#readme)

### Use case:
  - When deploying spring boot application in Kubernetes, if we are using Elastic Search for log monitoring, we can use this appender to log messages directly.
  - The advantage would be this eliminates the need for `side car` container or `Fluentd` solution to ship the logs to centralized monitor.

### Pre-step:
  - Create a simple spring boot application with https://start.spring.io
  - **Elastic Search** and **Kibana** configured in the local, and available at `http://localhost:9200/`

### Include the below dependencies in pom.xml
   - Add `logback-elasticsearch-appender` dependency

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.6.4</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.example</groupId>
	<artifactId>logdemo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>logdemo</name>
	<description>Demo project for Spring Boot</description>
	<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	    <maven.compiler.source>11</maven.compiler.source>
    	<maven.compiler.target>11</maven.compiler.target>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
    <dependency>
      <groupId>com.internetitem</groupId>
      <artifactId>logback-elasticsearch-appender</artifactId>
       <version>1.6</version>
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
### Below is the configuration to add `elastic search appender` 
 - Spring using [logback configuration documentation](https://docs.spring.io/spring-boot/docs/2.1.8.RELEASE/reference/html/howto-logging.html)
 - Note, we can use `springprofile` to control the configuration
 - For this demo, I updated the **`logback-spring.xml`** the default logback configuration file that spring can detect automatically.
 - Below xml configuration uses two appender `CONSOLE` and `ELASTIC`
 
```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration debug="true" scan="true">

<springProfile name="!local">  <!-- when the profile is not local and default, this appender will be applied -->
   <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <!-- encoders are assigned the type
         ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{35} - %msg %n</pattern>
    </encoder>
  </appender>
    <appender name="ELASTIC" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">
        <url>http://localhost:9200/_bulk</url>
        <index>demo-%date{yyyy-MM-dd}</index>
        <type>tester</type>
        <loggerName>es-logger</loggerName> <!-- optional -->
        <errorLoggerName>es-error-logger</errorLoggerName> <!-- optional -->
        <connectTimeout>30000</connectTimeout> <!-- optional (in ms, default 30000) -->
        <errorsToStderr>false</errorsToStderr> <!-- optional (default false) -->
        <includeCallerData>false</includeCallerData> <!-- optional (default false) -->
        <logsToStderr>false</logsToStderr> <!-- optional (default false) -->
        <maxQueueSize>104857600</maxQueueSize> <!-- optional (default 104857600) -->
        <maxRetries>3</maxRetries> <!-- optional (default 3) -->
        <readTimeout>30000</readTimeout> <!-- optional (in ms, default 30000) -->
        <sleepTime>250</sleepTime> <!-- optional (in ms, default 250) -->
        <rawJsonMessage>false</rawJsonMessage> <!-- optional (default false) -->
        <includeMdc>false</includeMdc> <!-- optional (default false) -->
        <maxMessageSize>100</maxMessageSize> <!-- optional (default -1 -->
        <authentication class="com.internetitem.logback.elasticsearch.config.BasicAuthentication" /> <!-- optional -->
        <properties>
            <property>
                <name>host</name>
                <value>${HOSTNAME}</value>
                <allowEmpty>false</allowEmpty>
            </property>
            <property>
                <name>severity</name>
                <value>%level</value>
            </property>
            <property>
                <name>thread</name>
                <value>%thread</value>
            </property>
            <property>
                <name>stacktrace</name>
                <value>%ex</value>
            </property>
            <property>
                <name>logger</name>
                <value>%logger</value>
            </property>
        </properties>
        <headers>
            <header>
                <name>Content-Type</name>
                <value>application/json</value>
            </header>
        </headers>
    </appender>
    <!-- We don't want to see a never ending cascade of connection failed ERRORS -->
    <!-- <logger name="my-error-logger" level="OFF" /> -->
    <logger name="es-logger" level="INFO" additivity="false">
        <appender name="ES_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <encoder>
                <pattern>%msg</pattern> <!-- This pattern is important, otherwise it won't be the raw Elasticsearch format anyomre -->
            </encoder>
        </appender>
    </logger>
    <root level="info">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="ELASTIC" />
    </root>
 </springProfile>
</configuration>
```
#### Sample Restcontroller code, with the logger message

```java
package com.example.logdemo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class LogdemoApplication {

  //we are using slfj loggerfactory to instantiate the logger 
	private static final Logger logger    = LoggerFactory.getLogger(LogdemoApplication.class);
 
	public static void main(String[] args) {
		logger.info("Application started ");
		SpringApplication.run(LogdemoApplication.class, args);
		logger.info("Application running..");
	}
	
	@GetMapping("/info")
	public String getInfo() {
    // we will be verifying the info logs displayed in Elastic search
		logger.info("Get endpoint invoked from client...");
		Date date = Calendar.getInstance().getTime();  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		String strDate = dateFormat.format(date); 
		return "Current time from service :"+strDate;
	}
}
```

When set 

#### OUTPUT:

 - When I tried to run the Spring boot application from Eclipse IDE, the profile didn't get picked up when passed as JVM argument like `-Dspring.profiles.active=local`.
 -  In order to pass the active profile, I had to use the environment variable `SPRING_PROFILES_ACTIVE`
   - Click `Run Configuration` of the corresponding Spring boot main class.
   - Then click the `Environment` tab, add a environment variable with active profile. Refer below snapshot.
 
     ![image](https://user-images.githubusercontent.com/6425536/156911200-51dd4be7-23f6-4cea-a628-b95b596ef1b3.png)

- Console output:
  - Upon application startup, we should be able to see the appender registered.
```
21:46:16.471 [main] INFO com.example.logdemo.LogdemoApplication - Application started 
21:46:22,742 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]
21:46:22,743 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [CONSOLE]
21:46:22,794 |-INFO in ch.qos.logback.core.joran.action.NestedComplexPropertyIA - Assuming default type [ch.qos.logback.classic.encoder.PatternLayoutEncoder] for [encoder] property
21:46:22,927 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [com.internetitem.logback.elasticsearch.ElasticsearchAppender]
21:46:22,942 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [ELASTIC]
21:46:22,968 |-INFO in ch.qos.logback.core.joran.action.NestedComplexPropertyIA - Assuming default type [com.internetitem.logback.elasticsearch.config.ElasticsearchProperties] for [properties] property
....
21:46:23,280 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to INFO
21:46:23,280 |-INFO in ch.qos.logback.classic.jul.LevelChangePropagator@70ab2d48 - Propagating INFO level on Logger[ROOT] onto the JUL framework
21:46:23,281 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [CONSOLE] to Logger[ROOT]
21:46:23,283 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [ELASTIC] to Logger[ROOT]
...
```

#### Now, when we hit the url endpoint, `http://localhost:8080/info`, the log message gets shipped to Elastic search.
- Refer below snapshot

![image](https://user-images.githubusercontent.com/6425536/156911691-da07ff95-fc02-40f2-9248-2325494eefdf.png)

![image](https://user-images.githubusercontent.com/6425536/156911499-8d1d7309-bf19-476a-b7da-61e0bdb4435c.png)

#### when the active profile is set to local, below message will be displayed
```
22:21:06.587 [main] INFO com.example.logdemo.LogdemoApplication - Application started 
22:21:10,387 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.
22:21:10,390 |-INFO in org.springframework.boot.logging.logback.SpringBootJoranConfigurator@50d13246 - Registering current configuration as safe fallback point
...
22:21:10,788 |-WARN in Logger[com.example.logdemo.LogdemoApplication] - No appenders present in context [default] for logger [com.example.logdemo.LogdemoApplication].

```
