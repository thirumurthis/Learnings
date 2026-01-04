# Stakater reloader

In Kubernetes in some case we need to reload the deployment in case if we patch a ConfigMap or Secret that is mounted to a container.

The [stakater repo](https://github.com/stakater/Reloader) to understand the details.

Below is an example, create a deployment resource with the annotation

```yaml
# reloader-deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  annotations:
    reloader.stakater.com/auto: "true"
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.14.2
        ports:
        - containerPort: 80
        envFrom:
        - configMapRef:
            name: my-config
        - secretRef:
            name: my-secret
```

- The configMap with annoation to restart

```
# reloader-cm.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: my-config 
  annotations:
    reloader.stakater.com/rollout-strategy: "restart"
data:
  app: "restart-app"
```
- The secret with annotation to ignore change reload

```yaml
# reloader-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: my-secret
  annotations:
    reloader.stakater.com/ignore: "true"
data:
  secret: cmVsb2FkZXItc2VjcmV0LWFwcA==

```
#### Install the stakater resource to the cluster

```sh
helm repo add stakater https://stakater.github.io/stakater-charts
helm repo update
helm install reloader stakater/reloader
```

##### Deploy the above manifest

```
kubectl create ns reloader
kubectl -n reloader apply -f reloader-deploy.yaml
kubectl -n reloader apply -f reloader-cm.yaml
kubectl -n reloader apply -f relader-secret.yaml
```

With the resources deployed 
  - Edit the configmap and on saving the changes we should see the pods restarts.
  - Edit the secret and on saving the changes we could see the pods NOT restarts, since the annotation added to ignore.
