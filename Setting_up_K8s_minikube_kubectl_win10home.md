install the choco MS package manger


using cmd as admin package run the below command to install it.

```
@"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
```
 [Installing-minikube-with-chocolatey-windows-package-manager](https://medium.com/@JockDaRock/installing-the-chocolatey-package-manager-for-windows-3b1bdd0dbb49)

After installation of choco PM then use below command


```
> choco install minikube
```

to start the cluster, use 

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
 
 
 
 
