## Configure Zitadel in KIND with https/tls

1. With the Apisix deployed as in the Apisix_with_https_url.md, we need to follow the same steps.
Note, we won't use Kuberentes Gateway Api in this example as well

Deploy the certificate job for zitadel

- Note the path is in reference to local area

```bash
kubectl apply -n zitadel -f ./zitadel/certs-job.yaml
```

```yaml
# certs-job.yaml
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

2. Deploy postgres

- with reference to `cd ./zitadel/postgresql` chart downloaded locally
```bash
helm upgrade --install --wait db -f ../postgres-values.yaml . \
     --create-namespace \
     --namespace zitadel
```

```yaml
# postgres-values.yaml
volumePermissions:
  enabled: true
tls:
  enabled: true
  certificatesSecret: postgres-cert
  certFilename: "tls.crt"
  certKeyFilename: "tls.key"
auth:
  postgresPassword: "abc"
```

3. Deploy zitadel

- with reference to `cd ./zitadel/zitadel` chart downloaded locally
```bash
  helm upgrade --install zitadel -f ../zitadel-values-tls.yaml . \
   --set zitadel.configmapConfig.ExternalDomain=zitadel.demo.com \
   --set zitadel.configmapConfig.ExternalPort=80 \
   --set service.port=80 \
   --create-namespace \
   --namespace zitadel
```
- below configuration creates machine user 
```yaml
# zitadel-values-tls.yaml
zitadel:
  masterkey: x123456789012345678901234567891y
  #selfSignedCert:
  #  enabled: true
  configmapConfig:
    ExternalSecure: true
    # modify the external domain to localhost or any domain exists
    ExternalDomain: localhost
    # add this configuration since we have ingress
    ExternalPort: 443
    # https://github.com/zitadel/zitadel/blob/main/cmd/setup/steps.yaml
    FirstInstance:
      DomainPolicy:
        UserLoginMustBeDomain: false # ZITADEL_DEFAULTINSTANCE_DOMAINPOLICY_USERLOGINMUSTBEDOMAIN
        ValidateOrgDomains: false # ZITADEL_DEFAULTINSTANCE_DOMAINPOLICY_VALIDATEORGDOMAINS
        SMTPSenderAddressMatchesInstanceDomain: false # ZITADEL_DEFAULTINSTANCE_DOMAINPOLICY_SMTPSENDERADDRESSMATCHESINSTANCEDOMAIN
      LoginPolicy:
          AllowUsernamePassword: true # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_ALLOWUSERNAMEPASSWORD
          AllowRegister: true # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_ALLOWREGISTER
          AllowExternalIDP: true # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_ALLOWEXTERNALIDP
          ForceMFA: false # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_FORCEMFA
          HidePasswordReset: false # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_HIDEPASSWORDRESET
          IgnoreUnknownUsernames: false # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_IGNOREUNKNOWNUSERNAMES
          AllowDomainDiscovery: true # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_ALLOWDOMAINDISCOVERY
          # 1 is allowed, 0 is not allowed
          PasswordlessType: 1 # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_PASSWORDLESSTYPE
          # DefaultRedirectURL is empty by default because we use the Console UI
          DefaultRedirectURI: # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_DEFAULTREDIRECTURI
          # 240h = 10d
          PasswordCheckLifetime: 240h # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_PASSWORDCHECKLIFETIME
          # 240h = 10d
          ExternalLoginCheckLifetime: 240h # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_EXTERNALLOGINCHECKLIFETIME
          # 720h = 30d
          MfaInitSkipLifetime: 720h # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_MFAINITSKIPLIFETIME
          SecondFactorCheckLifetime: 18h # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_SECONDFACTORCHECKLIFETIME
          MultiFactorCheckLifetime: 12h # ZITADEL_DEFAULTINSTANCE_LOGINPOLICY_MULTIFACTORCHECKLIFETIME
      Org:
        Machine:
          Machine:
            Username: sysuser1 # ZITADEL_DEFAULTINSTANCE_ORG_MACHINE_MACHINE_USERNAME
            Name: sysuser1 # ZITADEL_DEFAULTINSTANCE_ORG_MACHINE_MACHINE_NAME
          MachineKey:
            # date format: 2023-01-01T00:00:00Z
            # ExpirationDate: # ZITADEL_DEFAULTINSTANCE_ORG_MACHINE_MACHINEKEY_EXPIRATIONDATE
            # Currently, the only supported value is 1 for JSON
            Type: 1 # ZITADEL_DEFAULTINSTANCE_ORG_MACHINE_MACHINEKEY_TYPE
      PrivacyPolicy:
        TOSLink: "" # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_TOSLINK
        PrivacyLink: "" # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_PRIVACYLINK
        HelpLink: "" # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_HELPLINK
        SupportEmail: "" # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_SUPPORTEMAIL
        DocsLink: https://zitadel.com/docs # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_DOCSLINK
        CustomLink: "" # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_CUSTOMLINK
        CustomLinkText: "" # ZITADEL_DEFAULTINSTANCE_PRIVACYPOLICY_CUSTOMLINKTEXT
      # WebKeys configures the OIDC token signing keys that are generated when a new instance is created.
      WebKeys:
        Type: "rsa" # ZITADEL_DEFAULTINSTANCE_WEBKEYS_TYPE
        Config:
        RSABits: "2048" # ZITADEL_DEFAULTINSTANCE_WEBKEYS_CONFIG_BITS
        RSAHasher: "sha256" # ZITADEL_DEFAULTINSTANCE_WEBKEYS_CONFIG_HASHER  
    Machine:
      # Cloud-hosted VMs need to specify their metadata endpoint so that the machine can be uniquely identified.
      Identification:
        # Use private IP to identify machines uniquely
        PrivateIp:
          Enabled: true # ZITADEL_MACHINE_IDENTIFICATION_PRIVATEIP_ENABLED
        # Use hostname to identify machines uniquely
        # You want the process to be identified uniquely, so this works well in k8s where each pod gets its own
        # unique hostname, but not as well in some other hosting environments.
        Hostname:
          Enabled: false # ZITADEL_MACHINE_IDENTIFICATION_HOSTNAME_ENABLED
    Log:
      Level: debug # ZITADEL_LOG_LEVEL
      Formatter:
        Format: text # ZITADEL_LOG_FORMATTER_FORMAT  
    TLS:
      Enabled: false
    Database:
      Postgres:
        Host: db-postgresql
        Port: 5432
        Database: zitadel
        MaxOpenConns: 20
        MaxIdleConns: 10
        MaxConnLifetime: 30m
        MaxConnIdleTime: 5m
        User:
          Username: zitadel
          SSL:
            Mode: verify-full
        Admin:
          Username: postgres
          SSL:
            Mode: verify-full
  secretConfig:
    Database:
      Postgres:
        User:
          Password: xyz
        Admin:
          Password: abc

  dbSslCaCrtSecret: postgres-cert
  dbSslAdminCrtSecret: postgres-cert
  dbSslUserCrtSecret: zitadel-cert

