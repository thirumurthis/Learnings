Below is code snippet to validate if the string container a value in bash script.

## Compare if string present in the bash script

```bash
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
## check if the unput string is digit or ends with s/h/m

```bash
#!/bin/sh


#User input time delay
TIME_DELAY=$1

# if no input then delay defaults to 2 hour
if [ -z $TIME_DELAY ]
then
TIME_DELAY=2h
fi

# when the input doesn't match sleep exit application
pat="[0-9]+[hms]+"
if [[ "$TIME_DELAY" =~ ^[0-9]+$ ||"$TIME_DELAY" =~ $pat ]]
then
 echo "process executed script with TIME DELAY of $TIME_DELAY"
else
 echo "Incorrect time delay format, use pattern like 100, 100s, 20m, 2h ";
 exit;
fi


echo $TIME_DELAY

```

We can use the below command to clean the file which was created last 24 hours and 5 hrs ago
## Clean up the files

```bash
 find ~/path/*/log -mtime 0 -type f -name '*.log' -delete 
 find ~/path/*/log -mtime +5 -type f -name '*.log' -delete
 
```

## using shell function

utilprog.sh

```bash 
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
```bash
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
```bash
# in terminal open
$ vi somefile

# in another terminal
$ sh callingfun.sh

# check the response
```

## DATE manipulation

```bash
#!/bin/ksh

# input validation will be performed for 20,21 century
INPUT_YEAR=$1
# input month should be between 1-12, representing Jan-Dec resp.
# validation to be performed.
INPUT_MONTH=$2


#if either input year or month is null then compute based on today execution
if [[ -z $INPUT_YEAR || -z $INPUT_MONTH ]]
then

# Get today day 
CURRENT_DAY=$(date +'%Y-%m-%d' | cut -d'-' -f 3);

# Since the SQl executes from start and end of the month
# below logic is used with reference to reteintion period
if [ ${CURRENT_DAY} -lt 15 ]
then
    echo "- 3 months"
   DATE_INFO=$(date +'%Y-%m-%d' -d -3month)
fi

if [ ${CURRENT_DAY} -ge 15 ]
then
    echo " -2 month"
   DATE_INFO=$(date +'%Y-%m-%d' -d -2month)
fi

INPUT_YEAR=$(echo $DATE_INFO | cut -d '-' -f 1);
INPUT_MONTH=$(echo $DATE_INFO | cut -d '-' -f 2);
fi

