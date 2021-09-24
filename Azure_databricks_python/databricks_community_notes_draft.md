
Databricks:
  - is a managed Spark platform
  - you don't have to manage the cluster yourself
  - not need to equip yourself with intstallation and configuration knowledge to set up cluster and spark

Databricks Community:
   - cloud optimized, so it can be scaled easily
   - notebook build for colloberation
   - comes with package like Power Bi and Tabelu
   - Has a JOB scheduler (to schedule job)
   
SQL and Spark SQL:
   - Structure query langulage, the data should be in a structure.
   - Spark SQL also supports SQL (SQL is standard)
   - Spark SQL supports SQL ANSI and ISO standards
      - supporting insert, update,select, where, delete

Spark SQL is a module, in spark. The sql we write is converted by Spark Sql into java bytecode and executed over a cluster.

https://community.cloud.databricks.com/login.html

Signup for the free version

Cluster:
  - Any form of spark code, the spark can execute only through cluster.
  - Community version has only 1 node, with driver and 1 worker. 
  - Create the cluster and not the cluster will be active only when operations are performed, max idle time is 2 hours
  
  - Note that though the cluster is not avialable. If we have created databases and tables, those will be available, and those can be exceuted on other cluster.
  
  - The community version of the cluser are managed by the microsoft Azure, so availablity zone needs to be selected accordningly.
  
  - Create a notebook
  - in the notebook we can work
  
```
from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, StringType

spark = (SparkSession.builder.appName("Demo").getOrCreate())

# Loading a data (select the file-> upload and file to load data - provide the path to save the data)
```
#### DBFS - Databricks distributed filesystem is similar to Hadoop file system.
- The distributed file system can store a datafile in distributed manaer, which is it can be split multiple times and stored accross multiple nodes within a cluster.
- The clients used in makes that the files are stored under a folder.


 - continuing the above code flow
 
```
# mark down 

%md ## Read CSV files into DataFrame

# Create a schema 

schema = StructType([
   StructField("Order Id", StringType(), True),
   StructField("Product", StringType(), True),...
])

# CSV file path which needs to be loaded
sales_data_path = "dbfs://FileStore/salesdata/raw"

sales_raw_df=(spark.read.format("csv)
               .option("header",True),
               .schema(schema)
               .load(sales_data_fpath)
)

sales_raw_df.show(10)

sales_raw_df.printSchema()

# now we create a database and first table.

spark.sql("CREATE DATABAES IF NOT EXISTS sales_db")

# ensure we use the data base 
spark.sql("use sales_db")

### if we don't use the above step, the database table will be created in the default db. We don't want that.

# create our tables

spark.sql("""CREATE TABLE IF NOT EXISTS sales_raw
           ( OrderID STRING,
            Product STRING,
            ...
            ...)
          """)
## we can add tripple quote so we can perform multiple statements 

```

- For syntax, refer documentation

https://docs.databricks.com/sql/language-manual/sql-ref-syntax-ddl-drop-database.html

 ### We can use the **USING** clause to load the data from json, parquet, etc file.
 
 ### Since in the above query has ommited the USING clause, the Databricks will use **DELTA** which is default.
    - DELTA is Databricks specific file format
    - The DELTA file format is optimized for ACID database principle. 
    - Which can gaurantee the data integrity
    - This is advantage over the traditional file type
    
    - In the Create table, we have ommitted partition by, we can parition the data latter.
    - In the create table, we ommitted location, which we can specify the directory of the CSV files and other properties using Options and TBLPROPERTIES. like header, etc.

MANAGED and UNMANAGED Tables:
    - Currently, the table will be created at internal default location, which makes this as **managed** table.
        - this manage table means, when droping the table, the table, data and its metadata will also be deleted.
     - if we specify the location, the table will be classified as **unmanaged** table.
        - in unmanaged table, if we drop the table, we will not be transact to the table, but the data at the provided location will not be deleted.
        ```
        create Table simpletable Using org.apache.spark.sql.parquet OPTIONS (PAHT "/path/datalake") as select....
        ```
       - using unamanged tables, is the better option.
       
