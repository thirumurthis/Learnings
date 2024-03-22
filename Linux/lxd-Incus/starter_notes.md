- To check the networking configuration we can use `ifconfig` to read the details


- Once we install incus and create multiple containers with incus.
- All these multiple containers can be viewed as multiple servers

- The problem with the `eth0` ethernet device is, it cannot be shared among multiple system.
- We need to create multiple containers and show them on main LAN network.
- To do that (share the network) we need to create a virtual switch, which will connect the containers
   - Hardware switch is used to connect more than one system.

- To create virtual switch in ubuntu

```
# below installs virtual swtich
$ sudo apt install openswtich-switch
```
- once the swtich is installed, we need to make changes to network configration

```
$ cd /etc/netplan
$ ls
# there should be 1 yaml file (possibly more than 1)
# mostly the file starts with number
```
- edit the file, The existing file content might look like below. Remove it

```yaml
network:
   ethernets:
      eth0:
        dhcp4: true
   version: 2
```
- Add the below network configuration

```yaml
netowrk:
   version: 2
   ethernets:
      eth0:
        dchp4: false
        dchp6: false
   bridges:
      bridge0:
        interface: [eth0]
        addresses: [172.16.1.91/16] # this is the same address of the node mostly ssh'ed
        routes:
          - to: default
            via: 172.16.0.1  # this would be the default gateway - address of router 192.168.0.1/24 something like that
       nameservers:
         addresses:
          - 1.1.1.1
          - 1.0.0.1
       parameters:
         stp: true
         forward-delay: 4
       dhcp4: no
```
- Do a `ifconfig` command to list the existing network
- We need to apply the netplan use below command

```
$ sudo netplan apply
# should apply the command 
```
- with the applied netplan command, now if we issue the ifconfig we should see a bridge0 network added and ip address allocated there.

----

### Installing Incus

- get the key
```
$ curl -fsSL https://pkgs.zabbly.com/key.asc | gpg --show-keys --fingerprint
```
- create a folde to store the key ring

```
$ sudo mkdir -p /etc/apt/keyrings/
$ sudo curl -fsSL https://pkgs.zabbly.com/key.asc -o /etc/apt/keyrings/zabbly.asc
```

- Add the repository, perofrm it as sudo
```sh
sh -c 'cat <<EOF > /etc/apt/sources.list.d/zabbly-incus-stable.sources
Enabled: yes
Types: deb
URIs: https://pkgs.zabbly.com/incus/stable
Suites: $(. /etc/os-release && echo ${VERSION_CODENAME})
Components: main
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/zabbly.asc

EOF'
```

- Now we can install incus
```
$ sudo apt-get update
$ sudo apt install incus
# when prompted type y
```

- Add the current user to the incus-admin group

```
$ getent group incus
$ getent group incus-admin
# to add to the group
$ sudo usermod -aG incus-admin <current-user>

# to publish the changes
$ newgrp incus-admin
```

-example
```
user01@user01:~$ id
uid=1000(user01) gid=1000(user01) groups=1000(user01),4(adm),20(dialout),24(cdrom),25(floppy),27(sudo),29(audio),30(dip),44(video),46(plugdev),116(netdev),999(lxd)
user01@user01:~$ newgrp incus-admin
user01@user01:~$ id
uid=1000(user01) gid=997(incus-admin) groups=997(incus-admin),4(adm),20(dialout),24(cdrom),25(floppy),27(sudo),29(audio),30(dip),44(video),46(plugdev),116(netdev),999(lxd),1000(user01)
```

- ZFS utility needs to be installed before initializing the incus.
- This can be used for creating zfs pools for containers.

```
$ sudo apt install zfsutils-linux
```
- Before start using the incus, we need to init

```
$ incus admin init
```

- example output, note it should prompt for addition storage backend pool might be different when using physical server.
```
$ incus admin init
Would you like to use clustering? (yes/no) [default=no]:
Do you want to configure a new storage pool? (yes/no) [default=yes]: yes
Name of the new storage pool [default=default]:
Would you like to create a new local network bridge? (yes/no) [default=yes]:
What should the new bridge be called? [default=incusbr0]:
What IPv4 address should be used? (CIDR subnet notation, “auto” or “none”) [default=auto]:
What IPv6 address should be used? (CIDR subnet notation, “auto” or “none”) [default=auto]:
Would you like the server to be available over the network? (yes/no) [default=no]: yes
Address to bind to (not including port) [default=all]:
Port to bind to [default=8443]:
Would you like stale cached images to be updated automatically? (yes/no) [default=yes]:
Would you like a YAML "init" preseed to be printed? (yes/no) [default=no]:

```
