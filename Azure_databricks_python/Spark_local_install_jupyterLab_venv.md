Spark: INCLUDING local installation via PySpark

  PySpark is a python API for spark
  
  spark is not a programming language. it is a library that can be used to write programming
  
  pyspark allows you to write phyton based data processing applications that execute on distributed cluster in parallel
  
  Apache Spark is an anlytical processing enging for large powerful distributed data processing and also machine learning applications.
  
  Apache spark architecture:
  
   Apache Spark works on Master - Slave architecture
    Where master node is referred as "Driver"
    The slave nodes are referred as "Worker"
    
   When running a application,
   a Spark session creates a Spark context which is the entry point 
   
   Resources on worker nodes are managed by Cluster Manager
   
```

Spark Application
   Spark Driver                                          Worker Node1 (Spark Executor)
   spark session    --------   Master Node      ------   Worker Node2 (Spark Executor)
   spark context                Cluster Manager
     
```

 The Resource and Cluster Manager 
 
 companies relies on a resource management system such as Apache YARN, Mesos or in Spark standalone mode.
 
 Two main components of resource manager are cluster manager and worker 
 
 Cluster manager knows where the workers are located, how much memory and cpu each worker has
 
 When running a spark program, the cluster manager determines how many worker nodes will be required, how much memory(RAM) and cpu required on each worker node to run the program
 
 The Spark driver
  - this is central coordinator of Spark application
  - this interacts with the cluster manager, to figure out which machine to run the program logic on.
  - this requests the cluster manager to launch a spark executor to do the processing on worker nodes
  - this creates a Spark session (which created spark context) which then executes Spark configuraton to allocate required API's for your application (API - allocation of required tools)
  
The Spark Executor:
  - is a process, which does the actual logic on the worker nodes.
  

unified Stack:

The spark unified stack is built on top of a Spark Core.

The Spark core, provides all necessary functionality o manage and run distributed applications such as scheduling and fault tolerance.

```
Spark SQL     Spark Streaming               MLib      GraphX
               (real-time process)
               
         Spark core
     
     Standalone Scheduler    YARN    Mesos
```

Spark can work with data formats such as CSV,JSON, Avro, ORC, Parquet.
Can read data into dataframe, using pure sql syntax

Spark streaming: 
  ablitiy to process real-time data, data can be injected from Apache kafka, flume, twitter, etc.

-------------
Installing Spark:
Step 1:
  - Install the Java (latest java jdk 15 from oracle) 
  - Set the JAVA_HOME environment variable, so the SPARK can identify java and include it in path (if needed)
Step 2:
  - Spark has a dependency to hadoop.
     - It is difficult to setup the hadoop.
     - we can trick Spark, that we have a hadoop cluster is installed and available.
     - for Windows platform, download github (steveloughran/winutils -> readme - status)
     - select the appropriate hadoop version, mostly latest 3.2.1 (cdarlint/winutils)
     - download the exe 
     - create the hadoop/bin and copy paste the winutil.exe
     - The above is a mock hadoop
   - We need to set the spark the hadoop path variable, by adding the environment variables.
   - Create a new environment variables, HADOOP_HOME (value path to the winutils.exe location) also add it to path.
   
Step 3:
   - Python installation as main development language
   - make sure the path variable to environment variable
   - in order to exist the python shell type `exit()`
   
Step 4:
   - The python flavour of Spark, PySpark. 
   - PySpark is easy to install using pip (python package manager)
   - PySpark - is the spark used to work with python language.
   - from the command prompt
      > pip install pyspark
      
