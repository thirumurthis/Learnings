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

root@thirumurthi-HP:~# ip netns exec red arp
Address                  HWtype  HWaddress           Flags Mask            Iface
192.168.15.2             ether   62:e5:b4:e3:22:e3   C                     veth-red
root@thirumurthi-HP:~# ip netns exec blue arp
Address                  HWtype  HWaddress           Flags Mask            Iface
192.168.15.1             ether   3e:9d:e0:b6:c9:25   C                     veth-blue

```
