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
----
#!/bin/sh
IN=""

if [[ -z $IN ]]
then
        echo "blank values"
else
        echo "non-blank"
fi

=> output will print: blank values
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


### Using set -o for option name (set is built-in command and check [documentation](https://www.gnu.org/software/bash/manual/html_node/The-Set-Builtin.html)) flow up of above demonstration to stop the script from proceeding further in case of exception.

```
# below command will output hello in the terminal (instead of using a shell script using the below convention)
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
 
 # How to Fix `SSH Too Many Authentication Failures` Error
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
 #!/bin/sh

echo "Enter a number"
read -p "input : " var

var=${var:-"1"}

echo "Value : $var"

---
$ sh <abovescriptasfilename.sh>
output: (1st when use hits enter without any value)
 Enter a number
 input : 
 Value : 1
 
 Enter a number
 input : 3
 Value : 3
```

# To check if the string is starting with specific character
```
In command line 
$ echo "#disk_info" | grep -q '^#'; echo $?
 ==> outputs : 0
$ echo " #disk_info" | grep -q '^#'; echo $?
===> outputs : 1
----
#!/bin/sh
input="1"
 echo "#hello" | grep -q '^#'
 # $? output of the previous process
 # if condition on input not blank
    if [ $? -eq 1 -a ! -z "$input" ]
    then
     echo " print "
    fi
```

# Negation on if condition in shell script 

```
#!/bin/sh
IN="Hello"

# ! is used for negation like not blank in this case
if [[ ! -z $IN ]]
then 
   echo "Not blank value : $IN"
fi

output: Not blank value : Hello
```

# Linux/Unix `grep` usage
```
# -e regex pattern
      grep -e '|ONE|' -e '|one|' filename.txt
# create an input.txt file with following text 
# |one|two|three|four|
# |Hello|test|rest|next|
# -v inverse matches
      cat input.txt | grep -v '|test|'
# output: |one|two|three|four
      cat input.txt | grep -v '|four|'
# output: |Hello|test|rest|next|

      cat input.txt | grep -v -e '|one|' -e '|two|'
# output: |Hello|test|rest|next|
```

# Linux/Unix to `cut -d' ' -f 1 ` the data.
```
# create an input.txt file with following text 
# |one|two|three|four|
# |Hello|test|rest|next|

    cat input.txt | cut -d'|' -f 2
 output:
    two
    test
    
 Example:
    cat input.txt | cut -d' ' -f 1,3,4,6
```

# Linux command to print the ZERO SIZED FILES using FIND command
```
touch one.txt
touch two.txt

find . -size 0 -print
# . is the current directory
# -print is optional

Output:
./one.txt
./two.txt
     
```

# Linux/Unix command to count the files which were modified 60 days ago from today using `find` command
```
find . -name "*.*" -mtime -60 | wc -l

*.* => pattern of the file name like *.log,*.txt, etc.
- mtime -60 => switch for identifying the current time and substract 60 days from now
```

# Linux/Unix command to unzip set of zipped files in the folder using `unzip`
```
SOURCE_LOC=/path/where/files/present
DEST_LOC=/path/to/destitionation

unzip -qq "$SOURCE_LO/*.zip" -d "$DEST_LOC"
-q or -qq => switch to be quiet or quieter
```

# Linux command to set the current working directory in the korn shell prompt
```
   PS1="${HOSTNAME}:\${PWD} \$ "
```

# Linux command to remove carriage return ‘\r’ from the file in Unix
```
	tr -d '\r' < FileName_which_Has_Carriage_return.sh > FileName_where_the_trimed_content.sh
```

# Linux command to remove ^M from the files in VI editor
```
 :%s/[ctrlkey+v and ctrl-key+M]//g
# How it looks
 :%s/^V^M//g 
```

# Linux command to see the carriage return in a file using `cat`
```
 cat –v FileName_which_has_carriage_return.sh
```

# Command to use `awk` within linux
```
# create an input.txt file with following text 
# |one|two|three|four|
# |Hello|test|rest|next|

cat input.txt | awk -F'|' '{print $1,":",$2}'

output:
   one : two
   Help : test
```

# Last parameter stored in `$_`
```
echo one two three
# the values one two three are parameters
echo "$_"
# output: three
```

# Command to find the file contents in a jar file 
 - Helps to find the main jar file where the main method exists among lots of the jar file.
```
# template:
find . -name \*jar -print -exec jar -tvf {} \; | awk '/YOURSEARCHSTRING/ || /jar/ {print} '

# create a sample helloworld program with main method, and execute the below command from that file
find . -name \*jar -print -exec jar -tvf {} \; | awk '/main/ || /jar/ {print} '
# output: <jar_file_name_created.jar>

```

# Linux command to print the machine name 
```
uname -n | cut -d'.' -f1

# Example: if hostname.someval, then hostname will be printed.
```

