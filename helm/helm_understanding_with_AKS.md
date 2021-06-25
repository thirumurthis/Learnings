Helm: (Below are the command that can be performed)
  - create 
  - install
  - Repo add
  - Search

#### What is a helm at very high level?
Helm is a package of file, that has the information to deploy application in kubernetes.
  - so for every install or deployment there is a version.

##### Structure of the helm chart files:
```
Chart.yaml = includes the metadata for the application, like version, etc. 	

Values.yaml = contains the values that are injected to templates. This is injected at runtime

templates/
   |_ deployment.yaml => sample kubernetes definition file. This has variables to hold the values.

   |_ service.yaml => k8s definitions of service

   |_ _helpers.tbl => this contains pre-defined function, we can create our own user-defined function. So the template can interpret the function.
```
#### Helm 
  - is a package management enable creating, installing and managing application inside K8s easier.
  - we can create a new package or use an existing package.
  - used to install the package to cluster, and query the installed version of package on the kubernetes.
  - helm charts can be easily used to updated, deleted, rollback and  view history in the k8s

##### Commands 

 - Create: 
    - This command or switch is used to create the chart or template. 
 - install:
    - This command or switch will send the request to the helm server or kubernetes API, to create the resource present in the chart.
 - repo add:
    - We can add repo, say repo1 to community repo where we can serach for per-configured charts which can be downloaded and configured to install.


------------  

Refer(1)[https://www.youtube.com/watch?v=wuHpqRJMh6Y]

### Azure related setup - Not related to Helm
 ##### Creating ACR (Azure Container Registry)
   - After creating resource group, issue below command to create ACR. Pre-requsites install azure cli
   ```
   $ az acr create --resource-group <created-rg-name> --name <name-for-acr-resource> --sku Basic
   ```
  ##### Create AKS (Azure Kubernetes Service) 
    - Use below command to start the basic AKS cluster, for production ready with security check documentation
  ```
  $ az aks create -g <resource-grp-name> -n <name-for-aks> --location <westus/eastus> --attach-acr <name-of-the-acr-created-earlier> --generate-ssh-keys 
  
  --attach-acr switch provides permissiong to access the acr by aks cluster.
  --generate-ssh-keys is used for authentication
  
  the command will create a service principle and AKS cluster.
  ```

 ###### Kubectl installation from az command
 ```
 # below installs the kubectl client
 $ az aks install-cli
 
 ### This will setup the Azure version of kuectl
  - The kubeconf file should be included in the environment path if we are working on windows machine
 ```
 
 ##### For `kubectl` command to access the AKS cluster - we need to provide access 
   - use below command
 ```
 $ aks get-credentials --resource-group <name-of-rg> --name <name-of-aks-service>
  ## This command will store the credentials in kubeconfig file automatically, the kube config file will have the ssl certificate added
  ## use overwrite command if needed
 ```

##### Deploying the sample application in AKS 
  - First create an image and push to ACR.
  - 
  - STEP 1: Create a Dockerfile like below to create image.
   ```
   FROM node:latest
   
   WORKDIR /webapp
   COPY package.json ./
   
   RUN npm install
   
   COPY . . 
   EXPOSE 80
   CMD [ "node","server.js"]
   ```
  - STEP 2: Push image to ACR directly using the `az acr build` command
  ```
  > az acr build --image webapp:v1 --registry <name-of-ACR-created-earlier> --file Dockerfile
  
  ## The above command will build the image and push it to ACR repository (check the portal ACR)
  ```
  
 ##### Check if the any pods are running in the AKS using the kubectl
 
 ```
 > kubectl get pods 
 ```
--------------------

### Working with helm chart now!!!

#### Creating helm chart
  - Creating the helm chart, we can use `helm create` command

```
## navigate to a directory where the helm chart needs to be created, use below command

## Below command will only create a sample chart, we need to further develop the chart to create K8s resources using defintion files.
> helm create webapp 

#### A set of files and directory will be created Chart.yaml, values.yaml, set of template files, etc.
```
  **NOTE: For every change made to the defintion file and to be deployed, the version in the Chart.yaml should be updated. Update the appVersion property**
  
  - in the `Values.yaml` file update the `repository` properties with ACR url and image like <url>/<image>:<version> eg: `myacrdemo.azureacr.io/webapp:v1`.
  - if needed update the service related paramter, to expose use LoadBalance type service to access the application.

 #### Installing helm chart
   - Use below command to install the helm chart (note, update the required yaml files before installing)
  ```
  ### The helm chart directory is webapp and 
  > helm install <name-of-the-chart> <name-of-the-chart-template-folder>
  > helm install webapp webapp/
  
  ### if needed remove the uncessary yaml files created under the template directory
  ```
   - Above command will deploy the resources in K8S , use `kubectl` to view the resources.
  
 #### Once the application is deployed, with the loadbalancer External-ip access the webapp application
  ----------------
   ### Below is to view the kubernetes dashboard in AKS
    ###### Viewing kubernetes-dashboard` 
      - In some case we might required to delete the clusterbinding of `kubernetes-dashboard` use ` kubectl delete clusterrolebinding kubernetes-dashboard`
      - This needs to be recreated again:
  ```
  > kubectl create clusterrolebinding kuberentes-dashboard --clusterrole=cluster-admin --serviceaccount=kube-system:kubernetes-dashboard --user=clusterUser
  ```

    ##### To view the kubernetes dashboard using az command
  ```
  ## below command will open dashboard creating a proxy tunnel
  
  > az aks browse --resource-group <name-of-rg-created-earlier> --name <name-of-aks-service-created-earlier>
  
  ### This will open up the browser, prompt to authenticate select the kube config file and click sign in.
  ```
   - The kubernetes dashboard displays the resource deployed. (similar to minikube dashboard)
  --------------------
   
