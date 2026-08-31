## S3 compatible Seaweedfs in KinD cluster with simple Jbang based CLI to access services

Recently had to work on Seaweed file service to configure S3 compatible storage service. As part of the learning process have explored the options to configure the service in KinD cluster with SSL and in this article have documented the details with the configuration.

To install the Seaweed have used Seaweedfs operator chart and to access the S3 Gateway and UI services have used certificate-manager and Apisix Ingress controller deployed to the KinD cluster. These configuration are not production ready, for more production grade configuration refer the Seaweed documentation.

### Pre-requisites
  - KinD cli
  - kubectl cli
  - helm cli

### Summary

To start with we install KinD cluster with one control-plane and 3 data-plane. The configuration used uses extraPortMapping configuration to access the Apisix Ingress from the host machine.

The certificate manager and Apisix are deployed using the helm charts. The Apisix Ingress is deployed as dual mode (control plane and data plane), the etcd is deployed part of the control plane.

With the cert manager and Apisix installed to access the Apisix dashboard the Certificate Issuer, Apisix Tls and Apisix Routes are created (This is optional).

The Seaweedfs operator is installed using the seaweedfs-operator chart in seaweedfs-operator namespace. Once the operator CRDs are installed, the seaweed cluster resources is deployed in seaweedfs namespace using manifest yaml in the KinD cluster using kubectl command. To make the seaweedfs S3 gateway endpoint accessible from host machine with HTTPS, the Certificate Issuer and Certificate Request manifest also deployed to seaweedfs namespace. With the certificate resources installed, deploying the ApisixTLS resource with the DNS names defined for admin, filer and S3 endpoint will create a secret with the self-signed CA cert info. This TLS secret info is used in the Seaweedfs cluster manifest in the tls property. The ApisixRoute manifest configuration is finally deployed to create routes to access the Seaweed admin UI, filer UI and S3 endpoint.

### Installation

#### Kind cluster installation

The kind configuration manifest to install kind cluster with 1 control and 3 data plane.

```yaml
# file name: kind-cluster.yaml
---
apiVersion: kind.x-k8s.io/v1alpha4
kind: Cluster
name: dev  # name for the kind cluster
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080  # apisix port
    hostPort: 80
  - containerPort: 30443
    hostPort: 443
- role: worker
- role: worker
- role: worker
```

Use the kind cli to install the cluster, the command looks like below. Assume above yaml configuration content is stored in file named kind-cluster.yaml. Rest of this article uses the same format, the filename in the start of yaml will be used in the command that used to deploy to cluster.

```sh
kind create cluster --config kind-cluster.yaml
```

Once the kind cluster is installed, the kube config will be automatically updated, we could use kubectl get nodes to see the nodes.

<img width="300" height="95" alt="image" src="https://github.com/user-attachments/assets/cf150619-c70b-45c4-9d6f-15c2e7647519" />


#### Certificate manager installation

To install the certificate use below command

```sh
helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.20.2 \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true
```

Once the certificate manager is installed the certificate issuer and certificate request resource is created. The configuration for that would look like below.

These are required to configure the apisix to access the Apisix dashboard

```yaml
---
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: ss-apisix-issuer  #Name will be used in the apisix configuration
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
    name: ss-apisix-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - apisix.demo.com  # dns name add this to hosts file for loopback address
---
```

To install the Apisix resource use below command

```sh
kubectl create ns apisix
kubectl -n apisix apply -f apisix-cert.yaml
```


#### Apisix installation

The Apisix is installed in control-plane and data-plane. The command looks like below.

The certificate secret is passed as argument to the control plane installation. The resources are deployed in apisix namespace.

```sh
helm upgrade --install --create-namespace -n apisix apisix-cp apisix/apisix \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=control_plane \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=ss-apisix-cert-secret \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set apisix.admin.allow.ipList[0]=0.0.0.0/0 \
  --set etcd.replicaCount=3 \
  --set etcd.enabled=true \
  --wait
```

