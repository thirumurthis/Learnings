## Istio with Kuberentes Gateway API

- In this blog have detailed deploying Istio in Kind cluster and configure Kuberenetes Gateway API.

### Pre-requsites:
 - Docker desktop installed and running
 - Kind CLI
 - Helm CLI (v3.15.3+)
 - Understanding of Service Mesh (Isio basics and Gateway API)

### Istio with Kubernetes Gateway API

- From Istio documentation the recommends to use Kuberentes Gateway API since the release of version v1.1. 
- In the Istio Ingress Gateway `VirtualService` is created for routing, with Kuberentes Gateway API to route the `HTTPRoute` is created that  configures the service of the app.

 - To configure Kuberentes Gateway API in the cluster the CRD should be deployed in Kind cluster. The manifest for the CRD used in this example is `https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml`.

### Summary of components installed and deployed

- Creating a kind cluster with configuration exposing few ports to access the apps
- Deploy Istio using helm charts (base and istiod) with basic configuration (not production ready)
- Deploy Prometheus community chart includes Prometheus, Grafana and AlertManager in monitor namespaces (not production ready)
- Deploy the Kuberentes Gateway API CRD in cluster
- Deploy a simple Nginx (backend-app) configured to serve simple response
- Deploy Kiali (Istio Dashboard) configured to use the external services of Promethus and Grafana deployed on monitor namespace
- Create Gateway resoruce and configure HTTPRoutes to access the kiali and Nginx backend apps

> Note
>  - The Istio helm charts are downloaded to local machine and then deployed.
>  - It can also be deployed directly once the repo is added to helm CLI.

Represenation of the app deployed in the Kind cluster

![image](https://github.com/user-attachments/assets/30b72d2e-e316-4dda-b518-8261a284eeb2)

### Kind Cluster creation

- Create Kind cluster named istio-dev with below configuration
- Few ports exposed in the configuration
  - hostPort 9001 and 9002 used to access the Prometheus and Grafana
  - 8180 exposed to access the gateway from the host machine. Note, once the Gateway service is created it has to be edited to configure the port.

#### Kind configuration

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
- role: worker
  extraPortMappings:
  - containerPort: 31000
    hostPort: 8180
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31043
    hostPort: 9443
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31100
    hostPort: 9001
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31110
    hostPort: 9002
    listenAddress: "127.0.0.1"
    protocol: TCP
```

#### Command to create kind cluster

- If the above kind cluster configuration is stored in a file cluster_config.yaml, use below command to create the kind cluster

```sh
kind create cluster --config cluster_config.yaml --name istio-dev
```

### Install Istio

#### Download the charts locally

- To add the repo to the helm for the Istio charts

```
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update
```

- The Istio charts are download to `charts/` folder. The Istio base and istiod charts to local with below commands

```sh
-- create and navigate charts folder
mkdir charts
cd charts/

-- download install helm base
helm pull istio/base --untar

-- download install helm istiod
helm pull istio/istiod --untar
```

#### Create namespace to deploy the Istio charts

- To create `istio-system` namespace with below command, in which the Istio base and istiod charts will be deployed

```sh
kubectl create ns istio-system
```

#### Deploy the Istio base charts

- Deploy Istio base charts to kind cluster.
- The default revision is deployed in this case by passing `default` in value defaultRevision. 

```sh 
-- navigate to charts/base
cd charts/base
helm upgrade -i istio-base . -n istio-system --set defaultRevision=default
```

#### Deploy the Istio istiod charts

- Deploy Istio istiod, before deploying Istio CRD make sure the Istio base is deployed

```sh
-- deploy the Istio/istiod
-- navigate to charts/istiod
cd charts/istiod
helm upgrade -i istiod . -n istio-system --wait
```

#### Chart status check

- Once Charts are installed the status can be checked using below helm command. Chart status should be deployed. 

```sh
$ helm ls -n istio-system

NAME         NAMESPACE       REVISION   UPDATED                       STATUS          CHART                   APP VERSION
istio-base   istio-system    1          2024-09-01 08:10:11 -0700 PDT deployed        base-1.23.0             1.23.0
istiod       istio-system    1          2024-09-01 08:10:28 -0700 PDT deployed        istiod-1.23.0           1.23.0
```

#### Deploy Kuberented Gateway API

- To deploy the Gateway API CRD use below command

```sh
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml
```

##### Create and deploy Gateway API custom resource

- The Kubenetes Gateway API custom resource YAML content looks like below (refer `kuberentes-gateway-api.yaml`).
- Note, in case of Istio Ingress Gateway the apiVersion would be `networking.istio.io/v1alpha3`.

```yaml
# fileName: kuberentes-gateway-api.yaml
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: gateway
  namespace: istio-ingress
spec:
  gatewayClassName: istio
  listeners:
  - name: default
    #hostname: "127.0.0.1"  # commented here but we can use "*.example.com" refer docs
    port: 80
    protocol: HTTP
    allowedRoutes:
      namespaces:
        from: All
```

##### Deploy the Gateway API resource

- Create namespace `istio-ingress` and deploy the Gateway resource. Use below command,

```sh
-- create namespace command
kubectl create ns istio-ingress

-- deploy the gateway api resource
kubectl apply -f kuberentes-gateway-api.yaml
```

- Once the Gateway is deployed, validate the service creation. Use below command

```sh
kubectl -n istio-ingress get svc
```
- Output might look like below with random nodeports assigned

```
NAME            TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)                        AGE
gateway-istio   LoadBalancer   10.96.201.160   <pending>     15021:32753/TCP,80:30526/TCP   8m20s
```

#### Edit the Gateway API service to update the nodePort to match Kind cluster configuration

- Edit the Gateway service using `kubectl -n istio-ingress edit svc/gateway-istio`.
- The nodePort(s) has to be updated like in below snippet.
- The incomming traffic from port 8180 will be routed to 31000 nodePort, already updated in Kind cluster configuration.

```yaml
    ipFamilies:
    - IPv4
    ipFamilyPolicy: SingleStack
    ports:
    - appProtocol: tcp
      name: status-port
      nodePort: 31021  #<--- update to 31021 port not used for this example
      port: 15021
      protocol: TCP
      targetPort: 15021
    - appProtocol: http
      name: default
      nodePort: 31000  # <--- update this port from any to 31000 configured in kind config
      port: 80
      protocol: TCP
      targetPort: 80
