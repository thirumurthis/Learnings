#### CD configuration using `.gitlab-ci.yaml` and git runner instance.

```yaml
image: $DCKR_REGSTRY/repo/openjdk/v11:11.0

variables:
   DCKT_REGISTRY: thirurepo.azurecr.io
   GIT_SSL_NO_VERIFY: "true"
   K8S_VERSION: 1.22.0
   HELM_VERSION: 3.7.0
   ENV_TO_DEPLOY: staging
   
.environment_setup: &environment_setup |
  function kubectl_install{
     curl -v "https://dl.k8s.io/release/v${K8S_VERSION}/bin/linux/amd64/kubectl" \
              -o /usr/local/bin/kubectl && sudo install -o root -g root 0755 kubectl /usr/local/bin/kubectl;
  };
  function helm_install {
      curl -v "https://get.helm.sh/helm-v${HELM_VERSION}-linux-amd64.tar.gz | tar -zx && mv linux-amd64/helm /usr/local/bin/helm;
  }
  
 .az_config: &az_config |
 function az_login {
    az login --service-principal -u $GIT_APP_ID -p $GIT_PASSWRD --tenant $GIT_TENANT_ID;
 };
 function az_config{
     if [ -z "$ENV_TO_DEPLOY" ]; then echo "ENVIRONMENT CAN'T BE NULL. Using STAGING env" && ENV_TO_DEPLOY=staging; fi
     AZ_SUBSCRIPTION="XXXXXXXXXXXX"  # either hard code this value or fetch from the helm chart using yq/jq utils.
     AKS_RESOURCE_GRP="YYYYYYYYYYY"
     AKS_CLUSTER_NAME="ZZZZZZZZZZZ"
     az account set -s $AZ_SUBSCRIPTION
     az aks get-credentials -a --resource-group $AKS_RESOURCE_GRP --name $AKS_CLUSTER_NAME 
 }
 
 .update_tags_for_k8s: &.update_tags_for_k8s
 function update_tag{
    if [ -z "$NAME_OF_CHART ]; then echo "CHART NAME can't be empty; exit; fi;  ## check if this really stops the flow
    if [ -z "$MICROSERVICE-NAME ]; then echo "PROJECT NAME can't be empty; exit; fi;  ## check if this really stops the flow
    
    export GIT_URL=$(echo $CI_REPOSITORY_URL);  # check if the git repo url has any special chars with user account info
    export GIT_REPO=https://$GIT_USERACCNT:$GIT_PASWRD$GIT_URL$GIT_URL   # pass username and password along with the url
    
    mkdir temp
    cd temp
    
    git clone $GIT_REPO  # the repo where the helm-chart created will be copied, say the folder name helm-chart/first-chart
    
    ## if multiple charts are managed, probably use and new argument to pass with and navigate here
    cd helm-charts/$NAME_OF_CHART
    
    ## if you have any shell script for setting up any environments etc, can be invoked by providing permissing
    ## below script to update the version number in the helm chart - staging.yaml or uat.yaml, etc file
    ## deploy.sh -> logic will read the values-staging.yaml file fetch the deployment version if it is managed there.
    ## values-<env>.yaml, can be used to manage the version of different microservices and read here
    chmod +x deploy.sh
    ./deploy.sh $NAME_OF_CHART $ENV_TO_DEPLOY $MICROSERVICE-NAME $BUILD_VERSION
    
    ## Below script will add tagging to the images created and pushed to ACR registry
    chmod +x create-tags.sh
    
    ## create a tag with incremented version number and push back to git
    ./create-tags-in-git.sh $EVN_TO_DEPLOY
    
    if[ -n "$(git status --procelain=v1)" ]; then git -c "user.name=$GITUSERACCNT" commit -am "Tagged git for deployment";
    git push $GIT_REPO --all  
 }
 
 k8s-deployment:
    stage: deploy
      - *azure_setup
      - az_login
      - az_config
      - *setup_config
      - kubectl_install
      - helm_install
      - *configure_tags
      - update_tag
    script:
      - cd helm-chart
      - ./deploy-using-helm.sh $k8s-namespace $releaseName $ENV_TO_DEPLOY $OPTS  # assume the script takes info of namespace, relae name, environment, options if needed
    after_script:
      - echo "Completed"
    only:           # Use the only:refs and except:refs keywords to control when to add jobs to a pipeline based on branch names or pipeline types. 
      refs:
        - trigger    # For pipelines created by using a trigger token.
        - web        # For pipelines created by using Run pipeline button in the GitLab UI, from the project’s CI/CD > Pipelines section.
 
```
  - now with the above setup we can use CURL command to deploy
  ```
   # branch= master
   $ curl -X POST \
          -F toke=<az auth token> \
          -F "ref=<branch>"  \
          -F "variables[ENV_TO_DEPLOY]=staging" 
          -F "variables[MICROSERVICE-NAME]=microservice1"
          -F "variables[NAME-OF-CHART]=microservice1"
          -F "variables[BUILD_VERSION]=1.0.3
          https://<gitlab.com>/api/v4/projects/<id>/trigger/pipeline
  ```

- NOTES: [link](https://docs.gitlab.com/ee/ci/yaml/index.html#onlyrefs--exceptrefs)
```
 // say if the file that contains the subscription is managed witing the helm-chart repo in git, using below will be more robust
 //feting the subscription from the file named cd-pipleing.yaml, looking for properties env.staging.azsubscription. using yq version4
 eg. $(yq e 'env.$ENV_TO_DEPLOY.azsubscription' helm-chart/cd-pipeline.yaml)
```

 - Example of `only` and `except`:
 ```yaml
 job1:
  script: echo
  only:
    - branches

job2:
  script: echo
  only:
    refs:
      - branches
 ``` 
 
 ```yaml
 # value-staging.yaml
 deployment:
   general-version: 1.0.0
   microservice1: 1.0.3
   microservice2: 2.0.4
 ```
 
 ```sh
 #!/bin/sh
 # deploy.sh
 
 # hold the parameter in a variable
 CHART-NAME=$1   # this will be holding the folder within the helm-charts/ folder, like microservice1,2,...n folder
 DEPLOY-ENV=$2   # which environment to be deployed
 MICROSERVICE-NAME=$3  # which microservice needs to be updated or project also 
 VERSION-TO-DEPLOY=$4
 
 # cd $CHART-NAME   #This is not necessary since already the path is set in yaml if not then perform this
 
 cat values-$ENV_TO_DEPLOY.yaml > tempFileToUpdateVersion.yaml
 
  yq w tempFileToUpdateVersion.yaml deployment.${i} ${VERSION-TO-DEPLOY} > temp.yaml
  temp.yaml > tempFileToUpdateVersion.yaml
 
  cat tempFileToUpdateVersion.yaml > values-$DEPLOY-ENV.yaml
  rm tempFileToUpdateVersion.yaml
 ```
