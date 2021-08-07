`TCP/IP` - is also layered protocol stack, like ISO/OSI model. TCP/IP defines four of the ISO layers with some overlappings

| TCP/IP Layer | Equivalent OSI Layer | Function |
|-----------|--------------|------------|
| Application | Application | Application software requiring network services | 
| TCP | Transport | reliable data deivery |
| IP | Network | Non-guranateed, connectionless datagram delivery |
| Data-link (ARP/RARP, kernel) | Data-link | Physical network device control, IP-to-Ethernet adress resolution |

TCP/IP doesn't define the physical layer, while Session and Presentation are all incorporated in the Application layer.

`ARP` - TCP/IP doesn't define the Physical layer of the network model and therefore can't make assumptions about it. 

If we assume that there is a nework over which broadcasts can be made, then, in order for a device on a TCP/IP network to talk to another device, it must be able to refer to it 
by its IP address only, and let the Data-Link layer somehow resolve this IP address into physical network address.

In TCP/IP networks the `Address resolution Protocol (ARP)` handles this. When a device wishes to make contact with another device, it broadcasts an ARP requrest containing the desintation devices's IP addresss.

The destination devices sees its IP address in the request and respnses with its physical address, in an Ethernet environment, this is its raw 48-bit Ethernet address.

```
machine 1, sends a ARP request (i need to contact desitnation machine 2, i have ip).
machine 2/destination device sees its IP address in the request and responsds with physical address (Mac address). 
```

`RARP (Reverse Address Resolution Protocol (RARP)`:
This was designed to faclitate IP address resolution for diskless workstations and X terminals, for which it's either impossible or inconvinent to locally store the IP address configuration.

RARP allows a central server to map physical address to IP address on behalf of other systems. When a diskless workstation boots, for isntance, it sends out RARP request to find out its IP address.

The RARP server checks to see if the requesting machine is listed in its client table and, if it is, it responds with by client's IP address.

This also makes administering IP aadresses on a large network much simpler - when address changes need to be made, rather than having to go around to each machine and change each indivdual IO address, the netwrok adminstrator can make all hte changes at a central server. 

The next time the machine boot, they will receive the new IO address through RARP. 

_RARP has some problems , however, and a newer protocol with similar functionality, called **bootp** does its own RARP in some situations_

`IP addressing`

The IP Address is central to TCP/IP communication. A device must know another device's IP address if it hopes to communicate with it.

`The devices Ethernet address is  the physcial address and is essentially fixed`. 

`While the IP address is the device logical network addresss and is usually software deined.`

The logical address defintion makes IP addressing highly flexible and configurable, and it makes the physcially highly heterogeneous newrok look like one big local network.

IP address is a four-byte number and by convention, it's expressed as four decimal values seperated by full stops. 

Example, 128.8.15.92, the right most number identifies the machine and the numbers in left specify larger and larger subnetworks of the Internet.


  - `Class A, B and C address:`

The Internet Network Information Centor or (INTERNIC), allocates IP address, normally in blocks. Any one can requuest IP address for their network by contacting INTERNIC.

_Small network can request a **Class C** IP address allocation, which assigns first 3 bytes of the IP address. The right most byte can be used to specify up to 254 individual machines._

_Larger organization can request a **Class B** IP address allocation, which leaves the rightmost two bytes for machine assignments allowing for up to 65,536 individual machines on a network.__

_Really loarge ogranization can request a **Class A** IP Address alllocation, which leaves room for 16,777,26 individual machine._

Class A distribution are nearly impossible to attain, generally used for internation networks, govt., etc. Most large coporation use multiple class C addresses.

   - `Subnetting`:

Class A and Class B IP address allocation make it possible for an organization to assign thousands or even millions of individual IP addresses.

The assignable portion of the address can be though of as one big pool of addresses.

However, it's more popular to split up the byes of the assignable poriton of the address and use them to specify your subetworks in order to better organize your pool of assiganble address.

For instance, lsts say an orgainzation registers for the Calss B network `128.8.*.*` with INTERNIC. The last two bytes are now available to specify individual machines.

Instead, the network administrator decides to use the first available byte to indicate the department where the machine is allocated, and last byte to indicate the indivdual machine.

It is decided that the machine IP address are organized into 256 pools of 254 addresses each divided up by department.


```
                   |<-------- Network --------------->|<------------host/machine----->|
  Class B Address  |1 0 x x x x x x | x x x x x x x x | x x x x x x x | x x x x x x x |
  Subnet Mask      |1 1 1 1 1 1 1 1 | 1 1 1 1 1 1 1 1 | 1 1 1 1 1 1 1 | 0 0 0 0 0 0 0 |
  
  sunnet mask treats, Class B Address as if 256 seperate networks.
```
This makes the IP addres smuch more readable and managable for network admins. If is also easier to route network traffic accross an enterprise-wide network.


`Interconnecting Networks`

 As the networking requirements for an organization grow, the need quickly develops to link serveral smaller networks together to form one big network.

Typically, the organization will have a backbone network connecting smaller departmental networks. 

There are three main ways to connect networks:
   - repeaters
   - bridges
   - routers

 -  `Repeaters` :
 When two networks simply need to be physically connected to each other, and doing so would execeed the maximum cable lenght or permitted number of nodes on each segment, a **repeater** can be used.
 
 Repeaters provide `signal amplification and timing correction` - they serve to isolate the segments on either side of the repeaster from each other. This isolation prevents a calbe break on one segment from brining the whole network down.
 
 The total number of nodes needs to be deteremined before laying out network that will use repeaters.
 
 - `Bridges`:
Bridges are principally hardware-based devices that operate at the data-link level to pass packets from one network to another.

The bridge is connected to both networks and as the packets come through one side, it forwards them to the other side.

Some bridge contain firmware learning algorithm that figure out which addresses are on which side of the bridge, so that the bridge only forward packets destomed fpr address accross the bridge.

Bridges are very effective to connect two Ethernets, but thye can be quite expensibe and are generally not as configurable as routers. Two remote bridges can connect two networks via high speed link such as microware or laser.

- `Routers`:
`Routers are usually software-based` and operate at the network level. As such, they are protocol-specific (an IP router won't route IPX).

A router can be as simple as a UNIX macine with an extra ethernet adapter and proper software configuration (Unix networking has built-in routing capablilty)

Servers and workstations can therefore act as routers on a LAN.

When using non-dedicated machine as a router, be advised that routing can be highly intensive activity and many organizations find that they have to use dedicated routers to maintain performance.

Dedicated routers tend to make more use of advanced features, including improved security and more intelligent routing.

Two kinds are routing that UNIX machine can perform;
  - Static
      - static routing is only appropriate for `small, isolated networks` where the network topology is known and doesn't change frequently.
      - static routing is accomplished by adding route commands to the system startup files, which add specific routes directly into routing tables.
  - Dynamic
      - Dynamic routing, despite being more resource-intensive, is the way to go if your system is connected to a large, changing network or is directly connected to internet.
      - Since the topology of the internet constantly changing. static routing is totally impractical and dynamic routing must be used.
      - Dynamic routing is accompilished by running **routed**, or more powerful **gated** from startup file.
      - **routed** and **gated** daemons communicate with dynamic routing daemons on other systems to buid a puctre of the current ntework topology and automatically handle any changes. The **`/etc/gated`** daemon isn't available on all systems, refer system documenation.

   - Static and dynamic routing can be used together to optimize the routing within a large network. 





 



