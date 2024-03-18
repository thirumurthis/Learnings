## When working with WSL2 in order to skip providing the sudo password everytime we can do below

- we update the user to be an no pass by updating visudo and adding a line
```
$ sudo visudo
```
- scroll to bottom of this file and add below and save the change.

```
<user-name> ALL=(ALL) NOPASSWD: ALL
```

