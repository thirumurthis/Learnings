## Deploy Kafka cluster in KIND cluster

*   In this article, I will explain to show how we can deploy Strimzi Kafka enabling KRaft mode in KIND cluster within the Docker desktop.
    
*   The configuration defined in this article also shows how we can access Kafka broker from the host machine.
    

Prerequisites :

*   Basic understanding of Kubernetes and Operator patterns
    
*   Basic understanding of Apache Kafka
    
*   Docker Desktop installed and running
    
*   KIND CLI installed
    
*   GNU Make 4.0 installed (for automation)
    

Apache Kafka Zookeeper Vs KRaft:

*   Kafka community currently recommends using `Zookeeper` for leader election in the production environment. Due to lots of limitations Kafka community is moving out of `Zookeeper`.
    
*   `KRaft` is new way by which Kafka manages quoram between multiple clusters.
    
*   `Kraft` is still an experimental feature and **NOT** to be used in production yet.
    

Deploying Kafka in Kubernetes?

*   To deploy Kafka in Kubernetes the easiest option is Strimzi Apache Kafka Operator.
    
*   `Strimzi` Operators can be configured to deploy Kafka brokers in production.
    
*   `Strimzi` is CNCF supported project.
    
*   Persistent datastore in `Strimzi` has different options like,
    
    *   Ephemeral
        
    *   JBOD [Just a Bunch Of Disks](https://strimzi.io/blog/2019/07/08/persistent-storage-improvements/)
        
    *   Persistent Volumes provided by any Cloud providers
        
*   [strimzi.io](https://strimzi.io/quickstarts/) details the steps to deploy a persistent Kafka cluster in KIND.
    

### What does this article include?

*   We will deploy `Strimzi` Kafka cluster with 3 brokers and Ephemeral type datastore. With ephemeral configuration, if the broker is destroyed the data will be lost.
    
*   How to enables the experimental `KRaft` mode in `Strimzi` Operator.
    
*   How we can access the Kafka brokers running in `KIND` cluster from the host machine.
    
    *   In Windows machine to access the containers running `KIND` cluster we need to forward the port, by updating the Strimzi cluster configuration.
        
    
    > **INFO**
    > 
    > *   Accessing `Strimzi Apache Kafka` brokers from the outside of kubernetes cluster it requires bootstrap service to be exposed as NodePort. [Refer to this article that explains accessing Strimzi Kafka cluster from an external client](https://strimzi.io/blog/2019/04/23/accessing-kafka-part-2/)
    >     
    

#### Why KRaft (experimental) mode?

*   In my case, the reason use Kraft mode is deploying the Strimzi Kafka with Zookeeper in my machine keeps on restarting the Zookeeper and cluster pods very often. I think this is because of memory limitations since my laptop only had 8GB of memory.
    
*   `KRaft` feature doesn't include Zookeeper pods and related network traffics was less.
    
*   Deploying Kafka in KRaft mode with 3 brokers in `KIND` cluster was always successful. Another observation, when using the producer bat script to send messages to the cluster initially there were retry error messages.
    
*   Since it is easy to spin up in a local machine we can use this to develop Kafka based applications locally.
    

> **NOTE:-**
> 
> *   The same Strimzi Cluster configuration will work for `non- kRaft` mode where `Zookeeper` is used, the configuration requires including the `Entity operator` and `Topic operator` which can have empty values.
>     

### Steps to install Strimzi Apache Kafka in KIND cluster

*   1\. Spin up the KIND cluster with the required port forward configuration
    
*   2\. Create a namespace called `kafka`
    
*   3\. Install the `Strimzi` Operator (This can be done using Helm chart as well)
    
*   4\. Enable `KRaft` feature
    
*   5\. Install the Strimzi cluster using the CRD configuration
    
*   6\. Create Topic using Strimzi CRD in the Kafka broker
    
*   7\. Connect to Strimzi Kafka broker using Apache Kafka binary bat script
    

> **INFO**
> 
> *   Later in this article, we will see how to automate the process using GNU `make` .
>     

#### Step 1: Create KIND cluster with port forward configuration

*   To create `Kind` cluster we can use the below configuration which includes a list of port that forwards the network traffic to the cluster. Since we use GNU `make`save this in a file named `kafka_kind_cluster.yaml`.
    

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

*   If Docker Desktop is running, execute the below command to create `KIND` cluster
    

```plaintext
kind create cluster --name=kafka --config=kafka_kind_cluster.yaml
```

*   `docker ps` will display the ports that forwards the traffic like below
    
    ```plaintext
    CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES 
    e5c4d36547af kindest/node:v1.25.2 "/usr/local/bin/entr…" About an hour ago Up About an hour 127.0.0.1:6443->6443/tcp, 127.0.0.1:31234-31236->31234-31236/tcp, 127.0.0.1:9092->31092/tcp kafka-control-plane
    ```
    

#### Step 2: Create a namespace

*   We can create the namespace in the cluster Imperatively using `kubectl` command or Declaratively using YAML manifest.
    

```plaintext
kubectl create ns kafka
```

#### Step 3: Install Strimzi operator

*   `Strimzi` can also be installed using Helm chart as well, the steps are available in Strimzi documentation.
    

```plaintext
# namespace used is kafka
kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
```

#### Step 4: Enable KRaft feature

*   Kraft mode requires the feature `UseStimziPodSets` also to be enabled, since this feature is already enabled in `Strimzi Version 0.32.0` no action is required.
    
*   The enable `Kraft` mode we need to set the environment variable in Strimzi Operator. Before enabling the `KRaft` feature we ned to make sure the Operator is in `Running` status using `kubectl -n kafka get pods,svc,deployment` command.
    
*   Executing the command below will enable the `KRaft` feature in the operator eventually restarts it.
    

```plaintext
kubectl -n kafka set env deployment/strimzi-cluster-operator STRIMZI_FEATURE_GATES=+UseKRaft
```

#### Step 5: Install Kafka cluster using Strimzi CRD definition

*   This section will detail the `Strimzi` CRD to define Kafka cluster configuration that will spin up `3 brokers` with `Ephemeral` type storage.
    

> **INFO**
> 
> *   The listener configuration exposes the service as NodePort with the specified port.
>     
>     ```plaintext
>     name: external
>     type: NodePort
>     port: 9094
>     ```
>     
> *   When the Kafka client connects to the broker the bootstrap server sends the list of brokers with the leader info, by using `advertisedHost: 127.0.0.1` the host machine can connect to the broker.
>     
> *   Additionally, these broker ports are used in the KIND cluster configuration to forward the traffic to Kafka broker.
>     
> *   The configuration below still specifies the Zookeeper details since KRaft is an experimental feature. We don't need to specify the `Entity Operator` and `Topic Operator` for KRaft mode.
>     

*   The configuration below defines 3 brokers, expose service as Nodeport and Storage type as Ephemeral. Save the content to file `kraftmode.yaml`
    

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

*   Once `Kraft` feature is enabled and the `Strimzi` operator is in `Running` state, execute the command below to deploy the Kafka cluster.
    

```plaintext
kubectl -n kafka apply -f kraftMode.yaml
```

*   To track the status of the resource execute the command below.
    

```plaintext
kafka -n kafka get pods,deploy,svc
```

#### Step 6: Create Topic using Strimzi CRD

*   The configuration to create a Topic using the CRD is listed below, save the content to a file named topic.yaml
    

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

*   To create the topic and view the details of the created topic use the commands below
    

```plaintext
# to create the topic
kubectl -n kafka apply -f topic.yaml

# to view the created topic
kubectl -n kafka get kafkatopics
```

#### Step 7: Access the Kafka cluster

*   We can access the kafka broker using any Kafka client, the bootstrap server now is accessible via `localhost:9092`
    
*   Download the binaries from `Apache kafka` website, and use the `kafka-console-producer.bat`, `kafka-topic.bat`, etc. to connect the brokers.
    
*   The command below uses the producer script to send messages.
    

```plaintext
kafka-console-producer.bat --bootstrap-server localhost:9092 --topic example-demo aks=all
```

```plaintext
# once messages are sent we can list the topic with below command
kafka-topic.bat --bootstrap-server localhost:9092 --list
```

### Using the Makefile

*   The `Makefile` below automates part of the process of `deploying the operator`, `creating the cluster` and `deleting the kind cluster`.
    

> **Note:**
> 
> *   Save below content in a file called `Makefile`, make sure the recipe under the target starts with a `tab space` else make command will error out.
>     
> *   This `Makefile` contains additional targets like `installing kind cli` and `install kubectl`, etc. but this only works on Linux machines.
>     

```plaintext
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

#### Targets

*   Executing the command `make` will list the target names.
    
*   Step 1: To deploy the operator use command, `make deploy-operator`,
    
*   Step 2: Once the `Strimzi` operator status is `Running`. Execute `make install-kafka-cluster` command to install the cluster. This will enable the `KRaft` feature and install the cluster with the configuration.
    
*   Note: Make sure the file name matches and all files are in the same directory.
    

### Output

*   `make deploy-operator`
    

![image](https://user-images.githubusercontent.com/6425536/205426061-1df530e5-e25c-4545-b0d4-055b8e896071.png align="left")

*   `make install-kafka-cluster`
    

![image](https://user-images.githubusercontent.com/6425536/205426118-81f29c6d-a30d-4408-9161-bb51e5b9b7d8.png align="left")

*   Created topic details
    

![image](https://user-images.githubusercontent.com/6425536/205426204-bdfecea8-ebb1-4bf5-8a88-edf466106704.png align="left")

> **CODE:-** Code can be downloaded from [Github](https://github.com/thirumurthis/projects/tree/kafka_strimzi_kind_code).

### Additional info

*   If the KRaft mode is disabled (environment variable `UseKraft` is turned off), the configuration below can be used to deploy Kafka cluster that uses `Zookeeper` to manage leader elections.
    
*   In that case `Makefile` requires changes, remove the target that enables Kraft mode.
    

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
    config:
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      default.replication.factor: 1
      min.insync.replicas: 1
      inter.broker.protocol.version: "3.2"
    storage:
      type: ephemeral
  zookeeper:
    replicas: 3
    storage:
      type: ephemeral
  entityOperator:
    topicOperator: {}
    userOperator: {}
```
