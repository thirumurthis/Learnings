```
#! /bin/bash

export RG=<resource-group>
export DEVELOPERNAME=<developer-name-for-tag>
export STORAGENAME=<storage-account-name>
export ENV=<environement>

az deployment group create --resource-group $RG --template-file "arm_adls.json" --parameters "arm_adls_params.json" --parameters subscription=$SUBID storageAccntName=$STORAGENAME env=$ENV location=$LOCATION  deployer=$DEVELOPERNAME

```

- Below are the parameters that needs to be passed to the above shell scrip.
- once saved, execute the script with appropriate values. 

 - arm_adls_param.json
```
{
    "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentParameters.json#",
    "contentVersion": "1.0.0.0",
    "parameters": {
        "deployment_name": {
            "value": "demo-adls-gen2-deployment"
        },
        "pTag": {
            "value": "demo-project"
        },
		"appTag": {
            "value": "demo-app"
        },
		
        "dsTag": {
            "value": "manual-deploy"
        },
        "location": {
            "value": "westus"
        },
        "kind": {
            "value": "StorageV2"
        },
        "accessTier": {
            "value": "Hot"
        },
        "Container1Name": {
            "value": "my-app-container"
        }
	}
}
```
 - arm_adls.json
 - Reference [LINK](https://docs.microsoft.com/en-us/azure/templates/microsoft.resources/2021-04-01/deployments?tabs=bicep)

```json
{
    "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
    "contentVersion": "1.0.0.0",
    "parameters": {
        "deployment_name": {
            "type": "string"
        },
        "deployer": {
            "type": "string"
        },
        "subscription": {
            "type": "string"
        },
        "pTag": {
            "type": "string"
        },
        "appTag": {
            "type": "string"
        },
        "dsTag": {
            "type": "string"
        },
        "location": {
            "type": "string"
        },
        "storageAccntName": {
            "type": "string"
        },
        "env": {
            "type": "string"
        },
        "kind": {
            "type": "string"
        },
        "accessTier": {
            "type": "string"
        },
        "Container1Name": {
            "type": "string"
        }
    },
    "variables": {
	    "sku":"Standard_GRS",
        "rgStorageName": "[concat('app-', parameters('env'),'uswest-rg')]",
        "kvName": "app-demo-kv",
        "kvResourceId": "[resourceId(parameters('subscription'), variables('rgStorageName'), 'Microsoft.KeyVault/vaults', variables('kvName'))]"
    },
    "resources": [
        {
            "type": "Microsoft.Resources/deployments",
            "name": "demo-sa-deployment",
            "apiVersion": "2021-04-01",
            "properties": {
                "mode": "Incremental",
                "templateLink": {
                    "uri": "https://commontemplates.blob.core.windows.net/storage-account/2.2/azuredeploy.json",
                    "contentVersion": "2.5.0.0"
                },
                "parameters": {
                    "tags": {
                        "value": {
                            "project": "[parameters('pTag')]",
                            "application": "[parameters('appTag')]",
                            "deploymentSource": "[parameters('dsTag')]"
                        }
                    },
                    "name": {
                        "value": "[parameters('storageAccntName')]"
                    },
                    "deployedBy": {
                        "value": "[parameters('deployer')]"
                    },
                    "accessTier": {
                        "value": "[parameters('accessTier')]"
                    },
                    "sku": {
                        "value": "[parameters('sku')]"
                    },
                    "kind": {
                        "value": "[parameters('kind')]"
                    },
					"location": {
                        "value": "[parameters('location')]" 
                    },
                    "containers": {
                        "value": [
                            {
                                "name": "[parameters('Container1Name')]",
                                "publicAccess": "None"
                            }
                        ]
                    },
                    "omsWorkspaceResourceId": {
                        "reference": {
                            "keyVault": {
                                "id": "[variables('kvResourceId')]"
                            },
                            "secretName": "oms-ws-resource-id"
                        }
                    },
                    "allowBlobPublicAccess": {
                        "value": false
                    },
                    "enableHierarchicalNamespace": {
                        "value": true
                    }
                }
            }
        }
    ],
    "outputs": {}
}
```
