# Strimzi kafka Mirrormaker 2 with Kind cluster

In this blog demonstrated configuring Strimzi Kafka Mirrormaker 2 with use of kind cluster.

## Prequisite:

 - Docker Desktop
 - Kind CLI
 - Kubectl CLI
 - Helm CLI

## Summary

We create two Kind cluster `source` and `destination`, the kind cluser configuration exposes the NodePort so the container can be accessed from the host machine. 

With the Kind nodeport exposed, we can configure the Kafka Cluster with NodePort. With this configuration we Refer the [Strimzi blog](https://strimzi.io/blog/2019/04/23/accessing-kafka-part-2/).

This blog uses Strimzi version 0.46.0, the operator is installed using Helm chart. Note, from 0.46.0 version onwards Kafka removed the Zookeeper so we have to use Kraft mode. 

The Kafka cluster configuration includes KafkaNodePool with one controller and one broker configured with ephemeral storage.

The Mirrormaker2 configuration will be deployed to destination cluster, the topic created on the source cluster will be replicatd in the destination server.


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

<\<IMAGE - working diagram >>


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

### Deploy the kafka cluster with KRaft mode

#### Source kafka cluster configuration

- The listener configuration includes container name in the  `advertisedhost`, since we use the NodePort the docker port is exposed and uses the same bridge network we can access using the container name instead of the IP address. With the `docker network inspect kind` command we can get the name of the container cluster and IP address as well.
- The 

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

This blog doesn't use TLS port, in order to use the TLS port we need to create kafka user on the source and destination. Copy the kafka source ca-certificate and user ca-certificate to the destination cluster and update the MM2 configuration. 

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

- To deploy the MM2 kafka destination cluster use below command

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

- Deploy the topic using below command

```
$ kubectl --context kind-kafka-src -n kafka apply -f kafka-src-test-topic.yaml
```

<\<IMAGE - kafka pods >>


- To list the topics created on source use below command

```
kubectl --context kind-kafka-dest -n kafka exec pod/kafka-dest-source-0 -c kafka -- sh -c 'bin/kafka-topics.sh --list --bootstrap-server localhost:9092'
```

- To list the topics created on destination use below command

```
kubectl --context kind-kafka-src -n kafka exec pod/kafka-src-source-0 -c kafka -- sh -c 'bin/kafka-topics.sh --list --bootstrap-server localhost:9092'
```

- To produce message in the source exec to the pod and use below command

```
bin/kafka-console-producer.sh  --topic test-topic-1 --bootstrap-server kafka-src-kafka-bootstrap:9092
```

- To consume the message we can use below command after exec into the source pod

```
bin/kafka-console-consumer.sh --topic test-topic-1 --group tt1-group-1 --from-beginning --bootstrap-server kafka-src-kafka-bootstrap:9092
```
