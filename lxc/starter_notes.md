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

- list profile
```
lxc profile list
```

- show default profile

```
lxc profile show default 
```

copy profile
```
lxc profile copy default k8s
```

Use list profile to view 

to edit the lxc profile use

```
lxc profile edit k8s
```

update under config

```yaml
config:
   limits.cpu: "2"
   limits.memory: "2GB"
   limits.memory.swap: "false"
   linux.kernel_modules: ip_tables,ip6_tables,netlink_diag,nf_nat, overlay
   raw.lxc: "lxc.apparmor.profile=unconfined\nlxc.cap.drop=\nlxc.group.devices.allow=a\nlxc.mount.auto=proc:rw
    sys:rw"
   security.privileged: "true"
   security.nesting: "true"
  
```