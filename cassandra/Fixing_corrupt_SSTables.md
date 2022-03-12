### One of the cassandra node SSTables was corrupted, but still states the status as up.
- This causes the Cassandra node to restart in loop.

In order to fix the SSTable issue and make this node to join the cluster follow below steps.

- Restart the node using `sudo reboot` command
- Once the Machine is up, stop the cassandra service using `sudo systemctl stop cassandra.service`
- Navigate to the cassandra bin location, `cd /opt/cassandra/bin` then issue below command
```
# when service is offline
$ sstablescrub <key-space> <table-name-column-family>
# if the above command displays exception message, follow next step
```
- Navigated to the corrupt folder, took a backup by renaming the folder
- Started the cassandra service `$ sudo systemctl start cassandra.service`
- Use the command `$ sudo journallctl -f -u cassandra` to view the log files 
- Once hte cassandra service is started to repair provide the command `$ nohup sh /opt/cassandra/bin/nodetool repair -full &`
