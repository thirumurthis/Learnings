# Access container running in KIND Kubernetes cluster in Window Docker Desktop

## What is Kind?

 - With Kind (Kubernetes IN Docker) it is easy to spin up a local kubernetes cluster within Docker Desktop. The Kind runs as a container by itself.
 - Kind documentation is easy to understand, for more details and understanding refer [documentation](https://kind.sigs.k8s.io/) link.

## Motivation: 

  - With the default configuration we can't access the container running in Kind cluster within Windows Docker Desktop.
  - To access the running container we need to expose the configuration `extraPortMappings`.

## Pre-requisite tools required

  - 1. Docker Desktop installed and running.
  - 2. Install **KIND** using Chocolatey package manager refer [Kind documentation](https://kind.sigs.k8s.io/docs/user/quick-start/#installation) or [Chocolatey documentation](https://community.chocolatey.org/packages/kind) package manager.
  - 3. Install **Kubectl** installed using [chocolatey](https://community.chocolatey.org/packages/kubernetes-cli) package manager.
  - 4. Install **GNU make** in Windows using [Chocolatey](https://community.chocolatey.org/packages/make)
  

In this blog we will also see how to use the make utility to automate the steps to create the Kind cluster and deploy simple nginx container in the cluster. Once deployed we can use a target to verify whetehr we are able to access the container.

## Steps to run the nginx container in KIND cluster deployed in Docker Desktop

### 1. Create cluster using Kind cli, with the default configuration
  
```
# command to create cluster with default config. 
# the default cluster name is kind
> kind create cluster
```

```
# --name switch can be used to provide name for cluster
 > kind create cluster --name=test01
```
 
### Create KIND cluster with configuration yaml file

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

- Using the config file to the kind CLI

```
> kind create cluster --name=test --config=.\kind_cluster.yaml
```

#### Context info after the KIND cluster is created

![image](https://user-images.githubusercontent.com/6425536/199400230-f65e22a1-f65a-46cc-92f9-b7bf10201d58.png)

> **INFO:-**
>
> After deployment Kind automatically merges the configuration to the Kube config file, so we can use kubectl command directly. 

- Command to list the context, `*` indicates the kubectl uses that as current context

```
$ kubectl config get-contexts

CURRENT   NAME              CLUSTER           AUTHINFO          NAMESPACE
          docker-desktop    docker-desktop    docker-desktop
*         kind-test         kind-test         kind-test
          kind-test01       kind-test01       kind-test01
```

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

<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

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

#### make deploy output info

![image](https://user-images.githubusercontent.com/6425536/200214360-6279da19-8fd8-4e5a-8430-94cc5cb917a7.png)


- Once deployed, we can use the target `make check-container-access` to check if the container is accessible. Also to list the targets we can use `make` or `make info`
