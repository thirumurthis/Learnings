#### Configuring the postgres in multi-node multi-cluster in kind with active standby architecture 

Configure the service discovery using cilium cluster mesh

Command of execution to configure kind node in clium cluster mesh 


-- for manifest navigate to below file path 
cd /mnt/c/thiru/edu/tmp/cilium_service_mesh/

# STEP1 - deploy in kind 1 cluster 

-content of kind dev0 cluster
```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
networking:
   disableDefaultCNI: true
   podSubnet: "10.1.0.0/16"
   serviceSubnet: "10.11.0.0/16"
   kubeProxyMode: "none"
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 31081
        hostPort: 8081
        protocol: TCP
      - containerPort: 31443
        hostPort: 8443
        protocol: TCP
      - containerPort: 31235
        hostPort: 8085
        protocol: TCP
  - role: worker
  - role: worker
  - role: worker
```

- content of kind dev1 cluster

```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
networking:
   disableDefaultCNI: true
   podSubnet: "10.2.0.0/16"
   serviceSubnet: "10.12.0.0/16"
   kubeProxyMode: "none"
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 31082
        hostPort: 8082
        protocol: TCP
      - containerPort: 31444
        hostPort: 8444
        protocol: TCP
      - containerPort: 31235
        hostPort: 8086
        protocol: TCP
  - role: worker
  - role: worker
  - role: worker
```

```sh
kind create cluster --name cluster-dev0 --config kind_cluster_dev0.yaml
kind create cluster --name cluster-dev1 --config kind_cluster_dev1.yaml 
```
--install in cluster1 

```sh
CLUSTER1=kind-cluster-dev0
CLUSTER2=kind-cluster-dev1
```

# STEP 2
-- use below helm command to configure it .. some of these parameter already overridden in override values file
-- some config enables metrics 

- clusters.yaml (common configuration)
- use the docker network inspect and pick the ip's below

```yaml
clustermesh:
  config:
    clusters:
      kind-cluster-dev0:
        enabled: true
        address: cluster-dev0-control-plane # docker control plane name 
        port: 32379                         # any unused port for clustermeshapi communication 
        #ips:
        # - 172.18.0.5
        # - 172.18.0.4
        # - 172.18.0.3
        # - 172.18.0.2
      kind-cluster-dev1:
        enabled: true
        address: cluster-dev1-control-plane
        port: 32380
        #ips:
        # - 172.18.0.8
        # - 172.18.0.6
        # - 172.18.0.7
        # - 172.18.0.9

gatewayAPI:
   enabled: true
   hostNetwork:
    enabled: false  # if set to true then the L7 will not work i.e only Httproute and httpsroute will work Tcp route doesn't work
   
   # below can be used in case if we different gatway class to use
   #gatewayClass:
   #  create: true

kubeProxyReplacement: true

l7Proxy: true

envoy:
  enabled: true
  securityContext:
    capabilities:
      keepCapNetBindService: true
      envoy:
      # Add NET_BIND_SERVICE to the list (keep the others!)
      - NET_BIND_SERVICE
      - NET_ADMIN
      - SYS_ADMIN
      - BPF

# Mandated for Kind environments so Cilium attaches to the 
# nested container cgroup layout properly
cgroup:
  autoMount: 
    enabled: true
  hostRoot: /sys/fs/cgroup

ipam:
  mode: kubernetes

# check https://docs.cilium.io/en/stable/observability/hubble/setup/#hubble for helm
hubble:
  relay: 
    enabled: true
  metrics:
    enabled: true
    enableOpenMetrics: true
  ui:
    enabled: true
    #baseUrl: "/hubble"
    #service:
      # --- The type of service used for Hubble UI access, either ClusterIP or NodePort.
      #type: NodePort
      # --- The port to use when the service type is set to NodePort.
      #nodePort: 31235

operator:
  prometheus:
     enabled: true 
     port: 6942
```

cluster-1.yaml

