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
       - admin level (creating storage, network resources)
       - user level (normal operation) 

##### below command retrives the credentials and stores it in the location where the `kubectl` command expects to see it. (configuring the credentials)
```
$ az aks get-credentials --resource-group demo-rg --name demoAKSCluster --admin
## we are storing the credentials and assigning a admin
## merges the credentials infochanges to the ~/.kube/config location
```

#### use `kubectl version` command to verify the version of the client and the server info.
```
$ kubectl version
## sometimes the client version might be greater than the Server version 
## which is acceptable
```

#### After the above setup, how to make sure we are able to communicate with the AKS Cluster.
```
$ kubectl get nodes
## refer the the az aks create command we mentioned to include 1 node,
## and we should be able to see one node in the output
```

#### How to manage the worker node size and other attributes.
 - The minimum Azure osdisk space is ~30G. in some scenarios we might need a large disk size.
 - Attributes that mostly changes on the worker node after deployment.
   - `--node-osdisk-size`
   - `--node-vm-size`
   - `--max-pods` (scale limit is 110; default is 30)

##### List the available Vm disk size in specific location
```
$ az vm list-size -l westus -o table
```

#### How to deploy the application to AKS.
 - manifest file needs to be created, example: firstapp.yml.
 - This manifest file needs to include the ACR Name and include this part of the image name. <ACR_NAME>/<image-name>:version

- To get the ACR name use the below command
```
az acr list --resource-group demo-rg --query "[].{acrLoginServer.loginServer}" -o tsv
## lists the image info
```

- To deploy the yml file (firstapp.yml)
- Navigate to the yml file location

- Command to launch the application
```
$ kubectl apply -f firstapp.yml
 ## the yml file has a deployment and service resource object.
```

- After the deployment, command to view the service
```
$ kubectl get svc firstapp -w 
 # option -w is watch the status. (just hit ctrl + c to quit.
```

- To verify the output of the deployed application
```
$ curl http://<ip-address-obtained-from-service-command>(optional port number)
```

#### How to scale pods and nodes:

##### Scaling pods:

__`Manual:`__
 - The scaling can be done manually by defining the number of pods with below command.
 - Turning up the number of replicas will increase the number of pods running.
 - when we created the cluster we mentioned the number of pods as 10 (refer above)
 
Using the command below if we need to scale it, just by increasing the replicas.
```
$ kubectl scale --replicas=5 deployment/firstapp-v1
```

 - After the scalling, use the below to see the pods scaled
```
$ kubectl get pods
```

__`Automatic:`__ 
 - Based on the resources utlization we can tell AKS to auto scale.
 - By updating the scalable group.
 - There is a automated way to configure scalling when the CPU or resource utlization is more. 
 ```
 # in the firstapp.yml we need to configure it to turn on the autoscale
 ...
 ...
    resources: 
       requests:
          cpu: 250m   # minum
       limits:
          cpu: 500m   # maximum
 ...
 ```
  Note:
    - After updating this with the yaml file use `kubectl apply` command so that auto scalling will be enabled.  
 
 - Defining an autoscale using below command:
 ```
# using command if the pods resource are not used heavily the pods will scale to 3 
$ kubectl autoscale deployment firstapp-v1 --cpu-percent=50 --min=3 --max=10
 ```
 
  - check status of the pods after autoscalling with
  ```
  # hpa - horizontal pod autoscaler.
  $ kubectl get hpa
  ```
  
  #### Scaling the worker node automatically (node level autoscalling)
    - When the cluster is created with 10 nodes and say if we need to create 25 replicas. (which increasing the pods).
    - So when we update the AKS will autoatically allocate more nodes to execute the pods.
 
 ```
 # delete the already created autoscale group:
 # we are trying to check the other option node level autosacling.
 $ kubectl delete hpa firstapp-v1
 ## horizontal pod autoscaler will be deleted.
 ```
 - Lets now try to increase ther replicas manually
 ```
  $ kubectl scale --replicas=25 deployment/firstapp-v1
 ```
 
 - Monitor the pods status (include -o wide option to display the hostname)
 ```
 $ kubectl get pods -o wide -w
 # since we are watching it will take few minutes to see multiple nodes getting created
 # this is because we created the cluster with number 10 nodes.
 ```
 
 Note: 
  -Cluster autoscaling feature along with AKS auto scaling, this is because we set the cluster as 10 when we create, but the AKS autscales since we scaled the replicas to 25.
 
 
#### Setup the node 
  - Lets make the cluster to have two nodes (more than one node)
```
# disable the autoscale option on the cluster.
$ az aks update --disable-cluster-autoscaler --resource-group demo-rg --name demoAKSCluster 

## update the node count to 2 
 $ az aks scale --resource-group demo-rg --name demoAKSCluster --node-count 2
```
  - We will label the node
```
$ kubectl get nodes
$ kubectl label node <new_node_name> key=value
## key and value to set lable to particular machine.
## we can say that this node has GPU capable.
```
   - Using the label, we can force the pod to be deployed on that particular node/machine.
  ```
  ## the key value will be used in the `firstall-value.yml` file 
  ## spec section of the deployment resource is going to be updated.
  ## this means when this file is deployed it will be running on that specfic node.
  ...
  template:
     metadata:
     ...
     ...
     spec:
      -image:...
      ....
       key: value # This tags that this particular set of pods to  run on the node label defined key : value.
       ...
  ```
  
  Note:
    - Right now AKS support only one node pool.
    - The above will helps if multi node pool is supported by AKS.
 
 - After updating the firstapp-value.yml and firstapp.yml deploy those.
  ```
  # The below will create a single pod and the node.
   $ kubectl apply -f firstapp.yml
   
   # monitor the pods 
   $ kubectl get pods -o wide
   
   # deploy the firstapp-value.yml
   $ kubectl appy -f firstapp-value.yml
   // This will deploy the pod in the specifice node,
   
   $ kubectl get pods -o wide 
   
   also pass this output to awk like, | awk '{print $1 $7}' to print less content
  ```
  
### AKS storage options
  - `Local` scratch space (fast/SSD storage)
    - when the pod dies the storage will not be available.
    - Auto-provisioned persistence
