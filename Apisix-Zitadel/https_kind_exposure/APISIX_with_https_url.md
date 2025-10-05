## APISIX in KIND with https

1. Create Kind cluster with below configuration

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: apisix-ingress
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080
    hostPort: 80
    protocol: TCP
  - containerPort: 30443
    hostPort: 443
    protocol: TCP
```

2. Install `Kubernetes gateway API` _(not used for routing)_ and `Cert manager` with below command

 - Note, the Kuberentess gateway API is not used to create route here.

_Kuberentes Gateway API_

```bash
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.3.0/standard-install.yaml
```
 
 _cert manager_
 
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.17.2/cert-manager.yaml
```

3. Install the `apisx` using helm chart in `apisix`namespace. The `serviceNamespace` should be same as the namespace unless using different one. Most of the configuration is default we enabled the `apisix.ssl.enabled` in the chart.

```bash
kubectl create ns apisix
```

```bash
helm upgrade -i apisix apisix/apisix --namespace apisix \
--set apisix.ssl.enabled=true \
--set apisix.nginx.logs.errorLogLevel=info \
--set service.type=NodePort \
--set service.http.enabled=true \
--set service.http.servicePort=80 \
--set service.http.containerPort=9080 \
--set service.http.nodePort=30080 \
--set service.tls.servicePort=443 \
--set service.tls.nodePort=30443 \
--set dashboard.enabled=true \
--set ingress-controller.enabled=true \
--set ingress-controller.config.apisix.serviceNamespace=apisix \
--set ingress-controller.config.kubernetes.enableGatewayAPI=true \
--set ingress-controller.gatway.tls.enabled=false
```

4. Install Issuer and Certificate in the apisix namespace

```yaml
# file name: 1_apisix_cert_issuer.yaml
# deploy in zitadel namespace
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-apisix-ca-issuer
spec:
  selfSigned: {}
---
# deploy in zitadel namespace
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

```bash
kubectl -n apisix apply -f 1_apisix_cert_issuer.yaml
```

5. Add the entry `127.0.0.1 apisix.demo.com` in the hostname.

6. Create `ApisixTls` and `ApisixRoute` 

```yaml
# 2_dashboard_apisix_tls.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: sample-tls
spec:
  hosts:
    - apisix.demo.com
  secret:
    name: selfsigned-apisix-cert-secret  # certificate created by the cert-manager
    namespace: apisix
```

- apply the resource

```bash
kubectl -n apisix apply -f 2_dashboard_apisix_tls.yaml
```

```yaml
# 3_dashboard_apisix_route.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: dashboard-route
spec:
  http:
    - name: apisix-db
      match:
        hosts:
          - apisix.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: apisix-dashboard
          servicePort: 80
```

```sh
kubectl -n apisix apply -f 3_dashboard_apisix_route.yaml
```

7. Once all the apps are deployed and running in the kind cluster, then issue `https://apisix.demo.com` to access the dashboard.

8. If the ApisixTls resource is `not` installed then should see below message in the apisix pod logs

```
http_ssl_client_hello_phase(): failed to match any SSL certificate by SNI: apisix.demo.com, context: ssl_client_hello_by_lua*, client: 10.244.0.1, server: 0.0.0.0:9443
```

![image](https://github.com/user-attachments/assets/ab658088-8bc2-46cd-9e65-bc3d7803704d)

