#!/bin/bash

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
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt update

sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

sudo systemctl status docker

sudo groupadd docker
sudo usermod -aG docker $USER

###############
### If docker pull error with below such message
# ERROR: failed to create cluster: failed to pull image "kindest/node:v1.35.0@sha256:452d707d4862f52530247495d180205e029056831160e22870e37e3f6c1ac31f": command "docker pull kindest/node:v1.35.0@sha256:452d707d4862f52530247495d180205e029056831160e22870e37e3f6c1ac31f" failed with error: exit status 1
# Command Output: docker-credential-secretservice: error while loading shared libraries: libsecret-1.so.0: cannot open shared object file: No such file or directory
# error getting credentials - err: exit status 127, out: ``
######### fix
#  create a ~/.docker/config.json and add below content. use mkdir -p ~/.docker
#   { "credsStore": "pass" }
#
