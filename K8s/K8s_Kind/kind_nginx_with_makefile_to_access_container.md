## Sping up Kubernetes cluster with Kind in Docker Desktop

What is Kind?
 - With Kind (Kubernetes IN Docker) it is easy to sping up a local kubernetes cluster within Docker Desktop. The Kind runs as a container by itself.
 - Kind documentation is easy to understand, for more details and understanding refer [documentation](https://kind.sigs.k8s.io/) link.

Motivation: 

  - With the default configuration we can't access the container running in Kind cluster within Windows Docker Desktop.
  - To access the running container we need to expose the configuration `extraPortMappings`.

- Pre-requesites:
  - 1. Docker Desktop installed and running.
  - 2. Install Kind using Chocolatey pacakge manager other option refer the Kind [documentation](https://kind.sigs.k8s.io/docs/user/quick-start/#installation), [Chocolatey documentation](https://community.chocolatey.org/packages/kind) package manager.
  - 3. Kubectl installed using [chocolatey](https://community.chocolatey.org/packages/kubernetes-cli) package manager.
  - 4. Install `GNU make` in Windows using [Chocolatey](https://community.chocolatey.org/packages/make)
  

In this blog I will use the make utility to automate the process of creating the cluster and deploying simple nginx container in the cluster and once deployed verify accessing.

Without the makefile below steps need to be followed

1. Create cluster using Kind cli, with the default configuration
  
```
# command to create cluster with default config. 
# the default cluster name is kind
> kind create cluster
```
- specify the cluster name
```
# --name switch is used to provide name for cluster
 > kind create cluster --name=test01
```
 
- Create cluster using cluster configuration in yaml
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

- Pass the config file in the kind CLI

```
> kind create cluster --name=test --config=.\kind_cluster.yaml
```

> **INFO:-**
>
> After deployment Kind automatically merges the configuration to the Kube config file, so we can use kubectl command directly. 

- Below is the command to list the context. This command will output the created cluster info

```
> kubectl config get-contexts
```
- Output
   - `*` indicates the current context
```
$ kubectl config get-contexts
CURRENT   NAME              CLUSTER           AUTHINFO          NAMESPACE
          docker-desktop    docker-desktop    docker-desktop
*         kind-test         kind-test         kind-test
          kind-test01       kind-test01       kind-test01
```

2. Create a nginx container with the config file

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

3. To access the container from host machine, from Git Bahs use the `curl` command to hit the 8010 port.

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

### We automate the whole step abobe using make utility

- We can define the above steps in Makefile. 
- In brief, we have different target with recipe to perform the task in the make utility and these to be defined in `Makefile`.
- GNU make supports lots of features.

```
target:
   recipe 
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

- Place the cluster config (as kind_cluster.yaml) and manifest content (as app_deployment.yaml) in  file, under the directory along with the Makefile.


- From GitBash editor, navigate to the directory and issue the below command

```
> make deploy
```

- Once deployed, using the target `make check-container-access`
To list the targets use `make` or `make info`
