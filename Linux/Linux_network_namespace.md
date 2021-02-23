### How to create an network namespace and connect each other for communication.

#### Lets start with two namespace and try to connect.

`Pre-requisites:` - Ubunutu or Centos box. I had a Windows 10, WSL2 where installed Ubuntu 20.04

#### To create a new network namespace (netns)
```
$ ip netns add red
$ ip netns add blue
```

#### To list the network namespace created
```
$ ip netns 
or 
$ ip netns list
```

#### To execute any ip command in the network namespace
```
$ ip netns exec red ip link list
### ip netns exec <namespace-name> <command>
### below are few other commands
$ ip netns exec red ifconfig
$ ip netns exec red ping
```

##### To execute any ip command, in short form
```
$ ip -n red link
## this is similar to the command (above) referened below
$ ip netns exec red ip link

## other commands that work this approach are 
$ ip -n red ip add
```

#### To create a virutal link/ pipe to the two namespace
```
$ ip link add veth-red type veth peer name veth-blue

### where veth-red and veth-blue are interface connecting the namespace
```

#### Now check the status of the interface created with each namespace, both should be down
```
$ ip netns exec red ip link
$ ip netns exec blue ip link

root@thirumurthi-HP:~# ip netns exec red ip link
1: lo: <LOOPBACK> mtu 65536 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
2: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/sit 0.0.0.0 brd 0.0.0.0
9: veth-red@if8: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ether 3e:9d:e0:b6:c9:25 brd ff:ff:ff:ff:ff:ff link-netns blue
```

#### Add ip address to the namespace
```
$ ip -n red addr add 192.168.15.1/24 dev veth-red
$ ip -n blue addr add 192.168.15.2/24 dev veth-blue

###
dev - is device
also, only using network mask /24, the ping command is worked
when connection is established
```

#### Start the interface so we can connect to it
```
$ ip -n red link set veth-red up
$ ip -n blue link set veth-blue up

## now check the status of the interface
$ ip -n red link
$ ip -n blue link
```

#### To connect to the namespace and ping for connection
```
$ ip netns exec red ping 192.168.15.2
$ ip netns exec blue ping 192.168.15.1

root@thirumurthi-HP:~# ip netns exec blue ping 192.168.15.1
PING 192.168.15.1 (192.168.15.1) 56(84) bytes of data.
64 bytes from 192.168.15.1: icmp_seq=1 ttl=64 time=0.118 ms
64 bytes from 192.168.15.1: icmp_seq=2 ttl=64 time=0.054 ms
64 bytes from 192.168.15.1: icmp_seq=3 ttl=64 time=0.065 ms
```

#### Check the `arp` table for each command. if the tool is not available install using `apt install net-tools`
```
$ ip netns exec red arp
$ ip netns exec blue arp

## check the arp table, note the ip address references

root@thirumurthi-HP:~# ip netns exec red arp
Address                  HWtype  HWaddress           Flags Mask            Iface
192.168.15.2             ether   62:e5:b4:e3:22:e3   C                     veth-red
root@thirumurthi-HP:~# ip netns exec blue arp
Address                  HWtype  HWaddress           Flags Mask            Iface
192.168.15.1             ether   3e:9d:e0:b6:c9:25   C                     veth-blue
```

#### To cleanup or delete all the networknamespace
```
$ ip netns delete red
$ ip netns delete blue
```

### Points:
 - Both the namespace is isolated from the host.
 - In the above case, we only had two namespace, if we need to connect to many namespaces, we need to create a virtual switch.
------------

#### How to create virtual switch, so namespaces can connect to each other.
  - Create a network switch within a host, which will allow to connect when there are more than two namespace.
  - There are different options for virtual switch
    - `Linux Bridge` (this is native to linux, we will see how to use this)
    - `Open vSwitch (OvS)`

- To `create an internal bridge network on the host`, we add a new interface to the host using below command
```
$ ip link add v-net-0 type bridge

### note the type is set to bridge
```
 - NOTE: For the host, the v-net-0 is another interface like `eth0`.
 - Check the interface v-net-0 created in the host using below command,
```
$ ip link

root@thirumurthi-HP:~# ip link
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN mode DEFAULT group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
4: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP mode DEFAULT group default qlen 1000
    link/ether 00:15:5d:87:59:7e brd ff:ff:ff:ff:ff:ff
10: v-net-0: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ether 4e:21:eb:c3:53:95 brd ff:ff:ff:ff:ff:ff
```
  - Note: The created interface is DOWN. This needs to be started up. Use below command,

