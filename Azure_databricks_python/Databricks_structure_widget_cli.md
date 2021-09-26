
Databricks: 

 Workspace:
  - Dashboard
  - Notebooks (sequential execution, handle exceptions)
  - Libraries (third-party or locally-build code)
  - Data
  - Job
  - Experiments

 Data Management:
   - Databricks File System (DBFS) 
      - When creating a Databricks in Azure, we get this DBFS system automatically. 
      - A file system abstration over a blob store
   - Database:
      - Databricks has a default database
      - we can also create database
   -  Table
   - Metastore
       - The component that stores all the structured information of various tables and partitions in the database including column and column type information
       
 Cluster:
   - A set of computation resources and configuration (type of node) which you run notebooks and jobs
   - There are two types of structure
      - All purpose cluster 
      - Job Cluster
     - All purpose cluster:
          - this cluster can be created by UI, CLI or REST API
          - this can be manually terminated and restarted
          - multiple users can share such clusters
      - Job Cluster:
          - When you run a job a new job cluster will be created and termination once the job is complete
          - you cannot restart an job cluster
 Pool:
    - The instance Pools API allows to create, edit, and list instance pool. (intance - node or computer)
    - A set of idle, read-to-use instances that reduce cluster start and auto-auto scaling.
    - when attached to a pool, a cluster allocates its driver and worker nodes from the pool
    - if the pool does not have sufficient idle resources to accommodate the cluster's request, the pool expands by collecting new instances from the instance (cloud or azure) provider
    - When the attached cluster is terminated, the instance is returned to the pool and can be reused by different cluster
 Job:
   - A non-interactive mechanism for running a notebook or library either immediately or on scheduled basis
   

Databricks runtime
  - Databricks runtime
     - This includes Apacje Spark but also adds a number of components and updatd that improve usablitlity, and security.
  - Databricks runtime for Machine learning
     - provides ready-to-go environment for ML (incliding Tensorflow, Keras, PyTorch)
  - Databricks runtime for Genomics
  - Databricks Light
     - This provides a runtime options for jobs that don't need advanced performance, reliablity or autoscaling benefits provided by Databricks runtime


Search notebook:
   - use the search option on the left bottom.

cluster configuration
Spark, Tag, Logging, Init Scripts
Logging: To manage the cluster `logs` into DBFS file system we can set that in the Cluster creation section in UI.
tags: Also tags can be created if needed
init scripts:  script that is required to be executed before running the notebook, like importing modules

Worker node is going to process and send info to the Driver node.
 - If the worker node can hold 10 g and if we have worker node of 4 g and 3 nodes, the result sent back to Driver node will be 12 G, and result in out of memory.
 
####  Create a database and tables in databricks:
```
## using python code
  spark.sql("create database db_python)

## using scala code
%scala 
 spark.sql("create database db_scala)
 
## using sql code 
%sql
 create database db_sql
```

#### Load CSV file data into database as table (python)
   - upload the csv file to a location in the DBFS file store, using ui
```
## Step1: code to read the csv and load as dataformat

data_df=spark.read.format("csv").options("header",true).load("/FileStore/tables/data.csv")

## print the dataframe use show()
data_df.show()

## Step 2: code to load to table under db_python database

data_df.write.mode("overwrite").saveAsTable("db_python.python_data_table")

## note: mode (overrite | append)
##     - overwrite will overwrite if we rerun the command
##     - append will append the data to table if we rerun

## display the data from the table

spark.sql("select * from db_python.python_data_table").show()

## Note: show () by default loads few data, passing n will display n number of rows
```
 ##### Where to see the db data in the DBFS
   - The file will be stored under the Data section
   - The files will stored as `Parquet` format which is compressed format
   - The database data are persisted and will be available even when the cluster is terminated.
   
 #### How to view the tables 
 ```
 ### command 1
 %sql show tables from db_python
 
 ### command 2
 %sql use db_python
 
 ### command 3
 %sql show tables
 
 ### command 4 (using python )
 spark.sql("show tables from db_python").show()
 ```
    
 #### Creating a temp table and checking it in DBFS database