For production the admin value keys create secrets, refer the Apisix documentation. Below command will deploy the data-plane of Apisix. The certificate secret is passed in the arguments.

```sh
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

To access the dashboard from the host machine ApisixTLS and ApisixRoute needs to be deployed, the configuration is shown below

```yaml
# file name: apisix-route-config.yaml
---
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: sample-tls
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
        - name: proxy-rewrite
          enable: true
          config:
            headers:
              set:
                X-Api-Key: "edd1c9f034335f136f87ad84b625c8f1"
---
```

To deploy the Apisix resources (ApisixTls and ApisixRoute), we can use the below command to deploy the configuration to apisix namespace

```sh
kubectl -n apisix apply -f apisix-route-config.yaml
```

Once the resources are deployed, to access the Apisix dashboard, update the hosts file with loopback address. Note, if behind the proxy probably use different dns name that doesn't use `.com`.

```
127.0.0.1 apisix.demo.com
```

Now, from browser we can use `https://apisix.demo.com` to view the dashboard.

#### Seaweed installation

##### Seaweedfs operator installation

 The Seaweedfs operator chart is used to deploy the CRDs. The seaweedfs operator [git repo link](https://github.com/seaweedfs/seaweedfs-operator). Seaweed is configured with the self-signed certificate to access the endpoint.

To install seaweedfs operator use below helm command, add the helm repo using below command

```sh
helm repo add seaweedfs-operator https://seaweedfs.github.io/seaweedfs-operator/
helm repo update
```

Below command will install the operator to the seaweedfs-operator namespace, the CRD's are created by default.

```sh
helm upgrade --install seaweedfs-operator seaweedfs-operator/seaweedfs-operator \
--version 0.1.39 \
--namespace seaweedfs-operator \
--create-namespace
```

##### Seaweedfs certificate installation

To deploy the seaweedfs S3 gateways, we will create a namespace `seaweedfs`. The certificate issuer will be configured in the seaweedfs manifest and the TLS will be enabled. This would allow access S3 and other services of seaweedfs from host using self-signed certificate. Create the namespace using below command.  

```sh
kubectl create ns seaweedfs
```

The certificate issuer manifest configuration is shown below, which uses self-singed certificate.

```yaml
# file name: swfs-issuer.yaml
---
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: ss-sw-issuer
spec:
  selfSigned: {}
---
```

The certificate request which will be configured with the DNS to access the seaweedfs UI's and S3 services will be defined here. Have used singe certificate with multiple DNS names `admin.swfs.com`, `master.swfs.com`, `filer.swfs.com` and `s3.swfs.com`. 

```yaml
# file name: swfs-cert.yaml
---
# deploy in seaweefs namespace
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: ss-sw-crt
spec:
  commonName: admin.swfs.com  
  secretName: ss-sw-cert-secret # SECRET for cert created in this secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: ss-sw-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - admin.swfs.com  # dns name add this to hosts file for loopback address
    - s3.swfs.com
    - filer.swfs.com
    - master.swfs.com
---
```

The Apisix TLS manifest listed below when installed creates the secret with ca certificate info so we can access the endpoints from the host using SSL certificates. If this resource is not created then in Apisix data-plane we could see SNI related error related to the certificate.

```yaml
---
# file name: swfs-tls.yaml
---
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: seaweedfsc1-server-tls
spec:
  ingressClassName: apisix
  hosts:
    - admin.swfs.com
    - s3.swfs.com
    - filer.swfs.com
    - master.swfs.com
  secret:
    name: ss-sw-cert-secret  # SECRET for cert created by the cert-manager
    namespace: seaweedfs

```

To install the issuer and certificate request manifest in seaweedfs namespace use below 

```sh
kubectl -n seaweedfs apply -f swfs-issuer.yaml
kubectl -n seaweedfs apply -f swfs-cert.yaml
kubectl -n seaweedfs apply -f swfs-tls.yaml
```

##### Seaweedfs cluster installation

Service account resource for the `seaweedfs` which will be used to create internal resources. The service account is configured in the resource manifest.

```yaml
#file name: swfs-sa.yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: seaweed1-sa
```

Create the service account in seaweedfs namespace use below command

```sh
kubectl -n seaweedfs apply -f swfs-sa.yaml
```

Below is the seaweedfs cluster configuration, this is just the minimilistic deployment for more details refer the seaweedfs charts.

```yaml
# file name: seaweedfs-cluster.yaml
---
apiVersion: seaweed.seaweedfs.com/v1
kind: Seaweed
metadata:
  name: seaweedfsc1
spec:
  image: chrislusf/seaweedfs:latest
  volumeServerDiskCount: 1

  admin:
    serviceAccountName: seaweed1-sa

  master:
    replicas: 3
    serviceAccountName: seaweed1-sa
    volumeSizeLimitMB: 1024
  volume:
    replicas: 3
    requests:
      storage: 3Gi
    extraArgs:
     - -max=100
  filer:
    replicas: 2
    iam: true
    config: |
      [leveldb2]
      enabled = true
      dir = "/data/filerldb2"

  s3:
    replicas: 1
    serviceAccountName: seaweed1-sa   # CREATE SERVICE ACCOUNT FOR PERMISSION
    loggingArgs:
      - "-v=4"
    iam: true
    env:
     - name: S3_EXTERNAL_URL
       value: "https://s3.swfs.com"
  tls:
    enabled: true
    issuerRef:
       name: ss-sw-issuer
       kind: Issuer             # ClusterIssuer or Issuer, (default is Issuer)
       group: cert-manager.io
```

To install the cluster use below command 

```sh
kubectl -n seaweedfs apply -f seaweedfs-cluster.yaml
```

Once deployed use below command to check the status of deployment.

```sh
kubectl -n seaweedfs get pods
```

##### Seaweedfs Access resources 

To access the S3 service we need to create users and provide permission. With Seaweedfs we can use the S3Creds, S3Policy, S3Identity, S3PolicyBinding, etc. resources to configure the user.

Below configuration, creates user admin and provides read and wirte access. Refer the Seaweedfs documentation for fine grained access on buckets, etc.

Note, the seweedRef property uses the seaweedfs cluster name.

```yaml
# file name: swfs-access.yaml
---
apiVersion: seaweed.seaweedfs.com/v1
kind: S3Credentials
metadata:
  name: admin-creds
spec:
  seaweedRef:
    name: seaweedfsc1
  identityRef:
    name: admin
  secretRef:
    name: admin-s3-secret  # secret name created after deployment with accesskey and secretkey
---
apiVersion: seaweed.seaweedfs.com/v1
kind: S3Identity
metadata:
  name: admin
spec:
  seaweedRef:
    name: seaweedfsc1
  account:
    displayName: Admin
    email: admin@demo.com
---
apiVersion: seaweed.seaweedfs.com/v1
kind: S3Policy
metadata:
  name: rw-uploads
spec:
  seaweedRef:
    name: seaweedfsc1
  statements:
    - effect: Allow
      actions:
        - "*"  # * - allows all action to resources
      resources:
        - "*"
---
apiVersion: seaweed.seaweedfs.com/v1
kind: S3PolicyBinding
metadata:
  name: admin-uploads-binding
spec:
  seaweedRef:
    name: seaweedfsc1
  policyRef:
    name: rw-uploads   # S3Policy name to be bounded to the S3Identity
  subjects:
    - kind: S3Identity
      name: admin
---
```

To deploy the access configuration use below command

```sh
kubectl -n seaweedfs apply -f swfs-access.yaml
```

Once deployed the S3Creds resource will create an secret admin-s3-secret which will include the accesskey and secretkey. This will be used to access the S3 resources.

##### Seaweedfs Apisix Route installation

To access the UI's and S3 Gateway from host machine Apisix Route needs to be created, below is the configuration of the routes.

Note, the ApisixUpstream is created to pass through the headers from the incoming request to the backend, this is necessary in case when using aws cli to access the S3 service. If the Upstream and swfs-s3-route route config headers configuration is not set we could see invalid signature error when accessing the S3 service from host machine. 

```yaml
# file name: swfs-apisix-route.yaml
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: swfs-admin-route
spec:
  ingressClassName: apisix 
  http:
    - name: swfs-admin
      match:
        hosts:
          - admin.swfs.com
        paths:
          - "/*"
      backends:
        - serviceName: seaweedfsc1-admin
          servicePort: 23646
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: swfs-filer-route
spec:
  ingressClassName: apisix 
  http:
    - name: swfs-filer
      match:
        hosts:
          - filer.swfs.com
        paths:
          - "/*"
      backends:
        - serviceName: seaweedfsc1-filer
          servicePort: 8888
---
apiVersion: apisix.apache.org/v2
kind: ApisixUpstream
metadata:
   name: swfs-s3-upstream
spec:
  passHost: pass
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: swfs-s3-route
spec:
  ingressClassName: apisix 
  http:
    - name: swfs-s3
      match:
        hosts:
          - s3.swfs.com
        paths:
          - "/*"
      backends:
        - serviceName: seaweedfsc1-s3
          servicePort: 8333
      plugins:
        - name: proxy-rewrite
          enable: true
          config:
             headers:
               set:
                 X-Forwarded-Host: s3.swfs.com"
                 X-Forwarded-Proto: "https"
                 X-Forwarded-Port: "443"
```

##### Accessing Seaweedfs S3 Gateways service 

To access the UI and the S3 endpoint, update the hosts file to map loopback IP with DNS

```
127.0.0.1 admin.swfs.com master.swfs.com filer.swfs.com s3.swfs.com
```

<img width="2708" height="1670" alt="image" src="https://github.com/user-attachments/assets/ef5f7d0f-eadd-41dc-9e74-c04c3bb6992e" />

<img width="2572" height="934" alt="image" src="https://github.com/user-attachments/assets/a95b1e47-aebc-4d85-9d7b-0e058b907776" />

The s3.swfs.com will be the endpoint we can use with the S3 clients to connect to create and list buckets, resources, etc.

Note, master.swfs.com is not necessary to be exposed, since the service gets created just added a route. 

###### Fetch the certificate from the KinD cluster

To fetch the certificate use the `opensssl` tool, the public certificate will be stored in cert.pem file. The command to fetch the certificate is shown below.

```sh
openssl s_client -connect s3.swfs.com:443 -showcerts </dev/null 2>/dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > cert.pem
```

##### AWS CLI to list the buckets

To list the buckets install the aws cli in the local. To install in WSL2 Ubuntu use `sudo apt-get install -y awscli`

Fetch the accesKey and secretKey from the cluster, then use below command to list the buckets.

When using Git Bash or WSL2, the environment variables can be configured like below, which will fetch the required access and secret key. The METADATA environment is disabled to ignore addition logs that gets printed when using the aws cli command.

```sh
export AWS_ACCESS_KEY_ID=$(kubectl get -n seaweedfs secret admin-s3-secret -o go-template='{{index .data "accessKey" | base64decode}}')
export AWS_SECRET_ACCESS_KEY=$(kubectl get -n seaweedfs secret admin-s3-secret -o go-template='{{index .data "secretKey" | base64decode}}')
export AWS_EC2_METADATA_DISABLED=true
export AWS_ENDPOINT_URL="https://s3.swfs.com"
```

The aws cli command to list the topic is shown below

```sh
aws s3 --ca-bundle cert.pem ls
```

<img width="250" height="38" alt="image" src="https://github.com/user-attachments/assets/90f182eb-ad14-423b-9717-53baa0306e37" />

To create buckets using aws cli use below command

```sh
aws s3 --ca-bundle cert.pem mb s3://test-bucket 
```

<img width="250" height="70" alt="image" src="https://github.com/user-attachments/assets/d83c1d29-f27f-47eb-bac5-39242c6f0a38" />

<img width="2696" height="1678" alt="image" src="https://github.com/user-attachments/assets/e91b673a-0f08-44fb-bf47-07fad0efcfce" />
