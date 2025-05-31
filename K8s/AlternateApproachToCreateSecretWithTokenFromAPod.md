## This blog we will see an alternate option to create secret in a namespace using pods deployed to namespace

### Theory 

-  1. Create an RBAC role with a serviceaccount to create and patch secret 
Note, the `Role` created below is bound to service account named `default` with the `RoleBinding`.
Say, if we are using job deployment to load certs, we can create a dedicated service account with below roles and bind it. This is not demonstrated here.

```yaml
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: secret-creator
rules:
  - apiGroups: [ "" ]
    resources: [ "secrets" ]
    verbs: [ "create", "patch" ]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: certs-creator
subjects:
  - kind: ServiceAccount
    name:  default  # binding to default serviceaccount
roleRef:
  kind: Role
  name: secret-creator
  apiGroup: rbac.authorization.k8s.io
```

- 2. Create a pod using the nginx deployment on a namespace called `test`

```sh
$ kubectl -n test create deployment nginx --image=nginx
``` 

- 3. Exec into the nginx pod

```sh
$ kubectl -n test exec pod/nginx-123131-temp -- bash
```

- 4. In the pod we export the token to an environment variable. 
The token is mounted to pod at `/var/run/secrets/kubernetes.io/serviceaccount/token`.

 - Since we have exec in the pod in previous step, we use below command

```sh
$ export TOKEN_VAL=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token) 
```

- 5. Create a file `custom-secret.json` using the cat command, we can create this in local and use `kubectl cp` command as well.

```sh
/# cat >> custom-secret.json
{
    "apiVersion": "v1",
    "kind": "Secret",
    "data": {
        "user": "test"
    },
    "metadata": {
        "name": "test-cert"
    },
    "type": "Opaque"
}
<ctrl + D>
```

- 6. We use the curl command to post the custom-secret.json as data with the ca.crt and exported token
Note, the certificate of API server is mounted in the pod at `/var/run/secrets/kubernetes.io/serviceaccount/ca.crt`.
The secret is loaded as json format instead of yaml, to the end-point `https://kubernetes.default.svc/api/vi/namespaces/<NAMESPACE>/secrets`

```sh
/# curl --cacert /var/run/secrets/kubernetes.io/serviceaccount/ca.crt --header "Authorization: Bearer ${TOKEN_VAL}" --header "Content-Type: application/json" -X POST https://kubernetes.default.svc/api/v1/namespaces/test/secrets --data "$(cat custom-secret.json)"
```

- 7. once the curl command executed we can check the created secrets

```sh
$ kubectl -n test get secrets
```

------------------

### The above example was derived from the example of job below

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: certs-creator
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: secret-creator
rules:
  - apiGroups: [ "" ]
    resources: [ "secrets" ]
    verbs: [ "create", "patch" ]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: certs-creator
subjects:
  - kind: ServiceAccount
    name:  certs-creator
roleRef:
  kind: Role
  name: secret-creator
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: batch/v1
kind: Job
metadata:
  name: create-certs
spec:
  template:
    spec:
      restartPolicy: OnFailure
      serviceAccountName: certs-creator
      containers:
      - command:
          - /usr/local/bin/bash
          - -ecx
          - |
            apk add openssl curl
            
            export APISERVER=https://kubernetes.default.svc SERVICEACCOUNT=/var/run/secrets/kubernetes.io/serviceaccount
            export NAMESPACE=$(cat ${SERVICEACCOUNT}/namespace) TOKEN=$(cat ${SERVICEACCOUNT}/token) CACERT=${SERVICEACCOUNT}/ca.crt

            function createKey() {
              USER=$1
              openssl genrsa -out ${USER}.key 2048
              echo "created ${USER}.key"
            }

            function createSigningRequest() {
              USER=$1
              openssl req -new -key ${USER}.key -extensions 'v3_req' -out ${USER}.csr -config <(generateServerConfig)
              echo "created ${USER}.csr"
            }

            function generateServerConfig() {
              cat<<EOF
              [req]
              distinguished_name = req_distinguished_name
              x509_extensions = v3_req
              prompt = no
              [req_distinguished_name]
              CN = db-postgresql
              [v3_req]
              keyUsage = keyEncipherment, dataEncipherment
              extendedKeyUsage = serverAuth
              subjectAltName = DNS:postgres,DNS:zitadel,DNS:db-postgresql
            EOF
            }

            function signCertificate() {
              INCSR=$1 OUTCRT=$2 CA_CRT=$3 CA_KEY=$4
              openssl x509 -req -in $INCSR -CA $CA_CRT -CAkey $CA_KEY -CAcreateserial -days 365 -out $OUTCRT -extensions v3_req -extfile <(generateServerConfig)
            }

            function secretJson {
              USER=$1
              cat<<EOF
              {
                "apiVersion": "v1",
                "kind": "Secret",
                "data": {
                  "ca.crt": "$(base64 -w 0 ./ca.crt)",
                  "tls.crt": "$(base64 -w 0 ./${USER}.crt)",
                  "tls.key": "$(base64 -w 0 ./${USER}.key)"
                },
                "metadata": {
                  "name": "${USER}-cert"
                },
                "type": "kubernetes.io/tls"
              }
            EOF
            }

            function createCertSecret {
              USER=$1
              echo "CACERT value: ${CACERT}"
              echo "TOKEN value: ${TOKEN}"
              echo "APISERVER value: ${APISERVER}"
              echo "NAMESPACE value: ${NAMESPACE}"
              curl \
              --cacert ${CACERT} \
              --header "Authorization: Bearer ${TOKEN}" \
              --header "Content-Type: application/json" \
              -X POST ${APISERVER}/api/v1/namespaces/${NAMESPACE}/secrets \
              --data "$(echo -n $(secretJson ${USER}) | tr -d '\n')"
            }

            # Create a CA key and cert for signing other certs
            createKey ca
            openssl req -x509 -new -nodes -key ca.key -days 365 -out ca.crt -subj "/CN=My Custom CA"

            createKey postgres
            createSigningRequest postgres
            signCertificate postgres.csr postgres.crt ca.crt ca.key
            createCertSecret postgres

            createKey zitadel
            createSigningRequest zitadel
            signCertificate zitadel.csr zitadel.crt ca.crt ca.key
            createCertSecret zitadel
        image: bash:5.2.15
        imagePullPolicy: IfNotPresent
        name: create-certs
```