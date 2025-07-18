## Minio in Kind cluster configured with Cert-manager

In this blog have explained the steps to configure Minio with Certificate manager in Kind cluster. Additionally, Apisix is used as Gateway so the Minio tenant console can be accessed from the host machine. 

Minio is open-source object storage server used to store unstructured data. This blog doesn’t deep dive the concepts, only shows how to deploy and access Minio in KinD cluster for learning purpose.

#### Pre-requsities 
 - Docker desktop or Docker daemon process running in WSL2
 - Kind CLI installed
 - Kubectl CLI installed
 - Helm CLI installed


#### Summary
Cert manager, Apisix and Minio deployed to KinD cluster created in Docker. The Apisix and Minio apps deployed using helm chart. To access the Minio and Apisix Dashboard from host machine, we use Apisix Ingress controller to deploy routes. A self-signed certificate generated by certificate manager is configured with the Minio and Apisix so these apps can be accessed using https ur.


### Deploying Kind cluster

  The kind cluster configuration is below

```yaml
# file-name: kind-cluster.yaml
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

To create kind cluster deploy the kind configuration using below command

```
kind create cluster --config kind-cluster.yaml
```

Once deployed, check if the `kubectl get nodes` to see if the cluster is created.

### Deploying Certificate manager

To deploy the _cert manager_ use below command. 

**Note:**
  - if there are any new version available check cert manager [documentation](https://cert-manager.io/docs/usage/certificate/) on instruction to deploy to cluster 

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.17.2/cert-manager.yaml
```

### Deploying Apisix 

1. The Apisix is deployed to `apisix` namespace. Create the namespace use below command.

```bash
kubectl create ns apisix
```

To deploy the Apisix with helm chart, use below command. The `serviceNamespace` should match the namespace unless using different one. Most of the configuration is default we enabled the `apisix.ssl.enabled` in the chart. If we need to display the info logs use the setting `--set apisix.nginx.logs.errorLogLevel=info` in the command.


```sh
helm upgrade -i apisix apisix/apisix --namespace apisix \
--set apisix.ssl.enabled=true \
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

4. Install Certificate Issuer and Certificate requestor in the `apisix` namespace

Below is how to configuration looks like, save the 

```yaml
# file-name: 1_apisix_cert_issuer.yaml
# deploy in apisix namespace
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-apisix-ca-issuer
spec:
  selfSigned: {}
---
# deploy in apisix namespace
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-apisix-cert
spec:
  commonName: apisix.demo.com  # commonName is optional
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

- Use below command to deploy the configuration to cluster, make sure if there are multiple kind cluster to use the correct cluster context.

```bash
kubectl -n apisix apply -f 1_apisix_cert_issuer.yaml
```

5. Create `ApisixTls` and `ApisixRoute`

ApisixTls resource will associate the certificate to the route and enables HTTPS connection between clients and Apisix. 

```yaml
# file-name: 2_dashboard_apisix_tls.yaml
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

- To deploy the resource to cluster use below command.

```bash
kubectl -n apisix apply -f 2_dashboard_apisix_tls.yaml
```

 ApisixRoute to access the Apisix dashboard.

```yaml
# file-name: 3_dashboard_apisix_route.yaml
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

Deploy to the cluster using below command.

```bash
kubectl -n apisix apply -f 3_dashboard_apisix_route.yaml
```

6. Add the dns name entry `127.0.0.1 apisix.demo.com` to the hostname. For windows, the path is `C:\Windows\System32\drivers\etc\hosts`in the hostname.


7. Once all the apps are deployed and running state in the cluster, from browser use `https://apisix.demo.com` to access the dashboard.


