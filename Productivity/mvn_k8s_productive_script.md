# Automate repitative tasks using shell scripts

In day to day application development especially for Microservice architecture based application built using SpringBoot and  Kubernetes during development we tend to build artifacts images multiple times.

In this blog will detail how to automate few of the day to day development process using shell script and Git Bash. 
This is based on my day to day development activities where I tend to use `mvn` and `kubectl` commands frequently in my local environment.

## Prerequisites
   - Basic understanding of Shell scripting
   - Kind CLI installed (Running in Docker Desktop)
   - Git Bash Installed

## Automate mvn command as a alias in Git Bash

 - To set an alias command for maven command 
    - Add below command in `$HOME/.bashrc` file in windows.

```
alias runMvn='mvnFunc(){ POMFILE="pom.xml"; if [ -f "$POMFILE" ]; then mvn "$@" clean install; else echo "$POMFILE not found"; fi; unset -f mvnFunc; }; mvnFunc'
```

- If the Git Bash is already opened, to apply the changes to reflect on the open shell issue `$ source ~/.bashrc`.

- About the above code,
   - Defines a simple shell function, which checks is the pom.xml file exist in the current directory.
   - The usage of "$@" is to use the argument in the mvn command passed as argment.

### Command usage in Git Bash

- The alias command without any argument will execute the `mvn clean install`
```
$ runMvn
```

- The alias command with argument will execute `mvn -DskipTests clean intall`
```
$ runMvn -DskipTests
```

## Automate kubectl create and delete resource from yaml file

- To automate the kubectl apply and delete resources we create a simple shell script.
- The shell script will be configured in the `~/.bashrc`, 

```
alias runK8SScript='kubeFunc(){ TMPPWD=$(pwd); cd /c/shellScript/; sh runK8SScript.sh "$@" ; cd $TMPPWD; unset -f kubeFunc; }; kubeFunc'
```

- Create a Script (.sh) file named `runK8SScript.sh` with the below content.
   - Below script can be extended say if we have mulitple yaml file we can utilize it.
   - Likewise we can use helm to deploy charts as well.

```
#!/usr/bin/env bash

ACTION=

handleResource()
{
  INPUT_ACTION=$1
  CONTEXT=$2
  MANIFEST_PATH=$3
  MANIFEST_FILE=$4
  
  echo "[DEBUG] Executing .. kubectl --context $CONTEXT $INPUT_ACTION -f $MANIFEST_PATH$MANIFEST_FILE"
  # if namespace not provied within the manifest 
  # pass it to this function to a new variable
  kubectl --context $CONTEXT $INPUT_ACTION $MANIFEST_PATH$MANIFEST_FILE
}

info() { echo -e "$0 -<option> <action> \nusage:" && grep " .)\ #" $0 | sed 's/)\ ##//g' | sed 's/\([cd] \)/-\1/g';}

while getopts ":c:d:" option; do
  case $option in
    c) ## create
      echo "option provided $OPTARG"
      ACTION="C"
      ;;
    d) ## delete
      echo "option provided $OPTARG"
      ACTION="D"
      ;;
    *) 
       echo "not correct usage, use "
       info;
       exit 0;
  esac
done

if [[ -z "$ACTION" ]]; then 
  echo "other available options"
  info
fi

# using kind and the cluster context is defined
CONTEXT_TO_DEPLOY="kind-demo1"
# use / at the end
MANIFEST_FOLDER="/c/shellScript/manifest/"

#yaml file
MANIFEST_FILE_NAME="nginxDeploy.yaml"


if [[ "$ACTION" == "C" || -z "$ACTION" ]]; then
    SKIP=0
    read -p "Do you want to create resource (y/n)? - " input
    if [[ ! -z "$input" && !( "$input" == "y" || "$input" == "Y" || "$input" == "yes" || "$input" == "YES" ) ]]; then 
       echo "Resource creation ignored"
       SKIP=1
    fi
    if (( $SKIP == 0 )); then 
      echo "Creating resources"
      handleResource "apply" $CONTEXT_TO_DEPLOY $MANIFEST_FOLDER $MANIFEST_FILE_NAME
    fi
fi

if [[ "$ACTION" == "D" || -z "$ACTION" ]]; then 
    SKIP=0
    read -p "Do you want to delete resource (y/n)? - " input
    if [[ ! -z "$input" && !( "$input" == "y" || "$input" == "Y" || "$input" == "yes" || "$input" == "YES" ) ]]; then 
       echo "Resource deletion ignored"
       SKIP=1
    fi
    if (( $SKIP == 0 )); then 
        echo "Deleting resource"
        handleResource "delete" $CONTEXT_TO_DEPLOY $MANIFEST_FOLDER $MANIFEST_FILE_NAME
    fi
fi
```

###  Usage of the alias command

```
$ runK8SScript 
```

#### Output

![image](https://github.com/thirumurthis/Learnings/assets/6425536/9068927b-fe43-47c0-8e43-17f33a769a73)

```
$ runK8SScript -c create
```

#### Output 

![image](https://github.com/thirumurthis/Learnings/assets/6425536/34ccaee5-e5f3-4d79-91ab-8facd62799eb)

```
$ runK8SScript -d delete
```

#### Output

![image](https://github.com/thirumurthis/Learnings/assets/6425536/4cedf531-d884-4b65-afb7-1c354468dab2)
