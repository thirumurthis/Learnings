```sh
## GET the token (authorization)
BEARER_TOKEN=$(curl -X POST -d 'grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&resource=https://management.azure.com/' https://login.microsoftonline.com/${AAD-TENANTID}/oauth2/token | jq '.access_token' | sed 's/\"//g')

## pass the token for executing query
curl -vX POST -H "Authorization: Bearer ${BEARER_TOKEN}" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/${subscription-id}/resourceGroups/${resource-group-name}/providers/Microsoft.OperationalInsights/workspaces/${workspace-name}/api/query?api-version=2020-08-01

Note: in curl if using -d will apply post request

query1.json has the query
```
 - query1.json content.
```json
{ 
 "query" : "ContainerInventory | project Computer, Name, Image, ContainerState, StartedTime, FinishedTime | top 10 by FinishedTime desc",
 "timespan" : "PT1H"
}
```

check Url [Link](https://dev.loganalytics.io/documentation/Authorization/OAuth2)

Using Postman,
  - we can create Evnironment which holds the variable. Say, directoryId holds the AAD tenant id then
     - Add few more variables, so the bearer token can be stored directly in it.
  - The set variables can be used in the url like `https://login.microsoftonline.com/{{directoryId}}/oauth2/token`
  - Update testing section with below value

![image](https://user-images.githubusercontent.com/6425536/140666259-3f8902f7-8ce4-4048-8324-02344cbea545.png)

![image](https://user-images.githubusercontent.com/6425536/140666265-fe3c1287-549f-4376-90a8-854eba4cd0fd.png)

```
var json = JSON.parse(responseBody);
postman.setEnvironmentVariable("azureApp_bearerToken", json.access_token); //where azureApp_bearToken is another variable in evironment
//adding this cose will set the variable current value else,we need to manually add for consequtive request.
```

 Now with the bearer token. environment variabel, logAnalytics_workspace is set to hold APP id of loganalytics
 https://api.loganalytics.io/v1/workspaces/{{logAnalytics_workspace}}/query  with the query will render results
 - The Header is set with Authorization as "Bearer {{zureApp_bearerToken}}"
 - setting the body with below content

```
{ 
   "query": "Perf | where CounterName == 'Available MBytes Memory' | summarize avg(CounterValue) by bin(TimeGenerated, 2h)",
    "timespan": "PT12H"
}
```

----
#### STEP 1:
  - Create Service prinicpal (`Portal -> search Azure AAD -> App registration (left menu blade) -> New Regsitration button`)
    - Provide a name leave rest of the option as default. Click Register button.
    - Make note of application (client) id, etc. objects.
    
#### STEP 2:
  - Open up the Service prinicpal, click the `Certificate & keys`, input some client_secret description. save it
     - Copy the client secret information at this time, since it will NOT be available afterwards.
     
#### STEP 3:
  - Click the `API permission` within the Service principal created.
    - Click `Add permission`  (select the tab `API's my Organization uses`, search for `Log Analytics`
    - Select the `Log Analytics API`, choose `Delegate permission` and `Application permission`
    
#### STEP 4:
  - Search for Log Analytics in the portal, serach for the workspace to be quried.
    - click the `Access Control (IAM)` from left menu, search for "created service principal".
    - To add Role Assignment. (in here request someone who have access to do it)

Query formation:
  
  ##### Fetch the token using below curl query
  ```
  // format template
  curl -X POST -d 'grant_type=client_credentials&client_id=<Application_id/client_id of created SP:STEP1>&client_secret=<client secrets added in SP:STEP2>&resource=https://management.azure.com/' https://login.microsoftonline.com/<TENENT ID in here daa-devops id>/oauth2/token
  
  ```
  
  ##### PASS the token and fetch the query 
  - DIRECT API method
  ```
  // format template
  $ curl -X POST -H "Authorization: Bearer [TOKEN]" -H "Content-Type: application/json" -d @query1.json https://api.loganalytics.io/v1/workspaces/<workspace_id_of_ahm-osm-dev>/query
  
  ```
  - ARM API METHOD
  ```
  // format template
  $ curl -X POST -H "Authorization: Bearer [TOKEN]" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/<subscription of LA workspace>/resourceGroups/<resource-group-name-of-LA-workspace>/providers/Microsoft.OperationalInsights/workspaces/<LA workspace name>/api/query?api-version=2020-08-01
  
  ```
  
  - Note: Passing the query from a file as json (query1.json)
  ```json
  { 
 "query" : "ContainerInventory | project Computer, Name, Image, ContainerState, StartedTime, FinishedTime | top 10 by FinishedTime desc",
 "timespan" : "PT1H"
  }
  ```
  
  
  ------
  
 #### Automated approach using script:
 
 - Fetch and set the Bearer token in variable:
 ```
$ BEARER_TOKEN=$(curl -X POST -d 'grant_type=client_credentials&client_id=<clientid>&client_secret=<secret-key>&resource=https://management.azure.com/' https://login.microsoftonline.com/<tenent-id>/oauth2/token | jq '.access_token' | sed 's/\"//g')
 ```
 - Pass the Token variable  
 ```
$ curl -vX POST -H "Authorization: Bearer ${BEARER_TOKEN}" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/<subscription-id>/resourceGroups/ahm-storage-westus-rg/providers/Microsoft.OperationalInsights/workspaces/ahm-oms-dev/api/query?api-version=2020-08-01

$ curl -vX POST -H "Authorization: Bearer ${BEARER_TOKEN}" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/<subscription-id>/resourceGroups/ahm-storage-westus-rg/providers/Microsoft.OperationalInsights/workspaces/ahm-test-oms/api/query?api-version=2020-08-01

$ curl -vX POST -H "Authorization: Bearer ${BEARER_TOKEN}" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/<subscription-id>/resourceGroups/ahm-storage-westus-rg/providers/Microsoft.OperationalInsights/workspaces/ahm-prod-oms/api/query?api-version=2020-08-01
```
