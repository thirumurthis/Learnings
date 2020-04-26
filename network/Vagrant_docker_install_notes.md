After installing `vagrant` and `virtual-box` to setup a docker for linux in centos 8 bento box.

Vagrantfile content

```
# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://vagrantcloud.com/search.
  config.vm.box = "bento/centos-8"

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # NOTE: This will enable public access to the opened port
   #config.vm.network "forwarded_port", guest: 443, host: 8085
   
  config.vm.hostname = "docker-node1"
  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine and only allow access
  # via 127.0.0.1 to disable public access
  # config.vm.network "forwarded_port", guest: 80, host: 8080, host_ip: "127.0.0.1"

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.0.35"
  config.vm.network "private_network", type: "dhcp"
 
  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
   config.vm.network "public_network", 
   use_dhcp_assigned_default_route: true
   
   #config.vm.network :private_network, ip: "192.168.33.10", netmask: "255.255.255.0", :mac =>"080027822020", name:"vboxnet1", :adapter => 2
   #config.vm.network :public_network, bridge: "wlp59s0", ip: "192.168.33.10", :mac => "080027262020", :adapter => 3
  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider "virtualbox" do |vb|
  #   # Display the VirtualBox GUI when booting the machine
  #   vb.gui = true
  #
  #   # Customize the amount of memory on the VM:
     vb.memory = "512"
    
    #vb.customize ["modifyvm", :id, "--cpuexecutioncap", "50"]
   
   end
end
```

For the first time issue `$ vagrant init` which will create a Vagrantfile with bare minimum details.

Update the Vagrantfile with the above content.

Once the Vagrantfile is created then issue `$ vagrant up` from that specific directory. 
For the first time it will download the images.

After the vagrant box is up, issue `$ vagrant ssh` to login, the default use info would be `vagrant@vagrant`.

For installing docker, 
  - update the `yum` package manager usign `$ sudo yum update -y`
  - install `$ sudo yum install -y epel-release` and `$ sudo yum install -y vim git`
  - update yum config manager `$ sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo`
  - check if `dnf` is avilable using `$ sudo dnf`
     - dnf is used to download the containerid.io dependency, since docker-ce requires it.
      - `$ sudo dnf install https://download.docker.com/linux/centos/7/x86_64/stable/Packages/containerd.io-1.2.6-3.3.el7.x86_64.rpm`
   - To install the `docker-ce` issue `$ sudo yum install -y docker-ce`
   - Verify if docker is installed as service using `$ systemctl status docker`
   - Start the docker service using `$ systemctl start docker`
   - To investigate the service logs, use `$ journalctl -u docker`
   - Issue to see if the docker is running  `$ sudo docker version` should display the docker server info.
   - Include the docker user to user group, check the links below.

Trouble shooting for permission denied as vagrant user, 
check (docker link post installation)[https://docs.docker.com/engine/install/linux-postinstall/]

(link forum)[https://www.digitalocean.com/community/questions/how-to-fix-docker-got-permission-denied-while-trying-to-connect-to-the-docker-daemon-socket]

```
[vagrant@chefnode1 ~]$ docker version
Client: Docker Engine - Community
 Version:           19.03.8
 API version:       1.40
 Go version:        go1.12.17
 Git commit:        afacb8b
 Built:             Wed Mar 11 01:27:04 2020
 OS/Arch:           linux/amd64
 Experimental:      false
Got permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock: Get http://%2Fvar%2Frun%2Fdocker.sock/v1.40/version: dial unix /var/run/docker.sock: connect: permission denied
```

To fix the permission denied issue.
```
$ sudo groupadd docker

$USER will be vagrant in this case
$ sudo usermod -aG docker $USER

# restart the VM
```
Reference link:

  [Install docker in Centos 8](https://linuxconfig.org/how-to-install-docker-in-rhel-8)
  
  [Setup user group in Centos 8](https://linuxconfig.org/redhat-8-add-user-to-group)
  
  [Setup sudo user in Centos 8](https://devconnected.com/how-to-add-a-user-to-sudoers-on-centos-8/)

