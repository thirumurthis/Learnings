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

#### `DHCP (Dynamic Host Configuration protocol)`: 
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
 
### DNS 
  - Name resolution process, the address typed in an browser needs to be converted to ip address info.
  
  - If the user wanted to use www.microsoft.com in browser, the client machine will check with the DNS server (name server1) to resolve the name. 
  - If the DNS route name server1 exist then it will be sent back.
  - If the DNS route name server1 doesn't have that value it will check the root NS server.
  - If the root NS server doesn't contains the information it will check with other .com NS (for .com domain for major provider the route name server1 will contain the ip information.
  
  Note: Most of the heavy duty is done at the server-1 shown in the below diagram.
  
![image](https://user-images.githubusercontent.com/6425536/80284327-b4098b80-86d2-11ea-9403-5ddf45db7596.png)

command:

```
# nslookup - used to query the DNS queries from command line.
# command alone: will open up another interactive comand line to query
> nslookup
 > google.com
 ...
 ...
 > exit
 
# nslookup with address info

> nslookup www.bing.com
DNS request timed out.
    timeout was 2 seconds.
Server:  UnKnown
Address:  2001:558:feed::1

DNS request timed out.
    timeout was 2 seconds.
DNS request timed out.
    timeout was 2 seconds.
DNS request timed out.
    timeout was 2 seconds.
DNS request timed out.
    timeout was 2 seconds.
*** Request to UnKnown timed-out

### in some cases it displayes
### Name, Addresses, Aliases

## below will take to the other DNS server from where we can query the website name.
## refer the snapshot
> nslookup
  > server 8.8.8.8<ip address of different server to quyery of>
  > boeing.com
```

![image](https://user-images.githubusercontent.com/6425536/80285507-59742d80-86da-11ea-819f-a9e6797461ea.png)
 
### DNS Cache
 - Concept of DNS
 - The DNS server that we are querying will be cached that information for certain period of time (TTL)
 - The local machine will also cache that information for certain period.
 
```
## in browser, just type www.hollywood.com
## or use ping <url> command
## then in the command prompt try the below command

> ipconfig /displaydns
# below will be displayed
....

    www.hollywood.com
    ----------------------------------------
    Record Name . . . . . : www.hollywood.com
    Record Type . . . . . : 1
    Time To Live  . . . . : 49
    Data Length . . . . . : 4
    Section . . . . . . . : Answer
    A (Host) Record . . . : 52.34.76.207

    Record Name . . . . . : www.hollywood.com
    Record Type . . . . . : 1
    Time To Live  . . . . : 49
    Data Length . . . . . : 4
    Section . . . . . . . : Answer
    A (Host) Record . . . : 35.165.237.52
 ....
```
Note: 
  When changing DNS records, it will take sometime to propagate to internet.
  
##### Overriding the DNS in individual machine using `host` file

- Navigate to C:/Windows/System32/dirvers/etc/, there should be file `host` (no extension)
- Editing this file we can overrider the local DNS

hosts file content, save it
```
## enter value like below spearated by tab space
127.0.0.1	mysite1.com	www.mysite2.com  site2.com
127.0.0.1	www.site.com
192.168.40.16	supersite.com

```
use `ipconfig` command
```

> ipconfig /displaydns
# this will display the content with the host file
# editing the host file will clear the DNS cache
# now if using ping for example 

> ping www.site.com [this will ping local host address]
> ping supersite.com [this will take to 192.168.40.16 ip address]
```

This would be helpful if we are migrating web server from one ip address to another ip address. Just put the new ip information in the host file and test it.

Latter it can be reverted.

### `Record types in DNS`
   - default record type `A`, this turns the name to ip address
   - name server (`NS`) record type [set type=NS] 
   - mail exchange (`MX`) record type [set type=MX]
   - canonical name (`CNAME`) record type [set type=CNAME], 
      - kind of alias names
      - there can't be a CNAME on root of the domain like microsoft.com 
	   (note: there is no www above, www.microsoft.com will have CNAME, check below)    - `AAAA` record type (Quad A) [set type=AAAA]
      - This will provide an Ipv6 version of address     
   - `wildcard` record type
      - will return certain ip address for any name under certain domain.
      - The was local website hosted as _localdev.us_.
         - when using localdev.us, nslookup provided the ipaddress of the website.
         - also www.localdev.us, nslookup provided the ip address.
	 - when using client.localdev.us, nslookup provided the localhost ip address.
      - This allows developer to set the local server configuration using their host names to grab certain name spaces without setting up DNS server , for example
         - clientname1.localdev.us (this goes to certain ipaddress)
	 - clientname2.localdev.us (this goes to certain ipaddress)
       - use of wild card usage is certain ISPs will capture any failed DNS query will redirect to the ip address to their webserver. to avoid this use own DNS server which is costly setup.
          
`type=NS`
```
> nslookup
> server 8.8.8.8  //I am going to the different DNS provider (google in this case)
> set type=NS     // within that DNS server trying to set the record type as NS
> microsoft.com   // querying the name microsoft.com

## output looks something like below where the name resoultion process displays
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
microsoft.com   nameserver = ns2.msft.net
microsoft.com   nameserver = ns3.msft.net
microsoft.com   nameserver = ns4.msft.net
microsoft.com   nameserver = ns1.msft.net
> exit
```

`type=MX`
```
> nslookup
> server 8.8.8.8  //I am going to the different DNS provider (google in this case)
> set type=MX
> microsoft.com
## output looks like (when sending mails to microsoft.com it will use the below name)
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
microsoft.com   MX preference = 10, mail exchanger = microsoft-com.mail.protection.outlook.com

> pluralsight.com
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
pluralsight.com MX preference = 1, mail exchanger = aspmx.l.google.com
pluralsight.com MX preference = 10, mail exchanger = alt3.aspmx.l.google.com
pluralsight.com MX preference = 10, mail exchanger = alt4.aspmx.l.google.com
pluralsight.com MX preference = 5, mail exchanger = alt1.aspmx.l.google.com
pluralsight.com MX preference = 5, mail exchanger = alt2.aspmx.l.google.com
> exit
```

Note: 
  Preference is order in which machine sends the mail to send to, if one exchange is missing will go to the next.
  
`type=CNAME` 
```
> nslookup
> server 8.8.8.8
DNS request timed out.
    timeout was 2 seconds.
Default Server:  [8.8.8.8]
Address:  8.8.8.8

> set type=CNAME
>
> pluralsight.com // witout wwww
Server:  [8.8.8.8]
Address:  8.8.8.8

pluralsight.com
        primary name server = ns-1441.awsdns-52.org
        responsible mail addr = awsdns-hostmaster.amazon.com
        serial  = 1
        refresh = 7200 (2 hours)
        retry   = 900 (15 mins)
        expire  = 1209600 (14 days)
        default TTL = 86400 (1 day)

> www.pluralsight.com   // with www <- user input value
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
www.pluralsight.com     canonical name = www.pluralsight.com.cdn.cloudflare.net

> www.microsoft.com
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
www.microsoft.com       canonical name = www.microsoft.com-c-3.edgekey.net

```

`type=AAAA`
```
C:\Users\thirumurthi>nslookup
DNS request timed out.
    timeout was 2 seconds.
Default Server:  UnKnown
Address:  2001:558:feed::1

> server 8.8.8.8
DNS request timed out.
    timeout was 2 seconds.
Default Server:  [8.8.8.8]
Address:  8.8.8.8

> set type=AAAA
> www.google.com
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
Name:    www.google.com
Address:  2607:f8b0:400a:800::2004

> www.microsoft.com
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
Name:    e13678.dspb.akamaiedge.net
Addresses:  2600:1409:3800:186::356e
          2600:1409:3800:1a3::356e
          2600:1409:3800:187::356e
Aliases:  www.microsoft.com
          www.microsoft.com-c-3.edgekey.net
          www.microsoft.com-c-3.edgekey.net.globalredir.akadns.net
> exit
```

`wild card`
```
C:\Users\thirumurthi>nslookup
DNS request timed out.
    timeout was 2 seconds.
Default Server:  UnKnown
Address:  2001:558:feed::1

> server 8.8.8.8
DNS request timed out.
    timeout was 2 seconds.
Default Server:  [8.8.8.8]
Address:  8.8.8.8

>
> localdev.us
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
Name:    localdev.us
Address:  3.13.31.214

> www.localdev.us
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
Name:    www.localdev.us
Address:  127.0.0.1

> client1.localdev.us
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
Name:    client1.localdev.us
Address:  127.0.0.1

> client2.localdev.us
Server:  [8.8.8.8]
Address:  8.8.8.8

Non-authoritative answer:
Name:    client2.localdev.us
Address:  127.0.0.1

> exit
```
Note: 
   since the local host address is provided, many ISPs will try to forward or use their ip address server id's for failed DNS queries. For example, dockerhub.io
   
![image](https://user-images.githubusercontent.com/6425536/80287359-fb4d4780-86e5-11ea-8c89-ac9ec473c158.png)
-----

### `DNS Trace`

`In order to trace the DNS, we can use WireShark utility tool, this provides the network log happened on the transaction.`

Wireshark tool displays Mac address information also in the Source and Destinaton section.
Tip of the day: 

 `wmic` command to check if windows machine is 32 or 64 bit

```
## command and output:
> wmic os get osarchitecture
OSArchitecture
64-bit

> echo %PROCESSOR_ARCHITECTURE%
AMD64
```

Filtering using the DNS, tracking the flow of boeing.com site.

Under the Domain Name System section, first a signal is sent, and the response from the DNS is displayed below.

![image](https://user-images.githubusercontent.com/6425536/80290586-fd6dd100-86fa-11ea-869d-dba04a00c31a.png)

The client ip address, is requesting the DNS server for ip address for the name boeing.com

The DNS server requests the root name server where the boeing.com for A record.

One of the root name server responds with authoritive nameserver info of all the .com server, since it is not able to resolve the name to ip address. 

-------

### Ip routing / Network traffic routing.

`Subnet` - is a collection of computer that can communicate to each other without using a router.

A router connects different subnet. When a machine that wanted to talk to another machine with different subnet, the traffic needs to be routed by the router.

Tools used to verify the routes, command to trace the route:

##### `tracert` 
 tracert - provide the hops happened to reach the server from the requested client machine.
 ```
 > tracert www.yahoo.com
 Tracing route to atsv2-fp-shed.wg1.b.yahoo.com [2001:4998:c:1023::4]
over a maximum of 30 hops:

  1     3 ms     2 ms     5 ms  2601:601:1300:3da0:3e37:86ff:fe39:12a2
  2    17 ms    30 ms    17 ms  2001:558:4082:5b::1
  3    13 ms    13 ms    13 ms  po-303-1252-rur102.burien.wa.seattle.comcast.net [2001:558:a2:c061::1]
  4    11 ms    12 ms    10 ms  2001:558:a0:17d::1
  5     *        *        *     Request timed out.
  6     *        *        *     Request timed out.
  7    18 ms    17 ms    12 ms  10gigabitethernet2-12.core1.sea1.he.net [2001:470:0:3c5::1]
  8    11 ms    21 ms    13 ms  v6-six2.yahoo.com [2001:504:16::306:0:2846]
  9    20 ms    16 ms    15 ms  ae-7.pat1.gqb.yahoo.com [2001:4998:f007::1]
 10    18 ms    17 ms    16 ms  et-1-0-0.msr2.gq1.yahoo.com [2001:4998:f00f:208::1]
 11    97 ms    37 ms    14 ms  et-1-0-0.clr2-a-gdc.gq1.yahoo.com [2001:4998:c:fc22::1]
 12    17 ms    17 ms    22 ms  2001:4998:c:f801::1
 13    17 ms    15 ms    16 ms  media-router-fp1.prod1.media.vip.gq1.yahoo.com [2001:4998:c:1023::4]

Trace complete.
 
 ```

 ##### `pathping`
   This is more robust and provide more detail information about the routing.
   
   pathping - does additional statstical test to provide more relaiable results.
 
   pathping pings each hop link with 100 request, to report the data packet loss and the statistics.
   
 ```
 > pathping 
 Tracing route to atsv2-fp-shed.wg1.b.yahoo.com [2001:4998:c:1023::5]
over a maximum of 30 hops:
  0  thirumurthi-HP.hsd1.wa.comcast.net. [2601:601:1300:3da0:c02e:c71b:28ac:4bdd]
  1  2601:601:1300:3da0:3e37:86ff:fe39:12a2
  2  2001:558:4082:5b::1
  3  po-303-1252-rur102.burien.wa.seattle.comcast.net [2001:558:a2:c061::1]
  4  2001:558:a0:17d::1
  5     *        *     be-33650-cr01.seattle.wa.ibone.comcast.net [2001:558:0:f769::1]
  6     *        *        *
Computing statistics for 125 seconds...
            Source to Here   This Node/Link
Hop  RTT    Lost/Sent = Pct  Lost/Sent = Pct  Address
  0                                           thirumurthi-HP.hsd1.wa.comcast.net. [2601:601:1300:3da0:c02e:c71b:28ac:4bdd]
                                0/ 100 =  0%   |
  1    7ms     0/ 100 =  0%     0/ 100 =  0%  2601:601:1300:3da0:3e37:86ff:fe39:12a2
                                0/ 100 =  0%   |
  2   26ms     0/ 100 =  0%     0/ 100 =  0%  2001:558:4082:5b::1
                                0/ 100 =  0%   |
  3   18ms     0/ 100 =  0%     0/ 100 =  0%  po-303-1252-rur102.burien.wa.seattle.comcast.net [2001:558:a2:c061::1]
                                0/ 100 =  0%   |
  4   21ms     0/ 100 =  0%     0/ 100 =  0%  2001:558:a0:17d::1
                                0/ 100 =  0%   |
  5   19ms     0/ 100 =  0%     0/ 100 =  0%  be-33650-cr01.seattle.wa.ibone.comcast.net [2001:558:0:f769::1]

Trace complete.
 ```

`subnet` is defined by combination of ip address and subnet mask.

```
              Network    | Node
Ip address : 192.168.140 |.116
Subnet mask: 255.255.255 |.000

             Network | Node
Ip address : 192.168 |.140.116
subnetmask : 255.255 |.000.000

Note: when he subnet mask is off (0) those are representing node.
```

Representation of subnet mask of 255.255.255.000
##### Example 1:
```
192.168.40.0/24
or
192.168.40.0/255.255.255.0
or
192.168.40.0 - 192.168.40.255 [range of possible ip address]
```
`Note:`
  - So if a computer on this network reach an ip address within this subnet ipaddress range it can be reached directly.
   
  - So if a computer on this network needs to reach an ip address out of this subnet ip address range, it needs to go through the default route.

##### Example 2:

Subnet mask can also be, 255.255.254.0. this is called 23 bit subnet represented 

```
192.168.12.0/23
or
192.168.12.0/255.255.254.0
or
192.168.12.0 - 192.168.13.255
```

The 23 bit subnet, expands between two class c networks. (192.168.12, 192.168.13)

##### Example 3:

Subnet of 28 bits represented as below (this is a very small network)
```
192.168.12.16/28
or
192.168.12.16/255.255.255.240
or
192.168.12.16 - 192.168.12.21
```

#### How does an computer determine whether the ip address is on the same subnet or different subnet? How to reach other subnet?

Answer is it is defined by the `route table`.

Simplified route table:

| Destination | Netmask | Gateway |
| -------|-----------|-----------|
|0.0.0.0 | 0.0.0.0 | 192.168.115.1|
|127.0.0.0| 255.0.0.0| On-Link|
|192.168.115.0| 255.255.255.0| On-Link|
|255.255.255.255| 255.255.255.255| On-Link|


When the traffic is sent, the most specific match possible .

First row:

   Destination & Netmask 0.0.0.0  -> means any ip address and 192.168.115.1 is the default gateway

Second row:

   Destination 127.0.0.0/8 - 127 network is always the local host. This means NO need to go through the default gateway. This means traffoc never leaves the physical computer.

Third row:

   Destination 192.168.115.0/24 - This is the subnet currently the computer is located in.
   Traffic can be sent directly to this destination ip address.

Fourth row:

   Is used for broadcast, sent traffic that is destint for the subnet. The broadcast do not cross routers, don't need to send to default gateway. Broadcast to the entire subnet.

To view the route table we use `route` command in windows.

Most frequently used type of route command: (use command prompt of windows)
```
> route print  // to print the route table information

==========================================================================
Interface List
 62...78 ac c0 40 65 b9 ......Realtek PCIe GBE Family Controller
 ....
===========================================================================

IPv4 Route Table
===========================================================================
Active Routes:
Network Destination        Netmask          Gateway       Interface  Metric
          0.0.0.0          0.0.0.0      192.168.0.1     192.168.0.11     50
        127.0.0.0        255.0.0.0         On-link         127.0.0.1    331
        127.0.0.1  255.255.255.255         On-link         127.0.0.1    331
  127.255.255.255  255.255.255.255         On-link         127.0.0.1    331
............
        224.0.0.0        240.0.0.0         On-link      192.168.0.11    306
  255.255.255.255  255.255.255.255         On-link         127.0.0.1    331
  255.255.255.255  255.255.255.255         On-link      192.168.0.11    306
===========================================================================
Persistent Routes:
  None

IPv6 Route Table
===========================================================================
Active Routes:
 If Metric Network Destination      Gateway
  1    331 ::1/128                  On-link
 ...........
 67    281 ff00::/8                 On-link
 35    306 ff00::/8                 On-link
===========================================================================
Persistent Routes:
  None
  
  ----------------------
  In the above Ip4 table Interface is the current ip address.
  The metrics is a value, where the lowest cost will be used for traffic.
```

The default gateway is determined by the ip network configuration in the route table.


### NAT (Network Address Translation)

There is a shortage of Ipv4 address, how this issue is resolved by the ISPs.

ISP provides only one ip address.

At home we have multiple device that connected to the router, all the device that are connected will get a private ip.

`NAT` as a process converts the private ip address of the devices when the traffic exists the router and converts to the expternal public ip address (provided by the ISPs) as it reaches the internet. Also when the traffic comes back that is converted to correct internal ip address.

Represetnation of the router public ip and private ip
```

                    64.78.185.232 (public ip provided by ISP's)
		      Router  (ip 192.168.1.1)
   		     /	         |             \ 
		   /             |              \ 
	          /              |               \
	192.168.1.100     192.168.1.101    192.168.1.102
	  device1             device2        device3
```

When hosting a website in the internet, all we need is a public ip address.

All the Linksys, etc router has the ability to forward request coming certain port to my public ip address to be routed to specific ip address internally.

Say, any if we want to host a web server in local laptop, we can tell the router any traffic that is destint for port 80 (http) to route to internal ip address. The port cannot be used by two different ip address.

There are list of `private ip address` range are :
```
# Private Network Ranges:

10.0.0.0/255.0.0.0 (8)
172.16.0.0/255.240.0.0 (12)
192.168.0.0/255.255.0.0 (16)
```

### `Port connectivity`:

Transport layer protocol: `TCP` and `UDP`

`TCP` - All the web traffic runs over this protocol. Http, Mail traffic,etc.

Understanding TCP (Example: web application) and UDP (Example: Video confrencing) protocol.

![image](https://user-images.githubusercontent.com/6425536/80295717-f7dbaf80-8729-11ea-8f4c-ae67d99167a9.png)

##### Testing port connectivity:
  
  We can only test TCP port, since we can create a session in here. It is difficult test the UPD port. Use `telnet` command.
  
 ```
  > telnet <site> port
 ```
 Explantion:
 
   Once a connection is established using the telnet command at specific port, the server expects a GET/POST command assuming as browser is connected.
   
   If you issue cntr+c, then you would see HTTP 400 Bad request on the console. In some case we should close the command prompt.

   If the port is opened on the server side, it will be listening on that port, if not then will display a timed out message or connection failed message.

telnet is already available within the OS

Standard Port usage
```
1433 - Sql Server 
25 - SMTP port
```
#### `nmap` is an open source tool to scan the port.

In case we don't know which port to connect, then `nmap` utlity can help.

`nmap` tool will tell the list of port that is available in the server connecting to it.
```
> nmap -v site.com
### displays the port that command was successfully connect to.
```

##### command to see which port is open and which process are listening on which ports.

utility used is `netstat -ano'

```
> netstat -ano
Active Connections

  Proto  Local Address          Foreign Address        State           PID
  TCP    0.0.0.0:135            0.0.0.0:0              LISTENING       804
  TCP    0.0.0.0:445            0.0.0.0:0              LISTENING       4
  TCP    0.0.0.0:3306           0.0.0.0:0              LISTENING       3996
  TCP    0.0.0.0:5040           0.0.0.0:0              LISTENING       1672
  TCP    0.0.0.0:5357           0.0.0.0:0              LISTENING       4
  TCP    0.0.0.0:49664          0.0.0.0:0              LISTENING       760
  TCP    [2601:601:1300:3da0:81e9:410f:18e1:f757]:59910  [2001:559:19:2886::57]:443  CLOSE_WAIT      12356
  UDP    0.0.0.0:3702           *:*                                    2160
  UDP    0.0.0.0:3702           *:*                                    2160
  UDP    0.0.0.0:3702           *:*                                    4960
  UDP    0.0.0.0:3702           *:*                                    4960
```

0.0.0.0 -> port listens to every avilable ip address on this machine

The above will list the process id, under task id we can see the appropriate process information. (enable the process id tab, in the task manger of windows if needed)

##### Firewall

This determines which traffic is allowed and which are not.

For example, there is a rule open 80 and block 25 (smtp), then the machine doesn't allow SMPT traffic.

In windows we can see the firewall detail as in the below figure. 

In case of there is a need to debug the firewall drop packets, enable the logs for troubleshooting.

![image](https://user-images.githubusercontent.com/6425536/80296343-0d9fa380-872f-11ea-92f2-ef20cc3fef6d.png)

Rules:
   Few of the Inbound and outbound rules are defined out of the box.
   
 Lets add new inbound rule, Right click the firewall inbound rule and click `add new rule`
    - Program rule
        - When specific executable is running what ever port it is going to listen on, we can enable those using this rule.
	- select the action to perform either allow, etc.
	- select profile
	- save with a name.
    - Port rule 
        - say which port to open for TCP or UPD protocol, used in web server.
    - pre-defined rule
        - windows provided.
    - custom rule
        - provides handle more protocol
	- ICMPv4 protocol is used by the `ping` command.
	- follow the screen instruction.

![image](https://user-images.githubusercontent.com/6425536/80296408-d54c9500-872f-11ea-8980-f7c9b2a4693b.png)



 
