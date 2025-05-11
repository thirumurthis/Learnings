# Strimzi kafka Mirrormaker 2 with Kind cluster

In this blog demonstrated configuring Strimzi Kafka Mirrormaker2 using of KinD cluster. Strimzi Mirrormaker2 is used to replicate topics and data from one kafka cluster to another.

## Pre-requisite
 - Docker Desktop
 - KinD CLI
 - Kubectl CLI
 - Helm CLI

Note, this blog doesn't deep dive into the concepts, just demonstrates the basic configuration of Mirrormaker in KinD, the configuration can be updated for further learning in the local machine instead of setting up an actual K8S cluster.

## Summary

Two Kind cluster created as source and destination, the NodePort of the KinD container is exposed with the extraPortMapping config. So two KinD cluster can communicate with each other without any network customization. 

Refer the [Strimzi blog](https://strimzi.io/blog/2019/04/23/accessing-kafka-part-2/) for details how to configure the Kafka cluster to communicate with NodePort and other options.

Starting Kafka version 4.0.0 and Strimzi version 0.46.0, Zookeeper is removed to favor Kraft consensus mode. In this blog the Kafka cluster in deployed in Kraft mode, which includes KafkaNodePool resource setup with one controller and one broker using the ephemeral storage. Since ephemeral storage, the data won't be persisted if the container is deleted.

Mirrormaker2 can be deployed in different patterns, refer the [documentation](https://developers.redhat.com/articles/2023/11/13/demystifying-kafka-mirrormaker-2-use-cases-and-architecture#use_cases) for more details. In this blog the Mirrormaker2 is deployed at the destination/target Kafka cluster which follows the data replication in unidirectional pattern. The Mirrormaker2 configuration uses Plain listener ports and not TLS so no encryption is done.

 Note, this is not production ready configuration, mostly to be used for learning and development purpose. 

### Kind cluster configuration

Below is the Kafka source cluster configuration, the extraPortMapping config exposes 32200 port which will be used in the Mirrormaker2 config so destination cluster connects to source clusters listener with plain port.

```yaml
# file-name: kind-src-config.yaml 
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: kafka-src
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30192
    hostPort: 30192
  - containerPort: 32200
    hostPort: 32200
```

The kind kafka destination cluster configuration

```yaml
# file-name: kind-dest-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: kafka-dest
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30193
    hostPort: 30193
  - containerPort: 32201
    hostPort: 32201
```

Use KinD CLI to create the cluster in Docker desktop, command is as follows

```
-- to create source cluster
kind create cluster --config kind-src-config.yaml

-- to create destination cluster
kind create cluster --config kind-dest-config.yaml
```

Below diagram provides an overview of the cluster and topics detail

![kafka-strimzi](https://github.com/user-attachments/assets/5a8246aa-3836-4daa-83ce-60e540bdee2b)


### Strimzi operator installation

After creating the KinD cluster with above configuration, next would be installing the Strimzi operator. We use helm chart or kubectl to install the operator in here we use Helm CLI to deploy the command is shown below.

```
-- add the helm repo 
$ helm repo add strimzi https://strimzi.io/charts/
```

Create namespace on the source kind cluster

```
$ kubectl --context kind-kafka-src create ns kafka
```

Deploy the Strimzi operator in the source cluster

```
$ helm upgrade -i strimzi strimzi/strimzi-kafka-operator --version 0.46.0 -n kafka --kube-context kind-kafka-src
```

Deploy Strimzi operator in destination cluster, the context is changed in below command.

```
$ kubectl --context kind-kafka-dest create ns kafka
$ helm upgrade -i strimzi strimzi/strimzi-kafka-operator --version 0.46.0 -n kafka --kube-context kind-kafka-dest
```

### kafka cluster deployment

#### Source kafka cluster deployment

The Kafka cluster listener configuration includes container name in the  advertisedhost, since KinD cluster is exposed with NodePort both the kafka source and destination server can communicate with each other with the container name as both container are in the same bridge network. We can configure the advertisedHost with IP address but we use container name which will be easy incase if we need to automate the deployment with scripts.

To get the container IP address in Docker Desktop, use `docker network inspect kind`.

Kafka source cluster configuration

```yaml
# file-name: kafka-src-cluster.yaml
---
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
  name: source
  labels:
    strimzi.io/cluster: kafka-src
spec:
  replicas: 1
  roles:
    - controller
    - broker
  storage:
    type: jbod
    volumes:
      - id: 0
        type: ephemeral
        kraftMetadata: shared
---
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: kafka-src
  annotations:
    strimzi.io/node-pools: enabled
    strimzi.io/kraft: enabled
spec:
  kafka:
    version: 4.0.0
    metadataVersion: 4.0-IV3
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
      - name: external
        port: 9094
        type: nodeport
        tls: false
        configuration:
          bootstrap:
             nodePort: 30192
          brokers:
            - broker: 0
              advertisedHost: kafka-src-control-plane # Container name
              advertisedPort: 32200
              nodePort: 32200
    config:
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      default.replication.factor: 1
      min.insync.replicas: 1
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

Destination Kafka cluster configuration

```yaml
# file-name: kafka-dest-cluster.yaml
---
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
  name: source
  labels:
    strimzi.io/cluster: kafka-dest
spec:
  replicas: 1
  roles:
    - controller
    - broker
  storage:
    type: jbod
    volumes:
      - id: 0
        type: ephemeral
        kraftMetadata: shared
---
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: kafka-dest
  annotations:
    strimzi.io/node-pools: enabled
    strimzi.io/kraft: enabled
spec:
  kafka:
    version: 4.0.0
    metadataVersion: 4.0-IV3
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
      - name: external
        port: 9094
        type: nodeport
        tls: false
        configuration:
          bootstrap:
             nodePort: 30193
          brokers:
            - broker: 0
              advertisedHost: kafka-dest-control-plane # Cluster name
              advertisedPort: 32201
              nodePort: 32201
    config:
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      default.replication.factor: 1
      min.insync.replicas: 1
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

To deploy the cluster configuration to the KinD cluster use following command.

```
-- source kafka cluster 
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-cluster.yaml
```

```
-- destination kafka cluster 
$ kubectl --context kind-kafka-dest -n kafka apply -f kafka-dest-cluster.yaml
```

#### Kafka MirrorMaker2 configuration

The Mirrormaker2 configuration below uses the listener plain port. To use TLS listener port we need to create Kafka User with ACL and configure the ca certificates as secret in the Kafka destination cluster. The Mirrormaker2 configuration requires change to add the authentication with the cluster certificate and user secret name. Refer Strimzi documentation for more details. 

Mirrormaker2 configruation

```yaml
# file-name: mm2-target-plain.yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaMirrorMaker2
metadata:
  name: mm2-target-plain
spec:
  version: 4.0.0
  replicas: 1
  connectCluster: "kafka-dest" # Must be the target custer
  clusters:
  - alias: "kafka-src" # Source cluster
    bootstrapServers: kafka-src-control-plane:32200
  - alias: "kafka-dest" # Target cluster
    bootstrapServers: kafka-dest-kafka-bootstrap:9092
    config:
      # -1 means it will use the default replication factor configured in the broker
      config.storage.replication.factor: -1
      offset.storage.replication.factor: -1
      status.storage.replication.factor: -1
  mirrors:
  - sourceCluster: "kafka-src"
    targetCluster: "kafka-dest"
    sourceConnector:
      tasksMax: 1
      config:
        # -1 means it will use the default replication factor configured in the broker
        replication.factor: -1
        offset-syncs.topic.replication.factor: -1
        sync.topic.acls.enabled: "false"
        refresh.topics.interval.seconds: 600
    checkpointConnector:
      tasksMax: 1
      config:
        # -1 means it will use the default replication factor configured in the broker
        checkpoints.topic.replication.factor: -1
        sync.group.offsets.enabled: "false"
        refresh.groups.interval.seconds: 600
    topicsPattern: ".*"
    groupsPattern: ".*"
```

To deploy the Mirrormaker2 (MM2) in the kafka destination cluster use below command.

```
$ kubectl --context kind-kafka-dest -n kafka apply -f mm2-target-plain.yaml
```

Below is the manifest to create Kafka Topic

```yaml
# file-name: kafka-src-test-topic.yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: my-test-topic-1
  labels:
    strimzi.io/cluster: kafka-src
spec:
  partitions: 1
  replicas: 1
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824

```

To cerate the topic in cluster deploy the topics manifest with below command

```
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-test-topic.yaml
```

Below snapshot shows the created pods and status in both source and destination cluster with the Mirrormaker2 deployed. Note the mm2-target-plain-mirrormaker2-0 pod in the destination cluster.

![kafka_pods_cluster](https://github.com/user-attachments/assets/7a30b16c-aa3d-441f-a02e-ae1804be343a)


To list the topics created on source kafka cluster we can use below command. Same can be used to list topics in destination change the context and pod name.

```
$ kubectl --context kind-kafka-dest -n kafka exec pod/kafka-dest-source-0 -c kafka -- sh -c 'bin/kafka-topics.sh --list --bootstrap-server localhost:9092'
```

To produce and consume messages exec into the Kafka pod and issue below command.

```
-- exec into the kafka cluster pod to execute 
$ bin/kafka-console-producer.sh  --topic test-topic-1 --bootstrap-server localhost:9092
```

```
-- exec into the kafka cluster pod to execute
$ bin/kafka-console-consumer.sh --topic test-topic-1 --group tt1-group-1 --from-beginning --bootstrap-server localhost:9092
```

In below snapshot we could see the list of topics that are available on source and destination Kafka cluster after deploying mirrormaker2.

![topics_list](https://github.com/user-attachments/assets/d6cba90e-5dc7-48eb-83ea-4c05ad91b295)


After deploying the cluster and Mirrormaker2 configuration to the appropriate cluster, when a Kafka topic (named testing-topic-one) created with KafkaTopic manifest in the Kafka source cluster the topics will get replicated to the destination cluster with the name kafka-src.testing-topic-one (refer the last kubectl command output in below snapshot).

![topic_synced_after_sometime](https://github.com/user-attachments/assets/eab50011-ed64-475c-9fed-dd7abcbb4ef7)


In the below snapshot, first command produces message on to the topic in source Kafka cluster. Same message being consumed on the destination cluster replicated topic.
 
![synced_topic_with_msg_from_src](https://github.com/user-attachments/assets/118ecb92-e193-4caf-8461-24ce3d80e426)

Reference documentation [1](https://strimzi.io/blog/2020/03/30/introducing-mirrormaker2/)
