

### 1. Create a service principle
  - In portal select `App Registration` and follow the steps, by clicking Add link.
  - Provide a name for service prinicpal
    - This provides a Applicaton (client-id)
    - Also create a create a secret. Azure will provide this value copy it. since this will not be disaplyed once closed.

Note: The client-id and the secret kye from SP will be used to access the key vault

### 2. Create the key valut.
  - In portal search `key vault`, and click Add
  - Add the necessary secrtes (key- value pair)
  - Click `Access Policies`, click Add new and include the service prinicpal we created. 
     - Provide access and permission options.
   
  Note: We can add certificate as well.

in spring boot java app, after including the necessary `azure-keyvault` dependency, in applicaiton.properties 

```properties
azure.keyvault.uri=<https-url-of-the-key-vault>
azure.keyvault.client-id=<client-id-or-application-id-created-in-service-principal>
azure.keyvault.client-key=<secret-key-created-in-service-principal>
```
