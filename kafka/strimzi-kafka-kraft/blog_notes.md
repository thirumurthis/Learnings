## Apache kafka with Avro Schema registry


In this article have detailed the steps on configuring Apache Kafka in a KinD cluster. 
To work with schema registry includes steps to configure simple Apicurio schema registry using Apicurio operator.

This would help for local deployment.

All the component for this example is deployed in KinD cluster.

Pre-requisites

 - Docker Desktop/Daemon
 - KinD CLI
 - JBang 
 - Kubectl CLI
 - Helm CLI (4.x+)

The Apache Kafka is installed in KinD cluster with Strimzi helm chart in Kraft mode.

To access the Kafka from the host machine the Strimzi kafka configuration used NodePort, the container port is configured in the KinD Cluster extraPortMappings. Refer [KinD documentation](https://kind.sigs.k8s.io/docs/user/configuration/#extra-port-mappings) for more details.  
The NodePort should be determined ahead of deploying the KinD cluster.

The Avro Schema Apicurio registry can be accessed using domain name with https self-signed since the Apache Apisix Gateway API is deployed, the details shown below. 

The Apache Apisix 3.x version has been update significantly, the dashboard is deprecated and uses new admin ui embedded ui. 

#### KinD cluster creation 

- The KinD cluster configuration with set of NodePort for Kafka, AKHQ and Apisix.

```yaml
apiVersion: kind.x-k8s.io/v1alpha4
kind: Cluster
name: dev
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 31092
    hostPort: 31092
  - containerPort: 31093
    hostPort: 31093
  - containerPort: 31094
    hostPort: 31094
  - containerPort: 31095
    hostPort: 31095
  - containerPort: 31080  # akhq port
    hostPort: 31080
  - containerPort: 30080  # apisix port
    hostPort: 80
  - containerPort: 30443
    hostPort: 443
- role: worker
- role: worker
- role: worker
```

Note, with Apisix installed the AKHQ can be accessed by creating a ApisixRoute configuration. In this case we can access using localhost:31080.

Save the Yaml content to a file, say kind-config.yaml, then to create the 3 node cluster in Docker Desktop or Daemon use Kind cli. Below is the command, which will create cluster named dev

```sh
kind create cluster --config kind-config.yaml
```

<img width="2840" height="214" alt="image" src="https://github.com/user-attachments/assets/747120ae-9792-4433-aac6-0821b60754c4" />

### Install the Strimizi Kafka

The Strimizi is installed using helm chart, we pass in a override some configuration, the content looks like below.

Note, in this case have created the namespace ahead of time where the kafka cluster is being installed. The Strimzi operators will watch this namespace and if Kafka cluster is installed it will start reconciling and creates the cluster.
The namespaces is configured in the override yaml in watchNamespaces property.

Create namespace using command `kubectl create ns demo`

```yaml
# override with 3 replicas
replicas: 3

# watch namespace to scan when cluster or topics are created
watchNamespaces:
  - demo

# make strimizi operator not to scan all the namespace
watchAnyNamespace: false
```

Say, if the above Strimzi override yaml content saved in a file strimizi_override.yaml, then we can use the below command

```
helm upgrade -i -n kafka --create-namespace \
strimzi-opr oci://quay.io/strimzi-helm/strimzi-kafka-operator \
-f strimzi_override.yaml
```

To check the status use `kubectl -n kafka get pods`

### Install the Kafka cluster with KRaft mode

The Kafka cluster configuration with KRaft mode is shown below, to expose as NodePort the listeners configuration is updated with necessary ports.
With this configuration, the kafka cluster can be accessed with bootstrap url localhost:31092 from host machine.

```yaml
# Documentation - https://strimzi.io/docs/operators/in-development/deploying#minimal_configuration_for_kafka_connect
# Basic configuration (required)
apiVersion: kafka.strimzi.io/v1
kind: KafkaNodePool
metadata:
  name: kraft-cntrl-role # (1)
  labels:
    strimzi.io/cluster: demo-clstr # (2)
# Node pool specifications
spec:
  # Replicas (required)
  replicas: 3 # (3)
  # Roles (required)
  roles: # (4)
    - controller
  # Storage configuration (required)
  storage: # (5)
    type: jbod
    volumes:
      - id: 0
        type: persistent-claim
        size: 6Gi
---
apiVersion: kafka.strimzi.io/v1
kind: KafkaNodePool
metadata:
  name: kraft-brkr-role # (1)
  labels:
    strimzi.io/cluster: demo-clstr # (2)
# Node pool specifications
spec:
  # Replicas (required)
  replicas: 3 # (3)
  # Roles (required)
  roles: # (4)
    - broker
  # Storage configuration (required)
  storage: # (5)
    type: jbod
    volumes:
      - id: 0
        type: persistent-claim
        size: 6Gi
---
# Basic configuration (required)
apiVersion: kafka.strimzi.io/v1
kind: Kafka
metadata:
  name: demo-clstr
# Deployment specifications
spec:
  kafka:
    # Kafka version (recommended)
    version: 4.2.0
    # KRaft metadata version (recommended)
    metadataVersion: "4.2"
    # Broker configuration for replication (recommended)
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      default.replication.factor: 3
      min.insync.replicas: 2
    # Listener configuration (required)
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: external
        type: nodeport   # use lower case
        port: 9094
        tls: false
        configuration:
          bootstrap:
            nodePort: 31092
          brokers:
            - broker: 0
              advertisedHost: 127.0.0.1
              nodePort: 31093
            - broker: 1
              advertisedHost: 127.0.0.1
              nodePort: 31094
            - broker: 2
              advertisedHost: 127.0.0.1
              nodePort: 31095
  # Entity Operator (recommended)
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

To check the status of the Kafka cluster deployment use the command `kubectl -n demo get pods` should see pods with brokers and controllers with and entity operator.

<img width="700" height="335" alt="image" src="https://github.com/user-attachments/assets/00988de3-8099-463b-bfd9-83bdfd7657c5" />

### Create kafka topic

The kafka topic configuration looks like below. Two topic are created in below configuration where the labels is used by operator to identify the cluster on which the topics to be created.


```yaml
apiVersion: kafka.strimzi.io/v1
kind: KafkaTopic
metadata:
  name: test-topic-1
  labels:
    strimzi.io/cluster: demo-clstr
spec:
  topicName: test-topic-1
  partitions: 3
  replicas: 2
---
apiVersion: kafka.strimzi.io/v1
kind: KafkaTopic
metadata:
  name: demo-topic-1
  labels:
    strimzi.io/cluster: demo-clstr
spec:
  partitions: 1
  replicas: 1
  config:
    retention.ms: 7200000
```

To check the status of the topics use the command `kubectl -n demo get kt`, the READY should be True.

<img width="750" height="134" alt="image" src="https://github.com/user-attachments/assets/5992cee8-f4a3-4f8d-bcff-ec39b9268390" />


### Install AKHQ

The AKHQ credentials secret can be created with below command. To generate the Password value use `echo -n "admin" | sha256sum`

```sh
kubectl -n demo create secret generic akhq-secret \
--from-literal=AKHQ_ADMIN_USER=admin \
--from-literal=AKHQ_ADMIN_PASSWORD=8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918 \
--from-literal=AKHQ_READ_USER=user \
--from-literal=AKHQ_READ_PASSWORD=04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb
```

The configuration for AKHQ configuration deployed is shown below. The AKHQ installed with admin and reader roles for managing the topics in the cluster, we create the user credentials as secret and mount it as environment.

```yaml
configuration:
  akhq:
    server:
      access-log:
        enabled: true
        name: org.akhq.log.access
        format: "[Date: {}] [Duration: {} ms] [Url: {} {}] [Status: {}] [Ip: {}] [User: {}]" # Logger format
    connections:
      demo-cluster:
        properties:
          bootstrap.servers: "demo-clstr-kafka-bootstrap:9092"
    security:
      #default-group: reader
      roles:
          topic-admin:
          - resources: [ "TOPIC", "TOPIC_DATA" ]
            actions: [ "READ", "CREATE", "DELETE" ]
          - resources: [ "TOPIC" ]
            actions: [ "UPDATE", "READ_CONFIG", "ALTER_CONFIG" ]
      groups:
        admin:
          roles:
            - "admin"
            - "topic-admin"
          patterns:
            - ".*"
        reader:
          roles:
            - "topic/read"
            - "group/read"
          patterns:
            - ".*"
      basic-auth:
        - username: "${AKHQ_ADMIN_USER}"
          password: "${AKHQ_ADMIN_PASSWORD}" # use: echo -n "password" | sha256sum
          passwordHash: SHA256
          group:
            - admin
        - username: "${AKHQ_READ_USER}"
          password: "${AKHQ_READ_PASSWORD}" # use: echo -n "password" | sha256sum
          passwordHash: SHA256
          group:
            - reader
  # Please set the 'micronaut.security.token.jwt.signatures.secret.generator.secret' configuration, or ask your administrator to do it !
  micronaut:
    security:
      enabled: true
      default-group: no-roles
      token:
        jwt:
          signatures:
            secret:
              generator:
                secret: "TSxCQLAkP/bAxXW71tdUX64fIBshJJYbJ39iTvtjEjQ/qWhQaIWhFkETi6ryHw=="

service:
  type: NodePort
  httpNodePort: 31080

extraEnv: 
  - name: AKHQ_ADMIN_PASSWORD
    valueFrom:
      secretKeyRef:
        name: akhq-secret
        key: AKHQ_ADMIN_PASSWORD
  - name: AKHQ_ADMIN_USER
    valueFrom:
      secretKeyRef:
        name: akhq-secret
        key: AKHQ_ADMIN_USER
  - name: AKHQ_READ_PASSWORD
    valueFrom:
      secretKeyRef:
        name: akhq-secret
        key: AKHQ_READ_PASSWORD
  - name: AKHQ_READ_USER
    valueFrom:
      secretKeyRef:
        name: akhq-secret
        key: AKHQ_READ_USER
```

Save the above yaml AKHQ configuration in akhq_config.yaml, use below command to install to cluster.

```sh
helm repo add akhq https://akhq.io/
helm repo update
helm upgrade --install -n demo akhq akhq/akhq -f akhq_config.yaml
```

To check the status of the installation, use `kubectl -n demo get pods`, the akhq pod should be in Running state.

### Install Apisix in decoupled mode with self-signed certificate

Apache Apisix is deployed to the cluster with controller and data plane as deployment, there is another option to deploy as daemonset. With Apisix we can install the cert manager to generate self-signed certificate so the we can create routes for Apisix dashboard. Apicurio UI and backend app which could be accessed with SSL certificate.

Install the certificate manager to the cluster with below helm command

```sh
helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.20.2 \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true
```

Create an Issuer for accessing the Apisix admin UI embedded in the controller plance, we need to create this issuer in apisix namespace.

```yaml
---
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-apisix-ca-issuer
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-apisix-cert
spec:
  commonName: apisix.demo.com  
  secretName: selfsigned-apisix-cert-secret # cert created in this secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-apisix-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - apisix.demo.com  # dns name add this to hosts file for loopback address
---
```

Save the manfiest yaml configuration to apisix-certs.yaml. Install the Issuer and Certificate request to the cluster use below command.

```sh
kubectl create ns apisix
kubectl -n apisix apply -f apisix-cert.yaml
```

To check the status use command `kubectl -n apisix get certificate` to see the READY state is true. 

Install the Apisix deployment with de-coupled mode we use the control-plane and data-plane. 

To install the control-plane use below command, the ca sert should be provided else we would not be able to use https from browser.

```sh
helm upgrade --install --create-namespace -n apisix apisix-cp apisix/apisix \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=control_plane \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=selfsigned-apisix-cert-secret \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set apisix.admin.allow.ipList[0]=0.0.0.0/0 \
  --set etcd.replicaCount=3 \
  --set etcd.enabled=true \
  --wait
```
 
To deploy the data-plane use below command, the release name is apisix-dp and few configuration overrides.
 - service name of control plane is configured in the ingress controller `ingress-controller.gatewayProxy.provider.controlPlane.service.name` service name. After the control plane installed the control plane service can be used. If the `ingress-controller.gatewayProxy.provider.controlPlane.service.name` property is not updated correctly with the control-plane service the Apisix data-plane will not be able to connect to the control plane and reports errors which could seen in the logs.
 - The etcd service is disabled since it is deployed part of control-plane, the etcd url is configured with empty user name so data-plane pods doesn't require authentication to access etcd service internally.
 - The apisix service exposed as NodePort and respecitive port is used. This port is configured in KinD configuration with extraMappingPorts properties. The traffice could be routed from port 80 and 443 to backend service in the cluster. We can use the windows hosts file map a any domain name to 127.0.0.1 in this case `127.0.0.1 apisix.demo.com` With this config we can use custom domain name in hosts file with host name mapping in the hosts file.
- The admin key is hard coded which is not recommended for production deployment. Refer Apisix documentation for alternates options like creating secrets instead. 

```sh 
helm upgrade  --install apisix-dp \
  --namespace apisix \
  --create-namespace \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=data_plane \
  --set apisix.nginx.logs.enableAccessLog=true \
  --set apisix.nginx.logs.errorLogLevel=warn \
  --set apisix.admin.enabled=false \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=selfsigned-apisix-cert-secret \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set service.type=NodePort \
  --set service.http.enabled=true \
  --set service.http.servicePort=80 \
  --set service.http.containerPort=9080 \
  --set service.http.nodePort=30080 \
  --set service.tls.servicePort=443 \
  --set service.tls.nodePort=30443 \
  --set ingress-controller.enabled=true \
  --set ingress-controller.apisix.adminService.namespace=apisix-dp \
  --set ingress-controller.gatewayProxy.createDefault=true \
  --set ingress-controller.gatewayProxy.provider.controlPlane.service.name=apisix-cp-admin \
  --set externalEtcd.user="" \
  --set externalEtcd.host[0]=http://apisix-cp-etcd.apisix.svc.cluster.local:2379 \
  --set etcd.enabled=false \
  --set ingress-controller.gatewayProxy.provider.controlPlane.auth.adminKey.value=edd1c9f034335f136f87ad84b625c8f1 \
  apisix/apisix \
  --wait
```

Once the above command is applied, to check the status of the apisix data-plane pods use `kubectl -n apisix get pods`. There 3 replicas of etcd should be green after deployment followed by apisix control-plane and data-plane to b e green. Make sure all the pods are in Running state to proceed further.

Install the self-signed certificate for traffic we need to create ApisixTls and ApisixRoute for accessing the dashboard. the proxy-rewrite is just to add the header with the admin key.

```yaml
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: sample-tls
spec:
  ingressClassName: apisix
  hosts:
    - apisix.demo.com
  secret:
    name: selfsigned-apisix-cert-secret  # certificate created by the cert-manager
    namespace: apisix
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: dashboard-route
spec:
  ingressClassName: apisix 
  http:
    - name: apisix-db
      match:
        hosts:
          - apisix.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: apisix-cp-admin
          servicePort: 9180
      plugins:
        - name: proxy-rewrite
          enable: true
          config:
            headers:
              set:
                X-Api-Key: "edd1c9f034335f136f87ad84b625c8f1"
```

Save the yaml manifest to apisix-dashboard-route.yaml, apply using command `kubectl -n apisix apply -f apisix-dashboard-route.yaml`

INFO: Update the windows hosts file with `127.0.0.1 apisix.demo.com`, with this update we can access the Apisix dashboard with `https://apisix.demo.com/ui`

To check the status use `kubectl -n apisix get apisixtls` and `kubectl -n apisix get ar`

<img width="1995" height="1305" alt="image" src="https://github.com/user-attachments/assets/6fb59302-8424-4646-8cab-2c60b5a1bca9" />


### Deploy the Apicurio registry with Apicurio operator

The operator will be installed in the apicurio-registry namespace, using command `kubectl create ns apicurio-registry`.

To install the operator we can use below command

```sh
VERSION=3.2.1;
NAMESPACE=apicurio-registry; 
curl -sSL "https://raw.githubusercontent.com/Apicurio/apicurio-registry/$VERSION/operator/install/install.yaml" \
 | sed "s/PLACEHOLDER_NAMESPACE/$NAMESPACE/g" \
 | kubectl -n $NAMESPACE apply -f -
```

The configuration to create a registry ui and backend. The configuration details can be found in this link #https://www.apicur.io/registry/docs/apicurio-registry/3.2.x/getting-started/assembly-operator-config-reference.html#operator-ingress-reference_registry 

The environment variable configured is used to fix the CORS error when accessing the backend service from the ui via browser.

```yaml
apiVersion: registry.apicur.io/v1
kind: ApicurioRegistry3
metadata:
  name: sample
spec:
  app:
    env:
    - name: QUARKUS_HTTP_CORS_ORIGINS
      value: "https://apicurio.app.demo.com,http://apicurio.app.demo.com,https://apicurio.ui.demo.com,http://apicurio.ui.demo.comhttp://localhost:8080"
    - name: QUARKUS_HTTP_CORS
      value: "true"
    ingress:
      enabled: false
  ui:
   env:
    - name: REGISTRY_API_URL
      value: "https://apicurio.app.demo.com/apis/registry/v3"
   ingress:
    enabled: false
```

Save the apicurio configuration yaml to apicurio-configration.yaml, apply to cluster with command `kubectl -n apicurio-registry apply -f apicurio-configuration.yaml`

The configuration to access the Apicurio operator registry UI and app, we create ApisixTls and ApisixRoute in apicurio-registry namespace.

Below configuration creates one issuer for the namespace, two set of certificate is requested for UI and App. The ApisixTls is created for each ApisixRoute 

```yaml
# deploy in apicurio-registry namespace
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-ca-issuer
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-ui-cert
spec:
  commonName: apicurio.ui.demo.com  
  secretName: selfsigned-ui-cert-secret # cert created in this secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames: 
    - apicurio.ui.demo.com
---
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: sample-ui-tls
spec:
  ingressClassName: apisix
  hosts:
    - apicurio.ui.demo.com
  secret:
    name: selfsigned-ui-cert-secret  # certificate created by the cert-manager
    namespace: apicurio-registry
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: apicurio-ui-route
spec:
  ingressClassName: apisix 
  http:
    - name: apicurio-ui-http
      match:
        hosts:
          - apicurio.ui.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: sample-ui-service
          servicePort: 8080
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-app-cert
spec:
  commonName: apicurio.app.demo.com  
  secretName: selfsigned-app-cert-secret # cert created in this secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames: 
    - apicurio.app.demo.com
---
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: sample-app-tls
spec:
  ingressClassName: apisix
  hosts:
    - apicurio.app.demo.com
  secret:
    name: selfsigned-app-cert-secret  # certificate created by the cert-manager
    namespace: apicurio-registry
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: apicurio-app-route
spec:
  ingressClassName: apisix 
  http:
    - name: apicurio-app-http
      match:
        hosts:
          - apicurio.app.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: sample-app-service
          servicePort: 8080
```

Save the above manifest in apicurio-registry-route.yaml and apply using command `kubectl -n apicurio-registry apply -f apicurio-registry-route.yaml`

To check the status use `kubectl -n apicurio-registry get apisixtls,ar`

INFO: Add the entries to the windows hosts file with `127.0.0.1 apicurio.ui.demo.com` and `127.0.0.1 apicurio.ui.demo.com`

With the hosts entry updated the UI can be accessed using `https://apicurio.ui.demo.com`.

The actual registry can be accessed using `https://apicurio.app.demo.com`. In the Java application to access the registry we will grab the apicurio app self-signed certificate secret ca.crt content to access with SSL configuration.

<TO DO> 
example of java code with Avro schema only 
example of java code with registry-route