replicaCount: 1
```

4. create certificate for zitadel namespace with the issuer

```
kubectl -n zitadel apply -f zitadel_cert_issuer.yaml
```

```yaml
# zitadel_cert_issuer.yaml
# deploy in zitadel namespace
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-zitadel-ca-issuer
spec:
  selfSigned: {}
---
# deploy in zitadel namespace
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-zitadel-cert
spec:
  commonName: zitadel.demo.com  
  secretName: selfsigned-zitadel-cert-secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-zitadel-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - zitadel.demo.com  # dns name add this to hosts file for loopback address
---
```

5. Create ApisixTLS 

```bash
kubectl -n zitadel apply -f zitadel_apisix_tls.yaml
```

```yaml
# zitadel_apisix_tls.yaml
# tls.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixTls
metadata:
  name: zitadel-tls
spec:
  hosts:
    - zitadel.demo.com
  secret:
    name: selfsigned-zitadel-cert-secret
    namespace: zitadel
```

6. Create Apisix Route for zitadel ui

```bash
kubectl -n zitadel apply -f zitadel_apisix_route.yaml
```

```yaml
# zitadel_apisix_route.yaml
# route.yaml
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: zitadel-route
spec:
  http:
    - name: zitadel-ui
      match:
        hosts:
          - zitadel.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: zitadel
          servicePort: 80
```

7. Add an entry in the hosts file like `127.0.0.1 zitadel.demo.com`
8. From browser issue `https://zitadel.demo.com` should see the zitadel console page prompting for password.
Note, the user name is `zitadel-admin@zitadel.zitadel.demo.com`, change the default password `Password1!`.

