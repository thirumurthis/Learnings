### How to convert the long timestamp to a valid time stamp

```py
from pyspark.sql import functions as f
from pyspark.sql import types as t
from pyspark.sql.functions import col,lit
from datetime import datetime

df001 = spark.createDataFrame([(1639518261056, ),(1639518260824,)], ['timestamp_long'])
df001.show(2,False)
df002 = df001.withColumn("timestamp",f.to_timestamp(df001['timestamp_long']/1000))
df001.printSchema()
display(df002)
```
Check for my inputs with detailed output:
  - Answer [stackoverflow](https://stackoverflow.com/questions/49971903/converting-epoch-to-datetime-in-pyspark-data-frame-using-udf/70356729#70356729)


#### Display the table names under the db
```
%sql use demo_db
show tables
```

#### Using sysdate equivalent in databrick spark
```
%sql select to_date(insertdate, 'yyyy-MM-dd') dt, count(*) from demo_db.my-app-table where insertdate > current_date -30
group by to_date(insertdate, 'yyyy-MM-dd') order by dt
```
