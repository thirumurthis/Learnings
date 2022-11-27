## Deploy kafka cluster in Docker Desktop with KIND

- In this blog we will see how to setup a local development Kafka cluster in Docker desktop using Kind.

- In order to deploy the Kafka cluster into Docker desktop Kind cluster will be using the Strimzi operator. 

- Strimzi is a open source project, is part of the CNCF projects. It provides configuring to deploy the Apache Kafka broker and run in Kubernetes cluster.

- With Windows 10 and 8 GB ram, had difficulties running the Strimzi operator with Zookeeper configuration due to memory constraints in my machine, so in this case enabling the KRaft feature within the Strimzi operator.

- Apache Kafka community is working on removing the dependency of Zookeeper for leader election, which lead to the development of KRaft. 

- In Strimzi this KRaft is experimental feature check the documentation for more info.

- To enable the Kraft featuer we need to enable `UseStrimziPodSets` and `UseKRaft` feature by setting the evnironment variable in the Strizmi cluster operator. Which is done with below command after deploying the operator.

```
kubectl -n kafka set env deployment/strimzi-cluster-operator STRIMZI_FEATURE_GATES=+UseStrimziPodSets,+UseKRaft
```

- After this command the strimzi cluster operator will restart.

- To deploy the Kafka cluster we use the CRD manifest, refer the below yaml file.
The CRD still requires the Zookeeper configuration defined, but will not be used.

- The other options part of the Strimzi operator for zookeeper the entity operator and topic operator can be removed or commented out.
- Below configuration uses ephemeral storage, which means if the Kafak broker restarts the data will be deleted. This deployment is well be helpful for quick deployement.

- save this configuration as KraftMode.yaml
```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: my-cluster
  namespace: kafka
spec:
  kafka:
    version: 3.2.3
    replicas: 1
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: external
        port: 9094
        type: nodeport
        tls: false
        configuration:
          bootstrap:
            nodePort: 31092
          brokers:
            - broker: 0
              advertisedHost: 127.0.0.1
              nodePort: 31234
            - broker: 1
              advertisedHost: 127.0.0.1
              nodePort: 31235
            - broker: 2
              advertisedHost: 127.0.0.1
              nodePort: 31236
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      default.replication.factor: 3
      min.insync.replication: 2
      inter.broker.protocol.version: "3.2"
    storage:
      type: ephemeral
  zookeeper:
    replicas: 3
    storage:
      type: ephemeral
```

> **Important**
>
> - In order to access the Kafka brokers from the host machine (in this case my laptop) the NodePort and Bootstrap configuration is added to the CRD manifest. 
> - Pods running in Kind cluster can't be accessed directly from the host machine we need forward the port of container to host
> - Later in this blog, the kind cluster configuration will expose the necessary ports
> - Below configuration is used to expose the ports and access them from the host machine
> ```yaml
>         configuration:
>         bootstrap:
>           nodePort: 31092
>         brokers:
>           - broker: 0
>             advertisedHost: 127.0.0.1
>             nodePort: 31234
>           - broker: 1
>             advertisedHost: 127.0.0.1
>             nodePort: 31235
>           - broker: 2
>             advertisedHost: 127.0.0.1
>             nodePort: 31236
>```

- Kind cluster configuration
  - We are exposing all the ports, note that the kafka cluster external bootstrap service will be created as a node port and be exposed to 31092, since this port can be accessed from 9092 port from the host, with the below configuraion once the kafka cluster starts running we can access using `localhost:9092` 
  - save this file as kafka_kind_cluster.yaml
```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
networking:
  ipFamily: ipv4
  apiServerAddress: 127.0.0.1
  apiServerPort: 6443
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 31092
    hostPort: 9092
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31234
    hostPort: 31234
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31235
    hostPort: 31235
    listenAddress: "127.0.0.1"
  - containerPort: 31236
    hostPort: 31236
    listenAddress: "127.0.0.1"
```

- To setup the Kind cluster we can use the below make file 

