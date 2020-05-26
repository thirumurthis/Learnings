### Generally for AKS, the master or controll plan is managed by the Azure itself.
  - control plane scalability etc is handled by the cloud platform.
  - Recovery and backing up of master is taken care of the cloud platform.
  - When deploying an AKS service environemnt, the master node (environment) the number of master node that nees to exists is handled by the Azure itself.
  - we only manage the worker nodes, like numbe of workers needed with compute or gpu intense nodes.
  
##### In order to work with AKS, first we need a private ACR (Azure Container Registry)

##### Below steps helps setup the ACR, prerequisties to have a Azure account with IAM access.

- Create the own ACR registry is private registry, this ACR will be within the same systems domain that is placed within the same resource location and resource group like the other AKS deployment followed in this section or course. (means we will be pulling the container from local resource)
- Prerequistes : `$ az login`

 - Create a resource group for grouping all the resource.
```
$ az group create --name demo-rg --location westus
```

- Create the ACR using below command
```
$ az acr create --resource-group demo-rg --name aksdemo1 -sku Basic
```

- Login to the ACR registry (we created this with name aksdemo1)
```
$ az acr login --name aksdemo1
```

#### Create a image in docker and push it to this registry (once we are logged in)
  - the image can be pulled to the local machine and push that image to the ACR we created.
 
 ```
 ## check the docker/ folder of my github for commands and building images.
 $ docker build -t firstapp:v1 .
 
 ## once image is built.
 ```
 
 ### The image should be pushed to the private ACR so AKS can use it.
   - First in order to push the image to the ACR we need to know the `ACR` name.
   - use the below command to identify the name
```
# below command will list the name of the ACR registry we created
$ az acr list --resource-group demo-rg --query "[].{acrLoginServer:loginServer}" -o tsv

## mostly it would be aksdemo1.azurecr.io
```
   - Possibly, since we are using the shell/bash, just store the ACR name to a variable using `export ACR_NAME=aksdemo1.azurecr.io`.
   
#####  The docker image needs to be tagged before pushing
```
$ docker tag fistapp:v1 ${ACR_NAME}/firstapp:v1
```

##### Check the docker images for updates on the above command
```
$ docker images
```

##### Push the image to the ACR container using below command:
```
$ docker push ${ACR_NAME}/firstapp:v1
```
 Note: since we are already logged in, it will allow it.

##### To verify the image is pushed and exists in the ACR use below command:

```
$ az acr repository list --name aksdemo1 -o tsv
```

##### To make sure whether it can be pulled from the ACR registry, just to make sure the setup is working

```
## remove the image (if the container is running remove it using $docker rm <container>
$ docker images
$ docker rmi <image-id or (registry-name/image-name:version)>

## once removed, they pull the image
$ docker pull <image-registry/image-name:version>

## Run the image locally to validate (name is set with --name)
$ docker run --rm --name firstapp -p 8080:80 -d <image-registry:image-name:version>
// in this case <image-registry:image-name:version> = ${ACR_NAME}:firstall:v1

## verify the output
$ curl localhost:8080
```

##### Creating a `Service prinicpal` account or `service` account
    - This account is required, it allows AKS cluster to interact with other Azure resources.
    - In this case the AKS cluster needs to communicate to the ACR to pull the images, etc.
   
 - Create service principal using az command  
```
$ az ad sp create-rbac --skip-assignment
## ouputs a list of values with service cridentials, just copy this values it is required in future.
```

- Obtain the ACR resource ID, we need this to setup the role assignment in the Service principal.
```
$ az acr show --resource-group demo-rg --name aksdemo1 --query "id" -o tsv
```

- Create a role assignment in the Service principal
  - we need ACR Resource ID and application id (appId) from the Service prinicpal.
```
$ az role assignment create --assignee <application-id> --scope <acr-resource-id> --role Reader

### output the json on success full creation 
// we are creating role assignment so that the ACR can be accessed.
```
Note: 
  - The Service prinicple techinque used above is just a lighter way of doing.
  - in production system the best practice is to associate a Azure Active Directory (AAD) integration.

##### Add and Enable an extension for AKS in the az command.
  - we can use this for auto scale and other capability
```
$ az extension add --name aks-preview
// Note: it was preview version when writting up this.
```

##### Use the Application-id and password, displayed during the service principal creation.
```
## since we already created a service principal for the ACR with the READER role assignement.
## we will need to create AKS to access the ACR with the application id and the password

$ az aks create \
--resource-group demo-rg \
--name demoAKSCluster \
--node-count 1 \
--max-pods 10 \
--kubernetes-version <version-check-doc/1.12.4> \
--generate-ssh-keys \
--enable-vmss \
--enable-cluster-autoscaler \
--min-count 1 \
--max-count 3 \
--service-principal <application-id> --client-secret <password>
```

Note:
  - the az extension we added include `--enable-vmss`, `--enable-cluster-autoscaler` and `--min-count` and `--max-count` associates with autoscaler.
  - right now don't use it in prod since it is preview.
  
##### We need to verify whether we can talk to the AKS cluster.
  - Prerequisites: `kubectl` command needs to be available 
```
# to install the kubectl command
$ az aks install-cli
```

 - Next we need the credentials to talk to the AKS cluster
    - There are two types of credentials
       - admin level
       - user level

