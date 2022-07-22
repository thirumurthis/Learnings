https://www.shellhacks.com/git-config-username-password-store-credentials/
##### set the cridentials again. (mostly in Windows use the cerdential manager to update the password - should work)
```
git config --global credential.helper store
```

##### followed the below steps since it was not working for me
```
git config --system --unset credential.helper
```
##### for global
```
git config --global--unset credential.helper
```
##### After executing the above two unset commands, using git pull prompted for user id and passsword.

##### then was able to connect, i need to perform the set oparation again since everytime git pull prompts for password.


---
- When working with windows git bash, came accorss below issue when apply stash command

```
> git stash
warning: LF will be replaced by CRLF in ahm-web-devel/eclipse-settings/org.eclipse.platform_4.23.0_1473617060_linux_gtk_x86_64/configuration/org.eclipse.osgi/335/0/.cp/intro/css/default.css.
The file will have its original line endings in your working directory
error: lstat("myprojcet/eclipse-settings/org.eclipse.platform_......................): Filename too long
```

- Fix is 

```
> git config --global code.logpaths true
```

check [stackoverflow](https://stackoverflow.com/questions/22575662/filename-too-long-in-git-for-windows)
