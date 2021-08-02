
Steps involved in Linux boot process:

- Power On
- BIOS :
   - Performs `POST` - Power on Self Test
      - Search for Bootable Device (HDD, CD, USB, LAN, HBA card) to boot the system.
      - say in this case the linux was installed in HDD (hard disk) mostly the `/dev/sda`
   - BIOS then checks the `MBR (Master boot record)`.
      - MBR is present in first sector of bottable disk. 
      - MBR is represented in 512 byte 
        - First 446 bytes has boot loader
        - Next 64 bytes contains partition information
        - Last 2 is magic byte, acts as error indicator in MBR
         NOTE: The 446 is not the actual boot loader, this has reference to secondary boot loader. This is due limitation of small byte size. This has only the sector information of the main boot loader.
               The main boot loader is GRUB2.
      - GRUB2 (Grand unified bootloader) version 2
          - GRUB2 loads its configuration from the **`/boot/grub2/grub.cfg`**
          - and displays a menu where user can select which kernel to boot.
          - _It loads the vmlinuz kernel image_ (`/boot/vmlinuz-X.Y.Z-V.el8x86_64`)
          - and extract the content of **`Initramfs`** (`initramfs-X.Y.Z-V.el8x86_64.img`)
          NOTE: GRUB loads the kernel into memory and also extract the content of initramfs to memory.
                initramfs is an archive containing all modules and drivers, and this is also loaded to memory.
      - Kernel will initialize the hardware, for this it requries dirivers which is already loaded in memory by initramfs
      - Kernel then executes the `/sbin/init` from intiramfs as process PID 1. 
         NOTE: **ON RHEL 8 /sbin/init is a link to systemd**
               systemd is the first process that starts (at PID 1) and last process to stop when shutdown
      - `systemd` executs `init.target`
         - `systemd` also loads services concurrently.
         - `systemd` instance from the `Initramfs` executes all units for `initrd.target` target. This includes mounting the root file system on disk on to the /sysroot directory (this is temporary).
         - Once the system boots up the kernel root FS switched from intiramfs root `/sysroot` to system rootfs (i.e `/`) and systemd re-executes as system version
      - `systemd` looks for default.target
         - different target like `multi-user.target` or `gui.target`.
         - system reads the file linked by /etc/systemd/system/default.target
         - (for example, `/usr/lib/systemd/system/multi-user.target` to determin the defaut system target (equivalent to run level).
         - The system target file defines the services that systemd starts.

