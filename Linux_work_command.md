# to list the contents of the file 

```
 $ unzip -l <zip_file_name>.zip
```

# to extract only one file from zip

```
$ unzip -p <Zip_file_name>.zip /path_to_file_in_the_zip > file_content_to_save
```

# check if the file path is a directory or not in shell script

```
  if [ -d /path/to/dir ]   
  then 
      echo "directory present"
  else
      echo "directory not present"
  fi
```  

# check if the string is null or not in shell script
```
  echo "$1, $2, $3, $4"

  if [[ -z $4 ]]
  then
          echo "Inside if loop"
  else
          echo "else part of loop"
  fi
 # will print the " else part of loop" since the $4 command line argument is not zero
 # execute the shell with 1 2 3 4 5 
```

# To search content in the Zip file
```
 $ zgrep <content_to_search> <zip_file_name or Patter_of_zip_file>
 $ zgrep 'date' my-file.zip
 ```

# In unix/Linux to convert the any case to lower case using *tr* command
##### tr command translate the range of A-Z to lower case for standard ouput content
```
 $ stringTobeConvert="heLLO World"
 $ convertedString= $(echo "${stringTobeConvert}" | tr '[A-Z]' '[a-z]')
```

# To convert a pwd command output to a variable 
##### awk NF- Nubmer of fields, FS - File separator (no space between =)
```
$ outputOfCommand=`pwd`
$ listofinputs=`ls -l "/home/user/" | awk '{print $NF}'`
 output: 8 one two { prints the last word of the ls command which is trimmed using awk default delimiter ' ' (space), since total 8 (total number of block took by the file) is part of ls command output 8 is displayed along with other files in the folder}
```

# using *if* *else* in ksh script template
```
if [ "${var1}" = "one" ]
then
  echo "one"
elif [ "${var2}" = "two" ]
then 
  echo "two"
  exit
elif [ "${var1}" = "default" ]
then
  $var2='y'
fi
```

# Passing a string to another shell script, treat that as an list using set in ksh
```
firstshell.sh
   # store the number of files in the home folder to variable along with total block string.
   input1='ls -lrt /home | wc -l'
   . ~/secondshell.sh "${input1}"
   
secondshell.sh
   set -A var $input1
   returnVal=`eval ${var[0]}"`
   echo $returnVal
```

# Create a set of files using touch command and ls
```
ls -lrt *.ZIP | awk '{ print $9 }' | xargs -I '{}' touch {}.complete
```

# In order to stop the bash shell script when encounters an exception in line or pipeline flow
```
#!/bin/bash

# when the shell script executed with argument 1 if loop is executed
if [ $1 -eq 1 ]; then
  set -e;
fi
for i in {1..10}
do
  echo $i
  if [ $i -eq 6 ]; then
        echo "value 6";
        # below is to print the execution status of previous command
        echo $?
        # below comment will make sure to send an exit status of non-zero
        ls -unknown >& /dev/null
        echo $?
        echo `hash`
  fi
done;
-- save the above in a sampleScript.sh
# below is to mark the shell script as executable
$ chmod 775 sampleScript.sh
# run the shell script command with no arguments (#1) and with 1 as argument (#2)
$ ./sampleScript.sh
$ ./sampleScript.sh 1
```
  ##### Note: When executing without argument, notice all the lines are being executed, but with argument, the 'set -e' is executed and the script exection stops once it encouters a exist status of non-zero number
  [online linux terminal](https://bellard.org/jslinux)


# Using set -o for option name (set is built-in command and use [documentation](https://www.gnu.org/software/bash/manual/html_node/The-Set-Builtin.html))

```
# below command will output hello in the terminal (instead of using a shell script using the below convenstion)
/bin/bash -c 'set -e; set -o pipefail; ls >& /dev/null;echo "hello"; wait'

# below command will not ouput anything since the ls command has incorrect options or switch
/bin/bash -c 'set -e; set -o pipefail; ls -# >& /dev/null; echo "hello"; wait'
```

# To delete the blank lines from the VI editor

```
$ vi somefile
-- once opened with blank line space
use:
   :g/^$/d
 
 g - globally applied
 ^ - beginning of line
 $ - end of line 
 d - delete 
 
 similar to deleting lines in vm, to delete lines 1 to 10 use 
 
 : 1,10d
 ```
 
 # How to Fix “SSH Too Many Authentication Failures” Error
[Link](https://www.tecmint.com/fix-ssh-too-many-authentication-failures-error/)

```
  $ ssh -o IdentitiesOnly=yes hostname
```
 # To check if the string ends with specific value in shell script
```
#Console:
$ x="test.com/"
$ [[ "$x" == */ ]] && echo "yes"
yes

#Script file
x="test12"
if [[ "$x" == *12 || "$x" == *13 ]] then 
 echo "matched"
 continue
else
 echo "not matched"
fi
```

# To bulk commit a shell script #!/bin/sh use below apporach
```
: <<'COMMENT'
 string of code
  string of code
  string of code
  string of code
COMMENT

Note: the : and should start at start of the line.
```

# Setting default value to a variable in shell script

```
 var=${var:-"1"}
```
