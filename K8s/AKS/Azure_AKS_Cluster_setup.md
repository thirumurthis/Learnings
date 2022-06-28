
Below are some notes on how to deploy clusted using template.

refer the documentation 
https://docs.microsoft.com/en-us/azure/aks/learn/quick-kubernetes-deploy-rm-template?tabs=azure-cli


Create Resource group

$ az group create -l westus -n myproject-aks-local-rg

## using custom template
Assume we have the parameters.json

Before hand we need subnet id 
$az network vnet subnet show -n myproject-aks-subnet -g <resource-group-name> --vnet-name <name-of-the-vnet>

To create the cluster with the template and parameter 
``` 
az group deployment create --name deploy-k8s --resource-group myproject-aks-local-rg --template-file mytemplate.json --parameters parameters/mydeploy.parameters.json
```

-- To enable ports in the NSG we can use below cli inbound and outbound
```
az network nsg rule create -g <resource-group-name> --nsg-name <project-nsg-name> -n <name-for-inbound-rule> --priority <priority-number-ex-3090> --source-address-prefixes 'VirtualNetwork' --source-port-ranges '*' --destination-address-prefixes 'VirtualNetwork' --destination-port-ranges <port-range-ex-8081-8099> --direction Inbound --access Allow --protocol Tcp --description "Inbound rule for application"

az network nsg rule create -g <resource-group-name> --nsg-name <project-nsg-name> -n <name-for-the-nsb-outbound-rule> --priority <priorty-number-ex-3905> --source-address-prefixes '*' --source-port-ranges '*' --destination-address-prefixes '*' --destination-port-ranges <port-number-ex-5601> --direction Outbound --access Allow --protocol '*' --description "Outbound rule for access."
```
 
Mostly below will be the prestep, since we need the VNETs in place
Additionally if we need to extend the VNET

Create new subnet named 'myproject-aks-subnet' make sure it is big enough IP Address/24
 - if a VNET already exists create within that vnet or virtual network
a. We can use portal to create in a myproject-aks-vnet virtual network
with below property:
``` 
NSG: NONE
Route table : NONE
Service endpoints: Microsoft.AzureActiveDirectory
Delegate subnet to a service: NONE
```
 
Create new Service principal to access the AKS.
```
$  az ad sp create-for-rbac --skip-assingment --name myproject-aks-local-sp
$ az ad sp show --id "http://myproject-aks-local-sp"
# grab the output and store it in the azure keyvault
```
Add below info as well to the AKS

ObjectID : <object-id> 
AppId : <appId>
Secret : <password>  
```  
## we can use below are the autoscaler 
az aks nodepool update --resource-group <resource-group> --cluster-name <cluster-name> --name <name-for-pool> --enable-cluster-autoscaler --min-count 1 --max-count 10
```
 
 - In order make the pods to access the Azure Keyvault we can install below 
 https://github.com/Azure/kubernetes-keyvault-flexvol
 
 ```
 kubectl create -f https://raw.githubusercontent.com/Azure/kubernetes-keyvault-flexvol/master/deployment/kv-flexvol-installer.yaml
 ```
 
 
Below is the representation diagram:
 
 ![image](https://user-images.githubusercontent.com/6425536/176097414-d7d6056f-5d73-46b8-bb71-5f2abdba39bd.png)
 
 Note:
  - The resource mostly protected witihin private virtual network.
  - The cluster will not have any pulbic IP associated to be exposed.
  - Use KeyValut to inject the application runtime secrets.
  - AKS deployed to private network doesn't deploy default method of ingress traffic to the cluster, we need to use service resources to expose it.
 - This Cluster instance is deployed with advanced network plugin (Azure-CNI) as virtual network.
 - We can use NSG rules for additional network rules for AKS to properly work within the subnet.
 
 Create set of inbound and outbound rules.
  - Inbound: Allow * from <subnet>
  - Inbound: Allow tcp8001 and tcp9000 from <VNET/private network>
  - Outbound: Allow * to <subnet>
  - Outbound: Allow tcp8001 and tcp9000 to *
