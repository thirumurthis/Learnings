Ansible

When intsalling ansible, the a config file is created at /etc/ansible/ansible.cfg

This config file has different section

[defaults]
[inventory]
[privilege_scalation]
[paramiko_connect]
[ssh_connection]
[persistent_connection]
[colors]

Each of these section contains set of configuration.

For example, the default section has values like 
inventory    = /etc/ansible/hosts
log_path     = /var/log/ansible.log

-- 
Override 1: 
In case if we need to override the default config files, and have different configs for each playbook.

- we can copy the default config and place that under each playbooks directory
- For example, when configuring some hosts we need the ssh timeout to be longer, output not to be colored, etc.
- for web => say the config under /opt/webserver-pb/ansible.cfg
- for db  => say the config under /opt/dbserver-pb/ansible.cfg

So, the config file is placed in the directory and when we run the playbook the config file will be fetched from this directory.


Override 2:
- we want to keep the config outside of the playbook directory and use it.
- in this case we need to set the environment value for the config path
- set the ANSIBLE_CONFIG=/path/to/the/ansible.cfg, when this is set running ansible-playbook it will user this path to find the config file

Precedence of the override values of config file:

1. environment variable to set the path of config takes top precedence
2. the ansible config file configured in the current directory or playbook will take next precedence.
3. .ansible.cfg file from the user home directory ~/, if this file exists
4. default config path /etc/ansible/ansible.cfg

Note, not all values on the config file should be present in each file, only the override values like helm charts. The next value will be picked from the next precedence.

Override 3:
In case if we want to override one or few parameters without using the config file or modifying the default ansible.cfg file, then we can create a environment variable before running the ansible-playbook command

For example, we want to disable gathering for a playbook, then we can create the environemnt variable 
ANSIBLE_GATHERING=explicit

Note, the env name is constructed by prepending ANSIBLE_ and with upper case configuration field. The config file looks like below, check the documentation

[defaults]
gathering =implicit

Note, export ANSIBLE_GATHER=explict, will update the env for the session.

List or view the configuration

use below commadn to list the configuration
 
```
# list of all values and values can be set
$ ansible-config list
```

- To see which config from the current config file
```
$ ansible-config view
```

- To show the current active config settings
```
$ ansible-config dump

$ export ANSIBLE_GATHERING=explicit
$ ansible-config dump
# output 
DEFAULT_GATHERING(env:ANSIBLE_GATHERING)=explicit
```

-----
Playbook - is defined using yaml

-----
Inventory

- Ansible is agentless since it is connecting with ssh and winrm.

- Since ansible is agentless the information about the target system is stored in inventory file
- The default path is `/etc/ansible/hosts`
- The inventory is in ini format

Giving an alias name for server in the inventory for the host

```
web ansible_host=webserver1.domain.com

# inventory parameter
- ansible_host is an invertory parameter
# other parameters are (refer docs)
- ansible_port => 22/5785 (default is 22)
- ansible_connection => ssh/winrm/localhost
- ansible_user => root (default root for linux; administrator for windows)
- ansible_ssh_pass

```
- To indicate if the remote machine is linux or windows

```
web1 ansible_host=webserver1.domain.com ansible_connection=ssh
web2 ansible_host=webserver2.domain.com ansible_connection=winrm

# to connect to localhost and not to remote host then use below
localhost ansible_connection=localhost

```
- SSH key based access is the best practice to access the remote hosts in production env.

Inventory format can be ini or YAML. Note YAML is more structured and better for large host machine managements.

- Grouping parents in ini file, using `children`

```
[db_server]
db1
[web_server]
web1

[all_server:children]
db_server
web_server
```

--- 
Templating:

String modification:

{{ variable | lower }}  => to lowercase
{{ variable | upper }}  => to uppercase
{{ variable | title }}  => to title case
{{ variable | replace("one","two") }} => replace string in variable
{{ variable | default("custom") }}    => provide default variable if no value in variable

Numeric operations

{{ [1,2,3] | min }}
{{ [1,2,3] | max }}
{{ [1,2,3] | unique }}
{{ [1,2,3,4] | union (3,4,5) }}
{{ [1,3,4] | intersect (4,5) }}
{{ 100 | randon }}
{{ ["one","two","three" ] | join(" ") }}