```
### command 5
data_df.registerTempTable("python_tbl_temp")

### command 6
spark.sql("show tables from db_python").show()
#### the above will result in a temptable column as true
#### also the temp table will be created under default database so no database name will be displayed
```
 ##### Note: In the command 6, if we detach the cluster and run the command, the temp table will not be displayed.
 
#### How to know from where the data was loaded?
 - Use the DAG (Directed Acyclic Graph) to check where the data is scanned based on the command result sectio and expanding the jobs

### how to partition the data when writing to the table
```
### command 7

## Storing the paritioned data in different 
data_df.write.mode("overwrite").partitionBy("column").saveAsTable("db_python_part.python_table2")

## usually in the data section, the partitioned data will be in separate folder, for example if the partition is based on year (columnname is year with values 2019,2020, etc)
## then folders like year=2019, year=2020, etc will be created
```

#### how to view the data from the parition directly from the DBFS
 - Say the requirment is to see data in a specific partitioned file

```
### command 8

### copy the file path from the data section at the bottom in UI
file_path="/user/hive/warehouse/db_python.df/python_table2/year=2019"

#### since the files are parquet file
spark.read.format("parquet").load(file_path).show()

#### note the result will not display the year column since this already a partitioned data

```
#### if we need to add a column to the data we can add that partitioned data as a column, using lit function and withColumn
```
### command 9
from pyspark.sql.functions import lit

file_path="/user/hive/warehouse/db_python.df/python_table2/year=2019"
df=spark.read.format("parquet").load(file_path)
df1=df.withColumn("year",lit("2019"))
df1.show()

##### The above will add year column with value 2019 in the result.
```

#### How to display the data of dataframe and plot charts in the notebooks
  - use display function
```
### command 10

display(df1)

## the result will have a section to display the with the chart section
```

#### How to drop database and tabes
```
### command 11
#### To drop the table from DBFS database created
spark.sql("drop table db_python.python_table")

### command 12
spark.sql("drop database db_python cascade)

### cascade is used since the database was not empty db
### form dropping empty database use "drop database dbname"
```

#### NOTE: when executing any command to writting to the database Spark execution uses optimization techinque internally called execution plan.
   - Hive Catalog operation 
   - The spark execution plan will be used.
   - using data -> table -> will display the information about table.
#### NOTE: Spark doesn't have Index concept, like in traditional sql database
#### Tables can be accessed, using CLI and REST, and also UI

#### Creating tables using notebooks
```
### python format where df is dataformat and we need to create table
#### create table with data from dataframe
df.wirte.saveAsTable("databasename.tablename")

### creating EMPTY table 
#### using python
spark.sql("create table table_name(id INT, name. STRING))

#### using scala 
%scala
spark.sql("create table table_name(id INT, name. STRING))

#### using sql
%sql
create table table_name(id INT, name. STRING)

### creating PARTITION table
#### python
df.write.partitionBy("columnname").format("parquet").saveAsTable("databasename.tablename")

#### scala
%scala
df.write.partitionBy("columnname").format("parquet").saveAsTable("databasename.tablename")


### how to append or overwrite partitions in tables

#### python
df.write.mode("append").partitionby("columnname").format("parquet").saveAsTable("databasename.tablename")

%scala
df.write.mode("append").partitionby("columnname").format("parquet").saveAsTable("databasename.tablename")

%sql
insert into databasename.tablename values ("value1","value2")
```
#### Parquet - stores the data in compressed, columnar way.
 
### Note: Dataframe are recommended only for structured data
          unstructured data is not recommended
    Dataframe, goes based on the schema

## How to create folders and file, from notebook. 
 - using the dbutils of databricks utility tool
 
### DBUTILS command:

