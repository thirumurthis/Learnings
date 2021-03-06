Docker container network contians three major part:
 - __Container network model (CNM)__
 - __libnetwork__
 - __drivers__
 
 
`Cotainer network model` is a design specification for docker container, originated from `Docker Inc`.
  - Check the specs at [git link](https://github.com/docker/libnetwork/blob/master/docs/design.md).

`Container network interface` is a rivial for CNM, and used in Kubernetes environment, orgintated from `CoreOs, Inc.`


`CNM` defines three main constructs
  - `Sandbox` (a.k.a namespace in linux kenrel)
     - Kind of fenced area of OS or isolated area of OS, where we can tweak and do changes in isolation without impacting other area.
     - Once the sandbox is created, inside of it network stacks can created like ipconfig, DNS, Routing table and all network needed.
     - Sandobx is like a container without no app running inside it.
    
 - `Endpoint` (Network interace)
     - like eth0 in the linux world
     - like LAN in the Windows world
     
 - `Network` 
     - connected endpoints.
        
`Libnetwork` (canoncial implementation of CNM)
   - libnetwork is the actual implementation of the CNM developed in Go/Golang code by Docker Inc.
   - documentation details [git link](https://github.com/docker/libnetwork)
   - `socketplane` startup acquisation rewrote the whole Docker network and made it more robust, scalable.
   - The Libnetwork is written in Golang, so it is cross platform runs in ARM, etc.
   - plugable architecture
   
`Drivers` (network specific detail implementation)
   - when `libnetwork` implements and defines the fundametal sandbox, endpoint, network, management API, UX etc. 
   the actual specifics of different network types like local bridge network, multihost overlay leverage VXLAN are implemented in `drivers`
  
Type of drivers:
  - `local` => native
  - `remote` => third party drivers implementing the specs defined by libnetwork.
  
  | CNM | Libnetwork | Drivers |
  | --- | ----- | ----|
  |Design/DNA| Control plane & management plane| Data plane|
  
  
   ![image](https://user-images.githubusercontent.com/6425536/80312818-5a6d9380-879c-11ea-8406-392323e22eed.png)

`network` is a sub command within the docker

```
$ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
2fc7116b9f21        bridge              bridge              local
9115882c1e94        host                host                local
ce0dd5291963        none                null                local
```
`Note about the docker network output`:
  - NAME are arbitary, not necessary to be the same as the DRIVER.
  - SCOPE are local which means single-host, SWARM scope are multi-host.

__docker network inspect \<id or name from the network ls above\>__

```
$ docker network inspect bridge                                                                               [
    {
        "Name": "bridge",
        "Id": "2fc7116b9f2115d8ed3958215c71896f0f7f16ff86535024303688e5c9d0077b",
        "Created": "2020-04-26T15:27:34.010794339Z",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        "Labels": {}
    }
]
```

`Notes about the above command output`:
  - Ipv6 is not configured. Refer the `EnableIPv6` attribute.
  - IPAM (ip address management) - This is for specific to this network.
     - default one is used with subnet/ gateway, this pluggable.
  - This is not an internal network refer `internal` attribute.
  - Containers are not connected to this network, refer `Containers` attribute
  - Which bridge is this docker connected to in the case `docker0`, refer `com.docker.netowrk.bridge.name`
     - The options info might be different based on OS architecture (on X64 machine), widows will display different tools.
  
##### `docker network connect <container-name-or-id>` - to connect to the container network.

##### `docker info` - displays the container info, check the Network it is connected to.

----

### Single-host Networking 

How to create a user defined bridge network on a clean docker host? (below is on linux)

We will create a single host bridge network named, demo-bridge.

Note: The demo-bridge will be that specific Docker host, scope : local. If we have another docker host and create an single host named demo-bridge, this would be totally isolated.  

Say there are two docker host, namely host1 and host2. Containers on the host1 can't talk to container on host2 even if the bridge name are same on both host.
  
Let's create `802.1d bridge` device on docker single host. 

What is `bridge` network? 
  - `bridge` is a network term, but in VM world it means `virtual switch`.
  - it is also known as `vswitch`.

Creating a `802.1d bridge` network is going to create a virtual bridge/virtual switch inside the docker host. In the linux world this is like creating a linux bridge inside the Kernel. 

`Notes:` 
The docker bridge driver on a linux system, actually leverages the tested, mature, stable, fast linux bridge. Since it is in the kernel 2.0 it is fast.

`docker network create` with bridge driver will create a vswitch. This is entirely a  software vswitch. Once this is completed, we can set containers in the host.

```
$ docker network create -d bridge --subnet 10.0.0.1/24 demo-bridge
## outputs id of network, with hashed value.

// use network ls command to see if the bridge is created
$ docker network ls

// use inspect to see the config
$ docker network inspect demo-bridge
## outputs the network configuration in json format

## docker0 would be the default bridge
## demo-bridge is the newly created bridge with 802.1d standard
```

```
# list the bridge info in the linux both the docker0 and created one
$ ip link show 
```
-------
Tips: 

In ubuntu linux to investigate the bridge network, install the package from `apt-get` package manager called `bridge-utils`. `$ sudo apt-get install bridge-utils`

Once package is installed, use `$ brctl show` command to list the bridge info from kernel.

---------

#### Creating a container and attaching in the single host

```
# -d = detach mode
# -t = tty (terminal - psedo terminal)
# alpine is the image name. othe example, debian
# sleep - container will be live for a day

$ docker run -dt --name container1 --network demo-bridge alpine sleep 1d
## output information about the container info.

$ docker run -dt --name container2 --network demo-bridge alpine sleep 1d
```

Now inspecting the network will list the containers 
```
$ docker network inspect demo-bridge
### in output check the container attribute
### this means that both the containers can talk to each other 
### since has got ip and mac address.

[
    {
        "Name": "demo-bridge",
        "Id": "3225b6d141778b5b6c5a8007a8e4d82bbd1b41bc4f94339f0ff3c345242e2b5e",
        "Created": "2020-04-26T19:37:50.796371923Z",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "10.0.0.1/24"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {
            "a17ca1c4c7778f5fa352b131188663df631e0fc506a75027ccecc56758a22e8e": {
                "Name": "container1",
                "EndpointID": "d61969f1c1a0ad6e812ad708c015af76feb5e2d8bc52ccbf07db7b31e40c2e49",
                "MacAddress": "02:42:0a:00:00:02",
                "IPv4Address": "10.0.0.2/24",
                "IPv6Address": ""
            },
            "e4d7fb1e3aeda536f8158d21be2914796ee198ab61057945a2a6f6deb87b36ad": {
                "Name": "container2",
                "EndpointID": "a542e5e41291a85c48db2273a737306cbcd3c9bae6ede79d1cf4ada9e1d8e142",
                "MacAddress": "02:42:0a:00:00:03",
                "IPv4Address": "10.0.0.3/24",
                "IPv6Address": ""
            }
        },
        "Options": {},
        "Labels": {}
    }
]
```

Info: After the containers are attached to the host, then `brctl show` command on the demo-bridge will list two interface ids for two containers.

Representation:

![image](https://user-images.githubusercontent.com/6425536/80316730-4897ea80-87b4-11ea-8d51-5e490094cba9.png)

##### Testing the connectivity between the container1 and container2

```

$ docer exec -it container1 sh
# takes to the terminal of the container1

# Within the contained
$ ip a
## outputs the list of network
$ ping <ip-address-of-other-container-obtained-by-network ls-containers-attribute>
## output should see the ping successfully sending and receiving pacakges.
-----
/ # ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
2: sit0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN qlen 1000
    link/sit 0.0.0.0 brd 0.0.0.0
8: eth0@if9: <BROADCAST,MULTICAST,UP,LOWER_UP,M-DOWN> mtu 1500 qdisc noqueue state UP
    link/ether 02:42:0a:00:00:02 brd ff:ff:ff:ff:ff:ff
    inet 10.0.0.2/24 brd 10.0.0.255 scope global eth0
       valid_lft forever preferred_lft forever
       
/ # ping container2
PING container2 (10.0.0.3): 56 data bytes
64 bytes from 10.0.0.3: seq=0 ttl=64 time=0.181 ms
64 bytes from 10.0.0.3: seq=1 ttl=64 time=0.185 ms
64 bytes from 10.0.0.3: seq=2 ttl=64 time=0.180 ms
64 bytes from 10.0.0.3: seq=3 ttl=64 time=0.186 ms
64 bytes from 10.0.0.3: seq=4 ttl=64 time=0.182 ms
^C
--- container2 ping statistics ---
5 packets transmitted, 5 packets received, 0% packet loss
round-trip min/avg/max = 0.180/0.182/0.186 ms

```

##### How does docker container was able to communicate with each other in above configuration?

Starting docker 1.10 version, the every docker engine has embedded DNS server built in.

So when we create a container with the name flag, the entry is added with the DNS server. And anyother container on the same network can ping it by the name.


#### How to make the container accessible to external world?
##### (or) In other words, how to make the container in one host to talk to another container in different host? 
##### (or) In other words if the containers on a bridge network to be accessed outside of that network, another network or host?

We need to publish the container service on the host network. 

Right now the bridge created above, is not accessible to outside world.

This is where the port forwarding comes in place, in case of an web server application.

__` -p (host-port):(container-port)`__

```
# start the container with exposed port
$ docker run -d --name web-container1 --network demo-bridge -p 5050:8080 mywebapp
```

Representation:

![image](https://user-images.githubusercontent.com/6425536/80317480-e392c380-87b8-11ea-940f-6e6549cf1e48.png)

Get the public external ip address of the network and use that with the port, `73.108.109.22:5050`.

------------------

### Multi-host Overlay

There are multiple docker host on different network and each connected by router. These are easily scalable.

The `docker network create` a single a new network (layer 2 network/ layer 2 broadcast) expands to multple host.

And container any of the host on the different network can talk, no need for port mapping. 

This is achived by `docker network create <options>`

Representation:

![image](https://user-images.githubusercontent.com/6425536/80322359-63c92100-87d9-11ea-9989-dfad6a0b49c4.png)

##### What happens in the background on multi-host overlay network communication in docker?

Lets assume that there are two host/node in separate network connected by router.

On each host we build a sandbox network, and within this sandbox we build the network stack. In this case a single bridge network (eg. brdg0).

Then a VXLAN tunnel endpoint gets created and setup to that bridge. The eventually a VXLAN tunnel is established, this tunnel is the overlay network. This overlay tunnel is a single layer 2 broadcast domain.

A layer 2 broadcast domain in network means any node connecting to it will get an ip address that can talk directly to each other without a need for router.

The physical router doesn't knows aything about the traffic but it is still being used. The VXLAN encapsulation of packets in this stuff takes care of all of that transparently. This is called the __`layer 2 adjacency`__.

Then create container in the host/node and set them in the overlay, the docker creating an virtual Eternet adapter inside the container and virutally cabeling to the bridge brdg0, will communicate with each other directly over the tunnel.

All the above is hidden, and happens behind the scene. like TCP/UDP, ip address, mac address, etc.

Representation:

![image](https://user-images.githubusercontent.com/6425536/80322888-c1ab3800-87dc-11ea-89db-e6b6b82ef796.png)

Demo:

Open up the necessary ports 2789/upd, 2946/tcp/udp, 2377/tcp and Enable swarm mode on both nodes. Swarm node provide more handful tool

Node 1:
```
# creating a 2 node swarm mode.
$ docker swarm init
## now the node1 is in swarm mode and running as a manager.
## There will also be the command displayed for the node2 that needs to make this work.

## after perofroming the node2 operation

$ docker node ls
## display the node information.

$ docker network ls 
## displays teh network information could see an ingress (named) network using overlay driver with scope swarm.

# then create a network
$ docker network create -d overlay demo-overlay 
## 

$ docker network ls
## output should create a overlay created networks
## at this point check the node2 (Without any container being created in there
## the docker network ls command will not display the overlay we created here
## this is because docker uses lazy approach to setup the network)
## now in node2, create a container and run it at this point.

# create service in node1
$ docker service create --name demo-service --network demo-overlay --replicas 2 alpine sleep 1h

$ docker service ps demo-service
## displays tasks and replicas running on that node.

$ docker network inspect
## this displays info of the vxland id and the container info.
## peers network info too.
````

Node 2:
```
# the below command is from the node1
$ docker swarm join --token....
## node 2 joins the node1 in swarm mode.
## node 2 will be the worker now.

# after the overlay network is created in the node1
$ docker network ls

## create a container, it needs to use the demo-overlay network
## to create service in node 1, since we need to have service.

$ docker network ls
## will display the created network
## we created the service in the container in the node1 and the load is dispatched over 
## this node using replicas

$ docker network inspect demo-overlay
## displays only this container.
## both nodes are on different network and on different underlay (physical router) network.

```
------------

### MACVLAN driver

MACVLAN is a Linux specific driver, there is no similar driver for windows. 

Both `bridge` and `overlay` network driver are good but massively container centric. Both of these are great for connecting containers.

What if we need to connect containers to an exising VLANs, exisiting VMs and existing physical service? 

What we need to do if have to plum containers into these existing networks, this is where MACVLAN comes in.

MACVLAN's makes the container visible and accessible on existing network and VLANs.

MACVLANs VS Windows L2Bridge 
The MACVLAN gives the very own IP and gets its own MAC address to the container on existing network.

The widnows L2bridge network, give very own IP address and All containers share a common MAC address.

With two node containing two container, with MACVLAN those containers are  treated as indvidual node like on existing network.

Represenaton:

![image](https://user-images.githubusercontent.com/6425536/80324955-1902d580-87e8-11ea-9794-4cb05aba2d7f.png)

To make the MACVLAN to wokr, the network interface should be in `PROMISCUOUS MODE`. 
 - Most of the public cloud providers don't allow it.
 - To overcome it we use IPVLAN, since MACVLAN difficulties.
 
 ### IPVLAN driver

This is also similar to MACVLAN.

The IPVLAN also doesn't have bridge. The difference between the MACVLAN and the IPVLAN is the way IPVLAN handling the MAC address.
 
 `MACVLAN` - Allocates each sub interface with its own ip and own mac address.
 
 `IPVLAN` - Like the Windows L2Bridge, the IPVLAN also allocates own ip address, but uses the same MAC address.
 
 There are chances that public cloud can have IPVLAN available.
 
 Special consideration when working with DHCP, `DHCP server is configured to assign IP address based on MAC addresses`. That is not going to work with IPVLAN.
 
 There are going to be many DHCP clients, requesting for IP address all having same MAC address. DHCP handles only one IP address for one MAC address. This issue can be overcome by using client id instead of MAC address (The IPVLAN is a new experimental feature) so the usage of client id is just a guess.
 
The docker create command and use of docker run and docker service to set it up.

For IPVLAN all containers have own ip address but share the parent interface mac address, so from inside the container, we can't ping the parent network interface, since kernel drops them. Worth noting that Linux kernel implementation filters request from containers to the IP address of parent interface, the kernel drops them.

Demo:

Node 1:
```
## the ipvlan require infrastructure to be setup
## since the network 

## ipvlan_mode is l2 - is default which is layer2 network
$ docker network create -d ipvlan \
 --subnet-192.168.1.0/24 \
 --gateway=192.168.1.254 \
 --ip-range=192.168.1.0/28 \
 -o ipvlan_mode=l2 \
 -o parent=etho demo-ip
 
 ## outputs the hash id on success
 
 # create and run a container
 $ docker run -dt --name container1 --network demo-ip alpine sleep 1hr
 
 $ docker network inspect demo-ip
 
 # login to the continer and checking the ip
 $ docker exec -it container1 sh
 
 > ip a
 
 # ping the node2
 > ping 192.168.1.91
 
 # ping the node1, where the conatainer
 > ping <ip-address-of-parent-node> # the linux kernel is blocking the request.
 # ping is not working.
 
```

Node 2:
```
$ ping <container ip address>
# this also works.
```
- IPVLAN doesn't require not PROMISCUOUS MODE.
- Doesn't give every container a unique mac address.
- Warning: The IP address range should be choosed correctly. (IPAM should be right, since DHCP server provides ip address)

Representation:

![image](https://user-images.githubusercontent.com/6425536/80332944-427c2b00-8801-11ea-9802-4f05c9e6eaac.png)

---------

### Network Services

  - Service discovery
  - Load balancing
  - Port-based routing (routing mesh)
  - App-aware routing (Http routing mesh)
  
  Service discovery: 
    - Discovering the service.
    - automatic in Docker, when containers are created using `--name` or `--alias`.
    
  DNS Service discovery in Docker:
   - Every container gets a DNS name resolver (a light weight) and it is listening on `127.0.0.11:53` on every container. Port 53 is the standard DNS port.
   - This resolver intercepts all request from the container, and  forwards requests to DNS server running on the local Docker host. 
   - Then Docker host resolves the name, if not able to resolve it sends to the public DNS server to resolve the name. (The feature of Docker host reaching the public DNS server can be switched off.)
    
Service discovery is called network scoped, this means containers can resolve the name of other container that are present within the network.
   - if the container are on the same network, the name will be resolve.
   - if the container are on different network, the name will not be resolved.

The way network scoping works is the container creating request creates a socket back to the DNS server on the host. This embedded DNS server traces the socket back to the network from where it came from. Once it know that, it fitlers request based on Origin network. This how the name gets resolved.

#### VIP based Load Balancing:
  - Each service gets a single virtual ip address (VIP) and this stay with the service the end of the service (entire life of service, one VIP to one service).
  - When every service name gets resolved like the name, it will get resolved to service VIP.
  - This is in turn get load balanced accross all the VIP that individual task that are healthy.
  - Each service has one are more tasks, tasks are containers.
  - Every task gets an IP and all those IPs are grouped under single service wide VIP.
  - If the single VIP is hit with 5 times, it is going to round robin request to all those IPs (container).
  - All the above are automatic, and internal load balancing.
  
###### Request coming from outside the same network (coming from other side of the planet). This is where Routing Mesh comes to play.
  
   - Introduced in Docker 1.12.
   - Routing request that are coming from outside the network or coming from network.
   - Any request coming outside from the internet, should be able to processed by SWARM.
   
How Routing Mesh works when the one worker node is down.

```
## Enable swarm mode, built swarm that manages worker node.
## Initializing swarm creates couple of networks (refer previous section above)
## one of the network is "ingress" overlay nework.
## This is scoped to Swarm, and instantly made avialble to all managed worker.
## create service using below command (check documenation - $ docker service create..)
## (Assume) 3 task/container is created and published to a port 5000.
## The port 5000 is published to all the ingress network.

## Say there are 4 mananged worker node in swarm, and there are 3 tasks
## All the 4 managed node are under the ingress overlay network
## As mentioned above all the 4 mansged worker gets the request on port 5000.
## Assume if the node 4 is not servicing any task
## The node 4 handles the incoming request, does name resolution as discussed above.
## Resolves the service to a VIP.
## This request is forwarded to healthy node based on VIP based load balancer.
```

Demo:

```
## create 4 node and running docker service and under same network

Node 1:
## Enable Swarm init on all the node.
$ docker swarm init
## Create a overlay network 
$ docker network creat -d overlay demo-overlay

## create service 
$ docker service create --name demo-service -p 5000:8080 --replicas 3 --network demo-overlay my-web-app
### -p 5000:8080 (publishing to 5000 on the node on the SWARM, forwar to 8080 in container/task)

## check the service status
$ docker service ls 

## inspect the ingress network
$ docker network inspect ingress
## check the container
## the task from the my-web-app shows up, this was not attached manually.
## Every task gets attached automatically to the ingress network.

## check if the container is running in all the swarm services
$ docker service ps my-web-app

## in the webserver, use the ip address of the node 4 within the SWARM which is not serving the will be serving the request with response.
```  
  
#### HTTP Routing Mesh (HRM)
   - Built on top of port-based/L4 routing mesh
   - (Application layer/ Layer 7)
 
Limitation on the port-based routing:
  - The single service on swarm cluster can listen on particular network port. In another words there can't be two services listening on the same port.
  - Standard routing mesh operates on Layer 4/ Transport layer, there is no awareness of things happening in layer 7 (higher up)
  
  The HTTP Routing mesh (HRM) address those limitations.
  
  HRM - requires Docker Datacenter/ Docker universal control plane. This is Docker Inc. managed service, and pay for it. This might be different now, google more about this since the feature are changing frequently.
  
 ```
 ## Enable HRM feature in UCP - say we use Port 80
 ## The active HRM, creates a network ucp-hrm and also global service (ucp-hrm)
 ## Create your service
 ## Attach the created service to the ucp-hrm network.
 ## Multiple service and using the same port, using the HTTP headers.
 ## This was not applicable in Standard routing mesh.
 
 ## each service is created with the label and the ucp-hrm global service creates a key value pair with the service and label
 ## the incoming request for both the service is coming to port 80, and ucp-hrm peaks the host part and routes to the corresponding service.
 ```
  


 
 
  

    
  
