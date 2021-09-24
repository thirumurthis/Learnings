Azure databricks

```
Databricks workspace   Databricks workflow

           Databricks Rutime

Databricks IO (DBIO)     Databricks Serverless

     Databricks Enterprise Security
        Apache Spark 

Azure blob storage    Azure Data Lake Store   

Azure SQL Data Warehouse    Apache Kafka    HadoopStorage


```


  Apache Spark ecosystem
   
   Spark SQL & Dataframes   Stream  MLib Machine learning     Graph
    
    
 Dataframes - structured data, distributed data orgainzed based on column.
 
 Streaming - used for processing realtime data
 
-----------

When the clsuter is created by the user via Databricks, the Azure resource manager (ARM) template is deployed as a Azure resource in the user subscription.

The Databricks control plane - manges creation and deletion of the VM'storage

in addition to the above resources, in the user subscription a manged resource group is deployed that contains a VNet, Security group and a stroage account (blob storage).

Azure databricks ui - enables user to manage the databricks Cluster.

The Blob storage can be used to persist the file in databricks file system. So the data won't be lost even after the cluster is terminated.

Databricks works space stores notebooks, library and dashboars.

Databricks can interact with other Azure service
 - it can ingest, process and output data to many services
  like azure blob storage, azure data lakes, Azure data factory, cosmos db and azure synaps analytics.
 
  It can also work with streaming technologies like Eventhub, Apache Kafka, etc.
  
  Azure Databricks is used in Power BI for generating dashboards.
  
 
 ### what is Azure Databricks Workspace?
  - Workspace is the root folder for azure databricks
  - This is used to cluster in databricks
  - workspace stored notebooks,library and dashboards

  To create an Azure Databricks:
    - Create resoruce group or used one that is already available
    - choose Tier
       Standard Tier 
       Premium Tier  (Standard tier + Role Based Access control option)
       
     -  Select Network:
         - We can deploy the Azure databricks in our own Virtual Network (VNet Ingestion)
         - We can choose No if we don't have VNet

#### Creating Databricks cluster
  - Driver node and worker node 
    - Single node can be used for simple dataset.
    - standard
    - high concurrency
    
   - Interactive cluster - interactive code
   - Job cluster - This runs fast and robust code.

Notebook - contains runnable code in interactive mode 

Create a cluster and attach the notebook

To reduce the cluster start time, we can attach a cluster pre-defined pool of idle instances.
(none or pool instance)

Select the Databricks runtime.

Auto scaling (with enable autoscaling - min and max number of nodes) - by default it will run in 6 nodes

The Databricks workers run the spark executor and other services required for proper functioning.

all the distributed process happens in the worker. 

Driver Node: (Driver Type): maintenance information of all notebook that are attached to cluster.
it also run the apache spark master, that coordinates with spark executors

Driver type and worker type configuration can be same.

----------------------
To access the blob storage from Databricks notebooks.

Upload the json file to the container in blob storage

1. mount the blob storage in the notebook
  - check documentation, using dbutils.fs.mount... 
  for scala code looks like below
  ```
  dbutils.fs.mount(
   source = "wasbs://<container-name>@<blob-storage-name>.blob.core.windows.net/",
   mountPoint= "/mnt/custompath",
   extraConfigs = Map("fs.azure.account.key.<blob-storage-name>.blob.code.windows.net" -> "sas access key")
  )
  ```
  Note: mountPoint - mounts the object storage to DBFS.
  
  Run the cell, if the execution is successful, we can create table
2. Create a table

%sql
Drop table if exists <table_name>;
Create table <table_name>
using json
Options {
 paths "/mnt/custompath/<loaded-json-file-name-in-blob>.json
}


%sql
select * from <table-name>;

========================

#### Data tranformation
 Below we perform Extract data from Azure Data lake store and trasform usign Azure databricks. And finally load the data to Azure Data Sql Warehouse
 
Pre requesties:
 - Azure databricks account
 - Azure Data lake account
 - Azure Storage account
 
 Storage account:
    - blob container name: datacontainer 
 
 data lake store: in data explorer
    - folder named : datacontainer is created.
    
    in order to make Azure databricks to communicate with Azure Data lake, we need to have 
      - service principle id , 
      - tenant id & 
      - secret id.
    
    in order to get the service principle id and tenant id. - we need to use AAD app registration.
       for supported account types (who can use this application or access this API)
        - select Accounts in this organization direcory only (default - single tenant)
         & click registration We get below id's
             - Application (client) id (this is called service principle id)
             - tenant id will be crated
       - Create a secret. 
            - choose expiration, and copy the secret key

    Next Step: 
      - provide access to the service prinicple or App registration in Data lake store.
      - In the data lake store, click the data explored
         in the root folder click on Access , then click add.
         search for the app. (choose this folder and all children)
         
    We need to upload the Json file of vehicle details.
      - from Data explorer and select the folder
      - upload the json to this container.
      
   
   In Databricks create a new notebook
   choose language
   
