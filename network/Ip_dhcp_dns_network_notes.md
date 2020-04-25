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

Filtering using the DNS, tracking the flow of www.boeing.com site.

Under the Domain Name System section, first a signal is sent, and the response from the DNS is displayed below.

![image](https://user-images.githubusercontent.com/6425536/80288628-00ae9000-86ee-11ea-8c6d-820b8014a371.png)





