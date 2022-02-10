1. In databricks workspace create a python notebook.
2. We can use the magic %sql for executing queries.


#### Create database
```
%sql create database if not exists demo_db;
```

#### Use the Create Database for other commandlets
```
%sql use demo_db;
```

#### Creating the delta table using json schema
```py
import os
import json 
from pyspark.sql import functions as F, types as T

# the json schema should be created under the /DBFS/app/schema/json of Databricks
# either this can be uploaded or loaded using databricks-cli cp command

tbl_name_lst = [table.name for table in spark.catalog.listTables('demo_db')]
for file in os.listdir('/dbfs/FileStore/app/schema/json'):
  tbl_name = file.split(".json")[0]
  tbl_path = "/mnt/my-storage/my-app/deltalake/tables/{0}".format(tbl_name)
  if tbl_name not in tbl_name_lst:
    try:
      if not path_exists(table_path):
        with open(f"/dbfs/FileStore/app/schema/json/{tbl_name}.json", 'r') as f:
          # the file name of scheam will be the table name as well in this case
          tbl_schema = T.StructType.fromJson(json.loads(f.read()))
          tbl_dataframe = spark.createDataFrame([], tbl_schema)
          tbl_dfataframe.write.format("delta").save(tbl_path)
      # create table.
      spark.sql("CREATE TABLE {0} USING DELTA LOCATION '{1}'".format(tbl_name, tbl_path))
    except Exception as e:
      print(f"Error creating table {tbl_name}: {e}")
  else:
    print(f"Table {tbl_name} alread exists.")
```
