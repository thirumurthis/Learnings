
#### Linux Boot process Steps:
  - Power ON
  - BIOS
  - Master Boot Record (MBR) [first sector of the Hard disk]
  - Boot Loader (example: GRUB)
  - Kerner (Linux OS)
  - Initial RAM disk (initramfs image)
  - `/sbin/init` (parent process)
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
 ##### Advanced security frameworks:
   - `SELinux` in Red Hat-based systems.
   - `AppArmor` in Ubuntu.
 -------------------
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

 - `boot loader` - boots the operating system, __`GRUB`__ and __`ISOLINUX`__.
 - `service` - a program running as an background process.
 - `filesystem` - `etx3`,`ext4`,`FAT`,`XFS`, `NTFS`, and `btrfs`.
 - `desktop environment` - GUI on top of OS, example `GNOME`, `KDE`, `Xfce` and `Fluxbox`.
 - `shell` - CLI intepreter that interprets the command input. `bash`,`tcsh`, `zsh`.

-------------

 - once the boot loader loads both the Kernel and initial RAM-based file directly to the memory.
 - the kernel initializes and configures the computers memory and all hardware attached to the system (all processes, I/O subsystem, storage device, etc).
 - Kernel runs `/sbin/init`, once all hardware and mounted root filesystem is set up.
    - The `/sbin/init` process is the initial process which then starts other processes to get the system running.
    - The init is responsible for keeping the system running and shutting down cleanly. This manges non-kernel processes; cleans upon completion; etc.
    
    The init process was old conventional one (System V conventions), the newer distribution includes `systemd` and `Upstart` methods.
    
SysVinit was a serial process which didn't allow parallel processing.    
Now since use of containers which require almost instantneous startup times a alternative methods are used.
    
- `Upstart` 
    - Developed by Ubuntu.
    - Adopted in Fedora 9 and in RHEL 6.
- `systemd`
    - Adpoted by Fedora first
    - Adopted by RHEL 7 and SUSE
    - This replaced Upstart in Ubuntu 16.04+.
    
__`systemd`__
   - start up faster than init methods.
   - replaced serialed set of steps with parallelization techiniques.
   - multile service can be initiated simultatneously.
   - NOTE: `/sbin/init` points to `/lib/systemd/systemd`. systemd takes over the init process.
   
  `systemctl` command of `systemd` is used for most basic tasks.

Example: 
```
    //starting and stopping nfs service
    $ sudo systemctl start nfs.service
    $ sudo systemctl stop nfs.service
    
    /// Enable/ disable system service from starting up at system boot
    $ sudo systemctl enable nfs.service
    $ sudo systemctl disable nfs.service
    $ sudo systemctl restart nfs.service
    
    Note: the .service can be ommited.
```

filesystem
   - conventional disk filesystems: ext2,ext3,ext4,XFS,NTFS,Btrfs,JFS
   - Flash storage filesystems: ubifs,JFFS2,YAFFS
   - Speical purpose: procfs,sysfs,tmpfs,squashfs,debugfs

Partition is a physically contiguous section of disk.

Filesystem is a method of storing/finding files on hard disk (usually in a partition). Partition is a container in which filesystem resides.

|Name | windows convnetions | Linux |
|-------|----------|------------|
| partition | Disk | /dev/sda1|
| filesystem type| NTFS | Ext3/4/Bfrts|
| Mounting parameters | DriveLetter | MountPoint|
| Base Folder |  C:\, D:\ | / |

