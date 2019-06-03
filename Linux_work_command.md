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
