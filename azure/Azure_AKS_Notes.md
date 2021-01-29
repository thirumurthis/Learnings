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

## once removed, then pull the image
$ docker pull <image-registry/image-name:version>

## Run the image locally to validate (name is set with --name)
$ docker run --rm --name firstapp -p 8080:80 -d <image-registry-name:image-name:version>
// in this case <image-registry:image-name:version> = ${ACR_NAME}:firstall:v1
// --rm will remove the container volumes upon removing the container (clean up operation)

## verify the output
$ curl localhost:8080
```

##### Creating a `Service prinicpal` account or `service` account
    - This account is required, it allows AKS cluster to interact with other Azure resources.
    - In this case the AKS cluster needs to communicate to the ACR to pull the images, etc.
   
 - Create service principal using az command {on Azure Active Directory (ad)} 
```
$ az ad sp create-rbac --skip-assignment
## outputs a list of values with service cridentials, just copy this values it is required in future.
## make note of application-id, client-password, etc object info
```

- Obtain the ACR resource ID, we need this to setup the role assignment in the Service principal.
```
$ az acr show --resource-group demo-rg --name aksdemo1 --query "id" -o tsv
```

- Create a role assignment in the Service principal
  - we need ACR Resource ID and application id (appId) from the Service prinicpal.
```
$ az role assignment create --assignee <application-id> --scope <acr-resource-id> --role Reader

### outputs data in json format on success full creation 
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
  - right now don't use it in prod since it is preview (not stable at the time of writting).
  
##### We need to verify whether we can connect to the AKS cluster.
  - Prerequisites: `kubectl` command needs to be available 
```
# to install the kubectl command
$ az aks install-cli
```

 - Next we need the credentials to connect to the AKS cluster
    - There are two types of credentials
       - admin level (creating storage, network resources)
       - user level (normal operation) 

##### below command retrives the credentials and stores it in the location where the `kubectl` command expects to see it. (configuring the credentials)
  - The `~/.kube/config` file is updated or created with the cluster configuration so the `kubectl` command can connect to cluster.
```
$ az aks get-credentials --resource-group demo-rg --name demoAKSCluster --admin
## we are storing the credentials and assigning a admin
## merges the credentials infochanges to the ~/.kube/config location
```

