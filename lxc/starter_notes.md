- install the LCD using `sudo snap install lxd`

- once installed start the service using systemctl

- to exclude using sudo on lxc command add user to the LCD group. steps

```
# get the LCD group info
$ getent group lxd

# to add current user to group
$ sudo gpasswd -a <my-current-username> lxd

# to check the access
$ id
```