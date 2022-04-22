### in case we need to add pod identiy we need to use below condition in deployment yaml.

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ template "myapp" . }}-aks-deployment
  labels:
    app: {{ template "myapp" . }}-aks-svc
spec:
  replicas: {{.Values.replicaCount.myapprc}}
  selector:
    matchLabels:
      app: {{ template "myapp" . }}-project
  template:
    metadata:
      labels:
        app: {{ template "myapp" . }}-project
      {{- if not (empty .Values.msiPodIdentity) }}
        aadpodidbinding: {{.Values.msiPodIdentity}}
      {{- end }}
      annotations:
        checksum/config: {{ include (print $.Template.BasePath "/configmap.yaml") . | sha256sum }}      
    spec:
      containers:
        - name: myapp-container
          image: .....
        ...
```

Refer: https://github.com/helm/helm/issues/3601

Note: check the Azure documentation on the managed pod identity and how to define this in the k8s deployment

- values.yaml of helm can contain some map values.
