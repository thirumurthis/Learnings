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



   
