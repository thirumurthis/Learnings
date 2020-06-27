
Sample Persistent volume:

hostPath, life of the volume test-vol will be available even if the pod is died.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: redis-hostpath
  labels:
    app: redis-hostpath
spec:
  containers:
    - name: redis1
      image: redis
      volumeMounts:
      - name: hostvloume
        mountPath: /test-mnt
  volumes:
  - name: hostvloume
    hostPath:
      path: /test-vol

```

emptyDir where the volume will be ephemeral, not available only for the life cycle of pod.
```yaml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: null
  labels:
    run: pod-07
  name: pod-07
spec:
  containers:
  - image: nginx
    name: pod-07
    command: ["sleep","1000"]
    volumeMounts:
    - mountPath: /data/app
      name: myvolume
  automountServiceAccountToken: false
  serviceAccountName: backend-team
  volumes:
  - name: myvolume
    emptyDir: {}
  restartPolicy: Never
```

Persistent volume and Persistent Volume Claim:

the presisent vloume claim can be used in the pod with the claim name in volumes.
note: deleting the pvc will delete the pv also, but only the any directory within the /tmp/<> will be auto deleted else it wont get deleted.
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pvolume1
  labels:
    app: pvolume1
spec:
  capacity:
    storage: 500Mi
  accessModes:
  - ReadWriteOnce
  persistentVolumeReclaimPolicy: Delete
#  storageClassName: local-storage
  hostPath:
    path: /tmp/data

---

apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pvclaim1
  labels:
    app: pvclaim01
spec:
   accessModes:
   - ReadWriteOnce
   resources:
     requests:
      storage: 500Mi

```
using ConfigMap as persistent volume:
```
$ kubectl create configmap configmap-log --from-literal=log_level=info
```
```yaml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: null
  labels:
    run: cmvpod
  name: cmvpod
spec:
  containers:
  - image: busybox
    name: cmvpod
    command: ["sleep","90"]
    volumeMounts:
    - name: cmvpod
      mountPath: /etc/newconfig
    - name: localv
      mountPath: /etc/local-store
    resources: {}
  volumes:
  - name: cmvpod
    configMap: # the log_level file will be created under the /etc/newconfig (voumemount) with log_level file which has info as content.
      name: confmap-log
      items: # if items not included, all the key values are treated as file and mounted in the /etc/newconfig folder within the pod
      - key: log_level
        path: log_level
  - name: localv
    emptyDir: {}
  dnsPolicy: ClusterFirst
  restartPolicy: Always
status: {}


```
