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
 
 Deploy the first application in K8s, hello-node
 
 [hello-node](https://kubernetes.io/docs/tutorials/hello-minikube/#create-a-minikube-cluster)
 
 Sample deployment json for hello-node
 
 ```
 {
  "kind": "ReplicaSet",
  "apiVersion": "extensions/v1beta1",
  "metadata": {
    "name": "hello-node-55d9f949c6",
    "namespace": "default",
    "selfLink": "/apis/extensions/v1beta1/namespaces/default/replicasets/hello-node-55d9f949c6",
    "uid": "17f419b2-7760-4464-8389-4ec1a7931dce",
    "resourceVersion": "894",
    "generation": 1,
    "creationTimestamp": "2019-11-30T16:44:26Z",
    "labels": {
      "app": "hello-node",
      "pod-template-hash": "55d9f949c6"
    },
    "annotations": {
      "deployment.kubernetes.io/desired-replicas": "1",
      "deployment.kubernetes.io/max-replicas": "2",
      "deployment.kubernetes.io/revision": "1"
    },
    "ownerReferences": [
      {
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "name": "hello-node",
        "uid": "58ed6b2a-7f55-4b5c-8dd9-c6d73ca864c1",
        "controller": true,
        "blockOwnerDeletion": true
      }
    ]
  },
  "spec": {
    "replicas": 1,
    "selector": {
      "matchLabels": {
        "app": "hello-node",
        "pod-template-hash": "55d9f949c6"
      }
    },
    "template": {
      "metadata": {
        "creationTimestamp": null,
        "labels": {
          "app": "hello-node",
          "pod-template-hash": "55d9f949c6"
        }
      },
      "spec": {
        "containers": [
          {
            "name": "hello-node",
            "image": "gct.io/hello-minikube-zero-install/hello-node",
            "resources": {},
            "terminationMessagePath": "/dev/termination-log",
            "terminationMessagePolicy": "File",
            "imagePullPolicy": "Always"
          }
        ],
        "restartPolicy": "Always",
        "terminationGracePeriodSeconds": 30,
        "dnsPolicy": "ClusterFirst",
        "securityContext": {},
        "schedulerName": "default-scheduler"
      }
    }
  },
  "status": {
    "replicas": 1,
    "fullyLabeledReplicas": 1,
    "observedGeneration": 1
  }
}
```
 
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
```
