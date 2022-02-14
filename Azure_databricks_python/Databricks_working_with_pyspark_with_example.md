 ## Create a simple data frame in databrick
 
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
## Dataframe with data and column seperately

```py 
column = ['Employee','ID','Salary']
data1 = [('A','1',100.0),('B','2',200.0)]

# use toDF and pass the column list with * in arg
data1_df = spark.createDataFrame(data1).toDF(*column)
```

## Convert JSON String to a Dataframe applying schema, example. This can be converted to temp table and queried.
```py
from pyspark.sql import SparkSession, functions as F, types as T
from datetime import datetime
import json as json

jsonInput = """
{
  "employeeName":"dummy user1",
  "department" : ["Finance","Accounts"],
  "userName":"myusername",
  "joinDate": "2018-09-15T19:55:00.000+0000",
  "addressInfo":{ 
     "City": "Seattle"
  },
  "isActive" : true,
  "employeeId": 300
}
"""

#read as json using json lib
inputJson= json.loads(jsonInput)

# For debugging the json
print(inputJson)

# for debugging purpose
dateInfo = inputJson.get('joinDate')
print(f"date: {dateInfo}")

# Define the schema for json data
inputSchema = T.StructType([
  T.StructField('name', T.StringType(), False), # last argument is nullable or not
  T.StructField('department', T.ArrayType(T.StringType(),False),True),
  T.StructField('loginId',T.StringType(), False),
  T.StructField('joiningDate',T.TimestampType(),False),
  T.StructField('address',T.MapType(T.StringType(),T.StringType(),True), False),
  T.StructField('active',T.BooleanType(),False),
  T.StructField('id',T.IntegerType(),False)
])

# simply return the date format with timestamp
def getdateFormat():
  return "%Y-%m-%dT%H:%M:%S.%f%z"

addressDictionary= inputJson.get('addressInfo')
print(addressDictionary)
inputHasAddress = False
if( addressInfo in addressDictionary for addressInfo in ('city','streetName')):
  inputHasAddress = True

print(f"contians address in input? - {inputHasAddress}")

# Lets convert the format and set the key name from the input
def convertJsonToDataFrameRow():
  return {
    'name': inputJson.get('employeeName'),
    'department' : inputJson.get('department'),
    'loginId' : inputJson.get('userName'),
    'joiningDate': datetime.strptime(inputJson.get('joinDate'),getdateFormat()),
    'active' : inputJson.get('isActive'),
    'address' : inputJson.get('addressInfo'),
    'id':inputJson.get('employeeId')
  }

# Method to return provided address or defaul one
def fetchAddress(isDefault):
  if isDefault == True:
    return inputJson.get('addressInfo')
  else:
    return {
     "streetName":"",
     "city": ""
   }

# convert to the dataframe and use it for future
inputDF = spark.createDataFrame([convertJsonToDataFrameRow()],inputSchema);
display(inputDF)
```

- Output from the community edition: 
![image](https://user-images.githubusercontent.com/6425536/153781921-06e64713-afc1-4f26-b83a-5e04a3a2bf9b.png)

## Create a temp table view using the dataframe in Databricks and drop it
```py
# converting the Dataframe to a temp table 
TABLENAME="employee"
try:
  inputDF.createGlobalTempView(TABLENAME)
  tableDF = spark.sql(f"SELECT * FROM global_temp.{TABLENAME}")
  display(tableDF)
except Exception as e:
  print (f"Exception occurred: {e}")
  raise e
finally:
  spark.catalog.dropGlobalTempView(TABLENAME)
 ```

 - Output from community edition:
![image](https://user-images.githubusercontent.com/6425536/153782515-e6270d01-0d98-4bf0-891c-16f1b1ed7be5.png)
