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
    export GIT_URL=$(echo $CI_REPOSITORY_URL);  # check if the git repo url has any special chars with user account info
    export GIT_REPO=https://$GIT_USERACCNT:$GIT_PASWRD$GIT_URL$GIT_URL   # pass username and password along with the url
    mkdir temp
    cd temp
    git clone $GIT_REPO  # the repo where the helm-chart created will be copied, say the folder name helm-chart/first-chart
    cd helm-chart/first-chart
    # if you have any shell script for setting up any environments etc, can be invoked by providing permissing
    chmod +x fetch-display-version-info.sh
    ## fetch deployment version from staging.yaml ($ENV_TO_DEPLOY),
    chmod +x create-tags.sh
    chmod +x deploy.sh
    # create a tag with incremented version number and push back to git
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
      - kubeclt_install
      - helm_install
      - *configure_tags
      - update_tag
    script:
      - cd helm-chart
      - ./deploy-using-helm.sh $k8s-namespace $releaseName $ENV_TO_DEPLOY $OPTS  # assume the script takes info of namespace, relae name, environment, options if needed
    after_script:
      - echo "Completed"
    only:
      refs:
        - triggers
        - web 
 
```

- NOTES:
```
 // say if the file that contains the subscription is managed witing the helm-chart repo in git, using below will be more robust
 //feting the subscription from the file named cd-pipleing.yaml, looking for properties env.staging.azsubscription. using yq version4
 eg. $(yq e 'env.$ENV_TO_DEPLOY.azsubscription' helm-chart/cd-pipeline.yaml)
```
