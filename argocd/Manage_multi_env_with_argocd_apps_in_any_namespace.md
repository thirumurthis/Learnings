### ArgoCD application in any namespace


In this blog details ArgoCD installation in KIND cluster using cert manager. This provides a local environment for learning and not to be used as such in production.

In this blog the focus is to utilize the ArgoCD application in any namespace feature. ArgoCD can manage any namespace other than the default argocd.
This feature is not explictly enabled.


To follow along we require below items to be installed 

- Docker desktop
- KIND CLI
- Helm CLI
- Kubectl CLI


The application in any namespace feature in ArgoCD works only when ArgoCD is installed at Cluster-scope level.
The resource tracking method configuration in the argocd-cm config map should be updated either to `annotation` or `annotation+label`. 
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

The namespace to be controlled by Argocd should be configured in the `application.namespaces` property in argocd-cmd-params-cm config map. The configuration can be wild-chard as well. The configuration will be like below.

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
