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
