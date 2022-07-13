
### create a file with below value, save it as custom.env

```
CUSTOM_ENV=myvalue
```

- Open up a shell and issue below command to see the exported env variable for that specific shell 
```
$ set -o allexport; source custom.env; set +o allexport; echo $CUSTOM_ENV

myvalue
```