Loops and condition 
- Note the use of {% this indicates it is a block

{% for item in [0,2,4,6] %}
{{ item }}
{% endfor %}

{% for item in [0,1,2,3,4] %}
  {% if item == 3 %}
    {{ item }}
  {% endif %}
{% endfor %}

Check the Ansible documentation, since along with the jinja template filters, the Ansible has extended more filters to work with automation and infrastructure use cases.

{{ "/etc/hosts" | basename }}   => basename filter which return hosts and this works for linux
{{ "C:\windows\User\host" | win_basename }}  => windows basename filter returns host
{{ "C:\windows\User\host" | win_splitdrive }} => filter returns ["C:","\windows\User\host" ]
{{ "C:\windows\User\host" | win_splitdrive | first }} => filter returns "C:"
{{ "C:\windows\User\host" | win_splitdrive | last }} => filter returns "\windows\User\host"
 
How the ansible uses jinja2 templates on the playbook.

Ansible first runs the playbook over the jinja2 template engine, updates the variables and then uses this for execution.

How to create the jinja2 template file instead of inline templating?
- For example, say we need to update hostname in an index.html file

1. Create the specific file with j2 extension, in this case `index.html.j2`
2. The content of the file might look like
```
<html>
<body>
The hostname is {{ inventory_hostname }}
</body>
</htmls>
```

3. The ansible  inventory file content

```
[web_servers]
web1 ansible_host=web1.domain.com
web2 ansible_host=web2.domain.com

```

4. The playbook content, 

- To update the template we use template module
```
- hosts: web_servers
  tasks:
   - name: Copy index file
     src: index.html.j2
     dest: /var/www/nginx-default/index.html
```
- To simply copy the file we use below content
```
# with the copy module, we can only copy the file 
# we need to use the template module
- hosts: web_servers
  tasks:
   - name: Copy index file
     src: index.html
     dest: /var/www/nginx-default/index.html
```

Execution of ansible:

1. Based on the number of host in the inventory, ansible creates sub process to perform variable interpolation all the hosts
2. Each sub process gathers facts from the hosts
3. Each sub process starts executing playbooks
3.i Each invidual process does the templating task, with the interpolated variables for the hosts
3.ii The template module copies the index.html file to the host, since the variable value is substituted in 3.i step

When using templates in the role, create them under templates directory

Other example 

```
- hosts: localhost
  connection: local
  vars:
    dialogue: "The name is Bourne, James Bourne!"
  tasks:
     template:
        src: templates/name.txt.j2
        dest: /tmp/name.txt
```

-templates/name.txt.j2
```
{{ dialogue }}
```

---

hostname, ipaddress, monitor_port, type, protocol
{% for item in groups['lamp_app'] %}
{{ item}}, {{ hostvars[item].ansible_host }}, {{hostvars[item].monitor_port}}, {{hostvars[item].protocol}}
{% endfor %}

===
- hosts: monitoring_server
  become: yes
  tasks:
  - name: update conf
    template:
       src: templates/agents.conf.j2
       dest: /etc/agents.conf
===

using setup module 

```
ansible all -m ansible.builtin.setup -a 'filter=ansible_architecture'
```

===
using setup module in playbook

```
- hosts: all
  tasks:
  - setup:  # we can add gather_facts or filter etc.

- hosts: localhost
  tasks:
  - name: copy template csv
    template: 
      src: templates/inventory.csv.j2
      dest: /tmp/inventory.csv
```
- jinja2 template, to use the gather_facts.
- Note: ansible_facts is the magic variable

below facts are gathered
```
ansible_architecture
ansible_distribution_version
ansible_memtotal_mb
ansible_processor_cores
ansible_processor_count
```

```
hostname, architecture, distribution_version, mem_total_mb, processor_cores, processor_count
{% for item in groups['all'] %}
{{item}}, {{hostvars[item].ansible_facts.architecture}}, {{hostvars[item].ansible_facts.distribution_version}}, {{hostvars[item].ansible_facts.memtotal_mb}}, {{hostvars[item].ansible_facts.processor_cores}}, {{hostvars[item].ansible_facts.processor_count}}
{% endfor %}
```
=====

Variable:

In an inventory file we have defined variables like ansible_host, ansible_connection, ansible_ssh_pass


playbook, can include variable defined with vars attribute

```
-
  name: add server to resolv.conf
  hosts: localhost
  vars:
    dns_server: 10.1.101.10'
  tasks:
   - lineinfile:
          path: /etc/resolv.conf
          line: 'nameserver {{dns_server}}' 

```

- we can also have seperate dedicated files for variables

```
variable1: value1
``` 

- Say we have a playbook 
```
- name: Firewal config settings
  hosts: web
  tasks:
  - firewalld:
      service: https
      permanent: true
      state: enabled
  - firewalld:
      port: '{{http_port}}'/tcp
      permanent: true
      state: enabled
  - firewalld:
      port: '{{ smtp_port}}'/udp
      permanent: true
      state: enabled
  - firewalld:
       source: '{{inter_ip_range}}'/24
```
- If we need to create a variable file, we need to create the file name in the hostname provided in the inventory file

- inventory file

```
web ansible_host=localhost
```

- the variable file `web.yml`

```
http_port: 8081
smtp_port: 161-162
inter_ip_range: 192.0.2.10
```

so the variable in the playbook, it should be 

it should be within quotes 

source: '{{variable1}}'
source: this is value to {{variable}}  # note when within string no need to for single quotes

number type variables can hold integer and float values
boolean type variables for truthy variable the value can be'true', True, 'y', 't','on', '1',1,1.0
for falsy values False, 'false', 'n','no', 'off','0',0,0.0

list type variables
  source:
     - one
     - two
Dictionary type variables

- Registering variables and predence of variables

lets say we have an inventory file

```
web1 ansible_host=172.10.20.10 dns_server=8.8.8.8 # variable at host level
web2 ansible_host=172.10.20.11

[web_servers]
web1
web2

[web_servers:vars]
dns_server=7.7.7.7 # variable at group level
```

In the above sample inventory file we have dns_server variable defined in two places.

Note, ansible will create sperate variables for each host when specified under this section [web_servers:vars] i.e. web1 web2 will get dns_server variable and value.

1. variable provided at host level take precedence than variable provide at the group level.
so the dns_server in the host level will be used.

2. If a variable is defined in the playbook along with host level, group level. The play level takes more precedence than host and group.

3. we can pass the variable using `--extra_vars "dns_server=9.9.9.9"` when running using ansible-playbook command. in this case the variable passed with extra_vars take highest precedence.

There are different ways to use the variable, check the documentation for precendence list.

#### Register variable
  - in ceratain case we need store a output of one task and use it in another task.

```
- name: variable example
  hosts: all
  tasks:
  - shell: cat /etc/hosts
    register: result
  - debug:
     var: result  # accessible and will be printed
```

- in the above case, the output contains other information like command compeletion state rc (return code), start and end time, time took to complete the command, stdout, strerr, etc. which can be used

To print only the file content
```
var: result.stdout
var: result.rc
```

The scope of the registered variable, any variable created with register is scoped for that host and is avialable for rest of the playbook execution.

```
# play 1
- name: variable example
  hosts: all
  tasks:
  - shell: cat /etc/hosts
    register: result
  - debug:
     var: result.stdout  # accessible and will be printed

#play 2
- name: variable example
  hosts: all
  tasks:
  - debug:
     var: result.rc  # accessible in different play

```

- To view the data of the variable without debug module is to use `-v` in the command

```
ansible-playbook playbook1.yaml -i inventory -v
```

### Scope of the variable

```
web1 ansible_host=172.10.20.10 dns_server=8.8.8.8 # variable at host level
web2 ansible_host=172.10.20.11

[web_servers]
web1
web2

```
- playbook 

```
- name: print host
  hosts: all
  tasks:
  - debug:
      msg: dns server {{dns_server}}  # only values ofr web1 will be printed
```
- above is an example for host scope where the dns_server is accessible only by web1 host

### Magic variable

#### hostvars
In case if we need  to pass the variabled from one host to another we can use the magic variable.

- the magic variable `hostvars` can be used to get the dns_server variable defined above from web1 host to web2 host.

```
- name: print val
  hosts: all
  tasks:
  - debug:
      msg: '{{hostvars['web1'].dns_server}}'
```
like above we can use 

```
      msg: '{{hostvars['web1'].ansible_facts.architecture}}'
      msg: '{{hostvars['web1'].ansible_facts.processor}}'
```

##### groups magic variable

```
# groups returns all the host under the passed group name
msg: '{{ groups['web_servers']}}'
```

#### group_names
- This returs all the host the curret group is part of 

```
msg: '{{ group_names }}'
```

##### inventory_hostname

- This gives the name of the host configure in the inventory file. (not the Hostname or FQDN)

```
msg: '{{inventory_hostname}}'
```

- there are documents check using ansible playbooks.

#### ansible_facts
- when ansible connects to the remote host, it collects the information about the host machines like date, time, memory availab,etc. this are called facts.
- This are run by default by setup module. This is executed by default.
- The output of the playbook we could see the gather facts task default. unitl not set to false.

```
- hosts: all
  name: print info
  tasks:
  - debug:
      var: ansible_facts # will print all the collected facts
```

What if we don't want to gather facts 

```
- hosts: all
  name: print info
  gather_facts: no  # this will tell ansible not to gather facts
  tasks:
  - debug:
      var: ansible_facts
```

- The behaviour of gathering facts is governed by another variable called `gathering`, which is set to implicit in the configuration file. check the configuration section above.

gathering = implict.

if the gathering is provided in the playbook, it will take precedence over the one in configuration file.

--------

### Handlers
- handlers are used to notify another task that it needs to perform next activity.

For example, we update the services in Linux and we need to restart after update

```
- name: update service
  hosts: app service
  tasks:
    - name: Copy code
      copy:
         src: binary_code
         dest: /opt/application/
      notify: Restart application
  handlers:
    # name should match
    - name: Restart application
      service:
         name: app_service
         state: restarted 
```

### Role
- Say we have a DB configuration in tasks, we can extract the tasks and create reusable code.

- normal playbook looks like below

```
- name: Install and configure My SQL
  hosts: db-servers
  tasks:
   - name: Install Pre-requsites
     yum: name=pre-req-packages state=present
   - name: Install MySQL Package
     yum: name=mysql state=present
```

- In the above playbook, we can move the task to a seperate file called roles
- Which indicates that is primarily to install the DB and db role.


- The roles file, store it in mysql (place under the same path where playbook exist)
```
  tasks:
   - name: Install Pre-requsites
     yum: name=pre-req-packages state=present
   - name: Install MySQL Package
     yum: name=mysql state=present
```

- using the roles in playbook
```
- name Install MySQL
- hosts: db-servers
  roles:
    - mysql
```

To group and maintain the ansible code, we cand use `ansible-galaxy init <name-of-ansbile-role>`

```
ansible-galaxy init mqsql
```

- The above command will create a template structre for code

```
mysql
|_ README.md
|_ templates
|_ handlers
|_ tasks
|_ defaults
|_ vars
|_ meta
```

##### The default path where ansible looks for roles is `/etc/ansible/roles`
- This configuration is defined in  `/etc/ansible/ansible.cfg` under `roles_path = /etc/ansible/roles`
- we can download the public roles from the Ansible galaxy hub.

##### Installing 

```
ansible-galaxy install community.mysql

# this configuration, /etc/ansible/roles/community.mysql
``` 

- To use in the playbook use
```
- name: Install DB
  hosts: db-servers
  roles:
   - community.mysql
```
- Addiitonally we can pass parameter, like run as different user, etc.

```
- name: Install DB
  hosts: db-servers
  roles:
   - role: community.mysql
     become: yes
   - role: nginx
```

```
$ ansible-galaxy list
$ ansible-conf dump | grep ROLE
```
- To install role to different path

```
$ ansible-galaxy install comunity.mysql -p ./roles 
```

------
### Collections:
- Ansible provides a way to manage vendor specific modules and playbooks, say for example for CISCO, Juniper, etc have provided their own collection which can be installed.

```
ansible-galaxy collection install network.cisco
```
- Ansible Collections are way to package and distribute roles, plugins, etc.
- Contains extended function

working with collection

```
ansible-galaxy collection install amazon.aws
```

- playbook
```
- hosts: localhost
  collections:
    - amazon.aws

  tasks:
    - name: Create S3 bucket
      aws_s3_bucket: 
        name: app-bucket
        region: us-west-1
```

Collections provides modilarity.

- To create a collection we can do like below
```
- hosts: localhost
  collections:
   - my_namespace.my_collection
  roles:
   - my_custom_role

  tasks:
   - name: Use custom model
     my_custom_module:
       param: value
```
- Folder

```
my_collection/
|_ docs/
|_ galaxy.yml
|_ plugins/
   |_ modules/
      |_ my_custom_module.py
|_ roles/
   |_ my_custom_role/
     |_ tasks/
        |_ main.yml
```

- specify the requirements in the yaml file. `requirements.yml`
- specify all the requires collection in single file like below
```
collections:
  - name: amazon.aws
    version: "1.5.0"
  - name: community.mysql
    src: https://github.com....
    version: "1.0.0"
```
- below is the way to install the collections
- centralized and version mananged appropriately
```
ansible-galaxy collection install -r requirements.yml
```
