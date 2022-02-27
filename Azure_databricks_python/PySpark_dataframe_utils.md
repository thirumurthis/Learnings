### Working with PySpark Dataframes in Databricks

Dataframes are datastructure used within pyspark for storing data in array format with rows and columns.

Upload the csv data file into the Databricks community edition.
Note: 
  - By default the DFS file system in the Databricks Workspace UI is not enabled.
    - To enable, navigate to Settings -> Admin consoler -> Workspace Settings -> DBFS File Browser : enable it.

 From Data -> Select the DBFS button at top, create a folder `sample_data` and upload the csv file `sampleStudent.csv`.
 
 Sample csv data
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

##### Read the CSV data using spark into dataframe using pyspark
```
df_data = spark.read.option("header",True).csv('/FileStore/sample_data/sampleStudent.csv')

# passing the header with true as string, above command and below are the same
df_data1 = spark.read.option("header","true").csv('/FileStore/sample_data/sampleStudent.csv')

# Alternate way to inferschema
df_data2 = spark.read.csv('/FileStore/sample_data/sampleStudent.csv',header=True,inferSchema=True)
```
Note: 
  - If we are using pyspark setup in local.
      - we need to create and start SparkSession first
```
      # define the SparkSession from package
      import pyspark.sql import SparkSession
      # Create or get the spark session
      spark = SparkSession.builder.appName("Demo").getOrCreate()
      # view the spark values
      spark
```
#### To display the output of the dataframe
```
df_data.show()
```
- Output:
```
+---------+----+--------+----+
|StudentId|Name| Subject|Mark|
+---------+----+--------+----+
|        1| Ram| science|  80|
|        1| Ram|   maths|  90|
|        1| Ram|language|  85|
|        2| Tom| science|  72|
|        2| Tom|   maths|  75|
|        2| Tom| science|  79|
|        3| Kim| science|  81|
|        3| Kim|   maths|  92|
|        3| Kim|language|  89|
+---------+----+--------+----+
```

##### Using the databricks display() function.
  -  Displays the output much better way.
```
display(df_data)
```

#### To view the columns within the dataframe
```
data_df.colums
```
- Output 
```
 ['StudentId', 'Name', 'Subject', 'Mark']
```

#### To view the records using `head()`
```
data_df.head(3)
```
- Output
```
 [Row(StudentId='1', Name='Ram', Subject='science', Mark='80'),
 Row(StudentId='1', Name='Ram', Subject='maths', Mark='90'),
 Row(StudentId='1', Name='Ram', Subject='language', Mark='85')]
```

#### To display specific column or columns from dataframe
 - We can pick specific column using `select()` funtion
```
# below will fetch only the Mark column only
df_data.select('Mark').show()

# below will fetch two columns StudentId,Name 
df_data.select("StudentId","Name").show()

# below is example passing a list of columns
df_data.select(["Name","Mark").show()
```
- Output
```
+----+
|Mark|
+----+
|  80|
|  90|
|  85|
|  72|
|  75|
|  79|
|  81|
|  92|
|  89|
+----+

+---------+----+
|StudentId|Name|
+---------+----+
|        1| Ram|
|        1| Ram|
|        1| Ram|
|        2| Tom|
|        2| Tom|
|        2| Tom|
|        3| Kim|
|        3| Kim|
|        3| Kim|
+---------+----+
```
###### Other approachs to select columns from the dataframe

```
df_data.select(df_data.Subject,df_data.Mark).show()
df_data.select(df_data["StudentId"],df_data["Mark"]).show()

#using col() function from pypsark.sql
from pyspark.sql.functions import col
df_data.select(col("StudentId"),col("Mark")).show()

# Selecting all columns, different approach
df_data.select(*columns).show()

# Select All using the pyspark sql col function
df_data.select([col for col in df_data.columns]).show()
df_data.select("*").show()

# Below selects first 2 columns and top 4 rows
df_data.select(df_data.columns[:2]).show(4)

# Below selects column 2 and 3 and top 3 rows
df_data.select(df_data.columns[2:3]).show(3)
```

#### To validate the schema of the dataframe created.

```
df_data.printSchema()
```
 - Output
