
### logstash configuraiton 
  - In order to caputer the new lines when the logs are pushed out we can use below pattern
  - In one of my project, the logs from relational database is thrown with the constructed query which is in new line, 
  - this new line are trimmed with default configuration

```
intput{

  file {
    type => "name-for-type"
    path => "where/the/log/file/is/present*.log"
    start_position => "end"
    codec => multiline {
       pattern => "^%{TIMESTAMP_ISO8601}"
       negate => true
       what => previous
     }
    tags => ["my_web_app_log"]
   }
   
   file {
     type => "another-type-file"
     .....
      ....
     }
}
filter {
   if [type] == "name-for-type" {
     grok {
        patterns_dir => ["/opt/my-project/patterns"]
        match => {
           "message" => "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:%{MINUTE}:%{SECOND},%{NUMBER:msec}: %{NOTSPACE:threadname}: %{LOGLEVEL:loglevel} %{GREEDYDATA:class} - %{GREEDYDATA:message}"
         }
      }
   }
   if [type] == "another-type-file" {
     .....
      match => {
     "message" => [ "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:%{MINUTE}:%{SECOND},%{NUMBER:msec}: %{WORD:usernm} %{GREEDYDATA} thread #%{POSINT:thread} - %{FUNCTIONNAME:procname}: %{LOGLEVEL:loglevel} %{GREEDYDATA:class} - %{GREEDYDATA:message}",
           "%{YEAR}-%{MONTHNUM}-%{MONTHDAY} %{HOUR}:%{MINUTE}:%{SECOND},%{NUMBER:msec}: %{WORD:usernm}-%{DATA}: %{LOGLEVEL:loglevel} \(%{DATA:class}\) - %{GREEDYDATA:message}"]

    }
}
```
 - Note the output was configured in a sperate single conf file, since we faced an issue of duplicate data being recorded in the kibana

/opt/my-project/patterns => contains the file custompatterns which defines specific patters that needs to be applied
 - below is used in the second type grog pattern, which also demonstrate multi patter type.
```
FUNCTIONNAME [0-9a-zA-Z]+\.java
SPECSTRING ((?!Network)\S)+
ERRLOG \b(?:\w{3,6})\b
```

other patterns of Grok:

```
\A%{TIMESTAMP_ISO8601:timestamp}\s+%{HOSTNAME:hostname}\s+%{HOSTNAME:threadname}:\s+%{LOGLEVEL:loglevel}\s+\(%{JAVAFILE:javafile}:%{INT:linenum}\)\s+(-\s+)?(?<message>(.|\r|\n)*)({({[^}]+},?\s*)*})?\s*$(?<stacktrace>(?m:.*))

```
