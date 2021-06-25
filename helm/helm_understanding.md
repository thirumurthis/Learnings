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
  
  

    
  