```yaml
cluster:
  name: kind-cluster-dev0
  id: 1

clustermesh:
  useAPIServer: true

  config:
    enabled: true

  apiserver:
    service:
      #type: LoadBalancer
      type: NodePort           # clusterMesh Api in this case exposed using NodePort 
      nodePort: 32379          # all cilium pods from other cluster will reach to this endpoting based on the settings in clustermesh.config.clusters[].address/port
      annotations: {}
      # The following annotations are examples. Adapt them to your
      # environment and context.
      #
      # Optional: Have ExternalDNS create the DNS records to
      # reach this API server. Otherwise, create the same record
      # through your usual DNS management workflow.
      # annotations:
      #   external-dns.alpha.kubernetes.io/hostname: cluster1.example.com
      #
      # AKS:
      # annotations:
      #   service.beta.kubernetes.io/azure-load-balancer-internal: "true"
      #
      # EKS:
      # annotations:
      #   service.beta.kubernetes.io/aws-load-balancer-scheme: internal
      #
      # GKE:
      # annotations:
      #   networking.gke.io/load-balancer-type: Internal
      #   networking.gke.io/internal-load-balancer-allow-global-access: "true"
    tls:
      auto:
        enabled: true
        method: cronJob
        server:
          extraDnsNames:
            - cluster-dev0.example.com
            - cluster-dev1.example.com

k8sServiceHost: cluster-dev0-control-plane
k8sServicePort: 6443


  # Optional Cluster Mesh features that you may find useful:
  # enableEndpointSliceSynchronization: true
  # mcsapi:
  #   enabled: true
  #   corednsAutoConfigure:
  #     enabled: true

#ingressController:
#    enabled: true
#    ingressController: shared #dedicated #shared  #dedicated other option
#    service:
#      type: NodePort
#      insecureNodePort: 31081
#      secureNodePort: 31443
```

- cluster-2 yaml

```yaml
cluster:
  name: kind-cluster-dev1
  id: 2

clustermesh:
  useAPIServer: true

  config:
    enabled: true

  apiserver:
    service:
      #type: LoadBalancer
      type: NodePort
      nodePort: 32380
      annotations: {}
      # The following annotations are examples. Adapt them to your
      # environment and context.
      #
      # Optional: Have ExternalDNS create the DNS records to
      # reach this API server. Otherwise, create the same record
      # through your usual DNS management workflow.
      # annotations:
      #   external-dns.alpha.kubernetes.io/hostname: cluster2.example.com
      #
      # AKS:
      # annotations:
      #   service.beta.kubernetes.io/azure-load-balancer-internal: "true"
      #
      # EKS:
      # annotations:
      #   service.beta.kubernetes.io/aws-load-balancer-scheme: internal
      #
      # GKE:
      # annotations:
      #   networking.gke.io/load-balancer-type: Internal
      #   networking.gke.io/internal-load-balancer-allow-global-access: "true"
    # below is required for the Apiserver cilium to start correctly
    tls:
      auto:
        enabled: true
        method: cronJob
        server:
          extraDnsNames:
            - cluster-dev0.example.com
            - cluster-dev1.example.com

k8sServiceHost: cluster-dev1-control-plane
k8sServicePort: 6443


  # Optional Cluster Mesh features that you may find useful:
  # enableEndpointSliceSynchronization: true
  # mcsapi:
  #   enabled: true
  #   corednsAutoConfigure:
  #     enabled: true

#ingressController:
#    enabled: true
#    ingressController: shared #dedicated #shared  #dedicated other option
#    service:
#      type: NodePort
#      insecureNodePort: 31082
#      secureNodePort: 31444
```

```sh
helm upgrade -i cilium oci://quay.io/cilium/charts/cilium --version 1.20.2 \
   --namespace kube-system \
   --set prometheus.enabled=true \
   --set operator.prometheus.enabled=true \
   --set hubble.enabled=true \
   --set hubble.metrics.enabled="{dns,drop,tcp,flow,port-distribution,icmp,http}" \
   --kube-context $CLUSTER1   -f ./from_doc/clusters.yaml -f ./from_doc/cluster1.yaml
```
#STEP3 
-- copy the certificate from one cluster to another (this can be done by ESO) here done manually 

```sh
kubectl --context $CLUSTER1 get secret -n kube-system cilium-ca -o yaml | \
    kubectl --context $CLUSTER2 create -f -
```
#STEP4

--helm installation in the cluster 2 
```sh
helm upgrade -i cilium oci://quay.io/cilium/charts/cilium --version 1.20.2 \
   --namespace kube-system \
   --set prometheus.enabled=true \
  --set operator.prometheus.enabled=true \
  --set hubble.enabled=true \
   --set hubble.metrics.enabled="{dns,drop,tcp,flow,port-distribution,icmp,http}" \
   --kube-context $CLUSTER2  -f ./from_doc/clusters.yaml -f ./from_doc/cluster2.yaml
 ```  

