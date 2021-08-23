
- The azure vm local workstation had ~50MB swap memory

- use the command `$ swapon -s` to check and see the mounted swapfile, in my case it was `/mnt/resource/swapfile` which was 50MB in size.
   - Note: the `/mnt/resource`, is Azure temp disk provided for all the VM.
   - Other commands to see if the temp mounted disk `$ lsblk`, `$ df -h`, `$ fdisk -l`
   
- Edit `/etc/waagent.conf` file, update `ResourceDisk.SwapSizeMB=3072` for 3GB space.
- Restart the `waagent` service, `$ sudo systemctl waagent`

- `$ free -h` command now lists the 3GB space allocated to swap file, meanwhile the old data would e cleared.

