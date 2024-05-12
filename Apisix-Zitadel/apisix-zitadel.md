## Deploying Apache Apisix and Zitadel in Kuberentes

Pre-requesites:
- Below software installed
 - Docker desktop 
 - Kind
 - Helm
 - Kubectl


We will use Kind cluster to demostrate the deployment of Apisix and Zitadel.

### What is Apache Apisix?
Apache Apisix is opensource API Gateway based on Nginx and etcd. For more info, refer the Apisix documentation.

In this demonstration, Apache Apisix is installed to cluster using helm chart. 
Along with Apache Apisix additionally the Apisix Dashboard and Apisix Ingress controller will aslo be deployed.
This deployment is only for development, for production grade refer the documentation.

### What is Zitadel?
Zitadel provides identity management service along with authentication management. This application has a opensource version, whcih supports OIDC (OpenID connect specifications)

Zitadel application is also installed with helm chart, it provides a dashboard. Priod to deploying Zitadel, `ExternalDomain` and `ExternalPort` configuration needs to be provided in the values yaml.

Zitadel UI, default username uses the Externaldomain in it and also a default password.

## Configuring the Kind Cluster.
With Docker desktop running we can use below Kind configuration file to create the cluster.

- In the kind cluster the ingress is enabled with kubeadmConfigPatches
- Note the Apisix exposes the gateway service as NodePort or Loadbalancer, in Kind cluster we use NodePort service and so expose the port 30080 in Kind cluster configuration. This port will be passed in Apisix helm installation so we can access the Dashboard without any port frowarding.

```yaml
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

- Save the kind configuration to a file kind-cluster.yaml, and use below command to install

```sh
kind create cluster --config kind-cluster.yaml
```

## Installing Apisix

To install Apsix with helm charts, we need to add the repo to local and update it.

```sh
helm repo add apisix https://charts.apiseven.com
helm repo update
```

Pull the chart to local with below command, which will download the charts to local

```sh
helm pull apisix/apisix --untar
```
Alternatively, you can download the helm charts from [Apisix Helm Chart repo](https://github.com/apache/apisix-helm-chart)

Once the charts is pulled locally, update the chart version of etcd, apisix-dashboard and apisix-ingress-controller to latest version (at the time if this writing)

Below is snippet of the Chart yaml of apisix, once updated issue `helm dependency update` from the chart directory.

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

### Override values yaml for Apisix

Below is the override values file for Apisix deployment, which is used for this deployment.
Only few configuration has been updated like image name of init container, http service ports for Apisix service.
Compare with the default values.yaml file, most of the configuration will be similar. 

The kuberentes discovery registry is enabled here. There are other registry option which can be configured in Apisix, refer documentation.

```yaml
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
      
  # -- Apache APISIX service settings for http
  http:
    enabled: true
    servicePort: 80
    containerPort: 9080
    nodePort: 30080
  # -- Apache APISIX service settings for tls
  tls:
    servicePort: 443
    nodePort: 30443

apisix:
  # -- Enable nginx IPv6 resolver
  enableIPv6: true

  # -- Whether the APISIX version number should be shown in Server header
  enableServerTokens: true

  admin:
    # -- Enable Admin API
    enabled: true
    # -- admin service type
    type: ClusterIP
    # loadBalancerIP: a.b.c.d
    # loadBalancerSourceRanges:
    #   - "143.231.0.0/16"
    # -- IPs for which nodes in the cluster will also accept traffic for the servic
    externalIPs: []
    # -- which ip to listen on for Apache APISIX admin API. Set to `"[::]"` when on IPv6 single stack
    ip: 0.0.0.0
    # -- which port to use for Apache APISIX admin API
    port: 9180
    # -- Service port to use for Apache APISIX admin API
    servicePort: 9180
    # -- Admin API support CORS response headers
    cors: true
    # -- Admin API credentials
    credentials:
      # -- Apache APISIX admin API admin role credentials
      admin: <***edd1c9f034335f136f87ad84b625c8f1***> #Using default as in values yaml
      # -- Apache APISIX admin API viewer role credentials
      viewer: <***4054f7cf07e344346cd3f287985e76a2***>

  discovery:
   enabled: true
   registry:
     kubernetes:
        service:
          # apiserver schema, options [http, https]
          schema: http #default https

          # apiserver host, options [ipv4, ipv6, domain, environment variable]
          host: ${KUBERNETES_SERVICE_HOST} #default ${KUBERNETES_SERVICE_HOST}

          # apiserver port, options [port number, environment variable]
          port: ${KUBERNETES_SERVICE_PORT}  #default ${KUBERNETES_SERVICE_PORT}

