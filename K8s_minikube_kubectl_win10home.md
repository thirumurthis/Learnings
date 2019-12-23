# Minikube and Kubectl download and install on Windows 10 (no hyperV)

Directly download the executable of Minikube and Kubectl from Kubernetes.io.

For Kubectl executable:
  - Set the Environment PATH to the executable. 
  - In command prompt try `kubectl version` the command should display the version of the kubectl.

_Note:_ Assuming the system already has the VirtualBox installed and working (in my case i was able to provision a VM using vagrant)

After everything is setup issue below command
```
 > minikube start
 
# output: 
* minikube v1.6.2 on Microsoft Windows 10 Home 10.0.17763 Build 17763
* Selecting 'virtualbox' driver from existing profile (alternates: [])
* Tip: Use 'minikube start -p <name>' to create a new cluster, or 'minikube delete' to delete this one.
* Starting existing virtualbox VM for "minikube" ...
* Waiting for the host to be provisioned ...
* Found network options:
  - NO_PROXY=192.168.99.100
  - no_proxy=192.168.99.100
* Preparing Kubernetes v1.17.0 on Docker '19.03.5' ...
  - env NO_PROXY=192.168.99.100
  - env NO_PROXY=192.168.99.100
* Downloading kubeadm v1.17.0
* Downloading kubelet v1.17.0
* Launching Kubernetes ...
* Done! kubectl is now configured to use "minikube"
 
```
---------------

Alternate way to install Minikube and KubeCtl install the choco MS package manger (only an optional approach)

using `cmd` as admin package run the below command to install it.

```
@"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
```
 [Installing-minikube-with-chocolatey-windows-package-manager](https://medium.com/@JockDaRock/installing-the-chocolatey-package-manager-for-windows-3b1bdd0dbb49)

After installation of choco PM then use below command

```
> choco install minikube
```
----------
To start the cluster, use 

```
> minikube start
```
[quick-start on minikube](https://kubernetes.io/docs/setup/learning-environment/minikube/#quickstart)

Then interact with the clusted usign Kubectl command

[Interaction-with-cluster](https://kubernetes.io/docs/setup/learning-environment/minikube/#interacting-with-your-cluster)

```
> kubectl config use-context minikube
 output: Switched to context minikube
 ```
 
 command to view the dashboard:
 ```
> minikube dashboard
```

Once the dashboard ui is avialable:
 in order to retrive the image from docker, run the docker service (using the docker toolbox)
 
 in the command prompt issue below command and input user id and password
 ```
 > cd %USERPROFILE%/.docker
 > docker login
 ```
 
 a **config.json** file will be created. use the kubectl command to create a secret using the config.json.
 [Reference-link-to-before-begin](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#before-you-begin)
 
 ```
 kubectl create secret generic regcred \
    --from-file=.dockerconfigjson=%USERPROFILE%/.docker/config.json \
    --type=kubernetes.io/dockerconfigjson
 ```
 The above can be achived using the UI also usign create secert option.
 
 Deploy the first application in K8s, hello-node
 
 [hello-node](https://kubernetes.io/docs/tutorials/hello-minikube/#create-a-minikube-cluster)
 
```
> kubectl run 
 -- above command is used to run the docker image in the cluster
```

```
> kubectl get pod
-- above command to get the status of the pod
```

```
> kubectl expose deployment hello-node --type=NodePort
 -- above command is used to expose the service to be accessed by the external world
 -- note the --type there are different values, like LoadBalancer, NodePort, ClusterIP
 --- LoadBalancer uses the hyperV of the host machine to get the ip, etc.
```

```
> curl $(minikube service hello-node --url)
-- above command to access the cluster and the data using curl
-- output would be a http response
```

```
> kubectl delete deployment hello-node
-- above command will delete the deployment
```

```
> minikube stop
-- command to stop the minikube 
```

Deployments 
 - important for defining apps and services
 - collection of resources and references
 - typically described in YAML format
 - this file can be deployed accross any K8s 
 
 Deployment of tomcat server - practical [video demo](https://www.youtube.com/watch?v=Vj6EFnav5Mg)
 
 Defining deployment: [resource](https://github.com/LevelUpEducation/kubernetes-demo)
  - pod is an isntance of a container
  - deployment can have any number of pods
  - most deployment has simply one pod 
   -- no redundancy, no separation of service.
  
 1. Create a tomcat deployment.yaml
  ```
    apiVersion: apps/v1beta2
    kind: Deployment
    metadata:
      name: tomcat-deployment
    spec:
      selector:
        matchLabels:
          app: tomact
      replicas: 1
      template:
        metadata:
          labels:
            app: tomcat
        spec:
          containers:
          - name: tomcat
            image: tomcat:9.0
            ports
            - containerPort: 8080
```

Key things to note:
 - image name
 - number of replicas
 - container port - in this case the tomcat to be exposed to 8080
 - name of the application
 
** Note: 
 - K8s will automtaically default to public docker hub if repository is not presented. **
 
 2. Apply **kubectl apply** command
```
> kubectl apply -f ./deployment.yaml
-- note the tomcat deployment yaml is used.
-- apply command will takes the directives from the yaml file to the cluster
```

3. To **kubectl expose** to access tomcat from external world in 8080 port
 ```
 > kubectl expose deployment tomcat-deployment --type=NodePort
  -- --type=NodePort - tells to K8s that we are going to expose or export the containerport 8080 in the deployment file on external port/host
  -- this command exposes the service
 ```
 
 ```
 > minikube service tomcat-deployment --url
 -- This will provide the url to access the tomcat application
 -- use _curl_ command
 ```
 
 ----------------
 
 Command to List pods and its status
 ```
 > kubectl get pods
 -- provides the pods on all the namespace
 ```
 
 Command to list the details of the sepcific pod
 ```
 > kubectl get pods [pod_name]
  -- pod name obtained in the get pods command 
  -- detail about the deployment of the pod
 ```
 
 Command to expose the port
 ```
 > kubectl expose <type name> <identifier/name> [--port-external port[ [--target-port=container-port] --type=service-type
  -- exposes port (TCP or UDP) for a given deployment, pod or other resources
 ```
 
 Command to port-forward - forward port from local machine on which kubectl is running to pod on the remote host on the kubectl is connecting to.
 ```
 > kubectl port-forward <pod-name> [LOCAL_PORT:]REMOTE_PORT]
 -- this is helpful dealing with remote cluster map to local port to a remote pod on a container this command will be useful.
 -- in the above example since we are using minikube in local, this command is similar to expose
 -- example kubectl port-forward tomcat-deployment-xxxxxx 8080:8080
 ```
 
 Command to attach to a pod that is already running process to view output
 ```
 > kubectl attach <pod-name> -c <container>
 -- helps to attach to a process already running 
 ```
 
 Command to **debug** what is going inside a container
 in case to wanted to execute a command within a conatiner
 ```
 > kubectl exec [-it] <pod name> [-c container] --COMMAND [args..]
 -- -i will pass stdin to the container
 -- -t will specify stdin is a tty
 -- used to execute command to the container, process like bash 
 -- other option to use the log analysis command.
 ```
 
 Command to label pods
 ```
 > kubectl label [--override] <type> KEY1=VAL1...
 -- label the pods as key value pair. for example changing healty=false in deployment
 ```
 
 Command to run 
 ```
 > kubectl run <name> --image=image
 -- easy way to run the docker image on a cluster
 -- in ideal world, use the create deployment, and apply it to execute in cluster.
 -- this command to quickly run the docker image. 
 ```
