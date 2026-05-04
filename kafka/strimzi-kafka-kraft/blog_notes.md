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
 - Helm CLI (4+)

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

Save the Strimzi configuration yaml to a file named strimizi_override.yaml, use below helm command to deploy the strimizi operator in cluster

```
helm upgrade -i -n kafka --create-namespace \
strimzi-opr oci://quay.io/strimzi-helm/strimzi-kafka-operator \
-f strimzi_override.yaml
```

To check the status use `kubectl -n kafka get pods`

### Install the Kafka cluster with KRaft mode

The Kafka cluster configuration with KRaft mode is shown below, the listeners are configured with NodePort with necessary ports so the kafka cluster could be accessed from the host machine.

After applying below configuration and with the KinD cluster extraMappingPorts property updated, the kafka cluster can be accessed externally with bootstrap url `localhost:31092` from host machine.

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

<img width="1000" height="335" alt="image" src="https://github.com/user-attachments/assets/00988de3-8099-463b-bfd9-83bdfd7657c5" />

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

<img width="1000" height="134" alt="image" src="https://github.com/user-attachments/assets/5992cee8-f4a3-4f8d-bcff-ec39b9268390" />


### Install AKHQ

The AKHQ credentials secret can be created with below command. To generate the Password value use `echo -n "admin" | sha256sum`

```sh
kubectl -n demo create secret generic akhq-secret \
--from-literal=AKHQ_ADMIN_USER=admin \
--from-literal=AKHQ_ADMIN_PASSWORD=8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918 \
--from-literal=AKHQ_READ_USER=user \
--from-literal=AKHQ_READ_PASSWORD=04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb
```

The configuration for AKHQ configuration deployed is shown below. 
- The AKHQ installed with admin and reader roles for managing the topics in the cluster, we create the user credentials as secret and mount it as environment.
- The AKHQ is deployed in the same namespace as kafka cluster, so the bootstrap.servers url is configured with the kafka external bootstrap url which could be found from the service deployed part of the kafka cluster.

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

The AKHQ application can be accessed from browser with `http://localhost:31080`, should look like.

<img width="2024" height="1683" alt="image" src="https://github.com/user-attachments/assets/c6a31cb7-2ef6-4b47-bed2-50eb6076008c" />


### Install Apisix in de-coupled mode with self-signed certificate

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

Create an Issuer and Certificate to configure with the Apisix control-plane embedded admin UI dashboard. The Issuer and the Certificate should be deployed in the same namespace where the apisix is deployed in this case apisix namespace.

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

To check the status of deployed resources, use command `kubectl -n apisix get certificate`. The deployed resource should show True in the READY state. 

Install the Apisix deployment with de-coupled mode we use the control-plane and data-plane. 

To install the control-plane use below helm command
 - The ca cert secret created by the certficate manager should be configured in the `apisix.ssl.existingCASecret` property else we would not be able to use https from browser.
 - The `apisix.ssl.ccertCAFilename` is set to ca.crt which is key name of the certificate in the secret.

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

Save the yaml manifest to apisix-dashboard-route.yaml, apply to cluster using command `kubectl -n apisix apply -f apisix-dashboard-route.yaml`

INFO: Update the windows hosts file with `127.0.0.1 apisix.demo.com`, with this update we can access the Apisix dashboard with `https://apisix.demo.com/ui`

To check the status use `kubectl -n apisix get apisixtls` and `kubectl -n apisix get ar`

<img width="1000" height="1305" alt="image" src="https://github.com/user-attachments/assets/6fb59302-8424-4646-8cab-2c60b5a1bca9" />


### Deploy the Apicurio registry with Apicurio operator

Create the namespace apicurio-registry in which the apicurio-operator and registry will be installed. To create the namespace use the command `kubectl create ns apicurio-registry`.

To install the operator use below command, if there are new version refer the documentation to update it. The operator version v3 is used in this case.