#### use `kubectl version` command to verify the version of the client and the server info.
```
$ kubectl version
$ kubectl version --short
$ kubectl cluster-info
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
- AKS has multiple storage options, most common one is `local storage` to which the pod has directly associated with node, which is mapped to the underlying filesystem on which node it is getting deployed.

- When deploying the Kubernetes environment we are defining the availability and type of the local scratch space (fast/SSD storage). 

- There is no peristent, that is when the pod dies the storage also dies and is not available.

- There are persistent model available as part of Kubernetes environment, which is also supported by AKS. 
   - Classic block storage model 
      - This is a persistent model which can be configured to delete along with the persistent volume claim. (i.e. This persistent claim owns the persistent volume) 
      - Single pod to single block storage connection.
      - Like a static storage, it can be reclaimed if needed.
    
   - AKS offers `auto-provisioning` of file, using `SMB` file service model.
       - This gives the ability that multiple pods can read and write
       - That is we can have one underlying provisioned file resources that shared access accross multiple pods that has read and write access.

##### What are storage class and how to create a new storage class?
   - There are two storage class
       - default (low general purpose, managed disk)
       - managed-premium (high performance)
   - Both of these storages has a reclaim policy which indicates that the underlying Azure disk will be deleted when the pod used it dies or deleted. (this reclaimpolicy can be set in deployment resource.)
   - Both of these reclaim the disk after deleting the persistent volume claim.
      - In kubernetes there are two components to persistent volume and mapping persistent volume claim.
          - persistent volume
          - persistent volume claim
       -> If we create a persistent volume claim and there is no underlying persistent volume, to associate with that claim. Kubernetes backend storage sub system will create the volume for us.
       -> In the above case if we delete the persistent volume claim and if the underlying persistent volume doesn't have the reclaimpolicy as `retain`,  the claim will also delete the persistent volume.
       -> So in this case, when a persistent volume is associated with the Pod, and when the pod is restarted the persistent volume(same storage) will be available, but if we delete the pod and also delete the persistent volume claim the persistent volume also gets deleted.
        
 - When the defineing the `reclaimPolicy: retain` in the deployment manifest, it will retain the persistent volume even after the pod is deleted.
 
 ```
 # manifest that will create storage class content
 
 kind: StorageClass
 apiVersion: storage.k8s.io/v1
 metadata:
   name: managed-premium-retain
 provisioner: kubernetes.io/azure-disk
 reclaimPolicy: Retain
 parameters:
    storageaccounttype: Premium_LRS
    kind: Managed
    ...
 ```
 
##### To create the storage, use the mainfest in the below command.
 ```
 $ kubectl apply -f storage-class.yml
 ```
 
 ##### How to verify the storage classes?
 ```
 $ kubectl get storageclasses
 # lists the storage classes with the details
 ```
 
  - A persistentVolumeClaim request either Disk or file storage of particular storageClass, access mode and size.
  - Kubernetes API server can dynamically provision the underlying storage resources in Azure if there are no existing resources to fulfill the claim. ( i.e. it creates the storage volume).
  - Pod definition includes the volume mount once the volume has been connected to the pod.
  
````
## manifest file
## creating a filename-pvc and mounting them to the container.
....
# deployment
   spec:
      volumes:
       - name: firstacpp-pvc
         persistentVolumeClaim:
             claimName: firstapp-pvc
      containers:
        - image: <ACR_name>/<image-name>:<version>
          imagePullPolicy: Always
          name: firstapp
          volumeMounts:
             - mountPath: "/www"
               name: firstapp-pvc
         ....
 ---
 # service
 apiVersion: v1
 kind: Service
 metadata:
   labels:
      app: firstapp-volume
      name: firstapp-volume
 spec:
    ports:
    - name: web
      port: 80
      protocol: TCP
      targetPort: 80
    type: LoadBalancer
    selector:
       app: firstapp-volume
---
### used to defing the claim which specifies the type of storage, mode and size (check the description above).
### Doing this, this storage claim will be for particular pod
### If we need to create another pod we need to define the its own persistent volume claim
### This is common for standard database style environment
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: firstapp-pvc
spec:
  storageClassName: managed-premium-retain
  accessModes:
    - ReadWriteOnce
  resources:
     requests:
        storage: 2Gi
....
````
##### use below command to deploy the manifest.
```
$ kubectl apply -f firstapp-volume.yml

## use the below command to check the pod
$ kubectl get pod

## the pods takes some time to bring up the pods since pvc resource creation
```

##### Above defined persistent volume store is specific to single pod. What if we need to have a shared persistent or storage, so that multiple pods needs to write and read from the storage class?
 - That is multiple client can read and write to the persistent storage.
 - File type resource where clients need to write/read to it from multiple pods.

 - The File type resoruce is availabe by SMB service model, the AKS provides that option (refer the above notes). So the shared resource can be used by multiple pods to read and write. The management of the file type resource is handled by Azure using connectors.
 
 - In order to use the File storage resource we need to connect this  file storage resource with our AKS cluster. This can be achived by below steps.
    - Discover the specific storage resource name, using below command.
    ```
    $ az aks show --resource-group demo-rg --name demoAKSCluster --query "nodeResourceGroup" -o tsv
    ## This command gives the resource name which is concatenated string of resource group cluster name and location
    ```
    - Use that name to create a storage account (the storage name should be all lowercase, and that group should start with `MC_`). Below is to create the file storage
    ```
    $ az storage account create -n demoaksstorageaccount -g MC_demo-rg_demoAKSCluster_westus -l westus --sku Standard_LRS
     ## the resource group is used to create the account, here the standard is used.
     ## check the documentation whether premium storage can be used 
    ```
  ##### After creating the file resource, now update the manifest file to create a new storage class. Like mount the created storage account.
   ```
   
   kind: StorageClass
   apiVersion: storage.k8s.io/v1
   metadata:
       name: azfile
   provisioner: kubernetes.io/azure-file
   mountOnOptions:
     - dir_mode=0777
     - file_mode=0777
     - uid=1000
     - gid=1000
   parameters:
     skuName: Standard_LRS
     storageAccount: demoaksstorageaccount # the storage account we created in above step (not storage class)
     
   ```
 ##### create the storage class using below command
   ```
   ## content of the file-storageclass.yml is above
   $ kubectl apply -f file-storageclass.yml
   ```
 
 - Create a Role Based Access (RBAC) role binding, this is needed since the provisioner actually needs some data and store that data in this storage within the Kubernetes environment in order to make sure the connection continue to happen and we have right data so that the backend storage engine that are part of Azure environment to be able to talk to this file service when we created persistent volume claim and associate with the resource.
 
 `Note:` 
   -Before (refer the above steps) we created the Perisistent volume claim (PVC) part of the same deployment manifest. 
   - Now we will perform the PVC separetly, since this will be shared among multiple nodes.(create the PVC seprately and then associate it to the resource)
 
 ```
 ## the pvc roles and the content of file looks like below
 ## this contains two section a clusterRole and clusterRolebinding
 ## ClusterRole - defines the policy, we have the metadata name. get or create secrets
 ##  This role is being used by the resource, cannot be see this we need this for file provider to work
 ## ClusterRoleBinding - use the Role at the cluster level and bind to the resource (which is a service account) part of AKS pvc. we are making these to talk to each other. 
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole  # this 
metadata:
  name: system:azure-cloud-provider