```
$ ip link set dev v-net-0 up
```
![image](https://user-images.githubusercontent.com/6425536/108674776-a8c1f080-749a-11eb-9002-3eeb9ba32173.png)

 - In the first section, we created a _veth-blue_ and _veth-red_ interface, which needs to be delete using below command. Deleting one will delete the other interface.
```
$ ip -n red link del veth-red
$ ip -n blue link del veth-blue

### check if the link is delete using below  (should not display veth-red)
$ ip -n red link
```

#### Create a new virtual pipe/wire, to connect the namespace with the virtual swtich

```
$ ip link add veth-red type veth peer name veth-red-br
[ veth-red ----------------- veth-red-br]

$ ip link add veth-blue type veth peer name veth-blue-br
[ veth-blue ----------------- veth-blue-br]
```

#### Now, the links (virtual pipe/wire) needs to be attached to the namespace 
```
## attaching the veth-red to the namespace
$ ip link set veth-red netns red 

## connect the interface to the bridge network created usign master
$ ip link set veth-red-br master v-net-0

## same above for blue namespace as well
$ ip link set veth-blue netns blue
$ ip link set veth-blue-br master v-net-0
```

#### Since we deleted the namespace created earlier, now lets add ip address (same in above section) here as well.

- To add ip address to the namespace
```
$ ip -n red addr add 192.168.15.1/24 dev veth-red
$ ip -n blue addr add 192.168.15.2/24 dev veth-blue
```

#### Now, lets start the interface.
```
$ ip -n red link set veth-red up
$ ip -n blue link set veth-blue up

$ ip link set veth-red-br up
$ ip link set veth-blue-br up
```

#### Trobuleshoot using `ip netns exec red arp`, `ip netns exec red ifconfig`,etc.
#### Now we connect to each other namespace. Also if we create multiple namespace, we can add those the virtual switch, and it will be able to connect with each other.

- Now the namespaces are all in isolated private network.
- if we use `ping 192.168.15.1` from the host will not connect to the namespace.

```
root@thirumurthi-HP:~# ip netns exec red ping 192.168.15.2
PING 192.168.15.2 (192.168.15.2) 56(84) bytes of data.
64 bytes from 192.168.15.2: icmp_seq=1 ttl=64 time=0.871 ms
64 bytes from 192.168.15.2: icmp_seq=2 ttl=64 time=0.131 ms
64 bytes from 192.168.15.2: icmp_seq=3 ttl=64 time=0.107 ms
^C
--- 192.168.15.2 ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2079ms
rtt min/avg/max/mdev = 0.107/0.369/0.871/0.354 ms

--------------
root@thirumurthi-HP:~# ip netns exec blue ping 192.168.15.1
PING 192.168.15.1 (192.168.15.1) 56(84) bytes of data.
64 bytes from 192.168.15.1: icmp_seq=1 ttl=64 time=0.122 ms
64 bytes from 192.168.15.1: icmp_seq=2 ttl=64 time=0.115 ms
64 bytes from 192.168.15.1: icmp_seq=3 ttl=64 time=0.096 ms
64 bytes from 192.168.15.1: icmp_seq=4 ttl=64 time=0.117 ms
^C
--- 192.168.15.1 ping statistics ---
4 packets transmitted, 4 received, 0% packet loss, time 3100ms
rtt min/avg/max/mdev = 0.096/0.112/0.122/0.009 ms
```

#### Since the `v-net-0` interface is another dev in the host, in order to connect to this network from the host, we need to add an ip address to this host.

```
$ ip addr add 192.168.15.5/24 dev v-net-0
```
- Still the network namespace are private, cannot be connected from internet.

##### Since we have added a new ip address to the v-net-0, we will be able to access the red, blue namespace as well
```
### below is from the host machine

root@thirumurthi-HP:~# ping 192.168.15.1
PING 192.168.15.1 (192.168.15.1) 56(84) bytes of data.
64 bytes from 192.168.15.1: icmp_seq=1 ttl=64 time=0.262 ms
64 bytes from 192.168.15.1: icmp_seq=2 ttl=64 time=0.094 ms
64 bytes from 192.168.15.1: icmp_seq=3 ttl=64 time=0.093 ms
64 bytes from 192.168.15.1: icmp_seq=4 ttl=64 time=0.081 ms
^C
--- 192.168.15.1 ping statistics ---
4 packets transmitted, 4 received, 0% packet loss, time 3109ms
rtt min/avg/max/mdev = 0.081/0.132/0.262/0.074 ms
```

#### Now we have a network setup, this 