if [ ${#INPUT_YEAR} -ne 4 ]
then 
  echo "Input year not correct";
  exit;
fi
if ! [ $INPUT_YEAR -ge 2000 -a $INPUT_YEAR -le 2999 ]
then 
  echo "Input year not correct"
  exit;
fi

if ! [ $INPUT_MONTH -ge 1 -a  $INPUT_MONTH -le 12 ]
then 
  echo "Input month not correct"
  exit;
fi

echo input year $INPUT_YEAR
echo input month $INPUT_MONTH
```

## Printing first 10 and last 10 lines of the file using head and tail within shell function for list of files.
```bash
#!/bin/sh

rm logInfo.txt
# iterate the list of files, make sure that awk prints only the file name.
for n in `ls -lrt /home/user/test/filename* | awk -F' ' '{print $9}'`
do
echo "performing file ${n}" 
echo "File name: ${n}" >> logInfo.txt;
lines_in_file=0;
lines_in_file=$(wc -l "${n}" | awk -F' ' '{print $1}');
head_only=0;
head_num=10;
tail_num=10;

# if the file has only few lines no needs to print the tail
if [ ${lines_in_file} -le ${head_num} ]
then
 head_only=1;
fi
head -n ${head_num} ${n} | sed 's/  //g' >> logInfo.txt;

if [ ${head_only} == 0 ]
then
 tail_num=$(expr ${lines_in_file} - ${head_num})
if [ ${tail_num} -ge ${head_num} ]
then
  tail_num=10;
fi
echo "....." >> logInfo.txt
echo  >> logInfo.txt
 tail -n ${tail_num} ${n} | sed 's/  //g' >> logInfo.txt;
fi
echo "_______________***_______________" >> logInfo.txt
done;
cat logInfo.txt;

```

## script will run the specific process with some delay provided that the calling script takes year and month as input
  - usage of script Array's as key value pair
  - for look with start and end (range seq) usage
```sh
#!/bin/sh


if [ "$1" == "--help" ]; then
 echo "usage: sh ./$0 <year> <month> <timedelay>"
 echo "default values, year-2008, month-5, dealy 2h"
 exit;
fi
#User input time delay
TIME_DELAY=$3

# if no input then delay defaults to 2 hour
if [ -z $TIME_DELAY ]
then
TIME_DELAY=2h
fi

# when the input doesn't match sleep exit application
pat="[0-9]+[hms]+"
if [[ "$TIME_DELAY" =~ ^[0-9]+$ || "$TIME_DELAY" =~ $pat ]]
then
 echo "process executes script with TIME DELAY of $TIME_DELAY"
else
 echo "Incorrect time delay format, use pattern like 100, 100s, 20m, 2h ";
 exit;
fi

INPUT_YEAR=$1
INPUT_MONTH=$2

# if input year alone provided, then use the start range info from default value
# 2018 - 5 to 12 
# 2019 - 1 to 12 
# 2020 - 1 to 2

# input year is not provided it will be 2018 

# input month is not mandatory, not provided then will be  5 of 2018

# if input month is provided, then we need to use the year and month to detemine 
# the start range and end range for that month and also next year if applicable.

# if input is 2018
# then pass in 2018 5 to 12; 2019 1 to 12; 2020 1 to 2

# if input is 2018 8
# then pass in 2018 8 to 12; 2019 1 to 12; 2020 1 to 2

# if input is 2019 4
# then pass in 2019 4 to 12; 2020 1 to 2 

# array decleartion for holding the year month (default) range
declare -A arr
arr=( ["2018"]=5%12 ["2019"]=1%12 ["2020"]=1%2 )

if [[ -z $INPUT_YEAR ]]
then 
  INPUT_YEAR=2008
fi

if [ $INPUT_YEAR -ge 2020 -a $INPUT_YEAR -le 2008 ]
then 
  echo "Input year not correct, range is from 2018 to 2020"
  exit;
fi

if [[ -z $INPUT_MONTH ]]
then
## if no month is provided for 2018 take the 5 as month else 1 
if [ $INPUT_YEAR == 2008 ]; then
 INPUT_MONTH=5
else
 INPUT_MONTH=1
fi
fi

#If the input month should fall under 1-12
if ! [ $INPUT_MONTH -ge 1 -a  $INPUT_MONTH -le 12 ]
then
  echo "Input month not correct"
  exit;
fi

# for 2018 if the input month was less than 5 then default to 5
if [ $INPUT_YEAR == 2018 -a $INPUT_MONTH -lt 5 ]; then 
 echo "for 2018 year, 5th month will be used by default if the input month is less than 5"
 INPUT_MONTH=5
fi 

# itreate the default range and determine which date range to be passes
# if determine the start date of the input it should be fine, the default already holds the end date

for year in ${!arr[@]}
do
#echo  ${year} ${arr[${year}]}
MONTH_RANGE=${arr[${year}]}
#echo range check
# if the input year matches the key in the default array
# we should then identify the month range.
# if input is 2019, the iteration starts from 2018, so the 2018 should be skipped
# use else part to continue
if [ ${year} -ge ${INPUT_YEAR} ] 
then
 #read -ra MONTH <<< $MONTH_RANGE
START_MONTH=$(echo "${MONTH_RANGE}" | cut -d'%' -f 1)
END_MONTH=$(echo "${MONTH_RANGE}" | cut -d'%' -f 2)

# input year is equal and input month is different from default start one.
if [ "${year}" == "${INPUT_YEAR}" ]
then 
 START_MONTH=$INPUT_MONTH
fi 
#echo "PROCESS - start and end of the month from default value : $START_MONTH : $END_MONTH"
for mon in $(seq $START_MONTH $END_MONTH)
do
 echo "Procesing for year:month <=> ${year}:${mon}"
 # This script takes input a year and month.
 echo "started at `date +'%Y-%m-%d %H:%M:%S'`"
 echo " sh ./myspecific-script.sh ${year} ${mon}"
 echo "Process completed at `date +'%Y-%m-%d %H:%M:%S'`"
 echo " sleep $TIME_DELAY"
done;
else
 #echo "DONT PROCESS -> input year is not matched this iteration array-key:${year}, input year:${INPUT_YEAR}"
 continue;
fi
done;

```

