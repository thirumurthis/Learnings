## Istio with Kuberentes Gateway API

- In this blog we will see how to deploy Istio in Kind cluster configure Kuberenetes Gateway API.


Pre-requsites:
 - Docker desktop installed and running
 - Kind CLI
 - Helm CLI (v3.15.3+)
 - Understanding of Service Mesh
 - Understanding on Isio basics and Gateway


 From Istio documentation the recommended way to create Gateway is to use Kuberentes Gateway API since the release of version v1.1. With the Istio Ingress Gateway, we create a `VirtualService`, with Kuberentes Gateway API we have to create HTTPRoute which directly configures to the service.

 In order to configure the Kuberentes Gateway API, we need to deploy the CRD in Kind cluster

 ```
 kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml
 ```

Below is what will be done in this blog

- Create a kind cluster, exporting few node ports to access the apps
- Deploy Istio using helm charts (base and istiod) with basic configuration (not production ready)
- Deploy Prometheus community chart, which includes Prometheus, Grafana in monitor namespace
- Deploy the Kuberentes Gateway API CRD 
- Deploy an simple Nginx all to serve simple response
- Deploy Kiali (Istio Dashboard) which is configured to use the external services of Promethus and Grafana
- Create Gateway and HTTPRoutes resources to access the kiali and Nginx backend apps


Note,
  - The Istio helm charts where downloaded to my local and the deployed. It can also be deployed directly once the repo is added.

- Create Kind cluster named istio-dev with below configuration
 - hostPort 9001 and 9002 used to access the Prometheus and Grafana
 - 8180 will be used to access the gateway. Note, once the Gateway service is created we will edit it to configure this port.

Represenation of the app deployed in the Kind cluster

![image](https://github.com/user-attachments/assets/b678dd9f-8767-4255-8563-5ae85ad19446)


#### Configuration 
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
  - containerPort: 31120
```

#### Command to create kind cluster

- If the above kind cluster configuration is stored in a file cluster_config.yaml, use below command to create the kind cluster

```sh
kind create cluster --config cluster_config.yaml --name istio-dev
```

### Install Istio

- Add the repo to the helm

```
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update
```

Create a charts directory and download the base and istiod charts to local with below commands

```sh
-- download install helm base
helm pull istio/base --untar

-- download install helm istiod
helm pull istio/istiod --untar
```

- Create istio-system namespace

```sh
kubectl create ns istio-system
```

- Deploy Istio base charts to kind cluster.
- The default revision is deployed in this case by passing `default` in value defaultRevision. 

```sh 
-- navigate to charts/base
cd charts/base
helm upgrade -i istio-base . -n istio-system --set defaultRevision=default
```

- Deploy Istio istiod, before deploying Istio CRD make sure the Istio base is deployed

```sh
-- deploy the Istio/istiod
-- navigate to charts/istiod
cd charts/istiod
helm upgrade -i istiod . -n istio-system --wait
```

- Once installed we should be able to see the deployed chart version and status
```sh
helm ls -n istio-system
NAME         NAMESPACE       REVISION   UPDATED                       STATUS          CHART                   APP VERSION
istio-base   istio-system    1          2024-09-01 08:10:11 -0700 PDT deployed        base-1.23.0             1.23.0
istiod       istio-system    1          2024-09-01 08:10:28 -0700 PDT deployed        istiod-1.23.0           1.23.0
```

### Deploy Kuberented Gateway API

- Deploy the Gateway API CRD with below command

```sh
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.1.0/standard-install.yaml
```

- Create the Gateway resource with below yaml file.
- Note, in case if istio ingress gateway the apiVersion would be `networking.istio.io/v1alpha3`

- Save below YAML resource to `kuberentes-gateway-api.yaml`

```yaml
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

- To deploy the Gateway resource use below command

```sh
-- create namespace command
kubectl create ns istio-ingress

-- deploy the gateway api resource
kubectl apply -f kuberentes-gateway-api.yaml
```

- Once the Gateway is deployed, validate the resource creation using below command

```sh
kubectl -n istio-ingress get svc
```
- Output might look like below with random nodeports assigned

```
NAME            TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)                        AGE
gateway-istio   LoadBalancer   10.96.201.160   <pending>     15021:32753/TCP,80:30526/TCP   8m20s
```

- Edit the Gateway service using `kubectl -n istio-ingress edit svc/gateway-istio` and make sure the nodePort are updated to below port values. The 31000 port is already configured as containerPort in the kind cluster configuration.

```
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
- Deploy the Kiali server using helm chart

```sh
helm repo add kiali https://kiali.org/helm-charts
helm repo update
```

- Navigate to the charts folder and download the chart to local using below command

```sh 
helm pull kiali/kiali-server --untar
```

- To deploy the kiali server navigate to the downloaded charts and issue below command
- Note, the externa_service urls are defined but will be configured later.
- Prometheus and Grafana are deployed by single chart, and deployed to monitor namespace. The url pattern is `<service>.<namespace>:port`

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

- Create HTTPRoute for kiali, refer the configuration below and save it in a file named `kiali-http-route.yaml`

```yaml
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
- Deploy the kiali route using below command

```sh
-- the resource will be deployed to istio-system namespace and uses the Gateway API previously deployed
kubectl apply -f kiali-http-route.yaml
```

- To access the kiali server use `http://127.0.0.1:8180/kiali`

Below is how the Kiali UI looks after configuring the Nginx, Prometheus and Grafana.
![image](https://github.com/user-attachments/assets/2b7c821d-9646-4c51-9b04-e4816b6dda3e)

![image](https://github.com/user-attachments/assets/ce544224-2814-4a44-afac-22484337685d)

![image](https://github.com/user-attachments/assets/19ae2aba-fd4d-4ae8-9829-b27563679cf2)

Note, Kiali UI might throw warning messages if the Prometheus chart is not deployed.

### Deploy prometheus 

- Add repo to helm local and download the chart to local charts folder
- Note, below is not a production ready configuration, requires further hardening through configuration. Refer documentation.

```sh
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo udpdate
cd charts/
helm pull prometheus-community/kube-prometheus-stack --untar
```

- To deploy Prometheus

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

- Verify the status of chart deployment, output might look like below

```sh
$ helm ls -n monitor
NAME        NAMESPACE   REVISION  UPDATED                       STATUS    CHART                           APP VERSION
prometheus  monitor     1         2024-09-01 08:43:38 -0700 PDT deployed  kube-prometheus-stack-62.3.1    v0.76.0
```

- Accessing the Prometheus and Graphana
  - To access the Prometheus use `http://127.0.0.1:9001/` or `http://localhost:9001`

![image](https://github.com/user-attachments/assets/3a8fca83-e7d9-4458-a1aa-afbdcc9fd557)

  
  - To access the Grafana use `http://127.0.0.1:9002/` or `http://localhost:9002`, when prompted use username: `admin` and password: `prom-operator`.
![image](https://github.com/user-attachments/assets/15b02a2b-d40d-46e1-9eab-a1454136cb5f)

![image](https://github.com/user-attachments/assets/eee9ee05-fca4-4b08-857d-ace56c48f85c)


- Create backend app

- Resource defintion for the backend apps save this to a file named `backend_app_deployment.yaml`

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: backend-app
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

