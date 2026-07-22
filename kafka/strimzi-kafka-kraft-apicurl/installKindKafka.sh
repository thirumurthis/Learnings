#!/bin/bash

# create the kind cluster 
kind create cluster --config /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/kind_cluster.yaml

# create namespace for kafka
kubectl create ns demo

# using helm 4 no need to add repo 
# install the kafka operator
helm upgrade -i -n kafka --create-namespace \
strimzi-opr oci://quay.io/strimzi-helm/strimzi-kafka-operator \
-f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/strimzi_override.yaml

# install the cluster with knode pool broker and controller and cluster 
# demo-clstr
kubectl apply -n demo -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/kafka_cluster.yaml
kubectl apply -n demo -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/topics.yaml

#akhq
# echo -n "admin" | sha256sum
# ehco -n "user" | sha256sum
kubectl -n demo create secret generic akhq-secret \
--from-literal=AKHQ_ADMIN_USER=admin \
--from-literal=AKHQ_ADMIN_PASSWORD=8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918 \
--from-literal=AKHQ_READ_USER=user \
--from-literal=AKHQ_READ_PASSWORD=04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb


helm repo add akhq https://akhq.io/

helm repo update
# akhq to access
helm upgrade --install -n demo akhq akhq/akhq -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/akhq_config.yaml

