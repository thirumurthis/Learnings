check [link](https://access.redhat.com/solutions/27943)

Basically a python script is configured in the VM, and executed using shell script.

This python script will determined which package got updated, etc. refer the link above.


```
## uptime command provides how long the unix box is running.
$ uptime 
```
The return value is pushed to Elastic search using another shell script, which uses CURL command.

Script that used to send the data to ES
```
# create a data object
message="some message from script vm"
host=$(hostname) //use uname -a with cut 
data='{"hostname":"'"${host}"'", "message" :"'"${message}"'}';
curl -XPOST "http://elasticsearch:9xxx/indexname-$dateval/alertinfo/?pretty" -H 'Content-Type: application/json' -d ${data}
```
