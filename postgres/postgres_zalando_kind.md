
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

```sh

# add repo for postgres-operator-ui
helm repo add postgres-operator-ui-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator-ui

# install the postgres-operator-ui
helm install postgres-operator-ui postgres-operator-ui-charts/postgres-operator-ui -n postgres-op
```

- check the status 

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

- Database manifest

```yaml
# postgresql.yaml
apiVersion: "acid.zalan.do/v1"
kind: postgresql
metadata:
  name: acid-minimal-cluster
spec:
  teamId: "acid"
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
```


- The localhost `https://postgres.demo.com`, postgres ui

![image](https://github.com/user-attachments/assets/79e427c1-fae8-4356-9c70-e752f05d95e9)


Document for manifest - https://github.com/zalando/postgres-operator/blob/master/manifests/minimal-postgres-manifest.yaml

https://adrian-varela.com/postgres-operator/

example:
https://github.com/zalando/postgres-operator/issues/1466 = chart config

copy the secret of minio tls


Refering - https://github.com/zalando/postgres-operator/issues/845
adding the ca.crt to mount
```
$$$ NOTE BELOW DIDN'T WORK since there was certificate error
ssl.SSLCertVerificationError: [SSL: CERTIFICATE_VERIFY_FAILED] certificate verify failed: self-signed certificate (_ssl.c:1007)
```

```sh
kubectl get secrets app-minio-tls -o yaml -n tenant-0| sed "s/namespace: .*/namespace: postgres-op/" | kubectl apply -f -
```

using the tenant-o tls itself part of the minio deployment - tls.crt mount didn't work

```sh
kubectl get secrets tenant-0-ca-tls -o yaml -n tenant-0| sed "s/namespace: .*/namespace: postgres-op/" | kubectl apply -f -
```

create secert for minio access credentials

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: s3-minio-secret
data:
  username: minio
  credential: minio123
```

```sh
kubectl -n postgres-op apply -f postgres-cluster_bckp.yaml
```

Note, the Minio url `"https://minio.tenant-0.svc.cluster.local:443"` note the app-minio-tls cert has the dns list confifgured.

- For now the `pgdb-bckp` bucket is created using the mc client.


- With the above configuration we have created

```
k -n postgres-op exec -it  pod/acid-minimal-cluster-1 -- bash
root@acid-minimal-cluster-1:/home/postgres# psql -d foo -U zalando
psql (17.2 (Ubuntu 17.2-1.pgdg22.04+1))
Type "help" for help.

foo=#CREATE TABLE links (
  id SERIAL PRIMARY KEY,
  url VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR (255),
  last_update DATE
);
CREATE TABLE
foo=# select * from links;
id | url | name | description | last_update
----+-----+------+-------------+-------------
(0 rows)

foo=# INSERT INTO links (url, name)
VALUES('https://neon.com/postgresql','PostgreSQL Tutorial');
INSERT 0 1
```

logs:

```
2025-06-28 08:51:46 UTC [86]: [3] 685facd5.56 checkpointer   0  00000 DEBUG:  performing replication slot checkpoint
wal_e.worker.upload INFO     MSG: begin archiving a file
        DETAIL: Uploading "pg_wal/000000010000000000000001" to "s3://pgdb-bckp/spilo/postgres-op-tstdb-cluster/wal/17/wal_005/000000010000000000000001.lzo".
        STRUCTURED: time=2025-06-28T08:51:46.470146-00 pid=185 action=push-wal key=s3://pgdb-bckp/spilo/postgres-op-tstdb-cluster/wal/17/wal_005/000000010000000000000001.lzo prefix=spilo/postgres-op-tstdb-cluster/wal/17/ seg=000000010000000000000001 state=begin
2025-06-28 08:51:47 UTC [87]: [5] 685facd5.57 background writer   0  00000 DEBUG:  snapshot of 0+0 running transaction ids (lsn 0/2000080 oldest xid 956 latest complete 955 next xid 956)
2025-06-28 08:51:47 UTC [92]: [1] 685facd5.5c walwriter   0  00000 DEBUG:  creating and filling new WAL file
2025-06-28 08:51:47 UTC [92]: [2] 685facd5.5c walwriter   0  00000 DEBUG:  done creating and filling new WAL file
```