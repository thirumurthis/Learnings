#!/bin/bash

for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do 
  echo "removing $pkg"
  sudo apt-get remove $pkg -q; 
done;

# to run the installation with the script from docker.io
# curl -fsSL https://get.docker.com -o get-docker.sh
# sudo sh ./get-docker.sh --dry-run


sudo apt-get update

# Add Docker's official GPG key:
# this should work
sudo apt-get install ca-certificates gnupg

# Note: If there are issues in installing like certifcate error make sure
# the gpg file is provided in the services.list and also add this package to be be installed
# sudo apt install apt-transport-https ca-certificates curl software-properties-common

sudo install -m 0755 -d /etc/apt/keyrings

#below is not working when executed.
# sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
# sudo chmod a+r /etc/apt/keyrings/docker.asc

# below lines where obtained by using the direct docker shell script get-docker.sh with dry-run option
# the .asc format seems to be old and for wsl gpg key is used
sudo curl -fsSL "https://download.docker.com/linux/ubuntu/gpg" | sudo gpg --dearmor --yes -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# verification to see if the file got created
ls /etc/apt/keyrings/docker.gpg

# Add the repository to Apt sources:
# Note, make sure if the path is gpg not asc in the singed-by
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo ls -lrth /etc/apt/sources.list.d/docker.list

sudo apt-get update
# added based on the link - https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-22-04
sudo apt update
# added just to make sure if the package gets fetched from docker
apt-cache policy docker-ce

sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# add the user - This is needed since it avoids the need for sudo infront of each command
sudo groupadd docker
sudo usermod -aG docker $USER
newgrp docker
sudo service docker start

# to uninstall completely
# sudo apt-get purge docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin docker-ce-rootless-extras

# after removal delete below folders as well
# sudo rm -rf /var/lib/docker
# sudo rm -rf /var/lib/containerd


######### SETTING SHORT-CUTS ########

# git bash ~/.bashrc - add below 

# alias docker='wsl -d Ubuntu docker $@'
# alias docker-compose='wsl -d Ubuntu docker-compose $@'

# power shell notepad $PROFILE - add bewlow

# Function WslDocker { wsl -d Ubuntu docker $args }
# Function WslDockerCompose { wsl -d Ubuntu docker-compose $args }
# Set-Alias -Name docker -Value WslDocker
# Set-Alias -Name docker-compose -Value WslDockerCompose