```
root
 |-- StudentId: string (nullable = true)
 |-- Name: string (nullable = true)
 |-- Subject: string (nullable = true)
 |-- Mark: string (nullable = true)
```

##### How to view the datatypes
```
df_data.dtypes
```
- output
```
[('StudentId', 'string'),
 ('Name', 'string'),
 ('Subject', 'string'),
 ('Mark', 'string')]
```

#### Describe funtion of datafarame describes the data type
```
df_data.describe()

# Below will be listing the table format
df_data.descirbe().show()
```
- Output
```
DataFrame[summary: string, StudentId: string, Name: string, Subject: string, Mark: string]
+-------+------------------+----+--------+-----------------+
|summary|         StudentId|Name| Subject|             Mark|
+-------+------------------+----+--------+-----------------+
|  count|                 9|   9|       9|                9|
|   mean|               2.0|null|    null|82.55555555555556|
| stddev|0.8660254037844386|null|    null|6.912147117775906|
|    min|                 1| Kim|language|               72|
|    max|                 3| Tom| science|               92|
+-------+------------------+----+--------+-----------------+
```

#### Filtering the data from the dataframe
```
filterDf = df_data.filter(df_data.Name == "Ram").sort(df_data.Name)
# display(filterDf)
filterDf.show()
```
- Output
```
+---------+----+--------+----+
|StudentId|Name| Subject|Mark|
+---------+----+--------+----+
|        1| Ram| science|  80|
|        1| Ram|   maths|  90|
|        1| Ram|language|  85|
+---------+----+--------+----+
```

##### Using python `type()` to view the data type of dataframe
```
type(data_df)
```
- output 
```
pyspark.sql.dataframe.DataFrame
```

#### Adding Column to the dataframe using `withColumn()`
  - `withColumn()` operation is inplace operation so we need to assing the result to variable
```
df_data.withColumn('Grade',df_data['Mark']/100).show()

# assinging the output to another variable since returntype is a dataframe
df_data4=df_data.withColumn('Grade',df_data['Mark']/100)
```
- Output
```
+---------+----+--------+----+-----+
|StudentId|Name| Subject|Mark|Grade|
+---------+----+--------+----+-----+
|        1| Ram| science|  80|  0.8|
|        1| Ram|   maths|  90|  0.9|
|        1| Ram|language|  85| 0.85|
|        2| Tom| science|  72| 0.72|
|        2| Tom|   maths|  75| 0.75|
|        2| Tom| science|  79| 0.79|
|        3| Kim| science|  81| 0.81|
|        3| Kim|   maths|  92| 0.92|
|        3| Kim|language|  89| 0.89|
+---------+----+--------+----+-----+
```

#### Renaming the column using  `withColumnRenamed()`
  - Pass the existing colum and the new name for the column
```
df_data1.withColumnRenamed('Grade','DivideBy100').show()
```
- Output:
```
+---------+----+--------+----+-----------+
|StudentId|Name| Subject|Mark|DivideBy100|
+---------+----+--------+----+-----------+
|        1| Ram| science|  80|        0.8|
|        1| Ram|   maths|  90|        0.9|
|        1| Ram|language|  85|       0.85|
|        2| Tom| science|  72|       0.72|
|        2| Tom|   maths|  75|       0.75|
|        2| Tom| science|  79|       0.79|
|        3| Kim| science|  81|       0.81|
|        3| Kim|   maths|  92|       0.92|
|        3| Kim|language|  89|       0.89|
+---------+----+--------+----+-----------+
```
#### Dropping the column in the dataframe
  - Pass in single column or list of columns
  - in the above section, we added df_data4 dataframe with Grade, we will drop it
  - `drop()` operation is inplace operation so we need to assing the result to variable
```
df_data4=df_data4.drop('Grade')
df_data4.show()
```
- Output
```
+---------+----+--------+----+
|StudentId|Name| Subject|Mark|
+---------+----+--------+----+
|        1| Ram| science|  80|
|        1| Ram|   maths|  90|
|        1| Ram|language|  85|
|        2| Tom| science|  72|
|        2| Tom|   maths|  75|
|        2| Tom| science|  79|
|        3| Kim| science|  81|
|        3| Kim|   maths|  92|
|        3| Kim|language|  89|
+---------+----+--------+----+
```

