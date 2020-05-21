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
    - Add the file (with username as filename) and include the content `thiru ALL=(ALL) ALL` Note: thiru is username.
 
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
