Below is code snippet to validate if the string container a value in bash script.

##### Compare if string present in the bash script

```
#/bin/bash

index=stringtest-*-$(date -u +"%Y.%m.%d");

export catchword="";

if [[ $INDEX == *"test"* ]]; then 
    catchword="got-the-match-string"
else
    catchword="no-match-poor"
fi

echo $catchword

```

We can use the below command to clean the file which was created last 24 hours and 5 hrs ago
##### Clean up the files

```
 find ~/path/*/log -mtime 0 -type f -name '*.log' -delete 
 find ~/path/*/log -mtime +5 -type f -name '*.log' -delete
 
```

##### using shell function

utilprog.sh

```unix 
#!/bin/ksh
function checkIsProcessRunning
{
   PROCESS_STATUS=0  # 0 - NOT running ; 1 - running
   PROCESS=$1
   PROCESS_NAME=$2
   
   USER_NAME=`whoami`
   
   if [ -z "$PROCESS_NAME" ]
   then 
       PROCESS_NAME=""
   fi
   
   PROCESS_IDS=`ps -u $USER_NAME --no-headers -o pid,lstart,ppid,sid,comm,args,cmd | grep -v grep | grep -- $PROCESS_NAME | awk '{print \$1}`
   
   COUNTER=0
   for count in $PROCESS_IDS
   do
      COUNTER=`expr $COUNTER +1`
   done
   
   if [ $COUNTER -gt 1] 
   then 
      PROCESS_STATUS=1
   elif [ ! -z "$PROCESS_IDS" ]
   then 
     PROCESS_STATUS=1
   fi
   
   # below statement is only needed when the funtion is invoked using 
   # FUNCTION_RETURN_VALUE=`checkIsProcessRunning vim somefile`
   echo $PROCESS_STATUS 
   
   # is the calling function used as below no need to specify the echo
      
}
```

callingfun.sh
```unix
#!/bin/ksh

# import the other shell, so the shell function can be called here
./utilprog.sh

FUNC_CHECK=`checkIsProcessRunning vim somefile `
echo $FUNC_CHECK

checkIsProcessRunning vim somefile
echo $PROCESS_STATUS 
# since the shell is imported, PRCESS_STATUS is accessible in this scope.
```

To test
```unix
# in terminal open
$ vi somefile

# in another terminal
$ sh callingfun.sh

# check the response
```
