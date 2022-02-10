```sh
az login 
export SUBSCRIPTION_NAME=<subscription-name>
export RG=<resource-group>
export ADLSNAME=<name-for-adls>
az account set -s $SUBSCRIPTION_NAME

SUBSCRIPTIONID=$(az account show --query id | tr -d '"')

# get tenant id
TENANTID=$(az account show --query tenatId | tr -d '"')

# get SP name 
principleName=$(az ad singed-in-user show --query userPrincipalName | tr -d '"')

# get apid
export SERVICEPRINCIPAL_NAME=<serviceprinciple-name>
# Registred app id
SPAPPLICATIONID=$(az ad app create --display-name "${SERVICEPRINCIPAL_NAME}" --credential-description "Demo sp for adls" --query appId | tr -d '"')


SPOBJID=$(az ad sp create --id "$SPAPPLICATIONID" --query objectId | tr -d '"')

export DATE_TO_EXPIRE=365

secretInfo=$(az ad app credential reset --id ${SPAPPLICATIONID} --end-date ${DATE_TO_EXPIRY} --credential-description "secret4adls" --query password | tr -d '"')

#add role for adls for storage 
az role assignment create --assignee-object-id "${SPOBJID}" --role "Blobstore Contributor" --scope "/subscriptions/${SUBSCRIPTIONID}/resourceGroups/${RG}/providers/Microsoft.Storage/storageAccounts/${ADLSNAME}"

## add role for adls queue
az role assignment create --assignee-object-id "${SPOBJID}" --role "Storage Queue Data Contributor" --scope "/subscriptions/${SUBSCRIPTIONID}/resourceGroups/${RG}/providers/Microsoft.Storage/storageAccounts/${ADLSNAME}"


## add the secret to key valut
export SECRETKEY=<secret-key-name>
export KEYVAULTNAME=<vault-name>
az keyvault secret set --name $SECRETKEY --vault-name $KEYVAULTNAME --description "Add SP appid value" --subscription $SUBSCRIPTIONID --value $SPAPPLICATIONID


```
