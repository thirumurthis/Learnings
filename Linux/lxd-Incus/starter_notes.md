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
$ mkdir -p /etc/apt/keyrings/
$ curl -fsSL https://pkgs.zabbly.com/key.asc -o /etc/apt/keyrings/zabbly.asc
```

Add the repository 
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
sudo apt incus
```