#### New Data set, for further operations

 - Create the below data in a csv file and upload to `/FileStore/sample_data/sampleEmloyee.csv`
```
EmployeeId,Name,Department,Salary
1,Tim,Accounts,10000.00
2,Tom,Sales,12000.00
3,Bob,IT,15000.00
4,Rob,,100.00
5,,Security,3000.00
```

#### To read the data using spark

```
employee_df=spark.read.csv("/FileStore/sample_data/sampleEmployee.csv",header="true",inferSchema="true")
employee_df.show()
```
Note: 
  - By defaut, the Salary will be read as string.
  
- Output
```
+----------+----+----------+-------+
|EmployeeId|Name|Department| Salary|
+----------+----+----------+-------+
|         1| Tim|  Accounts|10000.0|
|         2| Tom|     Sales|12000.0|
|         3| Bob|        IT|15000.0|
|         4| Rob|      null|  100.0|
|         5|null|  Security| 3000.0|
+----------+----+----------+-------+
```

#### View the schema of the dataframe
```
employee_df.printScheam()
```
- Output
```
root
 |-- EmployeeId: integer (nullable = true)
 |-- Name: string (nullable = true)
 |-- Department: string (nullable = true)
 |-- Salary: double (nullable = true)
```

#### Dorp the rows based on null values in the dataframe
  - Below removes the rows which has null in an of the columns
```
# without specifying any of the options in the na and dropping 
employee_df.na.drop().show()
```
- output
```
+----------+----+----------+-------+
|EmployeeId|Name|Department| Salary|
+----------+----+----------+-------+
|         1| Tim|  Accounts|10000.0|
|         2| Tom|     Sales|12000.0|
|         3| Bob|        IT|15000.0|
+----------+----+----------+-------+
```

##### Drop the column with more options
 - using `how` takes `all|any`
 - how=any is the default one
```
# below drops the row when all the columns are null
## in this case if row had EmployeeId = null, Name = null, Department = null, Salary = null
employee_df.na.drop(how="all").show()

# below drops the row when any of the ciolumn is null
employee_df.na.drop(how="any").show()
```
 - using `threshold` value in drop, this tells at least there should be that much nulls

```
# below will remove if the row has that many nulls,
## our dataset didn't had two nulls so we will the result will not be displayed
employee_df.na.drop(how="any",thresh=2).show()
```
- Demonstarting the above with new data set in the dataframe `union` two dataframe

##### Adding null to the dataframe using pyspark, and also performing `union` of two dataframe
```
from pyspark.sql.functions import lit,col,when
# Define columns to add additional data set directly to the existing data frame
# Note, i don't wan't to update the CSV file, using spark function to update the null
# it is not directly easy to add a null to the data frame, i am using '' and converting those 
# values to None/null
columns = ['EmployeeId','Name','Department','Salary']
data = [(6,'','',0.0), (7,'','',0.0)]

newEmployeeRow_df = spark.createDataFrame(data, columns)
newEmployeeRow_df=newEmployeeRow_df.select([when(col(c)=="",None).otherwise(col(c)).alias(c) for c in newEmployeeRow_df.columns])
#df2.show()
#newEmployeeRow_df.show()
result_df = employee_df.union(newEmployeeRow_df)
#result_df.show()

result_df.na.drop(how="any",thresh=1).show();
``` 
 Note: the result rendered by above `was not as expected` check the documentation for more details.
 - output
```
# below is the newEmployeeRow_df.show()
+----------+----+----------+-------+
|EmployeeId|Name|Department| Salary|
+----------+----+----------+-------+
|         1| Tim|  Accounts|10000.0|
|         2| Tom|     Sales|12000.0|
|         3| Bob|        IT|15000.0|
|         4| Rob|      null|  100.0|
|         5|null|  Security| 3000.0|
|         6|null|      null|    0.0|
|         7|null|      null|    0.0|
+----------+----+----------+-------+
```
- `subset` within the drop function, used to drop nulls from a specific column
```
# with the above dataframe created by merging from csv and programatically
# using subest we drop the nulls from the department

result_df.na.drop(how="any",subset=['Department']).show();
```
- output 
```
+----------+----+----------+-------+
|EmployeeId|Name|Department| Salary|
+----------+----+----------+-------+
|         1| Tim|  Accounts|10000.0|
|         2| Tom|     Sales|12000.0|
|         3| Bob|        IT|15000.0|
|         5|null|  Security| 3000.0|
+----------+----+----------+-------+
```

