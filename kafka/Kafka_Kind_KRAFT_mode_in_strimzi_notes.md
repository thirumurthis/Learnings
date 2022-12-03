## Deploy kafka cluster in KIND cluster

- In this article will show how to deploy Strimzi Kafka (KRaft mode) in KIND cluster within Docker desktop.

Prerequisites :
  - Basic understanding of kubernetes and Operator pattern.
  - Basic understanding of kafak
  - Docker Desktop installed and running
  - KIND CLI installed 
  - GNU Make 4.0 installed (for automation)
 
Kafka broker:
  - Currently Kafka cluster uses `Zookeeper` for leader election.
  - Kafka community is moving out of `Zookeeper` and moving towards `KRaft` to manage leader election.
  - `Kraft` mode in Strimzi is still experimental feature and **NOT** to be used in Production.

Deploying Kafka in Kubernetes?
 - To deploy Kafka in Kubernetes the easiest option is to use Strimzi Apache Kafka operator.
 - Strimzi provides different configuration options to deploy in production based on requirement.
 - Strimzi is part of CNCF supported projects.
 - Strimzi has different options to configure persistent of datastore like, 
    - Ephemeral
    - JBOD (Just a Bunch Of Disks)[https://strimzi.io/blog/2019/07/08/persistent-storage-improvements/]
    - Persistent Volumes provided by any Cloud providers
 - Strimzi website has the documented the steps to setup a persistent Kafka cluster in KIND [strimzi.io](https://strimzi.io/quickstarts/).


What does this article includes?

 - Strimzi cluster configuration as Ephemeral persistent storage and in deploying in `KRaft` mode.
 - To access the Kafka brokers running in KIND cluster from host machine we need to expose the ports. Includes the port configuration in both KIND cluster and Strimzi cluster CRD configuration.
 - To access Strimzi cluster from external client, we need to expose external bootstrap service as NodePort.(Refer this article that explains about accessing Strimzi Kafka cluster from external client)[https://strimzi.io/blog/2019/04/23/accessing-kafka-part-2/]ntity
 

Why KRaft (experimental) mode?
 - In my case the reason use Kraft mode is deploying the Strimzi Kafka with Zookeeper in my machine keeps on restarting the Zookeeper and cluster pods very often. I think this is because memory limitation since my laptop only had 8GB memory.
 - KRaft mode doesn't include addition Zookeeper pods and related network traffics. Deploying in Kafka in KRaft mode the 3 broker cluster was running more stable for me.
 - For learning application that uses kafka this was easy to spin up.

> ** NOTE:-**
> - The same Strimzi Cluster configuration will work for Zookeeper, only thing is we need to include Entity operator and Topic operator.
>
  

### Steps to install Strimzi Apache Kafka in KIND cluster
  - 1. Spin up the KIND cluster with require port forward configuration
  - 2. Create a namespace called kafka (for this example)
  - 3. Install the Strimzi operator (this can be done using helm chart as well)
  - 4. Enable the `KRaft` feature in the cluster
  - 5. Install the Strimzi cluster using the CRD configuration 
  - 6. Use Strimzi CRD to deploy topic
  - 7. Use kafka client to connect to cluster

- Later in this article we will see how to automate the installation using Makefile.


#### Step 1: KIND cluster configuration
  - Create a directory and save below configration in file named `kafka_kind_cluster.yaml`.
  - The same will be used in Makefile as well.
  
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

- With Docker Desktop running, to create the kind cluster use below command

```
kind create cluster --name=kafka --config=kafka_kind_cluster.yaml
```
- using `docker ps` now will show the ports are forwaded by lising the `-p` in the result.

#### Step 2: Create namespace
  - We can create the namespace in the cluster either in Imperative (using kubectl command) or Declartive (using yaml manifest) way. 

```
kubectl create ns kafka
```

#### Step 3: Install Strimzi operator
  - Strimzi can be installed using helm chart as well

```
# namespace used is kafka
kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
```

#### Step 4: Enable KRaftMode

 - In order to enable KRaftMode, we need to enable `PodSet` feature using `UseStrimziPodSets`. This feature is already enabled in Version 0.32.0, so we don't need to enable this.
 - Once the operator is in running state we need to execute below command to enable the KRaft feature, by updating the environment variable. We can use below command to achive it.
 
 ```
 kubectl -n kafka set env deployment/strimzi-cluster-operator STRIMZI_FEATURE_GATES=+UseKRaft
 ```

#### Step 5:

- Below yaml file is the Strimzi cluster configuration to spin up three brokers, save this yaml content in file named `kraftmode.yaml`. Place this in the same directory, will be used in Makefile. 

- The listener in the below configuration contains `name: external` and `type: NodePort`, this expose the bootstrap service as NodePort. The brokers are advertised as localhost with `advertiseHost : 127.0.0.1` (loopback address) along with the port number as nodeport.

- The listed ports also used in the KIND cluster configuration which is forwaded.
- The reason to expose the Kafka broker port is, when the external client access the Kafka cluster the bootstrap-external service will provide the list of kafka broker address mostly with the <kafka broker name>:<port>, with advertiseHost this will be 127.0.0.1:<specified-nodeport>.

- Still we specify the Zookeeper configuration below since KRaft is experimental feature. But we don't need to specify the Entity Operator and Topic Operator in this configuration.

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

- Once the Operator restarted with the KRaft feature enabled, we can deploy the cluster using below command

```
kubectl -n kafka apply -f KraftMode.yaml
```

- Check the status of the deployment with below command

```
kafka -n kafka get pods,deploy,svc
```

#### Step 6: Creating a topic using Strimzi CRD kind Kafka Topic

- Below is the configuration to create Topic using the CRD.

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
    name: example-demo
    namespace: kafka
    labels:
      app: example-app
      strimzi.io/cluster: kafka
spec:
   topicName: example-demo
   partitions: 3
   replicas: 3
```

- use below command to view the topic

```
kubectl -n kafka get kafkatopics
```

#### Step 7: Access the Kafka cluster

- We can access the kafka broker using any Kafka client using the bootstrap server `localhost:9092`
- Download the binaries from Apache kafka website, and use the `kafka-console-producer.bat`, `kafka-topic.bat`, etc.

- Below producer which we can send message.

```
kafka-console-producer.bat --bootstrap-server localhost:9092 --topic example-demo aks=all
```

```
# to list the topic
kafka-topic.bat --bootstrap-server localhost:9092 --list
```

### Using the Makefile

- Below is the Makefile which will automate the process of deploying operator, creating cluster and deleting cluster

- Note: Tab space is required on the statements in the recipe of the target

```
SHELL :=/bin/bash

NAMESPACE ?= kafka
CLUSTERNAME ?= kafka
SLEEPSEC ?= 5000
#KAFKAEPHEMERALCONFIG := kafka-ephemeral-single.yaml
KAFKAEPHEMERALCONFIG := kraftmode.yaml
KAFKACLUSTER := kafka_kind_cluster.yaml

.DEFAULT_GOAL :=info

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

.PHONY: wait-zz
wait-zz: # sleep 5 second
	@echo "waiting...." \
	sleep ${SLEEPSEC}

.PHONY: delete-kind-cluster
delete-kind-cluster: ; @kind delete cluster --name=${CLUSTERNAME}

.PHONY: install-kafka-cluster
install-kafka-cluster: enable-kraft-feature wait-zz
	@kubectl -n ${NAMESPACE} apply -f ${KAFKAEPHEMERALCONFIG}
	@sleep ${SLEEPSEC}

.PHONY: enable-kraft-feature
enable-kraft-feature: 
	kubectl -n ${NAMESPACE} set env deployment/strimzi-cluster-operator STRIMZI_FEATURE_GATES=+UseKRaft

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

- `make ` will display the target name
- To deploy the cluster,
  - `make deploy-operator`, wait till the operator is in running status
  - `make install-kafka-cluster` 

### Output
	
- make deploy-operator

![image](https://user-images.githubusercontent.com/6425536/205426061-1df530e5-e25c-4545-b0d4-055b8e896071.png)

- make install-kafka-cluster

![image](https://user-images.githubusercontent.com/6425536/205426118-81f29c6d-a30d-4408-9161-bb51e5b9b7d8.png)

