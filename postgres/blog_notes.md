### Installing Postgres DB with Backup configured in minio in KIND cluster

- This article have included the information to install Postgres db in KinD cluster. The configuration enabled backup and to back the Postgres DB we use Minio (s3 compliant) service.

- In order to access the Minio from host machine with self-signed certificate we also install Cert manager and Apisix. the Apisix acts as a Gateway API. 


- The Postgres operator UI is also deployed but it is not used for cluster creation, we use the manfiest to install the Postgres DB cluster in KIND.


Pre-requisities:
  - Docker Desktop
  - KIND CLI
  - Helm CLI
  - Kubectl CLI


#### Summary
  - Create Kind Cluster
  - Deploy the cert manager
  - Deploy the Apisix 
  - Deploy the Minio
  - Deploy the Postgres 


### KIND cluster

- Below is the configuration of KinD cluster used, 

```yaml
# file name: kind-cluster.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: local-cluster
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

With the Docker Desktop running with kind cli we can crewate the clustrer using below command

```sh
kind create cluster --config kind-cluster.yaml
```

## Deploy the cert manager

- Below command will install cert-manager, we will use self-signed certificate to access the APISIX, Minio, postgres-operator UI with https url

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.17.2/cert-manager.yaml
```

### Deploy the apisix

Install the `apisx` using helm chart in `apisix`namespace. The `serviceNamespace` should be same as the namespace unless using different one. Most of the configuration is default we enabled the `apisix.ssl.enabled` in the chart.

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

Install Issuer and Certificate in the apisix namespace

```yaml
# file name: 1_apisix_cert_issuer.yaml
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
- Apply the above manfiest using below command in apisix namespace

```bash
kubectl -n apisix apply -f 1_apisix_cert_issuer.yaml
```

 Create `ApisixTls` and `ApisixRoute` 

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

```bash
kubectl -n apisix apply -f 3_dashboard_apisix_route.yaml
```


 - To access the apisix dashboard we have to configure the hosts file in windwos, Add the entry `127.0.0.1 apisix.demo.com` in the hostname.

 - Now from the browser we can issue `https://apisix.demo.com` should see insecure access from browser accepting the risk button should see the apisix dashboard login page.

Note, if the `ApisixTls` resource is `not` installed then should see below message in the apisix pod logs

```
http_ssl_client_hello_phase(): failed to match any SSL certificate by SNI: apisix.demo.com, context: ssl_client_hello_by_lua*, client: 10.244.0.1, server: 0.0.0.0:9443
```

