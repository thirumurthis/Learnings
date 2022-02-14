- Databricks has already set with the secret scope

- Below code can be used in single command let or seperate command within the Databricks notebook.

```py
# If any of the widgets are set it can be removed by using below command 
dbutils.widgets.removeAll()
# To create a widget where user can select the evironment can be achieved with below code.

if not ('DeployEnv' in dbutils.notebook.entry_point.getCurrentBindings()):
  # name of the widget, default value, dict of values to select
  dbutils.widgets.dropdown("DeployEnv", "None", ['None', 'DEV', 'TEST'])

# Pass in the inputEnv as parameter or select from widget 

inputEnv = dbutils.widgets.get(name='DeployEnv')
print(f"DeployEnv {inputEnv} will be used")
if inputEnv == 'None':
  dbutils.notebook.exit("Select an Env value from the widget dropdown from left top of this notebook.")  

# 
# If any of the widgets are set it can be removed by using below command 
dbutils.widgets.removeAll()
# To create a widget where user can select the evironment can be achieved with below code.

if not ('DeployEnv' in dbutils.notebook.entry_point.getCurrentBindings()):
  # name of the widget, default value, dict of values to select
  dbutils.widgets.dropdown("DeployEnv", "None", ['None', 'DEV', 'TEST'])

# Pass in the inputEnv as parameter or select from widget 

inputEnv = dbutils.widgets.get(name='DeployEnv')
print(f"DeployEnv {inputEnv} will be used")
if inputEnv == 'None':
  dbutils.notebook.exit("Select an Env value from the widget dropdown from left top of this notebook.")  
inputEnv = inputEnv.lower()
scopename = f"azure-us-{inputEnv}-scope"
storage_acct_name = f"app-storage-us-adls-{inputEnv}"

# since the scope is set to a key vault earlier we can use the scope directly here
# The key valut created and set as a scope in databricks work space, will contain the 
# service prinicple created app(client)id, directory(tenant)id, and stored secret value.
app_or_client_Id=dbutils.secrets.get(scope=scopename,key="name-of-the-key-from-keyvault-referring-appid")
tenant_or_directory_Id=dbutils.secrets.get(scope=scopename,key="name-of-key-from-keyvault-referring-TenantId")
# below will be the secret created within the service principle either in portal or using az cli
secretValue=dbutils.secrets.get(scope=scopename,key="name-of-key-from-keyvaut-referring-Secretkey")

# Define Spark config dictionary for mounting to DBFS to ADLS via service principal

configs = {"fs.azure.account.auth.type": "OAuth",
          "fs.azure.account.oauth.provider.type": "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
          "fs.azure.account.oauth2.client.id": app_or_client_Id),
          "fs.azure.account.oauth2.client.secret": secretValue,
          "fs.azure.account.oauth2.client.endpoint": f"https://login.microsoftonline.com/{tenant_or_directory_Id}/oauth2/token"}

# mount to the container for ahm
mountPnt = "/mnt/my-storage/demo-app"

# 
# Below command can be used for unmounting the container
dbutils.fs.unmount(mountPnt)

# only matching mountPnt will be created or checked
if not any(mount.mountPoint == mountPnt for mount in dbutils.fs.mounts()):
  print(f"Mount {mountPnt} to DBFS")
  dbutils.fs.mount(
    # pass in the container name 
    source = f"abfss://<container-name>@{storage_acct_name}.dfs.core.windows.net/",
    mount_point = mountPnt,
    extra_configs = configs)
else:
  print(f"Mount point {mountPnt} already mounted.")

# to test and list the mount that was created 
%fs ls /mnt/my-storage/demo-app

```


----------

### Below is the blog content

----------

## Pre-requisites

   - Azure ADLS Storage account already setup to understand this blog further
      - Create a Service Principal
      - Create key vault 
           - Store the Tenant Id, Client Id and the Secret in the key vault
           - Key vault is a key value store, in this case provide different key name to store the values

   - In order to understand below block a knowledge of Databricks will help better.

## How to add the key vault to the Azure Databricks Secret Scopes

1. Login to  Azure Portal, launch the Databricks Workspace 

2. From the Databricks workspace, in the address bar of browser append `#secrets/createScope` to the URL address and click enter to navigate to Secret Scope form:

