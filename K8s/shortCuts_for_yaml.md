##K8s 1.18+ short cuts to create yaml file:

### Create pods:
```
kubectl run pod-01 --image=nginx --restart=Never -o yaml --dry-run=client 
```
### Create deployment:
```
kubectl create deploy deploy-01 --image=nginx -o yaml --dry-run=client
```
### create service
#### ClusterIP
```
kubectl create service clusterip service-01 --tcp=80:80 -o yaml --dry-run=client
```
#### NodePort
```
kubeclt create service nodeport service-01 --tcp=80:80 -o yaml --dry-run=client
```
#### use `kubect create service --help`
### Other resources that can be created using Kubectl create
### configmap
### ResourceQuota
### namespace
### role
### secret


### Create cronjob yaml:
```
kubectl create cj cronjob-01 --image=busybox --schedule="* * * * *" -o yaml --dry-run=client
```
### Create job yaml
```
kubectl create job job-01 --image=busybox -o yaml --dry-run=client -- date
```
