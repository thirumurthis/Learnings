# First K8s and Docker application
- Install Docker
  - Create a sample spring boot application
     - Dockerfile 
  - validate locally
    ```
     # from the dockerfile location
     > docker build -t <image-name> .
     # running the image locally
     > docker run -d -p 8080:8080 <image-name>
    ```
  - push it to docker hub
  ```
    # create tag  (version or use latest)
    > docker tag <image-name> <username>/<image-name>:<version>
    # push the tag to docker hub
    > docker push <username>/<imange-name>:<version>
  ```
 - If Minikube is setup with kubectl (below deployment and service is opininated)
   - use kubectl command to deploy the image 
   ```
    > kubectl run <deployment-name> --image=<username/imagename>:<version> --port=<port the app runs>
   ```
   - use kubectl commnand to validate the deployment is successfull with below commands
   ```
    > kubectl get deployments
    # get replica sets
    > kubectl get rs
    # get deployment details
    > kubectl describe deployments
   ```
   - once the deployment is successfull expose the service
   ```
    > kubectl expose deployment <deployment-name> --type=NodePort
    # note NodePort is one type, there is another option clusterIP (check the K8s architecture md file for more info)
   ```
    - Minikube dashboard can be used to view the deployment
    ```
    > minikube dashboard
    ```
    
    - To view the running service info
    ```
    > kubectl get services
    
    # output:
    C:\HelloFromDocker>kubectl get services
     NAME            TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
     hello-service   NodePort    10.96.10.33   <none>        8800:31377/TCP   15s
     kubernetes      ClusterIP   10.96.0.1     <none>        443/TCP          7h18m
     
     # note: the 8800 port was exposed in the application
    ```
        
    - To view the running application
    ```
    > minikube service <deployment-name>
    ```
          
 # Kubectl command to get the yaml deployment file command:
 ```
 # in the above example use the below command to list all the info of the deployments and services 
 > kubectl get all 
 
 # use the deployment name from the list to see the yaml file
 > kubectl get <deployment-name> -o yaml
 ```

# Kubectl command to `scale` the container

```
# kubectl scale --replicas=<no-of-replicas> <deploy>/<deployment-name>
> kubectl scale --replicas=3 deploy/hello-service

> kubectl get rs
# output
 NAME                       DESIRED   CURRENT   READY   AGE
 hello-service-64b799fcc8   3         3         3       7h58m
```

# Kubectl deleting the resources
```
> kubectl delete deployment/hello-service,service/hello-service
# command will delete the resources, when viewed in minikube dashboard all pods will be deleted
```


# Kubectl to use selector quering label

```
# command displays the label info in the output along with the pods
> kubectl get pods --show-labels

# selector to query the pods label (below will list the pod with lable run = hello-service)
> kubectl get pods --selector run=hello-service

# selector to query pods with multiple label
> kubectl get pods --selector run=hello-service,pods-template-hash=72827827

# selector to use NOT matching the label
> kubectl get pods --selector run!=hello-service

# selector to use IN operator -- can be used shorty as -l
> kubectl get pods -l 'release-version in (0.0.1,latest)'

# delete pods that matches label
> kubectl delete pods -l run=hello-service
```

### Kubectl - creates deployment yaml with the provided values when using `kubectl run --image=<image-name>`
```
apiVersion: v1
items:
- apiVersion: apps/v1
  kind: Deployment
  metadata:
    annotations:
      deployment.kubernetes.io/revision: "1"
    creationTimestamp: "2019-12-24T14:59:14Z"
    generation: 1
    labels:
      run: hello-service
    name: hello-service
    namespace: default
    resourceVersion: "76195"
    selfLink: /apis/apps/v1/namespaces/default/deployments/hello-service
    uid: 90950172-1c0b-4b9f-a339-b47569366f4e
  spec:
    progressDeadlineSeconds: 600
    replicas: 1
    revisionHistoryLimit: 10
    selector:
      matchLabels:
        run: hello-service
    strategy:
      rollingUpdate:
        maxSurge: 25%
        maxUnavailable: 25%
      type: RollingUpdate
    template:
      metadata:
        creationTimestamp: null
        labels:
          run: hello-service
      spec:
        containers:
        - image: thirumurthi/hello-service:0.0.1
          imagePullPolicy: IfNotPresent
          name: hello-service
          ports:
          - containerPort: 8800
            protocol: TCP
          resources: {}
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
        dnsPolicy: ClusterFirst
        restartPolicy: Always
        schedulerName: default-scheduler
        securityContext: {}
        terminationGracePeriodSeconds: 30
  status:
    availableReplicas: 1
    conditions:
    - lastTransitionTime: "2019-12-24T14:59:19Z"
      lastUpdateTime: "2019-12-24T14:59:19Z"
      message: Deployment has minimum availability.
      reason: MinimumReplicasAvailable
      status: "True"
      type: Available
    - lastTransitionTime: "2019-12-24T14:59:14Z"
      lastUpdateTime: "2019-12-24T14:59:19Z"
      message: ReplicaSet "hello-service-75d67cc857" has successfully progressed.
      reason: NewReplicaSetAvailable
      status: "True"
      type: Progressing
    observedGeneration: 1
    readyReplicas: 1
    replicas: 1
    updatedReplicas: 1
kind: List
metadata:
  resourceVersion: ""
  selfLink: ""
```

## Kubectl to include health check `ReadinessProbes` and `LivenessProbes` 
   - update the deployment yaml file under the container
   
ReadinessProbes - http check
Liveness - checks perodically if the pod is healthy


# Kubectl to display logs of the pod
```
> kubectl get pods

> kubectl logs pods/<name-of-the-pod>

# below command will provide the status of any deployments, pods
> kubectl describe deploy/<name-of-deployment>
> kubectl describe pod/<name-of-pod>

# exec to the specific pod - exec to pod and pipe to bash shell in here
> kubectl exec -it <pod-name> /bin/bash

# in case of multiple container in pod
> kubectl exec -it <pod-name> -c <container-name> /bin/bash
```
