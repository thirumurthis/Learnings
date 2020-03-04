Not an complete documentation, this contains the abstract idea

##### Different approaches of implementing CI/CD pipeline

##### Using Azure Container Registry and AKS:
  
  **Github/Gitlab** -> use gitlab-ci.yml file to build the docker image and push to docker.io registry (or custom enterprise registry)
  ```
    use GitLab runner to build and test the image.    
    use Azure Kubernetes service to build the resource, once K8s service exposed to a IP.
    use K8s UI to deploy the image from the docker.io
  ```
  
  `gitlab-ci.yml` - file has different stages, for example if you build and test stage, each stage will be performing activites and deleting the resource. 
  In this case Build will build the resource, test has to use the built resource, there should be a way to make stages communicate with each other. The build stage should store the artifacts. 
  In the yml file define the `artifacts`: ... section with the location where it should be located.
  
  
  Reference [link](https://www.youtube.com/watch?v=VafY-qfpM8M), [link2](https://www.youtube.com/watch?v=Jav4vbUrqII)
    
  **Github/Gitlab**
  
  Create **Azure Container Registry** (ACR) resource and create a Task; the task will access the git using the access token
    
  if any changes are made to code and pushed, ACR task will create the build: and contains the image registered
    
  **AKS** resource is created, along with service principle and RBAC to access the ACR resources
    
```
      Create a deployment "yaml" script which defines the service, number replication, label of the cluster and pods
      
      kubectl CLI can be used to list the pods, scale, etc.
      $  kubeclt apply 
      
      # (above command deploys the image from the acr which takes the registered ACR and the image information)
```
   
Reference [link](https://docs.microsoft.com/en-us/azure/aks/)
    
  
