SELinux

- SELinux prevents any access that hasn't been specially allowed
- This protects applications from unauthorized access by other applications
- SELinux is not a firewall. Firewall is only for network traffic only.
- SELinux adds protection for bugs and zero-day exploits
- SELinux implements mandatory access control to go beyond discretionary access control


- SELinux securs parts of the operating system by using labels
- A label is applied to an initiator (user, processes), as well as a target object (files, ports)
   - The initiator is also referred as the source or domain
   - The target is defined as a type and class
- The labels are defined ni contexts
- A context has three common parts: 
   - user
   - role and 
   - type
- Out of these three, type is the most important part
- In the target, security levels can optionally be used
- To show context, the -Z option can be used with many commands
  ex.  ls -Zlrt, ps -Zaux

```
# ps -Zaux | grep ssh 
system_u:system_r:sshd_t:s0-s0:c0.c1023 root 30145 0.0  0.1 16808  9472 ?        Ss   22:31   0:00 sshd: /usr/sbin/sshd -D [listener] 0 of 10-100 startups
```
 - The process sshd is set with the context label `system_u:system_r:sshd_t` sshd_t - is type of context
 - s0-s0 is the secrity level
 - co.c1023 is the category

```
 # netstat -Ztelpun
 Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address   Foreign Address  State    User   Inode   PID/Program name     Security Context                                 
tcp6       0      0 :::22           :::*             LISTEN    0     75579   30145/sshd: /usr/sb  system_u:system_r:sshd_t:s0-s0:c0.c1023 
tcp        0      0 0.0.0.0:5000    0.0.0.0:*        LISTEN    0     52850   19301/conmon         unconfined_u:unconfined_r:container_runtime_t:s0 
```
  - Filterd to ssh, the key point is the port 22 where the security context set to sshd_t. 
  - SELinux has a policy defined with role  sshd_t to access the process sshd_t. If ssh port 5000 is used then that will be denied.


```
# ls -Z /var
system_u:object_r:var_t:s0 empty         system_u:object_r:var_log_t:s0 log                  system_u:object_r:tmp_t:s0 tmp
system_u:object_r:public_content_t:s0 ftp 
```

  - Note, the directory `ftp` is set to public_content_t type

- To perform an action, the initiator context must have access privileges to the target context.
- This is defined through rules in the SELinux policy
- For example, an Apache web server may run with the http_t context_type, the apache Documentroot is set to httpd_sys_content_t.
- If a rule exists in the policy to allow this type of access, access will be allowed
- Otherwise, access is denied with an AVC denied log message

## Terminology
- The context of the initiating process is called a `domain`.
- The context of the target resource is called the `type`.
- The object class to which access is provided in the type is called the `class`
   - File, directory and Socket are examples of classes
- The `domain` has permissions to access the type and class
- All are summarized in rules and looks like following:

```
allow <domain> <type>:<class> { <permissions> };
```
    - From the above format domain is the initiating process, type:class is the target


## Type Enforcement

- SELinux context lables are used for `type enforcement`
- In type enforcement, a specific domain gets permission to specific type and class
```
allow httpd_t httpd_sys_content_t: file { read };
```
  - Above means, the domain which are processes that are marked with httpd_t, can access files/directories to read which are marked with httpd_sys_content_t.
  - Above is one rule that can be used in SELinux type enforcement
- The rules are defined with permissions are stored in the policy
- If no rules exist for incoming requests, access will be denied


### Overview
- SELinux is more about protecting process. Theses processes wants to reach out to files or ports.
- In order to do protect the process SELinux adds a context label. Everything in SELinux will have context label. The context label identifies what are the process or files are all about.
- Example,
  - Say, we have a process with `httpd_t` context label, and we have a http port `http_port_t`. Together with the context label the permission are rendered. SELinux defines what exactly is `httpd_t` and `http_port_t` context label allowed to access.
