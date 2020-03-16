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
### Profile file, which controls the environment for that user or shell.

To see what is the shell being used use `which shell` command.

Check for the profile file, in most of the case `bash` or `ksh` shell will be used. `csh` is not rare.

```
 $ ls -lrta ~/
 # or 
 $ cd ; ls -lrta;
 $ cat .bash_profile
 # this file runs .bashrc part of the profile itself
 
 # any modification to the .bash_profile file can be made effective by executing 
 $ . ~/.bash_profile
 
 # Else logout and login to the machine, if GUI exists open a new terminal.
```

### `env` command for displaying the default runtime environment details for that session

```
$ env
```

### Setting up an `alias` command in .bash_profile

```
$ vi ~/.bash_profile

alias l="ls -lrta"

## logout and login or execute . ~/.bash_profile
```

**`Note:`**
  - No hard rule whether to update the `.bash_profile` or `.bashrc`. 
  - There is hirearchy, any value in .bash_profile will been override if the same exists in .bashrc. 
  
# `touch` command to create a marker file, this helps in shell scripting to indicate that the process is completed.

```
$ touch filename

## if the filename exists the time stamp is updated.
## if the filename is NOT exists the new file is created and timestamp is updated.
```  

### `stdinput - 0`, `stdoutput - 1`, `stderr -2` redirection

Redirect the standard input and error of an command
```
 $ someprocess > standardout.out 2> standarderr.out
 
 # > is simple representation of 1> in Linux
```

Redirect to a null device
```
  $ someprocess > /dev/null
```
### Redirecting to `standardout` in shell script 
```
### sample.sh
#! /bin/sh
 echo "somecontent" >&1

## when executing the shell script
$ sh sample.sh >> test.out
$ cat test.out 
somecontent
```

### Permissions for a file by default is controlled by `umask` value.

```
$ umask
0022

## Determining how the umask is used for computing permissions of directory and file
## DIRECTORY:  777 - 022(umask value) = 111 111 111 - 000 010 010 = 111 101 101 (rwx r-x r-x) 
## FILE:       666 - 022(umask value) = 110 110 110 - 000 010 010 = 110 100 100 (rw- r-- r--)
```
`ls -lrt` command will list the owner and group of the file respective order. 

Moving ownership of file, only if the user has access to the groups can change the permission using `chown`.

With sudo the ownership can be changed among any available user.
```
$ id <username>
# above provides list of access 
[vagrant@chefnode1 ~]$ id vagrant
uid=1000(vagrant) gid=1000(vagrant) groups=1000(vagrant)
```

**`Note:`**
  - with `chown` the group name can also be changed. 
    - Use command like `$ chown <owner-name>:<group-name-that-needs-to-be-changed> filename-or-foldername`.
  
`$ chgrp ` can only be used to change group not the ownership.  