# `fc -l` command to list the last few history commands other than `history` command
```
 ls 
 ps 
 fc -l
 # output last few commands executed (last 15 commands)
 1231 ls 
 1232 ps 
```

# command to print the port usage (note new command is `ss` like `$ sudo ss -tulp | grep ":22"` )
```
 netstat -nt | grep :22
 # output
 tcp        0      0 ::ffff:255.247.255.244:22    ::ffff:255.247.255.222:38933 ESTABLISHED
 tcp        0    184 ::ffff:255.247.255.239:22     ::ffff:255.247.255.8:3523   ESTABLISHED

```

# `sed` command to serach and replace the text in a file
```
# template
# sed 's/find_text/replace_text/g' search_file > newfile

# create an input.txt file with following text 
# |one|two|three|
# |Hello|test|rest|

sed 's/one/eight/g' input.txt > input1.txt
cat input1.txt
# output:
  eight|two|three|
  Help|test|rest|
```

# Linux/unix command to list the process in sorting order using `ps`, `tr`, `cut`, `sort`
```

ps -ef | tr -s " " | cut -d " " -f1,2,5,8 | sort -rk3,4

 # tr - translate command;
 #    -s => squeeze repeated string specified, in this case repeated space will be represented as single space
 # cut 
 #    -f 1,2 => field needs to be checked for the number prsented here, since the ps output might be different
 # sort 
 #    -rk (-r => reverse the result comparision; -k => sort via a key)
 #    
```

# Linux/Unix to use `date` command
```
date +%d%m%y%M%H%S
# output: 091219351818
date +%d-%m-%y#%M:%H:%S
# output: 09-12-19#36:18:16

# to execute this as a command and store to variable
OUTPUT=`date +%d-%m-%y#%M:%H:%S`;echo $OUTPUT;
# output: 09-12-19#38:18:16
```

# Linux/ Unix command to see difference between files
```
diff file1 file2 
sdiff file1 file2 

# Check the sed command section to replace the input.txt file one to eight example.
diff intput.txt input1.txt
# output:  changes are highlighted 
1c1
< one|two|three|
---
> eight|two|three|

sdiff input.txt input1.txt
# output: will be side by side comparision of files, the difference is highlighted with "|"
one|two|three|						      |	eight|two|three|
Help|test|rest|							Help|test|rest|

```

# Linux/Unix command to `sleep` for n seconds in the shell
```
#!/bin/sh

OUTPUT=`date +%d-%m-%y#%M:%H:%S`;
echo "EXECUTED AT : $OUTPUT";
sleep 30

OUTPUT=`date +%d-%m-%y#%M:%H:%S`;
echo "EXECUTED AT : $OUTPUT";

#output:
EXECUTED AT : 09-12-19#45:18:10
EXECUTED AT : 09-12-19#45:18:40

```

# Linux ssh issue with `Too many authentication fauilures`

```
# below command will connect to the VM without any failures issue.
# the other alertnate is to create ~/.ssh/conf file and set it up

$ ssh -o IdentitiesOnly=yes thiru@thiru-vm

<username@hostname>
```

# Linux SCP command, with `Too many authentication failures` issue
```
# below option will avoid the failures

$ scp -o PubkeyAuthentication=no <file-name-to-be-copied> username@hostname:~/path

```

# `find` command to find files and change the permissions
```
# list the files
# -type f => for regular files.
# -type d => for directories
# -type l => symbolic links
# use exec rather using xargs
$ find . -type f -print0 | xargs -0 chmod 775

# lists the files in single line
$ find . -type f -print0 | xargs -0 

# lists the files in new line
find . -type f -print | xargs -0 
```

# Command to display the content of file in unix, using `cat` and `tac`

cat filename => will display the content of the text starting from first line
tac filename => will display the content in reverse way
##### Use git-bash Windows/Linux application 
```
$ vi input.txt
1
2
3

$ cat input.txt
1
2
3

$ tac input.txt
3
2
1
```
##### `cat` command and additional useful options `-A` , `-b`, `-s`
```
$ cat -A input.txt
// -A displays the non-printalbe characters, in this case the EOF (end of line) with $

$ cat -b input.txt
// -b displays the line number

$ cat -s input.txt
// -s suppress repeated empty lines
```

# `file` command to find the type of the file
Unix doesn't need a extension of the file to identify the type of file
```
# use the file command 
$ file input.txt

$ file intput.sh
```

# Different types of compression and usage of `tar` command
`tar` - Tape Archiver, by default it _**doesn't**_ compress data.

By default the folder will be removed and the tar file will be created.

The usage of '-' in tar option is optional, only in few commands.
```
# to create a tar file
$ tar -cvf <filename.tar> /path-of-dir-file-to-tar1 /path-of-dir-file-to-tar2
$ tar -cvf mytar.tar /home /etc

