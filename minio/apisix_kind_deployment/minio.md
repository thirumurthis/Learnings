```
helm repo add minio-operator https://operator.min.io
```

- check the available version

```
helm search repo minio-operator
```

- Before installing we create the namespace and create certificate since we use cert-manager to issue certificates
 - Follow - https://min.io/docs/minio/kubernetes/upstream/operations/cert-manager.html

-1. Create cluster issuer certificate

```
kubectl apply -f 00_clusterissuer_cert.yaml
```

-2. Create minio operator namepsace
```
kubectl create ns minio-operator
```

```
 kubectl apply -f 01_operator_cert.yaml
```

-3. Create cert issuer for operator namespace

```
kubectl apply -f 02_operator_ns_cert_issuer.yaml
```

-4. Create a certificate secret for statefulset. Note the cluster DNS needs to be verified
- The secretname should be same

```
 kubectl apply -f 03_operator_sts_cert_secret.yaml
```

-5. install operator with TLS disabled - Instead, Operator relies on cert-manager to issue the TLS certificate.
```yaml
              env:
                - name: OPERATOR_STS_AUTO_TLS_ENABLED
                  value: "off"
                - name: OPERATOR_STS_ENABLED
                  value: "on"
```

-5.i. create the operator with helm chart 
```
helm upgrade -i \
  --namespace minio-operator \
  --create-namespace \
  --set replicaCount=2 \
  minio-operator minio-operator/operator
```
  #--values custom-minio-operator-values.yaml \ <---- there is some error 

-6. Create certificates before issuing the tenant helm deployment

```
kubectl create ns tenant-0
```
-7. Apply the certificate from clusterissuer to tenant-0 namespace

```
kubectl -n tenant-0 apply -f 04_tenant_ns_cert.yaml
```

-8. Note, if the tenant-0 is not the namespace of tenant name then the dns names should be updated

```
kubectl -n tenant-0 apply -f 05_minio-tenant-certIssuer_cert.yaml
```

-- download values froim the chart, update the custom values to create tenant `cd /mnt/c/thiru/edu/minio/`

- deploy the tenant
  - note the requestAutoCert flag is disabled

```
helm upgrade -i \
--namespace tenant-0 \
--create-namespace \
--set tenant.name=tenant-0 \
--values custom-minio-tenant-values.yaml \
tenant-0 minio-operator/tenant
```

 The custom values file includes some certificate and autogeneration certifacte disabled.
 - we can create serviceaccount as well

- optional to override the values from helm chart example
```
helm upgrade -i \
--namespace app-tenant-0 \
--create-namespace \
--set tenant.name=app-tenant-0 \
--set tenant.pools[0].servers=2 \
--set tenant.pools[0].volumePerServer=2 \
--set tenant.pools[0].size=4Gi \
--set tenant.configSecret.name=app-tenant-cfg \
--values custom-minio-tenant-values.yaml \
app-tenant-0 minio-operator/tenant
```

- after deploying the tenant, we need to create the apisix certificates

```
# make sure to update the namespace correctly
kubectl -n tenant-0 apply -f 1_minio_cert_issuer.yaml
kubectl -n tenant-0 apply -f 2_minio_tls.yaml
kubectl -n tenant-0 apply -f 3_tenant_route.yaml # if the tenant name is different check the backend service name
``` 

- We need to create apisix upstream with https scheme and that name of the upstream should match the service to be associated with. Currently there are bunch of error message
- Also we need to add plugin to the route with redirect, proxy-rewrite

```
kubeclt -n tenant-0 apply -f upstream_service.yaml
```

add the `minio.demo.com` in the hosts file and issue `https://minio.demo.com`

![image](https://github.com/user-attachments/assets/a9932885-15f3-47ba-a635-0e2e76b4a7c4)

For user name and password use 

```
k -n tenant-0 get secrets/myminio-env-configuration -ojsonpath='{.data.config\.env}' | base64 -d && echo
```
