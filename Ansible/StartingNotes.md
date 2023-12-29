#### Key terminology:

`Inventory` file : most of the hosts info goes in here
 - host groups
 - The default grouping is called `all`. All the hosts in the inventory file is placed under the groupping all.
Sample inventory file 

```
10.0.1.[1:3]   # to use ranges 

[all_hosts]
server1 ansible_ssh_host=10.0.1.30 # below is providing the alias for ip address
server2 ansible_ssh_host=10.0.1.40 
server3 ansible_ssh_host=10.0.1.50

[webserver]
server1

[dbserver]
server2

[backupserver]
server3 backup_file=/tmp/bckp   # backup_file is variable passed with a value

[all:vars]                      # variable applied to all the host
temp_path=/tmp/allapp  

[webserver:vars]                 # variable applied to webserver group
tmp_path=/tmp/webapp

```

`Playbook`: a simple yaml file with the details to install modules and info to server/host.
- Example

```yml
---
- hosts: webserver
  become: yes    # assuming root previleges equivalent to sudo
  
  tasks:
  - name: uninstall stress and sync apt index
	  apt:
	    name: stress
		state: absent  # to uninstall if pacakge is present
		update_cache: yes
```
Check the ansible documents for below 
`module` - different modules that can be used example - ping, file, etc.
`plugin` - Different from modules, refer doc

#### Ansible commands:

- The WSL2, we can install the ansible, using `apt` and `pip` check the doc for mode info.
- From the WSL2 command prompt apply below command:

-- command to print the files from the specific path 
```
$ ansible localhost -m find -a "paths=~ file_type=file"
 ( -m stands for module)
```
 
-- command to gather facts  using gather_facts module
```
$ ansible localhost -m gather_facts
```

-- command gather facts from all the host provided in the inventory file
```
$ ansible all -m gather_facts
(all is the group from the inventory file)
```
-- command to update the apt-get pacakage in ubuntu
```
$ ansible localhost -m apt -a update_cache=true 
```

-- command to update apt-get require root or sudo access so we use below command --become etc.
```
$ ansible localhost -m apt -a update_cache=true --become --ask-become-pass
(for command to be successful check the firewall setup, the wsl should work seemlessly)
```

Using playbook, below is the content of playbook and store it as stress.yml

```yaml
---
- name: "lesson 1 playbook"
  hosts: localhost
  
  tasks:
    - name: "rechability test"
      ping:
      
    - name: "install stress"
      apt:
        name: stress
        state: present
        update_cache: yes
```

To run the command, navigate to the path and run below command

```
$ ansible-playbook stress.yml --become --ask-become-pass
```

Note: 
 - To print the config in yaml file on the ansible-playbook output use below config .cfg file

Below is the anisble.cfg file configuration changes
```
[defaults]
# Yaml callback plugin
stdout_callback = yaml
# use of stdout_callback for ad-hoc commands only
bin_ansible_callbacks = True
interpreter_python = auto_silent
```

ad hoc command

```
$ ansible all -m ping -i <inventory-file>
```

Info:- 
- by using use `--limit` option with hostname or ip to apply ansible playbook to specific node or hostname
- by using `package` module, the apt or yum package manager will be determined by the OS, for debian based system the pacakage manager is `apt` and for fedora it is `dnf` or `yum`.
- `package` module based on the underling os will choose which pacakge manger to use based on the availability in os.

-------------------

Using VARIABLES and CONDITIONS:

Note:- some time in conditional we don't use `{{}}` for variable usage, example check the `when: ` conditional example below.


```yaml
---
- name: "variables and conditions in ansible"
  hosts: localhost
  gather_facts: no
  vars:
    srvloc:
       srv1: [1,2]
       srv2: [3]
       srv3: [4]
    count: 4
    print_flag: true
    test_flag: false
  
  tasks:
    - debug:
       msg: "hello : {{ srvloc.srv1|type_debug }}"  # prints list - type of the variable
    - debug:
       msg: "hello : {{ srvloc.srv1 }}"  # prints [1,2]
    - debug:
       msg: "hello : {{ count|type_debug }}"  # prints int 
    - debug:
       msg: "hello : {{ count }}"  # prints 4
    - debug:
       msg: " print_flag set true"
      when: print_flag # we can use == true and make a note on the intendation
    - debug:
       msg: " test_flag set true"
      when: test_flag == true 
    - debug:
       msg: " test_flag set true"
      when: not test_flag == true  ## use the not instead of !=
```
-----------------

Using TAGS:

```yaml
---
- name: "tags in ansible"
  hosts: localhost
  #  become: yes  # this is required if we are installing the pacakge requires password to be provided.
  gather_facts: no
  vars:
    demo: "for tags in ansible"
  tasks:
    - debug:
       msg: "for : {{ demo }}"
	  tags: printinfo
	- name: install NTP 
	  apt:
	    name:
		  - ntp
		state: present
	  tags: ntp
	- name: Start NTP service
	  service: name=ntp state=started enabled=yes
	  tags: ntp_start
```

