
## Monitoring the Apache Artemis Queue using ELK stack

### Installing Apache Artemis

  - Download the latest stable version and extract the zip/tar file.
  - Once the compressed file is extracted navigate to the bin directory, to create a broker. use below command

```
# navigate to the extracted folder and find the bin from there use below command
# COMMAND format: artemis create <directory-path-where-the-broker-to-be-created>

$ artemis create /brokers/broker_1

## above command will prompt for user name and password, we can use --user --password as options
## enter to be as an anonymous user
```

- Once broker created successfully, a bunch of folders will be created, like below

```
broker_1
  |_ bin
  |_ data
  |_ etc
  ...

# bin - contains executable to run the broker
# etc - configruation files present here
```

- To start the broker, we need to navigate to the broker_1 directory, under bin/ folder should see the executable.
  Note: 
   - In Windows/Linux the broker can be started as service, running in the background.
   - In Linux, in order auto start the service when VM/machine boots up a script needs to be manually created to call the executable.
    
- Execute below command to run the Artemis broker, from the broker_1 folder

```
/bin/artemis run
```

- From browser use `http://localhost:8161/console` to view the UI. The UI consle should prompt for user name and password. 
- Use the same info used during broker creation to login, once successfully logged in should see screen like in the snapshot.

 Note: Select the Queue tab (under the More drop down option if not visible)

