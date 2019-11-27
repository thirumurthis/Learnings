Below is code snippet to validate if the string container a value in bash script.

##### Compare if string present in the bash script

```
#/bin/bash

index=stringtest-*-$(date -u +"%Y.%m.%d");

export catchword="";

if [[ $INDEX == *"test"* ]]; then #if index is metricbeat or filebeat
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
