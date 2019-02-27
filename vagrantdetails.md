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
      :- In case of VM box is not running use > vagrant status and > vagrant up (to run the VM box.)
      :- In case of any network interface exception try > vagrant up command couple of times

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

**ABOUT VagrantFile details:**

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

**Note: other commented configuration code in the Vagrant file is not recommended to change by HashiCorp.**

 *The below commented code in vagrant file checks for updates when the box is started. default value is true.*
 
   ```     
    # config.vm.box_check_update = false 
   ```
   
**No need to uncomment this code, unless it needed.**

  Below code from vagrant File content is hypervicer provider specific 
 ```    
       # Example for VirtualBox:
       #
       # config.vm.provider "virtualbox" do |vb|
       #   # Display the VirtualBox GUI when booting the machine
       #   vb.gui = true
       #
       #   # Customize the amount of memory on the VM:
       #   vb.memory = "1024"
       # end
       #
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

Once the box is up, use *> vagrant ssh* to connect to the box, and at the root level should be able to see /vagrant_data with the data synced. 
    - if the data directory has files it will be synced automatically.
    
  **Note:**
      - The host (windows) machine Vagrantfile config, will be sync'ed in the vagrant box at root level under */vagrant* folder.
      
 
 
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
