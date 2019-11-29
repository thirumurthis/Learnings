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