## Simple troubleshoot step

-- in case of certificate error on the hubbler use  check the output 
```sh
kubectl --context=$CLUSTER2 -n kube-system get secret hubble-server-certs -o jsonpath='{.data.tls\.crt}' | base64 -d | openssl x509 -text -noout | grep DNS
```

# STEP 5

-- To configure the GatewayAPI we deploy the CRDS with direct manifest, this can be done using helm as well
-- need to deploy since supports httproute, etc
```sh
kubectl --context $CLUSTER1 apply --server-side -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.6.1/standard-install.yaml
```
```sh
kubectl --context $CLUSTER2 apply --server-side -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.6.1/standard-install.yaml
```
# STEP 6 

-- Deploy the obesevability prometheus and grfana

-- only uses this to deploy everything -  cilium includes latest image versions
-- and route related config overrides 
```sh
kubectl apply -f /mnt/c/thiru/edu/tmp/cilium_service_mesh/monitoring-example.yaml --context $CLUSTER1
```
-- only use this to deploy the prmoetheus and grafana

```sh
kubectl apply -f monitoring-example.yaml --context $CLUSTER2
```
-- annotate the service for discovering each (optional)
```sh
kubectl annotate -n cilium-monitoring svc/grafana  service.cilium.io/global="true" --context $CLUSTER1
kubectl annotate -n cilium-monitoring svc/prometheus  service.cilium.io/global="true" --context $CLUSTER1

kubectl annotate -n cilium-monitoring svc/grafana  service.cilium.io/global="true" --context $CLUSTER2
kubectl annotate -n cilium-monitoring svc/prometheus  service.cilium.io/global="true" --context $CLUSTER2
```

# STEP 7 

-- create gateway routing configuration 

-- deploys in the cilium-monitoring namespace 

```yaml
# get from: https://github.com/cilium/cilium/blob/379936f5c8f95ae82b08718880624bbc34f58798/examples/kubernetes/gateway/gateway-with-parameters.yaml
---
apiVersion: gateway.networking.k8s.io/v1
kind: GatewayClass
metadata:
  name: nodeport-gateway-class
spec:
  controllerName: io.cilium/gateway-controller
  description: The default Cilium GatewayClass
  parametersRef:
    group: cilium.io
    kind: CiliumGatewayClassConfig
    name: nodeport-gateway-config
    namespace: kube-system
---
apiVersion: cilium.io/v2alpha1
kind: CiliumGatewayClassConfig
metadata:
  name: nodeport-gateway-config
  namespace: kube-system
spec:
  service:
    type: NodePort
---
```

-- route grafana and prometheus

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: nodeport-gateway
  namespace: cilium-monitoring
spec:
  gatewayClassName: nodeport-gateway-class
  listeners:
  - protocol: TCP # HTTP - doesn't support the postgres tcproute from host so changed to TCP from HTTP
    port: 31081
    name: web-gw
    allowedRoutes:
      namespaces:
        from: All #Same
---
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: http-grafana-route
  namespace: cilium-monitoring
spec:
  parentRefs:
  - name: nodeport-gateway
    namespace: cilium-monitoring
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /grafana
    backendRefs:
    - name: grafana
      port: 3000
---
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: http-prometheus-route
  namespace: cilium-monitoring
spec:
  parentRefs:
  - name: nodeport-gateway
    namespace: cilium-monitoring
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /prometheus
    backendRefs:
    - name: prometheus
      port: 9090
```

-- below is for hubble ui

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: http-hubble-route
  namespace: kube-system
spec:
  parentRefs:
  - name: nodeport-gateway
    namespace: cilium-monitoring
  hostnames:
  - "hubble.local" # <--- Bind to a dedicated local domain Add to hosts 127.0.0.1 hubble.local from browser user http://hubble.local:8081
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /      # <--- Keep it at root so NGINX matches perfectly!
    backendRefs:
    - name: hubble-ui
      port: 80
```

After deploying the route check using the browser http://localhost:8081/grafana and http://localhost:8081/prometheus

### TESTING SERVICE DISCOVER
## To test the service discovery with the cilium use below steps 

