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

### Secrutiy In docker and `Security context` in K8S:

```
## to run the docker process as different user other than root we can specify the user in either the docker command 
$ docker run --user=1000 ubuntu sleep 1000

## in case if we need to build the image to run the command using user 100 then specify the user in Docker file
FROM ubuntu

USER 1000
```
 #### Enabling and disabling Linux capablities:
   - There are different linux capabilities like CHOWN, DAC, KILL, NET_BIND, SETPCAP, MAC_ADMIN, SYS_ADMIN, SYS_CHROOT, SETUID, etc.
   - To check the list of capablity check the file (/usr/include/linux/capability.sh)
   - **By default the docker runs the container with limited set of capabilities, so the process running within the container doesn't have disrupt host or other container running on the same host**
   - To override the behaviour or add additional capabilities we can use `--cap-add` option
   - To drop the capabilities use, `--cap-drop` option
   - To run the container with all privileges enabled, use the `--privileged` flag option.
   - In Docker this capabilities can be enabled/disabled as below 
 ```
 ### Below is the option to override the 
 # enable
 $ docker run --cap-add MAC_ADMIN ubuntu
 
 # disable or drop privilege
 $ docker run --cap-drop KILL ubuntu
 
 
 # to enable all privileges
 $ docker run --privileged ubuntu
 ```
 - Like Docker, we can add Linux capablilites as well.
 - In K8S the container is encapsulated within the POD.
    - The capabilities can be enabled at the POD level or Container level or both.
      - by enabling the capablitlies at the pod level, it will be carried over to all the containers
      - by enabling the capabilites at the container level, it will be available only to that container
      - by enabling the capablitlies at both POD and Container, the container capablities will override the POD level capability
  - Specifying security context using yaml file
 ```
 apiVersion: v1
 kind: Pod
 metadata:
    name: app-pod
 spec:
   securityContext:     # this will enable the user privileges to run as 1000 not as root at the POD level
     runAsUser: 1000
   containers:
   - name: ubuntu
     image: ubuntu
     command: ["sleep", "300"]
     securityContext:
        runAsUser: 1000
        capabilities:    # The capabilities is applicable only at the Container level, AT POD Level the Capabilites cannot be added. (NOTE)
           add: ["MAC_ADMIN"]   
 ```
 Note/TIP: 
  - When generating the yaml file of a running pod using the kubectl command, if adding a `securityContext` attribute check if an empty one is already defined. if we provide multiple security context at the POD level, the kubectl apply command works without throwing any issues. (`$ kubectl exec pod-name -- whoami`)
  - When enabling the capablilities in K8S yaml file, make user the runAsUser to be `root`
  - enabling or adding "SYS_TIME" capablities on a ubuntu image enables to run the date -s command successfully `$ kubect exec pod-name -- date -s '19 APR 2021 11:14:00'`

### Service Accounts:
  - Type of Accounts :
     - Service Account - This is used by machines, or process. An account used by the application to interact with the Kubernetes cluster. Example, monitoring application Promethus uses the service account to poll the Kubernetes API for performance metrics.
     - User Account - This is used by human, like for administrator for manging the cluster, developer to develop and deploy applications, etc.
 
 #### To Create a service account use
 ```
 ## create service account
 $ kubectl create serviceaccount <serviceaccount-name>
  $ kubectl create serviceaccount appaccess-sa
 
 ## list the service accounts
 $ kubectl get serviceaccount 
 
 ## describing the service account
 $ kubectl describe serviceaccount appaccess-sa
 ```
  - Note: The service account will create an Token automatically.
  - The serviceaccount token must be used by the external application while authenticating to Kubernetes API.
 - When creating the Service Account:
    - First creates the Service account object
    - Then generates a token for Service account
    - Then creates a secret object and stores that token inside that secret object.
    - The secret object is then linked to the service account. 
    - This token can be used as authentication Bearer token while making call to access REST API.
      - In Curl command we can provide this bearer token ` curl <rest-url-of-application> -k --header "Authorization: Bearer <token>`;

In general:
  - Create a service account
  - Assign right permission using RBAC mechanism
  - Export the service account token, to third party to access the application.

What can be done if the third party application is running/hosted with the same K8S cluster where the REST API is running.
 - In this case then, simply mount the service token secret as a volume inside the pod hosting the third party application itself.
 - No need to provide this token manually.

Note: Each namespace has its default service account and token created, the tockens are automatically mounted as volumes to the pod when Pods are created under the namespce.
