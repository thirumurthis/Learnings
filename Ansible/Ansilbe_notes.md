- Once the Ansible is installed in WSL, refer `install_ansible_in_wsl2.md` for more info

### Optional
- To work with ansible from your local, with the ubuntu distro, we need to check if we are able to connect to the with ssh
- To do that you follow below steps

```
$ ssh localhost
ssh: connect to host localhost port 22: Connection refused
```
- The above failed since the `openssh-server` is not running or not installed. To install issue

```
$ sudo apt install openssh-server
```
- With the openssh-server installed, if `ssh localhost` connects, it will prompt for password.

### Working with the ansible comamnd with the WSL itself as a remote 

- Create a `config.ini` file and add `localhost`
- Issue below command, the output will display it doesn't kniow which user
```
$ ansible all -i config.ini -m ping
localhost | UNREACHABLE! => {
    "changed": false,
    "msg": "Failed to connect to the host via ssh: username@localhost: Permission denied (publickey,password).",
    "unreachable": true
}
```
- To add the user and prompt for password update the command like below
- We enabled the user using `-u username`
- We also aded `--ask-pass` for ansible to prompt for password

```
$ ansible all -i host.ini -m ping -u <username> --ask-pass
SSH password:
localhost | FAILED! => {
    "msg": "to use the 'ssh' connection type with passwords, you must install the sshpass program"
}
```

- We need to install the `sshpass` package using `sudo apt install sshpass`
- With the sshpass program installed, the command will output the results like below
```
$ ansible all -i host.ini -m ping -u <username> --ask-pass
SSH password:
localhost | SUCCESS => {
    "ansible_facts": {
        "discovered_interpreter_python": "/usr/bin/python3"
    },
    "changed": false,
    "ping": "pong"
}
```

- Module `setup` to get the configuration details of the system
```
$ ansible all -i host.ini -m setup -u <username> --ask-pass
```

### Ansible inventory file
- Ansible will look for an inventory file by default under the `/etc/ansible/` path for the inventory if not specified.
- The `-i` is one way to provide the inventory file. This can be `ini` test format or `yaml` format.

#### Example inventory content
- simple list of hosts
```
localost
web1.my-domain.com
web2.my-domain.com
web3.my-domain.com
```
- Grouping the hosts
```
[local]
localhost

[webServers]
web1.my-domain.com
web3.my-domain.com

[dbServers]
db.my-domain.com
```
- Alias for the host in inventory
  - the domain name can be Ip address or FQDN
```
[local]
local ansible_host=localhost

[webServers]
web1 ansible_host=web1.my-domain.com
web2 ansible_host=web2.my-domain.com
```
- Passing parameters
- say, web1 server are windows machine and we pass parameter like below
```
[local]
local ansible_host=localhost

[webServers]
web1 ansible_host=web1.my-domain.com ansible_connection=winrm
web2 ansible_host=web2.my-domain.com ansible_connection=winrm

[dbServers]
db ansible_host=db.my-domain.com ansible_connection=ssh
```
- check the doc on building inventory file
- Inventory file has `all` and `ungrouped` section
- we can pass more than one invetory file
- The inventory file can also be generated dyanmically say by python script
- The incentory files can be grouped and can be specify to be executed in order

#### To use the alias name with the ansbile command

```
$ ansible local -i config.ini -m ping -u <username> --ask-pass

OR
$ ansible local -i config.ini -m ping --ask-pass
```

- The config.ini file look like
```
[servers]
local ansible_host=localhost
```

- To connect to the group of servers then

```
$ ansible servers -i config.ini -m ping --ask-pass
```

### Ansible playbook
- This is the entry point, which defines what needs to be done

`Playbook` is a single Yaml file containing set of plays.
`Plays` - defines set of `tasks` to be run on the host

