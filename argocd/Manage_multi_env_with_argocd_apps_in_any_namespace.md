### Argocd Application in any namespace

In this blog have explained how to deploy ArgoCD in KinD cluster and demonstrate ArgoCD to manage application resources in namespaces other than the control plane's namespace (which is `argocd`).
Refer ArgoCD documentation [applications in any namespace](https://argo-cd.readthedocs.io/en/latest/operator-manual/app-any-namespace/) for more details.

ArgoCD explicitly disables the application in any namespace feature and needs to be configured.

- The ArgoCD application should be installed is cluster-scope, so it has permissions to list and manipulate resources on a cluster level. This feature will not work if Argocd is installed in namespace-scoped mode.
- The argocd-cm configmap property [resource tracking method](https://argo-cd.readthedocs.io/en/latest/operator-manual/app-any-namespace/#switch-resource-tracking-method) should be updated to `annotation` or `annotation+label`. The configuration looks like below.

```
# argocd-cmd-patch.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: argocd-cm
data:
  application.resourceTrackingMethod: annotation
```

- Update the `application.namespace` property to include the namespace the argocd will use to manage from the target cluster.

```
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

### Depoloyment
- The argocd itself deployed with kustomize.
