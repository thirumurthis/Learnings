#!/bin/sh

### using jobs


## shell script

perform_operation(){
   line=$1
   encodeline=`echo -ne ${line} | base64 -w0`
   echodedline="'"${encodeline}"'"
   command="az storage message put --account-name <STORAGE_ACCNT_NAME> --auth-mode login -q <AZURE_BLOB_QUEUE_NAME> --content ${encodedline}"
   eval $command
}

while:
do 
    echo "select an option"
    echo "1. ... "
    echo "2. exit"
    
    read -p "(1/2)" operation
    case $operation in 
       1) read -p "enter file name where input is present" fileName
       echo " start processing"
       COUNT=0
       echo "start $(date +%T)"
       Start_time=$(date +%s)
       
          while IFS= read -r line; do
              COUNT=$(( $COUNT + 1))
              perfom_operation "${line}" &
              [ &( jobs | wc -l) -ge $( nproc ) ] && wait
              done < $fileName
          wait

          END_TIME=$(date +%s)
          ehco "complete := $(date +%T)"
          echo "total time $(( $END_TIME - $Start_time))
          ;;
          
          2) echo "exit"
          break
      esac
    done
    
