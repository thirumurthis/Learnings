
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
  
  `BIOS` in detail:
     - When starting x86 based Linux, when the powered on the Basic Input/Output system (BIOS) initialize the hardware, including screen and keyboard and tests main memory. {Process is called as Power On Self-Test (`POST`)}
     - BIOS software is stored in `ROM` chip of motherboard.
     - Once initialized, the remmainder of boot process is done by OS.
   
`MBR`
  - After POST, the system control is passed to `boot loader`.
  - boot loader usually stored on hard disk.
       - either in `bo
 
 `Kernel` - makes the interaction between the Hardware and the application possible.
 Version [Linux Kernel](kernel.org)
 
 `distribution` or `distro` - Collection of software making up a Linux bases OS. This consists of Kernel + number of other software tools.
 
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
`desktop environment` - GUI on top of OS, example `GNOME`, `KDE`, `Xfce` and `Fluxbox`.
`shell` - CLI intepreter that interprets the command input. `bash`,`tcsh`, `zsh`.

