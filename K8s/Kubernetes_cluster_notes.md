#### Setting up the K8s Cluster with one master and two nodes, using vagrant and virtual box.

After cloning the project [git](https://github.com/thirumurthis/kubernetes). Refer [Video](https://www.youtube.com/watch?v=HTj3MMZE6zg)

Run the vagrant up
   - There might be few hickups, just if any of the node is up, login in using `vagrant ssh <machine-name>`
   - Then issue the `sudo yum update` to update all the components.
   - Make sure the ssh is running using `suod systemctl status sshd`.

If you see any network issue due to Vagrant
----
#####   When setting up the Vagrant box `centos7` got the below exception

```
==> kmaster: Booting VM...
There was an error while executing `VBoxManage`, a CLI used by Vagrant
for controlling VirtualBox. The command and stderr is shown below.

Command: ["startvm", "d67ae16e-30fa-435f-ae4c-ab80b70c9238", "--type", "headless"]

Stderr: VBoxManage.exe: error: Failed to open/create the internal network 'HostInterfaceNetworking-VirtualBox Host-Only Ethernet Adapter #2' (VERR_INTNET_FLT_IF_NOT_FOUND).
VBoxManage.exe: error: Failed to attach the network LUN (VERR_INTNET_FLT_IF_NOT_FOUND)
VBoxManage.exe: error: Details: code E_FAIL (0x80004005), component ConsoleWrap, interface IConsole
```

Solution:

  check this [link](https://www.howtoforge.com/setup-a-local-wordpress-development-environment-with-vagrant/)
  
  - 1. Installed the Vagrant host updater plugin, from the windows 10 machine
  ```
  vagrant plugin install vagrant-hostsupdater
  ```
      
   - 2. Update the host driver as instructed below.
   ```
Note the Adapter referred here:  VirtualBox Host-Only Ethernet Adapter #2
Open Control Panel -> Network and Sharing Center. 
Now click on Change Adapter Settings. 
Right click on the adapter whose Name or the Device Name matches with VirtualBox Host-Only Ethernet Adapter # 2 and 
      click on Properties. 
      Click on the Configure button.
      Click on the Driver tab. 
      Click on Update Driver. 
               Select Browse my computer for drivers. 
               Now choose Let me pick from a list of available drivers on my computer. 
               Select the choice you get and click on Next. 
               Click Close to finish the update. N
     Now go back to your Terminal/Powershell/Command window and repeat the vagrant up command. (use the admin command prompt) 
     It should work fine this time.
   ```
  
  ##### Vagrant K8s node setup:
   - The First issue was the host machine was not able to access the Virtual machine VM's, the main thing is to setup the hostonly network.
  
  - After cloning the github project justmeandopensource for k8s playground.
  - I had to modfiy the vagrant file for network, kmaster yum update script.
  ```
  ## network change was to add a public and private network as below
    
    kmaster.vm.network "private_network", ip: "172.42.42.100"
    kmaster.vm.network :public_network, :bridge => "Intel(R) WiFi Link 1000 BGN", auto_config: false
    
    workernode.vm.network "private_network", ip: "172.42.42.10#{i}"
    workernode.vm.network :public_network, :bridge => "Intel(R) WiFi Link 1000 BGN", auto_config: false
    
    ## The above changes include 1. HostOnly network, since this resulted in the above 
    ## Network exception i had to install the driver on that adapter.
  ```
 
 After fixing the expected is, the three adapter was displayed as exepceted and were accessed uing ping command on that specific 172.42.42.100
 ```
 ==> kmaster: Preparing network interfaces based on configuration...
    kmaster: Adapter 1: nat
    kmaster: Adapter 2: hostonly
    kmaster: Adapter 3: bridged
==> kmaster: Forwarding ports...
    kmaster: 22 (guest) => 2222 (host) (adapter 1)
==> kmaster: Running 'pre-boot' VM customizations...
==> kmaster: Booting VM...
==> kmaster: Waiting for machine to boot. This may take a few minutes...
    kmaster: SSH address: 127.0.0.1:2222
 ```
---

- From the laptop (host), navigate to `C:/User/<name>/.kube` backup the `config` file.
- Login to the `kmaster` node, navigate to `/etc/.kube/config copy the content to the host location of C:/User/<name>/.kube/config.
- Open up a command prompt and try `ping 172.42.42.100` the ip address used in the Vagrantfile, if the ping is responding correctly.
- Try `> kubectl get nodes` which should list the running nodes.

```
C:\Users\thirumurthi>kubectl get nodes
NAME                   STATUS   ROLES    AGE   VERSION
kmaster.example.com    Ready    master   27h   v1.18.3
kworker1.example.com   Ready    <none>   25h   v1.18.3
kworker2.example.com   Ready    <none>   25h   v1.18.3
```
```
C:\Users\thirumurthi>kubectl cluster-info
Kubernetes master is running at https://172.42.42.100:6443
KubeDNS is running at https://172.42.42.100:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```

#### Helm Client on the workstation
 - To install the helm client on windows host, download it from the git (widnowsamd64).
 - Edit the Environment variable under system variables, HELM_HOME=<path of the helm exe)
 - in the command prompt try `> helm --version`

#### Helm Tiller setup from the workstation using kubectl
 - Create a service account nammed tiller, using below command:
 ```
 ## -n is the namespace
 > kubectl -n kube-system create serviceaccount tiller
serviceaccount/tiller created 
 ```
 - Create a RBAC rule using `clusterrolebinding` and `clusterrole`.
 ```
 >kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
clusterrolebinding.rbac.authorization.k8s.io/tiller created
 ```
 - To verify the `clusterrolebinding` tiller creation, use the below command
 ```
>kubectl get clusterrolebinding tiller
NAME     ROLE                        AGE
tiller   ClusterRole/cluster-admin   2m5s
 ```
 
