Running nginx container in Kind cluster and accessing from host machine

- With Docker Desktop in Windows the default kind cluster can't be accessed directly.

Based on the [documentation](https://kind.sigs.k8s.io/docs/user/configuration/#extra-port-mappings) we need to apply specific port configuration 

STEP 1: 
Below is the simple Kind cluster config which exposes the host port 

- Save the file as cluster_info_kind.yaml
```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  # port forward 80 on the host to 80 on this node
  extraPortMappings:
  - containerPort: 8008
    hostPort: 80
    # optional: set the bind address on the host
    # 0.0.0.0 is the current default
    listenAddress: "127.0.0.1"
    # optional: set the protocol to one of TCP, UDP, SCTP.
    # TCP is the default
    protocol: TCP
```

- install the kind wither from Chocolate or from Kind documentation

Now we need to execute the below command to create a cluster with above configuration

Note: Kind requires docker demon running 

```
> kind create cluster --name config=./cluster-info-kind.yaml
```
- once created the output of the command looks like below
![image](https://user-images.githubusercontent.com/6425536/199400230-f65e22a1-f65a-46cc-92f9-b7bf10201d58.png)

STEP 2:
- we need to create the nginx container that uses the Containerport and Hostport defined in the manifest file like below
- save the below file as nginx.yaml
```yaml
kind: Pod
apiVersion: v1
metadata:
  name: kind-demo
spec:
  containers:
  - name: web
    image: nginx
    ports:
    - containerPort: 80
      hostPort: 80
```

- Create the container with the Pod manifest

```
> kubectl apply -f ./nginx.yaml
```

STEP: 3
- Expose the pod as service in this case ClusterIp

```
> kubectl expose pod/kind-demo --port=80 target-port=80
```
- output of above command since we didn't include the label in the manifest the expose command will throw error soe create a label
```
PS C:\learn\kind> kubectl expose pod/kind-demo --port=80 --target-port=80
error: couldn't retrieve selectors via --selector flag or introspection: the pod has no labels and cannot be exposed
PS C:\learn\kind> kubectl label pod/kind-demo app=web
pod/kind-demo labeled
```
- The expose command output 

```
PS C:\learn\kind> kubectl expose pod/kind-demo --port=80 --target-port=80
service/kind-demo exposed
PS C:\learn\kind> kubectl get pod,sbc
error: the server doesn't have a resource type "sbc"
PS C:\thiru\learn\kafka-demo\kind_strimzi> kubectl get pod,svc
NAME            READY   STATUS    RESTARTS   AGE
pod/kind-demo   1/1     Running   0          2m30s

NAME                 TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
service/kind-demo    ClusterIP   10.96.213.86   <none>        80/TCP    15s
service/kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP   17m
```

Now we should be able to access the nginx container from 8008 port 

![image](https://user-images.githubusercontent.com/6425536/199401277-6ab2bb24-8f5e-45ec-9b82-ff47b4af281d.png)

