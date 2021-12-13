### connecting to Databricks cluster from local using Visual studio code.

 - OS: Centos 7.9 
 - Default Python installed version where 2.7.5 and 3.6 version 
 - In my case i am trying to connect to Databricks runtime 9.1 LTS where the cluster will be running.
     - Databricks runtime 9.1 LTS requires 3.8 python

### To install 3.8.12 version of python 
 Follow this [link](https://rakeshjain-devops.medium.com/how-to-install-python-3-on-centos-7-7-using-yum-and-source-and-set-as-default-1dee13396f7)
 
 ```
 ## Setup libs 
 [root@centos7 ~]# yum install gcc openssl-devel bzip2-devel libffi-devel -y

 ## download package - use wget if curl is not available 
 [root@centos7 ~]# curl -O https://www.python.org/ftp/python/3.8.1/Python-3.8.1.tgz

 ## unpackage the tar
 [root@centos7 ~]# tar -xzf Python-3.8.1.tgz
 
 ## compile the pacakge
 [root@centos7 ~]# cd Python-3.8.1/
 [root@centos7 Python-3.8.1]# ./configure --enable-optimizations
 
 [root@centos7 Python-3.8.1]# make altinstall
 
 [root@centos7 Python-3.8.1]# python3.8 --version

  ## ADDITIONALLY set the link
  sudo ln -sfn /usr/local/bin/python3.8 /usr/bin/python3.8
  sudo ln -sfn /usr/local/bin/pip3.8 /usr/bin/pip3.8  
 ```
 
 - Note i also did 
 ```
 [root@centos7 ~]# alternatives --install /usr/bin/python python /usr/bin/python2 50
 [root@centos7 ~]# alternatives --install /usr/bin/python python /usr/bin/python3.6 60
 [root@centos7 ~]# alternatives --install /usr/bin/python python /usr/bin/python3.8 65
 [root@centos7 ~]# alternatives --config python
  ## choose 3 option 
 ```
 ### check the python version installed now.
 ```
  $ python --version
  ## should list 3.8.12
 ```
 
 ### Note: The pip in my case was still using /usr/bin/python2.7 version
 
 ```
 $ pip3.8 install virtualenv
 ```
 
 ### Install Databricks connect 
 - Followed this (link)[ https://docs.databricks.com/dev-tools/databricks-connect.html]
 ```
 $ pip3.8 uninstall pyspark
 $ pip3.8 install -U "databricks-connect=9.1.*"
 ```
 
 ##### once databricks-connect is installed, create a cluster in Databricks Web UI workspace
 ##### choose 9.1 LTS runtime 
 ##### IN CLUSTER Advanced Option: add below configuration and save it. Start the cluster
 ```
 spark.databricks.service.port 15001
 spark.databricks.service.server.enabled true
 ```
 ### In the workstation Centos vm
  - Create a token for accessing the cluster,
  - issue below command to configure 
 ```
  $ databricks-connect configure
  ## select the cluster url and input the value accordingly 
  ## https://adb-<orgnization>.13.azuredatabricks.net/?o=<orgnization>#setting/clusters/<cluster-id>/configuration
  ## pass the information when prompted 
  URL
  clsuterid
  token
  organization
  port
 ```
  ### once the above configuration is set, issue below commond
 ```
 $ databricks-connect configure
 ```
 
  - i faced an issue, similar as in this [link](https://forums.databricks.com/questions/20144/databricks-connect-test-problem.html)
  ```
  import com.databricks.service.SparkClientManager <console>:23: error: object databricks is not a member of package com import com.databricks.service.SparkClientManager
  ```
  - The issue occured because the default python was set as python3.6.
  - After creating the symbolic link and updating the alternatives with 3.8 version
  - above issue got fixed.
  - Test passed.

### issue below command to get the jar directory of databricks-connect
```
$ databricks-connect get-jar-dir
```  
###  Connecting to visual studio code:
   - Install python extension to the visual studio code, note use latest version of visual code
   - i used yum locker plugin to lock the older version.
   - i had visual studio insider edition installed.

#### After installing the python extension in visual studio
#### from settings, (cntrl + shift + p) search for python interpretor and choose python 3.8.12 installed 

### Select Preference -> settings -> search for python. Change to json view, update below
```json
	 {
		"python.defaultInterpreterPath": "/bin/python",
		"python.linting.enabled": false,
		"python.venvFolders": ["/home/t/.local/lib/python3.8/site-packages/pyspark/jars"]
	 }
```

### To verify the clsuter connection is established
### create a new python file in the visual studio code: enter below code

```py
from pyspark.sql import SparkSession

spark = SparkSession.builder.getOrCreate()

print("TEST - ",spark.range(100).count())
```
-------

 - Use `databricks --help` to configure the cli locally. command `databricks configure --token`
 - Pass in the Cluster name, URL, Generated token, in my case created a profile to connect to dev Databricks cluster.

#### To push the schema to databricks for local development
  - Created the `table1_raw.json` schema definition file locally and use below command to push to Databricks.
  - Below command will copy the file from the local to Databricks
```
$ databricks fs cp ./schemas/table1.json dbfs:/FileStore/custom-datalake-schema/ --profile dev

```

 - To update with any change, if the file is already exits in the Cluster
```
$ databricks fs rm dbfs:/FileStore/custom-datalake-schema/table1_raw.json --profile dev

## then copy the file
$ databricks fs cp ./schemas/table1_raw.json dbfs:/FileStore/custom-datalake-schema/ --profile dev
```

If the token is expired, create a new token and set it using 
```
$ databricks configure --token --profile dev

## This will display the existing Databricks cluster url
## Then request for token <-- provide input
```

Once updated use below command to verify the Databricks access
```
$ databricks fs ls dbfs:/FileStore/custom-datalake-schema/
```
