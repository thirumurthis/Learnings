
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

The timeout cofiguration of `hawtio` can be configured by updating the JAVA_ARGS jvm arguments.

- With the UI console displayed, basic configuration is complete.

### To enable the JMX RMI
- We need to uncomment the `connector` tag from the `management.xml` under the etc folder.
- By default the JMX is not enabled, first we need to uncomment connectors tag.
```
### uncomment the line
 <connector connector-port="1099"/>`
```
 - In order to enable the JConsole to connect to the JMX service remotely, we need to add `-Djava.rmi.server.hostname=localhost` to JAVA_ARGS.
 - Update the `artemis.profile.cmd` (in windows) like below along with the other arguments
```
JAVA_ARGS=..... -Djava.rmi.server.hostname=localhost ...
```

- Noe restart broker, stop the existing broker process and start it again using `bin/artemis run`.
- In order to successfully access the JMX service we need to updated `jolokia-access.xml` else we will recieve below message.
```
$ curl -u admin:admin 'http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\"/Version'

## If we see the below response, then we need to update the jolokia-access.xml
{"error_type":"java.lang.Exception","error":"java.lang.Exception : Origin null is not allowed to call this agent","status":403}
```

Note: 
  - The JMX url can also be fetched direclty from the Atermis Hawtio console.
    -  clicking the JMX option in console and select the Borker info. For more details refer below snapshot.
 
![image](https://user-images.githubusercontent.com/6425536/158078034-eab807f0-bfab-4f33-9ed3-fe2ddabafd05.png)

   - Click the Version link under the attribute, the Jolokia URL is what we will be using in curl command

![image](https://user-images.githubusercontent.com/6425536/158078084-eb403466-94e1-4b50-971d-c145fe28346d.png)

- In order to successfully access the JMX service, the last change is commenting out the `<cors>` tag in `jolokia-access.xml` under etc folder.
 - For demonstration purpose I simply commented the content, but check Jolokia documentation for hardening the security access.

- Now restart the the broker and we should be able to access the JMX service. We should get a response like below.
```
curl -u admin:admin 'http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\"/Version'
{"request":{"mbean":"org.apache.activemq.artemis:broker=\"0.0.0.0\"","attribute":"Version","type":"read"},"value":"2.20.0","timestamp":1647191397,"status":200}
```

The Artemis configuration is completed now.

## Installing ELK 8.1.0

In order to installing ELK latest version (8.1.0) at the time of writting, download Elastic, Kibana and logstash from elasticsearch website.
- I am using the Windows machine, so extracted the Zip locally. 
#### Start the Elastic serach 
- Navigating to extracted elastic search folder issue the comamnd `$ bin/elasticsearch`. 
- Once Elastic Search is successfully started, from browser use `http://localhost:9200` to check if Eleastic node is up.
   - By default the latest version has security enabled, so you will be prompted with username and password.
##### Adding user and roles
 - Navigate to the extracted elastic search directory bin/ folder, use `elasticserach-users` executable script to add users.

- To create user below is the command. Check the documentation for more details. 
```
elasticsearch-users useradd user0 -p password -r superuser,kibana_system,kibana_user,logstash_system,logstas_admin
```
- The known roles that will be displayed part of the userdd command.
```
Known roles: [apm_system, watcher_admin, viewer, rollup_user, logstash_system, kibana_user, beats_admin, remote_monitori_admin, editor, data_frame_transforms_user, machine_learning_user, machine_learning_admin, watcher_user, apm_user, beats_system, transform_user, reporting_user, kibana_system, transform_admin, remote_monitoring_collector, transport_client, superuser, ingest_admin]
```
- We can add a role to  existing user with below comamnd.
```
.\elasticsearch-users roles thiru -a kibana_admin
```
- We can remove a role from existing user using below command.
```
.\elasticsearch-users roles thiru -r kibana_admin
```
### Setting up the Kibana

- For kibana to be connect to Elastic search, we need to update the `kibana.yml` with user name and password.
- From the extractd kibana file, navigate to config directory and update the `kibana.yml` file with below info.

```
elasticsearch.username: "user01"
elasticsearch.password: "password"
```

- Start the kibana using the executable under bin folder of extracted kibana file.
```
$ bin/kibana
```
- By default the 7.0+ version, when the index is added using the Dev Tools -> Stack Management, we used to see the index info under discover.
- But in the 8.1+ I had to follow below steps.
  - After clicking the Discover option, i need to setup the Spaces -> create a new data view. Refer below snapshot.

![image](https://user-images.githubusercontent.com/6425536/158080039-06dd74d5-6042-466b-82f9-4340136eb2ff.png)

Dataview creation, click "Create Data veiw" and add the index.

![image](https://user-images.githubusercontent.com/6425536/158080003-dddee158-1b8e-4473-b108-7b0cac8c1178.png)

#### Setting up the Logstash
- We need to create a configuration for logstash to push messages to Elastic search.
- Since we are using JMX service, we need to install the jmx plugin, using  below command
```
# navigate to the bin folder under the logstash extracted directory

bin/logstash-plugin install logstash-input-jmx
```
Refer the [elasticsearch documentation](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-jmx.html) for more info.

- Below is the logstash configuration file for using JMX pluging.
- We are going to place this configuration under the logstash extracted file, under config directory.
    - Note: We are not using data_stream.
- Create a file named `local-jmx.config` with below content. This file name will be passed as input to logstash executable.
```
input {
  jmx {
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
Note: 
  - In above configuration we are specifying the directory path where the jmx configuration is created.

- In the path `"C://thiru//learn//elk//8_1_0//config//"` I created the jmx query configuration 
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

- Start the logstash service, by navigating to the extracted logstash location bin directory and issue below command 
```
# bin/logstash <config-file-path-extracted-logstash-config-folder>

$bin/logstash C://thiru//learn//elk//8_1_0//config//local-jmx.config
```
- Once started you should be able to see below in the logstash console
```
[2022-03-13T12:34:23,991][INFO ][logstash.agent           ] Successfully started Logstash API endpoint {:port=>9600, :ssl_enabled=>false}
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

Note: 
 If the logstash When starting the logstash if you see below exception, cross check the configuration of local-jmx.config file
```
[2022-03-13T11:29:10,179][ERROR][logstash.agent           ] Failed to execute action {:action=>LogStash::PipelineAction::Create/pipeline_id:main, :exception=>"LogStash::ConfigurationError", :message=>"Expected one of [A-Za-z0-9_-], [ \\t\\r\\n], \"#\", \"=>\" at line 4, column 13 (byte 100) after input {\r\n  jmx {\r\n    path => \"C://thiru//learn//elk//8_0_1//config//jmxquery.config\"\r\n    jmxquery", :backtrace=>["C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/compiler.rb:32:in `compile_imperative'", "org/logstash/execution/AbstractPipelineExt.java:189:in `initialize'", "org/logstash/execution/JavaBasePipelineExt.java:72:in `initialize'", "C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/java_pipeline.rb:47:in `initialize'", "C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/pipeline_action/create.rb:50:in `execute'", "C:/thiru/learn/elk/8_0_1/logstash-8.1.0/logstash-core/lib/logstash/agent.rb:376:in `block in converge_state'"]}
[2022-03-13T11:29:10,407][INFO ][logstash.runner          ] Logstash shut down.
[2022-03-13T11:29:10,432][FATAL][org.logstash.Logstash    ] Logstash stopped processing because of an error: (SystemExit) exit
org.jruby.exceptions.SystemExit: (SystemExit) exit
```