when using the ansible-playbook command we can use it like below,

```
$ ansible-playbook tags_example.yaml --tags printinfo
```

- output
```
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

PLAY [tags in ansible] ******************************************

TASK [debug] ****************************************************
ok: [localhost] => {
    "msg": "for : for tags in ansible"
}

PLAY RECAP ******************************************************
localhost                  : ok=1    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

```
$ ansible-playbook tags_example.yaml --tags stress
```

- To list the tags within the playbook use below command

```
$ ansible-playbook tags_example.yaml --list-tags
```

- output 

```
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

playbook: tags_example.yaml

  play #1 (localhost): tags in ansible  TAGS: []
      TASK TAGS: [ntp, ntp_start, printinfo, stress]
```

- To run every task except ntp_start the below command can be used

```
$ ansible-playbook tags_example.yaml --skip-tags ntp_start
```

- How to run the tasks in the control node using the connnection local inside the playbook.

```yaml
---
- name: "running locally in ansible"
  hosts: localhost

  tasks:
    - name: Fetch local info
      debug:
         var: hostvars[inventory_hostname]
         verbosity: 1
```

When running the above yaml with the below command (no -v) doesn't print anything

```
$ ansible-playbook localhost_example.yaml
```

- with below does print the details of the localhost

```
$ ansible-playbook localhost_example.yaml -v

Note:- 
 we can add -v , -vv, -vvv or -vvvv, there are 4 levels of verbosity
```

Another approach to use `connection :local`

```yaml
---
- name: "running locally in ansible"
  hosts: localhost
  connection: local   # this bypass the ssh connection check a bit performance benefit can be acheived.
  tasks:
    - name: Fetch local info
      debug:
         var: hostvars[inventory_hostname]
         verbosity: 1
```

- Command line to start from specific task

```
$ ansible-playbook tags_example.yaml --start-at-task 'install NTP'
```

- command line to be interactive for each tasks

```
$ ansible-playbook tags_example.yaml --step
# above command will prompt for y/n for each step/tasks in the playbook
```

- Using variables in inventroy and using it in playbook, passing throug command line.

```yaml
# inventory file
# default path would be /etc/ansible/hosts

[all:vars]
tmp_path=/tmp/test

```

```yaml
# playbook file
---
- hosts: localhost
  tasks:
     - name: Create a file
	   file:
	     dest: '{{tmp_path}}'
		 state: '{{file_state}}'
	   when: tmp_path is defined
```

- command to run the above file 

```
$ ansible-playbook -i inventory_example.cfg variable_approachs.yaml -e file_state=touch 
```

- output

```
ansible-playbook -i inventory_example.cfg variable_approachs.yaml -e file_state=touch -v
No config file found; using defaults
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

PLAY [localhost] **************

TASK [Gathering Facts] ********
ok: [localhost]

TASK [Create a file] **********
changed: [localhost] => {"changed": true, "dest": "/tmp/test", "gid": 1000, "group": "t", "mode": "0777", "owner": "t", "size": 0, "state": "file", "uid": 1000}

PLAY RECAP ********************
localhost                  : ok=2    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

- the same can we executed with file_state with different value like `absent` which will remove the file

```
$ ansible-playbook -i inventory_example.cfg variable_approachs.yaml -e file_state=absent
```

------

Dynamic inventory file

- when working with cloud often we might need a dynamic inventory file, which for a AWS environment we can use aws plugin.

```
$ sudo apt install python3-pip
$ sudo pip3 install boto 
$ cd /opt/ansible/inventory/
$ touch aws_ec2.yaml
```

- aws_ec2.yaml file
```
# the keys can be obtained from AWS identity management of EC2 instance.
plugin: aws_ec2
aws_access_key: <key>
aws_secret_key: <secret>
keyed_groups:
 - key: tags
   prefix: tag
compose:
  ansible_host: private_ip_address   # this indicates we will be using the private_ip_address field value when we connect to aws

```

- modify the ansible configuration which is located default /etc/ansible/ansible.cfg
  
```
# under the ansible.cfg file under the `[inventory]` section add below 
enable_plugins = aws_ec2
```

With the above configuration, we will be able to get the hosts info dynamically from the EC2 

- To test this we can use below command

```
$ ansible-inventory -i /opt/ansible/inventory/aws_ec2.yaml --list
# above command will request aws for the hosts and provides the output based on the repsonse.
# includes the host information
```

------------------------------

using templates in ansible using Jinja2
Jinja2 is a popular templating tool in python

The file extension is `.j2`.

Create a tempplate file with below content, and save it as hello_template.j2

