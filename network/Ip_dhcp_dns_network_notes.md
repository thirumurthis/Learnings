##### Network configuration

```
IP Address config 

Ipv4 address : 192.168.1.22

subnet Mask 255.255.255.0

Default Gateway 192.168.114.1

DNS 75.75.75.75 
	     8.8.8.8
```

`Subnet mask`: This defines which part of the ip 	address is subnet and which part is the node. (255.255.255 -part doesn't change)

For a initial request, if the ip address is not reachable, then have go through the `Default Gateway`.

`DNS`: The DNS the computer will use to convert name to ip address, by looking through cache or internet DNS servers.

Commands:

`ipconfig`

`ipconfig /all | more`

`ipconfig /displaydns`

### How does a machine gets if IP Address info.

### `DHCP (Dynamic Host Configuration protocol)`: 
   The machine gets the ip conifguration is from DHCP server which contains the ip address.

DHCP server 
- At enterprise level would be a bigger, like an actual server 

- At home, it would be small DHCP build in server running at the router like Linksys, Dlink, which hands out the address.  

### Obtaining ip address:
When the machine comes online, 
 - it will need an ip configuration 
  - Client machine sends a broadcast message (like it needs an ip address) to DHCP server
  - The DHCP server recieves the message and responds with an ip configuration (like ip address, subnet mask, default gateway)
  - The client machine will recieve this messsage and configure itself 
  - The client machine then responds to DHCP server that it will use it. 
     - Thus DHCP server will not rlease that used ip address to any other device unless it is released.

`Note:`
 Newer machine starting with windows 2000+, if the computer machine is not able to identify the DHCP server, it will eventually gives an ip address in the range 169.254.x.x. 
 
 These ip address range are owned by microsoft and the computers will self configue to use this. 
  
 If you see this, then it is having some issue connecting to dhcp - used for trouble shooting.

### IPV4 vs IPV6

`IPv4` 
   - has 4 octate, and contains (2^32) address space

`IPv6` 
   - has 2^128 - address spaces eg. represenation: fde8:486a:5ece:fda4
   - Developed to address the address problem that Ipv4.
   - Google, Bing, Akamai - June 8th 2011 truned on IpV6 for testing and latter on set it permanent.
   - Backward compitablity is still retained.
 


