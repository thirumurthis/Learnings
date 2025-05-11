# Strimzi kafka Mirrormaker 2 with Kind cluster

In this blog demonstrated configuring Strimzi Kafka Mirrormaker2 using of KinD cluster. Strimzi Mirrormaker2 is used to replicate topics and data from one kafka cluster to another.

## Prequisite:

 - Docker Desktop
 - Kind CLI
 - Kubectl CLI
 - Helm CLI

Note, this blog doesn't deep dive into the concepts, just demonstrates the basic configuration of Mirrormaker in KinD, the configuration can be updated for further learning in the local machine instead of setting up an actual K8S cluster.

## Summary

- Two Kind cluster created as `source` and `destination`, the NodePort of the KinD container is exposed with the `extraPortMapping` config. So two KinD cluster can communicate with each other without any network customization. 

- To configure the Kafka cluster to communicate With NodePort refer the [Strimzi blog](https://strimzi.io/blog/2019/04/23/accessing-kafka-part-2/).

- From Kafka veersion 4.0.0 and Strimzi version 0.46.0 Zookeeper is removed to favour Kraft mode. To deploy the Kafka cluster in Kraft mode, the cluster configuration includes KafkaNodePool resource with one controller and one broker. Being a local environment the cluster is configured to use ephemeral storage, the data won't be persisted if the container gets deleted.

- Mirrormaker2 can be deployed in different patterns, refer the [documentation](https://developers.redhat.com/articles/2023/11/13/demystifying-kafka-mirrormaker-2-use-cases-and-architecture#use_cases) for more details. In this blog we have deployed the Mirrormaker2 at the target or destination where the Kafka topics are to be replicated. The Mirrormaker2 configuration directly uses Plain listener and not TLS so no encryption is done.

- Note, this is not production ready configuration, mostly to be used for learing and development purpose. 

### Kind cluster configuration

- The kind kafka source cluster configuration, the `extraPortMapping` config exposes `32200` port which will be used on the Mirrormaker2 config connects to listener with plain port.

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

- The kind kafka destination cluster configuration

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

- To create the cluster in Docker desktop use the Kind CLI, the command is as follows

```
-- to create source cluster
kind create cluster --config kind-src-config.yaml

-- to create destination cluster
kind create cluster --config kind-dest-config.yaml
```

- Below diagram provides an overview of the cluster and topics detail

![kafka-strimzi](https://github.com/user-attachments/assets/5a8246aa-3836-4daa-83ce-60e540bdee2b)


### Strimzi operator installation

- After creating the KinD cluster with above configuration then the Strimzi operator needs to be installed. We can use helm chart or Kubectl to install the operator, in here we use Helm CLI the command to deploy is shown below.

```
-- add the helm repo 
$ helm repo add strimzi https://strimzi.io/charts/
```

```
-- create namespace on the source kind cluster
$ kubectl --context kind-kafka-src create ns kafka
```

```
-- deploy the operator in the source cluster
$ helm upgrade -i strimzi strimzi/strimzi-kafka-operator --version 0.46.0 -n kafka --kube-context kind-kafka-src
```

- The Strimzi operator should also be deployed to destination cluster, the context is changed in below command.

```
$ kubectl --context kind-kafka-dest create ns kafka
$ helm upgrade -i strimzi strimzi/strimzi-kafka-operator --version 0.46.0 -n kafka --kube-context kind-kafka-dest
```

### kafka cluster deployment

#### Source kafka cluster deployment

- The listener configuration includes container name in the  `advertisedhost`, since we use the NodePort the docker port is exposed and uses the same bridge network we can access using the container name instead of the IP address. With the `docker network inspect kind` command we can get the name of the container cluster and IP address as well.

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

### Destination Kafka cluster deployment

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

- To deploy the cluster configuration to the KinD cluster use following command.

```
-- source kafka cluster 
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-cluster.yaml
```

```
-- destination kafka cluster 
$ kubectl --context kind-kafka-dest -n kafka apply -f kafka-dest-cluster.yaml
```

#### Kafka MirrorMaker2 configuration

- Mirrormaker2 configuration uses the listener plain port. 

To use TLS listener port Mirrirmaker2 configuration will be different were the we need to create kafka user on the source and destination. The certificates of the should be created as secrets on the destination Kind cluster. The Mirrormaker2 configuration has to be updated with the authentication details. Refer the strimzi documentation for more details. 

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

- With below command the Mirrormaker2 (MM2) can be deployed on kafka destination cluster.

```
$ kubectl --context kind-kafka-dest -n kafka apply -f mm2-target-plain.yaml
```

#### KafaTopic creation

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

- Deploy the topics manifest with below command

```
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-test-topic.yaml
```

- Below snapshot we could see the pods created and status of those pods in source and destination cluster

![kafka_pods_cluster](https://github.com/user-attachments/assets/7a30b16c-aa3d-441f-a02e-ae1804be343a)


- To list the topics created on source kafka cluster we can use below command. Same can be used to list topics in destination change the context and pod name.

```
$ kubectl --context kind-kafka-dest -n kafka exec pod/kafka-dest-source-0 -c kafka -- sh -c 'bin/kafka-topics.sh --list --bootstrap-server localhost:9092'
```

- Below command can be used to produce and consume message on the kafka pod. To execute the below command use `kubectl exec` command to shell into the kafka cluster pod.

```
-- exec into the kafka cluster pod to execute 
$ bin/kafka-console-producer.sh  --topic test-topic-1 --bootstrap-server localhost:9092
```

```
-- exec into the kafka cluster pod to execute
$ bin/kafka-console-consumer.sh --topic test-topic-1 --group tt1-group-1 --from-beginning --bootstrap-server localhost:9092
```

- In below snapshot we could see the list of topics that are available on source and destination kafka cluster after deploying mirrormaker2.

![topics_list](https://github.com/user-attachments/assets/d6cba90e-5dc7-48eb-83ea-4c05ad91b295)


- Now when we create a kafka topic named `testing-topic-one`, with KafkaTopic CR manifest. Once the topics is created the topic should be available in the destination server. Below snapshot shows the created topic creation on the source kafka cluster first and it is replicated in the destination kafka cluster with name `kafka-src.testing-topic-one` (refer the last command output in below snapshot).

![topic_synced_after_sometime](https://github.com/user-attachments/assets/eab50011-ed64-475c-9fed-dd7abcbb4ef7)


- Below snapshot the first command produce message on to the topic in source kafka cluster. The message being consumed on the destination cluster replicated topic.
 
![synced_topic_with_msg_from_src](https://github.com/user-attachments/assets/118ecb92-e193-4caf-8461-24ce3d80e426)

