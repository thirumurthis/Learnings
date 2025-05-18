## When working with WSL2 in order to skip providing the sudo password everytime we can do below

- we update the user to be an no pass by updating visudo and adding a line
```
$ sudo visudo
```
- scroll to bottom of this file and add below and save the change.

```
<user-name> ALL=(ALL) NOPASSWD: ALL
```
-----------------

## How to enable systemd in the WSL configuration

- edit the /etc/wsl.conf file (which is empty if not configured for other.
```
sudo vi /etc/wsl.conf
# add
[boot]
systemd=true
```
- Shutodown using `wsl.exe --shutdown Ubuntu` to take effect of running systemd.
- issue command `systemctl list-unit-files --type=service` to verify if the it is enabled.
-----------------

## Install Ansible to the WSL2
- refer the ansible doc
```
sudo apt-get install -y gcc python3-dev python3-pip libkrb5-dev && \
pip3 install --upgrade pip && \
pip3 install --upgrade virtualenv && \
sudo apt install krb5-user -y && \
pip3 install pywinrm && \
sudo apt install ansible -y
```
- `krb5-user`, `libkrb5-dev` package helps to work with windows authentication as well.
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

## To install powershell in wsl2

```
sudo apt-get install -y wget apt-transport-https software-properties-common && \
wget -q https://package.microsoft.com/config/ubuntu/22.04/packages-microsoft-pord.deb && \
sudo dpkg -i pacakges-microsoft-prod.deb && \
sudo add-apt-repository universe && \
sudo apt-get install -y powershell

# once installed issue below to use the powershell
$ pwsh
# once the powershell is opened install a module that allows to
# this power CLI which interacts with the VMware VSphere environemnt

PS /home/thirumurthi> install-module VMware.PowerCLI

PS ..> connect-viserver
```
--------------

### To open the the file from the wsl terminal

```
# below command opens the explorer from current directory
$ explorer.exe .
```

------------

## with the systemd installed, we can sping micro-k8s environemnt in wsl

```
sudo snap install microk8s --classic

sudo microk8s status
```

# installing minikube

```
$ sudo apt-get update \
sudo apt-get install \
ca-certifcates \
curl \
gnupg \
lsb-release

# install docker gpg key
$ sudo mkdir -p /etc/apt/keyrings
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
$ echo "deb[arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | suod tee /etc/apt/source.list.d/docker.list > /dev/null

$ sudo apt-get update

# install docker
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# add current user to docker user, so the current user interact with docker
$ sudo usermod -aG docker $USER && negrp docker

# install contrack
$ sudo apt install -y contrack

# install the minikuber
# download the minikube executable
$ curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linu-amd64
$ chmod +x ./minikube
$ sudo mv ./minikube /usr/local/bin/

# set minikube to use the docker driver
$ minikube config set driver docker

# install the kubectl and set context to minikube
$ curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/releae/stable.txt)/bin/linux/amd64/kubectl"

# start the minikube
$ minikube start
```


