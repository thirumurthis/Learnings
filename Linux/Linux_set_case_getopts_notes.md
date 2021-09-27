
```sh
#!/bin/bash

# filename: simple-setopts-script.sh

# below is similar statements, so the variables are set to the environment variable afte the command
# set -o allexport
# set -k

while getopts ":abc" i; do
    case "${i}" in
       a) 
           echo "option a provided"
           ;;
       b)
           echo "option b provided"
           ;;
       c)
           echo "option c provided"
           ;;
       *)
           echo "default option"
           ;;
    esac
 done
 shift $((OPTIND-1))
```
## Executing the shell script
```
$ simple-setopts-script.sh -a
```
 #### use gitbash with `getopts --help` which has OPTIND, 
  - OPTIND is intialized to 1 each time the shell or a shell script is invoked
 
 #### shift, shifts the positional parameter
