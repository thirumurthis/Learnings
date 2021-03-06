Install the vagrant to appropriate environment. (windows)
 
*<< Note: > - follows a command >>*
once installed, open power shell

use below command to verify installation:
```
 > vagrant -v
```
*output:*
  version will be displayed.

*---- once installation verified -------*

creat a folder under home directory (~ or %HOMEPATH%)
```
   > mkdir firstVagrantbox
   > cd firstVagrantbox
```
  - the folder will be an unique environment and doesn't intercept with other vagrant configs box environment.
  - this folder isolates the vagrant box from others
   
**COMMAND to INIT box:**
issue below command to initialize box.
```
  > vagrant init bento/ubuntu-16.04
```
bento - is the company manages the ubuntu distributions.
-  The `init` will create a new file called "Vagrantfile".
-  This file will have the appropriate box which has the configuration.

**COMMAND to RUN box:**
```
  > vagrant up 
```   
  - This command will run the box
  - This command checks the name of the box (eg. bento/ubuntu-16.04)
  1. Vagrant maintains a local cache of boxes 
       - if not in local it will download from vagrant cloud. <<this will be a step in up output.>>
       - the file in the cache are compressed.
  Location of cache, C:\Users\<user_name>\.vagrant.d\boxes\bento-VAGRANTSLASH-ubuntu-16.04\201812.27.0\virtualbox
  files under this folder: 
            box, metadata.json, virutaldes(.vmdk file), vagrantfile.
   2. Last step,
         vagrant will merge the VagarantFile from the box, with the one created under
         the independent folder (eg. firstVagrantbox/ folder)
 
 **Output of Command:**
```          Bringing machine 'default' up with 'virtualbox' provider...\
          ==> default: Box 'bento/ubuntu-16.04' could not be found. Attempting to find and install...
              default: Box Provider: virtualbox
              default: Box Version: >= 0
          ==> default: Loading metadata for box 'bento/ubuntu-16.04'
              default: URL: https://vagrantcloud.com/bento/ubuntu-16.04
          ==> default: Adding box 'bento/ubuntu-16.04' (v201812.27.0) for provider: virtualbox
              default: Downloading: https://vagrantcloud.com/bento/boxes/ubuntu-16.04/versions/201812.27.0/providers/virtualbox.box
              default: Download redirected to host: vagrantcloud-files-production.s3.amazonaws.com
              default:
          ==> default: Successfully added box 'bento/ubuntu-16.04' (v201812.27.0) for 'virtualbox'!
          ==> default: Importing base box 'bento/ubuntu-16.04'...
          ==> default: Matching MAC address for NAT networking...
             ....
```
*NOTE:* 
      Now the box is running, IN HEADLESS MODE - means no UI.
      VM is not having a window, if the window closed we can't see anything.
      
 **COMMAND to check STATUS of VAGRANT box:**
 ```
   > vagrant status
 ```
   - command will provide the status of the running VM
   - this is easy in case on single VM
   - ouput will be the status of the VM <eg. running.>
      
 **COMMAND to check all VM in the machine:**
This command can be executed at any directory
``` 
   > cd .. (move one level up to firstVagrantbox/)
   > vagrant global-status
```
   - this command list all status of the VM vagrant in the machine
**OUTPUT:**

|id   |    name |   provider |  state |   directory | 
------|---------|-------------|--------|-------------|
|74411a2 |  default | virtualbox | running | C:/chef |
|173c999 | default | virtualbox | running  | C:/Users/thirumurthi/myfirstbox |
|a30ca55 | default | virtualbox | poweroff | C:/Users/thirumurthi |

**COMMAND to execute in one specific VM:**
- Grab the id from the above command,  
  ``` 
     > vagrant halt 74411a2 
  ```
   - this command will shutdown the running VM
      
