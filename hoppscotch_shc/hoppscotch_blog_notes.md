
During REST API development was exploring tools opensource tools similar to Postman and came accross the Hoppscotch.
Hoppscotch provides lots of options it has both community and enterprise offering. In this article, have deployed the community version. Used for local development, this article details the configuration and commands necessary to deploy hoppscotch in KinD cluster. To access the Hoppscotch frontend we use Apisix Ingress and cert-manager, with self-signed certs we can access the UI using https url from browser.

With the Hoppscotch deployed API running in localhost and in internet can be accessed. Note, since the Hoppscotch container in local docker can't acccess the localhost API running on the host machine, we had to install the Hoppscotch browser extension and configure the localhost url in the extension, with this we can see how to access the endpoint running in host machine.

This article is with reference to the documentation provided in hoppscotch website - https://docs.hoppscotch.io/documentation/self-host/helm-chart-deployment/digital-ocean


Pre-requisites:
  - Docker (Using Docker Daemon community version runnning in WSL2)
  - KinD CLI
  - Kubectl CLI
  - Helm CLI
  - Git CLI


### Deploy the KinD Cluster 

To create the KinD cluster we use below configuration which maps the host 80 and 443 to NodePort exposed by the Apisix Data plane.
Note, below configuration will create 3 worker nodes

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: dev
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 80
    protocol: TCP
  - containerPort: 30443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
- role: worker
```

Save the above content to a file named `kind_cluster.yaml` and use KinD CLI to create the cluster. The command to create the cluster is as follows

```sh
kind create cluster --config kind_cluster.yaml
```

The output of the command will let us know if the cluster created successfully or not. 


### Deploy cert-manager

Certificate manager is deployed using helm chart. Below the helm command will install the cert-manager version 1.21.0 in cert-manager namespace

```sh
helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.21.0 \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true
```

To verify the deployment status use the command `kubectl -n cert-manager get all` to check the status of all resources.


### Deploy the self-signed certificate for Apisix Ingress

With the cert manager we create self signed certificates which will be used by Apisix, so we can access the application from host machine browser in https url.

Create the apisix namespace, since the certificates will be issued at namespace level not at cluster level.

```sh
kubectl create ns apisix
```

Below configuration will create certificate and secrets which will be configured in the Apisix deployment.

```yaml
---
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: ss-apisix-ca-issuer
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: ss-apisix-cert
spec:
  commonName: apisix.demo.com  
  secretName: ss-apisix-cert-secret # cert will be created in this secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: ss-apisix-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - apisix.demo.com  # dns name add this to hosts file for loopback address
---
```

Save above content to a file named `apisix_cert.yaml`. Then we can deploy the manifest using below command

```sh
kubectl -n apisix apply -f apisix_cert.yaml
```


### Install Apisix Ingress (Control and Data plane)


Apisix installation can be done in different variation, we deploy control plane and data plane seperately. Still the etcd is used from the Apisix chart itself.

Helm command used to deploy the control plane is shown below, note the secret name with the certificate should be updated correctly in the command, in this case `ss-apisix-cert-secret`. The secret would have been created by cert manager based on above deployment. 

```
helm upgrade --install --create-namespace -n apisix apisix-cp apisix/apisix \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=control_plane \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=ss-apisix-ca-issuer \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set apisix.admin.allow.ipList[0]=0.0.0.0/0 \
  --set etcd.replicaCount=3 \
  --set etcd.enabled=true \
  --wait
```

The data plane installation helm command will look like below, this will work as-is. For production deployments, the adminKey.value shouldn't be exposed like below instead use secrets (refer the Apisix documentation)

```
helm upgrade  --install apisix-dp \
  --namespace apisix \
  --create-namespace \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=data_plane \
  --set apisix.nginx.logs.enableAccessLog=true \
  --set apisix.nginx.logs.errorLogLevel=warn \
  --set apisix.admin.enabled=false \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=ss-apisix-cert-secret \
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

### Install the ApisixTls and ApisixRoute to access Apisix Dashboard 

Below are the configuration for ApisixTls and ApisixRoute. The ApisixRoute adds the adminKey in the incoming request which is also not production grade option. The ApisixTls uses the secret name generated by the cert manager with certificate info.

```yaml
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: apisix-route-tls
spec:
  ingressClassName: apisix
  hosts:
    - apisix.demo.com
  secret:
    name: ss-apisix-cert-secret  # certificate created by the cert-manager
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
	    # for production implementation chcek Apisix doc
        - name: proxy-rewrite
          enable: true
          config:
            headers:
              set:
                X-Api-Key: "edd1c9f034335f136f87ad84b625c8f1"
```

Save the above content to a file named `apisix_route.yaml`. To deploy the manfiest use below command.

```sh
kubectl apply -f apisix-route.yaml -n apisix
```

Once the ApisixTls and ApisxRoute is deployed, we can access the Apisix Dashboard using `https://apisix.demo.com/ui`.

Information:
  When working behind the proxy in enterprise network, sometimes the apisix.demo.com might be routed to external internet in that case use the the DNS something like apisix.local or apisix.demo. This also depends on type of proxy being used. 


### Install the Hoppscotch Community using Helm Chart

Clone the helm chart of Hoppscotch repo using below command.

```sh
git clone https://github.com/hoppscotch/helm-charts.git
```

## Go to helm charts directory cloned from hoppscotch, change the path if needed
cd /mnt/c/hoppscoth/helm-charts

## Install the chart for Community Edition

helm upgrade --install hoppscotch -n hoppscotch --create-namespace ./charts/shc --values ../overlay-values.yaml


helm template hoppscotch ./charts/shc --values ../overlay-values.yaml

## apply the route info if apisix route is configured

kubectl -n hoppscotch apply -f ../route.yaml

## set the host file with 127.0.0.1 admin.hs.com frontend.hs.com backend.hs.com

## in browser use https://admin.hs.com to view the ui, add the hoppscotch chrome extension to access localhost:5000 url and add it to the path.

