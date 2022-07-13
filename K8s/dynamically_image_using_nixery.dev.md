## In docker or kubernetes, we can use the nixery.dev image to dynamically install package.

```
$ kubectl run nixery1 --rm -it --image=nixery.dev/shell bash
```
- The above command will open up the running container in Bash shell mode where you can type commands.
- Executin curl or wget command will result in `command not found`

### We can dynamically install the required pacakge like curl and wget in the image
```
$ kubectl run nixery2 --rm -it --image=nixery.dev/shell/curl/wget bash
```

- Now in the command prompt, execute curl or wget which will work. `curl www.google.com`
