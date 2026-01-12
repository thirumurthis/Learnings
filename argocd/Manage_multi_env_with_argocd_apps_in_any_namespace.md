### ArgoCD application in any namespace


In this blog we will install ArgoCD with cert manager in KIND cluster. With the KIND cluster we will have an local environment to learn different components.

In this blog the focus is to enable ArgoCD application in any namespace feature and use it. The application in any namespace feature lets ArgoCD controller to manage any namespace in the remote cluster other than the default argocd. This feature is not explictly enabled.

### Pre-requisites:

Below software would be required to installing the ArgoCD,

- Docker desktop
- KIND CLI
- Helm CLI
- Kubectl CLI
- ArgoCD CLI

In the demonstration the ArgoCD is deployed in KinD cluster with Cert manager, Kubernetes Gateway and Apisix. These components are configured so the ArgoCD UI can be accessed from the host machine. Understanding of Cert manager and Apisix would be nice but it is not mandatory to follow along.

#### ArgoCD application in any namespace

The application in any namespace feature in ArgoCD works only when ArgoCD is installed at Cluster-scope level, so ArgoCD has access to create and list resources.

#### Configuration updates

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

2. The namespace to be controlled by Argocd should be configured in the `application.namespaces` property in argocd-cmd-params-cm config map. The namespace information can be comma seperated values, wild-chard format also supported. The configuration will be like below.

```yaml
# argocd-cmd-params-cm-patch.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-cmd-params-cm
data:
  # namespace to be managed by argocd
  application.namespaces: env-*
  # since cert-manager is used the insecure access in enabled
  server.insecure: "true"
```

** Note:- ** 
 The `application.namespaces` configured with `env-*` in the remote cluster we will be creating the namespace like `env-dev-0`, `env-dev-1`, `env-test-0`, etc.

