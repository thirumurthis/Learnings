### `sshd` at the VM or linux machine

In order to access the VM or linux box from remote machine, the VM server should run open-ssh server.

```
# to install the openssh-server if not present.
$ sudo apt-get install openssh-server

# command to validate the status of the sshd (dameon) process status
$ sudo systemctl status sshd
# or
$ sudo ps -ef | grep ssd

# command to restart the sshd process 
$ sudo systemctl restart sshd
```

### `/etc/ssh/sshd_conf` can be used to control the login steps
`sshd_conf` file property config parameters can be changed
   - to change the default 22 port `Port 22`.
   - to restrict directly accessing the root user (`ssh root@hostname`) property `PermitRootLogin yes`.
   - to enable or disable password less access to the vm `PasswordAuthentication yes`.
   - to control the number of threads, and number of times the incorrect password can be provided during login
  
  