```Makefile
SHELL :=/bin/bash

NAMESPACE ?= kafka
CLUSTERNAME ?= kafka
SLEEPSEC ?= 2000
#KAFKAEPHEMERALCONFIG := kafka-ephemeral-single.yaml
KAFKAEPHEMERALCONFIG := kraftmode.yaml
KAFKACLUSTER := kafka_kind_cluster.yaml

.DEFAULT_GOAL :=info

#------------------------------
#  Note:-
#     - PHONY - usually the targets will be a file in make, in order to indicate it as a task we use PHONY
#     - using @ before the commands in the recipe indicate the statement not to be printed in the stdout
#       without @, the statement will be printed and command will get executed
#     - $$ in the recipe is way to escape the character between the make and the shell command. (refer info target)
#

.PHONY: install-kind-linux
install-kind-linux: 
	@curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.17.0/kind-linux-amd64 
	@chmod +x ./kind 
	@mv ./kind /usr/local/bin/kind 
	@chmod +x /usr/local/bin/kind  

.PHONY: install-kubectl
install-kubectl: 
	@curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
	@chmod +x kubectl 
	@mkdir -p ~/.local/bin 
	@mv ./kubectl /usr/local/bin/kubectl 
	@chmod +x /usr/local/bin/kubectl

.PHONY: install-cluster
install-cluster:
	@kind create cluster --name=${CLUSTERNAME} --config=./${KAFKACLUSTER}

.PHONY: create-namespace
create-namespace:
	@kubectl create ns ${NAMESPACE}

.PHONY: install-operator
install-operator:
	@kubectl create -f 'https://strimzi.io/install/latest?namespace=${NAMESPACE}' -n ${NAMESPACE}

.PHONY: list-resource
list-resource:
	@kubectl -n kafka get pods,svc

.PHONY: list-log
list-log: 
	@kubectl -n ${NAMESPACE} logs pod/$(shell kubectl -n ${NAMESPACE} get pods --no-headers -o=jsonpath='{.items[0].metadata.name}')

.PHONY: wait-zz
wait-zz: # sleep 5 second
	@echo "waiting...." \
	sleep ${SLEEPSEC}

.PHONY: list-log-follow
list-log-follow: 
	kubectl -n ${NAMESPACE} logs pod/$(shell kubectl -n ${NAMESPACE} get pods --no-headers -o=jsonpath='{.items[0].metadata.name}') -f

.PHONY: list-pod-status
list-pod-status: 
	@echo 'POD                          STATUS        CONTAINER->READY*'
	@kubectl -n kafka get pods -o=jsonpath="{range .items[*]}{.metadata.name}{'| \t'}{.status.phase}{'| \t'}{range .status.containerStatuses[*]} {.name}{'->'}{.ready}{' | '}{end}{'\n'}{end}"

.PHONY: delete-kind-cluster
delete-kind-cluster: ; @kind delete cluster --name=${CLUSTERNAME}

.PHONY: install-kafka-cluster
install-kafka-cluster: enable-kraft-feature wait-zz
	kubectl -n ${NAMESPACE} apply -f ${KAFKAEPHEMERALCONFIG}

.PHONY: enable-kraft-feature
enable-kraft-feature: 
	kubectl -n ${NAMESPACE} set env deployment/strimzi-cluster-operator STRIMZI_FEATURE_GATES=+UseStrimziPodSets,+UseKRaft

.PHONY: deploy-operator
deploy-operator: install-cluster create-namespace install-operator wait-zz list-resource
	@echo "installing kafka"

.PHONY: kafka-producer
kafka-producer: 
	echo "kafka producer starting up..."
	@kubectl -n kafka run kafka-producer -ti --image=quay.io/strimzi/kafka:0.32.0-kafka-3.3.1 --rm=true --restart=Never -- bin/kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic

.PHONY: kafka-consumer
kafka-consumer: 
	echo "kafka consumer starting to listen..."
	@kubectl -n kafka run kafka-consumer -ti --image=quay.io/strimzi/kafka:0.32.0-kafka-3.3.1 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic --from-beginning
info:
	@awk '/^[a-zA-Z_-]+: / { print $$0; print "\n"}' $(MAKEFILE_LIST) | \
	awk -F":" 'BEGIN {print "targets" } /^[a-zA-Z_-]+/ {print "    "$$1}'
```
- 