3. In the Scope screen fill the fields as below

   - **Scope Name**, fill in the scope name (any name example  “db-app-demo-scope”. 
   - **DNS Name** fill the key vault DNS name
   - **Resource Id** fill the Key vault Resource Id name.

4. Hit Create button  

**Note:** 
  - To fill the `DNS Name` and `Resource Id` open the Azure key vault in sperate browser, copy the DNS name and Resource id.

Reference [Link](https://docs.microsoft.com/en-us/azure/databricks/security/secrets/secret-scopes)

### Using databricks-cli to view the created secret scope.

Note: Databricks-cli is applicable only in the Cloud version or paid version and not available in community edition.

Once the secret is created, we can view the scope information and associated key vault using `databricks-cli`. Below cli command
  ```
  databricks secrets list-scopes --profile  my-cluster
  ```
## Impacts of Service Principal renewal to Databrick ADLS mount
  - Before diving into the code details, lets see impacts of what happens when already mounted Databricks ADLS Mount when the Service Principal is renewed.

 ### Impact of renewing Service Principal on Mounted ALDS storage in Databricks

    - We developed a Databricks job for our business requirement and the job uses the mounted ADLS storage to access the orc file for processing.
    - To adhere to enterprise security policy after N days we renewed the Service Principal where new secret was created. 
    - After renewal we get below exception and the jobs where failing. 

```
response '{"error":"invalid_client","error_description":"AADSTS7000215: Invalid client secret is provided.
``` 

This blog is based my stack-overflow [question](https://stackoverflow.com/questions/70887371/azure-databricks-job-fails-to-access-adls-storage-after-renewing-service-princip). 


## Mounting the ADLS Storage in Databricks workspace

- Databricks has already set with the secret scope

- Below code uses the scope to access the key vault and configures the Spark session.

Note:  Below code can be copy pasted into a single command let within the Databricks notebook and executed.

```py

scopename = "db-app-demo-scope"                         # sample name
storage_acct_name = "app-storage-accnt-name"     # sample name
container_name = "mycontainer"                                 # sample name

# since the scope is set to a key vault earlier we can use the scope directly here
# The key vault created and set as a scope in databricks work space, will contain the 
# service principal created app(client)id, directory(tenant)id, and stored secret value.

# Note: we need to provide appropriate name below

app_or_client_Id=dbutils.secrets.get(scope=scopename,key="name-of-the-key-from-keyvault-referring-appid")
tenant_or_directory_Id=dbutils.secrets.get(scope=scopename,key="name-of-key-from-keyvault-referring-TenantId")
# below will be the secret created within the service principle either in portal or using az cli
secretValue=dbutils.secrets.get(scope=scopename,key="name-of-key-from-keyvaut-referring-Secretkey")

# Define Spark config dictionary for mounting to DBFS to ADLS via service principal

configs = {"fs.azure.account.auth.type": "OAuth",
          "fs.azure.account.oauth.provider.type": "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
          "fs.azure.account.oauth2.client.id": app_or_client_Id),
          "fs.azure.account.oauth2.client.secret": secretValue,
          "fs.azure.account.oauth2.client.endpoint": f"https://login.microsoftonline.com/{tenant_or_directory_Id}/oauth2/token"}

# mount to the container for ahm
mountPnt = "/mnt/my-storage/demo-app"

# Below command can be used for unmounting the container
# If the container is already mount point with that name, we simply unmount here .

dbutils.fs.unmount(mountPnt)

# only matching mountPnt will be created or checked

if not any(mount.mountPoint == mountPnt for mount in dbutils.fs.mounts()):
  print(f"Mount {mountPnt} to DBFS")
  dbutils.fs.mount(
    # pass in the container name 
    source = f"abfss://{container_name}@{storage_acct_name}.dfs.core.windows.net/",
    mount_point = mountPnt,
    extra_configs = configs)
else:
  print(f"Mount point {mountPnt} already mounted.")

# to test and list the mount that was created 
%fs ls /mnt/my-storage/demo-app
```

## Accessing the mount point in Databricks notebook using magic command`%sql` query
  - Say if we have a orc file with some data content, we can use the sql query and access the mount point directly to query data. 

```
# note I had a python note book and used the below sql magic command

%sql select * from orc.`/mnt/my-storage/demo-app/orc/demofile.orc`
```

 - Wildcard support, say if we have a folder with orc file we can use * like below

```
%sql select * from orc.`/mnt/my-storage/demo-app/orc/*`
```
