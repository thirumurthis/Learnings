
cd /mnt/c/thiru/edu/gitsource/Learnings/kafka

cd strimzi-kafka-kraft
kind create cluster --config /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/kind_cluster.yaml

# this is required to be crated first, else cluster will throw error since 
# watchnamespace is specified in override yaml
kubectl create ns demo


helm upgrade -i -n kafka --create-namespace \
strimzi-opr oci://quay.io/strimzi-helm/strimzi-kafka-operator \
-f strimzi_override.yaml

# install the cluster with knode pool broker and controller
kubectl apply -n demo -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/kafka_cluster.yaml


kubectl -n demo run kafka-producer -it \
--image=quay.io/strimzi/kafka:0.51.0-kafka-4.2.0 --rm=true \
--restart=Never -- bin/kafka-console-producer.sh \
--bootstrap-server demo-kafka-kafka-bootstrap:9092 \
--topic test-cmd

kubectl -n demo run kafka-consumer -it \
--image=quay.io/strimzi/kafka:0.51.0-kafka-4.2.0 --rm=true \
--restart=Never -- bin/kafka-console-consumer.sh \
--bootstrap-server demo-kafka-kafka-bootstrap:9092 \
--topic test-cmd


helm upgrade --install -n demo akhq akhq/akhq -f akhq_config.yaml

kubectl create ns apicurio-registry

# https://github.com/Apicurio/apicurio-registry/tree/main/operator
# the chart registry
VERSION=3.2.1;NAMESPACE=apicurio-registry; curl -sSL "https://raw.githubusercontent.com/Apicurio/apicurio-registry/$VERSION/operator/install/install.yaml" | sed "s/PLACEHOLDER_NAMESPACE/$NAMESPACE/g" | kubectl -n $NAMESPACE apply -f -


kubectl create ns apicurio-registry
kubectl -n apicurio-registry apply -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/apicurio-registry-operator.yaml

once the config is deployed 

port-forward the sample ui and sample app service 
k -n apicurio-registry port-forward svc/sample-app-service 8080:8080
k -n apicurio-registry port-forward svc/sample-app-service 8888:8080

from the browser use http://localhost:8888, the configuration includes cors config to work

below is just from the documenation

https://www.apicur.io/registry/docs/apicurio-registry-operator/1.2.0-dev-v2.6.x/assembly-operator-quickstart.html#registry-operator-quickstart
