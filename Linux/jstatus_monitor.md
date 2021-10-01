```sh
#!/bin/sh

PROCESSINFOS=$(jps -m | grep -v grep | grep MYPROCESS);

#file speartor set to new line not default space/tab
IFS=$'\n'
  printf "%-17s | %15s  | %15s  |  %10s |  %s \n" 'DATE' 'USED_HEAP(MB)' 'EOS0S1OS_CAP(MB)' 'ALLOC._HEAP' 'PROCESSID PROCESS'
  printf "%-17s | %15s  | %15s  |  %10s |  %s \n" '____' '_____________' '________________' '___________' '________________'
for processinfo in ${PROCESSINFOS}
do
  processinfo1=$( (echo $processinfo | cut -d " " -f 1,3,4));
  PID=$( echo $processinfo | cut -d " " -f1 )
#  echo $PID
#S0C     S1C      S0U    S1U      EC       EU        OC         OU       MC     MU       CCSC   CCSU      YGC     YGCT   FGC    FGCT     GCT   
#21504.0 22016.0  0.0   15808.5 304640.0 171116.6  699392.0   20178.4   57136.0 56145.0 7472.0  7251.7     17    0.400   2      0.638    1.038


  DATEINFO=$(date +'%m-%d-%y:%H:%M:%S')

  ALLOCATED_MEM=$( (jstat -gc $PID 2>/dev/null || echo "0 0 0 0 0 0 0 0 0") | tail -n 1 | awk '{split($0,a," "); sum=a[1]+a[2]+a[5]+a[7]; print sum/1024}' ) 2>/dev/null
  ALLOCATED_MEM=${ALLOCATED_MEM%.*}
  # echo $ALLOCATED_MEM
  USED_HEAP_MEMORY=$( (jstat -gc $PID 2>/dev/null || echo "0 0 0 0 0 0 0 0 0") | tail -n 1 | awk '{split($0,a," "); sum=a[3]+a[4]+a[6]+a[8]; print sum/1024}' ) 2>/dev/null
  USED_HEAP_MEMORY=${USED_HEAP_MEMORY%.*}

  EOS0S1OS_HEAP_CAP=$( (jstat -gc $PID 2>/dev/null || echo "0 0 0 0 0 0 0 0 0") | tail -n 1 | awk '{split($0,a," "); sum=a[1]+a[2]+a[5]+a[6]; print sum/1024}' ) 2>/dev/null
  EOS0S1OS_HEAP_CAP=${EOS0S1OS_HEAP_CAP%.*}

  printf "%-15s | %15d  | %16d  |  %11d |  %s \n" $DATEINFO $USED_HEAP_MEMORY $EOS0S1OS_HEAP_CAP $ALLOCATED_MEM $processinfo1
done;
```