# `ACL` (Access Control List) using `setfacl` and `getfacl`
 - Best practice in production environment is not to use 777 permission on a directory or files.
 - To provide elevated privleges, we can use ACL.
 
 [Reference Topic](https://www.linux.com/tutorials/how-manage-users-groups-linux/)
 
##### To see if the file is already having a ACL, use the command `getfacl filename`
```
[vagrant@chefnode1 ~]$ getfacl test1.txt
# file: test1.txt
# owner: vagrant
# group: vagrant
user::rw-
group::rw-
other::r--
```

##### Managing the ACL in file and use the `setfacl -M` command. Check the help of the command.
For a single user below is the command
```
# setfacl -m <u/g/o>:<name-of-user>:<permission-to-grant> file

$ setfacl -m u:vagrant:r filename
# above will create additional user read access
```

# Creating user in Linux using `useradd`, sudo access is required to execute.

`useradd` and `adduser` are same, `adduser` inturn calls `useradd`
```
$ useradd centos8
# above command creates an user centos8

$ cat /etc/passwd
# above command displays the user information that was created.

[vagrant@chefnode1 ~]$ cat /etc/passwd | grep -i centos8
centos8:x:1001:1001::/home/centos8:/bin/bash
```

##### List default config for `useradd -D`
```
$ useradd -D
[vagrant@chefnode1 ~]$ useradd -D
GROUP=100
HOME=/home
INACTIVE=-1
EXPIRE=
SHELL=/bin/bash
SKEL=/etc/skel
CREATE_MAIL_SPOOL=yes
```

##### List the groups in the Linux using `$ cat /etc/group`
```
[vagrant@chefnode1 ~]$ cat /etc/group | grep -i centos
centos8:x:1001:
```
### Added new user and group, add the new user to that group using `groupadd` and `usermod`
```
[vagrant@chefnode1 ~]$ sudo groupadd vagrant
[vagrant@chefnode1 ~]$ sudo usermod -a -G vagrantbox centos8

## After adding the user to the groyp
[vagrant@chefnode1 ~]$ cat /etc/passwd
centos8:x:1001:1001::/home/centos8:/bin/bash

## Before adding check the above for cat /etc/passwd once creating the user.

[vagrant@chefnode1 ~]$ cat /etc/group | grep -i vagrant
vagrantbox:x:1002:centos8
```
##### `id` command on the username
```
[vagrant@chefnode1 ~]$ id centos8
uid=1001(centos8) gid=1001(centos8) groups=1001(centos8),1002(vagrantbox)
```

### logs in most cases will be under `/var/log` directory
### To find the list of logs that are modified last 24 hours
```
$ find . -name "*.log" -mtime -1
```

### To find the list of files that was modified 7 days ago
```
$ find . -name "*.log" -mtime +7

## to see the file dates
$ find . -name "*.log" -mtime +7 | xargs ls -lrt
```

### To search a string within the files in the log directory that was modified within last 24 hours using `xargs`

`xargs` - will pass the content of the file in the below case as input to `grep` command.
```
$ find . -name "*.log" -mtime -1 | xargs grep -i error 
## each file from the find will be passed as argument to grep command by xargs

[vagrant@chefnode1 ~]$ sudo find /var/log -name "*.log"  -mtime -1 | xargs sudo grep -i error
/var/log/tuned/tuned.log:2020-01-04 21:24:26,356 ERROR    tuned.plugins.plugin_cpu: Failed to set energy_perf_bias on cpu 'cpu0'. Is the value in the profile correct?
/var/log/tuned/tuned.log:2020-03-08 06:35:20,325 ERROR    tuned.plugins.plugin_cpu: Failed to set energy_perf_bias on cpu 'cpu0'. Is the value in the profile correct?
```

### To use the xargs within ls command
```
$ ls -1 /var/log/*.log | xargs grep -i error
```

### Challenge #1: From the below file, find the number of occurance of the words
```
# content of the input.txt file
1,2,3,ONE
1,2,3,TWO
1,2,3,ONE
1,2,3,FOUR
1,2,3,TWO
1,2,3,TWO
1,2,3,ONE
1,2,3,ONE
1,2,5,SEVEN

# Command for the challenge
$ cut -d, -f4 input.txt | sort | uniq -c
#output
      1 FOUR
      4 ONE
      1 SEVEN
      3 TWO

# uniq command compares the date with the next line, so better in most case to use it with sort pipe.

# using awk command
$ awk -F, '{print $4}' input.txt  | sort | uniq -c
      1 FOUR
      4 ONE
      1 SEVEN
      3 TWO
```
##### `awk` with if condition check
Print the lines from the input, where the comma delimited second number is matching 4
```
# input.txt 
1,2,3,ONE
1,2,3,TWO
1,2,3,ONE
1,4,3,FOUR
1,2,3,TWO
1,4,3,TWO
1,2,3,ONE
1,2,3,ONE
1,2,5,SEVEN

$ awk -F, '{ if($2 == 4) print}' input.txt
1,4,3,FOUR
1,4,3,TWO
```

