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

## If behind proxy probably un comment below 
- set the `http_proxy` and `https_proxy` with the proxy url.
- Also note, some app expected to be lower case. Set this in `~/.bashrc`.

```
sudo visudo
```
 - below only for exposing the proxy
```
#Defaults:%sudo env_keep += "http_proxy https_proxy ftp_proxy all_proxy no_proxy"
```

Note: 
  - when using `visudo` to save and exist use `Ctrl + o` and `Ctrl + x`.

-----------

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


In case to use some of the bash alias, add below content to `vi ~/.bashrc`

```sh
# configure windows executable
#alias java="/mnt/c/Program\ Files/Java/jdk-23/bin/java.exe"
#alias javac="/mnt/c/Program\ Files/Java/jdk-23/bin/javac.exe"
#export JAVA_HOME="/mnt/c/Program\ Files/Java/jdk-23/"

alias k=kubectl
source <(kubectl completion bash)
complete -o default -F __start_kubectl k

#export PATH="$PATH:/mnt/c/Program Files/Java/jdk-23/bin"

export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"

alias run-ollama-in-docker='echo "docker run -d -v /mnt/c/thiru/edi/ai-models/ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama"; read -p "Do you want to execute the command? (y/n): " usrInput; if [[ "$usrInput" =~ ^[Yy]$ ]]; then docker run -d -v /mnt/c/thiru/edi/ai-models/ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama; else echo "skipped execution..."; fi'

alias run-llama-model-in-ollama='echo "docker exec -it ollama ollama run llama3.2"; read -p "Do you want to execute the command? (y/n): " usrInput; if [[ "$usrInput" =~ ^[Yy]$ ]]; then docker exec ollama ollama run llama3.2; else echo "skipped execution..."; fi'
```

----------------------------------------------------

#### Update Jan-2026 (Ubnutu docker install)

```sh
# Add Docker's official GPG key:
sudo apt update
sudo apt install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt update
```