- Example: save below content as playbook-one.yaml
```yaml
- name: "Get date and timezone"
  hosts: localhost
  gather_facts: yes
  tasks:
   - name: Get date
     ansible.bulitin.shell: date  # instead of ansible.builtin.shell we can use shell
     register: date

   - name: Get timezone
     ansible.builtin.shell: cat /etc/timezone
     register: timezone
    
   - name: display date
     ansible.builtin.debug:   # instead of ansible.builtin.debug we can use debug
       msg: "{{ date.stdout }}"
    
   - name: display timezone
     ansible.builtin.debug:
        msg: "{{ timezone.stdout }}"
```

- To execute the playbook, the command is
```
$ ansible-playbook playbook-one.yaml -i config.ini
```
- output
```
ansible-playbook playbook-one.yaml -i config.ini

PLAY [Get date and timezone]****************************

TASK [Gathering Facts] *********************************
ok: [localhost]

TASK [Get date] ***************************************
changed: [localhost]

TASK [Get timezone] **********************************
changed: [localhost]

TASK [display date] *********************************
ok: [localhost] => {
    "msg": "Sun Apr  7 21:53:14 PDT 2024"
}

TASK [display timezone] *****************************
ok: [localhost] => {
    "msg": "America/Los_Angeles"
}

PLAY RECAP *************************************
localhost                  : ok=5    changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

- Example of having multiple plays
```
- name: "Get date"
  hosts: localhost
  gather_facts: yes
  tasks:
   - name: Get date
     ansible.bulitin.shell: date  # instead of ansible.builtin.shell we can use shell
     register: date
   - name: display date
     ansible.builtin.debug:   # instead of ansible.builtin.debug we can use debug
       msg: "{{ date.stdout }}"
    
- name: "Get timezone"
  hosts: localhost
  gather_facts: yes
  tasks:
   - name: Get timezone
     ansible.builtin.shell: cat /etc/timezone
     register: timezone
   - name: display timezone
     ansible.builtin.debug:
        msg: "{{ timezone.stdout }}"
```

### Ansible linting

- To test the ansible play with check command

```
ansible-playbook playbook-filedir.yaml -i config.ini --check
```
- Note, the results will be displayed but no action would have done on the server

- Content for the file playbook-filedir.yaml is below
```yaml
- name: "Create directory and file"
  hosts: localhost
  tasks:
   - name: Create a directory
     file:
        path: /tmp/example
        state: directory
   - name: Create a file
     file:
         path: /tmp/example.txt
         state: touch
```

#### Diff mode
- With the `--diff` flag will provide the change before and after applying the changes.


```
ansible-playbook playbook-filedir.yaml -i config.ini --check --diff 
```
- no need to use `--check`

- output
```
PLAY [Create directory and file] *********************************

TASK [Gathering Facts] ********************************
ok: [localhost]

TASK [Create a directory] ******************************
--- before
+++ after
@@ -1,4 +1,4 @@
 {
     "path": "/tmp/example",
-    "state": "absent"
+    "state": "directory"
 }

changed: [localhost]

TASK [Create a file] ***************************
ok: [localhost]

PLAY RECAP ******************************************
localhost                  : ok=3    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

#### Syntax check
```
ansible-playbook playbook-filedir.yaml -i config.ini --syntax-check 
```

#### Ansible linit
- this lints the ansible plabook checks all the code

```
ansible-lint playbook-filedir.yaml -i config.ini 
```
- install the `ansible-lint` using `sudo apt install ansible-lint`

- output
```
ansible-lint playbook-filedir.yaml
WARNING  Listing 4 violation(s) that are fatal
risky-file-permissions: File permissions unset or incorrect
playbook-filedir.yaml:4 Task/Handler: Create a directory

yaml: wrong indentation: expected 6 but found 8 (indentation)
playbook-filedir.yaml:6

risky-file-permissions: File permissions unset or incorrect
playbook-filedir.yaml:8 Task/Handler: Create a file

yaml: wrong indentation: expected 6 but found 9 (indentation)
playbook-filedir.yaml:10

You can skip specific rules or tags by adding them to your configuration file:
# .ansible-lint
warn_list:  # or 'skip_list' to silence them completely
  - experimental  # all rules tagged as experimental
  - yaml  # Violations reported by yamllint

Finished with 2 failure(s), 2 warning(s) on 1 files.
```
