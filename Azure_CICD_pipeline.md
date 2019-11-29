Not an complete documentation, this contains the abstract idea

##### Different approaches of implementing CI/CD pipeline

##### Using Azure Container Registry and AKS:
  
  **Github/Gitlab** -> use git-ci.yml file to build the docker image and push to docker.io registry (or custom enterprise registry)
  
    use GitLab runner to build and test the image
    
    use Azure Kubernetes service to build the resource, once K8s service exposed to a IP, use the UI to deploy the image from the docker.io
    
  check [link](https://www.youtube.com/watch?v=VafY-qfpM8M)
    
  **Github/Gitlab**
    use create **Azure Container Registry** (ACR) resource and create a Task; the task will access the git using the access token
    
    if any changes are made to code and pushed, ACR task will create the build and contains the image registered
    
    **AKS** resource is created, along with service principle and RBAC to access the ACR resources
    
      Create a deployment "yaml" script which defines the service, number replication, label of the cluster and pods
      
      kubectl CLI is used to list the pods, scale, etc.
        kubeclt apply (deploys the image from the acr which takes the registered ACR and the image information)
   
   Check [link](https://docs.microsoft.com/en-us/azure/aks/)
    
  
