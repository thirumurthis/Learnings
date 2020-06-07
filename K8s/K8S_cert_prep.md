After setting up the nodes as  in example [Link](https://github.com/thirumurthis/Learnings/blob/master/K8s/Kubernetes_cluster_notes.md).

##### To setup the kubectl command:
  - In case of the lab with 1 master 2 worker, just scp the admin.conf from the master /etc/kubernets/ to the host machine.
  
##### Once copied check/validate with below commands:
```
$ kubectl version
Client Version: version.Info{Major:"1", Minor:"17", GitVersion:"v1.17.0", GitCommit:"70132b0f130acc0bed193d9ba59dd186f0e634cf", GitTreeState:"clean", BuildDate:"2019-12-07T21:20:10Z", GoVersion:"go1.13.4", Compiler:"gc", Platform:"windows/amd64"}
Server Version: version.Info{Major:"1", Minor:"18", GitVersion:"v1.18.3", GitCommit:"2e7996e3e2712684bc73f0dec0200d64eec7fe40", GitTreeState:"clean", BuildDate:"2020-05-20T12:43:34Z", GoVersion:"go1.13.9", Compiler:"gc", Platform:"linux/amd64"}
```
```
$ kubectl version --short
Client Version: v1.17.0
Server Version: v1.18.3
```
```
$ kubectl cluster-info
Kubernetes master is running at https://172.42.42.100:6443
KubeDNS is running at https://172.42.42.100:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

#### First program to run in pod:

Create a yml file with nginx container (demo1.yml).
 
```yaml
apiVersion: v1

kind: Pod
metadata:
   name: myapp-pod
   labels:
     app: myapp
     type: testpod

spec:
    containers:
     - name: nginx-container
       image: nginx
```

##### To create pod using the `kubectl` command
```
$ kubectl create -f demo1.yml
pod/myapp-pod created
```

##### To check the status of deployed pod

- The status of the pod `ContainerCreating`
```
C:\thiru\learn\k8s\certs\prog1>kubectl get pods
NAME        READY   STATUS              RESTARTS   AGE
myapp-pod   0/1     ContainerCreating   0          8s
```

- The status of the pod after created `Running` 
```
C:\thiru\learn\k8s\certs\prog1>kubectl get pods
NAME        READY   STATUS    RESTARTS   AGE
myapp-pod   1/1     Running   0          40s
```

