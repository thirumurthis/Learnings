### To automate the cookbook build



##### chef .gitlab.ci.yml

```yml

# define Docker registry url
# note group variables inherited like APP_ID, SECRET, TENANT_ID
variables:
  DOCKER_REGISTRY: < docker-registry-url-ACR-path-when-usingAzureContainerRegistry>
  CONTAINER_REGISRY_NAME: <name-of-the-registry-in-ACR>
  GIT_SSL_NO_VERIFY: "true"

stages:
  - deploy
  
deploy:
   stage: deploy
   only:
     - master
   script:
     - az login --service-principal -u $APP_ID -p $SECRET --tenant $TENANT_ID
     - az account set -s "Subscription-name"
     - az acr login --name $CONTAINER_REGISRY_NAME
     
     - docker build -t <acr-url/path/folder/project:vesion  --build-arg "appidOfSP=$APP_ID" --build-arg "secret=$SECRET" --build-arg "tenantid=$TENANT_ID"
```
