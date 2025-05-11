# Strimzi kafka Mirrormaker 2 with Kind cluster

In this blog demonstrated configuring Strimzi Kafka Mirrormaker 2 with use of kind cluster.

## Prequisite:

 - Docker Desktop
 - Kind CLI
 - Kubectl CLI
 - Helm CLI

## Summary

- We create two Kind cluster `source` and `destination`, the kind cluser configuration exposes the NodePort so the container can be accessed from the host machine. 

- With the Kind nodeport exposed, we can configure the Kafka Cluster with NodePort. With this configuration we Refer the [Strimzi blog](https://strimzi.io/blog/2019/04/23/accessing-kafka-part-2/).

- This blog uses Strimzi version 0.46.0, the operator is installed using Helm chart. Note, from 0.46.0 version onwards Kafka removed the Zookeeper so we have to use Kraft mode. 

- The Kafka cluster configuration includes KafkaNodePool with one controller and one broker configured with ephemeral storage.

- The Mirrormaker2 configuration will be deployed to destination cluster, the topic created on the source cluster will be replicatd in the destination server.

- This is not production ready configuration, only foe learing and development purpose. 


### Kind cluster configuration

- The kind kafka source cluster configuration

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

- With the cluster created with above configuration, then we need to deploy the Strimzi operator. We can use helm chart to install the operator the helm command to deploy is shown below 

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

- The same should be deployed to destination cluster with below command

```
$ kubectl --context kind-kafka-dest create ns kafka
$ helm upgrade -i strimzi strimzi/strimzi-kafka-operator --version 0.46.0 -n kafka --kube-context kind-kafka-dest
```

### Deploy kafka cluster with KRaft mode

#### Source kafka cluster configuration

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

### Destination Kafka cluster configuration

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

- To deploy the configuration to the kind cluster use following command

```
-- source kafka cluster 
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-cluster.yaml
```

```
-- destination kafka cluster 
$ kubectl --context kind-kafka-dest -n kafka apply -f kafka-dest-cluster.yaml
```

#### Kafka MirrorMaker2 configuration

- Below is the mirrormaker2 configuration which connects to the plain port. 

This blog doesn't use TLS port, in order to use the TLS port in Mirrirmaker2 configuration we need to create kafka user on the source and destination. Create secrets on the destination kind cluster with the source kafka cluster ca-certificate and user ca-certificate in the destination cluster. And update the Mirrormaker2 configuration with authentication and secret info. Refer the strimzi documentation for more details. 

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

- To deploy the Mirrormaker2 (MM2) on kafka destination cluster we can use below command

```
$ kubectl --context kind-kafka-dest -n kafka apply -f mm2-target-plain.yaml
```

#### Create topic using the CRD

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

- To deploy the topic we can use below command

```
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-test-topic.yaml
```

- Below snapshot lists the pods info on source and destination cluster

![kafka_pods_cluster](https://github.com/user-attachments/assets/7a30b16c-aa3d-441f-a02e-ae1804be343a)



- With below command we can list the topics created on source kafka cluster, changing the context and pod name the same command can be reused to list topics from destination kind cluster.

```
$ kubectl --context kind-kafka-dest -n kafka exec pod/kafka-dest-source-0 -c kafka -- sh -c 'bin/kafka-topics.sh --list --bootstrap-server localhost:9092'
```

- After executing into the kafka cluster pod on the source or destination kind cluster, we can use below command to produca and consume messages.

```
-- exec into the kafka cluster pod to execute 
$ bin/kafka-console-producer.sh  --topic test-topic-1 --bootstrap-server localhost:9092
```

```
-- exec into the kafka cluster pod to execute
$ bin/kafka-console-consumer.sh --topic test-topic-1 --group tt1-group-1 --from-beginning --bootstrap-server localhost:9092
```

- In below snapshot we could see the list of topics list that are available on source and destination kafka cluster after deployment of mirrormaker2.

![topics_list](https://github.com/user-attachments/assets/d6cba90e-5dc7-48eb-83ea-4c05ad91b295)


- A kafka topic named `testing-topic-one` was created using Strimzi KafkaTopic CR manifest, we could see in the below snapshot that after topic creation on the source kafka cluster it is replicated in the destination kafka cluster with name `kafka-src.testing-topic-one` (refer the last command output in the snapshot).

![topic_synced_after_sometime](https://github.com/user-attachments/assets/eab50011-ed64-475c-9fed-dd7abcbb4ef7)


- Below snapshot the first section we produce message on to the topic of source kafka cluster. The message can be consumed on the destination cluster topics which got replicated.
 
![synced_topic_with_msg_from_src](https://github.com/user-attachments/assets/118ecb92-e193-4caf-8461-24ce3d80e426)

