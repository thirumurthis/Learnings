
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
     
     