```

#### Deploy Kiali service 

- To deploy the Kiali server using helm chart, add the repo to helm CLI and download to local charts folder.

```sh
-- add repo to the helm cli
helm repo add kiali https://kiali.org/helm-charts
helm repo update
```

- Navigate to charts folder and download the charts to local.

```sh
cd charts/
helm pull kiali/kiali-server --untar
```

- Execute below command from the downloaded charts folder kaili-server.

> Note :-
> - The external_service urls are configured to access the Prometheus and Grafana. These components will be deployed by single chart later.
> - The components are deployed to monitor namespace, hence the url pattern is `<service-name>.<namespace>:port`

```sh
helm upgrade -i kiali-server . \
--set auth.strategy="anonymous" \
--set external_services.prometheus.url="http://prometheus-operated.monitor:9090" \
--set external_services.grafana.url="http://prometheus-grafana.monitor:80" \
--set external_services.grafana.in_cluster_url="http://prometheus-grafana.monitor:80" \
-n istio-system
```

- Verify status of the chart deployment and the status might look like below 

```sh
$ helm ls -n istio-system
NAME         NAMESPACE      REVISION   UPDATED                       STATUS          CHART                   APP VERSION
istio-base   istio-system    1         2024-09-01 08:10:11 -0700 PDT deployed        base-1.23.0             1.23.0
istiod       istio-system    1         2024-09-01 08:10:28 -0700 PDT deployed        istiod-1.23.0           1.23.0
kiali-server istio-system    1         2024-09-01 08:34:58 -0700 PDT deployed        kiali-server-1.89.0     v1.89.0
```

##### Create HTTPRoute resource to access Kiali server

- Create HTTPRoute resource for kiali as shown in the below yaml content, save the config in a file named `kiali-http-route.yaml`

```yaml
# filename: kiali-http-route.yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: kiali-http
  namespace: istio-system
spec:
  parentRefs:
  - name: gateway
    namespace: istio-ingress
  hostnames: ["127.0.0.1"]  # without hostname the service would not be accessible
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /kiali
    backendRefs:
    - name: kiali
      port: 20001
```

##### Deploy Kiali HTTPRoute

- To deploy the kiali route use below command

```sh
-- the resource will be deployed to istio-system namespace and uses the Gateway API previously deployed
kubectl apply -f kiali-http-route.yaml
```

##### Accessing Kiali from browser

- The address to access the Kiali UI is - `http://127.0.0.1:8180/kiali`

The Kiali UI looks like in below snaphsot. The Nginx backend app, Prometheus and Grafana where deployed here in the snapshot.