rules:
- apiGroups: ['']
  resources: ['secrets']  
  verbs: ['get','create']  
  # we are allowing to read and create secrets, this secrets are used to allow file
  # connection to backend
---
apiBersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: system:azure-cloud-provider
roleRef:
  kind: ClusterRole
  apiGroup: rbac.authorization.k8s.io
  name: system:azure-cloud-provider
subjects:
- kind: ServiceAccount
  name: persistent-volume-binder
  namespace: kube-system
 ```
 
 - Use the below command to deploy the above content place in a file-pvcrole.yml file
 ```
 $ kubectl apply -f file-pvcrole.yml
 ## this will create two resource defined as above clusterrole and clusterbinding.
 ```
##### As mentioned earlier, we create the PVC separately.

```
## the pvc file content.
## The name: azurefile => is the name for pvc which is used to identify and used to identify further.

apiVersion: v1
kind: PersistenceVolumeClaim
metadata:
   name: azurefile  ## This is the name for the preistence volume claim we will be using
spec:
  accessModes:
     - ReadWriteMany  ## allowing many pod to read and write.
  storageClassName: azurefile
  resources:
     requests:
        storage: 10Gi  ## requesting for 10gb 
```

 - To deploy the above content store in file_pvc.yml
 ```
 $ kubectl apply -f file_pvc.yml
 ## creates the pvc resource
 
 ## verify the created pvc and list it using below command
 $ kubectl get pvc
 
 
 ## To check the persistent volume and list it use below command
 ## also display the reclaim policy
 $ kubectl get pv
 ```
 
 #### since we have created the shared volume, we can create pod resources to access those.
 ```
 ....
    spec:
       volumes:
        - name: azurefile ## the created PVC
          persistentVolumeClaim:
             claimName: azurefile 
       containers:
        - image: <ACR_name>/<image>:version
          imagePullPolicy: Always
          name: firstapp
          volumeMounts:
             - mountPath: "/www"
               name: azurefile  ## name of the PV
---
apiVersion: v1
kind: Service
....
....
 ```
 
  - Use the above content store in a file-storageclass.yml file and use below command to deploy.
  ```
  $ kubectl apply -f file-storageclass.yml
  ```
 
 ### once the deployment is successful, how can we write some info to the PV or file resource.
 
 - below command will execute the shell commad just writting the hostname to a file to the mounted www location.
 ```
 ## the inner command gets the pod resource and get the hostname and writes it to a file.
 
 $ kubectl exec -it $(kubectl get pod -l app=firstapp-file -o jsonpath='{.items[0].metadata.name}') --sh -c 'hostname > /www/hostname; cat /www/hostname'
 ```
 
 ##### Cleaning up the storage:
  - Persistent storage is cleaned up `automatically` if the reclaim policy is set to Delete.
  - How to manually delete if we had reclaim policy `retain`, we need to find the underlying resources.
  
  ```
  $ kubectl get pvc
  $ kubectl get pv
  
  $ kubectl delete deployment <deploymentname>
  $ kubectl delte pvc <pbcName>
  $ kubectl dlete pv <pvName>
  ```
 
#### AKS Networking
  - Azure CNI or kubenet
     - CNI model - exposes the pod level ip addressed to the environment (advanced)
     - kubnet - hides the ipaddress info behind the node ip address.
   Note: 
     - The CNI networking needs to be determined before we deploying an AKS cluster.
     - kubenet is simple to start
     
  Additional services:
     - internal load balancer
     - external SLB 
     
  Ingress controller:
      - default host router module (uses DNS on top of load balancer Front end)
      