- In working is reaching out to the target, SELinux will check if there is a `rule` for the target to access the source context, if the rule doesn't exists, we will see `AVC denied`. (AVC - Access Vector Cache)
- SELinux uses the rule from SELinux policy which is a rule book and contains ~8000 standard rules. It knows which is allowed from which context to which target context. 
- SELinux is a label management system, it doesn't care about the process or specific port or file. 
- SELinux is on top of the security that is defined by descritionary access control.

#### where are the SELinux attributes are stored
- SELinux rules and contexts are stored in the policy
- File-based attributes are also stored in the filesystem extended attributes
- To view the attributes use below command 
```
$ getfattr -m secruty.selinux -d /etc/hosts

getfattr: Removing leading '/' from absolute path names
# file: etc/hosts
security.selinux="system_u:object_r:net_confg_t:s0"
----

$ ls -Z /etc/hosts
system_u:object_r:net_confg_t:s0
```

- If the right procedure is used, file context are written to the 
policy and from there applied to the filesystem.
- Writting directly to the filesystem is possible but not recommended

### Mandatory Access control
- By default linux uses Discretionary Access Control (DAC)
- In DAC, files have owners and the owner of the file can grant permissions to other users on a system. (from the security admin point of view this is not advisible, since the admin doesn't have an overview of the access roles)
- This approach makes it difficult to maintain complete control over security settings
- As an alternate use Mandatory Access control
- Mandatory Access Control is a framework, security is centrally managed and cannot be changed according to the discretion of individual users.
- Madnatory Access Control is used in addition to Discretionary Access to make a system more secure

### Mandatory Access Control
- In linux there are different system exists for Mandatory Access control
- All system are backed by the Linux Security Modules (LSM) which are the part of the linux kernel
- `AppArmor` originated in 1998 to secure specific processes by creating profile for them
- `SELinux` was initally developed in 2000 by NSA and Redhat with the goal of harden Linux completely
- Out of two solutions, SELinux is more inclusive but also harder to implement
- The main Linux distribution offering SELinux are RedHat family distro and Gentoo.

#### Understanding SELinux (MAC) and DAC  
- Regular permission part of DAC are always evaluated first. If the regular permission is denied it will be denied.
- SELinux Mandatory Access Control is used to further fine-tune access permissions
- Thus, a use that doesn't have filesystem permissions, won't get access regardless the SELinux policy settings


Lab:

Check the permission of /etc/crontab file

hint: use below command

ps -Zaux | grep corn

ls -Z /etc/crontab

ls -lZ /etc/crontab
 [Note, the -l in ls provides the discretionary access info]

### Enabling SELinux
 - Managing state and modes on Red Hat 
 - Installing SELinux on Ubuntu
 - Understanding Policies

#### Modes
- SELinux supports states and modes
- SELinux is either enabled or disabled in the Linux Kernel.
- Changing between enabled and disabled state, requires system restart
- On a system where SElinux is enabled, you can toggle between enforcing and permissive mode
  - In enforing mode, SELinux is fully operational and blocks unauthroized access
   - In permissive mode, SELinux doesn't block anything, write audit events to the aduit log. This mode can be used as a learning mode, taking a look at the logs on what exactly SELinux doing, and analyze why it blocking the requests.
- While analyzing and troubleshooting SELinux, ensures that the aduitd process is operational.

### State
- Managing states
   - From the Grub boot menu following options are available
      - selinux=[0|1]  => to enter disabled or enabled state. Setting to 0 disables, but requires reboot to be effective
      - enforcing=[0|1] => to set permissive or enforcing mode. This is not needed to be done on the boot sequence, this can be done after login
   - On a running system, use `seinfo` or `getenforce` to get information about current state and mode.
   - Use `setenforce` to toggle between enforcing and permssive mode.

Demo:
 sequence of step:
    reboot
    enter GRUB menu -> selinux=0
    Boot, using ctrl+X
    seinfo
    getenforce
    setenforce

Step 1:
In Centos/RedHat, reboot the machine if the GRUB menu is displayed (if the GRUB menu doesn't popup press Esc button)
Use `e` to edit the GRUB config. For example the edit screen looks like 
```
load_video
set gfxpayload=keep
insMod gzio
linux ($root)/vmlinuxz... shkernel=... .lv=cs/root rd.lvm.lv=sc/swap rhgb quiet
...
```

In the above config on line linux command we can delete the `rhgb quiet` (which is at the end, by removing this we could see what is happening in the logs during reboot) and add `selinux=0`

Step 2:
Use `seinfo`, if the package is not installed, install it. Be in root user.
The seinfo displays the policy info 
If the `setenforce` returns SELinux disabled, reboot the machine again.

If `getenforce` returns `Enforcing`, then issue `setenforce permissive`. 

#### Installing SELinux in Ubuntu

- Ubuntu doesn't use or support SELinux; Ubuntu uses AppArmor 
- In Ubuntu it requires multiple modification to get SELinux working.
- Use the Ubuntu server with below information.

Steps:

 sudo systemctl disable --now apparmor
 sudo apt update && sudo apt upgrade
 sudo apt install policycoreutils selinux-utils selinux-auditd -y
 sudo selinux-activate
 reboot
 Access GRUB and add enforcing=0
 cat /var/log/audit/audit.log | audit2allow -m initial > initial.te
 checkmodule -M -m -o initial.mod initial.te
 semodule_package -o initial.pp -m initial.mod
 semodule -i initial.pp

 Note, we cannot have SELinux and AppArmor on the same system.

 #### Understanding Policies

- The SELinux policy contains rules that allow domains to access specific types
- if an activity is not allowed in the policy, access will be denied
- As a result, to enable a distribution to work with SELinux, rules need to be added
- As a quick fix, audit2allow can be used to convert all deny messages into policy rules which next are loaded with semodule
- RedHat comes with a very inclusive target policy
- In this policy, a wide range of modules are loaded even for services that are not currently installed
- As a result, you won't need to configure a lot if default configuration used
- On unsupported Linux distributions, a perfectly matching policy is often not available. In this case we can use Refpolicy.
- Refpolicy is a generic policy provided by SELinux community
- https://github.com/SELinuxProject/refpolicy
- Using refpolicy allow users to compile their own policy from scratch.

Lab: 

- Start your Linux distrubtion with SELinux in disabled state, using a GRUB2 kernel parameter
- Next, add a user
- Restart in normal mode and check what happens
- Once logged in, switch to permissive mode. Next, switch back to the enforcing mode

## Context labels
- display context label
- how to set the context label
- using audit.log to examine

### Labels
- SELinux uses labels to manage security settings
- Labels can be set on initiator like processes and users and targets like files and network ports
- In a label, following elements are used
   - user: the SELinux user involved (user is not essentials part of the label)
   - role: the SELinux role that is used
   - type: the type, which defines set of permissions that belongs to the label
   - an optional security clearance 
   - an optional category 
#### To show labels
 - many commands use the -Z option in it
 - ps -Zaux lists all process with SELinux context label
 - ls -Z shows labels on files and directories

    - when using ps -Zaux the user process has the label unconfined_t

#### When to set the context labels
- A good SELinux policy provides standard labels for standard situations
- It implies that if distribution uses a good policy, no need to set anything in standard situations
- Not every distribution comes with good policy
- If non-standard elements are set, you'll have to set the appropriate label to allow access
- In many cases we don't need anything 

#### audit log
- SELinux messages are sent to `auditd` process
- This process writes messages to `/var/log/audit/audit.log`
- SELinux related messags are marked with AVC
- Use the audit log to identify issues with labels that are currently set
- sometime `auditd` is not always installed and enabled by default

For example, to troubleshoot say the `getenforce` to be permssive.
use `grep AVC /var/log/audit/audit.log`, ideally there should be some message if we don't see it indicates everything is fine.

To simulate an AVC error, edit `vi /etc/ssh/ssh_config`, update the config `Port 22` to different port say 2022 and restart the process using `systemctl restart sshd`.
Now, issuing grep AVC command on audit log should provide info.

#### Understanding context labels
- Context labels are set in the SELinux policy
- Files created in a directory inherit the directory context label
- When a file is moved, its context label moves along
- When a file is copied, it gets context label accordning to new location
- Conext inheritance is default
- Even if a file has a specific context, after it is created in a directory it first inherit the context from parent directory
- The specific context will only be created later when `restorecon` is used to re-apply context to the entire filesystem

Demo: for inheritence

- below command is used to set the context in the policy for the file. Note, even if the file doesn't exist the SELinux policy will know the context label public_content_t to be set when it is created
semanage fcontext -a -t public_content_t /etc/bogus

- issue below command we could see the file is not created
ls -Z /etc/bogus 

- create file with below command 
touch /etc/bogus

- below command will display the context label default is applied like `etc_t`
ls -Z /etc/bogus

- below command to be applied it will set the context label to the filesystem 
restorcon -V /etc/bogus

- Lab

  - Install Apache webserver (httpd)
  - configure directory `/web` as its document root
  - start webserver and access document using curl on localhost
  - check SELinux related messages have been logged and alspo context labels

  in centos distro use

  $ dnf install -y httpd
  $ vi /etc/httpd.confg <--- edit the document root to /web, and edit the `Directory /web` folder
  $ mkdir web
  $ cd web
  $ vi index.html <-- add some content
  $ system restart httpd
  $ curl localhost
   --- the message will not be dispalyed. and the server error nessage will displayed
   --- check the logs using `grep AVC /var/log/audit/audit.log`, if you see permissive mode 0 that means no rule to allow directory access
   -- we could see source allows httpd_t to access default_t directory
   -- in enforcing mode this denied

$ setenforce pemissive.
$ curl localhost --> we see hello world (html content)
- we will use semanage to add the rule

## managing context level
- how to find the right context 
- setting context on file
- setting context on port
- using customizable types
- lab above to add rule

### Finding the right context 
- Most service work with a default environment. We can check the context set on the environment and use it on the non-default env.
- use the man page check selinux-policy-doc package
- apply instruction generated by `sealert`

The httpd provides the default document root, we can take a look at it using `ls -lZ /var/wwww` the html document we get the non default rule.

### setting context
- To manage file context, the context should be writtern to the policy and from there applied to the filesystem.
- This approach makes it possible to fix mislabeled filesystems, using simple command
- use `semanage fcontext` to change the context in the policy
- we can use `restorecon` to apply the context from policy to the filesystem

```
$ mkdir /files
$ touch /files/file{1..10}
$ ls -Zd /files
$ semanage fcontext -a -t public_content_t "/files(/.*)?"  # applies the directory and files
$ ls -Zd /files
$ restorecon -Rv /file

$ cd /etc/seliniux/targetd/contexts/files/
-- it contains file_context.local.
$ cat file_context.local  #this file is generated by selinux don't edit
```

##### Setting context to the filesystem only
- use `chcon` to set the context to the filesystem only
- This can be convenient in exceptional cases like HA cluster resources or temprory settings
- If `chcon` is used to change the context on a file that hs it's own context set in the policy, executing `restorecon` will relabel the file
- If autorelabel which is triggered automatically in some cases this might wipe out the changes applied by `chcon`. that is the reason we shouldn't use it.
- Better use `setmanage`

```
$ touch /tmp/chconfile
$ chcon -t httpd_sys_content_t /tmp/chconfile
$ ls -Z /tmp/chconfile
$ restorecon -Rv /tmp
$ ls -Z /tmp/chconfile /etc/hosts
$ chcon -t httpd_sys_chotent_t /etc/hosts
$ restorecon -v /etc/hosts
```

### managing ports
- Labeling ports
  - Port context is only managed in the policy,
  - There is no need to use `restorecon` to apply port context from the policy to anywhere
  - Use `semange port -a -t http_port_t -p tcp 82` to set port context

```
$ vim /etc/ssh/sshd_config includes the default port 22
- we can edit the port 
$ systemctl stop sshd
$ systemctl start sshd
$ getenforce -- outputs the mode
$ grep AVC /var/log/audit/audit.log
$ setenvforce enforcing
-- now if we stop the sshd and start sshd, we could see error
-- take a look at the Se manage port documentation
$ man semanage-port
-- we could see example
$ semanage port -a -t ssh_port -p tcp 2022
$ systemctl restart sshd  -- now this work now.
```

### Customizable types
- A customizable type is a type that will persist through a standard `restorecon`
- They are commonly used on files that don't have fixed location
- Since the file location is not fixed, it's hard for the policy write a rule for them
- a list of customizable types is kept in `/etc/selinux/*/contexts/customizable_types`
- Thus, cutomizable types will not be relabled

```
$ vim /etc/selinu/targeted/customizable_types
-- we could see list of customizable types, and container_file_t is one type
$ touch /tmp/customizable1
$ ls -Z /tmp/cutomizable1
$ chcon -t container_file_t /tmp/customizable1
$ ls -Z /tmp/cutomizable1
$ restorecon -Rv /tmp
$ ls -Z /tmp/cutomizable1
```

#### configuring a non-default apache documentroot

```
- check if httpd is running
$ systemctl status httpd

$ grep Document /etc/httpd/conf/httpd.conf
 -- we could see  the document root is "/var/www/html"

$ vi /var/www/html/index.html  
-- add some text here

$ getenforce

$ curl localhost
-- should return the text value

$ vi /etc/httpd/conf/httpd.conf
-- change the path for document root to `/web`

$ systemctl restart httpd
-- we could see the httpd server start fails
$ systemctl status httpd
-- we could see log indicating web is not readable
$ vi /web/index.html
-- add some htlm content

$ curl localhost
-- we will see error page from the server

-- to troubleshoot the permission issue we use below command
$ setenforce permissive

-- issue below command
$ curl localhost

$ vi /etc/httpd/conf/httpd.conf
-- set the documentary root with /web
<Directory "/web">
 ...
</Directory>

-- once the config is updated we restart server
$ systemctl restart httpd

-- to repeat the last command of curl use 
$ !cu
-- above will use curl localhost
-- still it will not work since we have our own directory and context not set

-- to grep the AVC record
$ grep AVC /var/log/audit/audit.log
-- should see the deined, we have prodcess httpd_t accessing the target default_t
-- option 1. To write the rule to write the httpd_t to access defult_t. This is not a good idea.
-- option 2. We need to change context on custom rule

-- we will get the context lable of the default one `ls -Zd /var/www/html`
httpd_sys_contect_t will be the default on httpd context

-- to check the document use 

$ man semanage-fcontext
-- example section will give it below command 

-- -t set type.

$ semanage fconext -a -t httpd_sys_content_t "/web(/.*)"

$ ls -Zd /web

-- lets check this 
$ cd /etc/selinux/targeted/contexts/files/
-- could see the file file_ciontexts.local

$ cat file_contexts.local
--- we could see 
/web(/.*)? system_u:object_r:httpd_sys_content_t:s0
-- the ls -z command didn't reflect this change on the web folder
-- this is because it i not applied yet. 
-- we can apply using below command

$ restorecon -Rv /web
-- should be able to see some content

$ curl localhost
-- message from server
```

Lab: running SSH on port 443

```
$ man semanage-port
-- we can get example of command 

$ semanage port -a -t ssh_port_t -p tcp 443
-- the above command will provide error message
valueError: Port tcp/443 already defined
-- this is because 443 is an default port for ssl

$ man semnanage-port
-- get the command from manual
-- we have to update the rule we need to use `-m`

$ semanage port -m -t ssh_port_t -p tcp 443
-- instead of -a we use -m to modify

$ vim /etc/ssh/sshd_config
-- edit the port from 22 to 443

$ systemctl restart sshd

$ netstat -Ztulpen 
-- the output can be found to see tcp port 443

```

### Using Booleans
