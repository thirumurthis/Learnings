
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
       - either in `boot sector` in traditional BIOS/MBR system.
       - or the EFI partition Extensible Firmware Interface/UEFI (unified EFI) system)
       - Till this stage, the mass storage media is NOT accessed.
       - The information on date, time and most important peripherals are loaded from CMOS values.
  - Different boot loaders, common ones are 
      -`GRUB` (GRand Unified Boot loader)
      - `ISOLINUX` (booting from removable media)
  - Boot loader is repsonsible for loading the kernel image and the inital RAM disk or file system into memory. (filesystem contains critical files and device drivers needed to start the system)

  - For sytem using BIOS/MBR method, 
      - the boot loader resides at the first sector of the hard disk, know as the MBR (master boot record). Size of MBR is 512 bytes.
      - The boot loader examones the partition table and finds a bootable partition.
      - once bootable partition is identified, search for second stage boot loader, GRUB. Loads it into RAM.
      
   - For sytems using EFI/UEFI method,
      - UEFI frimware reads its Boot Manager data to determine which UEFI application is to be launched and from where. (that is, which disk partition EFI partition can be found)
      - the frimware then launches UEFI application.   
 
   - The second stage of boot loader resides under /boot.
   - a splash screen displayed, which allows to choose which OS to boot.
   - After choosing the OS, the boot loader loads the kernel of the selected OS into RAM and pass control to it.
   - The kernels are always compressed, so its will uncompress itself.
   - After this it will check and analyze the hardware and initialize hardware device drivers into the kernel.
 
`initramfs`:
  - The `initramfs` filesystem image contains programs and binary files that perform all actionas needed to mount the root filesystem, providing kernel functionality for the needed filesystem and device drivers for mass storage controllerss with a facility called udev (for user device).
  
  - `udev` is responsible for figuring out which devices are present, identifying device drivers they need to operate and load them.
  - The `mount` program instructs the operating system that a filesystem is ready for use.
      - associates it with a particular point in the overall hierarchy of the filesystem (mount point).
      - If the mount is successful, the initramfs is cleared from RAM and the init program on the rool filesystem `/sbin/init` is executed.
      
   - `init` handles the mounting and pivoting over to the final real root filesystem.
       - if special hardware drivers are needed before the mass storage can be accessed, this must be in the initramfs image.
   - `init` starts a number of text mode login prompts, at end of the boot process. This will be text mode or GUI prompt for user name and password.
      
 ------------------
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

