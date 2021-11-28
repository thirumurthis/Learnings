
Simple Representation and details:

![image](https://user-images.githubusercontent.com/6425536/143666148-b025f159-d459-47dd-a716-f9b93912553f.png)


Wireless Access Point and Wireless Bridge info

![image](https://user-images.githubusercontent.com/6425536/143666266-143147cd-7eea-439e-adee-979f03217af0.png)



DMZ - (DeMilitarization Zone)
    - This is the network between two firewall.
    
![image](https://user-images.githubusercontent.com/6425536/143732186-b022577b-4ff2-405b-8771-1326d5ef9510.png)


Network GateWay:
   - A Gateway is a network node that connects two networks using different protocols together.
   - It also acts as a gate between two networks.
   - It may be a router, firewall, server or other device that enables traffic to flow in and out of the network.

Different network cannot communicate with each other.
   - say, a network with CIDR 10.0.0.0/8 and another network with CIDR 20.0.0.0/8 cannot communicate directly.
   - In order to communicate we use a centralized device called router (but in case of router, it can only communicate with the same protocol).
- Gateway is also called as protocol convertors.

Difference between Gateway and Router
  - Router: 
      - Works on Layer 3 and Layer 4 of OSI Model
      - Supports dynamic routing
      - Routers provide additional features like DHCP server, NAT, Static Routing, and Wireless Networking, Mac address.
      - Route traffic from one network to another

   - GateWay:
      - Works up to Layer 5 of OSI Model
      - Doesn't support Dynamic routing
      - Protocol conversion like, VOIP to PSTN or Network Access control etc.
      - Translate from one protocol to another
