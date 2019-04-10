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
