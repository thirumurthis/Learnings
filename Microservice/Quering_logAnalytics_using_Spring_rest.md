#### Lets consider the AKS is pushing the logs to Log Analytics.

  - Create a service prinicipal to access the LogAnalytics API.
  - Make sure to provide Role Assignment in Access Control (IAM) 
 
 For more instruction check, [Link](https://dev.loganalytics.io/documentation/1-Tutorials/ARM-API)
 
 ### First lets try hitting the Query with the CURL command.
 ##### STEP 1: Below will obtain the bearer token with the service prinicpal
  ```
  $ curl -X POST -d 'grant_type=client_credentials&client_id=<Client ID of the ServicePrincipal>&client_secret=<Secret added in the service Principal>&resource=https://management.azure.com/" https://login.microsoftonline.com/<TENANT-ID>/oauth2/token
  ## note if -d option is included curl will automatically treat it as POST request
  ```
  
  #### STEP 2: Use Bearer token and hit the url with the query
  
  ```
  $ curl -vX POST -H "Authorization: Bearer [TOKEN]" -H "Content-Type: application/json" -H "Prefer: response-v1=true" -d @query1.json https://management.azure.com/subscriptions/<subscription-of-oms-loganalytics-workspace>/resourceGroups/<Resource-group-name-of-workspace>/providers/Microsoft.OperationalInsights/workspaces/<Workspace-name>/api/query?api-version=2020-08-01
  
  ## @query1.json -> is a file which contains the simple query part of the link above.
  ## above command will return the query result
  ```
  
  In order to implement the same, we can use the Spring boot rest template.
  
  ```java
  // setting httpheader 
  HttpHeader header = new HttpHeaders();
  headers.set("Content-Type",MediaType.Application_FORM_URLENCODED_VALUE)
  
  bodyoftheRequest = "grant_type=client_credentials&client_id"+logAnaytlicsAppid + "&client_secret="+logAnalyticsecert+"&respurces=https://management.azure.com/"

  HttpEntity<strig> tokenEntity = new HttpEntity<>(body,headers);
  try{
     ResponseEntity<String> bearerTokenresp = restTemplate.exchange("https://login.microsoftonline.com/<tenantID>/ouath2/token", headers);
     // parse the response for earer toekn
     // check the status of the request to be success.
     if(bearertokenResp.getStatusCode().is2xxSuccessful()){
       //split the response where .. response will be access_token : "token..."
       String tokenid = bearertokenResp.getBody().split("\"access_token\":\"")...
       //using http headers and rest template create the url to fire the query to obtain the result from log analytics
  }
  ```
