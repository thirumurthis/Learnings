### How to create an network namespace and connect each other for communication.

#### Lets start with two namespace and try to connect.

`Pre-requisites:` 
   - Ubunutu or Centos box. I had a Windows 10, WSL2 where installed Ubuntu 20.04

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

#### In order to `list the network interface` on the host machine
```
$ ip link
# loopback interface - is the localhost
# eth0 interface will also be displayed
```

#### To execute any ip command in the network namespace
  - below `ip link list` command when executed using `ip netns exec <name-space>` will execute within that network namespace
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
## the above command is similar to the command (above) referened below (-n is namespace switch)
$ ip netns exec red ip link

## other commands that work this approach are 
$ ip -n red ip add
```

##### With the network namespace created, we can check within that namespace for existence of `arp` and `route` table.
```
$ ip netns exec blue arp
$ ip netns exec blue route

## in above both case  there shouldn't be any results
```

#### To establish network connection 
##### Create a virutal link/ pipe to the two namespace. Creating interface.
  - In case of connecting two machine, we require a cable, in this case connection two namespace we require `virtual ethernet peer` (often called as `pipe`).
  - This something like a virtual cable with two interface at the end.
  - 2 steps are involved in creating and attaching the interface to network namespace
 
 ### **Step 1:** Create the link for each namespace that needs to pair.
  - with the below command the red (with name veth-red) and blue (with name veth-blue) namespace are linked
```
$ ip link add veth-red type veth peer name veth-blue

### where veth-red and veth-blue are interface connecting the namespace
```
### **Step 2:** Attach the interface to the namespace using below command:
```
$ ip link set veth-red netns red
$ ip link set veth-blue netns blue
```

##### Now check the status of the interface created with each namespace, both should be down
```
$ ip netns exec red ip link
$ ip netns exec blue ip link

root@thiru-HP:~# ip netns exec red ip link
1: lo: <LOOPBACK> mtu 65536 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
2: tunl0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ipip 0.0.0.0 brd 0.0.0.0
3: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/sit 0.0.0.0 brd 0.0.0.0
8: veth-red@if7: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ether f2:6b:bf:d7:77:e3 brd ff:ff:ff:ff:ff:ff link-netns blue
    
root@thirumurthi-HP:~# ip netns exec blue ip link
1: lo: <LOOPBACK> mtu 65536 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
2: tunl0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ipip 0.0.0.0 brd 0.0.0.0
3: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/sit 0.0.0.0 brd 0.0.0.0
7: veth-blue@if8: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ether ba:ba:9f:65:9f:99 brd ff:ff:ff:ff:ff:ff link-netns red
```
 ### **Step 3:** Assign the ip address using `ip addr` command,  within each namespace for communication.
 
##### Add ip address to the namespace, using 
```
$ ip -n red addr add 192.168.15.1/24 dev veth-red
$ ip -n blue addr add 192.168.15.2/24 dev veth-blue

### Note
dev - is device
also, only using network mask /24, the ping command will work
when connection is established
```
### **Step 4:** Start the interface in the namespace, so both namespace can communicate with each other

##### Start the interface so we can connect to it
```
$ ip -n red link set veth-red up
$ ip -n blue link set veth-blue up

## now check the status of the interface
$ ip -n red link
$ ip -n blue link
```
  - Output, after starting the interface in specific namespace. State of veth-red/blue are UP.
```
root@thiru-HP:~# ip netns exec red ip link
1: lo: <LOOPBACK> mtu 65536 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
2: tunl0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ipip 0.0.0.0 brd 0.0.0.0
3: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/sit 0.0.0.0 brd 0.0.0.0
8: veth-red@if7: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP mode DEFAULT group default qlen 1000
    link/ether f2:6b:bf:d7:77:e3 brd ff:ff:ff:ff:ff:ff link-netns blue
    
root@thiru-HP:~# ip netns exec blue ip link
1: lo: <LOOPBACK> mtu 65536 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
2: tunl0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/ipip 0.0.0.0 brd 0.0.0.0
3: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN mode DEFAULT group default qlen 1000
    link/sit 0.0.0.0 brd 0.0.0.0
