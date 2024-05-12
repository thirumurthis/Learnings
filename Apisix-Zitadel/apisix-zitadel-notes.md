# Deploying Apache Apisix and Zitadel in Kubernetes

In this blog will be demonstrating how to setup Apisix and Zitadel in Kind Kubernetes cluster for development.

Pre-requisites:

Software to be installed

* Docker desktop
    
* Kind
    
* Helm
    
* Kubectl
    

Basic understanding on Gateway and OpenId Connect.

## What is Apache Apisix?

* Apache Apisix is opensource API Gateway based on Nginx and etcd.
    
* In this demonstration Apache Apisix is installed to cluster using helm chart, along with Apisix Dashboard and Apisix Ingress controller.
    
* This is only for development and **not** production grade. For more info refer the [Apache Apisix](https://apisix.apache.org) documentation.
    

## What is Zitadel?

* Zitadel provides identity management service along with authentication management. Zitadel can also be installed as standalone application, refer [Zitadel](https://zitadel.com/docs/guides/start/quickstart) documentation.
    
* In this demonstration Zitadel application is installed with helm chart along with UI dashboard.
    
* Prior to deploying Zitadel application the define a domain name, this value should be set in the configuration `ExternalDomain` either in override values or passed as helm command.
    
* Zitadel can be installed with either Cockroach db or Postgres db. In this demonstartion Postgres db is used.
    
* The `Externaldomain` value is also used as part of default username and for password check the documentation.
    

## Installing Kind Cluster

Below yaml defines the Kind cluster configuration, with Docker desktop running we can create the the cluster with this configuration.

> Note :-
> 
> * The ingress option is enabled in the kind cluster.
>     
> * Apisix exposes the Gateway service as NodePort or Loadbalancer. In here NodePort service is used and port 30080 is exposed.
>     
> * By configuring the ports in `extraPortMappings` the Apisix Dashboard can be accessed doesn't require port-forwarding.
>     

```yaml
# filename: kind-cluster.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: api6-zitadel
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 30080
    hostPort: 80
    protocol: TCP
  - containerPort: 30443
    hostPort: 443
    protocol: TCP
```

* Save the content of kind configuration to a file named `kind-cluster.yaml` and use below command to create cluster.

```sh
kind create cluster --config kind-cluster.yaml
```

## Installing Apisix

To install Apsix with helm charts, add the helm repo to local and issue update command like below.

```sh
helm repo add apisix https://charts.apiseven.com
helm repo update
```

In here we pull the chart to local and use it. To download the latest chart to local use the command below.

```sh
helm pull apisix/apisix --untar
```

> Alternatively, you can download the helm charts from [Apisix Helm Chart repo](https://github.com/apache/apisix-helm-chart), requires understanding on helm charts.

In the downloaded helm chart update the chart version of `etcd`, `apisix-dashboard` and `apisix-ingress-controller` in `chart.yaml` file to latest version (below is the version number at the time if this writing)

After updating the version issue the command `helm dependency update` from the chart directory, helm will pull the specified version of dependent chart under the `charts` folder.

```yaml
appVersion: 3.9.1
sources:
  - https://github.com/apache/apisix-helm-chart

dependencies:
  - name: etcd
    version: 10.0.4
    repository: https://charts.bitnami.com/bitnami
    condition: etcd.enabled
  - name: apisix-dashboard
    version: 0.8.2
    repository: https://charts.apiseven.com
    condition: dashboard.enabled
    alias: dashboard
  - name: apisix-ingress-controller
    version: 0.14.0
    repository: https://charts.apiseven.com
    condition: ingress-controller.enabled
    alias: ingress-controller
```

### Custom override values yaml for Apisix chart

The override values file for Apisix deploymentis used for this deployment. Note, only few configuration is updated like image tag name of initContainer, http service ports for Apisix service. Refer the documentation for mode customization, but for local development this should suffice.

The Apisix discovery registry is enabled with Kubernetes cluster. There are other registry option which can be configured, refer documentation.

```yaml
# filename: apisix-values.yaml
image:
  repository: apache/apisix
  pullPolicy: IfNotPresent
  tag: 3.8.0-debian

replicaCount: 1

initContainer:
  image: busybox
  tag: latest

serviceAccount:
  create: true

rbac:
  create: true

service:
  type: NodePort
  externalTrafficPolicy: Cluster
  externalIPs: []
      
  http:
    enabled: true
    servicePort: 80
    containerPort: 9080
    nodePort: 30080   # port used in kind cluster configuration
  tls:
    servicePort: 443
    nodePort: 30443

apisix:
  enableIPv6: true
  enableServerTokens: true

  admin:
    enabled: true
    type: ClusterIP
    externalIPs: []
    ip: 0.0.0.0
    port: 9180
    servicePort: 9180
    cors: true
    credentials:
      admin: edd1c9f034335f136f87ad84b625c8f1  # Default value as in values yaml
      viewer: 4054f7cf07e344346cd3f287985e76a2

  discovery:
   enabled: true
   registry:
     kubernetes:
        service:
          schema: http 
          host: ${KUBERNETES_SERVICE_HOST}
          port: ${KUBERNETES_SERVICE_PORT}

externalEtcd:
  host:
    - http://etcd.host:2379
  user: root
  password: ""
  existingSecret: ""
  secretPasswordKey: "etcd-root-password"

etcd:
  enabled: true
  prefix: "/apisix"
  timeout: 30

  auth:
    rbac:
      create: false
      rootPassword: ""
    tls:
      enabled: false
      existingSecret: ""
      certFilename: ""
      certKeyFilename: ""
      verify: true
      sni: ""

  service:
    port: 2379

  replicaCount: 3

# Apisix Dashboard configuration
dashboard:
  enabled: true
  image:
    pullPolicy: IfNotPresent
    
  config:
    conf:
      etcd:
        # -- Supports defining multiple etcd host addresses for an etcd cluster
        endpoints:
          - apisix-etcd:2379
        prefix: "/apisix"
        username: ~
        password: ~

# -- Ingress controller configuration
ingress-controller:
  enabled: true
  image:
    pullPolicy: IfNotPresent
  config:
    apisix:
      adminAPIVersion: "v3"
```

* Save the above content in a file `apisix-values.yaml` as override values yaml within the apisix chart directory.
    
* Below command will install `Apisix`, `Apisix Dashboard` and `Apisix Ingress Controller`.
    

```bash
helm upgrade --install apisix -f apisix-values.yaml . \
    --set service.type=NodePort \
    --set service.http.nodePort=30080 \
    --set ingress-controller.enabled=true \
    --set dashboard.enabled=true \
    --create-namespace \
    --namespace ingress-apisix \
    --set ingress-controller.config.apisix.serviceNamespace=ingress-apisix
```

> Note :-
> 
> * Not all the configuration in override values yaml is customized, compare it with default values yaml file.
>     
> * Also, the line `--set service.type=NodePort --set service.http.nodePort=30080` in helm command can be removed since they are already added override yaml.
>     

* To check the status of Apisix deployments use below command.

```sh
kubectl -n ingress-apisix get pods,svc
```

![image](https://github.com/thirumurthis/Learnings/assets/6425536/6625d78f-b0ba-4fa9-8714-a0b32af11f56)

### Install Ingress route for Apisix dashboard

Once the `Apisix ingress controller` pod is in running state,Apisix dashboard ingress route can be configured to access from browser.

> Info :-
> 
> * `Apisix Ingress controller` uses CRD's to resolve Apisix routes and also resolves Ingress resource definition. It creates route and registers in apisix application as well.
>     

The ingress configuration is similar to the Kubernetes Ingress resources, in below yaml file any traffic from [`http://apisix.localhost/`](http://apisix.localhost/) will be routed to `apisix-dashboard` service to port 80.

```yaml
#filename:  apisix-dashboard-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api6-dashboard-ingress
  namespace: ingress-apisix  # The namespace where the apisix is installed
spec:
  ingressClassName: apisix
  rules:
  - host: apisix.localhost
    http:
      paths:
      - backend:
          service:
            name: apisix-dashboard
            port:
              number: 80
        path: /
        pathType: Prefix
```

* Save the ingress configuration in `apisix-dashboard-ingress.yaml` and use below command to deploy.

```sh
kubect apply -f apisix-dashboard-ingress.yaml
```

### Configure hosts file with domain name for loop back address

Open the hosts file and add below domains for loop back address (127.0.0.1)

```sh
127.0.0.1 localhost apisix.localhost zitadel.local backend.localhost
```

Now, open a browser and we should be able to access the Apisix dashboard.

* To login the user name and password is admin
    

![image](https://github.com/thirumurthis/Learnings/assets/6425536/2786d503-3e4b-400e-894b-690c0176bc29)

The configured routes will be displayed in the dashbord like

![image](https://github.com/thirumurthis/Learnings/assets/6425536/42c554ba-e304-423e-9d98-911ca312fef7)

> Info :-
> 
> * Installing `Apisix ingress controller` is optional, if **not** installed the route configuration should be created and updated manually.
>     
> * Below is optional only if Apisix ingress controller is NOT installed. To update the route configuration manually to Apisix.
>     
> * 1. Port forward the `apisix-admin` service use command - `kubectl -n ingress-apisix port-forward svc apisix-admin 9180:9180`
>         
> * 2. Obtain the admin key from the override values yaml under `apisix.admin.credentials.admin`. In this demonstration this default value as mentioned in Apisix documentation.
>         
>  * 3. Use Curl command to configure route. Below is the curl command to configure Apisix dashboard route.
>         

```sh
  curl http://127.0.0.1:9180/apisix/admin/routes/1 \
      -H "X-API-KEY: $admin_key" -X PUT -i -d '
      {
        "name": "apisix-dashboard-route",
        "desc": "Route for apisix dashboard",
        "labels": {"created-by": "user"},
        "uris": ["/","/*"],
        "host": "apisix.localhost",
        "upstream": {
            "name": "apisix-dashboard-upstream",
            "desc": "upstream for apisix dashboard route",
            "labels": {"created-by": "user"},
            "type": "roundrobin",
            "hash_on": "vars",
            "scheme": "http",
            "pass_host": "pass",
            "nodes": [{"host":"apisix-dashboard.ingress-apisix","port":80, "weight": 100 }],
            "timeout": { "connect": 60,"send": 60,"read": 60 }
          }
      }'
```

### Apisix route configuration representation

![image](https://github.com/thirumurthis/Learnings/assets/6425536/d14ccbb6-4f76-4266-984d-306e4f78528b)

Sample Apisix Dashboard route view showing all three parts `route`, `plugins` and `upstream` in route configuration.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/e25f6128-2016-4e5c-9f4c-1d25e546e592)

## Installing Zitadel

Zitadel installed with secure Postgres, below are the steps involved

* Installing certificates
    
* Installing Postgres DB
    
* Installing Zitadel
    

In order to configure the standalone Zitadel we need to define the `ExternalDomain` and `ExternalPort` in the Zitadel override yaml configuration.

> Note :-
> 
> * Latter in this blog, we configure a Nginx backend application to be accessed using Apisix route with oidc-connector plugin to connect to Zitadel.
>     
> * The Apisix pod tries to discover the Zitadel OIDC url within the pod which does not resolve [`http://zitadel.localhost`](http://zitadel.localhost). As a workaround, have created the helm release with the name `zitadel` (this creates the service as `zitadel`)and the app installed in `local` namespace. With this configuration, the url looks like [`https://zitadel.local`](https://zitadel.local), Apisix pod is able to resolve this url since kubernetes DNS resolution `<service-name>.<namespace>` is also same as the `ExternalDomain`.
>     

#### Installing certificates

* The certificate configuration yaml file is provided in the Zitadel documentation so we use that as such without any customization. Below are the instruction to deploy.
    

```sh
# Create namespace
kubectl create namespace local

# Generate TLS certificates
kubectl apply -f https://raw.githubusercontent.com/zitadel/zitadel-charts/main/examples/2-postgres-secure/certs-job.yaml -n local

# wait till the job is completed 
kubectl wait --for=condition=complete job/create-certs -n local
```

### Install Postgres

* Below is the Postgres override values yaml file which will be used for Postgres deployment.
    

```yaml
# filename: postgres-values.yaml
volumePermissions:
  enabled: true
tls:
  enabled: true
  certificatesSecret: postgres-cert
  certFilename: "tls.crt"
  certKeyFilename: "tls.key"
auth:
  postgresPassword: "abc"
```

* Save the above content in a file `postgres-values.yaml`
    
* Use below commands to install the 15.2.11 (latest version at the time of writting).
    

```sh
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install --wait db bitnami/postgresql --version 15.2.11 --values postgres-values.yaml
```

### Install ZITADEL

To add the helm repo to local

```sh
helm repo add zitadel https://charts.zitadel.com
helm repo update
```

Zitadel override values yaml file

```yaml
# zitadel-values.yaml
zitadel:
  masterkey: x123456789012345678901234567891y
  configmapConfig:
    ExternalSecure: false
    # modify the external domain to localhost or any domain exists
    # currently this value will be overrided from helm command with set
    ExternalDomain: localhost
    # add this configuration since we have ingress
    ExternalPort: 80
    TLS:
      Enabled: false
    Database:
      Postgres:
        Host: db-postgresql
        Port: 5432
        Database: zitadel
        MaxOpenConns: 20
        MaxIdleConns: 10
        MaxConnLifetime: 30m
        MaxConnIdleTime: 5m
        User:
          Username: zitadel
          SSL:
            Mode: verify-full
        Admin:
          Username: postgres
          SSL:
            Mode: verify-full
  secretConfig:
    Database:
      Postgres:
        User:
          Password: xyz
        Admin:
          Password: abc

  dbSslCaCrtSecret: postgres-cert
  dbSslAdminCrtSecret: postgres-cert
  dbSslUserCrtSecret: zitadel-cert

replicaCount: 1
```

* Save the content in file named `zitadel-values.yaml`, Note, the service port is set as 80, instead of default 8080 which will be configured in Apisix ingress configuration yaml as well.
    
* Use below command to deploy the zitadel in `local` namespace.
    

```sh
helm upgrade --install zitadel -f ./zitadel-values.yaml . \
    --set zitadel.configmapConfig.ExternalDomain=zitadel.local \
    --set zitadel.configmapConfig.ExternalPort=80 \
    --set service.port=80 \
    --create-namespace \
    --namespace local
```

### Creating Zitadel Apisix route

* The Apisix route configuration

```yaml
# apisix-zitadel-ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api6-zitadel-ingress
spec:
  ingressClassName: apisix
  rules:
  - host: zitadel.local
    http:
      paths:
      - backend:
          service:
            name: zitadel
            port:
              number: 80
        path: /
        pathType: Prefix
```

* Save the yaml content to `apisix-zitadel-ingress.yaml` and use below command to install zitadel ingress route.    

```sh
kubectl apply -f apisix-zitadel-ingress.yaml -n local
```

The Zitadel UI should be accessible via [`http://zitadel.local`](http://zitadel.local) from browser. In the login page when prompted the username is `zitadel-admin@zitadel.zitadel.local` and default password is `Password1!`. Refer [documentation](https://zitadel.com/docs/self-hosting/deploy/linux) for more info.

* Upon first access the UI looks like below.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/55668ec2-59bb-4662-ad81-9c28f61bf797)

## Create Nginx Backend app access with Apisix and Zitadel configuration

### Creating simple Nginx backend application

Below is the manifest file to deploy simple NGINX app with an endpoint `/greet` which responds back with JSON object.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: backend-app
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
          return 200 '{"status":"OK","message":"Greetings!! from server"}';
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
    - protocol: TCP
      port: 8081
      targetPort: 80
---
```

Before updating the Apisix route, to configure Zitadel as oidc-connector plugin, create Project and Application in Zitadel for client_id and client_secret.

Using Zitadel UI, create a project and input a project name.

* Click the `Create an application` and input name for application.
    
* Select `Web` and click continue.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/74fc4c3d-855e-4052-bef0-66e0cbf0473b)

* Select `code` (the screen looks like below)

![image](https://github.com/thirumurthis/Learnings/assets/6425536/3641f8d8-aa16-40c1-a2f2-528facfaee79)

* Input the redirect url, enable the `Development Mode` since we are using `http` url. In the `Redirect URIshttp://backend.localhost/greet/redirect`. This is the URL Zitadel redirects after successful authentication.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/1386a352-06ea-4b80-bdc4-217aa0cf37c8)

* Upon saving the screen displays `client_id` and `client_secret` make a note of it. Once closed this will not be displayed, else we need to re-create it.
    
* Below is the screen where URLs provided by the Zitadel Identity Provider.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/054b1a96-32aa-48a9-9b50-ea814a9e9ada)

In the Apisix route configuration below the plugins `redirect_uri:` [`http://backend.localhost/greet/redirect`](http://backend.localhost/greet/redirect)which should match the route config.

Note, if the redirect\_uri match the route config exactly, noticed issues in the Apisix logs when accessing the backend app, refer [zitadel documentation](https://apisix.apache.org/docs/apisix/plugins/openid-connect/#troubleshooting).

In this case the host is `backend.localhost` and uri is `/*` and we configured uri as [`http://backend.localhost/greet/redirect`](http://backend.localhost/greet/redirect). The redirect in the uri is not configured in the backend app on Nginx config.

Complete Apisix route configuration using the Apisix dashboard and the completed configuration looks like below. Update the `client_id` and `client_secret` from the Zitadel application.

```json
{
  "uri": "/*",
  "name": "backend-app",
  "methods": [
    "GET",
    "OPTIONS"
  ],
  "host": "backend.localhost",
  "plugins": {
    "openid-connect": {
      "_meta": {
        "disable": false
      },
      "bearer_only": false,
      "client_id": "****clientid created in zitadel app****",
      "client_secret": "*** Zitadel app client secret *****",
      "discovery": "http://zitadel.local/.well-known/openid-configuration",
      "introspection_endpoint": "http://zitadel.local/oauth/v2/introspect",
      "realm": "master",
      "redirect_uri": "http://backend.localhost/greet/redirect"
    }
  },
  "upstream": {
    "nodes": [
      {
        "host": "backend-svc.backend-app",
        "port": 8081,
        "weight": 10
      }
    ],
    "timeout": {
      "connect": 6,
      "send": 6,
      "read": 6
    },
    "type": "roundrobin",
    "scheme": "http",
    "pass_host": "pass",
    "keepalive_pool": {
      "idle_timeout": 60,
      "requests": 1000,
      "size": 320
    }
  },
  "status": 1
}
```

Click the Route section on Apisix Dashboard to create the Route configuration, shown in below snapshot.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/28c5ad66-b657-4842-ada8-05a16f030dc9)

Update the upstream configuration, the Host configuration is `<service-name>.<namespace>` of the backend application service.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/da51ef6d-697f-43be-9511-2a49df16443e)

Edit the openid-connect plugin and update the configuration

![image](https://github.com/thirumurthis/Learnings/assets/6425536/fef1c9c3-ad07-40a9-8dec-86c705877380)

Enable the plugin and click submit to create the route configuration.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/83c2c7dd-08a2-47ba-b8ae-4edb4d6194c2)

### Accessing backend app from browser

The [`http://backend.localhost/greet`](http://backend.localhost/greet) redirects to `zitadel.local` Zitadel login page.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/cd579785-f392-4a26-9cf5-3d5c8f9d9b6a)

![image](https://github.com/thirumurthis/Learnings/assets/6425536/7932aa16-5995-46b7-8fd5-ad925210b2ef)

After successful authentication the JSON payload will be displayed in the browser.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/c9b405aa-0230-4657-83b3-2218b99f99b2)

In certain case if browser throws 500 internal server error check the Apisix container logs to see if the correct redirect url is logged.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/807f6c09-fda8-4e00-a299-22405df90c81)