### Inserting data from csv dataframe to table
```
# we will be using pure Spark Sql to query the dataframe

spark.sql("select * from sales_raw_df").show(10)

## above will throw an error (Table or view not found)
## what this means is the dataframe is not compatible with the pure spark sql

## we need to use the API to fix this issue, 

## we need to convert the Dataframe to a temporary table and then only we can use spark SQL

## convert dataframe to table

sales_raw_df.createOrReplaceTempView("tmp_sales_raw")

## Now since we converted dataframe to table we can query it like below.
spark.sql("select * from tmp_sales_df").show(10,False)

## refer notes:

## inserting records to managed tables.
## we have to see if the tabels are compatible with each other.

spark.sql("describe tmp_sales_raw").show()

spark.sql("describe sales_raw").show()

## describe command will provides the schema 
## the matching cloumns and type says they are compatible.

## now insert
spark.sql(""" INSERT OVERWRITE sales_db.sales_raw
              SELECT `Order ID`,
                    `Product`
                    ......
              FROM tmp_sales_raw
          """)
 ### `` is used since the data frame header column had space.
 ### OVERWRITE command is used to indicate to delete the data if exists so refershsed with latest data is inserted 
 
## view the records from the table
spark.sql("SELECT * FROM sales_db.sales_raw")
```

### Removing error data
 - this can be done with pure Spark SQL 
 - create a new notebook (default to SQL) and attach a active cluster
```
# indicate we use db (Since SQL default we use sql directly)
use sales_db

## markdown heading
%md ### clean error data

select * 
from sales_raw 
limit 10

## fetch null records

select * from sales_db.sales_raw
where OrderId is null
limit 10

## below is the query which also lists the bad records with null
select * from sales_db.sales_raw
where OrderId = "Order Id"
limit 10

## remove the records that are null with spark sql

### query to expose the data without bad records

select * from sales_db.sales_raw
where OrderId != "Order ID"
and OrderId is not null

## compare with the temporary tables

WITH tmp_sales_raw as (
 select * from sales_db.sales_raw
 where OrderID != "Order ID"
   and OrderID is not null
) select * from tmp_sales_raw
limit 10

## to check whether the temp table doesn't contain bad records

WITH tmp_sales_raw as (
 select * from sales_db.sales_raw
 where OrderID != "Order ID"
   and OrderID is not null
) 
select * from tmp_sales_raw
where orderId is null  OR OrderId = "Order Id"
limit 10

## there should be no result if there are not bad records

```


## Note: Temporary table are table that are available only with the current session. The temp tables are automatically dropped at the end of the current session
## we can use managed/unmanaged tables to persist data

## spark sql 
  - split
  - substr
  - cast  used to convert the value to int
      - example `cast (OrderId as int) as OrderID`
  - convert the string to tim satmbo 
      - use `to_timestamp (Date, 'MM/dd/yy HH:mm')`
      - to get the `year (to_timestamp (Date, 'MM/dd/yy HH:mm'))`
      - to get the `month (to_timestamp (Date, 'MM/dd/yy HH:mm'))`
  
## Tranforming the sales data and inserting to a new table
 - The table can be directly created using the Insert sql stamtemt.
 
 - using the notebook with python default 
 ```
 # create a temp dataframe 
 
tmp_sales_df = spark.sql(""" select cast(OderID as int) as orderID, 
 ....
  ... 
  """)
  
 # create a temp table from dataframe.

tmp_sales_df.createOrReplaceTempView("tmp_sales")

 # describe the schema 
spark.sql("describe tmp_sales").show()

 # Create a table
 # now we want this table to be parquet
 # we need to partition by report year and month
 # compression using option .snappy (this is one of compression techinque used by parquet)
spark.sql("""
           CREATE TABLE IF NOT EXISTS sales(
           OrderID INT,
           Product STRING,
           ....
           ReportYear INT,
           Month INT
           )
           USING PARQUET
           PARTIONED BY (ReportYear, Month)
           OPTIONS ('compression' = "snappy")
           LOCATION 'dbfs:/FileStore/salesdata/published
           """)
# executing the above command the table will be created.

## Insert the data since the data type is different

spark.sql("""
    #// use the select query which used for dataframe to load the data from the temp_sales tempview
 """)
 ```

# Generating Analytics 
 - this would be quering the data loaded in the table 
 - creating the note book using SQL default language.
 
```
# list the tables 
show tables 

# describe the sales table , partition information is included
describe sales 

select 
  month,
  round(sum(Price * Quantity)),2) as TotalSales
  from sales
  group by month
  order by TotalSales desc
  
# how to create a visualization for the above query

select 
  month,
  round(sum(Price * Quantity)),2) as TotalSales
  from sales
  group by month
  order by TotalSales asc
# with using the graph button and charts in the cell.
# use plot option, select month and sales in x and y from the box.
```

## create a dashboard based on visualization

  - within the cell, click "add to timeline" and click save. that that will create a dashboard.
  


