 - in linux use the below shortcuts for kubectl command

```
alias kscale='func(){ kubectl scale deployment "$1" --replicas="$2";  unset -f func; }; func'

# usage: kscale  <deployment-name> <replica-count>
```
```
alias kexec='func(){ kubectl exec -it "$@" /bin/sh;  unset -f func; }; func'

# usage: kexec <pod-name>
# usage kexec -n <namespace-name> <pod-name>
```
```
alias kexecns='func(){ kubectl -n "$1" exec -it "$2" /bin/sh;  unset -f func; }; func'
# usage : kexecns <namespace-name> <pod-name>
```
```
alias kenv='kubectl config use-context <aks-context-for-env> && kubectl config set-context --current --namespace=<namespace--name-of-aks-env>'
## aks-context-dev => is context name and aks-dev => namespace, this change accordingly

# usage: kenv 
```

#### Below command will fetch the context from aks
- Before using that command if AAD is set with AKS cluster then we need to
 - In azure first use `az login` to login using azure cli
 - Check if the subscription status using `az account list -o table` to list the subscription list
 - set appropriate subscription using `az account set -s <subscription-name>` (we can use subscription is as well)

```
alias kgetcontext='az account set -s <subscription> && az aks get-credentials -g <resource-group-name> -n <aks-context-name> --overwrite-existing && kubectl config set-context --current --namespace=<aks-namespace>'

# usage kgetcontext
```
 - Note: During development if we need to push image to ACR, we need to set the correct subscrption where ACR is configured