##### Filling the null values in the dataframe with `fill`
```
# using the above dataframe created by union operation of csv and program
result_df.na.fill('Filling demo').show()
```
- output 
```
+----------+------------+------------+-------+
|EmployeeId|        Name|  Department| Salary|
+----------+------------+------------+-------+
|         1|         Tim|    Accounts|10000.0|
|         2|         Tom|       Sales|12000.0|
|         3|         Bob|          IT|15000.0|
|         4|         Rob|Filling demo|  100.0|
|         5|Filling demo|    Security| 3000.0|
|         6|Filling demo|Filling demo|    0.0|
|         7|Filling demo|Filling demo|    0.0|
+----------+------------+------------+-------+
```

- Filling the null with some value for specific column
```
# below fills the department column along with the filling demo string when null found
result_df.na.fill('Filling demo',['Department']).show()
```
- output
```
+----------+----+------------+-------+
|EmployeeId|Name|  Department| Salary|
+----------+----+------------+-------+
|         1| Tim|    Accounts|10000.0|
|         2| Tom|       Sales|12000.0|
|         3| Bob|          IT|15000.0|
|         4| Rob|Filling demo|  100.0|
|         5|null|    Security| 3000.0|
|         6|null|Filling demo|    0.0|
|         7|null|Filling demo|    0.0|
+----------+----+------------+-------+
```
- Using `Imputer` function to fill the null on a Integer column with mean, median, mode
  - In below creating a new dataframe with null data for integer data type  
```
from pyspark.sql.functions import lit,col,when
# Define columns to add additional data set directly to the existing data frame
# Note, i don't wan't to update the CSV file, using spark function to update the null
# it is not directly easy to add a null to the data frame, i am using '' and converting those 
# values to None/null
columns = ['EmployeeId','Name','Department','Salary']

#Note: Need to use only one None, if we use schema enforced, we can use multiple none which is null
# for now setting one row for Salary as null. (this will be filled by the imputer funtcion)
data = [(6,'','',None), (7,'','',20.0)]
 

newEmployeeRow_df = spark.createDataFrame(data, columns)
newEmployeeRow_df=newEmployeeRow_df.select([when(col(c)=="",None).otherwise(col(c)).alias(c) for c in newEmployeeRow_df.columns])
result_df = employee_df.union(newEmployeeRow_df)
result_df.show()
```
- Output of the dataframe
```
+----------+----+----------+-------+
|EmployeeId|Name|Department| Salary|
+----------+----+----------+-------+
|         1| Tim|  Accounts|10000.0|
|         2| Tom|     Sales|12000.0|
|         3| Bob|        IT|15000.0|
|         4| Rob|      null|  100.0|
|         5|null|  Security| 3000.0|
|         6|null|      null|   null|
|         7|null|      null|   20.0|
+----------+----+----------+-------+
```
- Defining the imputer function for Salary column (we can use mulitple columns with integer/double as well)
```
from pyspark.ml.feature import Imputer

imputer = Imputer(
    inputCols=['Salary'],
    outputCols=['Salary_imputed']
 ).setStrategy("median")
 
 # in strategy we can use mean, median, mode.
 # for multiple columns in the output to conactenate we can use 
 # [ f'{cols}_imputed for cols in ['col1','col2']]
```
```
imputer.fit(result_df).transform(result_df).show()
```
- output
```
# row 6th filled with 3000 instead of null
+----------+----+----------+-------+--------------+
|EmployeeId|Name|Department| Salary|Salary_imputed|
+----------+----+----------+-------+--------------+
|         1| Tim|  Accounts|10000.0|       10000.0|
|         2| Tom|     Sales|12000.0|       12000.0|
|         3| Bob|        IT|15000.0|       15000.0|
|         4| Rob|      null|  100.0|         100.0|
|         5|null|  Security| 3000.0|        3000.0|
|         6|null|      null|   null|        3000.0|
|         7|null|      null|    0.0|           0.0|
+----------+----+----------+-------+--------------+
```


