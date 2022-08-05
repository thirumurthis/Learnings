

- configuring Azure Managed Identities for accessing Azure resources, 
- thus eliminating the need to add credentials in the key vaults.

pre-requesited
 - issue `az login` authenticate using Authenticator app.


Below code does 
 - Enabling managed identity to VM
 - Adds access policy to KeyValut in Azure

```
#!usr/bin/sh

HOST_NAME="my-machine"

VM_HOST_NAME=`az vm list --query "[?contains(name, '$HOST_NAME')].name | [0]" -s <subscription-name> -o tsv`
RG=`az vm list --query "[?contains(name, 'HOST_NAME')].resourceGroup | [0]" -s <subscription-name> -o tsv`

VM_ID=`az vm show --name $VM_HOST_NAME "identity" --resource-group $RG -s <subscription-name> -o tsv`
IDENTITY_TYPE=`az vm show --name $VM_HOST_NAME --query "identity.type" --resource-group "$RG" -s <subscription-name> -o tsv`

if [[ -z "VM_ID" || ! "$VM_ID" =~ SystemAssigned.* ]]; then
# assign system assigned identity to VM_HOST_NAME
az vm identity assign --name $VM_HOST_NAME -r $RG -s <subscription-name> 
else
echo -e "SystemAsigned Identity has already set"
fi

VM_ID=`az vm show --name $VM_HOST_NAME --query "identity" --resource-group $RG --s <subscription-name>`
echo -e "the assigned identity $VM_ID"

# principle 
TENANT_ID=`az vm show --name $VM_ID --query "identity" --resource-group $RG -s <subscription-name> --query "identity.principalId" -o tsv`

KV_POLICY=`az keyvault show --name <name-of-keyvault> -g <azure-rg-name> -s <subscription-name> --query "properties.accessPoliceies[?obectId=='$TENANT_ID']" -o tsv`

## if kv_policy alreday exists is will be not null, else blank

if [[ -z $KV_POLICY ]]; then

az keyvalut set-policy --name <name-of-keyvalut> -g <name-of-resource-group> -s <subscription-name> --object-id $TENANT_ID --secret-permissions Get List

else

echo "policy already exists"
fi

```

- Adding managed identity to Azure MsSQL 
 - Note use Azure Data studio for below code execution from azure vm.
```

CREATE USER [VM_HOST_NAME_WITH_VM_MANGED_IDENTITY_ENEABLED] FROM EXTERNAL PROVIDER WITH DEFAULT_SCHEMA - [dbo];

ALTER ROLE db_datawriter ADD MEMBER [VM_HOST_NAME_WITH_VM_MANGED_IDENTITY_ENEABLED];
ALTER ROLE db_datareader ADD MEMEBER [VM_HOST_NAME_WITH_VM_MANGED_IDENTITY_ENEABLED];
```

- To verify, execute below query in sql

```
# below query should return the details
select * from 
sys.database_role_memebers drm join sys.database_principals role on drm.role_principal_id = role.principal_id
join sys.datatbase user on drm.member_principal_id = user.prinicpal_id
where username = 'VM_HOST_NAME_WITH_VM_MANGED_IDENTITY_ENEABLED';
```