```j2
Message from node:
 {{ inventory_hostname }}

Today's message:
{{ custom_message }}
```

- Below is the way to use the j2 template in the playbook.

```yaml
---

- name: "template usage"
  hosts: localhost
  vars:
    custom_message: "This is template usage example."

  tasks:
    - name: Simple debug task
      debug:
        msg: "template example"
    - name: Create html file
      template:
        src: hello_template.j2
        dest: /mnt/c/learn/ansible/lesson_02/test.html

```
-----------

Using LOOPS 

```yaml
---

- name: "lesson 2 playbook"
  hosts: localhost
  vars:
     direction: [north,west,south,east]

  tasks:
  - name: List direction
    debug:
      msg: "print this message for each direction - {{item}}"
    with_items: '{{direction}}'

```

- output 

```
$ ansible-playbook loop_example.yaml
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

PLAY [lesson 2 playbook] ***********

TASK [Gathering Facts] *************
ok: [localhost]

TASK [List direction] **************
ok: [localhost] => (item=north) => {
    "msg": "print this message for each direction - north"
}
ok: [localhost] => (item=west) => {
    "msg": "print this message for each direction - west"
}
ok: [localhost] => (item=south) => {
    "msg": "print this message for each direction - south"
}
ok: [localhost] => (item=east) => {
    "msg": "print this message for each direction - east"
}

PLAY RECAP *************************
localhost                  : ok=2    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

- `apt` module supports loops when provides the list in the name like below 

```yaml
---

- name: "package playbook"
  hosts: localhost
  vars:
     packages: [apache2,stress,ntp]

  tasks:
  - name: install packages
    apt:
      name:  {{packages}}  # this is the variable name
	  state: latest

```
- looping over the `dictionary object` 

```yaml
---
- name: "lesson 2 playbook"
  hosts: localhost
  vars:
     vehicles:
        sedan:
          model: toyota
          type: corola
        suv:
          model: honda
          type: crv
        truck:
          model: toyota
          type: tacoma
  tasks:
  - name: List vehicles
    debug:
      msg: "print vehicles - {{item.value.model}} && {{item.value.type}}"
    with_dict: '{{vehicles}}'

```
- output 

```
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

PLAY [lesson 2 playbook] ***********

TASK [Gathering Facts] *************
ok: [localhost]

TASK [List vehicles] ***************
ok: [localhost] => (item={'key': 'sedan', 'value': {'model': 'toyota', 'type': 'corola'}}) => {
    "msg": "print vehicles - toyota && corola"
}
skipping: [localhost] => (item={'key': 'suv', 'value': {'model': 'honda', 'type': 'crv'}})
ok: [localhost] => (item={'key': 'truck', 'value': {'model': 'toyota', 'type': 'tacoma'}}) => {
    "msg": "print vehicles - toyota && tacoma"
}

PLAY RECAP *************************
localhost                  : ok=2    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```
-----------

Ansible CHECK mode
  - the check option on the ansible is power full and lets us know if there are any issues in the playbook.
```
$ ansible-playbook template_usage.yml --check
```

----------

ROLES:
 - provides methods to orgainze and sharing the complex ansible orchestration.
 - allows us to load various files automatically like variables, handlers and other ansible components based on know file structure
 
Simply, Roles are in a way to structure the ansible playbooks.
The default folder contians yaml file in which we can provide variables with default values. To overrider these values, we can use vars folder yaml file or in command line with -e.

- file structure looks like below 
```
 roles/ 
   common/
       tasks/
	   handlers/
	   library/
	   files/
	   templates/
	   vars/
	   defaults/
	   meta/
```

 - Ansible galaxy is the website dedicated to shared and host the roles and create roles.
 - Ansible galaxy client is already configured in the control node by default.
 - In the `/etc/ansible/` default path create a directroy for `roles`, this is where ansible will be looking for roles.
 - we can create the role anywhere in the system and specify the path of the roles folder in playbook.
 - The ansible.cfg file can also be edited for roles.
 
 To create the role, 
 - Create the roles directory
 - navigate to the directory, and issue below command
   $ ansible-galaxy init testrole01
 
Once the roles is created we can add it to the playbook like below

```yaml

- name: "template usage"
  hosts: localhost
  roles:
  - testrole01
