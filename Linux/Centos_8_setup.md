### Setting up the centos:

- Download the iso (dvd) image.
- In VirtualBox create a new VM
   - Setup a Virtual Harddisk of size 35GB or more.
   - Set the network to Bridged Network and select the Wifi or EtherNet adapter.
 - After selecting the appropriate options
 - creating root password and user account.
 - Agreeing upon the license
 - use `ifconfig` to see the network ip address.
 - When issuing `sudo yum update -y` displayed sudo access not provided.
    - Add the file (with username as filename) and include the content `thiru ALL=(ALL) ALL` 
    - Note: thiru is username, and the best practice to name the file same as the username, this if for maintanbility.
    - sudoer.d contains files and can be configured so user will be able to execute specific task as sudo user 
    - `username ALL=(ALL) NOPASSWD:/usr/bin/du,/usr/bin/ping`
  - Other option in case of RHEL and Centos is to add to the default wheel group ` $ usermod -aG wheel username`, but this is not working for some reason. Note: adding the file in sudoers.d worked.
  
In the brand new setup, below will be displayed, no IPV4 ip setup.

![image](https://user-images.githubusercontent.com/6425536/82537228-86133d80-9afe-11ea-89c7-775e52afda42.png)

In order to add a static ip address navigate to `/etc/sysconfig/network-scripts/` folder copy the `ifcfg-esp0s3`
or `ifcfg-lo` (lo - is loopback address/localhost).

Content (completely comment out the IPV6 configs)

![image](https://user-images.githubusercontent.com/6425536/82538907-5154b580-9b01-11ea-95a7-b33b67e8d847.png)

use `$ sudo nmtui` command for UI based setup of host and network name.
once updated restart the networks service `$ systemctl start NetworkManager` if the `$ systemctl start network`.

Also note that use the `$ dnf check-update`, after the above steps, it will update the system. initially the network was slow.

But after sometime dnf command downloaded the package. `$ ping -c 5 google.com` should output correctly.

Note:
  - During the centos 8 installation step, after editing the network selection modifying the hostname.
  - for some reason there was NO need to edit the `/etc/sysconfig/network-scripts` ifcng-enp0s3 file.
  - After allowing sudo access, `$ sudo nmtui`, selected `enps03` network option, hit Edit, selected "Automatically connect"
  - after hitting OK, restarted the Networkmanager server `$ sudo systemctl restart NetworkManager`. 
  - upon `$ ip a` and `$ ifconfig` able to see an allocated ipaddress.

---------

Making the screen size bigger in Virtualbox:

For a linux machine to install the VirtualBox guest addition, to make the window full screen.

For `VirtualBox Guest addtion` installation on linux check the [link](https://www.virtualbox.org/manual/ch04.html#mountingadditionsiso) 

Upgrading the kernel for the centos8 machine to enable full screen capablity:
[VirtualBox Reference link](https://www.virtualbox.org/manual/ch02.html#externalkernelmodules)

Follow below command:

`sudo dnf install gcc`

`sudo dnf install make`

## check the firmware mode EFI/UEFI then different package needs to be installed
## for BIOS use the below 
```
sudo dnf install kernel-devel*

(or)

sudo dnf install kernel-devel-$(uname -r)
```

`sudo dnf install elfutils-libelf-devel`

Remove the centos8 image if that was mounted already for the first time and mount the VirtualBox guest iso.
click Devices -> insert guest addition cd image 
(Note: if already mounted, remove that from selecting Devices -> cd image.)

Upon removing the iso and adding it again, i see only the success message. 

There was no update on any of the dirver packages.

So navigated to the mounted drive location use `df -h` to list the device mounted. Navigate and run the `sh ./VBoxLinuxAdditions.run` to see if everything is alright. (as mentioned in the VBox documentation chapter 4)

## The shell script `VBoxLinuxAdditions.run` takes some time to update the modules, since we already have updated the gcc, make and kernel packages it will include the additional modules.

Restart the system. Nothing worked!!!

After the system rebooted, just go to the settings, Display change the resoultion in my case chosse `1200 x 768` (close to this).

Also i ejected the VBoxGuestAddtion.iso from Device -> CD/ios images or simply selecting Files -> CD eject within the Linux centos 8 

Check the image, where the script was executed:

![image](https://user-images.githubusercontent.com/6425536/82738997-05686300-9cf1-11ea-9d71-d168c8f3bfcb.png)

After selecting the resolution (there is a vertical scroll bar)

![image](https://user-images.githubusercontent.com/6425536/82739403-22526580-9cf4-11ea-83e9-ea15b5746be2.png)

------


