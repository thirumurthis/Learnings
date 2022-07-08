### Creating a working environment for kubernetes using K3D

- In this blog will be creating a workable environment in Docker Desktop.

What is [K3s](https://k3s.io)
  - K3S is a lightweight kubernetes distribution that can run on production, and certified by CNCF.
  - K3S has memory footprint since the etcd, alpha features are removed in this distribution.
  - K3S uses SQLLite as default storage, for persisted storage we can use MySql or PostGres.
  - K3S is built for IoT devices and Edge computing.

Terminology used in K3s:
  - `Server` this is the control-plane
  - `Agent` this is the worker node

On a single node K3s runs the server and agent as a single process.

What is [K3d](https://k3d.io)
 - K3d is a lightweight wrapper to run K3S (Rancher Lab's) in docker.
 - K3d makes it very easy to create single or multi node K3s cluster in docker, for local development.

Pre-requisites:
 - Docker desktop already installed in the Windows machine.
 - The `Chocolatey` already installed.

##### Installing K3d in local.
  - To install K3d in Windows machine, I am using the `Chocolatey` community owned package manager.
  - For more details refer the Coholatey documentation, this shouldn't be used in production.
  - Use command prompt in Administrator mode.
  
```
> choco install k3d
```
 
#### Check the version of the K3d executable

```
> k3d --version
k3d version v5.4.3
k3s version v1.23.6-k3s1 (default)
```

#### To create a single node cluster

 - First make sure the Docker Desktop is running.
 - Below command will the create a single node cluster with server and an agent.
 
```
> k3d cluster create my-cluster-01
```

#### Using `kubectl` command

- To find the cluster info, use below command

```
> kubectl cluster-info
```

- To get the nodes created

```
> kubectl get nodes
```

- To run the simple busybox in that cluster

```
> kubectl run my-busybox --rm -it --image=busybox
```

- To view the pods that is created use different command prompt.

```
> kubectl get pods
```

>  **Note**
> In case the kubectl command didn't connect to the cluster use 
> `k3d kubeconfig merge my-cluster-01 --kubeconfig-switch-context`


- To Delete the cluster using the k3d executable

```
> k3d cluster delete my-clister-01
```

#### To create a multi-node cluster

```
> k3d cluster create mycluster --servers 2 --agents 3 --port "8888:80@loadbalancer" --port "8889:443@loadbalancer"
```

- Ouput 

```
C:\user\learn\k8s>kubectl get nodes
NAME                     STATUS   ROLES                       AGE   VERSION
k3d-mycluster-agent-0    Ready    <none>                      37s   v1.23.6+k3s1
k3d-mycluster-agent-1    Ready    <none>                      37s   v1.23.6+k3s1
k3d-mycluster-agent-2    Ready    <none>                      37s   v1.23.6+k3s1
k3d-mycluster-server-0   Ready    control-plane,etcd,master   68s   v1.23.6+k3s1
k3d-mycluster-server-1   Ready    control-plane,etcd,master   53s   v1.23.6+k3s1
```

> **NOTE**
> -p, --port [HOST:][HOSTPORT:]CONTAINERPORT[/PROTOCOL][@NODEFILTER]  
>        => Map ports from the node containers (via the serverlb) to the host 
>        (Format: [HOST:][HOSTPORT:]CONTAINERPORT[/PROTOCOL][@NODEFILTER])
>   - Example: `k3d cluster create --agents 2 -p 8080:80@agent:0 -p 8081@agent:1`