##### Command to create directory in DBFS
##### command to create file and write data to it
##### command to display file content
##### command to list content in a directory
##### command to move files form one directory to another
##### command to remove file and directories
##### command to mount and unmount file system 
##### command to list donw mount 
##### command to refresh mount point
##### command to install packages
##### command to find current notebook path from UI
##### command to run one notebook to another 
##### command to exist notebook execution
##### command to list secret scopes
  - To read the data from keyvault using secret scopes.
  
### Commands for above operations
```
#### create a directory
dbutils.fs.mkdirs("/path/directoryname")

### note dbutils is library and fs is filesystem utils
### / is the default directory (we can use dbfs: as well in path)

#### create file and write data to it.
dbutils.fs.put("/path/filename.txt","content")
##### Example
dbutils.fs.put("dbfs:/FileStore/tables/demo.txt","hello world")

#### display file content
dbutils.fs.head("/path/filename.txt")

#### list files inf a directory
dbuitls.fs.ls("/path/")

#### move files from one directory to another
dbutils.fs.mv("/path1","/path2")

#### Copy file form one directory to another
dbutils.fs.cp("path1","path2")

#### remove file and directory 
##### file removal
dbutils.fs.rm("path/filename.txt)
##### directory removal
dbutils.fs.rm("path/",True)

#### mount and unmount file system
dbutils.fs.mount("mountpoint")
dbutils.fs.unmount("mountpoint")

#### list down mount
dbutils.fs.mounts()

#### refresh mount points (this can be used in case if we remount new filesystem and it didn't work, if we use below it should mount and refresh the filesystem)
dbutils.fs.refreshMounts()

#### installing packages
dbutils.library.installPyPI("tensorflow")
##### Example, use
dbutils.library.help()
dbutils.library.list()
dbutils.library.installPyPI("tensorflow")
##### NOTE: after installing mostly restartPython()
##### installing using dbutils, is this would be local notebook.
##### To install the library at the global we can install at the cluster level

#### find current notebook path from UI
##### below way we are using command, we can use the UI as well
dbutils.notebook.getContext.notebookPath
##### using scala
%scala
dubutils.notebook.getContext.notebookPath

#### run one notebook from another
##### without dbutils command ( $ used pass input)
%run pathofnotebook $argument1="value1" $argument2="value2"

##### with dbutils command  (pathofnotebook,timeout,inputparams)
dbutils.notebook.run("path",500,{arg1="value1","arg2":"val2"})

#### exit notebook execution
dbutils.notebook.exit("exit message like shutting down")

#### list secret scopes
dubutils.secrets.listScopes()
```

### NOTE: 
 - Using data -> DBFS -> we should be able to see default file path FileStore/
 - Uploading any file will be we have option to create a table with UI, which will default create  the table under /FileStore/tables . Use advanced option to select based on requirement.
 
 ### To list all the utilities in dbutils use help() function
 ```
 ### in notebook
 dbutils.help()
 ```
 
 ### To list all the options under fs,notebook, etc use below command
 ```
 dbutils.fs.help()
 ```

#### options to list the files with notebook
```
#### in command cell

%fs
ls /FileStore/tables/
``` 
 
### reading text file using dataframe
```
spark.read.text("dbfs:/FileStore/tables/file.txt").show()

## below can be used to dislay content with multiple lines, how is it read and displayed as list 
sc.textFile("dbfs:/FileStore/tables/file").collect()
```

### Databricks variable, Widget Types, Databricks notebook parameters
 - Variables assinging values
 - Widget Types: how to pass input to Databricks notebooks.
 - notebook paramter - tunging notebook settings.

#### What is widget and Types.
  - Input Widgets allow to add parameter to your notebook and dashboards. 
  - Widget API consits of calls to create various types of input widgets, remove them and get bound values.
  - Widgets are best suited if the notebook wanted to be re-executed with different paramters
  - Types of Widgets:
    - text - Input a value as text box
    - dropdown - Select a value from a list of provided values
    - combobox - Combinaton of text and dropdown. select provided value or input one in text box.
    - multiselect - Select one or more provided values from list.
    
