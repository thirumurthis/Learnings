# The k8s server requires Kind cluster to be running
- set the kubeconfig to below path when running the kubernetes in wsl2

```
export KUBECONFIG=\\wsl.localhost\Ubuntu-24.04\home\thirumurthi\.kube\config
```

in IDE use run configuration environment variables to set it

- The client can be accessed

```
 curl http://localhost:8085/input/in -d 'get me the list of pods on apisix namespace in kuberentes cluster'
 ```