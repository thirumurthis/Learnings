- Install using cert-manager

https://cert-manager.io/docs/installation/helm/#installing-with-helm


```
helm repo add jetstack https://charts.jetstack.io --force-update
```

```
helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.17.2 \
  --set crds.enabled=true \
  --set prometheus.enabled=false \  # Example: disabling prometheus using a Helm parameter
  --set webhook.timeoutSeconds=4   # Example: changing the webhook timeout using a Helm parameter
```
