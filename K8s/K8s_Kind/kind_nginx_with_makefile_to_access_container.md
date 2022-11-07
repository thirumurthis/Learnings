# Access container running in KIND Kubernetes cluster in Window Docker Desktop

## What is Kind?

 - With Kind (Kubernetes IN Docker) it is easy to spin up a local kubernetes cluster within Docker Desktop. The Kind runs as a container by itself.
 - Kind documentation is easy to understand, for more details and understanding refer [documentation](https://kind.sigs.k8s.io/) link.

## Motivation

  - KIND cluster deployed in Windows Docker Desktop with default configuration we will not be able to access the container running in the cluster from the host machine.
  - In order to access the container from the host machine we need to create KIND cluster using  `extraPortMappings` cluster configuration.
  - This is not the case if we create the KIND cluster in Linux based system, based on the documentation this is network based limitation in Windows and Mac.

## Pre-requisite tools installed

  - 1. Docker Desktop installed and running.
  - 2. Install **KIND** using Chocolatey package manager refer [Kind documentation](https://kind.sigs.k8s.io/docs/user/quick-start/#installation) or [Chocolatey documentation](https://community.chocolatey.org/packages/kind) package manager.
  - 3. Install **Kubectl** installed using [chocolatey](https://community.chocolatey.org/packages/kubernetes-cli) package manager.
  - 4. Install **GNU make** in Windows using [Chocolatey](https://community.chocolatey.org/packages/make)
  

In this blog we will also see how to use the make utility to automate the steps to create the Kind cluster and deploy simple nginx container in the cluster. Once deployed we can use a target to verify whether we are able to access the container.

## Instruction to create KIND cluster in Docker Desktop and deployed a Nginx container

### 1. Create KIND cluster with CLI
  
- KIND CLI used to create cluster with default configuration and specify the configuration in yaml file

```
# command to create cluster with default config. 
# the default cluster name is kind
> kind create cluster
```

```
# --name switch can be used to provide name for cluster
 > kind create cluster --name=test01
```
 
 #### KIND cluster configuration exposes the port

  - In order to expose the port, we can use `extraPortMappings` option in the cluster config yaml file. The content of the cluster config, save it as `kind_cluster.yaml`.
  
```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  # port forward 8010 on the host to 80 on this node
  extraPortMappings:
  - containerPort: 80
    hostPort: 8010
    listenAddress: "127.0.0.1"
    protocol: TCP
```

- KIND CLI command to use the config file

```
> kind create cluster --name=test --config=.\kind_cluster.yaml
```

- List of context created after the KIND cluster is created

![image](https://user-images.githubusercontent.com/6425536/199400230-f65e22a1-f65a-46cc-92f9-b7bf10201d58.png)

> **INFO:-**
>
> After cluster deployed the KIND kube config is automatically merged to the kubectl kube config file, so we can use kubectl command directly to access the context. 

- Command that lists the contexts after the KIND cluster is created
- `*` indicates the kubectl is using it as current context

```
$ kubectl config get-contexts

CURRENT   NAME              CLUSTER           AUTHINFO          NAMESPACE
          docker-desktop    docker-desktop    docker-desktop
*         kind-test         kind-test         kind-test
          kind-test01       kind-test01       kind-test01
```

- With the above configuration once the cluster is deployed if we check the `docker ps` we should see the 8010 port exposed.

![image](https://user-images.githubusercontent.com/6425536/200222117-b9d1d2ca-2cc0-451b-ae63-e2cd4a981c57.png)

### 2. Create a nginx container with the config file

- Instead of running nginx container using `kubectl run` command, we use the deployment manifest file below with ports specified.
  - We can see the `containerPort` and the `hostPort` in the deployment manifest both match cluster container port value 80.
  - The service type ClusterIP expose the container 80 port, so the container can be accessed from the host machine.
  - Save the deployment content to the file named `app_deployment.yaml` 

>**INFO:-**
>
> To access the nginx container from the host laptop, the service needs to be created.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: web
  name: web
spec:
  replicas: 1
  selector:
    matchLabels:
      app: web
  strategy: {}
  template:
    metadata:
      labels:
        app: web
    spec:
      containers:
      - image: nginx
        name: nginx
        ports:
        - containerPort: 80
          hostPort: 80
        resources: {}
status: {}
---
apiVersion: v1
kind: Service
metadata:
  name: web-svc
spec:
  selector:
    app: web
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
```

- Create the deployment with `kubectl -n web apply -f app_deployment.yaml`.

### 3. Access the container from host machine

3. With the Git Bash use the `curl` command to hit the 8010 port.

```
curl http://localhost:8010
```
Or from the browser on host machine which displays the default nginx home page like below

![image](https://user-images.githubusercontent.com/6425536/199401277-6ab2bb24-8f5e-45ec-9b82-ff47b4af281d.png)

## Makefile to deploy the KIND cluster

- `make` is a build tool used in Linux/Unix systems to build C/C++ code earlier
- The make file included all the above steps,
   - Create the KIND cluster, 
   - Create a deployment to run a nginx container
   - Target to run the curl command to access the container
- GNU make also supports lots of features, refer [makde documentation](https://www.gnu.org/doc/doc.html).

```
# Makefile simple representation
target:
<tab-space>recipe 
```

>**INFO:-**
>
> The recipe in the Makefile followed by the target name should start with tab.
> else it won't make will not be able to execute the Makefile

- Below make file content should be created as `Makefile`

```make
SHELL :=/bin/bash

NAMESPACE ?= web
CLUSTERNAME ?= test
CLUSTERCONFIG := kind_cluster.yaml
DEPLOYMENT_MANIFEST := app_deployment.yaml

.DEFAULT_GOAL :=info

#------------------------------
#  Note:-
#     - PHONY - usually the targets will be a file in make, in order to indicate it as a task we use PHONY
#     - using @ before the commands in the recipe indicate the statement not to be printed in the stdout
#       without @, the statement will be printed and command will get executed
#     - $$ in the recipe is way to escape the character between the make and the shell command. (refer info target)
#

.PHONY: create-namespace 
create-namespace: # create namespace
	@kubectl create ns ${NAMESPACE}

.PHONY: install-cluster
install-cluster: # install cluster
	@kind create cluster --name=${CLUSTERNAME} --config=./${CLUSTERCONFIG}

.PHONY: list-resource
list-resource: # list resources deployment, pods, service
	@kubectl -n ${NAMESPACE} get deploy,pods,svc

.PHONY: display-log
display-log: # display pod logs (first index is used)
	@kubectl -n ${NAMESPACE} logs pod/$(shell kubectl -n ${NAMESPACE} get pods --no-headers -o=jsonpath='{.items[0].metadata.name}')

# target within single line alternate approach

.PHONY: delete-cluster
delete-cluster: ; @kind delete cluster --name=${CLUSTERNAME}

.PHONY: apply-manifest
apply-manifest: # apply the manifest
	@kubectl -n ${NAMESPACE} apply -f ${DEPLOYMENT_MANIFEST}

.PHONY: deploy # creates cluster and deploy app
deploy: install-cluster create-namespace apply-manifest list-resource 
	@echo "Deploy cluster"

.PHONY: check-container-access
check-container-access: # checks the container after deployment
	@curl http://localhost:8010

info: # display the target names
	@awk '/^[a-zA-Z_-]+: / { print $$0; print "\n"}' $(MAKEFILE_LIST) | \
	awk -F":" 'BEGIN {print "targets" } /^[a-zA-Z_-]+/ {print "    "$$1}'
```

- Place the cluster config (as kind_cluster.yaml) and manifest content (as app_deployment.yaml) in same directory as the Makefile.


- From Git Bash editor, navigate to the directory and issue the below command

```
> make deploy
```

#### Output - using make targets

- The output of using the `make deploy`, deploy target which deploys the cluster and Nginx container.

![image](https://user-images.githubusercontent.com/6425536/200214360-6279da19-8fd8-4e5a-8430-94cc5cb917a7.png)

- Output of `make check-container-access` once the deployment is complete

![image](https://user-images.githubusercontent.com/6425536/200217172-8e715297-d081-49c0-a27b-c7ddd0bfd52a.png)


- Output of `make` or or `make info` will list the targets like below

![image](https://user-images.githubusercontent.com/6425536/200217353-2576f8c9-d894-4de6-8756-aee4e5fdb1b6.png)

## Exposing multiple port from cluster

![image](https://user-images.githubusercontent.com/6425536/200250963-d0ed4e12-9aba-4066-b7f8-5541a32d84f0.png)

- Cluster configuration to expose 8012 and 8013 is as follows

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  # port forward 8008 on the host to 80 on this node (kind)
  extraPortMappings:
  - containerPort: 8000
    hostPort: 8012
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 8001
    hostPort: 8013
    listenAddress: "127.0.0.1"
    protocol: TCP
```

- Below are the two nginx deployment using different ports
  - 8012.yaml
    - The nginx `containerPort : 80`, the service definition targetPort is 80 which handles the traffic to the web-pod1 container

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: web-pod1
  name: web-pod1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: web-pod1
  strategy: {}
  template:
    metadata:
      labels:
        app: web-pod1
    spec:
      containers:
      - image: nginx
        name: nginx
        ports:
        - containerPort: 80
          hostPort: 8000
        resources: {}
status: {}
---
apiVersion: v1
kind: Service
metadata:
  name: web-pod1
spec:
  selector:
    app: web-pod1
  ports:
    - protocol: TCP
      port: 8000
      targetPort: 80
```

- 8013.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: web-pod2
  name: web-pod2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: web-pod2
  strategy: {}
  template:
    metadata:
      labels:
        app: web-pod2
    spec:
      containers:
      - image: nginx
        name: nginx
        ports:
        - containerPort: 80
          hostPort: 8001
        resources: {}
status: {}
---
apiVersion: v1
kind: Service
metadata:
  name: web-pod2
spec:
  selector:
    app: web-pod2
  ports:
    - protocol: TCP
      port: 8001
      targetPort: 80
```
