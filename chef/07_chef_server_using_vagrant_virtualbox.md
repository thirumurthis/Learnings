#### Install `Vagrant` and `VirtualBox`.
  - Using windows 10 home, with WSL2 (Windows subsystem linux), with hyper-v. 
  - If you run docker on the same machine, the hyper-v will be enabled as Auto
  - If the hyper-v is enabled, VirtualBox will NOT work.
  
To make virtualbox work, issue below command. For more info check [link]( https://stackoverflow.com/questions/50053255/virtualbox-raw-mode-is-unavailable-courtesy-of-hyper-v-windows-10/50119065#50119065)
  ```
  > bcdedit
  #### check for hypervisorlaunchtype : Auto => if this is Auto, then Virtualbox won't startup 
  ```
  - disable the hypervisor and restart the system. Below command will disable hyper-v
  ```
  > bcdedit /set hypervisorlaunchtype off
  
  ### restart the system/laptop
  ```
 - to enable it use below command 
 ```
  > bcdedit /set hypervisorlaunchtype Auto
  
  ### restart the system/laptop
 ``` 

#### STEP 1: Install `Chef Workstation`
  - use `chef --version` to list the installed package.

#### STEP 2: Create a directory to download the `Vagrantfile` for Chef Infra server setup. `mkdir chef-repo`
#### STEP 3: Navigate to chef-repo directory, download the Vagrantfile
```

## Windows
>  Invoke-WebRequest -OutFile Vagrantfile https://learnchef.s3.eu-west-2.amazonaws.com/knife-profiles/Vagrantfile

## Linux
$ curl https://learnchef.s3.eu-west-2.amazonaws.com/knife-profiles/Vagrantfile > Vagrantfile
$ wget https://learnchef.s3.eu-west-2.amazonaws.com/knife-profiles/Vagrantfile 
```

#### STEP 4: Run the instance
```
> vagrant up
```

#### STEP 5: Update local host to use name instead of ip address
```
### Windows
 Add-Content C:\Windows\System32\drivers\etc\hosts "192.168.33.199 learn-chef.automate"
 
### Linux
echo 192.168.33.199 learn-chef.automate | sudo tee -a /etc/hosts
```
--------------
#### CLEAN up

```
> vagrant suspend
> vagrant destroy
```

#### Update the hostname in case of linux
```
$ sudo sed -i '' '192.168.33.199 learn-chef.automate/d' /etc/hosts
```
