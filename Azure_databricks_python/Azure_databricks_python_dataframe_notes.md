Azure Databricks python:

dataframe :- is a table or 2-d array like structure which contains column and rows.

each column contains values of variable of rows.
each rows contains value of each column

Creating simple dataframe

```

from pyspark.sql import *

Create example data - departments and employees

department1= Row(id='1', name='Logisctics')
department2=Row(id='2'), name='Analytics')

Employee = Row("firstname","lastname","id")
employee1 = Employee('tom','hardy','12')
employee2 = Employee('Jim',None,'11')

department1empoyee1 = Row(department=department1, employees=[employee1,employee2])
department1empoyee2 = Row(department=department2, employees=[employee1])

## dataframe
print(department1)
print(employee1)
print(department1empoyee1.employees[0].lastname)
```
- creating data frame from list of data

```
departmentwithEmployees1 = [depart1employee1,depart1employee2]
departmentwithEmployees2 = [depart1employee2]

# datafames
df1 = spark.createDataFrame(departmentwithEmployees1)
dispaly(df1)
df2 = spark.createDataFrame(departmentwithEmployees2)

# renders
# json structure of department and employee as differen row
```

```
unionDF = df1.union(df2)
```

- write data to parquet file

```
# remove the file if it exists.
dbutils.fs.rm("/tmp/depemploye.parquet",true)

unionDF.write.parquet("/tmp/depemploye.parquet")
```

- read data from the parquet file 

```
parquetDF= spark.read.parquet("/tmp/depemploye.parquet")
display(parquetDF)
```

- to read the data from the csv, simply change the parquet to csv in above


```
csvDF= spark.read.csv("/tmp/depemploye.csv")
display(csvDF)
```

- explode the data from the unionDF
```
from pyspark.sql.functions import explode

explodeDF = unionDF.select(explode("employees").alias("e"))
flattenDF = explodeDF.selectExpr("e.firstname","e.lastname","e.id")

flattenDF.show()
```

- filtering the data

```
filterDf = flatten.filter(flattenDF.firstname == "tom").sort(flattenDF.lastname)
dispaly(filterDf)
```
- with column and asc

```
from pyspark.sql.functions import col,asc
filterDf=flattenDF.filter((col("firstname") == "tom") | col("firstname") == "jim")).sort(asc("lastname"))
display(filterDf)
```

- filter class is same as where clause in sql

```
whereDF = flattenDF.where((col("firstname") == "tom") | col("firstname") == "jim")).sort(asc("lastname"))
display(whereDF)
```

- filter nulls with different value
```
nonNullDF = flattenDF.fillna("NullValue")
display(nonNullDF)
```

- display rows with missing firstname and lastname
```
filterNonNullDF = flattenDF.filter(col("firstname").isNull() | col("lastname").isNull()).sort("id")
```

- aggregation using count distinct groupby

```
from pyspark.sql.function import countDistinct

countDistinctDF = nonNullDF.select("firstname","lastname")\
.groupBy("firstname")\
.agg(countDistinct("lastname").alias("distinct_lname"))

display(countDistinctDF)
```

- getting the explain plan

```
# to read teh explain plan
countDistinctDF.explain();
```

- summing/aggregating the column

```
columnSumDF = nonNullDF.agg({"id":"sum"})
display(columnSumDF)
```

-- Getting the statistics

```
nonNullDF.describe("id").show();
# give count, mean
```

- we can use panda, matplotlib to plot in databricks

```
import pandas as pd
import matplotlib.pyplot as plt

plt.clf()
pdDF= nonNullDF.toPandas()
pdDF.plot(x='firstname',y='id',kind='bar',rot=45)
display()
```
