- Using deployment srtaetrgy:
  - RollingUpdate
      -maxSurge
      -maxUnavailable
      
 ```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: tomcat-deployment-rolling-update
spec:
  replicas: 2
  template:
    metadata:
      labels:
        app: tomcat
        role: rolling-update
    spec:
      containers:
      - name: tomcat-container
        image: tomcat:${TOMCAT_VERSION}
        ports:
        - containerPort: 8080
        readinessProbe:
          httpGet:
            path: /
            port: 8080
  strategy:
    type: RollingUpdate  # other startegy is Recreate
    rollingUpdate:
       #(maxSurge: optional field that specifies the maximum number of Pods that can be created over the desired number of Pods)
       # The value cannot be 0 if MaxUnavailable is 0. The default value is 25%.
       maxSurge: 5  # this value can be absolute number or percentage 
       # (maxUnavailable: is an optional field that specifies the maximum number of Pods that can be unavailable during the update process.)
       # The value cannot be 0 if maxSurge is 0. The default value is 25%.
       maxUnavailable: 2 # this value can be absolute number or percentage 
 ```
