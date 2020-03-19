
```unix
#!/bin/sh
exe_date=`date +%m%d%y%H%M%S`

THISPROCESS=$0
echo "Starting this $THISPROCESS ..."

LOGFILE=/path/to/log/logfilename_${exe_date}.log

# input to query in case if needed
QUERY_INPUT=$1
if [ -z $QUERY_INPUT ] 
then
   # just put in default value if there is no input
   QUERY_INPUT=0 
fi

DB_CONNECTION_STRING=<put_in_thick_client_URL>

result=`sqlplus -S ${DB_CONNECTION_STRING} << EOF
              SET PAGES 0 lines 500
              @ /path/to/sql/scripts/script-to-execute.sql ${LOGFILE} ${QUERY_INPUT};
              EOF`
echo $result
```

```sql
SET SERVEROUTPUT ON SIZE UNLIMITED
SET HEADING OFF VERIFY OFF FEEDBACK OFF ECHO OFF

rem -----------
rem COMMENTS goes here
rem -----------

-- arguments passed from the shell script sql execution
SPOOL &1

DECLARE
    COUNT NUMBER :=0;
    QUERY_INPUT NUMBER := &2; -- input from shell script
BEGIN
    DBMS_OUTPUT.PUT_LINE('STARTED, Time : ' || TO_CHAR(SYSDATE, 'DD-MON-YYYY HH24:MI:SS');
    
    FOR index IN 1 .. QUERY_INPUT 
    LOOP
      COUNT := COUNT+1;
    END LOOP;
    -- // THERE CAN BE DIFFERENT SCRIPTS HERE OPENING CURSOR PERFORMING 
    -- // DELETION ACTIVITY, THEN BELOW EXCEPTION WILL BE THROWING EXCEPTION
 EXCEPTION
      WHEN OTHERS
      THEN  
      IF SQLCODE = -24381
      THEN
         FOR cnt IN 1 .. SQL%BULK_EXCEPTIONS.COUNT
         LOOP
            DBMS_OUTPUT.put_line (SQL%BULK_EXCEPTIONS (indx).ERROR_INDEX || ': ' || SQL%BULK_EXCEPTIONS (indx).ERROR_CODE);
         END LOOP;
      ELSE
         RAISE;
      END IF; 
END;
/

SET ECHO ON FEEDBACK ON
SHOW ERRORS
SPOOL OFF
```
