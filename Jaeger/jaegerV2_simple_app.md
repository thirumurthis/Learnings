- The kind cluster with config

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30317
    hostPort: 8317
  - containerPort: 30318
    hostPort: 8318
  - containerPort: 30990
    hostPort: 9090
  - containerPort: 30300
    hostPort: 3000
  - containerPort: 31686
    hostPort: 8088
- role: worker
- role: worker
- role: worker
```

-- to install the kind cluster
```
kind create cluster --config kind-config.yaml --name dev0
```

-- to install the jaeger default in-memory
```
helm upgrade -i -n jaeger jaeger jaegertracing/jaeger --create-namespace
```
- Simple service override after installting

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jager-nodeport-svc
spec:
  type: NodePort
  selector:
    app.kubernetes.io/component: all-in-one
    app.kubernetes.io/instance: jaeger
    app.kubernetes.io/name: jaeger
  ports:
    - protocol: TCP
      name: jaeger-ui
      port: 16686     
      targetPort: 16686  
      nodePort: 30300
    - protocol: TCP
      name: collector-1
      port: 4317
      targetPort: 4317
      nodePort: 30317
    - protocol: TCP
      name: collector-2
      port: 4318
      targetPort: 4318
      nodePort: 30318

```

kubectl -n jaeger 
