
### Read stream 
 - Pre-requsites - the mount is already set for the specific ADLS storage account
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
```
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
    fde_readstream
   # .drop('column')
    .repartition(1)
    .writeStream
    .trigger(once=True)
    .option("checkpointLocation", "/mnt/data/_checkpoints/test")
    .foreachBatch(batchLoader)
    .start()
)
```

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