-- testing
```sh
kubectl --context $CLUSTER1 apply -f global-test-service-dev0.yaml 
kubectl --context $CLUSTER2 apply -f global-test-service-dev1.yaml 

kubectl --context $CLUSTER2 run backend-pod --image=nginx:alpine --labels="app=backend"

kubectl --context $CLUSTER2 exec backend-pod -- sh -c 'echo "hello from cluster2-dev1 Network" > /usr/share/nginx/html/index.html'

kubectl --context $CLUSTER2 exec backend-app -- sh -c 'echo "hello from cluster2-dev1 Network" > /usr/share/nginx/html/index.html'
```
-- from dev0  should see the response give half a second for apps to be configured
```sh
kubectl --context $CLUSTER1 run test-client --rm -it --image=curlimages/curl --restart=Never -- curl -s http://multi-cluster-service
```

### TESTING CILIUM CLUSTER STATUS 

-- exec in to the alpine node, and install kubectl, cilium cli, should access the docker and kind from the container 
```sh
docker run -it --rm -v ${HOME}:/root -v ${PWD}:/work -w /work --net host alpine sh

apk add --no-cache curl 
apk add --no-cache bash

curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

chmod +x ./kubectl
install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

CILIUM_CLI_VERSION=$(curl -s https://raw.githubusercontent.com/cilium/cilium-cli/main/stable.txt)
CLI_ARCH=amd64
if [ "$(uname -m)" = "aarch64" ]; then CLI_ARCH=arm64; fi
curl -L --fail --remote-name-all https://github.com/cilium/cilium-cli/releases/download/${CILIUM_CLI_VERSION}/cilium-linux-${CLI_ARCH}.tar.gz{,.sha256sum}
sha256sum --check cilium-linux-${CLI_ARCH}.tar.gz.sha256sum
tar -xzf cilium-linux-${CLI_ARCH}.tar.gz 
mv cilium /usr/local/bin
rm cilium-linux-${CLI_ARCH}.tar.gz cilium-linux-${CLI_ARCH}.tar.gz.sha256sum

CLUSTER1=kind-cluster-dev0
CLUSTER2=kind-cluster-dev1 

cilium clustermesh status --context $CLUSTER1 --wait
cilium clustermesh status --context $CLUSTER2 --wait

cilium status --context $CLUSTER1
cilium status --context $CLUSTER2

cilium clustermesh status --context $CLUSTER1
cilium clustermesh status --context $CLUSTER2
```

-------#####---------

### POSTGRES DEPLOYMENT

STEP 8 
```sh
CLUSTER1=kind-cluster-dev0
CLUSTER2=kind-cluster-dev1 
```

# create namespace 
```sh
kubectl --context $CLUSTER1 create ns zalando
kubectl --context $CLUSTER1 create ns postgres
```
# install the operator on cluster 1
```sh
helm upgrade --install --kube-context $CLUSTER1 postgres-operator postgres-operator-charts/postgres-operator -n zalando --create-namespace
```
# create postgres db in the cluster 1 dev0 No need to update the hpa all the host and local are allowed and some are trust and some passowrd
content of file pg_cluster_dev0.yaml (primary cluster)

```yaml
apiVersion: "acid.zalan.do/v1"
kind: postgresql
metadata:
  name: postgres-cluster
  namespace: postgres
spec:
  teamId: "postgres"
  numberOfInstances: 3
  users:
    zalando:  # database owner
    - superuser
    - createdb
  databases:
    zalando: zalando  # dbname: owner
  postgresql:
    version: "18"
    parameters:
       wal_level: "replica"
       max_wal_senders: "10"
  
  patroni:
    pg_hba: 
      - local all         all                   trust   # 3. CRITICAL: Must be trust for internal Patroni tool health checks
      - host  all         all     127.0.0.1/32  trust
      - host replication  standby all           md5     # Open to any network, but strictly guarded by a password
      - host all          all     all           md5     # Open to any network, but strictly guarded by a password
  volume:
    size: 4Gi
```

```sh
kubectl --context $CLUSTER1 apply -f pg_cluster_dev0.yaml -n postgres
```

-- content of file pg_service0_svc.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-primary-global
  namespace: postgres
  annotations:
    service.cilium.io/affinity: local
    service.cilium.io/global: "true"
    service.cilium.io/shared: "true"
spec:
  ports:
  - name: postgres
    protocol: TCP
    port: 5432
    targetPort: 5432
  selector:
    application: spilo
    spilo-role: master