Step 4.1:

   After installing the pyspark as above - the application didn't open up the spark.
   
   1. Additionally download https://spark.apache.org/download.html (select stable vesion, Pre-built for Hadoop) 
   1.1 Downloaded is a tgz, use 7Zp to extract the tar and from there extract the files to any location (C:/spark/spark-3.2.1-hadoop-3.1/)
   2. added python path PYTHON_HOME = C:/python39 environment variable
   3. Added the winutils ((C:/spark/spark-3.2.1-hadoop-3.1/hadoop/bin)
   4. Add SPARK_HOME = C:/spark/spark-3.2.1-hadoop-3.1/
   5. Add HADOOP_HOME=C:/spark/spark-3.2.1-hadoop-3.1/hadoop
   6. in path add %HADOOP_HOME%\bin

After above steps the spark setup was compelted.

To test the pyspark

```
>>> big_list = range(1000)

## To really say the spark is installed
## to use Spark specific code RDD (resilient distributed dataset), 
## RDD - It refers to data divided into logical partition and computed in different nodes.
## in our case it will be one node.

## lets create RDD using parallelize method 
## from the spark context, and break the data into 
## two partition

# sc - spark context (entry point to program)
>>> rdd = sc.parallelize(big_list,2)

## pyspark shell is a python shell, where the spark library imported to it.

## use rdd filter method to hold odd numbers into a variable
## using a python lambda functionality

>>> odds = rdd.filter(lambda x:x %2 != 0)
## The lamda will loop through every value from the RDD list and assign each number to x

## print the first 5 number in the odds rdd
>>> odds.take(5)

## The above method take (5) was throwing error 
python command not able to find.

## For fixing the above take command issue, find where the python.exe is installed and set that path by creating a file spark-env.cmd (for windows) under the 
C:/spark/spark-3.2.1-hdoop3.1/conf/

set PYSPARK_PYTHON=C:\Users\thirumurthi\AppData\Local\Programs\Python\Python39\python.exe
```  
  
#### Jupyter Note book installation

- 3.9 jupyter needs Microsoft C++ build tools .
https://visualstudio.microsoft.com/visual-cpp-build-tools/
 
- set the path of the Python and python/scripts where pip exe exists in environment path variable
C:\Users\thirumurthi\AppData\Local\Programs\Python\Python39\Scripts

- issue 
```
  > pip install jupyter
  # navigate to a folder
  > jupyter notebook
  
  # the browser will load the jupyter note book automatically
```

 - The jupyter notebook runs outside the spark.
 - To use spark in Jupyter notebook we need to import pyspark. since it doesn't comes automatically.
 
 in jupyter notebook cell 
 ```
 import pyspark
 
 sc = pyspark.SparkContext('local[*]')
 
 ## spark context is entry point to spark program.
 ## this spark context allows to connect to cluster and create RDD's
 ## local[*] => means running in the local with single machine node; * - means to create as many worker threads as needed for logical units
 ```
 
 if the jupyter notebook doesn't recognize pyspark
 in jupyter notebook
 
```
issue ->  > pip install findspark

# below code should be first to import pyspark
import findspark
findspark.init('C:\spark\spark-3.1.2-bin-hadoop3.2')

import pyspark
blist = range(1000)
sc = pyspark.SparkContext('local[*]')
rdd= sc.parallelize(blist,2)
odds = rdd.filter(lambda x:x%2 != 0)
odds.take(4)
 ```
 
 #### Spark Web ui
  - apache spark provides the spark web ui to moniotr and track the process
  
  1. load the pyspark shell in command prompt
  
  Below is a simple application which computes square for the list of number passed 
  
  we will user RDD Map function to transform the data
  we also need to use function map transformation, which is used for complex oparation.
  
# Declar a variable to hold list

>>> nums_list = [1,2,3,4]
>>> rdd = sc.parallelize(nums_list)
### create a variable to hold lamda 

>>> squared = rdd.map(lambda x: x*x).collect()

#### create the function
### print tab
>>> for num in squared:  ### press enter
        print(num)    ### press tab first to indent
                      ### press enter twice to print output

To find the data type 
>>> type(rdd)   ## this displays the data type of the rdd

#### to launch the Spark ui, DON'T stop the pyspark shell.
  " http://localshot:4040"

this ui opens the ui.

The UI has different tabs:
   jobs, stage, etc.
the application name is default with PySparkShell

When we write application, we need to create our own application name. Each program has its own assigned URL

If we are running more than one job, then the port number will be localhost:4040, localhost:4041, 4042..

Scheduling mode:

three scheduling mode:
   - standalone mode (FIFO)
   - YARN mode  ( this makes sense of large cluster)
   - Mesos mode ( this makes sense of large cluster)

A spark job is equal to the number of actions in the application. like above `squared = rdd.map(lambda x:x *x ).collect()` - this is one job where we map and collect

Each spark JOB should have one or more STAGE.
spark stages are created based on what operations can be performed serially or in parallel.

STAGE represents a UNIT OF WORK
Each STAGE can have one or more TASK

Example stages:
  Connecting to a database is one stage
  another stage is shuffling the data

understanding the Event timeline in spark ui

I provides an indication of the application flow over a period of time.
  like, when the executor service was instantiated.
   also displays when collect action was called.
   
 The time line represents the Spark session, since we didn't close the spark pyspark shell this displays the info.
 
 #### Stages:
 
 DAG visualization -> Directed Acyclic Graph, which represent Spark execution methodology.
 
 Aggregated metrics by Executor.
 
 Spark Stage can create one or more Tasks, and each task is a unit of execution. 
 Each task is fedarated to Spark executor
 A task maps to a CPU core and is exected by the Executor.
 
 Since we had the single machine (FIFO) mode, and my machine had 4 cores, the task listed 4 items. That is execute 4 or more task in parallel.
 
 The Executor tab, confirms the number of executors, which displays Compeleted tasks
 
 ===================
 
 Spark concepts:
 
  - 1. Create a new directory where we want the spark data to be placed.
  - 2. open the Command prompt, and navigate there
  - 3. create a new directory for salesdata (C:/thiru/learn/python/spark-program/salesdata)
  - data is at https://github.com/PacktPublishing/Apache-Spark-3-for-Data-Engineering-and-Analytics-with-Python-/tree/main/Section%202%20Resources
  
  Navigate to the directory where the csv file is placed.
  
  - Open up the jupyter notebook using below command
    > jupyter notebook  

  - Now create a new notebook.
  - import the pyspark (include the findspark and init)
  
```
from pyspark.sql import SparkSession
# using the count functon of pyspark sql
from pyspark.sql.functions import count

# Create a spark session 
spark = (SparkSession.builder.appName("TotalOrdersPerRegion").getOrCreate())


## There can be only one spark session per JVM. PySpark compiles to Java Byte code

## Spark required Java instance to operate.

## the sparksession object is used to get or create new spark session.
## we need to call the SparkSession class builder inner class 
## this class will give access to appName function which take a parameter appname, this is displayed in the spark ui 

type(spark)
```

### The spark Driver program is the core 

## How spark executes the application
  - load the sales data to variables
  - spark session to load the data from csv
```
sales_file = "sales_records.csv"

# tell the file is a csv
sales_df= (spark.read.format("csv").option("header","true").option("inferSchema","true").load(sales_file))

# sales_df is a dataframe.
# data frame is a distributed collection of data organized into rows and columns
# its like a table with rows and columns in memory
# when we load the data into the dataframe we also need to infer the schem
# schema is a structure that describes how the data is organized
# csv file has a header info represent each column data sperated by comma
# this allows to individal data

sales_df.select("Region","Country","Order ID").show(n=10,truncate=False)
# n = in show displays 10 records, and truncate =false tells not to truncate rows

```

- performing aggregation
```
count_sales_df= (sales_df.select("Region","Country","Order ID")
.groupBy("Region","Country").agg(count("Order Id").alias("Total Orders"))
.orderBy("Total Orders", ascending=False))

count_sales_df.show(n=10,truncate=False)
print("Total rows = ",(count_sales_df.count()))
```
### Spark tranformation

- Two types of transformation
  - Narrow 
      - where the output is computed from a single input partition 
      - which mean data distributed accross partitions
       - say partition1 has 1-100 records
       - second partition 101-150 records
       - single input referes to each partition and calculation or perhaps data operation can be carried out without partition being exchanged or shuffle data
       Example: filter() is a narrow transformation.
  - Wide
      Example: groupBy and orderby is wide since it has to be shuffled.
      
      Shuffling: 
        - means the action to combine the data is arranged to a new partition.
        - this data come from affected partition and move around the cluster to a new partition
 
 Transformation always result in a new Dataframe.
 
 underneath the Dataframe is RDD, therefore action or data operation doesn't end up in new RDD.
 
### DAG (directed Acyclic graph)

- The spark has a lazy evaluation means that execution will not start untl an action is triggered. Transformation are lazy in nature. That is when we call some operation on an RDD, it does not execute immediately

The below code will not be executed until, the count_sales_df.show() is action called.

count_sales_df= (sales_df.select("Region","Country","Order ID")
.groupBy("Region","Country").agg(count("Order Id").alias("Total Orders"))
.orderBy("Total Orders", ascending=False))

-----

RDD (Resilient Distributed Datasets)

RDD API was released in 2011

RDD is an immutable and reslient distributed collection of elements of your data, that is partitioned accross nodes in your cluster

```

   RDD
   
   item1  item2 item3
   item4  item5 item6
   .../    |       \
     /      |        \
    /        |        \
 Worker    worker      worker 
 Spark      spark       spark
 executor   executor    executore
```

RDD operations are typically referred to as Transformation and Actions, and operations are executed in parallel.

RDDs are fault-tolerant 

RDD is low level api.

Why RDD?
- to handle unstructured data that cannot be handed by structured API's such as Dataframe and Datasets. unstructured data such as streams or streams of text.

want to optimize spark application using low level API.

### Core difference between Dataframes vs RDD is that RDD's manipulate Row java object to spark types which are mainly data types.

```
 import findspark
findspark.init('C:\spark\spark-3.1.2-bin-hadoop3.2')

from pyspark.sql import SparkSession
spark = (SparkSession.builder.appName("TestRDDs").getOrCreate())

# RDD's are lazy transformation
words_list = "spark makes life a lot easier and put me into sprits. spark is too awesome".split()
# split sentence as words will be stored in words_list

type(words_list)

# create RDD for words
words_rdd = spark.sparkContext.parallelize(words_list)

# to print the values in the rdd, iterate the list and print it 

words_data = words_rdd.collect()

for word in words_data:
    print(word)
```

### using transformation on rdd 
 - distinct() and filter()
 
```
# remove the duplicate spark word

words_rdd.count()
words_rdd.distinct().count()


# Transformation always end up in new RDD, so the duplicte is not removed

words_unique_rdd = words_rdd.distinct()
for word in words_unique_rdd.collect():
    print(word)
    
# to filter transformation to print words startign with S

def wordStartsWith(word,letter):
    return word.startswith(leter)
    
# use anoymous function is lambda
words_rdd.filter(lambda word: wordStartsWith(word,"s")).collect()

# displays the output in notepad
```
- Map and flatMap transformation
   - Map transformation is used for adding a new column, updating another column or transform data
   - the transformed output will have the same number of records

```
# calculate the numbers square
# * at the begining is to unpack the range function
num_list = [*range(1,21)]

# creating a tuple using ()
nums_squared_rdd = num_rdd.map(lambda n: (n,n*n))

for num in nums_squared_rdd.collect():
    print(num)

# example word tranformation using map    
words_transform_rdd = words_rdd.map(lambda word: (word, word[0], wordStartsWith(word,"s")))

for word in words_transform_rdd.collect():
    print (word)
    
# flatmap is extension of map
# example take a word and print letters

# printing only 10 letters 
words_rdd.flatMap(lambda word: list(word)).take(10)
```
### SortByKey Transformation
 - This requires a key value pair 
 - So we can create a tuple
```
countries_list = [("India",91),("USA",4),("Greece",13)]
countries_rdd = spark.sparkContext.parallelize(countries_list)
# create a list of sorted countries
srtd_countries_list = countries_rdd.sortByKey().collect()
for country in srtd_countries_list:
    print(country)

# sorting by descending order of ranking (value is ranking)
# c in lambda will be a tuple, and using the c[1] as key we sort on it
# False set for descending order
srtd_countries_list = countries_rdd.map(lambda c:(c[1],c[0])).sortByKey(False).collect()

for country in srtd_countries_list:
    print(country)
```

# RDD actions
below are example action :
collect
count
take

reduce - used to reduce an RDD. Aggregate the values into one values.

```
nlist = [1,5,2,3,4]
# create function to summ all the values.
result = spark.sparkContext.parallelize(nlist).reduce(lambda x,y : x+y)
print(result)
```

Above is similar to below 
```
def sumList(x,y):
    print(x,y)
    return x+y

result = spark.sparkContext.parallelize(nlist).reduce(lambda x,y : sumList(x,y))
print(result)
```
```
def wordLengthReducer(leftWord,rightWord):
    if len(leftWord) > len(rightWord):
        return leftWord
    else:
        return rightWord
        
# prints the longest word in the list.
words_rdd.reduce(wordLengthReducer)

words_rdd.first()

spark.sparkContext.parallelize(range(1,21)).max()
```

## Spark Structured API's

```
                                         Untyped APIs (Data Frame)
DataFrame
               ------>  Structured     
                          APIs            Typed APIs DataSet[T]
DataSet  
```
 Some thing better than using RDD
 
 Simple RDD program to compute average age of person 
 
 ```
 # compute the average ages 
# this is difficult understanding, so we use structured data
agesRDD = (dataRDD
          .map(lambda x: (x[0],(x[1],1)))
          .reduceByKey(lambda x, y: (x[0]+y[0], x[1] +y[1]))
          .map(lambda x: (x[0], x[1][0]/x[1][1])))
```
 above is hard to understand, this is one of the reason structred API was introduced.
 
 
 Dataset API does not have support for Python (only available in Scala/java)
 
 The dataset API is a collection of strongly typed JVM data objects in Scala and classes in java
 
 TYPED Ojbects enforces data type: (java)
 i.e. float avg_age = 10.0;
 avg_age="some string" // causes error
 
 UNTYPED object does NOT enforce data type:
 i.e.  avg_age = 10.0
 avg_age= "some String" // this still works
Python won't complaints.

From above we can see Scala and Java can be preffered over python in case where data integrity is highest priority.

Spark supports DataFrame which is an Untyped API without the use of dataset, at least we can assign a schem to enforce data integrity. That has support to both python and scala.

```
spark = (SparkSession.builder.appName('AvgAges').getOrCreate())

data_df = spark.createDataFrame([("Linda",20),("Don",19)],["name","age"])

avg_df = data_df.groupby("name").agg(avg("age"))
avg_df.show()
```

The DATAFRAME accounts for readable code

The dataframe allows to have structured presentation of data 

The DataFrame allows to assign schem to solve the data integrity problem.
-------------
### Below is the ways to create a python project
### Creating a Python environment 

```
# create a directory for the project

# issue command to create a environment
> python -m venv venv/

## the above will create a virtual environment, and creates a folder venv

## To activate the environment in windows use below command.

> venv\Scripts\activate
 ## after running above command the command prompt will start wiht (venv)
 ## note in the venv, we are not using the global python library confirm with $ pip list command
 
# for mac/linux use $ source venv/bin/activate
```
-
in the new virtual environment we can install pyspark, using pip. 
- we can use wheel (allow as pip to modern way of installing package)

```
(venv)> pip install wheel
(venv)> pip install pyspark
(venv)> pip install pandas
# visualization tool
(venv)> pip install seaborn
# to install editor
(venv)> pip install jupyterlab

# after installing above 
(venv)> jupyter lab
```

Note: in venv, the df.show() was not working. within the venv/Scripts folder, copy the python.exe and create a python3.exe
-- Lets create some dataframes and work in the venv 

```
from pyspark.sql import SparkSession
from pyspark.sql.types import StructType,StructField,StringType,IntegerType

# create session
spark= (SparkSession.builder.appName("PysparkDfPractice").getOrCreate())
```
To know more about the data types referes
https://spark.apache.org/docs/latest/sql-ref-datatypes.html

### Spark SQL is a module for working with structured data, and Spark SQL types are used to describe the structure of the data.

## To work with data we need to add some kind of structure
## that structre is a schema and data types.

- we are going to use StructType and StructField to create the schema, for each column we create data type.

```
data = [ ("James","","Smith","36636","M",3000),
          ("Michael","R","", "40288","M",4000)]
          
type(data)  #list

# define a schema 
schmea = StructType([
    StructField("firstname",StringType(),True),
    StructField("middlename",StringType(),True),
    StructField("lastname",StringType(),True),
    StructField("id",StringType(),True),
    StructField("gender",StringType(),True),
    StructField("salary",IntegerType(),True)
    ])
    
# Create a dataframe and assign a variable

df = spark.createDataFrame(data=data,schema=schmea)

# print the schema name
df.printSchema()

prints below as out put 
root
 |-- firstname: string (nullable = true)
 |-- middlename: string (nullable = true)
 |-- lastname: string (nullable = true)
 |-- id: string (nullable = true)
 |-- gender: string (nullable = true)
 |-- salary: integer (nullable = true)

# To show the data from the data frame
df.show(truncate=False)
```
- underneath the Dataframe RDD is used

### Dataframe reader and writer
 - reader and writer are built-in within the API
 - The file refered to big data file.
 
```
# to upload the csv file
# set file path to variable

file_path="./path/of/csv.csv"

example_df = (spark.read.format("csv")
       .option("header",True)
       .option("inferSchema",True)
       .load(file_path))

# Below is called projection in spark
example_df.select("cloumn1","column2").show(10)

# to print the schema
example.printSchema()

# to print the list of columns
exmaple_df.cloumns

## To write the dataframe to hte Parquet file

output_path = './path/of/parquet'
example_df.write.format("parquet").mode("overwrite").save(output_path)
```
# working with structured Operations
## Manainging Performance Errors
  - When running single node, it requires temporary space.
  - since there is no real no cluster, there is no proper clean up.
  - there might be disk lock error.
  - once the spark restart instance.
  - "Disk block manager error" - may cause due to spark variable. to stop the spark session use 
  ```
  spark.stop() # this will stop and fix the block manger error.
  ```
## Read from JSON

```
from pyspark.sql.types import ArrayType, FloatType, DateType, BooleanType

### create a schema
persons_schema = StructType([
    StructField("id", IntegerType(),True),
    StructField("fname", StringType(),True),
    StructField("lname", StringType(),True),
    StructField("fav_movies", ArrayType(),True),
    StructField("date_of_birth", DateType(),True),
    StructField("active", BooleanType(),True),
    StructField("salary", FloatType(),True)
])

## read the json file from the data frame

json_file_path = './path/of/json/file.json';
persons_df = (spark.read_json(json_file_path, persons_schema, multLine="True"))

## print the schema to check it matches our declaration
persons_df.printSchema()

## to display the first 10 records
persons_df.show(10)

## if the rows are truncated use 
persons_df.show(10,truncated=False)
```

## Columns and Expressions
- There are lots of functions for data frame for columns and expression

```
from pyspark.sql.functions import col,expr

## use the persons_df above
persons_df.select(col("fname"),col("lname"),col("date_of_birth")).show(5)

### the col function show particular used to display particular column in the data frame
### the same can be done using expr
persons_df.select(expr("fname"),expr("lname"),expr("date_of_birth")).show(5)

## import sql function to concatenate

from pyspark.sql.functions import concat_ws

## using col function
(persons_df.select(concat_ws(' ', col("fname"),col("lname)).alias("full name"),
col("salary"),
(col("salary")*0.10 + col("salary")).alias("salar_increase"))).show(10)

## using expression, you can assign expression in the string 
(persons_df.select(concat_ws(' ', col("fname"),col("lname)).alias("full name"),
col("salary"),
expr("salary * 0.10 + salary").alias("salar_increase"))).show(10)
```

## Filter and Where conditions

```
## to use with the build in function Not need to import filter from the pyspark functions

## We didn't use the select, filter infers the all the column
persons_df.filter('salary < = 3000').show(10) 

## filter and where are same, the above can be used like below
persons_df.where('salary < = 3000').show(10) 


## using and operator

persons_df.where((col("salary") <= 3000) & (col("active") == True)).show(10) 

## filter with multiple conditions including birth year 

from pyspark.sql.functions import year

persons_df.filter((year("date_of_birth") == 2000) | 
         (year("date_of_birth") == 1999)).show()

## to use array contains

from pyspark.sql.functions import array_contains

persons_df.where(array_contains(persons_df.fav_movies,"name-of-the-move-to-serach")).show()

```

## Distinct, Drop Duplicates, order by

```
## import sql function required 
from pyspark.sql.functions import count,desc

## to get unique set of indicators
## active is boolean and has true or false
persons_df.select("active").show(10)

# to make the unique to list only true and false two set
persons_df.select("active").distinct().show()

# dropping duplicate values 

(persons_df.select(col("fname"),year(col("date_of_birth")).alias("year"),
col("active")).orderBy("year","fname")).show(10)

## the above might be requirement to drop records with duplicate on two columns, in the case year and active

dropped_df = (persons_df.select(col("fname"),
year(col("date_of_birth")).alias("year"),
col("active")).dropDuplicates(["year","active"])).orderBy("year","fname")

## how to order the data based on year in descending order
(persons_df.select(col("fname"),year(col("date_of_birth")).alias("year"),
col("active")).orderBy("year",ascending=False)).show(10)

```

## Rows and Union

### create individual row items and package them to their own data frame and then combine 

```
from pyspark.sql import Row

person_row = Row(101,"Robert","Tim",["MI1","MI2"],4200.00,"1976-01-01",True)

# to create more number
persons_rows_list = [Row(101,"Bob","Mark",["MI1","MI2"],4200.00,"1976-01-01",True),
Row(102,"Kim","Jang",["MI1","MI2"],4200.00,"1976-01-01",True),
Row(101,"Willy","Jame",["MI1","MI2"],4200.00,"1976-01-01",True)]

## lets append the person row list with pers_row

persons_rows_list.append(person_row)

print(presons_rows_list)

## Accessing the rows from the list, below display fname
person_row[1]

## by passing the heading to the list, the spark will create a schema

new_persons_df = spark.createDataFrame(persons_rows_list, ["id","fname","lname","fav_movies","salary","date_of_birth","active"])

## with the above we can create a union operation
## persons_df was from the json file
add_persons_df = persons_df.union(new_persons_df)
add_persons_df.sort(desc("id")).show(10)
```

## Adding, Renaming, Dropping columns

#### how to add add and remove columsn
```
from pyspark.sql.functions import round

augmented_persons_df = persons_df.withColumn("salary_increase",expr("salary * 0.10 + salary))

augmented_persons_df.show(10)

# to print the list of columns in the new list, lists the heading
augmented_persons_df.columns

augmented_persons_df2 = (augmented_persons_df
       .withColumn("birth_year", year("date_of_birth")),
       .withColumnRenamed("fav_movies","movies")
       .withColumn("salary_x10",round(col("salary_increase"),2))
       .drop("salary_increase))

# below list the dataframe with birth_year, renamed fav_movies,and coloum salary_x10 rounded, dropping salary_increase

augmented_persons_df2.show(10)

```

## Working with Missing or Bad data (null or empty values)

```
bad_movies_list = [Row(None, None, None),
  Row(None, None, 2020),
  Row("John Doe", "Awesome Movie", None),
  Row(None, "Awesome Movie", 2021),
  Row("Mary Jane", None, 2019),
  Row("Vikter Duplaix", "Not another teen movie", 2001)]

bad_movies_columns = ["actor_name", "movie_title","produced_year"]

# create the dataframe for list with schema
bad_movies_df = spark.createDataFrame(bad_movies_list,schema=bad_movies_columns)

# there are null values , to drop any null values in the rows. na_drop() is spark function to drop rows
# na_drop with no parameter, will drop with row

# we can also pass parameter any
bad_movies_df.na_drop().show()

# the above can also be written as below
bad_movies_df.na_drop("any").show ()

# if we want to drop only entire row contains null 
# only when all the rows are null will be dropped
bad_movies_df.na_drop("all").show()

# we might need to filter dataframe on specific column

bad_movies_df.filter(col("actor_name").isNull() != True).show()

# we need records that didn't have actor name or null in it.
bad_movies_df.filter(col("actor_name").isNull() != False).show()

# summary of the data value using descirbe
# statistics of the columns which holds int or string value
bad_movies_df.describe("produced_year").show()
```

## User-Defined Functions (create in python and convert how spark can understand)
 - we cannot perform data transformation using python code.
 - we only use pyspark sql function to transform
 - we might need to write user defined function in order to apply transformation
 - how to create a spark user defined function

```
# import function to creare userdefined function

from pyspark.sql.function import udf

students_list = [("John",80).
("Jame",87),
("Rosy",90),
("Tim",78)]

students_column = ["name","score"]

students_df = spark.createDataFrame(students_list, schema=students_column)

students_df.show()

# python function 

def letterGrade(score:int):
    grade = ''
    if score > 100:
       grade = "Over"
    elif score >= 90:
       grade = "A"
    elif score < 89"
       grade = "B"
    return grade

# verify the function above
print(letterGrade(90))

# convert the lettergrade function to spark defined function

letterGradeUDF = udf(letterGrade)

# to use the function 

students_df.select("name","score", letterGradeUDF(col("grade")).alias("grade")).show()

``` 
 NOTE: Spark User defined function (UDF) are very poorly optimized, try to avoid using them as much as possible.

## Aggregation (grouping of data)
 - sum, max , avg , count, etc is provided by spark
 
```
flight_file='./path/to/csv/file.csv'
flight_summary_df = (spark.read.format("csv")
                    .option("header","true")
                    .option("inferSchema","true")
                    .load(flight_file))

flight_summary_df.count()

flight_summary_df.show(10,False)

# print schema
flight_summary_df.printSchema()

## rename the cloumn on the same dataframe.
flight_summary_df = fligh_summary_df.withColumnRenamed("count","flight_count")


```

### Aggregation function:

approx_count_distinct(col) - returns the approximate number of unique items per group

skewness(col) - returns the skewness of the distribution of the values of the given column per group

kurtosis(col) - returns the kurtosis of the distribution of the valies of the given column per group

sumDistinct(col) 
countDistinct(col)
```
from pyspark.sql.functions import count, countDistinct

flight_summary_df.select(count("origin_airport"),count("dest_airport")).show()

# count excludes counting null values in the row

# to include min max, etc
from pyspark.sql.functions import min,max,sum,sumDistinct, avg

flight_summary_df.select(min("flight_count"),
    max("flight_count"))
    
# older students data 
students_df.select(sum("score").show

# union and duplicate dataframe
students_df = students_df.union(students_df);

students_df.select(sum("score")).show()

students_df.select(sumDistinct("score")).show()

# display average
fligh_summary_df.select(avg("flight_count"), (sum("flight_count)/count("flight_count"))).show()
```
Note:  count excludes counting null values in the row

### aggregation with grouping
```
flight_summary_df.groupBy("origin_airport").count()
.orderBy("count", ascending=False).show(5,False)
### the dataframe count () includes a count row and we are using that to order by 

(flight_summary_df.groupBy("origin_airport")
   .agg(max("flight_count").alias("max_flight_count"))
   .orderBy("max_flight_count",ascending=False)).show(5,False)
   
(flight_summary_df.groupBy("origin_state","origin_city")
.count()
.where(col("origin_state")=="CA")
.orderBy("count", ascending=False)).show(5)


(flight_summary_df.groupBy("origin_airport")
   .agg(max("flight_count"),
        min("flight_count"),
        sum("flight_count"),
        count("flight_count"))).show(5)

```
