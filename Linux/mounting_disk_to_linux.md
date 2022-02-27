### Tips to the mount in the linux

#### Create the folder
#### mount the folder to the disk

- Once the Linux Azure VM is created, under disk section
- a new disk can be attached.

#### Mount the folder 
 - First identify the disk available.
 
```
sudo mount /dev/sdb2 /projectdata

# the mount disk listed in the lsblk
# to point to the directory
```

#### To the make the mount to be available even after the restart. We need to make an entry in the `/etc/fstab`

 - Make sure to carefully update the `/etc/fstab` since if corrupted the VM will not be able to boot up.

#### To update the `/etc/fstab` 
```
# command that will list the disk that needs to be mounted
# this will list the disk that needs to be mounted
lsblk

## find the disk mount
```

##### use `blkid` to find the uuid of the disk
```
blkid
# identify the uuid for the dev to be mounted
```

#### edit the `/etc/fstab`
```
vi /etc/fstab
# update the below line similar at the end of the file

# UUID=<uuid-from-blkid-command-for-device>   /projectdata   btfrs noatime,nodiratime,nodev,noexec,nosuid    1  2
```

#### Check the `mount -a` 
```
mount -a 
## if the mount is set correctly, then above command
## shouldn't display any ERROR message
```

- Finally, to verify the mount

```
df -h
lsblk
# other options
lsblk -Sp
cat /proc/mdstat
```
