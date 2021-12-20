For any application deployed in AKS in different environment like Dev, Test, UAT, PROD, etc.

- There will be different context for K8s, below are the steps to login to each environment in Azure AKS.


1. Login to Azure using 
```
$ az login
```

2. Update the context and set context to specific name space.

```
$ az account set -s <subscription-name> && \ 
  az aks get-credentials -g <resource-group-name> -n <name-of-k8s-cluster> --overwrite-existing && \
  kubectl config set-context --current --namespace=<namespace-in-node>
```
 - Note: Above command can be aliased in .bashrc, with alias kube<env>getcontext='<above command>'
  
 3. Once updated, issued

```
 $ kubectl get ns 
 To sign in, use a web browser to open the page https://microsoft.com/devicelogin and enter the code <some-code> to authenticate.
  
 $ kubectl get pods -n <name-space>
  // lists the pods in specific namespace
```