- commonly used widget:
  - 1.Say the notbook is developed in generic way to handle full load and incremental load.
       - we need to pass the flag to diffrentiate the run is incremental or full load. say loadType
  - 2. We might also require process data, based on the date. which needs to be passed as input. say processDates
  - 3. We might need to pass the configuration file path to the notebook. This configuration file contains metadata like input path, etc. we might required to pass this configuration file path as well to notebook.

#### Create widget and set values
```
dbutils.widgets.text("x","1")
## another options
dbutils.widgets.text("sourcepath","none","Input Path")
```  

#### How to create widget to pass dynamic values to python notebook
```
dbutils.widgets.text("sourcepath","")
dbuitls.widgets.dropdown("dd_name","paymenttype",["payment","credit","debit","transaction"])

dbutils.widgets.combobox("cb_name","jobtype",["track","schedule","cancelled"])

dbutils.widgets.multiselect("ms_columns","transactionType",["credit","debit","failed"])
```
#### How to read dynamic parameter values in Python notebook 
  - How to read the above values in python
```
sourcelocation=dbutils.widgets.get("sourcepath")
```
 
#### How to remove widget from python notebook
```
dbutils.widgets.remove("name-of-widget")
### to remove all
dbutils.widgets.removeAll()
```
#### How to run notebook whenever we change the dynamic parameter values 

#### Widgets panel settings 


#### How to create widget to pass dynamic values to scala notebook
```
## note the use of Array()
dbutils.widgets.text("sourcepath","")
dbuitls.widgets.dropdown("dd_name","defaultvalue",Array("payment","credit","debit","transaction"))

dbutils.widgets.combobox("cb_name","defaultvalue",Array("track","schedule","cancelled"))

dbutils.widgets.multiselect("ms_columns","defaultvalue",Array("credit","debit","failed"))
```

#### How to read dynamic parameter values in Scala notebook 
   - var in scala it can change
   - val in scala is immutable
```
var sourcelocation=dbutils.widget.get("sourcepath")
```
#### How to remove widget from scala notebook
```
dbutils.widgets.removeAll()
dbutils.widgets.remove("widget-name")
```

#### To view the list of support utilities use 
```
dbutils.widgets.help()
```

## Example of using widget
```
### NOTEBOOK1 (say, calculateNotebook)
## cell 1
dbutils.widgets.text("a","0","input first number")

### cell 2
dbutils.widgets.text("b","0","input second number")

## cell 3
n1=dbutils.widgets.get("a")
n2=dbutils.widgets.get("b")
res=int(n1) + int(n2)
print(res)

```

```
## NOTEBOOK2 (invokeCalculate)

### cell 1
%run /User/mymail/path/calculateNotebook $a=10 $b=20

### executing shift + enter above cell will print 30
```

NOTE: In the community version, i see the Widgets updates quickly only when executed as Scala 

------------
## Databricks CLI

### Installing databricks 
  - Step1:  Install python
  - Step2: issue `$ pip install databricks-cli`
  - Step3: verify if it is installed 
     - issue `$ databricks`, this will list options
  - Step4: configure databricks to connect,
     - issue `$ databricks configure --host --token`
  - Step 5: In order list the cluster info and use command use below `$ databricks clusters -h`

### How to authenticate databricks cli
```
  $ databricks configure --host <host> --token <token>
   ## provide the databricks host: https://<databricks-name>.azuredatabricks.net/
   ## provide token
```
- NOTE 
   - Host:`To get the Azure databricks host copy the url`
   - Token: `To get the token in the User Setting, click on generate new token`
```
### How to create cluster
```
### To create cluster using cli
$ databricks clusters create --json "{\"culster_name":\"mycluster\",\"spark_version\":\"8.1.x-hls-scala2.12\",\"node_type_id\":\"Standard_D3_v2\",\"num_workers\":0}" 
```
### How to list the cluster
```
$ databricks clusters list

