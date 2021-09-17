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
  - The set variables can be used in the url like `https://login.microsoftonline.com/{{directoryId}}/oauth2/token
  - Update testing section with below value

```
var json = JSON.parse(responseBody);
postman.setEnvironmentVariable("azureApp_bearerToken", json.access_token); //where azureApp_bearToken is another variable in evironment
//adding this cose will set the variable current value else,we need to manually add for consequtive request.
```