![image](https://github.com/user-attachments/assets/ab658088-8bc2-46cd-9e65-bc3d7803704d)


** Info**

Note, if the ApisixTls resource is `not` installed then should see below message in the apisix pod logs

```
http_ssl_client_hello_phase(): failed to match any SSL certificate by SNI: apisix.demo.com, context: ssl_client_hello_by_lua*, client: 10.244.0.1, server: 0.0.0.0:9443
```

### Deploying Minio

The Minio Operator is installed first, then we deploy the tenant.
For more details to understand how to configure the Minio with self-signed certificate (Strictly not recommended for production) with Cert manager refer [minio documentation](https://min.io/docs/minio/kubernetes/upstream/operations/cert-manager.html).

Once the Minio operator is installed, then tenants can be deployed using the tenant charts. 

>**Info:**
> - Before deploying the operator and tenants, the namespace and certificates needs to be deployed.

 Add the operator chart to the local helm repo

```
helm repo add minio-operator https://operator.min.io
```

1. Create cluster issuer certificate

```yaml
# file-name: 1_self_signed_clusterissuer.yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: selfsigned-root
spec:
  selfSigned: {}
```

- Deploy to cluster using below command

```bash
kubectl apply -f 1_self_signed_clusterissuer.yaml
```

2. Create namespace for minio-operator deployment, in this case the namespace is `minio-operator`.

```bash
kubectl create ns minio-operator
```

3. Create certificate request resource for minio-operator namespace. 

```yaml
# file-name: 2_operator_cert_request.yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: minio-operator-ca-certificate
  namespace: minio-operator # Make note of namespace
spec:
  isCA: true
  commonName: operator
  secretName: operator-ca-tls
  duration: 70128h # 8y
  privateKey:
    algorithm: ECDSA
    size: 256
  issuerRef:
    name: selfsigned-root  # the clusterissuer name
    kind: ClusterIssuer
    group: cert-manager.io
```

Deploy the above manifest to cluster using below command

```bash
kubectl -n minio-operator apply -f 2_operator_cert_request.yaml
```

4. Create certificate for operator sts (secure token service)
 
 Note: 
   - The `secretName` shouldn't be changed in the configruation below.
   - Also, check the `dnsNames`, for the cluster and update as required in below configuration.

```yaml
# file-name: 4_operator_sts_cert_requestor.yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: sts-certmanager-cert
  namespace: minio-operator
spec:
  dnsNames:
    - sts
    - sts.minio-operator.svc
    - sts.minio-operator.svc.cluster.local # Replace cluster.local with the value for your domain.
  secretName: sts-tls  # don't change this
  issuerRef:
    name: minio-operator-ca-issuer
```

Deploy the certificate to the minio-operator using below command.

```bash
kubeclt -n minio-operator apply -f 4_operator_sts_cert_requestor.yaml
```

5. Install operator with TLS configuration environment variable disabled, the override config will look like below. 
If The configuration would look like below,  Instead, Operator relies on cert-manager to issue the TLS certificate.

```yaml
# file-name: 5_custom-operator-values.yaml
operator:
    env:
    - name: OPERATOR_STS_AUTO_TLS_ENABLED
        value: "off"
    - name: OPERATOR_STS_ENABLED
        value: "on"
```

Deploy the minio-operator to `minio-operator` namespace using below command. The `replicaCount` is set to 2.

```sh 
helm upgrade -i \
  --namespace minio-operator \
  --create-namespace \
  --set replicaCount=2 \
  --values 5_custom-operator-values.yaml \
  minio-operator minio-operator/operator
```

6. Next we create tenant named `tenant-0`, the namespace is also same as the name of tenant. Create the certificate request for `tenant-0`.

- Create namespace

```sh
kubectl create ns tenant-0
```

```yaml
# file-name: 6_tenant-0-cert_requestor.yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: tenant-0-ca-certificate
  namespace: tenant-0
spec:
  isCA: true
  commonName: tenant-0-ca
  secretName: tenant-0-ca-tls
  duration: 70128h # 8y
  privateKey:
    algorithm: ECDSA
    size: 256
  issuerRef:
    name: selfsigned-root
    kind: ClusterIssuer
    group: cert-manager.io
```

Deploy to cluster using below command

```sh
kubectl -n tenant-0 apply -f 6_tenant-0-cert_requestor.yaml
```

7. Create certificate Issuer and certificate request for tenant-0 namespace, with the dnsNames required to access the tenant. The Issuer and Certificate request manifest looks like below.

```yaml
---
# file-name: 7_tenant-0-cert-issuer-request.yaml
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: tenant-0-ca-issuer
  namespace: tenant-0
spec:
  ca:
    secretName: tenant-0-ca-tls
---
# refer- https://min.io/docs/minio/kubernetes/upstream/operations/cert-manager/cert-manager-tenants.html
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: minio-tenant-cert
  namespace: tenant-0
spec:
  dnsNames:
    - "minio.demo.com"       # This is added to the certificate to allow access from host machine
    - "minio.tenant-0"
    - "minio.tenant-0.svc"
    - 'minio.tenant-0.svc.cluster.local'
    - '*.minio.tenant-0.svc.cluster.local'
    - '*.tenant-0-hl.tenant-0.svc.cluster.local'
  secretName: app-minio-tls # The certificate will be generated and store in this secret
                            # This secret should be updated in the tenant-0 custom-tenant-override.yaml file
  issuerRef:
    name: tenant-0-ca-issuer
```

 Deploy the above manifest to cluster using below command once the manfiest is saved to the file name.

 ```sh
 kubectl -n tenant-0 apply -f 7_tenant-0-cert-issuer-request.yaml
 ```

8. To deploy the tenant using helm chart, we need to update the default values yaml file for our configuration.

**Note:** 
 - Download the helm chart to local using `helm pull minio-operator/tenant --untar`. Copy the default values yaml file to local and update that for further tenant deployment.

 Deploy the tenant using below command, few values are overrided using helm set command.

 ```sh
helm upgrade -i \
--namespace tenant-0 \
--create-namespace \
--set tenant.name=tenant-0 \
--values custom-minio-tenant-values.yaml \
tenant-0 minio-operator/tenant
```

The custom override values yaml for tenant-0 is below, most of the values are default. The override values includes comments.

**Note:**
  - Update the `externalCertSecret` field in the override values with the secret name the cert-manager created the secret with certificate.
  - Set the `requestAutoCert` field to false, since we use self-signed certificate generated by cert-manager.

```yaml
#file-name: 8_custom_tenant_override_values.yaml
# Root key for MinIO Tenant Chart
tenant:
  name: app-minio-tenant # will be overriden by the helm command
  image:
    repository: quay.io/minio/minio
    tag: RELEASE.2025-04-08T15-41-24Z
    pullPolicy: IfNotPresent
  imagePullSecret: { }
  initContainers: [ ]
  scheduler: { }
  # The default configuration is being used, for production find better approach to create the secret adhead
  configSecret:
    name: myminio-env-configuration
    accessKey: minio
    secretKey: minio123

  poolsMetadata:
    annotations: { }
    labels: { }
  pools:
    - servers: 2
      name: pool-0
      volumesPerServer: 2
      size: 2Gi        # The capacity per volume requested per MinIO Tenant Pod.
      storageAnnotations: { }
      storageLabels: { }
      annotations: { }
      labels: { }
      tolerations: [ ]
      nodeSelector: { }
      affinity: { }
      resources: { }
      securityContext:
        runAsUser: 1000
        runAsGroup: 1000
        fsGroup: 1000
        fsGroupChangePolicy: "OnRootMismatch"
        runAsNonRoot: true
      containerSecurityContext:
        runAsUser: 1000
        runAsGroup: 1000
        runAsNonRoot: true
        allowPrivilegeEscalation: false
        capabilities:
          drop:
            - ALL
        seccompProfile:
          type: RuntimeDefault
      topologySpreadConstraints: [ ]
  mountPath: /export
  subPath: /data
  metrics:
    enabled: false
    port: 9000
    protocol: http
  certificate:
    externalCaCertSecret: [ ] 
    externalCertSecret:
     - name: app-minio-tls       # IMPORTANT - update this with the secret created in the tenant-0 namespace 
       type: cert-manager.io/v1  # For cert namanger use this value
    requestAutoCert: false  # When using custom cert or self-signed cert disable this configuration
    certConfig: { }
  features:
    bucketDNS: false
    domains: { }
    enableSFTP: false
  buckets: [ ]  # if we need to create any buckets by default use this config
  users: [ ]
  podManagementPolicy: Parallel
  liveness: { }
  readiness: { }
  startup: { }
  lifecycle: { }
  exposeServices: { }
  serviceAccountName: ""
  prometheusOperator: false
  logging:
    anonymous: true   # udpated the configuration for logging
    json: true
    quiet: true
  serviceMetadata: { }
  env: [ ]
  priorityClassName: ""
  additionalVolumes: [ ]
  additionalVolumeMounts: [ ]
ingress:
  api:
    enabled: false
    ingressClassName: ""
    labels: { }
    annotations: { }
    tls: [ ]
    host: minio.local
    path: /
    pathType: Prefix
  console:
    enabled: false
    ingressClassName: ""
    labels: { }
    annotations: { }
    tls: [ ]
    host: minio-console.local
    path: /
    pathType: Prefix
```

9. Below steps is not required if the tenant-0 certificate is used ApisixTls. In here create a namespace certificate Issuer for tenant-0 and a certificate request manifest for Apisix access.

```yaml
#file-name: 9_apisix_tenant_0_cert_issuer_requestor.yaml
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-minio-ca-issuer
spec:
  selfSigned: {}
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-minio-cert
spec:
  commonName: minio.demo.com
  secretName: selfsigned-minio-cert-secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-minio-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames: minio.demo.com
---
```

Deploy the manifest with below command

```sh
kubectl -n tenant-0 apply -f 9_apisix_tenant_0_cert_issuer_requestor.yaml
```

10. Create ApisixTls,  ApisixRoute and ApisixUpstream for minio resource

- The ApisixTls manifest

```yaml
# file-name: 10_apisix_tls_minio.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: minio-tls
spec:
  hosts:
    - minio.demo.com
  secret:
    name: selfsigned-minio-cert-secret
    namespace: tenant-0
```

- ApisixUpstream enables Apisix to forward request as https to backend service

```yaml
# file-name: 10_apisix_upstream_minio.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixUpstream
metadata:
  name: tenant-https-upstream # Name of your upstream
spec:
  nodes:
  - host: tenant-0-console # Replace with your backend service domain/IP
    port: 9443
  scheme: https # Specify HTTPS protocol for the upstream
```

- The ApisixRoute manifest for minio routing

  - The plugins included to the backend to route to backend, note the minio uses https and to route the client https to backend we use redirect plugin with below config.

```yaml
#file-name: 10_apisix_route_minio.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: tenant-route
spec:
  http:
    - name: tenant-console
      websocket: true
      match:
        hosts:
          - minio.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: tenant-0-console
          servicePort: 9443
      plugins:
       - name: proxy-rewrite
         enable: true
         config:
            scheme: https
       - name: redirect
         enable: true
         config:
            http_to_https: true
            https_port: 9443   # The service created by tenant is not default 443 so overrided
```

Deploy the ApisixTls, ApisixUpstream and ApisixRoute using below command to `tenant-0` namespace

```sh
kubectl -n tenant-0 apply -f 10_apisix_tls_minio.yaml
kubectl -n tenant-0 apply -f 10_apisix_upstream_minio.yaml
kubectl -n tenant-0 apply -f 10_apisix_route_minio.yaml
```

### Accessing Minio

The tenant override values includes the user name and password info, we can fetch the same info from the cluster using below command. The `configSecret` field in the override values yaml is the secret name

```sh
kubeclt -n tenant-0 get secrets/myminio-env-configuration -ojsonpath='{.data.config\.env}' | base64 -d && echo
```

Add a entry for the dns in the host file in windows (same as in apisix)

```
127.0.0.1 apisix.demo.com mini.demo.com
```

From the browser issue `https://minio.demo.com` should render the login page

![image](https://github.com/user-attachments/assets/a9932885-15f3-47ba-a635-0e2e76b4a7c4)


### Creating Buckets in minio using container in same cluster

- To verify the minio using client from the same kubernetes cluster, we can deploy the minio/mc image

- Create the deployment with the deployment manifest. 

**Note:**
 - Instead of hard coding the access key and secret key in the env of deployment beloww, we can exec into the pod after deployment and use `export MINIO_ACCESS_KEY=<value>` to set the env values.
 - The service we use here is `minio` created part of the `tenant-0` deployment, which uses 443 port.

```yaml
# file-name: minio_client_deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: minio-client
spec:
  replicas: 1
  selector:
    matchLabels:
      app: minio-client
  template:
    metadata:
      labels:
        app: minio-client
    spec:
     containers:
     - name: mc
       image: minio/mc
       command: ["/bin/sh", "-c", "--"]
       args: ["sleep infinity"] # Keep the container running
       env:
         - name: MINIO_ENDPOINT
           value: "minio.tenant-0.svc.cluster.local:443"  # Replace with your MinIO service endpoint
         - name: MINIO_ACCESS_KEY
           value: "minio" # Replace with your MinIO access key
         - name: MINIO_SECRET_KEY
           value: "minio123" # Replace with your MinIO secret key   
       resources:
         limits: 
           cpu: 250m
           memory: 524Mi
         requests:
           cpu: 100m
           memory: 254Mi
```

- To deploy the manifest use below command 

```sh
kubectl -n tenant-0 apply -f minio_client_deploy.yaml
```

- Since the deployment is configured with the environment variable, once we  exec to the pod and use the `mc alias` command like below. The `--insecure` flag is used since we haven't mounted the self-signed certificate.

```bash
kubectl -n tenant-0 exec -it $(kubectl -n tenant-0 get pod -l'app=minio-client' --no-headers -o custom-columns=":metadata.name") -- bash
# mc --insecure alias set myminio https://${MINIO_ENDPOINT} ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY}
# mc mb myminio/test-000
```

Log into minio we could see the bucket created if the deployment is successful.

![image](https://github.com/user-attachments/assets/c033dc3c-2679-4d84-aa7e-842bb02745a2)


