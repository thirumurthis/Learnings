
- Flow of the DNS resolution in Linux VM



![image](https://user-images.githubusercontent.com/6425536/171055763-ce6fb829-3b54-4c51-bf38-7a5c0c7b55a9.png)


Using the docker desktop, you can install the RHEL 8+ version using below command
```sh
docker run -it --name test registry.access.redhat.com/ubi8 bash
```

Note: 
 - `dnf` is successor of the `yum` package manager. In future the `dnf` will be used. (similar to `service` which is updated with `systemctl` now`.
 - Also `deamon` process is also referred as service, and most of the process that ends with `d` is a daemon process. like `systemd`, `sshd`, etc.

Sample dig commands
```
# install using dnf, command: dnf install bind-utils

$ dig google.com 

# to fetch A record
$ dig A google.com +short 
-- output: 142.251.33.110

# to fetch AAAA record (IP6) address
$ dig AAAA google.com +short
-- output: 2607:f8b0:400a:80a::200e


```
