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
