
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
