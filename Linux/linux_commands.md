
`$ grep -ril /path/name` - to list the files

`$ man -s 5 passwd` - uses the man category to list the manual page of password

`$ tar -xzvf <name-of-tar-file.tar.gz>` - extract gun zip file compressed file command

`$ tar -xjvf <name-of-tar-file.tar.bz2>` - extract bun zip file compressed file command

`$ tar -czvf <name-of-tar-file.tar.gz>` - create tar bun zip format compressed file

`$ tar -uvf name-of-tar-file.tar /path/to/files` - will only update the modified files within the tar, note the tar archive is not compressed.

`$ ln hardlink destinationlink` - create links 

`$ stat Hardlink` - gives the status of the files, to verify the access of files with security context.

`$ nslookup bing.com` - to query the DNS server

`$ tree / -L 1` - tree path and it level 1 

`$ df ` - used to display how many mount points or disks.

`$ df -h ` - display the mount size in human readable format.

`$ df -h -x tmpfs` - -x removes or filters the specific files system from the results being displayed.

`$ df -hT -x tmpfs` - -T used to see the file system type xfs,ext4, 

`$ du -sh file-name/directory-name` - to get the file size of folder size 

`$ who ` - who all are logged in

`$ w` - who all are logged in with more info

`$ last` - to display the actual reboot and loging log out informations

`$ yum repolist` - list the repos from where yim will be able to update the package. 

`$ subscription manager repos --enable=<repostory>` - the repository is on of the repo listed using `subscription manager repos --list`.
Also, to update the repo, edit `/etc/yum.repos.d/redhat.repo` the enable to 1. 

`$ yum update -y` - the existing package that needs update will be updated.

`$ yum makecache` - which will cache the repostiory in local, won't reach out to online for yum repo

`$ grep -l demo` - file name with demo will be greped

`$ grep -l test demo*` - file name with test, demo* will be displayed

`$ echo $GREP_COLOR` - displays the choose color for GREP_COLOR if already set.
`$ export GREP_COLOR='1;32;42'` - sets the select color of the grep command when searching for text.

`$ grep "^Startstring" filename` - search the string that are starting with the Startstring in the filename
`$ grep "endString$" filename` - search the string that are ending with endString in the filename

----------
Repository in Linux 8, the RHEL isntallaton media ships with component `Appstream` and `BaseOS` repository.
 - Appstream used to install the application.
 - BaseOs is used to install the OS packages.

How to setup the Appstream and BaseOs:
  - if we had mounted the ISO file using Vmware or Virtualbox, this would be under the CD/DVD
  - copy the Appstream and BaseOs to /rpms/ (recursively)
update the `/etc/yum.repos.d/` create a file Appstream.repo

```
[AppStream]
name=app stream repo
baseurl=file:///rpms/Appstream/
enable=1
gpgcheck=0 # if this is 1, then specify the path name
```
- Create similar file for BaseOs.

`$ yum clean all` - to clean the yum cache

`$ yum repolist` - now this will list the repo list. both appstream and baseos repo.

`$ rpm --import RPM-GPG-KEY-redhat-release` this will set the security so don't prompt any warning message not licensed.
------------

`$ getenforcing` - get the SELinux mode currently set.
`$ setenforcing 0` - set the SELinux to permissive mode. {this makes the systemctl to refer the custom location other than /usr/local/bin
`$ setenforcing 1` - set the SELinux to enforcing mode.

-------
`$ sed 's/<search-string>/<string-to-replace>/' <file-name-to-search>` - search and replace the string, in console.
`$ sed 's#<search-string>#<string-to-replace>#' <file-name-to-search>` - using # instead of /

`$ sed '2,$s/<search-string>/<string-to-replace>/' <file-name-to-search>` - to search from the second line onwards. 2 - in the first address is the line from which we wanted to make the substitution onwards. $ - in the second address is the end of the file 

General format: ` $ sed [address, [address] ] <function> [arguuments ]`

`$ sed '/<string-to-start-with>/s/<string-to-search>/<string-to-replace>/' <file-to-search`  

Example : `$ sed '/john/s/doe/known/' text.txt` ( string john doe, will be replaced as john known.)

`$ sed 's/[0-9][0-9]*/2&/' temp.txt` - To add leading 2 to the string matching the pattern.

Example: above command will output

input: "string 00190 hello test"  
output: "string 200190 hello test"

`$ sed 's/\([0-9]\)\([0-9]\)\([0-9]\)/\3\2\1/' text01.txt` - wrapping part of the search string in `\(` and `\)` cases them to be remembered.

For example: reverse the string:
inputs: 
  "string 123 test1"
  "string 200 test2"
ouputs:
  "string 321 test1"
  "string 002 test2"

The strings are referred to by the number in the order where matched, so \1 refers to the number 1, \2 refers to 2 and \3 refers to 3. In the replacement string the sequence \1, \2 and \3 is replaced by the third string, the second string and the first string respectively.

Note: At least __nine__ sub-strings of a match can be remebered and reused.

------------

`$ time ls` - prints time taken to execute the command

`$ watch ls -lrht` - monitors the ls command for every 2 seconds 

---------

`$ scp -r folder <username>@<remote-server-host>:<destination-path>` - remote secure copy. -r recursively copies all files in a folder

`$ scp -p file-to-copy <username>@<remote-server-host>:<destination-path>` - remove secure copy to preserve the metadata of files like created time, etc.

`$ scp -C file-to-copy <username>@<remote-server-host>:<destination-path>` - compress and to copy data use -C 

`$ scp -l 500 file-to-copy <username>@<remote-server-host>:<destination-path>` - copy files with low rate, useful when bandwidth is very low, here the copy data at 500 kbps bytes rate.

----------

`$ ps -fU <user-name>` - list the process specific to the user

`$ ps -aux --sort=-pcpu,+pmem` - to sort the process with high memory Cpu and memory utilization. + and - used to sort that field.

`$ ps -eo pid,ppid,cmd,i,%mem.%cpu | grep <some-process-name>` - ni is the nice value the priority value assigned to the process.

`$ nice -10 sleep 900 &` - set the nice value for the sleep command in this case.
`$ renice -10 -p <pid> ` - this will be used to change the nice value. This will be helpful for running any program.

------------

`$ mount <remotehost>:<dirpath> <localpath>` - mounting path

`$ df -h ` - list the mounted disk along with tmpfs (temp filesystem)

`$ findmnt -S /dev/sda1` - displays the mount details

`$ lsblk /dev/sda1` - displays the mount details

`$ findmnt -nr -o target -S /dev/sda1` - return only the mountpoint.

`$ grep /dev/sda1 /etc/mtab` - currently mounted filesystems

`$ lsblk -f /dev/sda1` - also displays the mounted filesystems or mountpoint.

`$ fdisk -l` - details of mount endpoint
