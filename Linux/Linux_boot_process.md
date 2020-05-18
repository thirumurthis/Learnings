
Linux Boot process Steps:
  - Power ON
  - BIOS
  - Master Boot Record (MBR) [first sector of the Hard disk]
  - Boot Loader (example: GRUB)
  - Kerner (Linux OS)
  - Initial RAM disk (initramfs image)
  - /sbin/init (parent process)
  - Command shell (using getty)
  - X Window System (GUI)
  
 
 `Kernel` - makes the interaction between the Hardware and the application possible.
 Version [Linux Kernel](kernel.org)
 
 `distribution` or `distro` - Collection of software making up a Linux bases OS.
 
 ```
 
                      Linux Kernel
Distro          /            |           \
Family      Fedora           Debian        SUSE  .....
             |               |             |
  ---------------------------------------------------
Distros      |               |             |
           RHEL            Ubuntu        SLES
             |               |             |
           /   \             |             |
      CentOS   Oracle      LinuxMint      OpenSUSE
                Linux
              
 ```

`boot loader` - boots the operating system, __`GRUB`__ and __`ISOLINUX`__.
`service` - a program running as an background process.
`filesystem` - `etx3`,`ext4`,`FAT`,`XFS`, `NTFS`, and `btrfs`.
