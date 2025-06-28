
Using helm chart

1. Installing operator

```sh
helm repo add postgres-operator-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator


kubectl create ns postgres-op

# install the postgres-operator
helm install postgres-operator postgres-operator-charts/postgres-operator -n postgres-op
```

```sh
 kubectl --namespace=postgres-op get pods -l "app.kubernetes.io/name=postgres-operator"
```

1.i Installing the operator ui
```bash

# add repo for postgres-operator-ui
helm repo add postgres-operator-ui-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator-ui

# install the postgres-operator-ui
helm install postgres-operator-ui postgres-operator-ui-charts/postgres-operator-ui -n postgres-op
```

- check 

```sh
 kubectl --namespace=postgres-op get pods -l "app.kubernetes.io/name=postgres-operator-ui"
```

- creating route for operator ui

```sh
kubectl -n postgres-op apply -f issuer_cert_tls.yaml
```

```yaml
# issuer_cert_tls.yaml
---
# deploy in zitadel namespace
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-postgre-ca-issuer
spec:
  selfSigned: {}
---
# cert-request.yaml
# deploy in zitadel namespace
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-postgres-cert
spec:
  commonName: postgres.demo.com  
  secretName: selfsigned-postgres-crt-secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-postgre-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - postgres.demo.com  # dns name add this to hosts file for loopback address
---
# tls.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: postgres-op-tls
spec:
  hosts:
    - postgres.demo.com
  secret:
    name: selfsigned-postgres-crt-secret
    namespace: postgres-op
```

- Route configuration, add an entry in the hosts file `127.0.0.1 postgres.demo.com`

```yaml
# postgres-route.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: postgres-op-ui-route
spec:
  http:
    - name: postgres-op-ui
      match:
        hosts:
          - postgres.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: postgres-operator-ui
          servicePort: 80
```