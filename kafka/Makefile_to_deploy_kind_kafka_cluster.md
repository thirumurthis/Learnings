- Makefile to deploy kind cluster
```
SHELL :=/bin/bash

NAMESPACE ?= kafka
CLUSTERNAME ?= kafka
SLEEPSEC ?= 2000
KAFKAEPHEMERALCONFIG := kafka-ephemeral-single.yaml
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
install-kafka-cluster: 
	@kubectl -n ${NAMESPACE} apply -f ${KAFKAEPHEMERALCONFIG}

.PHONY: deploy
deploy: install-cluster create-namespace install-operator wait-zz list-resource
	@echo "installing kafka"

info:
	@awk '/^[a-zA-Z_-]+: / { print $$0; print "\n"}' $(MAKEFILE_LIST) | \
	awk -F":" 'BEGIN {print "targets" } /^[a-zA-Z_-]+/ {print "    "$$1}'
```