![image](https://github.com/user-attachments/assets/2b7c821d-9646-4c51-9b04-e4816b6dda3e)

![image](https://github.com/user-attachments/assets/ce544224-2814-4a44-afac-22484337685d)

![image](https://github.com/user-attachments/assets/19ae2aba-fd4d-4ae8-9829-b27563679cf2)

> Note :-
> Kiali UI might throw warning messages if the Prometheus chart is not deployed.

### Deploy prometheus 

- Add repo to helm local and download the chart to local charts folder
- Note, below is not a production ready configuration, requires further hardening through configuration. Refer documentation.

```sh
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo udpdate
cd charts/
helm pull prometheus-community/kube-prometheus-stack --untar
```

#### Create namespace and deploy the charts to Kind cluster

- To create namespace monitor and deploy Prometheus, use below set of commands

```sh
-- create namespace
kubectl create ns monitor

-- navigate to downloaded charts
cd charts/kube-prometheus-stack

-- deploy prometheus
helm upgrade -i prometheus . \
--namespace monitor \
--set prometheus.service.nodePort=31100 \
--set prometheus.service.type=NodePort \
--set grafana.service.nodePort=31110 \
--set grafana.service.type=NodePort \
--set alertmanager.service.nodePort=31120 \
--set alertmanager.service.type=NodePort \
--set prometheus-node-exporter.service.nodePort=31130 \
--set prometheus-node-exporter.service.type=NodePort
```

#### Status check of Prometheus chart deployment

- Verify the status of chart deployment use the helm command mentioned below and the output of the status will be similar to below snippet.

```sh
$ helm ls -n monitor
NAME        NAMESPACE   REVISION  UPDATED                       STATUS    CHART                           APP VERSION
prometheus  monitor     1         2024-09-01 08:43:38 -0700 PDT deployed  kube-prometheus-stack-62.3.1    v0.76.0
```

#### Access Prometheus and Grafana

- Accessing Prometheus
  - To access the Prometheus use `http://127.0.0.1:9001/` or `http://localhost:9001`

![image](https://github.com/user-attachments/assets/3a8fca83-e7d9-4458-a1aa-afbdcc9fd557)

- Acessing Grafana
  - To access the Grafana use `http://127.0.0.1:9002/` or `http://localhost:9002`, when prompted use username: `admin` and password: `prom-operator`.
![image](https://github.com/user-attachments/assets/15b02a2b-d40d-46e1-9eab-a1454136cb5f)

![image](https://github.com/user-attachments/assets/eee9ee05-fca4-4b08-857d-ace56c48f85c)


### Creating the NGNIX backend app

#### Deploy the backend app to kind cluster

- Below YAML content is resource defintion for the backend apps including namespace, service and deployment.
- The namespace label `istio-injection: enabled` will automatically create the Istio Proxy when the backend pod is created.
- Save the YAML content to a file named `backend_app_deployment.yaml`.

```yaml
# filename: backend_app_deployment.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: backend-app
  labels:
    istio-injection: enabled
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-nginx-config
  namespace: backend-app
data:
  nginx.conf: |
    worker_processes auto;
    error_log stderr notice;
    events {
      worker_connections 1024;
    }
    http {
      variables_hash_max_size 1024;

      log_format main '$remote_addr - $remote_user [%time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';
      access_log off;
      real_ip_header X-Real-IP;
      charset utf-8;

      server {
        listen 80;
        
        location /greet {
          default_type application/json;
          return 200 '{"status":"OK","message":"Greetings!! from server","current_time":"$time_iso8601"}';
        }
      }
    }

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-server
  namespace: backend-app
  labels:
    app: backend-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend-server
  template:
    metadata:
      labels:
        app: backend-server
    spec:
      volumes:
       - name: nginx-config
         configMap:
           name: backend-nginx-config
           items:
           - key: nginx.conf
             path: nginx.conf
      containers:
      - name: backend-server
        image: nginx
        ports:
        - containerPort: 80
        volumeMounts:
         - name: nginx-config
           mountPath: /etc/nginx
        resources:
          requests: 
            memory: "128Mi"
            cpu: "250m"
          limits:
            memory: "256Mi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: backend-svc
  namespace: backend-app
spec:
  selector:
    app: backend-server
  ports:
    - name: tcp-port
      protocol: TCP
      port: 8081
      targetPort: 80
---
```

- To deploy the backend app issue below command

```sh
-- the namespace will be created note, the label istio-injection is enabled which will create a envoy proxy sidecar automatically 
kubectl apply -f backend_app_deployment.yaml
```

#### Define HTTPRoute for the backend app 

- To access the backend we need to define a HTTPRoute like below and save it to a file named `app-httproute.yaml`

```yaml
# app-httproute.yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: http
  namespace: backend-app
spec:
  parentRefs:
  - name: gateway
    namespace: istio-ingress
  hostnames: 
   - "127.0.0.1"
  rules:
  - matches:
    - path:
        type: PathPrefix
        value: /greet
    backendRefs:
    - name: backend-svc
      port: 8081
```

- To deploy the HTTPRoute use below command

```sh
kubectl apply -f app-httproute.yaml
```

### Testing the access of Backend API
- From browser use `http://127.0.0.1:8180/greet` should see the respone like below

![image](https://github.com/user-attachments/assets/4948d4c0-8e33-40bb-9c89-8ac0885c3cdd)

