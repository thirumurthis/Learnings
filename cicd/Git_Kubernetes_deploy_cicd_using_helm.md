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
     AZ_SUBSCRIPTION="XXXXXXXXXXXX"  # either hard code this value or fetch from the helm chart using yq/jq utils. eg. $(yq e 'env.$ENV_TO_DEPLOY.azsubscription' helm-charts/cdpipeline.yaml)
     AKS_RESOURCE_GRP="YYYYYYYYYYY"
     AKS_CLUSTER_NAME="ZZZZZZZZZZZ"
     az account set -s $AZ_SUBSCRIPTION
     az aks get-credentials -a --resource-group $AKS_RESOURCE_GRP --name $AKS_CLUSTER_NAME 
 }
 
 
 
```
