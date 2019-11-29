##### Helm's - is defined as a pakcage manager for Kubernetes.
This makes deploying application to k8s easy and configurable.

Still uses manifest files defined for k8s but makes it configurable.

**Example:**

    If we need to deploy sonar-cube to k8s cluster, then create the config files for db, webapp, secrets.
    
    The manifest files created and managed in a package, when we wanted to deploy sonar-cube package to k8s.
    
    We just deploy the package which contains the app defined in the package.
  
  
**yaml file contains attributes like:**
  - replication
  - pods
  
  
**Helm creates a template information with set of files**
    templates/_helpers.tpl
    
    templates/deployment.yaml
    
    templates/ingress.yaml
    
    values.yaml - like a parameter file in ARM template, like properties file, which is referenced within other yaml file
    
    Chart.yaml
    
we can add the custom template under the templates folder.
  
  use ```helm package <package-name>``` to package the yaml files. which outputs a package-name.tgz
  use command ```helm install <packag-name>``` to deploy the yaml to container
  
  Note the yaml file contains the info about the docker images, etc.
  
  
