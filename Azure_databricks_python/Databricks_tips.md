### To Read a content of the file from the DBFS file system, when the path is know use below command in the notebook
```
dbutils.fs.head("dbfs:/FileStore/json/schema/file.json");
```
