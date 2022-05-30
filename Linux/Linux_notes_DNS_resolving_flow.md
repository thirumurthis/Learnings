
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

#### To track the request sent to the DNS server

```sh

###@ 1. issue the below tcpdump command
#run this command in one shell to capture all DNS requests
sudo tcpdump -s 0 -A -i any port 53

#### 2. make the below request
#make a dig request from another shell
dig linkedin.com
```

### How to view the call to the DNS server using dig command
```sh
$ dig +trace linkedin.com
```


### Notes about DNS

- DNS is used by cloud providers and CDN providers to scale their services. In Azure/AWS, Load Balancers are given a CNAME instead of IPAddress. They update the IPAddress of the Loadbalancers as they scale by changing the IP Address of alias domain names. This is one of the reasons why A records of such alias domains are short lived like 1 minute.
- DNS can also be used for discovering services. For example the hostname serviceb.internal.example.com could list instances which run service b internally in example.com company. Cloud providers provide options to enable DNS discovery.
- DNS can also be used to make clients get IP addresses closer to their location so that their HTTP calls can be responded faster if the company has a presence geographically distributed.
- DNS Loadbalancing and service discovery also has to understand TTL and the servers can be removed from the pool only after waiting till TTL post the changes are made to DNS records. If this is not done, a certain portion of the traffic will fail as the server is removed before the TTL.
