#### `Helm` - Kubernetes package manager

- Install the helm using `chocolatey` from windows, check the helm documentation.

### [`helm hub`](https://hub.helm.sh) is a public repostiroy for pulbic helm charts.
  - Some of the helm hub projects are valid. like nginx, nginx-ingress, etc.
  - documentaion provides installing, etc.
  

#### Install `Minikube` docker driver version. Refer the Minikube documentation on how to install using `Chocolatey` package manager.

#### Below is how to install a helm chart from the helm hub
 ##### `Step 1`: Identify the project from helm hub repo:
  - identify the helm project to be installed. in our case we will install `kube-state-metrics`.
  - Search the kube-state-metrics in the hub, make sure this is managed by kubernetes, by referring the repo.
  
 ##### `Step 2`: `Initialize the repo` for bitnami kube-state-metrics (refer the [documentation](https://artifacthub.io/packages/helm/bitnami/kube-state-metrics))
 ```
  > helm repo add bitnami https://charts.bitnami.com/bitnami
 ```
 ##### `Step 3`: `Issue repo update` to retrive all the relevant charts
 ```
 > helm repo update
 ```
 ##### `Step 4`: `Check if the helm repo` is setup correctly, list the repo
 ```
 > helm repo list
 ### display the name of the repo (bitnami) and url (https://chart...)
 ```
 ##### `Step 5`: `Install kube-state-metrics` in the minikube cluster (under k8s-metrics namespace)
   - i. lets install the kube-state-metrics in specific namespace 
   ```
   > kubectl create namespace k8s-metrics
   
   > kubectl get ns
   ```
   
  - ii. Now, we need to install the kube-state-metrics chart in k8s-metrics namespace
   ```
    > helm install kube-state-metrics bitnami/kube-state-metrics -n k8s-metrics
  ```

  #### `Step 6`: `Validate if the chart is installed` in the cluster
  ```
  > helm ls -n k8s-metrics
  ## displays the chart informaton
  ```
  
  #### To verify the objects created by `kube-state-metrics` explore the k8s-metrics namespace
  ```
  > kubectl get all -n k8s-metrics
  ```
  
  - In order to see what kind of data/metrics are being gathered about the cluster by this chart.
   - we can use the UI, for this we need to forward the service from the k8s cluster to the port on host computer.
   ```
   > kubectl port-forward svc/kube-state-metrics 8080:8080
   ```
   - Then open `localhost:8080` to see the data gathered.
   - This information can be pushed to monitoring agent, like pormotheus.
   
  ### Why Helm?
    - Without the chart, the above installation will be difficult, since we need to have different yaml file to define object
    - Manage those different yaml file, and the use `kubectl apply` or `kubectl create`.
    - Helm speeds of the process.

  #### In order to explore more on the kube-state-metrics chart (the third party) and customize the properties. 
  - List avialable helm commands
  ```
  > helm show help
  ## above list the useful options
  ```
  - List a brief info on the helm chart
  ```
  > helm show chart bitnami/kube-state-metrics
  ### use <repo-name>/<chart-name>
  ```
    - The `values` file is not displayed by the `helm show chart` command.
    - To display the values file, use below command
    ```
    > helm show values bitnami/kube-state-metrics
    ### outputs yaml
    
    > helm show values bitmai/kube-state-metrics > output.yaml
    ### The output file contains all the values that kubernetes turns into object.
    ```
   - The output.yaml file can be updated for customization and used to update the cluster.
 
 #### How to update a new version of the existing chart kube-state-metrics?
    - `helm ls -n k8s-metrics` list the version of chart used, and the APP version is version of the underlying code.
  - If we wanted to run the version 0.4.0 of kube-state-metrics.
  
 ```
 > helm upgrade kube-state-metrics bitnami/kube-state-metrics --version 0.4.0 -n k8s-metrics
 ## the above command will start deploy the new version as pod
 
 > helm upgrade <release> <chart-name> [-flags]
 ```
 
 ### How to develop the helm chart?
 ##### Create a new `helm chart`?
  - use the command line
  - Create a new directory, and navigate to it.
   ```
   >  helm create first-demo-chart
   ## above code will create a set of template files, which will be used for deployment.
   ```
   
  - `chart.yaml` 
     - pre-populated values api-version, name, description, etc.
     - `type` used to tell whether it is an `application` or `library` cart
     - contains high level information that can be passed into the helm template engine.
  - `values.yaml`
     - this file has key-value pair.
     - this file has the image to be used, this is fields are set in deployment, helm will be used by helm
  - `charts folder` 
    - this is used to store sub-charts, that have dependencies to top level chart.
    - this might be third-party chart or library chart.
  - `template/service.yml` 
     - this is a default service object (kubernetes manifest file)
     - here in this file there are some dynamic value reference. like `name: {{ include "first-demo-chart.fullname" . }}`
       - this value is fetched from `_helpers.tpl` file.
      - the usage of `{{ Values.service.port }}` - this is from the Values file.
   - `template/deployment.yml`, `template/ingress.yml`,etc.
   
  ##### all the above helm chart files are passed to the helm template engine.
 
 ### Lets deploy a configMap using helm.
   - we can create the configmap manifest file from scratch within the created helm chart. delete the files within the template folder
   - create a file configMap.yaml, create a manifest file
     ```yaml
       apiVersion: v1
       kind: configMap
       metadata:
          name: demo-first-configmap
       data: 
         port: "8080"
     ```
   - lets install the chart, within the helm chart directory
   ```
   > helm install first-demo-chart .
   ## displays the chart deploy details and the namespace 
   ```
   ```
   > kubectl describe cm first-demo-configmap
   ```
   #### update the configmap.yaml with additional value
   ``yaml
    data: 
      port: "8080"
      filename: "note.txt"
   ``
   
   #### After updating the configMap.yaml, if we need to view the content before deploying it to K8s cluster. Use below command
   ```
   > helm template first-demo-chart .
   ### this displays how the config file would be resolved by the template engine
   ```
   
   #### Now we need to apply the updates in configMap to the k8s cluster using below command.
   ```
   > helm upgrade first-demo-chart .
   ### this will the apply the updated config value changes.
   ```
  
  ### Lets deploy a secrets file using helm
   -  Create a secrets manifest file under template folder.
   - lets use below content
   ```yaml
   apiVersion: v1 
   kind: secret
   metadata:
      name: first-demo-secret
   type: Opaque
   data:
      username: user1  # to be base64 coded
      password: password
   ```
   
    Note: the data for scerets to be base64 coded, using ` echo -n 'admin' | base64` from bash.
 
   - To view the content of the charts use below command
   
   ```
   > helm template first-demo-chart .
   ## now the output will contain the configmap and secrets information
   ```
   - deploy the the secrets to cluster
   ```
   > helm upgrade first-demo-chart .
   ```
   - in cluster, verify the secrets data
   ```
   > kubectl describe secrets first-demo-secret
   ```
  
  ### How to rollback using helm history.
   - How to list the list of histroy to rollback? command to do this is:
 ```
    > helm history first-demo-chart
```
    
   - Now since we know which version that i deployed, we can pick the version from list and rollback using below command
  
```
    > helm rollback first-demo-chart
    ### This command will rollback to the immediate latest version.
```
    
   - Note: if we need to rollback to a older version, get the version from `helm history` command
   
 ```
      > helm rollback first-demo-chart 2
      ## here 2 is the version # obained from the history command of helm
      ## if 2 is listed in the history
 ```
     
  - At any point of time use below command to see the history or rollback version currently used.
 ```
    > helm history first-demo-chart
 ```
   
   
