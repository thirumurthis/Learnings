The idea to push the logs to Elastic stac

Below example, will use the Volume mount and file beat configuration (as sidecar) to push data to kibana.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app
  namespace: elastic-stack
  labels:
    name: app
spec:
  containers:
  - name: app
    image: kodekloud/event-simulator
    volumeMounts:
    - mountPath: /log
      name: log-volume

  - name: sidecar
    image: kodekloud/filebeat-configured
    volumeMounts:
    - mountPath: /var/log/event-simulator/
      name: log-volume

  volumes:
  - name: log-volume
    hostPath:
      # directory location on host
      path: /var/log/webapp
      # this field is optional
      type: DirectoryOrCreate
```

- Image used for elastic serach and kibana is as follows
```
### Elastic search
image: docker.elastic.co/elasticsearch/elasticsearch:6.4.2

### Kibana
image: kibana:6.4.2
```
