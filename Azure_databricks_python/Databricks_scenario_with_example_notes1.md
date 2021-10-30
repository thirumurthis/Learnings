 ### How to create a simple data frame in databrick
 
 ```py
 data = [{"Employee": 'A', "ID": 1, "Salary": 121.44, "Trainee": True},
        {"Employee": 'B', "ID": 2, "Salary": 300.01, "Trainee": False},
        {"Employee": 'C', "ID": 3, "Salary": 10.99, "Trainee": False},
        {"Employee": 'E', "ID": 4, "Salary": 33.87, "Trainee": True}
        ]
  
  data_df = spark.createDataFrame(data)
  display(data_df)
 ```
 ![image](https://user-images.githubusercontent.com/6425536/139539659-66358220-92fa-4f03-aa70-304ef54007ea.png)

  - Alternate options
 ```py
 data = [{"Employee": 'A', "ID": 1, "Salary": 121.44, "Trainee": True},
        {"Employee": 'B', "ID": 2, "Salary": 300.01, "Trainee": False},
        {"Employee": 'C', "ID": 3, "Salary": 10.99, "Trainee": False},
        {"Employee": 'E', "ID": 4, "Salary": 33.87, "Trainee": True}
        ]
  
 # command
  from pyspark.sql import SparkSession  
  ## in databricks already the spark will hold the spark session
  spark = SparkSession.builder.getOrCreate()
  
 # command 
  data_df = spark.createDataFrame(data)
  display(data_df)
  ## python type to view the data type of data
  type(data)
 ```
#### Dataframe with data and column seperately

```py 
column = ['Employee','ID','Salary']
data1 = [('A','1',100.0),('B','2',200.0)]

# use toDF and pass the column list with * in arg
data1_df = spark.createDataFrame(data1).toDF(*column)
```
