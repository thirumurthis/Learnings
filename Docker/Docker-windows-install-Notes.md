Docker install had the issue 

Ethernet Adapter #3' (VERR_INTNET_FLT_IF_NOT_FOUND). ,VBoxManage.exe: error: Failed to attach the network LUN (VERR_INTNET_FLT_IF_NOT_FOUND) 

VBoxManage.exe: error: Failed to attach the network LUN (VERR_INTNET_FLT_IF_NOT_FOUND) ,VBoxManage.exe: error: Details: code E_FAIL (0x80004005), component ConsoleWrap, interface IConsole ,,Details: 00:00:03.292581 Power up failed (vrc=VERR_INTNET_FLT_IF_NOT_FOUND, rc=E_FAIL (0X80004005)),

in order to fix this, refer the below link
[fix link](https://caffinc.github.io/2015/11/fix-vbox-network/)

---
have to resinstall the docker toolbox, have to install the vboxdr.inf (right click  and install) the driver.

[gitLink](https://github.com/docker/toolbox/issues/473)

uninstall virtual box, and install both dropbox and virtual box.
