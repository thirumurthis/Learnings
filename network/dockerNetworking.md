Docker container network contians three major part:
 - __Container network model (CNM)__
 - __libnetwork__
 - __drivers__
 
 
`Cotainer network model` is a design specification for docker container, originated from `Docker Inc`.
  - Check the specs at [git link](https://github.com/docker/libnetwork/blob/master/docs/design.md).

`Container network interface` is a rivial for CNM, and used in Kubernetes environment, orgintated from `CoreOs, Inc.`


`CNM` defines three main constructs
  - `Sandbox` (a.k.a namespace in linux kenrel)
     - Kind of fenced area of OS or isolated area of OS, where we can tweak and do changes in isolation without impacting other area.
     - Once the sandbox is created, inside of it network stacks can created like ipconfig, DNS, Routing table and all network needed.
     - Sandobx is like a container without no app running inside it.
    
 - `Endpoint` (Network interace)
     - like eth0 in the linux world
     - like LAN in the Windows world
     
 - `Network` 
     - connected endpoints.
        
`Libnetwork` (canoncial implementation of CNM)
   - libnetwork is the actual implementation of the CNM developed in Go/Golang code by Docker Inc.
   - documentation details [git link](https://github.com/docker/libnetwork)
   - `socketplane` startup acquisation rewrote the whole Docker network and made it more robust, scalable.
   - The Libnetwork is written in Golang, so it is cross platform runs in ARM, etc.
   - plugable architecture
   
`Drivers` (network specific detail implementation)
   - when `libnetwork` implements and defined the fundametal the sandbox, endpoint, network, management API, UX etc. 
   the actual specifics different network types local bridge network, multihost overlay leverage VXLAN are implemented in drivers
  
  Type of drivers:
      - `local` => native
      - `remote` => third party drivers implementing the specs defined by libnetwork.
  
  | CNM | Libnetwork | Drivers |
  | --- | ----- | ----|
  |Design/DNA| Control plane & management plane| Data plane|
  
  
   ![image](https://user-images.githubusercontent.com/6425536/80312818-5a6d9380-879c-11ea-8406-392323e22eed.png)

`network` is a sub command within the docker

```
$ docker network ls                                                                      NETWORK ID          NAME                DRIVER              SCOPE
2fc7116b9f21        bridge              bridge              local
9115882c1e94        host                host                local
ce0dd5291963        none                null                local
```
`Note about the docker network output`:
  - NAME are arbitary, not necessary to be the same as the DRIVER.
  - SCOPE are local which means single-host, SWARM scope are multi-host.

__docker network inspect \<id or name from the network ls above\>__

```
$ docker network inspect bridge                                                                               [
    {
        "Name": "bridge",
        "Id": "2fc7116b9f2115d8ed3958215c71896f0f7f16ff86535024303688e5c9d0077b",
        "Created": "2020-04-26T15:27:34.010794339Z",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        "Labels": {}
    }
]
```

`Notes about the above command output`:
  - Ipv6 is not configured. Refer the `EnableIPv6` attribute.
  - IPAM (ip address management) - This is for specific to this network.
     - default one is used with subnet/ gateway, this pluggable.
  - This is not an internal network refer `internal` attribute.
  - Containers are not connected to this network, refer `Containers` attribute
  - Which bridge is this docker connected to in the case `docker0`, refer `com.docker.netowrk.bridge.name`
     - The options info might be different based on OS architecture (on X64 machine), widows will display different tools.
  
##### `docker network connect <container-name-or-id>` - to connect to the container network.

##### `docker info` - displays the container info, check the Network it is connected to.


  
  
   