### Deploy minio

 - Refer previous article [Minio in KinD with Cert-Manager](https://thirumurthi.hashnode.dev/minio-in-kind-cluster-with-cert-manager)


 ### Deploy postgres

 Installing operator

```sh
helm repo add postgres-operator-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator
```


```sh
kubectl create ns postgres-op
```

```sh
# install the postgres-operator
helm install postgres-operator postgres-operator-charts/postgres-operator -n postgres-op
```

```sh
 kubectl --namespace=postgres-op get pods -l "app.kubernetes.io/name=postgres-operator"
```

Installing the operator ui

```sh
# add repo for postgres-operator-ui
helm repo add postgres-operator-ui-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator-ui
```

```sh
# install the postgres-operator-ui
helm install postgres-operator-ui postgres-operator-ui-charts/postgres-operator-ui -n postgres-op
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

#### Deploy the cluster using manfiest

- For postgres to access the Minio the Minio tls cert is copied to the postgres namespace

```sh
kubectl get secrets app-minio-tls -o yaml -n tenant-0| sed "s/namespace: .*/namespace: postgres-op/" | kubectl apply -f -
```

- The backup configuration is configured with environemnt variables in the manfiest. Also the configuration is not production ready in terms of DB user and password. Refer to the documentation for more details.

```yaml
apiVersion: "acid.zalan.do/v1"
kind: postgresql
metadata:
  name: pgdb-cluster
spec:
  teamId: "pgdb-cluster"
  volume:
    size: 1Gi
  numberOfInstances: 2
  users:
    zalando:  # database owner
    - superuser
    - createdb
    foo_user: []  # role for application foo
  databases:
    foo: zalando  # dbname: owner
  preparedDatabases:
    bar: {}
  postgresql:
    version: "17"
    parameters:
      max_connections: "50"
      max_standby_archive_delay: "-1"
      max_standby_streaming_delay: "-1"
      #http://postgresql.org/docs/current/runtime-config-logging.html
      logging_collector: "off"
      log_checkpoints: "off"
      archive_timeout: "1500s"
      # DEBUG5, DEBUG4, DEBUG3, DEBUG2, DEBUG1, INFO, NOTICE, WARNING, ERROR, LOG, FATAL, and PANIC.
      log_min_messages: debug2
      log_min_error_statement: debug1
      # https://www.postgresql.org/docs/current/runtime-config-logging.html#GUC-LOG-LINE-PREFIX
      log_line_prefix: "%t [%p]: [%l] %c %b %u %h %x %a %e "
      pgaudit.log: "write, ddl, role" #pgaudit.log = 'read, write, ddl, role' 
      #pgaudit.log_level: "log" #default
      pgaudit.log_relation: "on"
      shared_preload_libraries: "bg_mon,pg_stat_statements,pgextwlist,pg_auth_mon,set_user,timescaledb,pg_cron,pg_stat_kcache,pgaudit"
  # https://github.com/zalando/postgres-operator/issues/2197
  # https://github.com/patroni/patroni/blob/master/docs/replica_bootstrap.rst#standby-cluster
  # https://github.com/zalando/postgres-operator/issues/1391
  # env - https://github.com/zalando/spilo/blob/master/ENVIRONMENT.rst
  env:
   - name: AWS_ACCESS_KEY_ID
     valueFrom:
       secretKeyRef:
         name: s3-minio-secret
         key: username
   - name: AWS_SECRET_ACCESS_KEY
     valueFrom:
       secretKeyRef:
         name: s3-minio-secret
         key: credential
   - name: AWS_REGION
     value: "us-east-1"  # check minio default values yaml
   - name: AWS_ENDPOINT
     value: "https://minio.tenant-0.svc.cluster.local:443"
   # https://postgres-operator.readthedocs.io/en/latest/administrator/#wal-archiving-and-physical-basebackups
   - name: USE_WALG_BACKUP  #setting this will override use of both AWS_ENDPOINT and AWS_REGION
     value: "true"
   - name: WAL_S3_BUCKET
     value: pgdb-bckp
   - name: WALG_S3_PREFIX
     value: s3://pgdb-bckp/backups
   - name: BACKUP_SCHEDULE
     value: "*/5 * * * *"
     #value: "2023-01-29T10:04:06+00:00"
   - name: AWS_S3_FORCE_PATH_STYLE
     value: "true"
   - name: BACKUP_NUM_TO_RETAIN
     value: "2"
   - name: WALG_S3_CA_CERT_FILE
     value: /tmp/crt/ca.crt
  # https://github.com/zalando/postgres-operator/blob/master/charts/postgres-operator/crds/postgresqls.yaml
  # use kubectl explain
  patroni:
    synchronous_mode: true
    synchronous_mode_strict: true
  additionalVolumes:
    - mountPath: /tmp/crt/ca.crt
      subPath: ca.crt
      name: app-minio-tls # tenant-0-ca-tls 
      targetContainers:
        - postgres
      volumeSource:
        secret:
          secretName: app-minio-tls # tenant-0-ca-tls 
  resources:
     limits:
       cpu: "1"
       memory: "3G"
     requests:
       cpu: 150m
       memory: 500Mi
```


- To restore we can use different database name and the configuration looks like below. Note the timestamp in the clone which is important to be updated with the closest backup time stamp.

```yaml
apiVersion: "acid.zalan.do/v1"
kind: postgresql
metadata:
  name: pgdb-restore-cluster #to clone to new instance of the db provide different name
spec:
  teamId: "pgdb-3-cluster"
  volume:
    size: 1Gi
  numberOfInstances: 2
  env:
    - name: CLONE_AWS_ACCESS_KEY_ID
      valueFrom:
        secretKeyRef:
            name: s3-minio-secret
            key: username
    - name: CLONE_AWS_SECRET_ACCESS_KEY
      valueFrom:
        secretKeyRef:
            name: s3-minio-secret
            key: credential
    - name: CLONE_AWS_ENDPOINT
      value: "https://minio.tenant-0.svc.cluster.local:443"
    - name: WAL_S3_BUCKET
      value: pgdb-bckp
    - name: CLONE_AWS_REGION
      value: "us-east-1"
    - name: CLONE_WALG_S3_PREFIX
      value: "s3://pgdb-bckp/backups"
    - name: CLONE_AWS_S3_FORCE_PATH_STYLE
      value: "true"
    - name: CLONE_USE_WALG_RESTORE
      value: "true"
    - name: WALG_DISABLE_HISTORY_FETCH
      value: "true"
    - name: CLONE_WALG_s3_CA_CERT_FILE
      value: "/tmp/crt/ca.crt"

  clone:
    cluster: pgdb-3-cluster #posrgres cluster db name that the backup was enabled
    timestamp: 2025-05-27T00:00:23+00:00  # the timestamp closes fetched using backup_list of wal command
```