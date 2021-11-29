
Simple representation of Azure network:

VNet - Any resource within the VNet can communicate to another resource without any Route tables.
     - When creating VNet, create it on specific location or region (West US). Any virtual machine created should be grouped under the VNet of same location or region.
     - To connect VM between two VNet (running on West US) with VM machine in with VNet in East US. It can be achievied using `VNet peering`.

Subnet - Small network range.

Route tables - Used to connect one VNet to another VNet. Example, we can create a Route table in Azure to connect to on-perm network.

VNet, Subnet representation:

![image](https://user-images.githubusercontent.com/6425536/143666662-44f0eaa5-a9c1-4e02-bec7-3eab0a4a6ef1.png)


Netowrking

![image](https://user-images.githubusercontent.com/6425536/143671160-cef78c58-9d3a-42c5-a9b6-7940e60096a8.png)

About IP's

![image](https://user-images.githubusercontent.com/6425536/143671467-95a02c83-5979-4815-ae8d-5ff2bea2443a.png)


| Topic | Description |
| ------ | ------- | 
| Route tables | Route tables is used for routing network/data from one network to another network. For example, a machine from VNet1 to needs to communitcate to machine in VNet2, if a machine from VNet1 needs to access Storage account, or a Network that needs to communicate to on-perm. (VNet peering is another option for connecting network)|
| Route table.. | Say if a machine wanted to access on-perm network, then create a route rule with an ip address (10.10.0.1/16). So if any machine in the VNet network tries to access the IP address rule of the ruote table, takes to on-premisis network. | 
| NSG | NSG (Network security Group) is used to filter network traffic to and from Azure resources in Azure VM. A NSG contains security rules that allow or deny inbound netowrk traffic to, or outbound traffic from, several types of resources. Each rule requires destination, port and protocol. | 
| NIC | A network interface (NIC) is the interconnection between a VM and a virtual network (VNet). A VM must have at least one NIC, but can have more than one, depending on the size of the VM you create. Each NIC attached to a VM must exist in the same location and subscription as the VM. Each NIC must be connected to a VNet that exists in the same Azure location and subscription as the NIC. You can change the subnet a VM is connected to after it's created, but you cannot change the VNet.|
| Service EndPoint | Virtual Network (VNet) Service Endpoint policy allows to filter egress (outgoing) VNet traffic to a particular Azure service. The Azure service can be Azure Storage account, Azure Database. This provides granular controll over the network access |
| ASG (Application Security Group) | Application Security Group helps to manage the security of Virtual machines by grouping them according to the applicaton that run on them. An Application centric way of using NSG. Say, if there are group of VM's act as Web server, few VM's act as Application Servers, few VM's act as DB servers - Say the DB server should be connected by the Application Web server. So the using ASG we can provide granular controll. Addign a VM to the service group the ASG can be applied.|
| Azure Firewall| Azure Firewall is a managed, cloud based network security service that protects Azure Virtual network.|
| Azure Firewall Manager | This is a security Management service that provides central security policy and route manamgement for cloud-based security perimeters. |
| Bastion Host (also called as Jump box) | Azure provided service for more security. |
| NAT Gateway | Virtual Network NAT (Network Address Translation) simplifies outbound-only internet connectivity for virtual networks. It basically hides the identity or IP address of the VM to the client. This is highly resilient and fully managed. |
| Azure DNS | Azure DNS is a hosting service for DNS domains that provides name resolution by using Microsoft Azure infrastructure. DNS handles converting name (www.microsoft.com) to IP address. |
| Azure LoadBalancer | Based on the traffic the load will be blanced by this service. Types - Round robin, Client Affinity, Server affinity. This can be configured at Layer 7 (application layer - HTTP/HTTPS) level and Layer 4 (TCP/IP, UPD) level.| 
| Application Gateway| Application Gateway is similar to **Load Balancer**, only difference is Application gateway can only be configured at Layer 7 (Application layer - HTTP/HTTPS). Additionally Application gateway provides Path based routing - based on URL path traffic can be routed to different application. Multi-site routing- based on the URL domain the traffic can be routed to pool of servers. | 
| Azure Traffic Manager | This is another network service provided by Azure. Traffic Manager deteremines the best possible endpoint to route the client request. When the client/user hits an url from browser usually the DNS will share the IP address of IP address of the server or load balancer, in this case the traffic manager IP address will be provided to the user by DNS. Based on the user request and rules defined in the Traffic manager, it will determine the best route to access the application. This can be used for Highly available solution or disaster recovery. |
| ExpressRoute | This is one-way of connecting Azure Network to on-prem or customer data center. This will create a dedicated connection (or pipe) between datacenter and Azure network. Being dedicated connection this will be very fast, also expensive. On-prem network will also setup a Gateway (some a device to create a gateway.) |
| VPN Gateway | This is another way of connecting Azure network to on-prem network. This option where a secure but data transfered via internet, the metwork bandwidth depends on the internet connection.  |
| VNet Peering | Connecting one VNet to another VNet. Same region can have multiple VNet, and to connect between these VNet we can use VNet peering. This is called `Regional VNet peering`. When connecting different VNet accross region (say, West US to East US) this is called as `Global VNet peering`. Under Virutal Network of Azure portal, select Peering option to configure VNet Peering. |
| Hub and Spoke Model | More than two network (for example say three networks) that needs to connect to On-prem netowrk, instead of creating VPN Gateways on all the the networks, we can use Hub and Spoke model where we can configure all the three networks in peering with a central network and configure the VPN Gateway only on the hub network. But we need to configure a `Gateway Transit`|
| Gateway Transit | Gateway Transit is a peering property that lets one virtual network use the VPN gateway in peered virtual network for cross-premsis or vnet to vnet connectivity | 

Bastion:

![image](https://user-images.githubusercontent.com/6425536/143797522-18bf629e-5517-4523-b24d-07cd1424c8ae.png)

To access the VM in the VNet,
   Option 1: Expose the RDP port (in case of Windows machine) and SSH port (in case of Linux) so it can be accessed over Internet, but this is NOT a secure way to do in enterprise.
   Option 2: Bastion server based login for more secure approach. Azure provides Bastion Host/server which accepts connection over HTTPS (or port 443) protocol. In this case the user doesn't need any RDP/SSH client, they can access via Azure portal.
   
Application Gateway representation:

![image](https://user-images.githubusercontent.com/6425536/143800123-ae6429d7-2620-4e19-80c3-2da5e97c4f92.png)

Azure Traffic Manager:
   - Say if the Application is running in different regions like West US, East US, UK. Configuring a Traffic manager, will redirect the User from West US to West US hosted servers.

ExpressRoute:
   - One option to connect on-perm to Azure network with dedicated connection.   
   
   ![image](https://user-images.githubusercontent.com/6425536/143802687-27db65c9-b88c-49ca-a2d7-381f6e915293.png)

