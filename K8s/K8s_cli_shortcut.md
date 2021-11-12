 - in linux use the below shortcuts for kubectl command

```
alias ksclae='f(){ kubectl scale deployment "$1" --replicas="$2";  unset -f f; }; f'
alias kexec='f(){ kubectl exec -it "$@" /bin/sh;  unset -f f; }; f'
alias kevnv='kubectl config use-context aks-context-dev && kubectl config set-context --current --namespace=aks-dev'
## aks-context-dev => is context name and aks-dev => namespace, this change accordingly
alias kgetcontext='az account set -s <subscription> && az aks get-credentials -g <resource-group-name> -n <aks-context-name> --overwrite-existing && kubectl config set-context --current --namespace=<aks-namespace>'

```
