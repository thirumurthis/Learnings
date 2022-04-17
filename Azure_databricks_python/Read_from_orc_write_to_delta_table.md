### Create the database

```py
%sql create database if not exists demo_db
```

### The sample CSV content is as follows
  - Load this into the Databricks Cluster area
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

### Read the data from csv and create a table

```py
dataframe = spark.read.options(header='True',inferSchema='True').csv('dbfs:/FileStore/sample_data/sampleStudent.csv')
```

### Write the data to delta file and create a table using the delta file
  - below creates a table score
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
### Delete the loaded content to the score table
  - The content of the table will be deleted and start with empty database.
```py
%sql delete from demo_db.score
```
![image](https://user-images.githubusercontent.com/6425536/163702936-b1ee2189-63a7-4e3d-9e2d-42260a698e5d.png)


##### As mentioned in the [link](https://github.com/thirumurthis/Learnings/blob/master/Azure_databricks_python/Read_From_Delta_Table_and_create_orc_file.md) 
the Delta table are loaded to the student table, and orc file is created in a specific location.
 - we will be using that orc file to load the score table, which has the same schema as the student table.

### Code to read from the Delta table
```py
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

### Code to define the batch loader to merge data to delta table score

```py

def upsertDataUsingBatchLoader(df, batch_id): 
  #datapartition = 'studentid_p'
  data_msg_df = (df)   # The dataframe df can be used directly. instead of saving to another variable.
  # display(data_msg_df)
  
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
### Code to write the information to Delta table
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
- After executing the above write stream, the output will be more like in below snapshot
- 
![image](https://user-images.githubusercontent.com/6425536/163702880-7f72b498-2473-4f1d-b20b-ec6398325b0d.png)

- Finally the score table will be updated with the data.

![image](https://user-images.githubusercontent.com/6425536/163702908-339e4105-e871-46dc-a1b9-9181f4c1f426.png)
