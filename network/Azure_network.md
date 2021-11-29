
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
| Route table.. | Say if a machine wanted to access on-perm network, then create a route rule with an ip address (10.10.0.1/16). So if any machine in the VNet network tries to access the IP address rule of the ruote table, takes to on-perm network. | 
| NSG | NSG (Network security Group) is used to filter network traffic to and from Azure resources in Azure VM. A NSG contains security rules that allow or deny inbound netowrk traffic to, or outbound traffic from, several types of resources. Each rule requires destination, port and protocol. | 
| NIC | A network interface (NIC) is the interconnection between a VM and a virtual network (VNet). A VM must have at least one NIC, but can have more than one, depending on the size of the VM you create. Each NIC attached to a VM must exist in the same location and subscription as the VM. Each NIC must be connected to a VNet that exists in the same Azure location and subscription as the NIC. You can change the subnet a VM is connected to after it's created, but you cannot change the VNet.|
| Service EndPoint | Virtual Network (VNet) Service Endpoint policy allows to filter egress (outgoing) VNet traffic to a particular Azure service. The Azure service can be Azure Storage account, Azure Database. This provides granular controll over the network access |
| ASG (Application Security Group) | Application Security Group helps to manage the security of Virtual machines by grouping them according to the applicaton that run on them. An Application centric way of using NSG. Say, if there are group of VM's act as Web server, few VM's act as Application Servers, few VM's act as DB servers - Say the DB server should be connected by the Application Web server. So the using ASG we can provide granular controll. Addign a VM to the service group the ASG can be applied.|
| Azure Firewall| Azure Firewall is a managed, cloud based network security service that protects Azure Virtual network.|
| Azure Firewall Manager | This is a security Management service that provides central security policy and route manamgement for cloud-based security perimeters. |
| Bastion Host (also called as Jump box) | Azure provided service for more security. |
| NAT Gateway | Virtual Network NAT (Network Address Translation) simplifies outbound-only internet connectivity for virtual networks. It basically hides the identity or IP address of the VM to the client. |


Bastion:

![image](https://user-images.githubusercontent.com/6425536/143789522-22542165-5ce5-4bdd-a751-c43c8062da80.png)

To access the VM in the VNet,
   Option 1: Expose the RDP port (in case of Windows machine) and SSH port (in case of Linux) so it can be accessed over Internet, but this is NOT a secure way to do in enterprise.
   Option 2: Bastion server based login for more secure approach. Azure provides Bastion Host/server which accepts connection over HTTPS (or port 443) protocol. In this case the user doesn't need any RDP/SSH client, they can access via Azure portal.
   
 