For Scala:
configuration to connect to data lakes. we already have appid, tenant id, secret
   spark.conf.set("dfs.adls.oauth2.access.token.provider.type","ClientCredential")
   spark.conf.set("dfs.adls.oauth2.client.id","app or client-id") //Application id 
   spark.conf.set("dfs.adls.oauth2.credential","secret") //Authenticaion-key
   spark.conf.set("dfs.adls.oauth2.refresh.url","htts://login.microsftonline.com/<tenant-id>/oauth2/token")
    

val vehicleDetails = spark.read.json("adl://<container>>.azuredatalakestore.net/<container-or-folder-name>/<jsonfilename.json>) 

//to display the details
vehicleDetails.show();

//Transformation

val specificColumns = vehicleDetails.select("column1","column2");
specificColumns.show();

// reaname column

val nameCloumn = specificColumns.withColumnRenames("column2","price")
nameColumn.show()

// next - load the data to sql warehouse
// Create the resource for Azure Synapse Analytics (known as Sql DatawareHouse)

create pool and provide servername and details
 -- create firewall configuration, and add a new rule. by providing the start and end ip address. (0.0.0.0 to 255.255.255.255 for example)
 
After entering to the database 
 Enter a query as `Create master key` in query editor
 
 run the query
 
 - Moving the transformed data i Databricks to Sql DW
 
 NOTE: 
  - Sql dataware house uses Azure blob storage as temporary storage location for moving data between Azure Sql Datawarehouse and Azure Databricks 
 
 
 
 notebook Code in scala:
 ```
 val blobStorage = ""
 val blobContainer = ""
 val blobAccessKey = "sas token"
 
 // create a temp folder: that will be used to move data between Databricks and Sql Datawarehouse
 
 //creating temp folder code
 
 val tempDir = "wasbs://"+blobContainer+"@"+blobStorage+"/tempDirs"
 val accountInfo = "fs.azure.account.key."+blobStorage
 sc.hadoopConfiguration.set(accountInfo, blobAccessKey)
 
 //connect to dataware house in the notebook for loading data
 val dwDatabase = "databasename"
 val dwServer = "servername"
 val dwUser = "username"
 val dwPassword = ""
 val dwJdbcPort = "1433"
 var dwJdbcExtraOption = "encrypt=true;trustServiceCertificate=true;hostNameInCertificate*.database.widows.net;loginTimeout=60"
 
 val sqlDwUserConnect= "jdbc:sqlserver://"+dwServer+".database.windows.net:"+dwJdbcPort+";database="+dwDatabase+";user="+dwUser+";password="+dwPassword
 
 
// Above code will connect to sql datawarehouse

// below code will load the data 

spark.conf.set("spart.sql.parquet.writeLegacyFormat","true")
renamedColumn.write.format("com.databricks.spark.sqldw")
.option("ur",sqlDwUserConnect)
.option("dbtable","VehicleDetails")
.option("forward_spark_azure_storage_credentials","True")
.option("tempdir",tempDir)
.node("overwrite")
.save()
```
// running the above, the will be progress, the data will be loaded via blob storage.
// a new table will be created in the Sql Dw with the table name specified in the code above.

// in the Sql DW query browser you can write the query and find the data loaded.

Above is the ETL a BASIC Transformation

#### Advanced Transformation using UDF (User defined function)

There are many built in function

Below are few functions
```  
min(x:Int,y:Int) :Int
max(x:Int,y:Int) :Int
ceil(x:Double):Double
exp(x:Double):Double
```
Above function sometimes is not enough for requirement, so we might need to create our own function

##### Creating user defined function in Databricks. In this example we apply discout for certain price

In databricks take the new notebook: in scala associate a cluster

1. Extract the details form the azure data store 

   # connect to the blob storage using below code
```   
   spark.conf.set("dfs.adls.oauth2.access.token.provider.type","ClientCredential")
  spark.conf.set("dfs.adls.oauth2.client.id","APP_ID")
   spark.conf.set("dfs.adls.oauth2.credential","secret-name")
   spark.conf.set("dfs.adls.oauth2.refresh.url","https://login.microsoftonline.com/<tenant-id>/oauth2/token")
```   
   ---
   
   # to read the data and display the data
  ``` 
   val vehicleDetails = spark.read.json("adl://<storage-name>.azuredatalakestore.net/<container-name>/<json-file>.json)
   
   vehicleDetails.show()
  ``` 
  ##### Creating a function  
  # below function takes and returns the discounted price
 ``` 
  def CalculateDiscount(price: Double, discount: Double): Double=price-(discoutn/100*price);
 ``` 
  #### Create a UDF to invoke that function 
  
  val discountUDF = udf[Double,double,Double](CalculateDiscount)
  
  #### once the UDF is defined this needs to be regiestered to Spark session
 ``` 
  spark.udf.register("discout_UDF",discountUDF)
  ```
   - # register will help registering the userdefined function to spark session.

 code to create a temp view, to store the vehicle details which retrieved from the azure data lake store
``` 
  vehicleDetails.createOrReplaceTempView("vehical_table")  

   spark.sql("select vehicleId from vehicle_table").show()
```   
#### how to invoke and use the UDF
```
spark.sql("select VehicleId, discount_UDF(Price,20) as discounted from vehicle_table").show()
```
==============================

 #### Advanced tranformation using join
 
   1. upload the customer details (json) into the datalake store.
   2. Now the previous vehicle data is in different json
   
   3. Create new notebook in databricks.
   
       1. we should extract the vehicle details and customer details from the datalake store.
       2. connect to the datalake store, (using App id, secret, tenant id)
   
       3. to read the data use below code
           val vehicleDetails = spark.read.json("adl://container path where json file is present).as("vehicle")
           vehicleDetails.show()
           
       4. similarly to extract the customer details, as above use the corresponding datalake json file
       val customerDetails = spark.read.json("...path").as("customer")
       
       5. to join the data between the vehicle and customer
       
       val customerResult = vehicleDetails.join(customerDetails).where($"vechicle.vehicleId" === $"cstomer.vehicleId");
       # above alias table.
       customerResult.show();
   
       # to fetch 
       customerResult.select("column1","column2"..)
       
       # we can use sql to perform this operation
       # code:
       customerDetails.createOrReplaceTempView("CustomerView")
       vehicleDetails.createOrReplaceTempView("VehicleView")
       
       spark.sql("select c.Colum1 from customerview c inner join vechicleview v on c.vehicleId = v.vehicleId").show()

======================

1. copy data from storage account to data lake using Azure data factory

2. Extract data from data lake using Azure databricks

3. integrate azure databricks with Azure data factory

4. using the azure data factory, connect back to databricks in the workflow and select notebook.

5. use the dragging option to connect pipleine and databricks.
---------------------


Using Azure key vault in Azue data bricks.

 - Create a key valut in azure, and add the secret.
   - Note: make a note of the key valut properties
       Note: DNS name & Resource Id values.
 - Create a secret:
     - provide a name, 
     - Store the access Key of the Storage account (created earlier in secret - eg: simple-secret)
     
 Once the secret is cerated.
 
  - Launch the Databricks, 
    - copy existing URL and add #secrets/createScope
       note: There is no UI to navigate to secrets in Databricks. (the createScope is case sensitive)
       url will be "https://<databricks-url>>#secrets/createScope
    - provide the scope name (same name as the secret name, simple-secret)
    - Leave the Manage Principal : Creator
    - Add Azure Key vault  DNS name and Resource Id (noted earier when creating the Key Vault)
    
Now navigate to the notebook and create one.

Previous approach code:

%scala
dbutils.ds.mount(
soure = "wasbs://<blob-storage-name>@<container-name>.bolb.core.windows.net/",
mountPoint = "/mnt/custompath",
extraConfigs = Map("fs.azure.account.key.<storage-name>.blob.core.windows.net" -> blob_strage_accoing_access_key)
)

# the extraConfigs section will be changed, since we have added the scope with secret holding the storage account access key.

# the code will be looking like below.

%scala
 val blob_storage_account_access_key = dbutils.secrets.get(scope = "<name-of-databricks-secret-created", key="name-of-the-secret-created-in-key-valut")
 
 ## running above code will respond wiht "REDACTED" which means the access was successful from databricks,
 
 Now the above code to mount the blob storage will change as below.
 
%scala
dbutils.ds.mount(
soure = "wasbs://<blob-storage-name>@<container-name>.bolb.core.windows.net/",
mountPoint = "/mnt/custompath",
extraConfigs = Map("fs.azure.account.key.<storage-name>.blob.core.windows.net" -> dbutils.secrets.get(scope = "<name-of-databricks-secret-created", key="name-of-the-secret-created-in-key-vault") )
)

=========================
### CI/CD for Databricks in Azure devops environment.

Create an organization

- sync the Active Directory
- in databricks


=================
Dataframes:
 
 - RDD (resilient Distributed dataset)
 - Dataframes
 - Dataset
 
 Dataframe API was release as an abstraction on top of the RDD, followed later by the Dataset API.
 
 Dataframes are the distributed collections of data, orgainzed into rows and columns.
 



 
