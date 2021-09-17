```sh

BEARER_TOKEN=$(curl -X POST -d 'grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&resource=https://management.azure.com/' https://login.microsoftonline.com/${AAD-TENANTID}/oauth2/token | jq '.access_token' | sed 's/\"//g')

curl -vX POST -H "Authorization: Bearer ${BEARER_TOKEN}" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/d83e2a57-3aa1-40bc-a380-e45e2fd7e500/resourceGroups/ahm-storage-westus-rg/providers/Microsoft.OperationalInsights/workspaces/ahm-oms-dev/api/query?api-version=2020-08-01
```

check Url [Link](https://dev.loganalytics.io/documentation/Authorization/OAuth2)
