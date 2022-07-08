### Creating a working environment for kubernetes using K3D

- In this blog will be creating a workable environment in Docker Desktop.

#### What is [K3s](https://k3s.io)?
  - K3S is a lightweight kubernetes distribution that can run on production, and certified by CNCF.
  - K3S has memory footprint since the etcd, alpha features are removed in this distribution.
  - K3S uses SQLLite as default storage, for persisted storage we can use MySql or PostGres.
  - K3S is built for IoT devices and Edge computing.

##### Terminology used in K3s:
  - `Server` this is the control-plane
  - `Agent` this is the worker node

On a single node K3s runs the server and agent as a single process.

#### What is [K3d](https://k3d.io)? 
 - K3d is a lightweight wrapper to run K3S (Rancher Lab's) in docker.
 - K3d makes it very easy to create single or multi node K3s cluster in docker, for local development.

Pre-requisites:
 - Docker desktop already installed in the Windows machine.
 - The `Chocolatey` package manager installed.

> **Note**
>
> How K3D is different from Minikube, K3D runs in Docker as container, Minikube requires VM.

#### Install K3d in local machine

  - We cab install K3d in Windows machine using `Chocolatey` package manager.
  - For more details refer the [`Chocolatey`](https://chocolatey.org/) documentation, this shouldn't be used in production.
  - Use command prompt in Administrator mode.
  
```
> choco install k3d
```
 
##### Check the version of the K3d executable

```
> k3d --version
k3d version v5.4.3
k3s version v1.23.6-k3s1 (default)
```

#### Create a single node cluster

 - First make sure the Docker Desktop is running.
 - Below command will create a single node cluster
 
```
> k3d cluster create my-cluster-01
```

 - To list the cluster

```
> k3d cluster list
```

 - To delete the cluster

```
> k3d cluster delete my-clister-01
```

##### Use `kubectl` command to validate cluster

 - To get cluster info

```
> kubectl cluster-info
```

 - To get the nodes created

```
> kubectl get nodes
```

 - To run the simple busybox, removable and with interactive terminal

```
> kubectl run my-busybox --rm -it --image=busybox
```

  - From differnet command prompt or terminal, issue below command to veiw the running pod.
```
> kubectl get pods
```

>  **Note**
> 
> In case the kubectl command didn't connect to the cluster 
> use below command to merge context to `kubeconfig`
>
> `k3d kubeconfig merge my-cluster-01 --kubeconfig-switch-context`

##### Why `docker container ls` displays 3 container for single cluster?

  - The third container is a loadbalancer to handle the network traffic.
  - This will act as proxy to request traffic to the server, acting as an ingress.

  - Docker container list 
```
CONTAINER ID   IMAGE                            COMMAND                  CREATED              STATUS              PORTS                             NAMES
3a7f313a8af3   ghcr.io/k3d-io/k3d-tools:5.4.3   "/app/k3d-tools noop"    About a minute ago   Up About a minute                                     k3d-my-cluster-01-tools
e8c727ebef9e   ghcr.io/k3d-io/k3d-proxy:5.4.3   "/bin/sh -c nginx-pr…"   About a minute ago   Up About a minute   80/tcp, 0.0.0.0:53872->6443/tcp   k3d-my-cluster-01-serverlb
b58a0c7641e1   rancher/k3s:v1.23.6-k3s1         "/bin/k3s server --t…"   2 minutes ago        Up About a minute                                     k3d-my-cluster-01-server-0
```

#### Create a multi-node cluster

 - Below command will create a multi node cluster, with 2 servers and 3 woker node

```
> k3d cluster create my-multi-cluster --servers 2 --agents 3 --port "8888:80@loadbalancer" --port "8889:443@loadbalancer"
```

- Output of `kubectl get pods`

```
C:\user\learn\k8s>kubectl get nodes
NAME                     STATUS   ROLES                       AGE   VERSION
k3d-mycluster-agent-0    Ready    <none>                      37s   v1.23.6+k3s1
k3d-mycluster-agent-1    Ready    <none>                      37s   v1.23.6+k3s1
k3d-mycluster-agent-2    Ready    <none>                      37s   v1.23.6+k3s1
k3d-mycluster-server-0   Ready    control-plane,etcd,master   68s   v1.23.6+k3s1
k3d-mycluster-server-1   Ready    control-plane,etcd,master   53s   v1.23.6+k3s1
```

-  By default Docker Desktop doesn't allow access to the containers in that case we can use port forwarding.

> **Note**
>
> ```
>  -p, --port [HOST:][HOSTPORT:]CONTAINERPORT[/PROTOCOL][@NODEFILTER]  
>        => Map ports from the node containers (via the serverlb) to the host 
>        (Format: [HOST:][HOSTPORT:]CONTAINERPORT[/PROTOCOL][@NODEFILTER])
>   - Example: `k3d cluster create --agents 2 -p 8080:80@agent:0 -p 8081@agent:1`
> ```

#### Rancher desktop alternate to Docker Desktop

Alternatively we can create Kuberentes cluster using [Rancher Desktop](https://rancherdesktop.io/), no need for Docker desktop.