3. RBAC permissions for ArgoCD to list and access resources in the KIND cluster. The RBAC installed in this blog is from argocd project example [argoproj/argo-cd/examples](https://github.com/argoproj/argo-cd/tree/master/examples/k8s-rbac/argocd-server-applications). The configuration is added to the kustomization manifest.  


#### Implementing ArgoCD resources

With the above ArgoCD configuration changes deployed in the KinD cluster, next step would be to create AppPorject and Application resources.

1. The namespaces to be managed by ArgoCD controller is configured in the `spec.sourceNamespaces` property of the ArgoCD AppProject manifest.  

```yaml
# argocd_app_project.yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: app-in-any-ns-appproject
  namespace: argocd
spec:
  description: "parent argocd application project"
  destinations:
  # The namespace in destination is set to * for demonstration 
  # The namespace that ArgoCD will manager in that remote cluster
  - namespace: '*'
    server: '*'
  # Allow manifests to deploy from any Git repos; * - refers any repo
  sourceRepos:
   - '*'
  sourceNamespaces:
   - env-*   # list of namespace argocd will be managing
  clusterResourceWhitelist: # configure which resource argocd to maintain
   - group: '*'
     kind: '*'
```

2. The `spec.project` in the ArgoCD application manifest uses the AppProject created above. The manifest also includes the destination where the kuberentes resources of application to be deployed.

- Application configuration for resource to be deployed by ArgoCD in one of the remote cluster.

```yaml
# argocd_app_in_any_ns_app_1.yaml
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
    server: https://172.19.0.3:6443  # URL for the remote cluster fetched using kubectl get endpoint
    namespace: env-dev-0
```

- Application configuration for resource to be deployed by ArgoCD in one of the remote cluster.

```yaml
# argocd_app_in_any_ns_app_2.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: app-in-any-ns-app-2
  namespace: argocd
spec:
  project: app-in-any-ns-appproject
  source:
    repoURL: https://github.com/thirumurthis/argocd-app.git
    targetRevision: HEAD
    path: app-in-any-ns/my-backend-app/
  destination:
    server: https://172.19.0.5:6443  # URL for the remote cluster fetched using kubectl get endpoint
    namespace: env-dev-1
```


#### ArgoCD deployment in KinD Cluster

The kustomization manifest in the code below is used to deploy ArgoCD in KIND cluster.

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

** Note:- ** 
   - Place the files - kustomization.yaml, argocd-cm-patch.yaml and argocd-cmd-params-cm-patch.yaml place under argocd_install/kustomize.
   - The Kustomization manifest `patches` property includes patched ArgoCD config map enable application in any namespace will applied automatically.

### KIND cluster configuration for ArgoCD

Below is the Kind configuration to install ArgoCD cluster

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

Below command is used to creaet the Kind cluster, with the above configuration. Ensure the Docker Desktop is running.

```sh
kind create cluster --config kind_argocd_config.yaml
```

### Deploy Kuberentes Gateway

To install the Kubernetes Gateway to the cluster, we use below command

```sh
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.4.1/standard-install.yaml
```

### Deploy cert manager

To install cert manager use below command.

```sh
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.19.2/cert-manager.yaml
```

### Deploy ArgoCD using Kustomize manifest

To install the ArgoCD we use the kustomize configurations and the patch files are stored in the argocd_install/kustomize folder and issue below command.

```sh
kubectl create ns argocd
kubectl apply -n argocd  -k argocd_install/kustomize/
```

### Deploy APISIX using helm chart

The APISIX deployed with helm chart will be used to create Ingress to access the ArgoCD UI from the host machine. Below set of command is used to install with configuration.

Add the helm repo for Apisix 

```sh
helm repo add apisix https://charts.apiseven.com
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

Create namespace

```
kubectl create ns apisix
```

Helm command to install Apisix with override configuration

```
helm upgrade -i apisix apisix/apisix --namespace apisix \
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
--set ingress-controller.gatewayProxy.createDefault=true
```

### Deploy Issuer and Certificate (Self-signed certificate) 

Self singed certificate is used here. Since the cert manager is already deployed once the pods are in ready and running state. Use the Issuer and Certificate manifest and deploy to `argocd` namespace.

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

Certificate resource 

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
    - argocd.localhost
```

To deploy to the argocd cluster use below command.

```
kubectl -n argocd apply -f argocd_issuer.yaml
kubectl -n argocd apply -f argocd_certificate.yaml
```

### Install Apisix ingress  

Below is the ingress manifest used to access the argocd server. The Apisix ingressClassName is used to pass the traffic to argocd-server service on the port 443. Apply this ingress to argocd namespace.

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
        - argocd.localhost
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
  - host: argocd.localhost
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

### ArgoCD UI Access

The Apisix ingress is configured with comman name `argocd.demo.com`, add this to the host file in Windows like below.

```
127.0.0.1 argocd.demo.com
```

**Info:**
 - Accessing the local argocd behind the proxy with the `argocd.demo.local` might throw `DNS resolution error`. In this case add `argocd.localhost` in the hosts file like  `127.0.0.1 argocd.localhost` and access `http://argocd.localhost` to access the app.

From browser use the URL `http://argocd.demo.com` which will redirect to `https` and allow unverified access since self signed certificate.

Login with the username `admin` and for password use below command. To reset the password use ArgoCD UI settings menu.

```
kubectl  -n argocd get secret/argocd-initial-admin-secret -ojsonpath={'.data.password'} | base64 -d; echo
```

Once logged in the ArgoCD UI if the application are deployed it would looks like in below snaphsot. 

![image](https://github.com/user-attachments/assets/56f639a7-51ea-4dde-a399-46af710fd0fa)


### Add Git repo to ArgoCD server

The manifest used to deploy in the remote cluster includes a nginx deployment, configmap and service resources. The manifest is aded to directory `app-in-any-ns/my-backend-app`. The application manifest includes the path of the deployment resource.
The public Github repo [argocd-app](https://github.com/thirumurthis/argocd-app/) that includes manifest. To add the public Git repo to ArgoCD server first login using ArgoCD CLI. Below is the command to login to argocd server, provide user name and password when prmopted.

```
# argocd login <argocd-server>
argocd login argocd.demo.com
```

To add the git repo use below command. Note, when using private repo it requires username and the PAT token for password.

```
argocd repo add https://github.com/thirumurhis/argocd-app
```

### Create remote KinD cluster 

To create a target Kind cluster use below configuration for ArgoCD to manage 

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

Before applying the ArgoCD resource to the ArgoCD server create the namespace `env-dev-0` in the remote cluster. So once the ArgoCD AppProject and Application resources are deployed, ArgoCD server will use manifest from the Git repo and deploy the application in the remote cluster.


### Add remote cluster to ArgoCD server

In Windows WSL for ArgoCD to access the remote KIND cluster, the remote cluster endpoint URL should be configured in the server property in the `~/.kube/config`. To get the endpoint for the remote cluster use `kubectl get endpoints --context <context-name-of-remote-cluster>`. Note, when creating the kind cluster the kube config will be updated automatically with context info.

```yaml
- cluster:
    certificate-authority-data: ... # redacted
    server: https://127.0.0.1:38917  # <-- The server url to be updated with the remote cluster endpoint
  name: kind-dev-env-1
```
The Kind cluster will use the docker sub-net ip address during cluster creation.

Use below command to add cluster with argocd server.

```
# argocd cluster add <context-name> --name <name-for-kind-cluster>
argocd cluster add kind-dev-env-0 --name kind-dev-env-0
```

The output of the command

```
WARNING: This will create a service account `argocd-manager` on the cluster referenced by context `kind-dev-env-0` with full cluster level privileges. Do you want to continue [y/N]? y
INFO[0001] ServiceAccount "argocd-manager" already exists in namespace "kube-system"
INFO[0001] ClusterRole "argocd-manager-role" updated
INFO[0001] ClusterRoleBinding "argocd-manager-role-binding" updated
WARN[0001] Failed to invoke grpc call. Use flag --grpc-web in grpc calls. To avoid this warning message, use flag --grpc-web.
Cluster 'https://172.19.0.3:6443' added
```

### Deploy the ArgoCD AppPorject resource 

The ArgoCD AppProject manifest mentioned above will be created, which is different from the default AppProject which already exists.

```
kubectl apply -f argocd_app_project.yaml
```

### Deploy the ArgoCD Application resource

The ArgoCD application manifest is configured to use the AppProject created with the repos and destination of the server to which ArgoCD will deploy the nginx application from Git repo.

```yaml
# update the destination server url of the remote cluster.
kubectl apply -f argocd_app_in_any_ns_app_1.yaml
kubectl apply -f argocd_app_in_any_ns_app_1.yaml
```

### Output of deployed resource from remote cluster

Once the ArgoCD application deployed, the deployment, config map and service from the Git repo will be deployed to the remote Kind cluster.

ArgoCD UI will create applications as seen in the first snapshot above.

Deployed resource from cluster 1

```sh
$ kubectl --context kind-dev-env-0 -n backend-app get all

NAME                                  READY   STATUS    RESTARTS       AGE
pod/backend-server-559885cd7b-fwlp9   1/1     Running   1 (127m ago)   22h

NAME                  TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)    AGE
service/backend-svc   ClusterIP   10.96.115.0   <none>        8081/TCP   22h

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/backend-server   1/1     1            1           22h

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/backend-server-559885cd7b   1         1         1       22h
```

Deployed resource from cluster 1

```sh
$ kubectl --context kind-dev-env-1 -n backend-app get all

NAME                                  READY   STATUS    RESTARTS       AGE
pod/backend-server-559885cd7b-hz6xc   1/1     Running   3 (128m ago)   6d19h

NAME                  TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)    AGE
service/backend-svc   ClusterIP   10.96.238.6   <none>        8081/TCP   6d19h

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/backend-server   1/1     1            1           6d19h

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/backend-server-559885cd7b   1         1         1       6d19h
```
