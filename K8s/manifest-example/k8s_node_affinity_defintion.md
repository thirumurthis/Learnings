Nodeaffinity on deployment, added at the pod level:

Assume the cluster has 1 master and two woker, where the worker2 has the label "type=worker2"

- `Taint and Toleration` cannot gaurantee that the pod with the toleration lands on the requested node.
- But along with the NodeAffinity, it can be made sure.
- Taint the node, then add the nodeaffinity using the Labels on the node.
- `requiredDuringSchedulingIgnoredDuringExecution` 
      - Assume the pod is scheduled in the node and if the environment lable is change,this option will not evict the already scheduled pod.
      - if the nodes doesn't have the label, then in this option the pods will not be scheduled.
      
- `preferedDuringSchedulingIgnoredDuringExecution` 
  - Assume if the label is not avialable in the cluster and worker, with this option the scheduler will do its best to place the pod matching nodes.
  - somehow it will place the pods in any of the node within the cluster.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: affinitynode1
  name: affinitynode1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: affinitynode1
  strategy: {}
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: affinitynode1
    spec:
      containers:
      - image: busybox
        name: busybox
        resources:
          requests:
            cpu: "0.2m"
      affinity:
        nodeAffinity:
         requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: "type"
                operator: "In"
                values:
                - worker2
status: {}
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: affinity-node1
  name: affinity-node1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: affinity-node1
  strategy: {}
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: affinity-node1
    spec:
      containers:
      - image: nginx
        name: nginx
        resources:
          requests:
            cpu: "0.2m"
      affinity:
        nodeAffinity:
         preferredDuringSchedulingIgnoredDuringExecution:
         - weight: 90
           preference:
             matchExpressions:
             - key: type
               operator: "In"
               values:
               - worker1
status: {}
```
