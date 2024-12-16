### Argocd Application in any namespace

In this blog have explained how to deploy ArgoCD in KinD cluster and demonstrate ArgoCD to manage application resources in namespaces other than the control plane's namespace (which is `argocd`).
Refer ArgoCD documentation [applications in any namespace](https://argo-cd.readthedocs.io/en/latest/operator-manual/app-any-namespace/) for more details.

In order to configure, the application in any namespace feature we need to configure it, since it is disabled by explicitly.

- This feature can only be enabled and used when ArgoCD installed as cluster-wide instance. This feature will not work if Argocd is installed in namespace-scoped mode.
- Switch [resource tracking method](https://argo-cd.readthedocs.io/en/latest/operator-manual/app-any-namespace/#switch-resource-tracking-method) to `annotation` or `annotation+label`. This should be updated in the argocd-cm configmap.

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-cm
data:
  application.resourceTrackingMethod: annotation
```
