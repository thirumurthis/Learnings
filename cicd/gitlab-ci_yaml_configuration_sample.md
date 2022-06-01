
 ### Below are few notes on setting up the gitlab-ci.yaml process.
 
  CICD pipeline, use of . and & in the flow.
  Also check for documentation on extends
 
 ```
 variables:
    DEPLOY_METADATA : myproject
    
 include:
    remote: "https://myproject.blobl.core.windows.net/general/secret-detection/2.0/pipeline.yml?ref=projectdeployment-default-pipeleine.yml'
    
 
 ## All the CI variables are stored in the git variables either project level or group level
 .azure_login: &azure_login | 
   az cloud set --nmae "${CI_ENTERPRISE_CLOUD_ENVIRONMENT}"
   az login --service-principal -u "${CI_PROJECT_CLIENT_ID_APPID} -p "${CI_PROJECT_SECRET}" --tenant "${CI_PROJECT_TENANT_ID}"
   az account set -s "${CI_PROJECT_SUBSCRITION}"
   
   
 stages:
   - checkout  # stage is defined in the include remote pipeline
   - deploy  
   
  .deploy:
    stage: deploy
    image: $CI_ENTERPRISE_DOCKER_REPO/project/azure-cli/v2
    dependecies:
      - checkout 
   variables: 
     ARM_PARAM_FILE : "azdeploy.parameters.json"
     ARM_TEMPLATE: "azdeploy.json"
   script:
    - *azure_login    # we defined this part of .azure_login
    - echo "[$(date --utc + '%Y-%m-%d %H:%M:%S.%N %Z)] creating resources "
    - TimeSecond=0
    - test $(az group show --name "${CI_PROJECT_RG}" | wc -c) -ne 0 || az group create --name "${CI_PRJOECT_RG} --location "${CI_PROJECT_LOCATION}"
    - duration="$TimeSecond"
    # the script present in the git path and takes some paramters to execute the az command to deploy arm template.
    - bash "${PATH_SCRIPTS_DIR}/azure/deploy_script_using_arm_template.sh "${CI_APP_NAME}" "${CI_PROJECT_RG}" "{ARM_TEMPLATE}"
    - echo [$date -u -d @"$duration" + '%-Mm %-Ss')] "completed"
    - echo "export PROJECT_NAME=${CI_PROJECT_NAME}-${CI_PIPELINE_ID}-${CI_JOB_ID}" >> $DEPLOY_METADATA
  artifacts
    paths:
      - $DEPLOY_METADATA  # defined in the variable section
  
  deploy:
    extends: .deploy
    
  checkout:
   stage: checkout 
   extends: .checkout_pipline_scripts
 ```
