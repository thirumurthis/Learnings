### Creating Dataframe programatically with data and schema

```
from pyspark.sql.types import StructType,StructField, StringType, IntegerType
data2 = [("Tim","","Smith","100","M",3050),
    ("Don","J","Fred","102","M",5500),
    ("Ben","","Afflec","103","F",-1)
  ]

schema = StructType([ \
    StructField("firstname",StringType(),True), \
    StructField("middlename",StringType(),True), \
    StructField("lastname",StringType(),True), \
    StructField("id", StringType(), True), \
    StructField("gender", StringType(), True), \
    StructField("salary", IntegerType(), True) \
  ])
 
df = spark.createDataFrame(data=data2,schema=schema)
df.printSchema()
df.show(truncate=False)
```
- Output
```
root
 |-- firstname: string (nullable = true)
 |-- middlename: string (nullable = true)
 |-- lastname: string (nullable = true)
 |-- id: string (nullable = true)
 |-- gender: string (nullable = true)
 |-- salary: integer (nullable = true)

+---------+----------+--------+---+------+------+
|firstname|middlename|lastname|id |gender|salary|
+---------+----------+--------+---+------+------+
|Tim      |          |Smith   |100|M     |3050  |
|Don      |J         |Fred    |102|M     |5500  |
|Ben      |          |Afflec  |103|F     |-1    |
+---------+----------+--------+---+------+------+
```

##### Other ways are to read from the csv, json, etc
```
data = [("James","Smith","USA","CA"),
    ("Michael","Rose","USA","NY"),
    ("Robert","Williams","USA","CA"),
    ("Maria","Jones","USA","FL")
  ]
columns = ["firstname","lastname","country","state"]
df = spark.createDataFrame(data = data, schema = columns)
df.show(truncate=False)
```

### converting json to dataframe
 - the json is passed as string, not read from the file.

```
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
# display(inputDF)
inputDF.show()

```
- Output:
```
{'employeeName': 'dummy user1', 'department': ['Finance', 'Accounts'], 'userName': 'myusername', 'joinDate': '2018-09-15T19:55:00.000+0000', 'addressInfo': {'City': 'Seattle'}, 'isActive': True, 'employeeId': 300}
date: 2018-09-15T19:55:00.000+0000
{'City': 'Seattle'}
contians address in input? - True
+-----------+-------------------+----------+-------------------+-----------------+------+---+
|       name|         department|   loginId|        joiningDate|          address|active| id|
+-----------+-------------------+----------+-------------------+-----------------+------+---+
|dummy user1|[Finance, Accounts]|myusername|2018-09-15 19:55:00|{City -> Seattle}|  true|300|
+-----------+-------------------+----------+-------------------+-----------------+------+---+
```

### Now since we have the dataframe, we can create a Temp Table to play with, below is the code to create temp table using `createGlobalTempView("tablename")`
#### Creating the data frame form the temp table using `spark.sql` function
```
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