__`File Hierarchy Standard (FHS)`__
  - Linux uses '/' characer to separate paths. (like windows doesn't have drive letter)
  - Multiple dirves and/or partitions are mounted as directories in the single fiesystem.
  - if user name is "user" a USB drive in FEDORA listed as /run/media/user/FEDORA.


```
 /
         /boot - static files of the boot loader  
         /dev  - device files 
         /etc - Host specific system configuration (opt,xml directories)
         /home - user home directories (/home/user1, /home/user2...)
         /lib - essential shared libraries and kernel modules  
         /media - mount point for removalbe media  
         /mnt - mount point for a temporary mounted filesystem   
         /opt - add-on application packages  
         /sbin - system binaries   
         /srv - Data for services  
         /tmp - Temporary files  
         /usr - multi-user utilities  application   (/usr/local -> /usr/local/bin, /usr/local/games,...)
         /root - root user home directory
         /proc - virtual filesystem, process status as text files
         /var - variable files
```

##### Choosing distribution:
Which one to choose Server or desktop version?

 `Server` => RHEL/CentOs, Ubuntu Server, SLES, Debian.
 `Desktop` => Ubuntu, Fedora, Linux Mint, Debian.
 `Embedded` => Yocto, Open Embedded, Android.
 
 Hardware - x86, ARM, PPC.
 
--------------

##### GUI
 - Linux machine use X Windows system, simply called as X.
 - X is a old software, and recently `Wayland` a newer system is superseding it as a default display in Fedora, RHEL 8.
 
`X` 
  - A desktop environment consits of a session manager.
  - The session manager starts and maintains the components of graphical session, wndows manger and utilities.
  
  Even if the display manager is not started by default runlevel, it can be started in different way after logging on to the text mode in console.
   - By running `startx` from the command line. (or display managers `gdm`, `lightdm`, `kdm`, `xdm`)
   - The default display manger for `GNOME` is called `gdm`. (lightdm is used in ubuntu before 18.04)
   - __`gnome-tweak-tool`__ or __`gnome-tweak`__ use __`Alt - F2`__ and type the name to customize the options.
     
------------
#### Networking
   - DNS and Name resolving in Ubuntu, config file to display the host information
      - `$ cat /etc/hosts`
      - `$ cat /etc/resolv.conf`
   - Commands to resolve name,
      - `$ host google.com`
      - `$ dig google.com`
      - `$ nslookup google.com`

##### Network configuration files:
   - The network files are located under `/etc`.
      - `/etc/network` - for Debian family. 
      - `/etc/sysconfig/network` - for Fedora, SUSE family.
   - low level utility __`nmtui`__ and __`nmcli`__ doesn't change much in any of the Linux distro.   
   
###### Network interface:
  - connecting channel between a device and network.
  - Physically, network interface proceed through a network interface card(NIC). It can be implemented as software code.
  - Information about particular or all network interface can be displayed by `ip` and `ifconfig` utlities.  (`/sbin/ifconfig`). `ip` (`/sbin/ip`) command is newer one.
  - Some distro, doesn't install net-tools package, in that case install them manually.
  
```
# usage to display ip address
$ /sbin/ip addr show

# usage to display route information
$ /sbin/ip route show
```

##### route
  - data moves from source to destination by passing thorough series of routers and networks.
  - The ip routing protocols enable routers to build up a forwarding table that correlates final destination with the next hop address.

```
# newer with the ip aommand.
$ ip route

$ route -n 
```

| Description | command |
|----|---|
| Display current routing table | `$ route -n` or `$ ip route` |
| Add static route |  `$ route add -net address` or `$ ip route add` |
| delete static route | `$ route del -net address` or `$ ip route del` |

`traceroute` - utility used to inspect the route which the data packet takes to reach teh desitnation host. Used to troubleshoot network delays.
` $ traceroute <address>`

##### More network tools/ utilities:

| Tools/utilities | description |
|------------|-------------|
| ethtool | queries network interface and can also set varous parameter like speed|
|netstat | Display all active connections and routing table info.|
|nmap | scans open ports on a network, used for security analysis|
|tcpdump | Dumps network traffoc for analysis (wireshark) |
|iptraf | monitors network traffic in text mode|
| mtr | combines funtionality of ping and traceroute to continuously update display|
|dig| Test DNS working status. Replacement for host and nslookup.|

##### Browser:
 - Graphical
    - Firefox, Chrome, Konqueror
 - Non-Graphical
     - Lynx (text-based web browser), 
     - ELinks (based on lynx, displays tables and frames), 
     - w3m (text-based web browser)
   
__`wget`__ : To download files and information from CLI.
  - Large file downloads
  - Recursive downloads (web page referring another web page etc)
  - password required downloads
```
# example
$ wget <url>
```

__`curl`__: Obtains information about a URL, such as the source code being used.
```
$ curl <url>

$ curl -o page.html https://<somesite>.com
# output stored on page.html
```

__`ssh`__:
 - `ssh system-name`
 - `ssh -l user1 systemname` - to login as some user
 - `ssh system-name command` - to run a command on remote machine.

__`scp`__ (secure copy):
  - `scp <localfile> <user@remotesystem>:/home/username/`
  
__`mtr`__ :
  - enhanced version of traceroute
  ```
  $ sudo mtr --report-cycles 3 8.8.8.8
  -- 8.8.8.8 google dns server
  ```

##### syntax and special character used in shell scripts

| char | descrption |
|-------|-----------|
| # | used to add comment, except when used `\#` or `#!` |
| \ | used ad the end of the line to indicate continuation on the next line |
| ; | used to interpret, what follows as a new commend (delimiter) |
| $ | Indicates what follows is an environment variable |
| > | redirect ouput |
| >> | append output |
| < | redirect input |
| | | pipe the result into next command| 


Splitting long command
```
$ echo hello \
> this \
> is test 

hello this is test
```

|command | description |
| ----- | ----- |
| `$ echo one; echo two; echo three` | executed one after other, even if the preciding command fails.|
| `$ echo one && test 1 == 2 && echo three` | executes the following command only when the preceding command is successful |
| `$ echo one && test 1 == 3 && echo three` | same as above, if want to abort subsequent command when an earlier one fails.?|
| `$ test 2==1 || echo 2 || echo 3` | Procceds until something succeeds and then stop executing any furtehr steps |


### How to display the list of bash shell commands? Answer is `$ help`.

##### shell parameters and arguments 

`$ myshell.sh 1 2 3` 

| parameter | description |
| --- | ---- |
| $0 | script name |
| $1 | first parameter|
| $2| second parameter |
| $* | All parameter |
|$# | number of arguments |


### Command Substitution:

To substitute the result of a command as portion of another command.
 - `$( )` 
 - (\`\`) - this is deprecatd in new scripts.
 
```
$ ls -lrt /lib/modules/$(uname -r)/
```

### How to make the variable avialable to the child processes (sub shells)?
```
 export VAR=value
 or 
 VAR=value; export VAR;
 
 ## if the child process changes the value of this variable, the parent process won't see that change.
```

### function
```
function_name () {
 commands.
 }
```

#### How to pass arguments to function?
The first argument will be $1, second $2, ....

##### `if-else`

```
if TEST-CONDITION; then COMMANDS; fi

or 

if condition
then
   statement
else 
   statement
fi

example:

if [ -f "$1" ]; then
  echo file "$1" exits
else
  echo file not exists
fi

# [] is to delineate the test condition
```

__`NOTE:`___
  - the use of `[[ ]]` in if test condition is not an error. This avoids problem when referring to an empty environment variable without surrounding it in double quotes.
  
  | condition | descrption |
  |----|-----------|
  | -e file |check if file exists|
  |-d file | check if file is a directory |
  | -f file | check if the file is a regular file (not a symbolic link, device node, directory, etc. |
  | -s file | checks if the file is of `non-zero size` |
  | -g file | checks if the file has `sgid` set |
  | -u file | checks if the file has `suid` set |
  | -r file | checks if the file is readable |
  | -w file | checks if the file is writable |
  | -x file | checks if the file is executable|
  
##### Boolean Expression:
   - && AND
   - || OR
   - ! NOT
   
  - `[ -e <filename> ]` - check if file exists
  - `[ $num1 -gt $num2 ]` - num1 greater than num2
  
##### How to compare Strings?
   - Compare string using the operator __`==`__.
   
   ```
   if [ str1 == str2 ]; then 
      echo mathced
   fi 
   // sample code
   VAR=test
   if [ "$VAR" == test ]; then 
     echo matched
    fi
   ```
 
 | operator | description|
 |---------|----------|
 | -eq | equalt to|
 | -ne | not equal to |
 | -gt | greater than |
 | -lt | less than |
 | -ge | greater than or equal to |
 | -le | less than or equal to|
 
 ### Arthiemtic Expressions:
  - Using `expr` utilitly (this is standard but deprecated)
     - ```expr 8 + 8 ``` 
  - Using $(( .... )) syntax.
     - ``` $ echo $((x+1)) ```
  - Using the built in shell command __`let`__. 
     - ```$ let x=( 1+3 ); echo $x ; ```
 
 Note: The `expr` is replaced with `$((...))` in modern scripts.
 
### String manipulation:

| operator | description |
|---|-------|
| `[[ str1 > str2 ]]` | compares the sorting order of str1 and str2 |
| `[[ str1 == str2 ]]` | compares the characters in str1 with str2 |
| `length=${#str1}` | stores the lenght of str1 in the variable length |


#### How to compare only part of string? 
  - To extract the first `n` chars of a string use ``` ${string:0:n}``` here, 0 is the offset in the string (from which char to start with).
  - To extract charactes in a string after a dot (.), ```${string#*.}```
  
```
VAR=Sample.Text
echo ${VAR:0:4}; # Samp
echo ${VAR#*.}; # Text , string after the .
```

### `case` statement:

```
case expression in 
   pattern1) execute commands;;
   pattern2) execute commands;;
   *)  execute default commands or nothing ;;
esac

# example:

echo "input (y/n)"
read response

case "$response" in
  "y")  echo user selected y
  "n")  echo user selected n
  *) echo use not selected y/n
esac
exit 0
```

### Looping :
  - `for` 
     ```
       for variable-name in list 
       do
           command;
       done;
       
       # example
       val=0
       for i in 1 2 3 4 5
       do 
          val=$(( $val + $i ))
       done
      ```
       
  - `while`
      ```
      while condition is true 
      do 
         commands;
      done;
      // codition is often enclosed with []
      
      i=0;
      while [ $i < 10 ]
      do
        echo $i
        i=$(($i+1))
      done;
      exit 0;
      ```
  - `until`
     ```
     until condition is true
     do
        commands;
     done;
     
     # example 
     
     i=0;
     until [ $i < 10 ]
     do
       echo $i
       i=$(($i+1));
     done;
     ```

### How to debug a shell script?
  - by executing the shell using `-x` debug mode
  - bracketing parts of the script with `set -x` and `set +x`.
     - `set -x` => turns the debugging on
     - `set +x` => turns the debugging off
     
### How to redirect errors to File and Screen ?

  | file stream | description | file descriptor |
  | -----|-------|--------|
  |stdin| Standard input, by default the keyboard/terminal for programs run from the command line | 0 |
  | stdout | Standard output, by default the screen for program run form the comand line | 1| 
  | stderr | standard error, where output error messages are shown | 2|
  
  
  ```
  # using redirection, we can save teh stdot and stderr output streams to one file or two separate files for later analysis after a program executed.
  
  #!/bin/sh
  val=0
  for i in 1 2 3 4
  do 
    val=(($val+$i)) # intentionally not added $
  done
  echo $val
  ls some!@file
  
  $ ./script1.sh 2> error.log
  $ cat error.log
  ```
  
  __`mktemp`__ utility (create temporary file and directory to store data for a short time.
  
  The data will be stored in temp file and only available to that specific program, and can be delted.
  
  ```TEMP=$(mktemp /tmp/tempfile.XXXXXX)``` -> create a temporary file.
  ```TEMPDIR=$(mktemp -d /tmp/tempdir.XXXXX)``` -> create a temporary directory.
  
  
 ### Discarding output with /dev/null:
   - commands like `find` will produce voluminous amounts of output along with message if not able to access directory due to permission issue.
   - to avoid this we can redirect the large output to a special file (device node) called `/dev/null`.
   
   ```
    ls -lR /tmp > dev/null # entire std output stream is ignored, errors will appear in console.
    
    $ ls -lR /tmp >& /dev/null  # both stdout and stderr will be dumped to /dev/null
   ```
   
### How to generate random number in shell script? using __`$RANDOM`__.
   ```
   $ echo $RANDOM 
   # linux kernel build in random number generator or by the openSSL library function.
   ```
  -  Some servers have hardware random number generators that take as input different types of noise signals, such as thermal noise and photoelectric effect. 
  - A transducer converts this noise into an electric signal, which is again converted into a digital number by an A-D converter. This number is considered random. However, most common computers do not contain such specialized hardware and, instead, rely on events created during booting to create the raw data needed.
 - Regardless of which of these two sources is used, the system maintains a so-called entropy pool of these digital numbers/random bits. Random numbers are created from this entropy pool.
  - The Linux kernel offers the `/dev/random` and `/dev/urandom` device nodes, which draw on the entropy pool to provide random numbers which are drawn from the estimated number of bits of noise in the entropy pool.
   - `/dev/random` is used where very high quality randomness is required, such as one-time pad or key generation, but it is relatively slow to provide values. /dev/urandom is faster and suitable (good enough) for most cryptographic purposes.
   - Furthermore, when the entropy pool is empty, /dev/random is blocked and does not generate any number until additional environmental noise (network traffic, mouse movement, etc.) is gathered, whereas /dev/urandom reuses the internal pool to produce more pseudo-random bits.
   
#### `sed` Stream editor tool

 | command | description |
 |-----|-----------|
 | `sed -e command <filename>` | performs the command operation on the file and display output to stdout |
 | `sed -f sedcommandscript <file>` | specify the sed commmand in sedcommandscript file , the output will be displayed on the screen |
  
  `-e` option is used for multiple editing command.
  
  ```
  $ sed -e s/hello/HELLO/g /home/user1/sample.txt
  
  $ cat sample.txt
  hello help hello
  ```

##### Replacing option

| command | description |
|---|----|
| `sed s/pattern/replace_string/ filename` | substitute the first string occureance in every line|
| `sed s/pattern/replace_string/g filename` | substitute all string occurrence in every line of file (g -global) |
| `sed 1,3s/pattern/replace_string/g filename` | substitue all string occurrences in a range of lines |
| `sed -i s/pattern/replace_string/g filename` | save changes for string substitution in `same file`|

use `-i` since it will replace the existing file, which is not reversible.

Example:
```
$ sed -e 's/one/1/' -e 's/two/2/' -e 's/three/3/' filename
```

#### `awk` utility
 - Authors Alfred `A`ho, Peter `W`einberger, Brain `K`ernighan
 - powerful interpreted language.
 
| command | description |
|------|--------|
| `awk 'command' file | specifying command directly at command line |
| `awk -f scriptfle file` | specfying a file which contains the script to be executed |

Example :
```
$ awk '{print $0}' /home/user1/sample.txt  # prints entire file

$ awk -F: '{print $1}' /home/user1/sample.txt # prints first field of every like when the data looks like one:1 (1 will be output)

$ awk -F: '{print $1 $3}' /home/user1/sample.txt # prints 1st and 3rd field of every line
```

##### File Manipulation utilities
 - `sort`:
  sort is used to rearrange lines of a text file
  
  | syntax | description |
  |----|----|
  |`sort <filename>` | sort the line according to the char at start of line |
  |`cat file1 file2 \| sort` | combine 2 files and sort the lines |
  |`sort -r <filename>` | sort lines in reverse order |
  |`sort -k 3 <filename>` | sort the lines by the 3rd field on each line instead of starting char |
  
  `-u` option is used for `unique` values after sorting the records, similar to running `uniq` command.
  
 - `uniq`
   - uniq removes duplicate consecutive lines in a text file.
    
    ```
      $ sort file1 file2 | uniq > temp.txt
      
      $ sort -u file1 file2 > temp.txt 
      // Both the above command performs the same operation
      
      $ uniq -c filename  ## Counts the number of duplicate char in a file.
    ```
    
 - `paste`
   - paste command can be used to create a single file combining multiple columns from different files.
   - `-d` option is for specifing delimiters instead of tab.
   - `-s` option cause past to append the data in series rather than in parallel (horizontal rathern than vertical fashion)
   
  ```
  $ cat file1
  12345
  67890
  11111
  
  $ cat file2
  Tom
  Bob
  Ram
  
  $ paste file1 file2
  12345    Tom
  67890    Bob
  11111    Ram
  
  $ paste -d ':' file1 file2
  ```
   
  - `join` 
    - If we have two files with some similar columns, example phone number in two file one with first name and the other with last name.
    - the data can be combined wihout repeating the data using `join` and `paste`
    
  ```
  
  $ cat file1
  1111 file1one 
  2222 file1two
  3333 file1three
  
  $ cat file2
  1111 file2one
  2222 file2two
  3333 file3three
  
  $ join file1 file2
  1111 file1one file2one
  2222 file1two file2two
  3333 file1three file2three
  
  // if any of the file has additional rows the join will ignore that.
  ```
  
  - `split`
    - split is used to break up or split file into equal sized segments for easier viewing and manipulation.
    - we can split a file into 1000 line segments.
    - the original file remains unchanged, a set of files with same name and prefix is created.
  
  ```
  $ split infile <prefix>
  ```
  
 ##### Regular expression:
 
 | Search pattern | description |
 | ------ | --------- |
 | . | Match any single char|
 | a\|z | Match a or z |
 | $ | Match end of string (meta character)|
 | ^ | Match start of the string |
 | * | Match preceding item, 0 or more times (meta character) |


```
# aunt made nice coffee
a..  => matches aun
m.|n.  => matches both ma and ni
..$ => matches ee
n.* => matches nice coffee
n.*e => matches nice
aunt.* => matches whole sentence
```

 - `grep`
    - is a text searching tool
    
  | command | description |
  |-------|---------|
  | `grep [pattern] <filename>` | search for pattern in a file print matching lines|
  | `grep -v [pattern] <filename>` | print all liines does NOT match the pattern|
  | `grep [0-9] <filename>` | print the lines that contain number 0 to 9 |
  | `grep -C 3 [pattern] <filename>` | print context of lines (specified number of lines above and below the pattern) for matching pattern. |
  
  
  - `strings`
     - used to extract all printable characters strings found in the file or files passed as argument.
     - it is useful in locating human-readable content embedded in binary file.
     - for text file can use grep.
     
     ```
     $ strings book.csv | grep match_string
     # csv are commana seperated file
     ```
 
 - `tr` utility
    - tr used to translate specified characters into other characters or delete them.
    - syntax: ` tr [options] set1 [set2]`
    - it requires at least 1 argument and max 2.
    
  Example:
  ```
  tr abcde ABCDE => convert lower case to upper case for abcde
  tr '{}' '()' < infile > outfile => Translated braces into parenthesis 
  echo "help is on the way" | tr [:space:] '\t' => translate white space to tab
  echo "This   is   working" | tr -s [:space:] => squeeze repetition of chars 
  echo "this is delete" | tr -d 't' => delete specified character using -d 
  echo "my emp id is 1235" | tr -cd [:digit:] => complement the sets using -c and prints only number
  tr -cd [:print:] < infile.txt => remove all non-prinable chars from a file
  tr -s '\n' ' ' < infile.txt => join all the lines in a file into single line
  ```
   
  - `tee`
     - tee takes the output from any command and while sending it to standard ouput, it also saves it to a file.
     
     ```
     $ ls -lrt | tee outputfile.txt
     ```
  
  
  - `wc`
     - word count
     - `-l` option displays number of lines
     - `-c` option displays number of bytes
     - `-w` option displays number of words
   
   ```
   $ wc -l filename
   $ wc *txt
   ```
   
   - `cut`
     - cut is used for manipulating column based files and extract specified columns. default is tab char.
     
   ```
     $ ls -l | cut -d" " -f3
     -d => delemiter
     -fN =>field number
  ```
   
#### Environment variables:
  - holds specific values which may be utlized by the command shell, like bash 
  - `set`,`env` or `export` - ways ot view the values currently set in environment.
  
  ```
  $ echp $SHELL => value of specific variable
  
  # exporting new variable
  $ export VARIABLE=value (or VARIABLE=value; export VARIABLE)
  
  # to make the variable availabe when the shell opens (that is permanently)
  # 1. edit ~/.bashrc file add the line 
  #    export VARIABLE=value
  # 2. issue 
  #    $ source ~/.bashrc   #or 
  #    $ .~/.bashrc     # or 
  #    $ bash  # (opens a new bash shell)
  ```
  
 - `HOME` 
   - is an environment variable that represents the home or login.
   - ` $ echo $HOME` shows the value of the HOME environment. (/home/user1)
 
 - `PATH` 
    - is an ordered list of directories, which is scanned when the command is given to find the appropriate program.
    - each path is separated by colons (:)
    - `:path1:path2` or `path1::path2`
  
  example:
 ```
 $  export PATH=$HOME/bin:$PATH
 $ echo $PATH
 ```
  
  - `SHELL` 
     - this points ot the default shell and contains the path name to the shell
     - ` $ echo $SHELL`
  
  - `PS1` variable
     - PS1 is primary prompt variable which controls what command line promt looks like
     - `\u` -username
     - `\h` - hostname 
     - `\w` - current working directory
     - `\!` - History number of this command
     - `\d` - date
   
  Example
  ```
 $ echo $PS1
 
 $ export PS1='\u@\h:\w$ '
  user1@hostname:~$ #
  
  # restore the PS1
  $ OLD_PS1=$PS1
   # experiment with the ps1 and then if needed replace it.
  $ PS1=$OLD_PS1
  ```
  
  Few more env variables,
    - `HISTFILE` (location of history file),
    -`HISTFILESIZE` (maximium number if lines in history file default 500),
    - `HISTSIZE` (max number of command in history file), `HISTCONTROL` (how commands are stored), 
    - `HISTIGNORE` (which command lines can be unsaved).
  
  ### How to execute the previous command? using `!!` (pronunced as bang-bang)
  `CTRL - R ` used for historic search for the history commands
  
  ##### Executing previous commands
  ```
  ! - start a history substituion
  !$ - refer to the last argument in a line
  !n - refer to the nth command line in history
  !string - refer to most recent command starting with string.
  ```
  Example:
  ```
  $ sleep 20
  $ !sl
  sleep 20
  ```
  
### Keyboard  Shortcut commands on shell:
  
  |Shortcut|	Task|
|-------|-----|
|CTRL-L	|Clears the screen|
|CTRL-D	|Exits the current shell|
|CTRL-Z	|Puts the current process into suspended background|
|CTRL-C	|Kills the current process|
|CTRL-H	|Works the same as backspace|
|CTRL-A	|Goes to the beginning of the line|
|CTRL-W	|Deletes the word before the cursor|
|CTRL-U	|Deletes from beginning of line to cursor position|
|CTRL-E	|Goes to the end of the line|
|Tab	|Auto-completes files, directories, and binaries|
  
  
### File permission

```
rwx : rwx : rwx
 u:    g:    o
 
 u - user/owner
 g - group
 o - others
 
 $ chmod uo+x,g-w file  
 # above command provides user and other to execute permission
 # group will lose write permission
 
```

### Linux Graphical use interface and finding tools:

 - press `ALT + F2`
 - enter command:
   - GNOME: `gnome-help` or `yelp`
   - KDE: `khelpcenter`
 gnome-help works of ubuntu.
 
 #### package documentation can be found at `/usr/share/doc`.
 
 #### online resources
  - [Centos](https://wiki.centos.org/Documentation)
  - [Ubuntu](https://help.ubuntu.com/)
 
 #### opening up the graphical help from command line `$ yelp man:cat`
 
 #### How to open terminal using the Graphical command interface?
 ```
 Press ALT + F2
 Enter: gnome-terminal or konsole
 ```
 
 ## How to setup `sudo` if it is not enabled?
  
   - 1. As superuser (root), since the sudo command is not available, we use `su`, At commandline prompt type `su` and hit enter.
   - 2. will be prompted with the root password
   - 3. Create a `/etc/sudoers.d/` directory to create a configuration file to enable user accont to use sudo.
      - if the user name is `user1` after performing the above steps, create the file and content
        ```
        # echo "user1 ALL=(ALL) ALL" > /etc/sudoers.d/user1
        ```
   - 4. Change permission on this file `$ chmod 440 /etc/sudoers.d/user1`.
       
 
 ### what is `virtual terminal` (VT) in linux?
  - VT are console session that use the entire display and keyboard outside of a graphical environment.
  - There can be multiple active terminal but only one is visible at a time.
  
  - This would help when the graphical desktop is running into issues.
  - To switch between VTs screen press
     ` CTRL -ALT- function key`
   - `CTRL+ALT+F2` ,` CTRL+ALT+F3`, ` CTRL+ALT+F4` ...
   - After switching the one VT (you should see the tty1, etc) just use `ALT+ F4` to switch to another VT (no need for CTRL).

  
  ### how to turn off the Graphical Desktop?
  - For newer systemd-based distribution, the display manager runs as a service.
  - To stop GUI desktop, we can use `systemctl` utility.
  - Most distribution will also workg with `telinit` command.
  ```
  # to stop the display manager
  $ sudo systemctl stop gdm  (or) $ sudo telinit 3
  
  # to restart the display manager
  $ sudo systemctl start gdm  (or) $ sudo teminit 5
  
  # for ubuntu 18.04 uses lightdm instead of gdm
  ```
   
 ### how to shut down machine using command line?
   - `halt` and `poweroff` command issues `shutdown -h` to halt system.
   - `reboot` issues `shutdown -r` to reboot instead of shutting down.
   - To perform this operation need to be as root user.
 
 When administrating multi-user system, we can notify and shutdown.
 ```
 $  sudo shutdown -h 10:00 "Shutting down notification" 
 ```
   
 #### command to locate applications, `$ which diff` and `$ whereis diff`. 
  
 #### how to change to the previous directory using `-`? `$ cd -` 
  
### `Absolute` and `Relative` path:
   - multiple slashes ////usr//bin is valid, but system sees it as /usr/bin.
   - `absolute pathname` : `$ cd /usr/bin`
   - `relative pathname` : `$ cd ../../usr/bin`
   - `.` - present directory
   - `..` - parent directory
   - `~` - home directory
 
- `tree` utility to see the directory in hierarical structure.

-__`Hard Links`__:
  - a file file1 already exists, a hard link can be created using 
  ```
   $ ln file1 filelink2
   # the above command creates another file reference,
   
   $ ln -il file1 filelink2 
   # -i => displays the inode number, for those two files it will be same.
  ```
    Note: editing the file name in most modern system shouldn't cause issue, but in some case it might create two objects or files.
    
 __`Soft Links`__:
   - soft links are created with `-s` option in `ln`.
   ```
    $ ln -s file1 filelink1
    $ ln -li file1 filelink1
      filelink1 -> file1
    $ cat filelink1  # outputs the content of file1  
    # the filelink1 now not looks like a regular file. it points to file1
   ```
   - Symbolic links takes no extra space unles the name is very long.
   - easily modified to point to different place.
   - it is like creating a shortcut.
   - unlike `hard link`, `soft link` can point to objects even on different file systems, partition and/or disk and other media which may or may not be currently available or even exist.


### Redirection

|name | symbolic name | value | example|
|--|--|--|--|
|standard input| stdin |0 | keyboard|
|standard output| stdout | 1| terminal |
|standard error | stderr| 2| log file|

Redirecting error to error file:
```
$ command_to_do_something 2> error_file
```

Redirecting the output to a file
```
$ command_to_do_something 1> output_file
```
##### short-hand notation to send anything written to file descriptor 2 (stderr) to same place as file descriptor 1 (stdout) : 2>&1
```
 $ command_do_something > output_file 2>&1
 
 $ command_do_something >& output_file # this is same as the above 
```

 - __`Searching for files`__:
   - `locate`
      - utility program search takine advantage of previously constructed database of files and directories on the system.
      - ` $ locate zip | grep bin ` - list all files and directories with both zip and bin in their name.
      - `locate` utlizes a database created by a related utility, __`updatedb`__.
      - `updatedb` can be executed any time but as a root user.
      
   - `find`
     - `$ find /usr -name gcc` => search for files and direcories named gcc
     - `$ find /usr -type d -name gcc` => serach for directories named gcc
     - `$ find /usr -type f -name fcc` => search for regular file named gcc
     - Advanced option
        - `$ find -name "*.swp" -exec rm {} ';' => {} is place holder
        - end the above command with either `';'` or `'\;'`.
        - use `-ok` option instead of `-exec`, in `-ok` it will prompt for user action before executing.
        
        - find file based on time
           - `$ find / -ctime 3` => -ctime is when the inode metadata last changed. (file ownership)
           - `-atime` => last accessed/last read
           - `-mtime` => modified/last written
           - n => number of days
           - +n => greater than that number
           - -n => less than that number.
           - -cmin, -amin and -mmin
         - `$ find / -size 0` 
         - `$ find / -size +10M -exec command {} ';'`
##### Wild cards and matching file name:

| wildcard | description|
|--|--|
| ?  | matches any single char|
| * | matches any string of char|
|[set]| matches any char in the set, eg [abd]|
|[!set] | matches any char NOT in the set|

### User creation
  - `useradd` and `userdel` command line tools are used for creating and removing accounts.
  - Each user is identified by a unique integer (user id or UID).
  - a separate database associaes a username with each UID.
  - upon creation new user information is added to the user database.
  - the user's home directory must be created and populated with some essential files.
  
  - structure of `/etc/passwd` file

|field Name | Details | description |
|--------| -----------| -----------|
|username | user login name| should be between 1 - 32 character long|
|password| user password (char x if password is stored in `/etc/shadow` file in encrypted format| Not shown in Linux when it being typed.|
|user id (UID) | Every user should have a user id| UID 0 - reserved for root; UID 1-99 - reserved for predefined accounts; UID 100-999 - reserved for system accounts and groups; UID 1000+ - reserved for normal users;|
|Group Id (GID) | primary group ID stored in `/etc/group` file| -|
|User Info | Optional field allows insertion of extra info about user| name of user |
| Home directory | Absolute path of user home directory | /home/user1|
|shell | Absolute path of default shell | /bin/bash|

##### Types of accounts
  - Linux distingushes between several account types:
    - root
    - system
    - normal
    - network
  -__`last`__ utility shows the last time each user logged into the system.  
    - This command also provides inactive user information.
  
  `root` - is the most privileged account, when signed in as root shell prompt displays `#` (if using bash and not have customized the PS1)
  
  - SUID (Set owner User ID upon exection) special kind of file permissing given to a file. use of SUID provides temporary permissions to a user to run a program with the permissions of the file owner(root) instead of permission held by the user.
  
  Below are some operation which doesn't require root access
   - running a network client
   - using devices like printer
   - operation on files that the user has permission to access
   - Running SUID-root applicaition (executing programs such as passwd).
   
 #### What is `sudo` and `su`?
   - sudo or su temporarily gran root access to a normal user.
   
 | su | sudo|
 |--|--|
 | when elevating privilege, need to enter the root password.| when elevating privilege, you need to enter the user's password not the root password.|
 |Once a user elevates to the root account using `su`, the user can do anything that the root user can do. | offers more feature and is considered more secure and more configurable. Exactly what the user is allowed to do can be precisely controlled and limited.(user always give their pssword to do further operation or can aviod doing configurable time interval.|
 |limited logging feature | detailed logging feature |
 
 
 `sudo` has the ability to keep track of unsuccessful attemps at gaining root access.
 
 - users authorization for using sudo is based on configuraton stored in the `/etc/sudoers` file and in `/etc/sudoers.d` directory.
 - `/var/log/secure` file contains the log information upon `sudo bash` check the log message.
 
 ##### Working of sudoers file:
   - when sudo is invoked, the program will look at `/etc/sudoers` and the file in `/etc/sudoers.d` to determine if the user has right to use sudo.
   - The basic structure of the entried in these files are 
   ```
   who where = (as_whom) what
   ```
   
   - New Linux distribution allows to use file with the same username under /etc/sudoers.d
   
   #### Where to check the sudo logs?
    - `/var/log/auth.log` in case of Debian distrubution for any failures.
    - `var/log/mesages` or `/var/log/secure` in other systems.
    
  ### Process isolation:
    - Process are naturally isolated and due to this Linux is considered to be more secure.
    - One process cannot access resources of another process, even if the process if running with the same user privileges.
    - Linux thus makes it difficult for viruses and security exploits to access and attach random resources on a system.
    
  - Below feature or security mechanisms that limit risk:
    - control groups (cgroups)
       - Allow system admin to group processes and associate fine resources to each group.
    - containers
        - Makes possible to run multiple isolated linux containers on single system by relying on cgroups.
    - Virtualization
        - Hareware is emilated in such a way that not only process can be isolated, but entire systems isolated and execute on one physical host.


##### How are hardwares represented in Linux?
  - `/dev/sd*`

#### How to use password algorithm?
   - The SHA-512 algorithm is widely used for security application and protocols.
   - This also includes, TLS, SSL, PHP, SSH, S/MIME and IPSec.
   - SHA-512 is most tested hashing algorithm.
   
  - To test the SHA-512 encoding try below
  ```
  echo -n user1 | sha512sum
  ```
  
  Password practices:
    - configure password expirey information for users.
    - PAM (Pluggable Authentication Modules) can be configured to automatically verify that password created or modified using the passwd utility is suffciently strong. (This is implemented using a library called `pam_cracklib.so`, which can also be replaced by `pam_passwdqc.so`.
    - Installing password cracking program, jhon the ripper, to secure the password file and detect weak password entries.
  
  Command to check the exipriation of the password __`chage`__
  ```
  $ chage --list <user-name>
  ```

### Securing the boot loader ?
  - providing password prompt during boot process is not sufficient to protect the boot loader proces.
  - There is a possbility to use alternate boot media such as optical disks or pen drives.
  
  - For older GRUB 1 boot method, it is relatively easy to set a password for grub.
  - For GRUB 2 version, this is little complicated.( more flexible and take advantage of user-specific passwords.
  - Never edit `grub.cfg` directly.
  - instead modify the configuration files in `/etc/grub.d` and `/etc/defaults/grub`.
  - after modification run, `$ update-grub` or `$ grub2-mkconfig` and save new configuration file.
