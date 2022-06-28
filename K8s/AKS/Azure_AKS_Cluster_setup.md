

Create Resource group

$ az group create -l westus -n myproject-aks-local-rg

## using custom template
Assume we have the parameters.json

Before hand we need subnet id 
$az network vnet subnet show -n myproject-aks-subnet -g <resource-group-name> --vnet-name <name-of-the-vnet>

To create the cluster with the template and parameter 
az group deployment create --name deploy-k8s --resource-group myproject-aks-local-rg --template-file mytemplate.json --parameters parameters/mydeploy.parameters.json

refer the documentation 
https://docs.microsoft.com/en-us/azure/aks/learn/quick-kubernetes-deploy-rm-template?tabs=azure-cli

-- IN case if we need to enable ports in the NSG we can use below cli

az network nsg rule create -g <resource-group-name> --nsg-name <project-nsg-name> -n <name-for-inbound-rule> --priority <priority-number-ex-3090> --source-address-prefixes 'VirtualNetwork' --source-port-ranges '*' --destination-address-prefixes 'VirtualNetwork' --destination-port-ranges <port-range-ex-8081-8099> --direction Inbound --access Allow --protocol Tcp --description "Inbound rule for application"

az network nsg rule create -g <resource-group-name> --nsg-name <project-nsg-name> -n <name-for-the-nsb-outbound-rule> --priority <priorty-number-ex-3905> --source-address-prefixes '*' --source-port-ranges '*' --destination-address-prefixes '*' --destination-port-ranges <port-number-ex-5601> --direction Outbound --access Allow --protocol '*' --description "Outbound rule for access."

Additionally if we need to extend the VNET

Create new subnet named 'myproject-aks-subnet' make sure it is big enough IP Address/24
 - if a VNET already exists create within that vnet or virtual network
a. We can use portal to create in a myproject-aks-vnet virtual network
with below property:
NSG: NONE
Route table : NONE
Service endpoints: Microsoft.AzureActiveDirectory
Delegate subnet to a service: NONE

Create new Service principal to access the AKS.

$  az ad sp create-for-rbac --skip-assingment --name myproject-aks-local-sp
$ az ad sp show --id "http://myproject-aks-local-sp"
# grab the output and store it in the azure keyvault
Add below info as well to the AKS

k8sSpObjectID : <object-id> 
k8sSpAppId : <appId>
K8sSpSecret : <password>
  
  
## we can use below are the autoscaler 
az aks nodepool update --resource-group <resource-group> --cluster-name <cluster-name> --name <name-for-pool> --enable-cluster-autoscaler --min-count 1 --max-count 10
