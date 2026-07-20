follow the steps in this link 

https://docs.hoppscotch.io/documentation/self-host/helm-chart-deployment/digital-ocean


## install cert manager
## use below helm command to install 

helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.20.2 \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true

## Create the certificate in cluster using the route info 
kubectl create ns apisix

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
  secretName: ss-apisix-cert-secret # cert created in this secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: ss-apisix-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - apisix.demo.com  # dns name add this to hosts file for loopback address
---
```

### save above to a file apisix-cert.yaml
kubectl apply -f apisix-cert.yaml -n apisix

## install apisix cluster and data plane

To install apisix with control plane use 

```
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

To install the data plane

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

## Install the apisix route 

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

## save the above file to apisix-route.yaml

kubectl apply -f apisix-route.yaml -n apisix

## Clone the repository
git clone https://github.com/hoppscotch/helm-charts.git

## Go to helm charts directory cloned from hoppscotch, change the path if needed
cd /mnt/c/hoppscoth/helm-charts

## Install the chart for Community Edition

helm upgrade --install hoppscotch -n hoppscotch --create-namespace ./charts/shc --values ../overlay-values.yaml


helm template hoppscotch ./charts/shc --values ../overlay-values.yaml

## apply the route info if apisix route is configured

kubectl -n hoppscotch apply -f ../route.yaml

## set the host file with 127.0.0.1 admin.hs.com frontend.hs.com backend.hs.com

## in browser use https://admin.hs.com to view the ui, add the hoppscotch chrome extension to access localhost:5000 url and add it to the path.

## Then the API endpoints should be accessible

If the API is not accessible, then try below option to add host to the coredns

kubectl edit configmap coredns -n kube-system

   .:53 {
        errors
        health {
           lameduck 5s
        }
        ready
		# add below 
        hosts {
            172.18.0.1 host.docker.internal
            fallthrough
        }
		
        kubernetes cluster.local in-addr.arpa ip6.arpa {
           pods insecure
           fallthrough in-addr.arpa ip6.arpa
           ttl 30
        } 
		
## add the hosts to the deployment deployed

kubectl patch -n hoppscotch deployment hoppscotch-community --type='json' -p='[
  {
    "op": "add",
    "path": "/spec/template/spec/hostAliases",
    "value": [
      {
        "ip": "172.18.0.1",
        "hostnames": ["host.docker.local"]
      }
    ]
  }
]'