# -- external etcd configuration. If etcd.enabled is false, these configuration will be used.
externalEtcd:
  host:
    - http://etcd.host:2379
  user: root
  password: ""
  existingSecret: ""
  secretPasswordKey: "etcd-root-password"

# -- etcd configuration
# use the FQDN address or the IP of the etcd
etcd:
  # -- install etcd(v3) by default, set false if do not want to install etcd(v3) together
  enabled: true
  # -- apisix configurations prefix
  prefix: "/apisix"
  # -- Set the timeout value in seconds for subsequent socket operations from apisix to etcd cluster
  timeout: 30

  # -- if etcd.enabled is true, set more values of bitnami/etcd helm chart
  auth:
    rbac:
      # -- No authentication by default. Switch to enable RBAC authentication
      create: false
      # -- root password for etcd. Requires etcd.auth.rbac.create to be true.
      rootPassword: ""
    tls:
      # -- enable etcd client certificate
      enabled: false
      # -- name of the secret contains etcd client cert
      existingSecret: ""
      # -- etcd client cert filename using in etcd.auth.tls.existingSecret
      certFilename: ""
      # -- etcd client cert key filename using in etcd.auth.tls.existingSecret
      certKeyFilename: ""
      # -- whether to verify the etcd endpoint certificate when setup a TLS connection to etcd
      verify: true
      # -- specify the TLS Server Name Indication extension, the ETCD endpoint hostname will be used when this setting is unset.
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
        # -- apisix configurations prefix
        prefix: "/apisix"
        # -- Specifies etcd basic auth username if enable etcd auth
        username: ~
        # -- Specifies etcd basic auth password if enable etcd auth
        password: ~

# -- Ingress controller configuration
ingress-controller:
  enabled: false
  image:
    pullPolicy: IfNotPresent
  config:
    apisix:
      adminAPIVersion: "v3"
```

- Save the override values yaml in a file named `apisix-values.yaml` in the apisix chart.
- From apisix helmchart issue below command

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

Note:- 
  - We override some of the values with helm chart as well. For example, you can remove the line `--set service.type=NodePort --set service.http.nodePort=30080` since they are already added in `apisix-values.yaml`

- Once deployed the apisix application should start running as expected. Use below command to check the status

```sh
kubectl -n ingress-apisix get pods
```
![image](https://github.com/thirumurthis/Learnings/assets/6425536/6625d78f-b0ba-4fa9-8714-a0b32af11f56)


Now since the apps are running, we need to create a ingress route to access the Apisix dashboard from browser

- Since the Apisix Ingress controller is installed, the CRD's will resolve the Ingress yaml definition and create routes for us.
The ingress configuration is similar to the Kuberentes Ingress resources, in here we specify any route from the host `apisix.localhost` to be routed to apisix-dashboard service port 80. 

```yaml
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

- Save the ingress configuration in `apisix-dashboard-ingress.yaml`, to deploy use below command

```sh
kubect apply -f apisix-dashboard-ingress.yaml
```

##### Configure the local hosts file with domain name

Open the hosts file and add below domain for loop back address

```sh
127.0.0.1 localhost apisix.localhost zitadel.local backend.localhost
```

Now, open a browser and we should be able to access the Apisix dashboard.
- To login the user name and password is admin

![image](https://github.com/thirumurthis/Learnings/assets/6425536/2786d503-3e4b-400e-894b-690c0176bc29)

The configured routes will be displayed in the dashbord like 

![image](https://github.com/thirumurthis/Learnings/assets/6425536/42c554ba-e304-423e-9d98-911ca312fef7)


In case if the Apisix ingress controller is not installed, we can create the route configuration and update manually.

To apply the route configuration manually, 
1. Port forward the `apisix-admin` service `kubectl -n ingress-apisix port-forward svc apisix-admin 9180:9180`
2. From the override values yaml grab the admin_key, should be under `apisix.admin.credentias.admin`. This is default value mentioned in documentation.
3. Use below curl command to configure route

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
Route configuration representation

![image](https://github.com/thirumurthis/Learnings/assets/6425536/d14ccbb6-4f76-4266-984d-306e4f78528b)

From the Dashboard sample route where we see all three parts of the configuration
![image](https://github.com/thirumurthis/Learnings/assets/6425536/e25f6128-2016-4e5c-9f4c-1d25e546e592)


