
### Read stream 
 - Pre-requsites - the mount is already set for the specific ADLS storage account (note the path /mnt/my-content/data/.. is ADLS path)

sample data 
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

Note:
  - if using community edition, we can create a sample csv file and upload it to a location in this case 
```py
dataframe = spark.read.options(header='True',inferSchema='True').csv('dbfs:/FileStore/sample_data/sampleStudent.csv')
# ways to convert the dataframe into json schema file.
schemaJson = dataframe.schema.json()
print(schemaJson)
```
  - We are going to create a simple delta table, after reading the csv content as in above, we can execute below code.
```py
tableName='demo_db.student'
# Note: If this file or directory already exists delete using %fs rm -r '/tmp/'
savePath='/tmp/delta'
sourceType='delta'

dataframe.write \
  .format(sourceType) \
  .save(savePath)

# Create the table.
spark.sql("CREATE TABLE " + table_name + " USING DELTA LOCATION '" + save_path + "'")  
display(spark.sql("SELECT * FROM " + tableName))
```
![image](https://user-images.githubusercontent.com/6425536/163699058-9417190b-a5ab-468e-b134-bcd83acaf626.png)
 
### Actual code to read the above Deta table.
 - If using the community edition with the above table then change `load('/tmp/delta')`
```py
data_delta_readstream = ( 
    spark.readStream
    #.dropDuplicates('column1') //option to drop columns
    .format("delta")
    #.option("overwriteSchema", "true")
    #.schema("/schmea")
    .load("/mnt/my-content/data/my-delta-table/")
)
```

### Batch to perform orc file writer
```py
def batchLoader(df, batch_id):
  (
    df
    .repartition(1)
    .write
    .format("orc")
    .mode("append")
    .save('/mnt/my-content/output/path/data/')  
  )
```

### write content to orc using write stream
```py
data_delta_writestream = ( 
    data_delta_readstream
   # .drop('column')
    .repartition(1)
    .writeStream
    .trigger(once=True)
    .option("checkpointLocation", "/mnt/data/_checkpoints/test")
    .foreachBatch(batchLoader)
    .start()
)
```
![image](https://user-images.githubusercontent.com/6425536/163701192-8cfa132a-afaa-4fb0-b575-56e0fe3feab1.png)


![image](https://user-images.githubusercontent.com/6425536/163699173-cdd53fbd-3bea-46ed-a135-11a46eb2b31c.png)

-----
## Read from ORC file and write to Delta table Example
### Read stream 
```py
  duplicateColumnDic = { "table1" :["field1","field2"]};
  data_readstream = ( 
    spark.readStream
    #.dropDuplicates(duplicateColumnDict[table1])
    .format("orc")
    #.option("overwriteSchema", "true")
    .schema('/path/to/schema.json')
    .load('/path/to/input/location/')
  )
```

## Batch loader
```py
def upsertDataUsingBatchLoader(df, batch_id): 
  datapartition = 'dateupdated_p'
  data_msg_df = (
    df
    .drop(datapartition)
    .dropDuplicates(['field1','field2'])
    .withColumn(fdatapartition, F.date_format(F.col('updateddate'), 'yyyy').cast('int'))
  )
  display(data_msg_df)
  ## create a temp  table in Databricks spark
  data_msg_df.createOrReplaceTempView("temp_table")

  # use the sparksession
  data_msg_df._jdf.sparkSession().sql("""
    MERGE INTO database.my_delta_table mdt
    USING temp_table tt
    ON mdt.field1 = tt.field1 AND mdt.field2 = tt.field2
    WHEN MATCHED THEN UPDATE SET *
    WHEN NOT MATCHED THEN INSERT *                     
          """)
```

### Write to Delta table 

```py
# uses the read
  data_writestream = ( 
    data_readstreams
    .drop('updateddate_p')
    .repartition(1)
    .writeStream
    .trigger(once=True)
    .format("delta")
    #.option("mergeSchema", "true")
    .option("checkpointLocation", "/path/to/the/chekpoint/location")
    .foreachBatch(upsertDataUsingBatchLoader)    #Pass in the funtion that uses the batch loader
    .start()
  )
```
