```
helm repo add minio-operator https://operator.min.io
```

- check the available version

```
helm search repo minio-operator
```
- create the operator
```
helm install \
  --namespace minio-operator \
  --create-namespace \
  --values custom-minio-operator-values.yaml \
  minio-operator minio-operator/operator
```

- Create certificates before issuing the tenant helm deployment

```
kubectl create ns tenant-0
```

```
- note if the tenant-0 is not the namespace of tenant name then the dns names should be updated
kubectl -n tenant apply -f minio-tenant-certificate.yaml
```
-- download values 
-- use that to create tenant

 `cd /mnt/c/thiru/edu/minio/`

- deploy the tenant
  - note the auto certificate is disabled
  - also due to that the service port is not 443 (need to check why it is so)
```
helm upgrade -i \
--namespace tenant-0 \
--create-namespace \
--set tenant.name=tenant-0 \
--values custom-minio-tenant-values.yaml \
tenant-0 minio-operator/tenant
```

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
kubectl -n tenant-0 apply -f 3_minio_route.yaml # if the tenant name is different check the backend service name
``` 

add the `minio.demo.com` in the hosts file and issue `https://minio.demo.com`

![image](https://github.com/user-attachments/assets/a9932885-15f3-47ba-a635-0e2e76b4a7c4)

For user name and password use 

```
k -n tenant-0 get secrets/myminio-env-configuration -ojsonpath='{.data.config\.env}' | base64 -d && echo
```
