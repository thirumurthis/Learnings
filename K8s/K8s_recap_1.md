### Kubernetes Architecture
 - Below image depics the relationship between
   - Node
   - Pod
   - Container

##### 1 to 1 pod to container representation
  - This is the best practice, easy to sacle up and down
  
![image](https://user-images.githubusercontent.com/6425536/120360157-4ceadd00-c2bd-11eb-8e03-dfab6c67bc1e.png)


##### 1 to many pod to container representation
   - We can use multi-containers pods, but recommended to use a helper pods not of same kind/application.
   
![image](https://user-images.githubusercontent.com/6425536/120377928-9f82c400-c2d2-11eb-97c9-a68cc9c46b38.png)


#### Assuming already a `ReplicaSet` is defined and manged by the K8s cluster, what are different ways to scale up the replica sets (instance of pods).
  - Note: For replicaset the yaml file should contain the selector section
```
### update the existing yaml file, replicas : <desired number of replicas> and issue below command
$ kubectl replace -f replicaset-definition.yaml

### Using kubectl scale command with replicas option 
$ kubectl scale --replicas=6 -f replicaset-definition.yaml

### using kubectl scale command and existing replicaset name
$ kubectl scale --replicas=6 replicaset name-of-replica-set-running
```

#### Deployment representation within the nodes,

![image](https://user-images.githubusercontent.com/6425536/120940352-b447c900-c6d1-11eb-982b-b7988fb7b773.png)

#### How to set `environment` variables. use the env array within the defintion yaml.
 - The 

Different ways of speicifying environment variables:
```
## 1. direct way, within the yaml file using plain key value pair approach
...
spec:
  container:
   - name: ...
     ...
     env: 
       - name: ENV_KEY1
         value: valueof1
  ...
  
 ## 2. Environment variables, specified from the configMap
 
 env: 
    - name: ENV_KEY1
      valueFrom:
         configMapRef: 
         
 ## 3. Environment variables, specified from the Secret
 
 env:
    - name: ENV_KEY1
      valueFrom:
          secretKeyRef:
```

#### ConfigMaps
  - In the Pod definition file, say we have many environment variable declared as if the PODS grow it would be difficult to manage those environment variables.
  - `ConfigMaps` helps us to declare those environment variables (key value pair) centrally and refer it within the Pod definition file.

```
## creating configMap using imperative way
$ kubectl create configmap <configMap-name> --from-literal=<key>=<value> --from-literal=<key>=<value>

## Another way to configure the configMap is using the file
$ kubectl create configmap <configmap-name> --from-file=<path-to-file>
  ### the file in this case is a properties file with key value pair.
  ### the data from this file is read and stored under the name of the file.
  
## Declerative way, using a definition yaml file

apiVersion: v1
kind: ConfigMap
metadata: 
   name: application-configuration
data:  # Not this is different for Pods/deployment - used to be spec, here it is data
   ENV_KEY1: value1
   ENV_KEY2: value2
```
 - Name of the configuration will be used to associate with the pods.

#### Other ways to inject environment variables to pod:
  - through configMap 
```
envFrom: 
   - configMapRef:
       name: application-configuration 

## more example
spec:
  containers:
    - name: test-container
      image: k8s.gcr.io/busybox
      command: [ "/bin/sh", "-c", "env" ]
      envFrom:
      - configMapRef:
          name: special-config
          
```
 - through single Environment value
```
  env: 
    - name: ENV_KEY1
      valueFrom:
        configMapKeyRef:
          name: application-configuration
          key: ENV_KEY1
```
- through volumes
```
volumes:
  - name: application-config-volume
    configMap:
      name: application-configuration 
```
`Key Take` - when using configMapRef in yaml, it is an list use - under envFrom

### Secrets:
 - Like configMap, if wanted to store passwords in encrypted way we can use secrets.
 - base64 encoded text.

#### how to create secrets, in different apporach
```
## using command
$ kubectl create secret generic <secret-name> --from-literal=KEY1=VALUE1 --from-literal=KEY2=VALUE2
$ kubectl create secret generic test-secret --from-literal=User=VALUE1 --from-literal=Password=VALUE2
## Creating an Yaml definition file and creating it.
```
#### Different ways to and associate the secrets as environment variable to a POD
 - adding secret to pod as environment 
```
 ## Associating a secret as environment variable to POD
 envFrom:
   - secretRef:
        name: test-secret  # <secret-name> created using command
 ```
 - adding single key from the secret as environment variable to the pod 
 ```
 ## Directly as single environment variable
 
 env:
 - name: DB_PASSWORD
   valueFrom:
       secretKeyRef:
          name: <secret-name>
          key: DB_Password  ## The key specified in the 
```
- adding secret from volume to the pod
```
volumes:
- name: secret-volume
  secret:
     secretName: test-secret # Secret name created either using command or definition yaml
```
  - Note: when mounting a secret from volume, each attribute in secret is created as seperate file with value of the secret as content.
  -   say, KEY1 will be the file and VALUE1 will be the content of the file if the secret was created with --from-literal=KEY1=VALEU1 

