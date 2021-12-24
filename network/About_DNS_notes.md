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
  - 



[.](https://support.constellix.com/support/solutions/articles/47000862695-how-dns-works)
