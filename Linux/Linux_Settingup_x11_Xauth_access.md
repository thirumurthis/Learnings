#### in case if we need to setup the Xterm on a headless machine (centos)

STEP: 1
```
#from the client machine, connect to the server (headless), issue below command

$ ssh -X username@hostname
```

  - In certain case if you see below message use `-Y` flag
```
Warning: untrusted X11 forwarding setup failed: xauth key data not generated

```

```
$ ssh -Y -o IdentitiesOnly=yes username@hostname

# -o is used since the correct certificate couln't be used by the linux machine
```

STEP: 2
##### List the xauth token info for DISPLAY environment
```
$ xauth list $DISPLAY
mydomain-hostname.com/unix:10  MIT-MAGIC-COOKIE-1  ad1......3aef
```

STEP: 3
##### Add above token to xauth service [copy and include that content 
```
$ xauth add mydomain-hostname.com/unix:10  MIT-MAGIC-COOKIE-1  ad1......3aef
```

STEP: 4
##### Set the DISPLAY environment variable to use correct terminal number
   - This should be same listed in Xauth token above in this case it is 10
```   
$ export DISPLAY=:10
```

STEP: 5
##### Open the jvisualvm for connecting the monitoring process
```
$ jvisualvm
```

#### Connect to corresponding process
#### In jvisual select provide the JMX Connection details and try connecting 