# Redirect the tar file creation to different path, output the created tar to different path.
$ tar -cvf mytar.tar /home /etc -C /tmp 

# to list the content of the tar file
$ tar -tvf mytar.tar

# To extract the tar file 
$ tar xvf mytar.tar

```
##### Compress options with the `tar` file itself 
```
#-z is for gzip
#-j is for bzip2
#-J is for xz
```

##### Below are few different type of compression techniques, xy is the latest one which is slow but efficient compressions
  - bz2
  - gzip
  - zip
  - xz
```
# to gzip use the command
$ gzip directory-name
$ gzip mytar
// The gzip adds the extension .gz automatically

# to un-gzip use the command
$ gunzip mytar.gz

# using bzip2
$ bzip2 mytar

# using xz compression
$ xz mytar
// use xz --help for more options
```

# viewing the file using `more`, `less`, `tail`, `head` usage
 more - one way navigation, provides a percentage at the bottom.
 less - allows to move backwards and forward, this is more useful.
 ```
 $ cat input.txt | less
 
 # tail to display last 10 lines of a file
 $ tail -n 10 input.txt
 
 # to open and stream the file content in real time using -f, helpful in the log file viewing
 $ tail -f input.txt
 //use cntrl + c to come out.
 
 # head to display first 10 lines of a file
 $ head -m 10 input.txt
 ```
 
 # commands `cut`, `sort`, `tr` 
  cut - to cut 
  sort - to sort
  tr - to translate
  ```
  # cut the field
  $ cut -f 3 -d : /etc/passwd | sort | less
  // -f field followed by field number.
  // -d delimeter to cut
  
  # sort to use number or numeric sort using -n
  $ cut -f 3 -d : /etc/passwd | sort -n | less
  
  # translate: lower cast to upper case
  $ cut -f 1 -d : /etc/passwd | tr [:lower:] [:upper:]
  // usage of tr [a-z] [A-Z] is available to use but incase of special char it will cause issues.
  ```

  # String manipulation
  ```unix
   string=abcde
   echo ${#string} // prints length of the string
  ```
[Reference](https://www.tldp.org/LDP/abs/html/string-manipulation.html)


  # associated array usage is `-A` doesn't maintain the order, in case to maintain order use `-a` like below.
  ```sh
$ declare -a arr
$ arr=( [2018]=5%12 [2019]=1%12 [2020]=1%2 )
$ for year in "${!arr[@]}"; do printf '%s: %s\n' "${year}" "${arr[${year}]}"; done
  ```
  
  Reference : [stackexchange link](https://unix.stackexchange.com/questions/582347/for-loop-with-key-value-pair-the-key-is-sort-order-is-not-maintained)
  
  # In order to shift the arguments passed use `shift` option.
  
  ```sh
  #!/bin/sh

export idx=0 ;
# if the first argument is not null then prints
while [ "$1" != "" ]
do
  # just a counter
  idx=$(expr ${idx} + 1);
  echo "Parameter ${idx} : $1 $2 $3 " # Note just validate input arguments.
  shift  # move all the positional parameter by one
done

# ouput : {input sh <script.sh> 1 2 3
# Parameter 1 : 1 2 3 
# Parameter 1 : 2 3 
# Parameter 1 : 3 

```

# setting  arguments to next statement using `set` for next statment 
```sh
#!/bin/sh

echo "$1 $2"
set var1 var2
echo "$1 $2"  # the input argument is set with this value
set `date`
echo $6  # prints the 6 item 

#output : setCommandExample.sh test1 test2
# test1 test2
# var1 var2
# PM <since date format was - day, month dd, yyyy hh:mm:ss PM>

```

# `while` loop usage example
```sh
#!/bin/sh

i=0;
while true 
do
i=$(($i+1));
echo $i
if [ $i == 10 ]; then
 echo "$i"
break;
fi

done;

```

# approximate month calculation, shell string to `date` conversion technique
```sh
#!/bin/sh

#DATE1=$(date +'%m-%d-%Y')  # Way to convert string to date
DATE1=$(date '+%s')  # %s is standard format of date

echo $1

# Input from user in format DD-Mom-yyyy
INPUT="$1"

if [[ -z "${INPUT}" ]]; then
echo " no input"
INPUT="01-MAY-2018";
fi

#DATE2=$(date -d ${INPUT} +'%m-%d-%Y')  # different format of date 
DATE2=$(date -d $INPUT '+%s')

echo DATE1 - DATE2= $DATE1 - $DATE2


#Diff=$(date -u -d@$(($DATE1-$DATE2)) +%m:%d:%Y) # another way to find difference

Diff=$((($DATE1-$DATE2)/(60*60*24)))
echo $Diff days

echo $(($Diff/30)) months 

# single statement to provide the date in month difference, this is 30 days approximation

echo $((($(date '+%s')- $(date -d $INPUT +'%s'))/(60*60*24*30))) months

```
