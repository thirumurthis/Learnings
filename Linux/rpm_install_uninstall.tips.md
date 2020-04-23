Below command setup the apm-server for Elastic stack

Installed the package

```
$ curl -L -O https://artifacts.elastic.co/downloads/apm-server/apm-server-6.8.0-x86_64.rpm
$ sudo rpm -vi apm-server-6.8.0-x86_64.rpm

//To start the service (systemctl is the new version of service command)
$ systemctl start apm-server
```

To uninstall
```

//To stop the service 
$ systemctl stop apm-server

// to check the status at the runtime
$ systemctl status apm-server

// To prevent from stating the service at the boot time 
$ systemctl disable apm-server

$ sudo rpm -e apm-server
# apm-server is the package name.
```

```
// use journalctl command to see the logs of the service

$ journalctl -u apm-server
```
