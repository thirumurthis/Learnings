
--to install cert manager

helm install \
  cert-manager oci://quay.io/jetstack/charts/cert-manager \
  --version v1.20.2 \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true

-- apisix deployment 
 
helm upgrade --install --create-namespace -n apisix apisix-cp apisix/apisix \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=control_plane \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=selfsigned-apisix-cert-secret \
  --set apisix.ssl.certCAFilename=ca.crt \
  --set apisix.admin.allow.ipList[0]=0.0.0.0/0 \
  --set etcd.replicaCount=3 \
  --set etcd.enabled=true \
  --wait
 
--additional - 
#  --set apisix.ssl.fallbackSNI=apisix.demo.com \
 
-- used this for data_plane 
 
helm upgrade  --install apisix-dp \
  --namespace apisix \
  --create-namespace \
  --set apisix.deployment.mode=decoupled \
  --set apisix.deployment.role=data_plane \
  --set apisix.nginx.logs.enableAccessLog=true \
  --set apisix.nginx.logs.errorLogLevel=warn \
  --set apisix.admin.enabled=false \
  --set apisix.ssl.enabled=true \
  --set apisix.ssl.existingCASecret=selfsigned-apisix-cert-secret \
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

#  --set apisix.ssl.fallbackSNI=apisix.demo.com \

port forward 
curl -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1" http://localhost:9999/apisix/admin/ssls

  
kubectl -n apisix apply -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/apisix_https.yaml

after deploying try to use http: url http://apisix.demo.com/ui


-- below is not requiired since the configuration added under ssl chart
use admin to load the cert fetched from secret

cd /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/tmp_cert

curl http://127.0.0.1:9999/apisix/admin/ssls/1 \
-H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f1" -X PUT -d '
{
     "cert" : "'"$(cat tls.crt)"'",
     "key": "'"$(cat tls.key)"'",
     "snis": ["apisix.demo.com"]
}'

------------

apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: apicurio-ui
spec:
  ingressClassName: apisix 
  http:
    - name: apicurio-ui-http
      match:
        hosts:
          - registry.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: sample-ui-service
          servicePort: 8080
---
apiVersion: apisix.apache.org/v2
kind: ApisixRoute
metadata:
  name: apicurio-app
spec:
  ingressClassName: apisix 
  http:
    - name: apicurio-app-http
      match:
        hosts:
          - registry.demo.com
        paths:
          - "/*"
      backends:
        - serviceName: sample-app-service
          servicePort: 8080
---

kubectl apply -n apicurio-registry -f /mnt/c/thiru/edu/gitsource/Learnings/kafka/strimzi-kafka-kraft/apicurio_route.yaml
---


apicurio config


#https://www.apicur.io/registry/docs/apicurio-registry/3.2.x/getting-started/assembly-operator-config-reference.html#operator-ingress-reference_registry
apiVersion: registry.apicur.io/v1
kind: ApicurioRegistry3
metadata:
  name: sample
spec:
  app:
    env:
    - name: QUARKUS_HTTP_CORS_ORIGINS
      value: "http://localhost:8888,http://localhost:8080,http://127.0.0.1:8888,http://127.0.0.1:8080"
    - name: QUARKUS_HTTP_CORS
      value: "true"
    - name: QUARKUS_LOG_CONSOLE_JSON
      value: "true"
    ingress:
      enabled: false
    #  host: simple-app.apps.cluster.example:8080 #simple-app-svc.apicurio-registry.svc.cluster-domain.example #simple-ui-service.
  ui:
   env:
    - name: REGISTRY_API_URL
      #value: "sample-app-service.apicurio-registry:8080/apis/registry/v3" #localhost:8099 #sample-app-service.apicurio-registry.svc:8080
      value: "http://localhost:8080/apis/registry/v3"
    - name: QUARKUS_HTTP_CORS_ORIGINS
      value: "http://localhost:8888,http://localhost:8080,http://127.0.0.1:8888,http://127.0.0.1:8080"
    - name: QUARKUS_HTTP_CORS
      value: "true"
    #- name: QUARKUS_LOG_CONSOLE_JSON
    #  value: "true"
   ingress:
    enabled: false
    #host: simple-ui.apps.cluster.example



To acces the apicurio registry https create the keystore
-- from browser exporte the certificate 

keytool -import -file browserCertExport.crt -alias root-ca -keystore truststore.jks -storepass secret -trustcacerts -noprompt


additionalProperties (common)

Sets additional properties for either kafka consumer or kafka producer in case they can’t be set directly on the camel configurations 
(e.g.: new Kafka properties that are not reflected yet in Camel configurations), 
the properties have to be prefixed with additionalProperties..,
 e.g.: additionalProperties.transactional.id=12345&additionalProperties.schema.registry.url=http://localhost:8811/avro. 
 If the properties are set in the application.properties file, 
 they must be prefixed with camel.component.kafka.additional-properties and the property enclosed in square brackets,
 like this example: camel.component.kafka.additional-propertiesdelivery.timeout.ms=15000.
 This is a multi-value option with prefix: additionalProperties.

------------------
java camel code with Apiregistry deployed
Note the pem certificate ca.crt fomr the secret needs to be copied and update the ca cert

For slf4j logs enable the simple jar  to print the logs
For certificate pikx error make sure the latest 3.2.1 deserialize rversion