```
 
------------

Ansible SECRET and VAULTS
 - Ansible supports encryption with AES encryption

- to create an encrypt file

```
$ ansible-vault create test.yaml
# this command prompts for a password, and confirm password
``` 

- If we run the file with ansible-playbook we will see an error 

```
$ ansible-playbook test.yaml
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'
ERROR! Attempting to decrypt but no vault secrets found
```

To run the file if that is a playbook use `--ask-vault-pass`

```
$ ansible-playbook test.yaml --ask-vault-pass
```

- To encrypt an existing file we can use 

```
$ ansible-playbook encrypt <file_that_needs_to_be_encrypted>
```

- How to edit the encrpyted file

```
$ ansible-vault edit test.yaml
```

### How to using secrets.
  - To encrypt only string.

1. we create the encrypted string using below command
```
$ ansible-vault encrypt_string --vault-id @prompt mysuperwonderfulsecret
```

- output 
```
New vault password (default):
Confirm new vault password (default):
Encryption successful
!vault |
          $ANSIBLE_VAULT;1.1;AES256
          33343163346264356662396431366434383132303538643365383538356235626232333438623961
          6535663165383465393439643935646664376166616366360a613536643038363264346536386338
          34666561353362623235393530616637623836336232623532386563653831343239623032363561
          6537333035316665610a623737653134626236623039363961313261396238366137393236313765
          66643965613834343838623432633264396535663833393934613931383463323232
```

- Using the secret string in ansible playbook

```yaml
---

- name: "secerts"
  hosts: localhost
  vars:
     super_secret: !vault |
                   $ANSIBLE_VAULT;1.1;AES256
                   33343163346264356662396431366434383132303538643365383538356235626232333438623961
                   6535663165383465393439643935646664376166616366360a613536643038363264346536386338
                   34666561353362623235393530616637623836336232623532386563653831343239623032363561
                   6537333035316665610a623737653134626236623039363961313261396238366137393236313765
                   66643965613834343838623432633264396535663833393934613931383463323232

  tasks:
    - name: secret usage
      debug:
        var: super_secret  # print value of secret

```

- To run if we use without the valut password we see the exception message

```
$ ansible-playbook secret_str.yaml
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

PLAY [secerts] **************

TASK [Gathering Facts] ******
ok: [localhost]

TASK [secret usage] *********
fatal: [localhost]: FAILED! => {"msg": "Attempting to decrypt but no vault secrets found"}

PLAY RECAP ******************
localhost                  : ok=1    changed=0    unreachable=0    failed=1    skipped=0    rescued=0    ignored=0
```

To decrypt use below command

```
$ ansible-playbook secret_str.yaml --ask-vault-pass
```
------------

- Working with network in ansible

- Below is the play book to print the ip address as prefix and manipulation with build utils of ansible

```yaml
---
- hosts: localhost
  connection: local

  vars:
    gateway_ip: 10.10.10.1
    networkmask_ip: 255.255.255.224

  tasks:
  - set_fact: ip1="{{gateway_ip}}/{{networkmask_ip}}"
  - set_fact: ip2="{{ip1| ansible.utils.ipaddr('network/prefix')}}"
  - debug:
     msg: "network is {{ip2}}, gateway is {{gateway_ip}}"
  - set_fact: ip3="{{gateway_ip|ansible.utils.ipaddr('int') + 1}}"
  - set_fact: ip4="{{ip3|ansible.utils.ipv4('address')}}/{{ip2|ansible.utils.ipaddr('prefix')}}"
  - debug:
     msg: "incremented host address is {{ip4}}"
```

- To run this, it requires netaddr python package to be installed. use `sudo pip3 install netaddr` before running the playbook

- output 
```
[WARNING]: No inventory was parsed, only implicit localhost is available
[WARNING]: provided hosts list is empty, only localhost is available. Note that the implicit localhost does not match 'all'

PLAY [localhost] ****************

TASK [Gathering Facts] **********
ok: [localhost]

TASK [set_fact] *****************
ok: [localhost]

TASK [set_fact] *****************
ok: [localhost]

TASK [debug] ********************
ok: [localhost] => {
    "msg": "network is 10.10.10.0/27, gateway is 10.10.10.1"
}

TASK [set_fact] *****************
ok: [localhost]

TASK [set_fact] *****************
ok: [localhost]

TASK [debug] ********************
ok: [localhost] => {
    "msg": "incremented host address is 10.10.10.2/27"
}

PLAY RECAP **********************
localhost                  : ok=7    changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

------

we can use state registered in ansible and make thing check. 
- to achieve idempotent

- below we copy a file change a string and grep for the change and decide based on the change status

```yaml

---

- hosts: localhost
  connection: local
  vars:
    target: /mnt/c/learn/ansible/lesson_02/test_file.txt

  tasks:
  - name: idempotent file tasks
    copy:
      src: ../../lesson_02/test
      dest: '{{target}}'
  - name: Modify file
    command: sed -ie 's/example/sample/g' {{target}}

  - name: Find state
    command: grep 'sample0' {{target}}
    register: grep_state
    ignore_errors: true
  - name: show state of the file
    debug:
      var: grep_state.rc
  - name: Print state - success
    debug:
      msg:
       - "Changed here!"
    when: grep_state.rc == 0
  - name: Print state - failed
    debug:
      msg:
       - "Changed not here!"
    when: grep_state.rc != 0
	
```