```

```sh
kubectl --context $CLUSTER1 apply -f pg_service0_svc.yaml
```

# in cluster-2 create namespace 
```sh
kubectl --context $CLUSTER2 create ns zalando
kubectl --context $CLUSTER2 create ns postgres
```

### copy the secret from primary to the secondary

```sh
kubectl --context=$CLUSTER1 get secret standby.postgres-cluster.credentials.postgresql.acid.zalan.do -n postgres -o yaml | \
  yq 'del(.metadata.creationTimestamp, .metadata.uid, .metadata.resourceVersion, .metadata.namespace, .metadata.annotations["kubectl.kubernetes.io/last-applied-configuration"])' | tee /dev/tty | \
  kubectl --context=$CLUSTER2 apply -n postgres -f -
```

### in cluster-2 dev1 install the postgres operator 

```sh
helm upgrade --install --kube-context $CLUSTER2 postgres-operator postgres-operator-charts/postgres-operator -n zalando --create-namespace
```

### content of file pg_cluster_dev1.yaml 
-- note, the standby_host is service discovery done by the cilium, if using kube-vip the patroni config for hba was not required but in case if cilium it is required

```yaml
apiVersion: "acid.zalan.do/v1"
kind: postgresql
metadata:
  name: postgres-cluster
spec:
  teamId: "postgres"
  volume:
    size: 4Gi
  numberOfInstances: 3
  postgresql:
    version: "18"
  standby:
    standby_host: "postgres-primary-global.postgres.svc.cluster.local"       # IP of primary postgres - kubevip loadbalancer of primary
    standby_port: "5432"             # NodePort of primary postgres
  patroni:
    pg_hba: 
      - local all         all                   trust   
      - host  all         all     127.0.0.1/32  trust
      - host replication  standby all           md5     
      - host all          all     all           md5   
```

### in cluster-2 dev1 deploy the postgres db 

```sh
kubectl --context $CLUSTER2 apply -f pg_cluster_dev1.yaml -n postgres
```

-- deploy the service discovery content of hte pg_service_dev1.yaml 
-- no selector and the annotations are addred affinity 

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-primary-global
  namespace: postgres
  annotations:
    service.cilium.io/affinity: remote
    service.cilium.io/global: "true"
    service.cilium.io/shared: "false"
spec:
  ports:
  - name: postgres
    protocol: TCP
    port: 5432
    targetPort: 5432

```

### apply when performing switch over in this case

```sh
kubectl --context $CLUSTER1 apply -f pg_service_dev1.yaml -n postgres
```

-- After deploying those configuration, to verify if the service is accesisble we can use below commands 

```sh
kubectl --context $CLUSTER1 run alpine-shell --rm -it --image=alpine --restart=Never -- sh
# nc -vz 172.18.100.10:5432
```

-- Also to check the cilium traffic flow, get the cilium and check the service list 
 
```sh
CILIUM_POD=$(kubectl get pods -n kube-system -l k8s-app=cilium --context $CLUSTER2 -o jsonpath='{.items[0].metadata.name}')
kubectl exec -n kube-system $CILIUM_POD --context $CLUSTER2 -c cilium-agent -- cilium service list | grep 5432
```
-- output looks like bleow 

```
22   10.12.43.180:5432/TCP    ClusterIP      1 => 10.1.1.200:5432/TCP (active)
23   10.12.122.42:5432/TCP    ClusterIP
24   10.12.77.33:5432/TCP     ClusterIP
```

-- to switch over with kubectl use below we need to remove the standby config from the CLUSTER2 first 
-- then in CLUSTER1 we need to add the standby configuration
-- before to check the content use 
```
kubectl --context $CLUSTER1 get postgresql postgres-cluster -n postgres -oyaml
```

### TO PERFORM SWITCH OVER FOR TESTING
-- below patch applied without any error  

```sh
kubectl --context $CLUSTER2 patch postgresql postgres-cluster -n postgres --type=merge -p '{"spec":{"standby": null}}'
```
--after patch use to check the content of the configuration 
```sh
kubectl --context $CLUSTER1 get postgresql postgres-cluster -n postgres -oyaml
```

```sh
kubectl --context $CLUSTER1 patch postgresql postgres-cluster -n postgres --type='merge' -p '{"spec":{"standby":{"standby_host":"postgres-primary-global.postgres.svc.cluster.local","standby_port":"5432"}}}'
```

we can use kubevip since this provides a load balancer for control plane and that will handle the traffic. in cilium we use service discuvery with dns

use the python program to sync with the API endpoint 

To verify if the postgres is performing replication streaming use `patronictl list` to check it .


To Do create a sample spring app with a simple backend and configure backend with the following configuration.
