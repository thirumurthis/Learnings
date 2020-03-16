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
 
 **`NOTE:`**
   In `/etc/conf/sshd_conf` file if the `ListenAddress 0.0.0.0` represents that from any server ppl can login.
   When we specify any specific address, only that ssh works from that specific ip.
 
 
### Accessing Linux VM from remote client

#### In the **`Client machine`** - that wants to connect to remote VM, perform below step:
  - Navigate to .ssh directory using `$ cd ~/.ssh` 
  - Validate the permission of the `~/.ssh` directory using `ls -lrta` command (refer below notes for fixing the permissions if not 700)
	- use `$ ssh-keygen` command which will create `private` and `public` key. 
	  - By default the file name would be `id_rsa.pub` and `id_rsa`, but user can define/specify the name of file along with the path
  - use passphrase for more security.
    - `ssh-keygen` can take options to generate key using different algorithms
	
#### In **`Remote machine`**, the user wanted to connect.
  - Login and copy the public key content `id_rsa.pub` to the `~/.ssh/authorized_keys` directory. Append to it in case if there some content already exists. 
  
#### To copy the content to remote machine below are different ways:- from client machine,
    - use `scp file username@hostname:~/path-to-where-file-to-be-copied`. 
	- use `ssh-copy-id -i /path-to-private-key username@hostname`
	- copy paste to the `authorized_keys` directly, appending to existing content in the file.

**Note:**

  `authorized_keys` and `id_rsa` (private keys) best practice is to have `600` permission. Though it works with other permissions.
  `.ssh` directory should have `700` permission.
	 
#### From **`Client machine`** use private key to login to **`Remote machine`**
```
 $ ssh -i <private-key-file> user@hostname
 ## hostname can also be DNS name if setup for the linux remote server
 
 ## Example 
 $ ssh -i ~/.ssh/id_rsa.pub docker@default
```

#### Quick overview
```
# from client side
$ cd ~/.ssh
$ ssh-keygen 
## input the info
$ ssh-copy-id -i ~/.ssh/id_rsa username@hostname
```

**Note:**
  - when connecting using ssh if there is message "permissions 0655 for <~/.ssh/>private-key is open" then fix the permission for best practice.

In the Linux server verify the folder under home directory `~/.ssh` has 700 permission. It is necessary for ssh to have this permission.

```
# use below command to change permission
$ sudo chmod 700 ~/.ssh
# or 

$ sudo chmod o+rwx ~/.ssh
##  the g+rwx 
g - group
o - other
a - all 
## Example:

chmod u=rx file (Give owner rx permissions)
chmod go-rwx file (Deny rwx for group, others)
chmod a+rwx file (Give rwx to everybody)
chmod g+rx,o+x file (Use comma to combine permssions for different role (u-owner;g-group; o-other)

```
##### Demo:
```
docker@default:~$ ls -lrt
total 0
-rwx------    1 docker   staff            0 Mar 15 21:19  test.txt

docker@default:~$ chmod u+rwx,g+r,o+r test.txt

## After
docker@default:~$ ls -lrt
total 0
-rwxr--r--    1 docker   staff            0 Mar 15 21:19 test.txt 
```

Usually the using the command `$ cd ` will navigate to home directory.

But in rare cases the admin might have pointed home directory to different directory, to find the home directory we can use the below steps.
This can be done when creating the user account.

```
$ cat /etc/passwd | grep <user-name>

## Example
docker@default:~$ sudo cat /etc/passwd | grep docker
docker:x:1000:50:Docker:/home/docker:/bin/bash

## /home/docker is the home directory in this case
```

# Automation using `ssh` setup.
If say, there are n number of nodes and as admin wanted to check the RAM usage using `free -h` command.

Once the ssh is setup on all the nodes, then we can use a shell script like below to execute the command.
```
#! /bin/sh

ssh username@hotsname1 "free -h"
ssh username@hotsname2 "free -h"
```

##### Troubleshoot the connectivity in case of ssh

**i.** Using ssh, there is no response for a long time, then check if the fire wall has the port opened for inbound traffic.

  - Sometimes firewall is setup to block unintended traffic to the server.(In AWS EC2 the firewall is called `security group`.)  
  - `Inbound` => Traffic that is going into the machine. Connectivity issue is usually in inbound, since we are trying to connect to the machine.
  - `Outbound` => Traffic that is going out of the machine

**ii.** If the inbond traffic rule is set and enable, then try using `ping ip-address/hostname` command. If ping is responding with 0% packet loss, then if `telnet ip-address/hostname` to see any response. `telnet` is NOT enabled in most of the cases.

**`Note:`**
    `mc hostname/dns-name port#` - alternate command for `telnet` since it is not installed in most of the machine. [cygwin mc prompts a different window.]

**iii.** If the connection is established from a client, and for some reason a new session is not able getting established. Then there is a possiblity that the `sshd` service is down on the server.

##### In the server see if the sshd service is running and restart.

```
$ sudo systemctl status sshd
# output:  
  [vagrant@chefnode1 ssh]$ sudo systemctl status sshd
● sshd.service - OpenSSH server daemon
   Loaded: loaded (/usr/lib/systemd/system/sshd.service; enabled; vendor preset: enabled)
   Active: active (running) since Sun 2020-03-15 23:31:42 UTC; 13min ago
     Docs: man:sshd(8)
           man:sshd_config(5)
 Main PID: 764 (sshd)
    Tasks: 1 (limit: 2844)
   Memory: 7.5M
   CGroup: /system.slice/sshd.service
           └─764 /usr/sbin/sshd -D -oCiphers=aes256-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,aes256-cbc,aes128-gcm@openssh.com,aes128-ctr,aes128-cbc -oMACs=hmac-sha2-2>
   Mar 15 23:31:41 chefnode1 systemd[1]: Starting OpenSSH server daemon...
   Mar 15 23:31:42 chefnode1 sshd[764]: Server listening on 0.0.0.0 port 22.
   Mar 15 23:31:42 chefnode1 sshd[764]: Server listening on :: port 22.
   Mar 15 23:31:42 chefnode1 systemd[1]: Started OpenSSH server daemon.
   ...

# Restart the sshd if not running
$ sudo systemctl restart sshd

# To Stop the sshd service
$ sudo systemctl stop sshd
```

