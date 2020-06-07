##### Logback configuration `logback.xml`

```xml
<configuration>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>
                %d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n
            </Pattern>
        </layout>
    </appender>

    <property name="LOG_LOC" value="C:/logs/app.log"/>

    <appender name="FILE-ROLLING" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_LOC}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>C:/logs/archived/app.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <!-- archive file size max 10MB -->
            <maxFileSize>10MB</maxFileSize>
            <!-- total size of all archived files, when total size > 1GB delete old archived file -->
            <totalSizeCap>1GB</totalSizeCap>
            <!-- hold 10 days -->
            <maxHistory>60</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d %p %c{1.} [%t] %m%n</pattern>
        </encoder>
    </appender>

    <logger name="org.logback.demo" level="debug" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE-ROLLING"/>
    </logger>

    <root level="debug">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE-ROLLING"/>
    </root>

</configuration>
```
##### maven pom.xml dependency
```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.logback.demo</groupId>
  <artifactId>LogBackDemo</artifactId>
  <version>1.0-SNAPSHOT</version>

  <name>LogBackDemo</name>
  <url>http://www.web.com</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>1.7</maven.compiler.source>
    <maven.compiler.target>1.7</maven.compiler.target>
  </properties>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
    
    <!-- with reference to the SLF4J documentation, logback-core and logback-classic dependency -->
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.30</version>
    </dependency>
   <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-core</artifactId>
      <version>1.2.3</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.2.3</version>
    </dependency>
  </dependencies>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.1.0</version>
        </plugin>

        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.8.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>2.22.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-install-plugin</artifactId>
          <version>2.5.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>2.8.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-site-plugin</artifactId>
          <version>3.7.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-project-info-reports-plugin</artifactId>
          <version>3.0.0</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>

```

###### Sample java code
```java
package org.logback.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackDemo.class);
    public static void main(String ... args){

        LOGGER.info("This is in main method");
        System.out.print("in main...");
    }
}

```