![image](https://user-images.githubusercontent.com/6425536/158077709-ff6af17f-3d53-46ae-b08e-446e96a79f02.png)

Tip:- 
 - The timeout configuration of `hawtio` can be configured by updating the JAVA_ARGS jvm arguments.

- With the UI console displayed, basic configuration is complete.

#### To enable the JMX RMI

- We need to uncomment the `connector` tag from the `management.xml` under the etc folder.
- By default the JMX is not enabled, first we need to uncomment connectors tag.

```
### uncomment the line
 <connector connector-port="1099"/>
```
 - In order to enable the JConsole to connect to the JMX service remotely, we need to add `-Djava.rmi.server.hostname=localhost` to JAVA_ARGS.
 - Update the `artemis.profile.cmd` (in windows) like below along with the other arguments

```
JAVA_ARGS=..... -Djava.rmi.server.hostname=localhost ...
```

- Now restart broker, stop the existing broker process and start it again using `bin/artemis run`.
- In order to successfully access the JMX service we need to updated `jolokia-access.xml` else we will receive below message.

```
$ curl -u admin:admin 'http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\"/Version'

## If we see the below response, then we need to update the jolokia-access.xml
{"error_type":"java.lang.Exception","error":"java.lang.Exception : Origin null is not allowed to call this agent","status":403}
```

#### Artemis Console to fetch JMX URL

- The JMX URL can be fetched directly from the Artemis Hawtio console.
- Refer the snapshot below click the JMX option in console and select the Broker.
 
![image](https://user-images.githubusercontent.com/6425536/158078034-eab807f0-bfab-4f33-9ed3-fe2ddabafd05.png)

 - Click the Version link under the attribute, the Jolokia URL is what we will be using in curl command

![image](https://user-images.githubusercontent.com/6425536/158078084-eb403466-94e1-4b50-971d-c145fe28346d.png)

#### Update jolokia-access.xml to access the JMX service

- In order to successfully access the JMX service, the last change is commenting out the `<cors>` tag in `jolokia-access.xml` under etc folder.
- For demonstration purpose the content in `<restrict>` tag is commented.
- Refer Jolokia documentation for hardening the security access.

- After updating the access xml, restart the the broker service and we should be able to access the JMX service successfully. 

```
# Successful response from JMX service

curl -u admin:admin 'http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\"/Version'
{"request":{"mbean":"org.apache.activemq.artemis:broker=\"0.0.0.0\"","attribute":"Version","type":"read"},"value":"2.20.0","timestamp":1647191397,"status":200}
```

The Artemis configuration is completed now.

#### Connecting to JMX service using JConsole

- Use the `service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi` and approirate broker username and password to connect.

![image](https://user-images.githubusercontent.com/6425536/158084695-70a60524-1263-46a4-8389-e202e2ccc133.png)

![image](https://user-images.githubusercontent.com/6425536/158084643-a2d1193a-ebcc-43d7-9b50-77870002a2b5.png)

## Installing ELK 8.1.0

In order to installing ELK latest version (8.1.0) at the time of writting, download Elastic, Kibana and Logstash from elastic search website.

### Setting up Elastic Search

- Use the executable from the extracted elastic search zip and use the command `$ bin/elasticsearch`. 
- From browser use `http://localhost:9200` to check if Elastic node is up.
   - By default the 8.1.0 version has security enabled, so the URL will prompt with username and password.

#### Adding user and roles to Elastic Search

- From the bin/ folder use `elasticserach-users` executable to add users.
- Below command will create user. Refer the documentation for more details.

```
elasticsearch-users useradd user0 -p password -r superuser,kibana_system,kibana_user,logstash_system,logstas_admin
```

- The known roles that can be used is listed below, new roles can be added refer documentation.

```
Known roles: [apm_system, watcher_admin, viewer, rollup_user, logstash_system, kibana_user, beats_admin, remote_monitori_admin, editor, data_frame_transforms_user, machine_learning_user, machine_learning_admin, watcher_user, apm_user, beats_system, transform_user, reporting_user, kibana_system, transform_admin, remote_monitoring_collector, transport_client, superuser, ingest_admin]
```

- User below command to add a role to  existing user.

```
.\elasticsearch-users roles user0 -a kibana_admin
```

- Use below command to remove a role from existing user.

```
.\elasticsearch-users roles user0 -r kibana_admin
```

### Setting up Kibana

- Kibana needs to connect to Elastic search, since the security is enabled, the `kibana.yml` should be updated with username and password.
- The `kibana.xml` file should be under the extracted Kibana file.

```
elasticsearch.username: "user01"
elasticsearch.password: "password"
```

- Start the Kibana using the executable under bin folder.

```
$ bin/kibana
```

- In previous Kibana version 7.0, after adding the index under Dev Tools -> Stack Management, we used to see the index info under Discover option.

- In Kibana version 8.1.0, we need to create a new Data View.
  - Under Discover option, click Manage Spaces -> click Create Data View. Refer below snapshot.

![image](https://user-images.githubusercontent.com/6425536/158080003-dddee158-1b8e-4473-b108-7b0cac8c1178.png)

- Click "Create Data view" and add the index.

![image](https://user-images.githubusercontent.com/6425536/158083816-ebb6e763-3ea9-4441-8a40-9bb1650b207d.png)

![image](https://user-images.githubusercontent.com/6425536/158080039-06dd74d5-6042-466b-82f9-4340136eb2ff.png)

### Setting up Logstash

- We need to create a configuration for logstash to push messages to Elastic search.
- Since we are using JMX service, we need to install the jmx plugin, using  below command

```
# navigate to the bin folder under the logstash extracted directory

bin/logstash-plugin install logstash-input-jmx
```

Refer the [Elastic Search documentation](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-jmx.html) for more info.

- Below is the logstash configuration file for using JMX plugging.
- Place below configuration file under the conf directory under unzipped logstash file.
- Create a file named `local-jmx.config` and copy below content. 
  - Note: The file name will be passed as input to logstash executable. We are not using data_stream.

```
input {
  jmx {
   # path refers to the directory of the jmx config file
    path => "C://thiru//learn//elk//8_1_0//config//"
    polling_frequency => 60
    nb_thread => 5
    type => "jmx"
  }
}
output {
    elasticsearch {
      hosts => ["http://localhost:9200"]
      index => "artemis-%{+YYYY.MM.dd}"
      user => "user0"
      password => "password"
    }
}
```

- Create a file and copy below content under the `path` specified in above configuration.
- In this case, `jmxquery.config` under `C://thiru//learn//elk//8_1_0//config//`.

```
{
  "host" : "127.0.0.1",
  "port" : 1099,
  "url": "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi",
  "username" : "admin",
  "password" : "admin",
  "queries" : [
  {
    "object_name" :  "org.apache.activemq.artemis:broker=\"0.0.0.0\"",
    "attributes" : ["version"]
  },
  {
    "object_name" : "org.apache.activemq.artemis:broker=\"0.0.0.0\"",
    "attributes" : [ "total_message_count" ]
   }
 ]
}
```

- Start the logstash service using the executable under logstash*/bin directory, below is the command.

```
# bin/logstash <config-file-path-extracted-logstash-config-folder>

$bin/logstash C://thiru//learn//elk//8_1_0//config//local-jmx.config
```

- On successful start up we should be able to see message like below in the console.

```
[2022-03-13T12:34:23,991][INFO ][logstash.agent ] Successfully started Logstash API endpoint {:port=>9600, :ssl_enabled=>false}
[2022-03-13T12:34:27,862][INFO ][org.reflections.Reflections] Reflections took 358 ms to scan 1 urls, producing 120 keys and 417 values
[2022-03-13T12:34:29,614][INFO ][logstash.javapipeline    ] Pipeline `main` is configured with `pipeline.ecs_compatibility: v8` setting. All plugins in this pipeline will default to `ecs_compatibility => v8` unless explicitly configured otherwise.
[2022-03-13T12:34:29,902][INFO ][logstash.outputs.elasticsearch][main] New Elasticsearch output {:class=>"LogStash::Outputs::ElasticSearch", :hosts=>["http://localhost:9200"]}
[2022-03-13T12:34:31,068][INFO ][logstash.outputs.elasticsearch][main] Elasticsearch pool URLs updated {:changes=>{:removed=>[], :added=>[http://thiru:xxxxxx@localhost:9200/]}}
[2022-03-13T12:34:31,971][WARN ][logstash.outputs.elasticsearch][main] Restored connection to ES instance {:url=>"http://user0:xxxxxx@localhost:9200/"}
[2022-03-13T12:34:32,011][INFO ][logstash.outputs.elasticsearch][main] Elasticsearch version determined (8.1.0) {:es_version=>8}
[2022-03-13T12:34:32,017][WARN ][logstash.outputs.elasticsearch][main] Detected a 6.x and above cluster: the `type` event field won't be used to determine the document _type {:es_version=>8}
[2022-03-13T12:34:32,160][INFO ][logstash.outputs.elasticsearch][main] Config is not compliant with data streams. `data_stream => auto` resolved to `false`
[2022-03-13T12:34:32,169][INFO ][logstash.outputs.elasticsearch][main] Config is not compliant with data streams. `data_stream => auto` resolved to `false`
[2022-03-13T12:34:32,181][WARN ][logstash.outputs.elasticsearch][main] Elasticsearch Output configured with `ecs_compatibility => v8`, which resolved to an UNRELEASED preview of version 8.0.0 of the Elastic Common Schema. Once ECS v8 and an updated release of this plugin are publicly available, you will need to update this plugin to resolve this warning.
[2022-03-13T12:34:32,311][INFO ][logstash.outputs.elasticsearch][main] Using a default mapping template {:es_version=>8, :ecs_compatibility=>:v8}
[2022-03-13T12:34:32,467][INFO ][logstash.javapipeline    ][main] Starting pipeline {:pipeline_id=>"main", "pipeline.workers"=>4, "pipeline.batch.size"=>125, "pipeline.batch.delay"=>50, "pipeline.max_inflight"=>500, "pipeline.sources"=>["C:/thiru/learn/elk/8_0_1/logstash-8.1.0/config/local-jmx.config"], :thread=>"#<Thread:0x3b6bfa1d run>"}
[2022-03-13T12:34:34,771][INFO ][logstash.javapipeline    ][main] Pipeline Java execution initialization time {"seconds"=>2.28}
[2022-03-13T12:34:35,035][INFO ][logstash.inputs.jmx      ][main] Create queue dispatching JMX requests to threads
[2022-03-13T12:34:35,038][INFO ][logstash.inputs.jmx      ][main] Compile regexp for group alias object replacement
[2022-03-13T12:34:35,069][INFO ][logstash.javapipeline    ][main] Pipeline started {"pipeline.id"=>"main"}
[2022-03-13T12:34:35,121][INFO ][logstash.inputs.jmx      ][main][6f6e2863248f814723878dd763c828e7917fe8560054cfa2dd7382ab88016533] Initialize 5 threads for JMX metrics collection
```

#### Issues during logstash start up
 
- If there are exception during logstash startup like below.
 - Check the configuration file, in this case `local-jmx.config`. For more details refer documentation.

```
[2022-03-13T11:29:10,179][ERROR][logstash.agent           ] Failed to execute action {:action=>LogStash::PipelineAction::Create/pipeline_id:main, :exception=>"LogStash::ConfigurationError", :message=>"Expected one of [A-Za-z0-9_-], [ \\t\\r\\n], \"#\", \"=>\" at line 4, column 13 (byte 100) after input {\r\n  jmx {\r\n    path => \"C://thiru//learn//elk//8_0_1//config//jmxquery.config\"\r\n    jmxquery", :backtrace=>["C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/compiler.rb:32:in `compile_imperative'", "org/logstash/execution/AbstractPipelineExt.java:189:in `initialize'", "org/logstash/execution/JavaBasePipelineExt.java:72:in `initialize'", "C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/java_pipeline.rb:47:in `initialize'", "C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/pipeline_action/create.rb:50:in `execute'", "C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/agent.rb:376:in `block in converge_state'"]}
[2022-03-13T11:29:10,407][INFO ][logstash.runner          ] Logstash shut down.
[2022-03-13T11:29:10,432][FATAL][org.logstash.Logstash    ] Logstash stopped processing because of an error: (SystemExit) exit
org.jruby.exceptions.SystemExit: (SystemExit) exit
```

-----

### Output in Kibana with result fetched from Artemis JMX service

Once the logstash starts successfully the kibana should start displaying the message. In this case the version number.
 - From the below snapshot we can see the `metric_value_string` displays the version of the Artemis which is 2.20.0.

![image](https://user-images.githubusercontent.com/6425536/158081109-a764d0e8-0cf5-42a0-b236-553f879e5dec.png)

- Once the below mentioned Spring Boot code (refer next section) is started.
- The below curl command can be used to push message to the queue `test.rest.queue`.

```
# Curl command to push message test01 to queue

curl -X POST http://localhost:8085/artemis/sendMessage?message=test01
```

- After executing the curl command twice, the `metric_value_string` should display 2 the number of messages in the queue.
-  In this case there are 2 messages among all the queue. That is displayed in the Kibana as well.

![image](https://user-images.githubusercontent.com/6425536/158081213-77942621-0448-463c-a185-a8c6deab64e8.png)

- Kibana screenshot displaying version and message count in queue.

![image](https://user-images.githubusercontent.com/6425536/158081238-f9388e8c-4406-4710-99f0-ce4c58b00d94.png)

---------

### Spring Boot Application to Produce message Artemis with Proton QPid client.

 - Create RestController

```java
package com.artemis.demo.monitorqueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class ArtemisProducer {

    @Autowired
    JmsTemplate jmsTemplate;

    @PostMapping(value = "/artemis/sendMessage")
    public void sendMessage(
        @RequestParam(name = "message", required = true) final String[] message){
            jmsTemplate.convertAndSend("test.rest.queue", message);
    }
}
```

- Create JMS Connection factory configuration for Qpid client

```java
package com.artemis.demo.monitorqueue;

import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ArtemisQueueConfiguration {

    @Value("${artemisUser}")
    private String userName;

    @Value("${artemisPassword}")
    private String password;

    @Value("${brokerUrl}")
    private String brokerUrl;

    @Value("${connectionCount}")
    private int connectionCount;

    @Bean
    public JmsConnectionFactory jmsConnectionFactory() {
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(userName, password, brokerUrl);
        return jmsConnectionFactory;
    }

    @Bean(initMethod="start",destroyMethod="stop")
    public JmsPoolConnectionFactory jmsPoolConnectionFactory() {
        JmsPoolConnectionFactory jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
        jmsPoolConnectionFactory.setMaxConnections(connectionCount);
jmsPoolConnectionFactory.setConnectionFactory(jmsConnectionFactory());
            return jmsPoolConnectionFactory;
        }

    @Bean
    public JmsConfiguration jmsConfiguration(@Value("${connectionCount}") int connectionCount){
        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        jmsConfiguration.setConcurrentConsumers(connectionCount);        jmsConfiguration.setConnectionFactory(jmsPoolConnectionFactory());
        return jmsConfiguration;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return new JmsTemplate(jmsConnectionFactory());
    }
}
```

 - application.properties with the configuration details

```properties
brokerUrl = amqp://localhost:5672
artemisUser = admin
artemisPassword = admin

connectionCount=3

server.port=8085
```

- pom.xml with the dependencies

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
	<groupId>com.artemis.demo</groupId>
	<artifactId>monitorqueue</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>monitorqueue</name>
	<description>Project to create and push message </description>
	<properties>
		<java.version>11</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-artemis</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-spring-boot-starter</artifactId>
			<version>3.15.0</version>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<!-- For AMQP protocol support queue -->
		<dependency>
		    <groupId>org.apache.qpid</groupId>
		    <artifactId>qpid-jms-client</artifactId>
		    <version>1.5.0</version>
		</dependency>
		<dependency>
		    <groupId>org.messaginghub</groupId>
		    <artifactId>pooled-jms</artifactId>
		    <version>2.0.4</version>
		</dependency>
		<dependency>
		    <groupId>org.apache.camel</groupId>
		    <artifactId>camel-jms</artifactId>
		    <version>3.15.0</version>
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
