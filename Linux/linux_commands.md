
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