7: veth-blue@if8: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP mode DEFAULT group default qlen 1000
    link/ether ba:ba:9f:65:9f:99 brd ff:ff:ff:ff:ff:ff link-netns red
```

##### To connect to the namespace and ping for connection
```
$ ip netns exec red ping 192.168.15.2
$ ip netns exec blue ping 192.168.15.1

root@thirumurthi-HP:~# ip netns exec blue ping 192.168.15.1
PING 192.168.15.1 (192.168.15.1) 56(84) bytes of data.
64 bytes from 192.168.15.1: icmp_seq=1 ttl=64 time=0.118 ms
64 bytes from 192.168.15.1: icmp_seq=2 ttl=64 time=0.054 ms
64 bytes from 192.168.15.1: icmp_seq=3 ttl=64 time=0.065 ms
```
- Simple check to see the IP address on that namespace
```
root@thiru-HP:~# ip netns exec blue ifconfig
veth-blue: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 192.168.15.2  netmask 255.255.255.0  broadcast 0.0.0.0
        inet6 fe80::b8ba:9fff:fe65:9f99  prefixlen 64  scopeid 0x20<link>
        ether ba:ba:9f:65:9f:99  txqueuelen 1000  (Ethernet)
        RX packets 23  bytes 1930 (1.9 KB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 23  bytes 1930 (1.9 KB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

root@thiru-HP:~# ip netns exec red ifconfig
veth-red: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 192.168.15.1  netmask 255.255.255.0  broadcast 0.0.0.0
        inet6 fe80::f06b:bfff:fed7:77e3  prefixlen 64  scopeid 0x20<link>
        ether f2:6b:bf:d7:77:e3  txqueuelen 1000  (Ethernet)
        RX packets 23  bytes 1930 (1.9 KB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 23  bytes 1930 (1.9 KB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
```

##### Check the `arp` table for each command. if the tool is not available install using `apt install net-tools`
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
### **Step 5:** Clean up the namespace created

##### To cleanup or delete all the networknamespace
```
$ ip netns delete red
$ ip netns delete blue
```

### Points:
 - Both the namespace is isolated from the host, so the ip route and arp table are also isolated the host now doesn't have any idea of it.
 - In the above case, we only had two namespace, if we need to connect to many namespaces, we need to create a virtual switch.
 - Simple Representation

![image](https://user-images.githubusercontent.com/6425536/147781115-9897ea4c-78cb-4789-ae94-6962854abea8.png)

------------

#### How to create `virtual switch`, so namespaces can connect to each other. _When there are more number of network namespace to connect_
  - Create a network switch within a host, which will allow to connect when there are more than two namespace.
  - There are different options for virtual switch
    - `Linux Bridge` (this is native to linux, we will see how to use this)
    - `Open vSwitch (OvS)`

##### Similar step as above will follow here as well
  - Step 1: Create a link or interface of type `bridge`. (use, `ip link add v-net-o type bridge`) and start it up.
  - Step 2: Create network namespace like red, blue, orange, purple, etc. (use `ip netns add red`)
  - Step 3: For each namespace create link or interface to corresponding netnamespace to connect to the `bridge`. (use `ip link add veth-red type veth peer name veth-red-br`)
            - `bridge` are like cables with interface at the end.
  - Step 4: Attach the interface to the bridge (cable to be attached to bridge). (use `ip link set veth-red netns red` and `ip link set veth-red-br master v-net-0`)
            - since we are creating a bridge type, we need to connect both the interfaces one to the netns and another in virtual switch (v-net-0).
  - Step 5: Add `ip address` for each net namespace. (use `ip -n red addr add 192.168.15.1/24 dev veth-red`)

 - Simple representation
 ![image](https://user-images.githubusercontent.com/6425536/147783293-79907ab4-fbb1-4ef5-b466-87931ceac214.png)

### **Step : 1** - creating bridge interface
##### Below details the above steps:
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

### **Step 1.1:** - Start the virtual bridge network interface.
```
$ ip link set dev v-net-0 up
```
![image](https://user-images.githubusercontent.com/6425536/108674776-a8c1f080-749a-11eb-9002-3eeb9ba32173.png)

 - In the first section, we created a _veth-blue_ and _veth-red_ interface, which needs to be delete using below command. 
 - Deleting one will delete the other interface.
```
$ ip -n red link del veth-red
$ ip -n blue link del veth-blue

## Note: Executing anyone of the above command will delete the other interface.

### check if the link is delete using below  (should not display veth-red)
$ ip -n red link
```

### **STEP 3:** - For the namespace create a interface or cable.

#### Create a new virtual pipe/wire (cable), to connect the namespace with the virtual swtich

```
$ ip link add veth-red type veth peer name veth-red-br
[ veth-red ----------------- veth-red-br]

$ ip link add veth-blue type veth peer name veth-blue-br
[ veth-blue ----------------- veth-blue-br]
```
### **STEP 4:** - Attach the interface at the virtual switch and the net namespace itself
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

### **STEP 5:** - Add ip address to the netnamespace.
#### Since we deleted the namespace created earlier, now lets add ip address (same in above section) here as well.

- To add ip address to the namespace
```
$ ip -n red addr add 192.168.15.1/24 dev veth-red
$ ip -n blue addr add 192.168.15.2/24 dev veth-blue
```

### **STEP 5.1:** - Start up the service

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
root@thiru-HP:~# ip netns exec red ping 192.168.15.2
PING 192.168.15.2 (192.168.15.2) 56(84) bytes of data.
64 bytes from 192.168.15.2: icmp_seq=1 ttl=64 time=0.871 ms
64 bytes from 192.168.15.2: icmp_seq=2 ttl=64 time=0.131 ms
64 bytes from 192.168.15.2: icmp_seq=3 ttl=64 time=0.107 ms
^C
--- 192.168.15.2 ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2079ms
rtt min/avg/max/mdev = 0.107/0.369/0.871/0.354 ms

--------------
root@thiru-HP:~# ip netns exec blue ping 192.168.15.1
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

NOTE:
  - The created network namespace and the virtual switch created is not accessible by host network. But when pining from the netns red, blue, etc. each can communicate.
  - The host is one network and the namespace in the another network. So can't communicate.
  - Actually the virtual switch bridge is another `network interface` on the host with an ip address 192.168.15.0 pointing to the net namespace.
  - In order to make this network bridge interface to communicate to the host machine/laptop, add an ip address to it. Refer below info for details.


#### Since the `v-net-0` interface is another device (dev) in the host, in order to connect to this network from the host, we need to add an ip address to this host.

```
$ ip addr add 192.168.15.5/24 dev v-net-0
```

```
#### Below is the route table after, adding the up address to the network virtual switch (executing above command)

root@thiru-HP:~# route
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
default         thiru-HP. 0.0.0.0         UG    0      0        0 eth0
172.x.x.x       0.0.0.0         255.255.240.0   U     0      0        0 eth0
192.168.15.0    0.0.0.0         255.255.255.0   U     0      0        0 v-net-0
```
- Still the network namespace are private, _cannot_ be connected from internet.

##### Since we have added a new ip address to the v-net-0, we will be able to access the red, blue namespace as well
```
### below is from the host machine

root@thiru-HP:~# ping 192.168.15.1
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

#### Now we have a network setup, this network is not reached from outside or this network cannot reach the external network.

- say, if there is an external network with ipaddress (192.168.1.3), if we use 
```
$ ip netns exec blue ping 192.168.1.3
## The ping cannot connect to the network since it is not access external network

$ ip netns exec blue ping www.google.com
```

#### In order to access the external network, we need add a `Gateway`.
- add an entry in the route table.
- How to find the gateway.
   - **Gateway is the system on the local network that connects to other network**
   - The `localhost` is the system which has all the namespace, in here the `v-net-0` interface.
   - So the `localhost` (v-net-0) is the gateway that connects the networks together.
   
```
## route table of the blue namespace
$ ip netns exec blue route

root@thiru-HP:~# ip netns exec blue route
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
192.168.15.0    0.0.0.0         255.255.255.0   U     0      0        0 veth-blue
```
- Add a route entry in the blue namespace, to route all the traffic through gateway.
```
$ ip netns exec blue ip route add 192.168.1.0/24 via 192.168.15.5

## 192.168.15.5 - is v-net-0
```
- The host has two ip address, 
   -  1. on bridge network (192.168.15.5)
   -  2. another on the external network (blue) 192.168.15.2
 - We cannot use the above two ip address (in the route) from the blue namespace.
 - The blue namespace can reach the gateway in its local network 192.168.15.5
 - The default gateway should be reachable from the blue namespace, when add it to route.
 - Though the gateway is added, from the blue namespace we can use ping but NO response will be received. (refer the last command output below)
 
 ```
root@thiru-HP:~# ip netns exec blue ip route add 192.168.1.0/24 via 192.168.15.5
root@thiru-HP:~# ip netns exec blue route
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
192.168.1.0     192.168.15.5    255.255.255.0   UG    0      0        0 veth-blue
192.168.15.0    0.0.0.0         255.255.255.0   U     0      0        0 veth-blue
root@thiru-HP:~#
root@thiru-HP:~# ip netns exec blue ping 192.168.1.3
PING 192.168.1.3 (192.168.1.3) 56(84) bytes of data.
^C
--- 192.168.1.3 ping statistics ---
4 packets transmitted, 0 received, 100% packet loss, time 3158ms
 ```

 - The blue namespace is not able to recive response back, this is because similar to a situation where from home network when we reach the internet using router. 
 - The home network has a private ip address which is not resolved by the internet this is where the NAT comes into play.
 
 - How to make the blue namespace access the external network, we need a NAT enabled on the host acting as a gateway. So it can send message using own name and own address.
 - How to enable NAT, add a new rule in the `iptables`

```
$ iptables -t nat -A POSTROUTNG -s 192.168.15.0/24 -j MASQUERADE

### Add a new rule in NAT iptable to the POSTROUTING chain to masquerade or replace  the from address of all packets coming from the source network 192.168.15.0 with its own ip address. Thus when any one recieving the packets from this network will think this is coming from the host not from the namespace.
```
- After performing the above step , we should be able to access the external network.

#### If the net namespace need to access the internet. It cannot be accessed, since the routing table doesn't have routes to the network 192.168.1.0 to connect to any thing else.

#### To reach external network, connect to internet talk to host by adding the default gateway
```
$ ip netns exec blue ip route add default via 192.168.15.5

root@thiru-HP:~# ip netns exec blue ip route add default via 192.168.15.5
root@thiru-HP:~#
root@thiru-HP:~# ip netns exec blue route
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
default         192.168.15.5    0.0.0.0         UG    0      0        0 veth-blue
192.168.1.0     192.168.15.5    255.255.255.0   UG    0      0        0 veth-blue
192.168.15.0    0.0.0.0         255.255.255.0   U     0      0        0 veth-blue
```
- Now after adding the default gateway we can access the external network
```
root@thiru-HP:~# ip netns exec blue ping www.google.com
PING www.google.com (172.217.14.228) 56(84) bytes of data.
64 bytes from sea30s02-in-f4.1e100.net (172.217.14.228): icmp_seq=1 ttl=114 time=23.4 ms
64 bytes from sea30s02-in-f4.1e100.net (172.217.14.228): icmp_seq=2 ttl=114 time=14.6 ms
64 bytes from sea30s02-in-f4.1e100.net (172.217.14.228): icmp_seq=3 ttl=114 time=17.5 ms
^C
--- www.google.com ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2004ms
rtt min/avg/max/mdev = 14.605/18.517/23.415/3.663 ms
```

### How to connect the blue namespace from the external network.
  - Say if there is an web application in the blue namespace.
  - Two ways to make this possible,
    - 1. give away the private identity of private network to second host. (not preferred)
        - Add ip route entry to second host, telling the n/w 192.168.1.2
    - 2. add port forwarding rule in iptables using below command
     ```
     $ iptables -t nat -A PREROUTING --dport 80 --to-destination 192.168.15.2:80 -j DNAT
     
     ### above iptables command tells any traffic comming to port 80 on localhost, is to be forwarded to port 80 on the ip assinged to the blue namespace
     ```
     
<details>
    <summary> reference </summary>
    
Reference [.](https://www.youtube.com/watch?v=j_UUnlVC2Ss)
</details>