#### Filter Operation in dataframe in pyspark

```
# find the students who scored above 80 in science
# approach 1 to write the query directly in the fitler function
df_data.filter("Subject = 'science' and Mark >= 80").show()
```
- Output
```
# dataframe values 
+---------+----+--------+----+
|StudentId|Name| Subject|Mark|
+---------+----+--------+----+
|        1| Ram| science|  80|
|        1| Ram|   maths|  90|
|        1| Ram|language|  85|
|        2| Tom| science|  72|
|        2| Tom|   maths|  75|
|        2| Tom| science|  79|
|        3| Kim| science|  81|
|        3| Kim|   maths|  92|
|        3| Kim|language|  89|
+---------+----+--------+----+

# Filtered output 
+---------+----+-------+----+
|StudentId|Name|Subject|Mark|
+---------+----+-------+----+
|        1| Ram|science|  80|
|        3| Kim|science|  81|
+---------+----+-------+----+
```
##### Performing selection over the fiter conditions
 - using the query in filter fuction, and using `&` operators
 
 - Note: the values returned are inline, make sure to store to different variable
```
df_data.filter("Subject = 'science' and Mark >= 80").select(['Name','Mark']).show()

# Same query as above but using operator & 
df_data.filter((df_data["Subject"] == 'science') &  
               (df_data["Mark"] >= 80)).select(['Name','Mark']).show()
```
- Output:
```
# for the same dataframe df_data value as above, below is the filtered value
+----+----+
|Name|Mark|
+----+----+
| Ram|  80|
| Kim|  81|
+----+----+
```
 - using `|` (or) operator
```
df_data.filter((df_data["Subject"] == 'science') | 
               (df_data["Mark"] >= 80)).select(['Name','Mark']).show()
```

- using `~` (NOT) condition or inverse opreation

```
df_data.filter(~(df_data["Subject"] == 'science')).select(['Name','Subject','Mark']).show()
```
- output 
```
+----+--------+----+
|Name| Subject|Mark|
+----+--------+----+
| Ram|   maths|  90|
| Ram|language|  85|
| Tom|   maths|  75|
| Kim|   maths|  92|
| Kim|language|  89|
+----+--------+----+
```

#### `GroupBy` and `Aggregate` in dataframe
##### GroupBy

 - groupBy name and find the total score for each student
```
# note if the the column name is not PASSED to the sum, the integer/double 
# will automatically aggergate all these fields

df_data.groupBy('Name').sum("Mark").show()
```
- output
```
+----+---------+
|Name|sum(Mark)|
+----+---------+
| Tom|      226|
| Ram|      255|
| Kim|      262|
+----+---------+
```
 - groupBy and find the max score on each subject
```
df_data.groupBy('Subject').max("Mark").show()
```
- Output
```
+--------+---------+
| Subject|max(Mark)|
+--------+---------+
|   maths|       92|
| science|       81|
|language|       89|
+--------+---------+
```
- Groupby without any column in sum
```
df_data.groupBy('Name').sum().show()
```
- output 
```
+----+--------------+---------+
|Name|sum(StudentId)|sum(Mark)|
+----+--------------+---------+
| Tom|             6|      226|
| Ram|             3|      255|
| Kim|             9|      262|
+----+--------------+---------+
```
- Groupby and find the mean
```
df_data.groupBy('Name').mean().show()
```
- Output
```
+--------+--------------+-----------------+
| Subject|avg(StudentId)|        avg(Mark)|
+--------+--------------+-----------------+
|   maths|           2.0|85.66666666666667|
| science|           2.0|             78.0|
|language|           2.0|             87.0|
+--------+--------------+-----------------+
```

##### Using the `aggregate` function directly passing key
```
df_data.agg({'Mark':'sum'}).show()
```
- Output
```
+---------+
|sum(Mark)|
+---------+
|      743|
+---------+
```

