#### What is DNS?
  - DNS server maps IP address to domain name. 
  - Where is this informaiton stored? - **nameservers**

#### what is nameservers?
  - Name servers store DNS records which are the actual file that says this domain maps to this ip address.
  - Where are nameservers?
     - The name servers are distributed around the world?
  - This nameserves are called `root nameservers`, instead of storing every domain ever, they stoe the locations of the `TLD` (`top level domains`)

#### What is TLD's (top level domains)?
  - TLD's are the two or three character strings lik .com at the end of domain name.
  - Each TLD has their own set of nameservers that store the informaton that says who is _authoritative_ for storing the DNS records for that domain.

#### What is authoritatve nameserver?
  - The authoritative nameserver is typlically the DNS provider or the DNS registrar (like GoDady that offers both DNS registration and hosting).
  - Here we can find the DNS record that maps example.com to the IP address 127.66.127.10

#### What is TTL's and Caches?
  - When quering a domain name the browser will ask your local resolving name server if they have DNS records for that domain cached.
  - The resolving name server is typically you ISP (internet service provider), and popular websites will likely be cached.
  - Local name resolution, avoind DNS lookup process.
  - When creating a record, you have option to set a TTL (time to live)
  - TTL's tell resolving name servers how long they can store the record information. (this can rang from 30s to days)

  - If the records are not cached, then resolving name server will ask the root name server for the TLD for that domain, which will point you to the provider authoritative for hosting the records.

##### Request flow from the browser to client server:
```

 [Request] ---> { ISP       } ----> {Root      } ---> { TLD        } ---> { Authoritative } ----> { DNS    } -----> { web server } ----> { request served }
                  Resolving          Nameserver         Nameserver          Nameserver              Record            and site 
                  nameserver                            like .com           DNS hosting                               files
                                                        or .net             Provider

```

#### What is traceroute?
  -  How many network hops took to resolve the domain.
  -  How this system connected to internet.
  -  we can use traceroute to identify the source of latency.

###### what is taceroute used for?
   - Traceroute is used to track transit delays of packets accross network. 
   - Ping tells the RTT (round trip time), between a user and the system.
    - traceroute will fail, if two or three packets sent are lost. (means faulty configuration somewhere)

#### What is CDN (content delivery network)?
  - A CDN is a wide network of web server that are configured to deliver contetn to clients from the geogrpahically closest server (or server with the fastest connection) to the client thereby optimizing the transfer speed.

##### CDN Redundancy & Reliability
   - CDN's have built-in redundancy, which makes outage resistant (every server has a backup).
   - CDN's are optimized to automatically route traffic to the nearest available server or point of presence (POP). i.e. if any node is down, the request are forwarded to closest server.
   - This also takes load off from the origin server. (The origin server holds the original content and data for your webpage).
   - Since contents are distributed, this is more resistant to slowdowns and outage mitigating issues like traffic spikes and DDoS attacks.
   - These `edge servers` can either push or pull content from the origin server.

##### CDN benefits:
  - provides search engine optimization (SEO) ranking.
 
#### CDN and DNS 
 - CDN service 
   - The DNS holds the web together by pointing a domain name to an IP address or hostname.
   - The CDN provider will provide a hostname that you will point to with a DNS record.
   - For Example, to point for www.mynewsite.com to CDN service, create a `CNAME` record for `www` and point it to myCDNHostname.com.
   - The reason to use the hostname of CDN is the IP address of the edge server will change depending on the location of the user making the query.
   - Each time a user request content from a CDN the DNS gives the user the IP of CDN server, using DNS load balancing.
  
[.](https://support.constellix.com/support/solutions/articles/47000862695-how-dns-works)
