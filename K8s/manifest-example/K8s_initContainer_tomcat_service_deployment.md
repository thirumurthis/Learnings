### To deploy a sample tomcat webapp init container.
 - initcontainer is different from the lifecycle of the pod.
 - intitCotnainer will be executed first, kind of a setup for the actual app to execute.

Below uses busybox image to setup a index.html, with the shared mount the created file will be used within the tomcat.

- Same can be achive with the command and args, commented as below.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tomcat-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
     app: tomcat
  template:
    metadata:
      labels:
        app: tomcat
        role: rolling-update
    spec:
      volumes:
      - name: shared-volume
        emptyDir: {}
      initContainers:
      - name: busybox
        image: busybox
        volumeMounts:
        - name: shared-volume
          mountPath: /app-data/temp
        command: ["/bin/sh"]
        args: ["-c","echo '<h1>hellow from k8s!</h1>' > /app-data/temp/index.html"]

      containers:
      - name: tomcat-container
        image: tomcat:9.0
        # command: ["/bin/bash"]
        # args: ["-c","mkdir -p /usr/local/tomcat/webapps/temp; touch index.html"]
        volumeMounts:
        - name: shared-volume
          mountPath: /usr/local/tomcat/webapps/temp
        ports:
        - containerPort: 8080
        readinessProbe:
          initialDelaySeconds: 60
          httpGet:
            path: /temp/index.html
            port: 8080
            scheme: HTTP
        #  exec:
        #    command:
        #      - curl
        #      - http://localhost:8080/temp/index.html
  strategy:
    type: RollingUpdate
    rollingUpdate:
       maxSurge: 2
       maxUnavailable: 1
---
kind: Service
apiVersion: v1
metadata:
  name: tomcat-service
spec:
  selector:
    app: tomcat
  type: NodePort
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
    nodePort: 30080
```
