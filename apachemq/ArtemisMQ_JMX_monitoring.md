
In order to setup Apache Artemis in local
 - Download the latest version (stable) and extract the zip/tar file.

First we need to create a broker, for that we can use below command.
```
# navigate to the extracted folder and find the bin from there use below command
# artemis create <directory-path-where-the-broker-to-be-created>

$ artemis create /brokers/broker_1

## above command will prompt for user name and password, we can use --user --password as options
## enter to be as an anonymous user
```

Once the above command executed successfully, the folder will be created which has a set of standard folder
```
broker_1
  |_ bin
  |_ data
  |_ etc
  ...
```

- To start the broker, we need to navigage to the broker_1 directory
- The bin/ directory has the executable to start the process.
  Note: 
    - In Windows/Linux the broker can be started as service, running in the background.
    - In Linux, in order auto start the service when VM/machine boots up this needs to be set as a service manually.
    
Below command runs the Artemis broker
```
/bin/artemis run
```
 - Now navigating to the `http://localhost:8161/console` should prompt for user name and passowrd. Use the same info used during broker creation, to login.

 - Now if you see the screens loaded, then everything seems to work as expected.
 
Enabling the JMX RMI option
By default the JMX is not enabled, to enable the JMX RMI option we need to enable the port by uncommenting connectors.

- In `manangement.xml` uncomment the `<connector connector-port="1099"/>`

Also in order to enable the JConsole to connect to the JMX service we need to pass the JAVA_ARGS `-Djava.rmi.server.hostname=localhost` for which we need to updae  `artemis.profile.cmd` in the JAVA_ARGS
```
JAVA_ARGS=..... -Djava.rmi.server.hostname=localhost ...
```

- Restart the artemis broker, just by stopping the existing broker process and starting it again.

- Now, we can use `curl` command to check if we are able to access the JMX service
- Below we will check the version of the Apache artemis
```
 curl -u admin:admin 'http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\"/Version'
{"error_type":"java.lang.Exception","error":"java.lang.Exception : Origin null is not allowed to call this agent","status":403}
```
 Note: The JMX url can be fetched from the Atermis Hawtio console, by clicking the JMX option, and selecting the Borker info.
 
 <<snpshot>>
 
 - If we didn't update the `jolokia-access.xml` for cors configuration, we will not be able to access the JMX. For enabling it simply coment out the cor tags under the restrict tag. 
 For demonstration purpose, I have commented out all the cors information under the `<restrict>` tag, but check the jolokia documentation for hardening the security further.

- Once commented out, restart the broker. The above curl command should responded with reponse message like below.
```
curl -u admin:admin 'http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\"/Version'
{"request":{"mbean":"org.apache.activemq.artemis:broker=\"0.0.0.0\"","attribute":"Version","type":"read"},"value":"2.20.0","timestamp":1647191397,"status":200}
```
Installing ELK latest version (8.0.1) at the time of writting.

- Download all three zip, I am using Windows machine. 
- First start the Elastic Search, by navigating to bin/elasticsearch. Once started, hit `http://localhost:9200`, by default the security is enabled and prompt for username and password will be displayed.
- Navigate to the bin folder in the Elastic serach extracted directory, we need to add a user using the utilities

- Creating and user with role. Below command will create a user0 with roles 
```
elasticsearch-users useradd user0 -p password -r superuser,kibana_system,kibana_user,logstash_system,logstas_admin
```
- The know roles with the default configuration are listed by the same command.
```
Known roles: [apm_system, watcher_admin, viewer, rollup_user, logstash_system, kibana_user, beats_admin, remote_monitori_admin, editor, data_frame_transforms_user, machine_learning_user, machine_learning_admin, watcher_user, apm_user, beats_system, transform_user, reporting_user, kibana_system, transform_admin, remote_monitoring_collector, transport_client, superuser, ingest_admin]
```
- To add a role after user is added, we can use below command
```
.\elasticsearch-users roles thiru -a kibana_admin
```
-To remove the role on an user 
```
.\elasticsearch-users roles thiru -r kibana_admin
```

- For kibana to be connect with the Elastic search, we need to update the user name and password of the user created above to `kibana.yml` file.
- Search for username

```
elasticsearch.username: "user01"
elasticsearch.password: "password"
```

After updating the kibana.yml, start the kibana process by navigating to kibana extracted folder
```
$ bin/kibana
```

- Configuring the Logstash to push message.
- Since we are using JMX service, we need to install the jmx plugin, using  below command
- Once the logstash zip is extracted, navigate to the bin directory, and issue below command
```
bin/logstash-plugin install logstash-input-jmx
```
Refer the [elasticsearch documentation](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-jmx.html) 

- In order to configure the jmx configuration, under the logstash extracted folder, navigate to config and create a file with below content. Note we are not going to use data_stream.
- Name the file as `local-jmx.config`. This file name will be passed as input to logstash executable.
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
Note in above configuration we are specifying the directory path where the jmx configuration is created.

- In the path `"C://thiru//learn//elk//8_1_0//config//"` create the jmx query configuration 
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
- Start the logstash, by navigating to the extracted logstash location, and issue below command 
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
