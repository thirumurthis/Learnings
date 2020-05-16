
##### VM's representation on Host machine, created using `Vagrant` and `VirtualBox`.

In order to connect to the physical network, VM's need below two component,
- `Virtual NIC (Network Interface Card)` vNIC
- `Virtual Switches/Bridges`
    - virtual switches are logically defeined __`layer 2`__ device which transfer frames between nodes.

Representation of the network components

![image](https://user-images.githubusercontent.com/6425536/82128495-76979b80-9770-11ea-89e7-6f20be38a48f.png)

 - vNIC's of VM are connected to the virtual ports on the virtual switch.
 - This virtual is connected to through the host physical NIC to the physical network.
 
 __`Virtual Switches`__ 
  - Virtual Switches are similar to the physical switches, which creates a separate broadcast domain.
  - To connect two broadcast domains, we need __`layer 3`__ router.

Below is the depiction of using `Router (layer 3)` connection between two networks.

![image](https://user-images.githubusercontent.com/6425536/82128820-f161b600-9772-11ea-8e1d-7a10eae85fdb.png)


`Virutal machine network connection:`
  - There are three network connection modes in VM, when Virtual NIC is setup.
     - 1. `bridged connection`
     - 2. `NAT`
     - 3. `host-only`
     
`Bridge connection`
    - The VM directly connect to the physical network through host machine physcial NIC.
    - From the below representation, the host machine's physical NIC acts as a `bridge` for the three VMs to the physcial network.
    - Just like any host and physcial computer, `the VM's obtain IP address infromaton from `__`DHCP`__` server on the physical network`. (DHCP server will be within the router itself).
    - When VM's connected using `bridge network mode`, the VM's appears to other nodes as just another computer/host on the network.

Representation of the connection to physical network.    
![image](https://user-images.githubusercontent.com/6425536/82129844-c1b6ac00-977a-11ea-98f9-42471f5244e3.png)

  - We can install physical NIC's to each Virtual machines (VM's).
  - Doing so, each VM's virtual NIC gets a connection to its own dedicated physical NIC.
  - In bridged connection mode, the IP addresses of these VM's are visible to other computers in the network and directly accessible by other computers on the network.
  - This kind of setup can be used for `mail server`,`file server` or `web server`.
  
__`Network Address Translation (NAT)`__ mode:
 - In this mode, the VM's rely on the host to act as a NAT device.
 - So in this mode, the `virtual DHCP server` is responsible for assigning IP address information to these three VM's depicted below in the image.
 - Thus forming a `private network`.

![image](https://user-images.githubusercontent.com/6425536/82130608-ab601e80-9781-11ea-9921-b6d64761463a.png)

 - The other physical machines in the network gets the IP address information from the physical `DHCP server`. Thus these form a `external physical network`.
 - The `host machine` is sitting between the two networks
     - This host machine translates the IP address from VM's to IP address of the host. 
     - This host machine also listens for returning traffic, so it can deliver it to the VM.
 - The external physical network sees traffic from the VM's as if it is coming from the host itself.
 - The NAT mode is appropriate to use when virtual machine are mainly used as a `client workstation` used to check emails, surf internet.


__`host-only`__:
  - In this mode, the VM's on the host can talk with each other and with their host. They cannot communicate with any other computers in that network.
  - This is appropriave for `isolated private virtual network` for performing cyber attack experiments.

