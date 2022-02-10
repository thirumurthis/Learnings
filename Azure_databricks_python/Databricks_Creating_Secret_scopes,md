Steps:

1. Login to  Azure Portal, launch the Databricks Workspace 

2. Navigate to the create scope page in databricks by appending "#secrets/createScope" to the URL address.

3. 
  1. In "Scope Name", fill in the name example  “app-us-west-<env>-scope”. 
     1.1  (open the Key-vault that needs to be connected in sperate browser, copy the DNS name and Resource id) 
  2. In "DNS Name" fill the key vault DNS name
  3. In "Resource Id" fill the Key vault Resource Id name.

4. Hit Create button  

Reference [Link](https://docs.microsoft.com/en-us/azure/databricks/security/secrets/secret-scopes)

--
Later in order to view the scope info and associated key vault.
  - Install databricks-cli, and use the appropriate command.
  ```
  databricks secrets list-scopes --profile  mycluster
  ```
  Form info refer my stack-overflow [link](https://stackoverflow.com/questions/70887371/azure-databricks-job-fails-to-access-adls-storage-after-renewing-service-princip)