**HOW TO CONNECT TO THE RUNNING VM BOX?**
   - by default, ubuntu not include graphical shell
   - vagrant vm is headless mode (unless configured for graphical interface)
   - connect using SSH is common method
       - terminal which used to connect
   - Vagrant includes SSH client to connect.
 
 **COMMAND To CONNECT using SSH:**
  USE command prompt / power shell:
  ``` 
     > vagrant ssh
  ```
   - above command will connect to the vagrant box, user name and host name is 
     *vagrant@vagrant:*
   - use linux command `ifconfig` to list the network details of the box.

 *Note:* 
  - In case of VM box is not running use *> vagrant status* and *> vagrant up* (to run the VM box.)
  - In case of any network interface exception try *> vagrant up* command couple of times

  ```   
    > exit  - command to disconnect the box.
  ```   
    ssh command is used to run the commands in the box like install configration, etc.
    
 **COMMAND to SHUTDOWN the running box:**
  ``` 
    > vagrant halt 
 ```
  - command will be shutdown gracefully.
  - to start again then issue > vagrant up
  
 **COMMAND to DELETE and RESET the way the box was initally set:**
 ``` 
    > vagrant destory
 ```

Vagrant Cloud :
  Base box - starting point of the vm boxes that has been downloaded.
  The base box can be created and uploaded to the public / private vagrant cloud account.

Vagrant File :
 - Vagrant box are configured using Vagrantfile
 - This VagrantFile are ruby files.

**Editing Vagrantfiles using visual studio code:**

<<Using visual safe code, (view -> extensions type vagrant) install. 
Use Integrated environment for entering following command.>>
  ``` 
     > cd firstVagrantbox
     > code -r .  (this command in Vs code, to open the appropriate directory where the vagrantfile is present.)
  ```    
  Ctrl~ opens, the powershell within the VS code.

***ABOUT VagrantFile details:***

   *within the VS code power shell, if we create a new folder and issue > vagrant init*
   
     # code below in vagrant file is to indicate the version (2) of Vagrant file being used.
```
     Vagrant.configure("2") do |config|
```
     # code below indicates which box we are using, in this scenario the value is base which needs to be changed 
     # this will be used by vagrant to build.

```
     config.vm.box = "base"  << use "bento/ubuntu-16.04">>
```
     # once above is completed and issued > vagrant up , will start the virtual box.

**Note:**
     HashiCorp recommends to leave the configuration code in the Vagrant file, unless it is really necessary to update.

 *The below commented code in vagrant file checks for updates when the box is started. default value is true.*
 
```     
    # config.vm.box_check_update = false 
```
   
**No need to uncomment this code, unless it needed.**

  Below code from vagrant File content is hypervicer provider specific 
 ```    
       # Example for VirtualBox:
       # config.vm.provider "virtualbox" do |vb|
       #   # Display the VirtualBox GUI when booting the machine
       #   vb.gui = true
       #   # Customize the amount of memory on the VM:
       #   vb.memory = "1024"
       # end
```

**COMMAND TO Reconfigure or HALT & START Vagrant box:**
```
    > vagrant reload
```
 
**SYNC'ING HOST FOLDER with VAGRANTBOX folder: **
   
   - Vagrantfile (in windows, C:/firstVagrantBox/Vagrantfile)
   
    # Share an additional folder to the guest VM. The first argument is
    # the path on the host to the actual folder. The second argument is
    # the path on the guest to mount the folder. And the optional third
    # argument is a set of non-required options.
 ```
      # config.vm.synced_folder "../data", "/vagrant_data"
 ```
Above *config.vm.synced_folder* should be uncommented.
 - The first parameter **"../data"** - is the directory that needs to be synced in host (windows) machine.
  - The folder "data" should be created one level above the folder where Vagrantfile config is present.
 - The second parameter **"/vagrant_data"** will be at the root level of the vagrant box.

use the *> vagrant reload* command to reload the Vagrant box (command is combination of halt & up)

 - Once the box is up, 
 - use *> vagrant ssh* to connect to the box 
 - at the root level, a folder /vagrant_data should be created with the ../data synced automaticall. 
     
**Note:**
 - The host (windows) machine Vagrantfile config, will be sync'ed in the vagrant box at root level under */vagrant* folder.
      
***VAGRANT NETWORKING:***
Three types of,
 - Port Forwarding
 - Private Network
 - Public Network
 
 **Note:**
  - Vagrant is not secure enough, for exposing to public network different setting needs to be done.
 
 
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