## to get specific cluster info
$ databricks clusters get --cluster-id "clusterid"
$ databricks clusters get --cluster-name "clustername"
```
### How to terminate cluster
```
$ databricks clusters delete --cluster-id "clsuterId"
```
### How to start cluster 
```
$ databricks clusters start --cluster-id "clusterId"
```
### How to restart cluster
```
$ databricks clusters restart --cluster-id "clusterId"
```
### How to check events
```
$ databricks clusters events --cluster-id "clusterId"
## like which user started stopped the cluster, etc.
```

### How to permanently delete the cluster
```
$ databricks cluster permanent-delete --cluster-id "clusterId"
## removes the cluster from the workspace
```
### How to get list of notebook in workspace/folder
```
$ databricks workspace -h
$ databricks workspace list

## to list the info from the folder
$ databricks workspace list /Users/

## to create folder using cli
$ databricks workspace mkdirs /cli-folder
```
### How to import notebook into databricks from local
```
$ databricks workspace import -l python C:/localpath/test.py /Users/cli-folder

## -l is language python | scala | R | sql
## C:/... is the local where file is present
## /User/cli-folder to which path to import 

### Import and overwrite the file if it already exists
$ databricks workspace import -o -l python C:/localpath/test.py /Users/cli-folder

## -o used for overwrite

### To import all the file ins the folder
$ databricks workspace import_dir  C:/localpath/test.py /Users/cli-folder

```
### How to export notebook
```
$ databricks workspace export /Users/cli-folder/test.py C:/localpath/

## /Users/cli... the path in databricks to export locally
## c:/localpath/ where the exported files to be saved

### To expot all files under dir
$ databricks workspace export_dir /Users/cli-folder C:/localpath/

```
### How to remove notebook
```
$ databricks workspace rm /Users/cli-folder/test.py

$ databricks workspace rm /Users/cli-folder -r 

## -r for recursive
```
### How to create folders
```
$ databricks workspace mkdir /Users/cli-folder
```

## using Databricks CLI, for below points 
### How to get list of folders in dbfs
```
$ databricks fs ls 

# to list files in specific directory 
$ databricks fs ls dbfs:/temp
```
### how to create folder in dbfs
```
$ databricks fs mkdirs dbfs:/cli-folder
```
### how to copy files from local to dbfs
```
$ databricks fs cp C:/localpath/temp.txt dbfs:/cli-folder
$ databricks fs ls dbfs:/cli-folder  
```
### To list the content of the file 
```
$ databricks fs cat dbfs:/cli-folder/temp.txt
```
### how to move and remove files and folders 
```
$ databricks fs mv dbfs:/cli-folder/temp.txt dbfs:/cli1/temp.txt

$ databricks fs rm dbfs:/cli-folder/temp.text
## to delete directory
$ databricks fs rm dbfs:/cli-folder/ -r 
```
### how to install libraries
```
$ databricks libraries install --cluster-id "clusterId" --pypi-package "tensorflow"
$ databricks libraries install --cluster-id "clusterId" --pypi-package "pyodbc"

```
### how to uninstall libraries
```
$ databricks libraries uninstall --cluster-id "clusterId" --pypi-package "tensorflow"
$ databricks libraries uninstall --cluster-id "clusterId" --pypi-package "pyodbc"
```

### List all the libraries installed on all the cluster
```
## Below lists the libraries installed 
$ databricks libraries list

## Below lists all the libraries installed in all cluster with the STATUS of installation
$ databricks libraries all-cluster-statuses
```
### how to create and run job
```
$ databricks jobs create --json-file jobs-config.json
## above command will provide a job id, we need to run the job we issue below command

$ databricks jobs run-now --job-id 400
```
- sample jobs-config.json
```json
{
"name": "job1",
"new_cluster" : {
  "spark_version":"8.1.x-scala2.12",
  "node_type_id":"Standard_D3_v2",
  "num_workers":0},
  "timeout_seconds":4800,
  "max_retries":2,
  "notebook_task":{
    "notebook_path":"/Users/myemail/jobs",
    "base_parameters" :{
         "name" : "username-info"
      }
   }
}
```
