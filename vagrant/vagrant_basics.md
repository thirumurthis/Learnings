
### Vagrant basics

- Vagrant to setup MEAN/LAMP stack 

- Pre-requsites, install Vagrant, and Virtual box (default provider). Windows comes with hyper-v

Init vagrant with a box
```
# simple ubuntu plain box
> vagrant inti ubuntu/trusty64

# above command will create a Vagrantfile.
```
 - Main components of Vagrantfile
```
  config.vm.box  - Operating system defintion
  config.vm.provider - provider, default is Virtualbox
  config.vm.network - network configuration
  config.vm.sync_folder - mount files or set local machine folder
  config.vm.provisioner - provisioning the box after creating using vagarnt.
```

 - Terminology:
   - `Guest` - The virtual box vm itself.
   - `host` - The actual machine (windows where the virtualbox is installed.
   
 ##### Simple vagrant config (default file contains most of comments commented out)
 ```
   Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/trusty64
      
      config.vm.provider "virtualbox" do |vb|
         vb.memory = 1024
         vb.cpu = 2
      end
  end
 ```
 
 - whenever the Vagrantfile is upadated, use the command `> vagrant reload` to restart the vagrant vm box to apply the changes.
 
 ##### Ways to setup the MEAN stack in the virtualbox vm
   - login using `ssh` command is `> vagrant ssh` , and update it using `sudo apt-get update`
   - using provisioner, like chef, shell to install once the virtualbox is created
   
 ###### setting up MEAN stack using ssh
  ```
  # login using ssh to virtualbox
  $ sudo apt-get install apache2
  # above command will install an apache server. the index.html default file will be created at /etc/www/html/index.html
  ```
  
 ##### update the vagrantfile to setup the network configuration
  
 ```
 Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/trusty64
      
      config.vm.provider "virtualbox" do |vb|
         vb.memory = 1024
         vb.cpu = 2
      end
      
      # with below configuration, if we use http://localhost:8080 from the laptop browser the index.html on
      # the VM box server can be accessed.
      config.vm.network "forwarded_port", guest: 80, host: 8080
 end
 ```
 
##### Setting up private network in Vagrantfile

 ```
 Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/trusty64
      
      config.vm.provider "virtualbox" do |vb|
         vb.memory = 1024
         vb.cpu = 2
      end
      
      # with below configuration, if we use http://192.168.33.68 from the laptop browser it will access index.html from 
      # the VM box server can be accessed.
      config.vm.network "private_netowrk", ip: 192.168.33.68
 end
 ```
 
##### how to mock a local domain address with the above private_network configuration, in this case like www.thiru.local
  
   -  in the laptop (host machine) edit the hostname file
     -  in windows, as admin user update the file `C:\Windows\System32\drivers\etc` add
     
```
      # multiple domain name can be set in the hostname file
      192.168.33.60 www.thiru.local thiru.local
```
  -  once the above changes is done, from browser try `http://thiru.local` to see the index.html

  
##### how to use the sync_dir, within the Vagrantfile config
  
```
 Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/trusty64
      
      config.vm.provider "virtualbox" do |vb|
         vb.memory = 1024
         vb.cpu = 2
      end
      
      # with below configuration, if we use http://192.168.33.68 from the laptop browser it will access index.html from 
      # the VM box server can be accessed.
      config.vm.network "private_netowkr", ip: 192.168.33.68
      
      # "." represent the current directory where the vagrantfile is present
      # "/etc/www/html" directory now points to this location in the host machine
      config.vm.sync_folder ".", "/etc/www/html"
 end
 ```
   - After updating the above configuration for folder, issing `> vagrant reload` will update the vagrant vm box.
   - now if we issue the `http://thiru.local` command, there won't be index.html listed instead the file .vagrantfile will be displayed
      - this signifies that the host directory is now being used by the apache2 server.
  
 ##### how to handle the directory permission with the Vagrantfile config
 
```
 Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/trusty64
      
      config.vm.provider "virtualbox" do |vb|
         vb.memory = 1024
         vb.cpu = 2
      end
      
      # with below configuration, if we use http://192.168.33.68 from the laptop browser it will access index.html from 
      # the VM box server can be accessed.
      config.vm.network "private_netowkr", ip: 192.168.33.68
      
      # "." represent the current directory where the vagrantfile is present
      # "/etc/www/html" directory now points to this location in the host machine
      config.vm.sync_folder ".", "/etc/www/html", :mount_options => ["dmode=777", "fmode=666"]
  end
```
 
 Note: `mac and linux` supports `nfs` (network file system) which increase the performance of the vagrantbox. [Windows not support it]
  - inorder to create a nfs, update the config as below
 
```
      config.vm.sync_folder ".", "/etc/www/html", nfs => { :mount_options => ["dmode=777", "fmode=666"] }
```
  - use `> vagrant reload`, will display the usage of nfs. It might prompt for system password.
    
##### using provisioner option to install the apache2 server
  - in the Vagrantfile update the below configuration
 
 ```
 Vagrant.configure("2") do |config|
      config.vm.box = "ubuntu/trusty64
      
      config.vm.provider "virtualbox" do |vb|
         vb.memory = 1024
         vb.cpu = 2
      end
      
      # with below configuration, if we use http://192.168.33.68 from the laptop browser it will access index.html from 
      # the VM box server can be accessed.
      config.vm.network "private_netowkr", ip: 192.168.33.68
      
      # "." represent the current directory where the vagrantfile is present
      # "/etc/www/html" directory now points to this location in the host machine
      config.vm.sync_folder ".", "/etc/www/html", :mount_options => ["dmode=777", "fmode=666"]
      
      # the manual steps are now put in as inline shell commands
      config.vm.provision "shell" , inline <<-SHELL 
        apt-get update
        apt-get install -y apache2
      SHELL
 end
```
 - Now destroy the box using `> vagrant destroy` and use the above config file to create the box `> vagrant up`.
 - Note: when running above config file, the index.html will be created under the mounted directory.
 
 ##### If we need to use a shell script file for provisioning then update the Vagrantfile config as below, comment the inline shell lines
```
   # bootstrap.sh file contains the apt-get update, etc commands.
   # if a LAMP stack needs to updated, provide the details in this sh file
   config.vm.provision "shell", path: "bootstrap.sh"
```
   `bootstrap.sh` file content
```sh
   apt-get upgrade
   apt-get install -y apache2
   a2enmod rewrite
   apt-add-repository <custom location>
   apt-get update
   # php 7 not available in the repo.
   apt-get install -y php7.2
   # other dependencies
   service apache2 start
   ....
   ....
```
  
