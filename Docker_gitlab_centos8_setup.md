Once the centos iso image is downloaded and setup using VirtualBox.

With the default network (meaning the `ifconfig` ) set with the dynamic ip which is set by DHCP server of the Wifi.

Installed the docker first.

  - Add the repository to the `sudo dnf config-manager --add-repo=https://download.docker.com/linux/centos/docker-ce.repo`
  - `sudo dnf install docker-ce docker-ce-cli containerd.io`, the latest package of `continerd.io` was not able to resolve when executed
     - Centos/RHEL 8 blocks the installation since it has its own `podman docker`, so install the rpm directly.
     - ` sudo dnf install https://download.docker.com/linux/centos/7/x86_64/stable/Packages/containerd.io-1.2.6-3.3.el7.x86_64.rpm` 
        - at the time of writting above is the latest version.
      - After installing, use the command `sudo dnf install docker-ce docker-ce-cli` now it will install the latest version.
      - Use `sudo dnf repolist -v` to list the version available in the repo.
      
      - if needed disable the firewall service `sudo systemctl disable firewalld` (if need to enable, use enable command)
      - Start the docker service, `sudo systemctl enable --now docker` (this will enable docker service and start the service)
      
  Installing Git. 
  
   - instruction are at [GitLab - ominibus image](https://docs.gitlab.com/omnibus/docker/README.html)
   - set the GITLAB_HOME in shell `$ vi ~/.bashrc`, add `export GITLAB_HOME=$HOME/gitlab` (or $HOME/srv as mentioned in the doc)
      - restart the bash, using `.~/.bashrc` or open an new shell terminal.
   - check if the docker service is running both the client and the internal server host `docker version` (note: not using --version)
   - use the below docker command (note the hostname using the ip address of the centos virutal box (this is dynamic ip)
        - also the port is changed from 22 to 9222 which is published.
        
```
          sudo docker run --detach \
        --hostname 192.168.0.45 \
        --publish 443:443 --publish 80:80 --publish 9222:22 \
        --name gitlab \
        --restart always \
        --volume $GITLAB_HOME/gitlab/config:/etc/gitlab \
        --volume $GITLAB_HOME/gitlab/logs:/var/log/gitlab \
        --volume $GITLAB_HOME/gitlab/data:/var/opt/gitlab \
        gitlab/gitlab-ce:latest
 ```
 
 - see the container is running using `docker container ls`
 - After validating, open up a browser and use `198.162.0.45` this will open up the inital gitlab page to change the password.
    - Also, for the first time when i logged the ip in the address bar, it threw `Http: 502` error.
       - the 502 error was because the installation of conatiner was still in progress.
       - After changing the password, the screen will reload to admin page provide the user id and password 
           - (username is `root` and password updated in the pervious step.)
    - The gitlab service cane also be exposed to the host machine, use the cmd utility to ping the 192.168.0.45.
       - if the host is able to ping, then in browser just use the ip address the gitlab will be avialable.

The gitlab ce instance, haven't setup the https certificates and looks like below.

 ![image](https://user-images.githubusercontent.com/6425536/82720154-9691f800-9c65-11ea-8cb7-14080d8ee12a.png)