```sh
VERSION=3.2.1;
NAMESPACE=apicurio-registry; 
curl -sSL "https://raw.githubusercontent.com/Apicurio/apicurio-registry/$VERSION/operator/install/install.yaml" \
 | sed "s/PLACEHOLDER_NAMESPACE/$NAMESPACE/g" \
 | kubectl -n $NAMESPACE apply -f -
```

The configuration to create a registry ui and backend. The configuration details can be found in [this link](https://www.apicur.io/registry/docs/apicurio-registry/3.2.x/getting-started/assembly-operator-config-reference.html#operator-ingress-reference_registry)

The environment variable configured fixes the CORS error when accessing the backend service from the ui via browser.

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

Save the apicurio configuration yaml to apicurio-configration.yaml, to apply to cluster use the command `kubectl -n apicurio-registry apply -f apicurio-configuration.yaml`

The configuration to access the Apicurio operator registry UI and app, ApisixTls and ApisixRoute needs to be created in apicurio-registry namespace.

Below configuration creates one Issuer for the apicurio-registry namespace, certificate request will use the same issuer for UI and App. Two ApisixTls is created for each ApisixRoute below is the configuration. 

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

Save the above manifest in apicurio-registry-route.yaml, to deploy use the command `kubectl -n apicurio-registry apply -f apicurio-registry-route.yaml`

To check the status use `kubectl -n apicurio-registry get apisixtls,ar`

INFO: Add host mapping entries to the windows hosts file like below. With the hosts entry updated the UI and App can be accessed using `https://apicurio.ui.demo.com` and `https://apicurio.app.demo.com`

```
127.0.0.1 apicurio.ui.demo.com
127.0.0.1 apicurio.ui.demo.com
```

With registry deployed below java code is sample on validating if the registry is accessible. The certificate can be obtained from the secret or browser. 
- Copy the ca.crt content of the secret `selfsigned-app-cert-secret` created by the cert manager configured in the Certificate request config above. Save the ca.crt content to a file caCert.crt and pass it to the RestClientOptions. - Export the certificate from browser hitting the certificate lock symbol, navigate to `https://apicurio.app.demo.com` in browser accept the SSL error from browser and navigate further. Save the certificate export to a file named browserCaCert.crt.

The code uses a boolean flag to determine which crt to use, both the ca certificate works.

```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS io.apicurio:apicurio-registry:3.2.1@pom
//DEPS io.apicurio:apicurio-registry-java-sdk
//DEPS io.apicurio:apicurio-registry-common
//DEPS org.slf4j:slf4j-jdk14

import io.apicurio.registry.client.common.DefaultVertxInstance;

import java.util.ArrayList;
import java.util.List;

import io.apicurio.registry.client.RegistryClientFactory;
import io.apicurio.registry.client.common.RegistryClientOptions;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.models.ArtifactMetaData;
import io.apicurio.registry.rest.client.models.ArtifactSearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleAvroRegistryAcces {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAvroRegistryAcces.class);

    private static final RegistryClient client;
    static {
        // Create a Service Registry client
        String registryUrl = "https://apicurio.app.demo.com/apis/registry/v3";
        client = createProperClient(registryUrl);
    }

    public static void main(String[] args) {
        // Register the JSON Schema schema in the Apicurio registry.
        // final String artifactId = "employee-info"; // UUID.randomUUID().toString();
        final String groupId = "default";

        try {
            // RegistryDemoUtil.createSchemaInServiceRegistry(client, artifactId,
            // Constants.SCHEMA);
            getSchemaFromRegistry(client, groupId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // If we do not provide our own instance of Vertx, then we must close the
            // default instance that will get used.
            DefaultVertxInstance.close();
        }
    }

    public static RegistryClient createProperClient(String registryUrl) {
        final String tokenEndpoint = System.getenv("AUTH_TOKEN_ENDPOINT");
        boolean dontExecute = false;
        if (tokenEndpoint != null && dontExecute) {
            final String authClient = System.getenv("AUTH_CLIENT_ID");
            final String authSecret = System.getenv("AUTH_CLIENT_SECRET");
            return RegistryClientFactory.create(RegistryClientOptions.create(registryUrl)
                    .oauth2(tokenEndpoint, authClient, authSecret));
        } else {
            RegistryClientOptions clientOptions = RegistryClientOptions.create(registryUrl);
            boolean useCaCertFromSecret = false;
            if (useCaCertFromSecret) {
                clientOptions.trustStorePem("./caCert.crt");
            } else {
                // else extract from the browser option
                clientOptions.trustStorePem("./browserCaCert.crt");
            }
            return RegistryClientFactory.create(clientOptions);
        }
    }

    public static ArtifactMetaData getSchemaFromRegistry(RegistryClient service, String groupId) {

        LOGGER.info("---------------------------------------------------------");
        LOGGER.info("=====> Fetching artifact from the registry for JSON Schema with ID: {}", groupId);
        try {
            List<ArtifactMetaData> metaData = new ArrayList<>();
            ArtifactSearchResults result = service.groups().byGroupId(groupId).artifacts().get();
            result.getArtifacts().forEach(itm -> {
                LOGGER.info("ARTIFACT ID -----[{}] ", itm.getArtifactId());
                ArtifactMetaData metaDataInfo = service.groups().byGroupId(groupId).artifacts()
                        .byArtifactId(itm.getArtifactId()).get();
                assert metaDataInfo != null;
                metaData.add(metaDataInfo);
            });

            LOGGER.info("=====> Successfully fetched JSON Schema artifact in Service Registry: {}",
                    metaData.size() > 0 ? metaData.get(0) : null);
            LOGGER.info("---------------------------------------------------------");
            return metaData.size() > 0 ? metaData.get(0) : null;
        } catch (Exception t) {
            throw t;
        }
    }
}
```

Save the file to SimpleAvroRegistryAccess.java, then to run we can use `jbang SimpleAvroRegistryAccess.java`. The output of the above java code would look like below 

<img width="2389" height="622" alt="image" src="https://github.com/user-attachments/assets/dd693eb1-ad8d-40ce-bad7-fdb7dcac9814" />

### Example Kafka consumer and producer

#### Example Kafka consumer and producer with simple message

Below is the Java Jbang example with Producer and Consumer where a random message triggered with Camel routes.

- producer
 
```java
///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import java.util.Random;

import static java.lang.System.out;
import static java.lang.System.setProperty;

class DemoPublisher{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        Random random = new Random();
        Main main = new Main();

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("timer:Msg?period=60000")
               .process(exchange -> exchange.getIn().setBody(random.nextInt(1000)))
               .setBody(simple("msg from publisher = ${body}"))
               .to("kafka:test-topic-1?brokers=localhost:31092")
               .to("stream:out");;
            }
         });
        main.run();
    }
}
```

- consumer

```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.kafka:kafka-clients:4.2.0
//-- //nop dependency to skip log
//-- //DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

import static java.lang.System.out;
import static java.lang.System.setProperty;

class DemoConsumer{

    public static void main(String ... args) throws Exception{
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");

        Main main = new Main();

        main.configure().addRoutesBuilder(new RouteBuilder(){
            public void configure() throws Exception{
                from("kafka:test-topic-1?brokers=localhost:31092&"+
                "maxPollRecords=1000&consumersCount=1&seekTo=BEGINNING&"+
                "groupId=kafkaGroup")
               .log("consumed: ${body}")
               .to("stream:out");
            }
         });
        main.run();
    }
}
```

Save the Java class with the class name like `DemoConsumer.java` asnd `DemoPublisher.java`. Open different cmd terminal, and run below command to execute the consumer and producer.

```
jbang DemoConsumer.java
jbang DemoPublisher.java
```

<img width="1862" height="1515" alt="image" src="https://github.com/user-attachments/assets/2b987cf9-14e0-405f-9fb5-7753c8f7b548" />

#### Example code with dynamic Avro Schema

- Producer

```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
// -- below nop dependency to skip the logs
//-- //DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS org.apache.avro:avro-compiler:1.12.1
//DEPS org.apache.avro:avro-maven-plugin:1.12.1
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.1

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.generic.GenericDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
//---don't use below format 
// ---import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.camel.dataformat.avro.AvroDataFormat;

import java.util.List;
import java.util.ArrayList;

import static java.lang.System.out;
import static java.lang.System.setProperty;;

class AvroProducer {

    public static void main(String... args) throws Exception {
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        setProperty("org.slf4j.simpleLogger.log.org.apache.camel", "info");

        Main main = new Main();

        List<String> inputString = new ArrayList<>();

        String str1 = """
                {"name":"User1","address":{"string":"11,somewhere,country"}}
                """;
        String str2 = """
                {"name":"User2","address":{"string":"12,somewhere,country"}}
                """;
        String str3 = """
                {"name":"User3","address":{"string":"13,somewhere,country"}}
                """;
        String str4 = """
                {"name":"User4","address":{"string":"15,somewhere,country"}}
                """;
        inputString.add(str1);
        inputString.add(str2);
        inputString.add(str3);
        inputString.add(str4);

        AvroProducer producer = new AvroProducer();
        Schema schema = producer.getSchema();

        GenericRecord record1 = producer.getRecord(schema, "User1", "11,somewhere,country");
        GenericRecord record2 = producer.getRecord(schema, "User2", "12,somewhere,country");
        GenericRecord record3 = producer.getRecord(schema, "User3", "13,somewhere,country");
        GenericRecord record4 = producer.getRecord(schema, "User4", "14,somewhere,country");
        GenericRecord record5 = producer.getRecord(schema, "User5", "15,somewhere,country");

        List<GenericRecord> inputRecords = new ArrayList<>();

        inputRecords.add(record1);
        inputRecords.add(record2);
        inputRecords.add(record3);
        inputRecords.add(record4);
        inputRecords.add(record5);

        List<ByteArrayOutputStream> inputJsonStream = new ArrayList<>();
        List<byte[]> inputJsonByteArray = new ArrayList<>();

        ByteArrayOutputStream stream1 = producer.getByteStream(schema, record1, false, true);
        ByteArrayOutputStream stream2 = producer.getByteStream(schema, record2, false, true);
        ByteArrayOutputStream stream3 = producer.getByteStream(schema, record3, false, true);
        ByteArrayOutputStream stream4 = producer.getByteStream(schema, record4, false, true);
        ByteArrayOutputStream stream5 = producer.getByteStream(schema, record5, false, true);

        inputJsonStream.add(stream1);
        inputJsonStream.add(stream2);
        inputJsonStream.add(stream3);
        inputJsonStream.add(stream4);
        inputJsonStream.add(stream5);

        inputJsonByteArray.add(stream1.toByteArray());
        inputJsonByteArray.add(stream2.toByteArray());
        inputJsonByteArray.add(stream3.toByteArray());
        inputJsonByteArray.add(stream4.toByteArray());
        inputJsonByteArray.add(stream5.toByteArray());

        System.out.println("number of input " + inputJsonStream.size());

        System.out.println("print jsonstream values");
        inputJsonStream.forEach(System.out::println);
        System.out.println("print jsonbytearray values");
        inputJsonByteArray.forEach(System.out::println);

        ByteArrayOutputStream streamb1 = producer.getByteStream(schema, record1, false, true);
        ByteArrayOutputStream streamb2 = producer.getByteStream(schema, record2, false, true);
        ByteArrayOutputStream streamb3 = producer.getByteStream(schema, record3, false, true);
        ByteArrayOutputStream streamb4 = producer.getByteStream(schema, record4, false, true);
        ByteArrayOutputStream streamb5 = producer.getByteStream(schema, record5, false, true);

        List<ByteArrayOutputStream> inputBinaryStream = new ArrayList<>();
        inputBinaryStream.add(streamb1);
        inputBinaryStream.add(streamb2);
        inputBinaryStream.add(streamb3);
        inputBinaryStream.add(streamb4);
        inputBinaryStream.add(streamb5);

        AvroDataFormat avroDataFormat = new AvroDataFormat(producer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());

        main.configure().addRoutesBuilder(new RouteBuilder() {
            public void configure() throws Exception {

                onException(Exception.class)
                        .process(exchange -> {
                            Exception cause = exchange.getException();
                            cause.printStackTrace();
                            System.out.println("---x[Exception occurred]x---");
                        })
                        .handled(true)
                        .stop();

                from("direct:start")
                        .split(body())
                        .process(exchange -> {
                            System.out.println("unmarshall data:- " + exchange.getIn().getBody());
                        })
                        .log("publishing msg: ${body}")
                        .marshal(avroDataFormat)
                        .to("kafka:test-topic-1?brokers=localhost:31092"
                                + "&valueSerializer=org.apache.kafka.common.serialization.ByteArraySerializer")
                        .to("stream:out");
            }
        });
        main.start();
        // create producer template
        ProducerTemplate template = main.getCamelContext().createProducerTemplate();

        template.sendBody("direct:start", inputRecords);

        Thread.sleep(5000);
        main.stop();
    }

    public Schema getSchema() {
        String userSchema = """
                {"type": "record",
                "name": "employee",
                "fields":[
                    {"name": "name","type":"string"},
                    {"name": "address", "type": ["string", "null"]}
                    ]}
                """;
        return new Schema.Parser().parse(userSchema);
    }

    public GenericRecord getRecord(Schema schema, String name, String address) {

        GenericRecord record = new GenericData.Record(schema);
        record.put("name", name);
        record.put("address", address);
        return record;
    }

    public ByteArrayOutputStream getByteStream(Schema schema, GenericRecord record, boolean binaryEncoder,
            boolean jsonEncoder) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());

        try {
            if (binaryEncoder) {
                BinaryEncoder binaryAvroEncoder = EncoderFactory.get().binaryEncoder(output, null);
                writer.write(record, binaryAvroEncoder);
                binaryAvroEncoder.flush();
            }
            if (jsonEncoder) {
                JsonEncoder jsonAvroEncoder = EncoderFactory.get().jsonEncoder(record.getSchema(), output);
                writer.write(record, jsonAvroEncoder);
                jsonAvroEncoder.flush();
            }
        } catch (IOException e) {
            System.err.println("Serialization error:" + e.getMessage());
        }
        return output;
    }
}
```

- consumer

```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.apache.kafka:kafka-clients:4.2.0
//-- below nop dependency can be used to skip log
//--/ / DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

// ------ not use this import org.apache.camel.model.dataformat.AvroDataFormat;
import org.apache.camel.dataformat.avro.AvroDataFormat;

import static java.lang.System.out;
import static java.lang.System.setProperty;;

class AvroConsumer {

    public static void main(String... args) throws Exception {
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        setProperty("org.slf4j.simpleLogger.log.org.apache.camel", "info");

        Main main = new Main();

        AvroConsumer consumer = new AvroConsumer();
        AvroDataFormat avroDataFormat = new AvroDataFormat(consumer.getSchema());
        avroDataFormat.setInstanceClassName(GenericData.Record.class.getName());

        main.configure().addRoutesBuilder(new RouteBuilder() {
            public void configure() throws Exception {
                from("kafka:test-topic-1?brokers=localhost:31092&"
                        + "maxPollRecords=1000&consumersCount=1&seekTo=BEGINNING")
                        .unmarshal(avroDataFormat)
                        // process to print the message from exchange
                        // .process(exchange -> System.out.println("inside the
                        // consumer"+exchange.getIn().getBody()))
                        .log("consumed unmarshalled message: ${body}")
                        .to("stream:out");
            }
        });
        main.start();
        Thread.sleep(10000);
        main.stop();
    }

    public Schema getSchema() {
        String userSchema = """
                {"type": "record",
                "name": "employee",
                "fields":[
                    {"name": "name","type":"string"},
                    {"name": "address", "type": ["string", "null"]}
                    ]}
                """;

        return new Schema.Parser().parse(userSchema);
    }
}
```

The producer and consumer example above is saved as `AvroProducer.java` and `AvroConsumer.java`. To run the example code open different cmd prompt and execute below commands.

```
jbang AvroConsumer.java
jbang AvroProducer.java
```

The output snapshot, the top section is consumer, make sure the topics messages are removed.

<img width="2821" height="1577" alt="image" src="https://github.com/user-attachments/assets/5e07e984-3604-4b4d-8d7e-51731b8141d6" />

#### Example Java producer and consumer code with the Apicurio registry 

- producer

```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
// To skip the logs use nop dependency
// - / / DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS org.apache.avro:avro-compiler:1.12.1
//DEPS org.apache.avro:avro-maven-plugin:1.12.1
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.1

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;

import static java.lang.System.out;
import static java.lang.System.setProperty;;

class RegistryAvroProducer {

        public static void main(String... args) throws Exception {
                out.println("Starting camel route...");
                setProperty("org.slf4j.simpleLogger.logFile", "System.out");

                Main main = new Main();

                // List of input employee info as string
                // The schema will be fetched from apicurio registry
                List<String> inputString = new ArrayList<>();

                String str1 = """
                                {"name":"User1","address":{"string":"11,somewhere,country"}}
                                """;
                String str2 = """
                                {"name":"User2","address":{"string":"12,somewhere,country"}}
                                """;
                String str3 = """
                                {"name":"User3","address":{"string":"13,somewhere,country"}}
                                """;
                String str4 = """
                                {"name":"User4","address":{"string":"15,somewhere,country"}}
                                   """;
                inputString.add(str1);
                inputString.add(str2);
                inputString.add(str3);
                inputString.add(str4);

                // the ca.crt content from the envrionemnt the secret
                // generated by the cert manager in the cluster
                String caCert = """
                  // CA.CRT CONTENT FROM THE SAMPLE APP REGISTRY SECRET CREATED BY CERT MANAGER
                  // OR THE CERTIFACTE CONTENT EXPORTED FROM THE BROWSER ACCESSING
                  // https://apicurio.app.demo.com URL
                        """;

                // Convert the ca cert string to file and place in temp directory
                Path caPath = Files.writeString(Files.createTempFile("ca", ".pem"), caCert);

                System.out.println("Temp path of the created certificate - " + caPath.toAbsolutePath());

                main.configure().addRoutesBuilder(new RouteBuilder() {
                   public void configure() throws Exception {

                        onException(Exception.class)
                        .process(exchange -> {
                            Exception cause = exchange.getException();
                            cause.printStackTrace();
                            System.out.println("---x[Exception occurred]x---");
                         })
                        .handled(true)
                        .stop();

                        from("direct:start")
                        .split(body())
                        .log("publishing msg: ${body}")
                        // check for ssl config-
                        // https://github.com/Apicurio/apicurio-registry/blob/main/schema-resolver/src/main/java/io/apicurio/registry/resolver/config/SchemaResolverConfig.java
                        .to("kafka:demo-topic-1?brokers=localhost:31092"
                           + "&valueSerializer=io.apicurio.registry.serde.avro.AvroKafkaSerializer"
                           + "&additionalProperties[apicurio.registry.url]=https://apicurio.app.demo.com/apis/registry/v3"
                           + "&additionalProperties[apicurio.registry.auto-register]=true"
                           + "&additionalProperties[apicurio.registry.tls.truststore.type]=PEM"
                           + "&additionalProperties[apicurio.registry.tls.truststore.location]="
                           + caPath.toAbsolutePath())
                        .to("stream:out");
                   }
                });

                // To continously produce message use run method
                // main.run();

                main.start();
                // create producer template
                ProducerTemplate template = main.getCamelContext().createProducerTemplate();

                template.sendBody("direct:start", inputString);

                Thread.sleep(5000);
                main.stop();
        }
}
```

- consumer

```java
///usr/bin/env jbang "$0" "$0" : exit $?
//DEPS org.apache.camel:camel-bom:4.18.1@pom
//DEPS org.apache.camel:camel-core
//DEPS org.apache.camel:camel-main
//DEPS org.apache.camel:camel-stream
//DEPS org.apache.camel:camel-kafka
//DEPS org.apache.camel:camel-jackson-avro
//DEPS org.apache.camel:camel-avro
//DEPS org.apache.kafka:kafka-clients:4.2.0
// To skip logs from printing use the nop dependency
// -/ / DEPS org.slf4j:slf4j-nop:2.0.13
//DEPS org.slf4j:slf4j-simple:2.0.13
//DEPS org.slf4j:slf4j-api:2.0.13
//DEPS org.apache.avro:avro:1.12.1
//DEPS io.apicurio:apicurio-registry-avro-serde-kafka:3.2.2

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.camel.builder.*;
import org.apache.camel.main.*;
import static java.lang.System.*;

class RegistryAvroConsumer {

    public static void main(String... args) throws Exception {
        out.println("Starting camel route...");
        setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        setProperty("org.slf4j.simpleLogger.log.org.apache.camel", "info");

        Main main = new Main();

        // the ca.crt content from the envrionemnt the secret
        // generated by the cert manager in the cluster
        String caCert = """
                // CA.CRT CONTENT FROM THE SAMPLE APP REGISTRY SECRET CREATED BY CERT MANAGER
                // OR THE CERTIFACTE CONTENT EXPORTED FROM THE BROWSER ACCESSING
                // https://apicurio.app.demo.com URL
                """;

        Path caPath = Files.writeString(Files.createTempFile("ca", ".pem"), caCert);
        System.out.println("certificate path " + caPath.toAbsolutePath());

        main.configure().addRoutesBuilder(new RouteBuilder() {
            public void configure() throws Exception {

                onException(Exception.class)
                .process(exchange -> {
                    Exception cause = exchange.getException();
                    System.out.println(cause.getMessage());
                    cause.printStackTrace();
                    System.out.println("--x[Exception occurred]x--");
                })
                .handled(true)
                .stop();

                from("kafka:demo-topic-1?brokers=localhost:31092"
                    + "&maxPollRecords=1000&consumersCount=1"
                    + "&seekTo=BEGINNING"
                    + "&groupId=kafka-group-id"
                    + "&valueDeserializer=io.apicurio.registry.serde.avro.AvroKafkaDeserializer"
                    + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
                    + "&additionalProperties[apicurio.registry.url]=https://apicurio.app.demo.com/apis/registry/v3"
                    + "&additionalProperties[apicurio.registry.auto-register]=true"
                    + "&additionalProperties[apicurio.registry.tls.truststore.type]=PEM"
                    + "&additionalProperties[apicurio.registry.tls.truststore.location]=" + caPath.toAbsolutePath())
                    .log("Consumed Avro Message: ${body}")
                    .to("stream:out");
            }
        });
        // with run continously polls
        // main.run();
        main.start();
        Thread.sleep(10000);
        main.stop();
    }
}
```

Make sure the topics is empty, to avoid consumers throwing exception with non Avro serialized messages in topic. To run the producer and consumer code, use beliow command.

```
jbang RegistryAvroProducer.java
jbang RegistryAvroConsumer.java
```

The output snapshot below, the top section produces messages and bottom section consumes the messages 

<img width="2818" height="1576" alt="image" src="https://github.com/user-attachments/assets/2690f9b5-8a40-4bf9-8a92-71f943fdb994" />

Snapshot of AKHQ topic were the producer sent the serialize message
<img width="2000" height="1069" alt="image" src="https://github.com/user-attachments/assets/a09160bb-7bbf-4873-9b0a-e00c8acb7f03" />

Snapshot of Apicurio UI we could see the artifact created automatically by the producer
<img width="2009" height="1685" alt="image" src="https://github.com/user-attachments/assets/9d538227-7689-4743-8ddd-03c9e7d62936" />

<img width="1949" height="1457" alt="image" src="https://github.com/user-attachments/assets/1b1465a4-2704-4070-a3eb-fd00c765dead" />

