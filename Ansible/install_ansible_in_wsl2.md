## Install Ansible to the WSL2
- refer the ansible doc

- Below packages installed in wsl2 supports only Linux.
```
sudo apt-get install -y gcc python3-dev python3-pip&& \
pip3 install --upgrade pip && \
pip3 install --upgrade virtualenv && \
sudo apt install ansible -y
```

- below command supports to manage both windows and linux.
- `winrm` is how we can connect to windows machine, more like `ssh` for linux.
```
sudo apt-get install -y gcc python3-dev python3-pip libkrb5-dev && \
pip3 install --upgrade pip && \
pip3 install --upgrade virtualenv && \
sudo apt install krb5-user -y && \
pip3 install pywinrm && \
sudo apt install ansible -y
```
- `libkrb5-dev`,`krb5-user`- package helps to work with windows authentication as well.
- so ansible can be connect to windows target requesting password

--------------

## Persistence alias

```
cd ~
vi ~/.bash_aliases
# edit to add ansible configuration aliases example

alias ansibledir="cd /mnt/c/User/path/of/the/onedrive"

# issue below or close and re-open the terminal
source ~/.bash_aliases
```
--------------
