**Objective**:

- With the orc file created from the Delta table in the [post](https://thirumurthi.hashnode.dev/databricks-pyspark-read-data-from-delta-table-and-create-orc-using-readstream-and-writestream), now we will read that orc file and upsert the Delta table score (which has the same schema as the student table mentioned in that post).

### Initial setup of table

#### Create the database

```
%sql create database if not exists demo_db
```

#### Load the sample CSV content to Databricks cluster

  - Create a csv file using below sample content

```csv
StudentId,Name,Subject,Mark
1,Ram,science,80
1,Ram,maths,90
1,Ram,language,85
2,Tom,science,72
2,Tom,maths,75
2,Tom,science,79
3,Kim,science,81
3,Kim,maths,92
3,Kim,language,89
```
 - Load the data to Databricks cluster workspace

![image](https://user-images.githubusercontent.com/6425536/163703106-bb2b2dc0-43c0-4c44-8664-93d03c586a4a.png)


#### Read the data from csv using spark and create the table

```py
dataframe = spark.read.options(header='True',inferSchema='True').csv('dbfs:/FileStore/sample_data/sampleStudent.csv')
```

#### Write the data to delta file and create a table using the delta file

  - Below creates a table score and loads the data

```py
tableName='demo_db.score'
savePath='/tmp/demo_score'
sourceType='delta'

dataframe.write \
  .format(sourceType) \
  .save(savePath)

# Create the table.
spark.sql("CREATE TABLE " + tableName + " USING DELTA LOCATION '" + savePath + "'")  
display(spark.sql("SELECT * FROM " + tableName))
```

#### Delete the loaded content to the score table

  - The content of the table will be deleted and start with empty database.

```py
%sql delete from demo_db.score
```

![image](https://user-images.githubusercontent.com/6425536/163702936-b1ee2189-63a7-4e3d-9e2d-42260a698e5d.png)

> As mentioned in this [post](https://thirumurthi.hashnode.dev/databricks-pyspark-read-data-from-delta-table-and-create-orc-using-readstream-and-writestream)  also [git](https://github.com/thirumurthis/Learnings/blob/master/Azure_databricks_python/Read_From_Delta_Table_and_create_orc_file.md) the Delta table are loaded to the student table, and orc file is created in a specific location. We will use the same orc file to load the score table(which has the same schema as the student table).

### Code to read from the Delta table using `readStream`

```py
# schema defined, if we have the json version we can use it
rdSchema = ("StudentId int, " +
            "Name string, " +
            "Subject string, " +
            "Mark int")

data_readstream = ( 
    spark.readStream
     .schema(rdSchema)
     .format("orc")
     .load('/dbfs/FileStore/sample_data/student_data/')  #path to the orc file
)
```

### Code defines the batch loader to merge dataframe and upsert delta table score

```py
def upsertDataUsingBatchLoader(df, batch_id): 
  #datapartition = 'studentid_p'
  data_msg_df = (df)   # we can use the df dataframe directly instead of use another variable.

  #display(data_msg_df)
  
  ## create a temp  table in Databricks spark
  data_msg_df.createOrReplaceTempView("temp_student_tbl")

  # use the sparksession to merge the table
  data_msg_df._jdf.sparkSession().sql("""
    MERGE INTO demo_db.score mdt
    USING temp_student_tbl tst
    ON mdt.StudentId = tst.StudentId
    WHEN MATCHED THEN UPDATE SET *
    WHEN NOT MATCHED THEN INSERT *                     
          """)
```

### Code to write the information to Delta table using `writeStream`

```py
data_writestream = ( 
    data_readstream
    .repartition(1)
    .writeStream
    .trigger(once=True)
    .format("delta")
    .option("checkpointLocation", "/dbfs/FileStore/sample_data/update_tbl/")
    .foreachBatch(upsertDataUsingBatchLoader)    #Pass in the funtion that uses the batch loader
    .start()
  )
```

**Output**

- After executing the above write stream, the output will be more like in below snapshot

![image](https://user-images.githubusercontent.com/6425536/163702880-7f72b498-2473-4f1d-b20b-ec6398325b0d.png)

**Output**

- Finally the score table will be updated with the data.

![image](https://user-images.githubusercontent.com/6425536/163702908-339e4105-e871-46dc-a1b9-9181f4c1f426.png)
