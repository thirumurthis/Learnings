### ArgoCD application in any namespace


In this blog details ArgoCD installation in KIND cluster using cert manager. This provides a local environment for learning and not to be used as such in production.

In this blog the focus is to utilize the ArgoCD application in any namespace feature. ArgoCD can manage any namespace other than the default argocd.
This feature is not explictly enabled.

Pre-requisites:
To follow along we require below items to be installed 

- Docker desktop
- KIND CLI
- Helm CLI
- Kubectl CLI
- ArgoCD CLI

Cert manager, Kubernetes Gateway and Apisix are installed in order to access the ArgoCD from the host machine.
Basic understanding of Cert manager and Apisix will help understand the resource details, but it is not mandatory to try on local Kind cluster.

#### ArgoCD to manage application in any namespace
The application in any namespace feature in ArgoCD works only when ArgoCD is installed at Cluster-scope level.

#### Configuration

1. The resource tracking method configuration in the argocd-cm config map should be updated either to `annotation` or `annotation+label`. 
Refer [resource tracking method](https://github.com/thirumurthis/Learnings/blob/master/argocd/Manage_multi_env_with_argocd_apps_in_any_namespace.md#:~:text=resource%20tracking%20method) from ArgoCD documentation. The configuration would look like below.

```yaml
# argocd-cm-patch.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-cm
data:
  application.resourceTrackingMethod: annotation
```

2. The namespace to be controlled by Argocd should be configured in the `application.namespaces` property in argocd-cmd-params-cm config map. The configuration can be wild-chard as well. The configuration will be like below.

```yaml
# argocd-cmd-params-cm-patch.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-cmd-params-cm
data:
  application.namespaces: env-*
  # since cert-manager is used the insecure access in enabled
  server.insecure: "true"
```

Note :- The `application.namespaces` configured with `env-*` in the remote cluster we will be creating the namespace like `env-dev-0`, `env-dev-1`, `env-test-0`, etc.

3. Configure the RBAC permissions in the cluster. The example from argocd project is used, [argoproj/argo-cd/examples](https://github.com/argoproj/argo-cd/tree/master/examples/k8s-rbac/argocd-server-applications)
The RBAC is configured in the kustomization manifest which will be installed during ArgoCD deployment.  


#### Implementation details

- Create an ArgoCD AppProject resource with the `spec.sourceNamespaces` property with namespace the argocd will be managing in the remote server.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: app-in-any-ns-appproject
  namespace: argocd
spec:
  description: "parent argocd application project"
  destinations:
  # If namespace namespace and server NOT set to * currently seeing an issue when deploying argocd application resources
  # The application destination server 'https://172.19.0.4:6443' and namespace 'env-dev-0' 
  # do not match any of the allowed destinations in project 'app-in-any-ns-appproject'
  - namespace: '*'
    server: '*'
  # Allow manifests to deploy from any Git repos
  # * - refers any repo
  sourceRepos:
   - '*'
  sourceNamespaces:
   - env-*   # list of namespace argocd will be managing
  clusterResourceWhitelist: # configure which resource argocd to maintain
   - group: '*'
     kind: '*'
```


- Create the ArgoCD application with the cluster destination and code source the manifest to be deployed.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: app-in-any-ns-app-1
  namespace: argocd
spec:
  project: app-in-any-ns-appproject
  source:
    repoURL: https://github.com/thirumurthis/argocd-app.git
    targetRevision: HEAD
    path: app-in-any-ns/my-backend-app/
  destination:
    server: https://172.19.0.3:6443  # fetched using kubectl get ep from target cluster (https://github.com/argoproj/argo-cd/issues/4204)
    namespace: env-dev-0
```

The ArgoCD manifest are deployed to KIND cluster using Kustomize. The Kustomize manifest will be

```yaml
# kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: argocd
# refer https://argo-cd.readthedocs.io/en/stable/operator-manual/installation/ for git url with
# specific version
resources:
  - https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
  - https://github.com/argoproj/argo-cd/examples/k8s-rbac/argocd-server-applications?ref=stable
patches:
  - path: argocd-cm-patch.yaml
  - path: argocd-cmd-params-cm-patch.yaml
```

Note:- 
   - Copy above code snippets to file name - kustomization.yaml, argocd-cm-patch.yaml and argocd-cmd-params-cm-patch.yaml place under argocd_install.
   - We will use kustomize to install argocd.
   - The patches property includes config map patch during ArgoCD installation the configuration required for application in any namespace will be updated automatically.

### Creating the KIND cluster

- Kind configuration to install ArgoCD cluster

```yaml
# kind_argocd_config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: argocd-local
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30080  # traffic flow via APISIX gateway 
    hostPort: 80
    protocol: TCP
  - containerPort: 30443  # traffic flow via APISIX gateway
    hostPort: 443
    protocol: TCP
- role: worker  # confifgured but worker node not utlized
```

### Create the Kind cluster, ensure the Docker Desktop is running 

```
kind create cluster --config kind_argocd_config.yaml
```

### Install Kuberentes Gateway to the cluster

```
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.2.0/standard-install.yaml
```

### Install cert manager to the Kind cluster
```
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml
```

### Install ArgoCD to the Kind cluster
- The kustomize configurations and the patch files are stored in the argocd_install folder

```
kubectl create ns argocd
kubectl apply -n argocd  -k argocd_install/kustomize/
```

### To deploy APISIX we use helm command

```
helm repo add apisix https://charts.apiseven.com

helm repo add bitnami https://charts.bitnami.com/bitnami

helm repo update

kubectl create ns ingress-apisix

helm upgrade -i apisix apisix/apisix --namespace ingress-apisix \
--set service.type=NodePort \
--set service.http.enabled=true \
--set service.http.servicePort=80 \
--set service.http.containerPort=9080 \
--set service.http.nodePort=30080 \
--set service.tls.servicePort=443 \
--set service.tls.nodePort=30443 \
--set dashboard.enabled=true \
--set ingress-controller.enabled=true \
--set ingress-controller.config.apisix.serviceNamespace=ingress-apisix \
--set ingress-controller.config.kubernetes.enableGatewayAPI=true
```

### Self signed certificate for accessing the ArgoCD UI

- We are using self singed certificate from the certificate manager.
- Once the cert manager is installed and the pods are in ready and running state, create issuer and certificate resource in the argocd namespace
- We are not using cluster issuer.

```yaml
# argocd_issuer.yaml
# deploy in argocd namespace
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-argocd-ca-issuer
spec:
  selfSigned: {}
```

- Create certificate 

```yaml
# argocd_certificate.yaml
# deploy in argocd namespace
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-argocd-cert
spec:
  commonName: argocd.demo.com  
  secretName: selfsigned-argocd-cert-secret
  duration: 2160h
  renewBefore: 360h
  issuerRef:
    name: selfsigned-argocd-ca-issuer # issuer resource name
    kind: Issuer
  dnsNames:
    - argocd.demo.com  # dns name add this to hosts file for loopback address
```

- To deploy the issuer and certificate use below command

```
kubectl -n argocd apply -f argocd_issuer.yaml
kubectl -n argocd apply -f argocd_certificate.yaml
```

### Create ingress with Apisix to access Argocd UI 
- The backend service is configured to argocd-server port 443

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: argocd-ingress
  annotations:
    # add an annotation indicating the issuer to use.
    cert-manager.io/issuer: "selfsigned-argocd-ca-issuer"
    # nginx server config for redirect
    nginx.ingress.kubernetes.io/ssl-passthrough: "true"
    nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    # redirect loop err_too_many_redirects fix
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  # apisix-ingress-controller is only interested in Ingress
  # resources with the matched ingressClass name, in our case,
  # it's apisix.
  ingressClassName: apisix
  tls:
    - hosts:
        - argocd.demo.com
      secretName: argocd-cert-manager-tls # cert-manager will store the created certificate in this secret.
  rules:
  - host: argocd.demo.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: argocd-server
            port:
              number: 443
```

#### ArgoCD UI access

- The Apisix ingress is configured with comman name `argocd.demo.com` which needs to be added to the hostfile

```
127.0.0.1 argocd.demo.com
```

From browser the `http://argocd.demo.com` will redirect to `https`, since we are using self signed certificate allow unverified access in browser.
Login with the username admin and for password use below command. The password can be updated using ArgoCD UI settings.

```
kubectl  -n argocd get secret/argocd-initial-admin-secret -ojsonpath={'.data.password'} | base64 -d; echo
```

![image](https://github.com/user-attachments/assets/56f639a7-51ea-4dde-a399-46af710fd0fa)


- Adding Git repo using git repo
Make sure to login to argocd server with argocd cli using `argocd login argocd.demo.com`.
The git repo with the manifest created [argocd-app](https://github.com/thirumurthis/argocd-app/tree/main). 


- Create a target Kind cluster with below configuration for ArgoCD to manage 

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: dev-env-0
   extraPortMappings:
   - containerPort: 80
     hostPort: 9005
     protocol: TCP
   - containerPort: 443
     hostPort: 9006
     protocol: TCP
```

- Create the namespace `env-dev-0` in the remote cluster, so when the ArgoCD application is created in the argocd server the deploy the application in the remote cluster.
