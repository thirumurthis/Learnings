### Kubernetes Architecture
 - Below image depicts the relationship between
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

#### How to set `environment` variables and use it in pods, use the `env` array within the defintion yaml and other approach.

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

-----------------------------------------------

### ConfigMaps
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

----------------------------

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

---------------------

### `Security context` in K8S (security in docker):

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

---------

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

 - The default service account has restriction, and is automatically attached to the pod when created.
 - If we need to attach a different service account that was created, we can update the Pod defintion yaml file (or deployment file)

```
spec:
   containers: 
     - name: nginx
       image: nginx
       serviceAccount: <service-account-newly-created>
``` 

- In order to tell the pods not to use/mount the default service token we can use below attribute `automountServiceAccountToken`:
```
spec:
  containers:
    - name: nginx
      image: nginx
      automountServiceAccountToken: false   # this will make sure not to create the default token as volume when creating a pod
```
---------------------------

### Resource within container (request and limits of resources)
  - The resource like CPU and Memory can be limited at a namespace level using `LimitRange`
     - `LimitRange` type can be used to specify the `min` and `max` memory and Cpu usage of pod or container within the namespace. 
     - The memory and cpu resource can be requested and set to limit for a container in pod defintion file as well
     ```
     spec:
        containers:
        - name: container-name
          resources:
             limits:
                memory: "200Mi"   # note the double quotes, Mi is mibibytes not megabytes for megabytes specify 200M
                cpu: "1"
             requests:
                memory: "100Mi:   # when creating the pod Kubelet will use this as a minimum level to schedule the pods on the node.
                cpu: "0.5"
     ```
     - Note: At container level, if only the limits - is set in definition file after creating the pod the requested memory/cpu will be same as the limit value
     -       At container level, if only the requests - is set in defintion file after creating the pod the limit will be the max (utilizes the all available in the Node)
     -       Requests attribute memory/cpu should always be less than the limit attribute value.
     ```
     Note: If a Container specifies its own memory limit, but does not specify a memory request, Kubernetes automatically assigns a memory request that matches the limit.
     Similarly, if a Container specifies its own CPU limit, but does not specify a CPU request, Kubernetes automatically assigns a CPU request that matches the limit.
     ```
  - `ResoruceQuota` can also be used to hard limit for number of resources that can be created like pods, service, deployment, etc. in the namespace
      - First create the namespace, and then create the resourceQuota using `$ kubectl create resourcequota rq1 -n <name-space>` or using `yaml resource definition file`.
      - using `$ kubectl describe resourcequota rq1 -n <name-space>` will list the hard limit and usage limit.
      
##### Quality of service: 
  - in the pod defintion file for container if below condition is set, the qosClass will be as listed:
    - If the limits and requests of memory is `equal` - after creating the pod, using `kubectl get pods -o yaml` should see the status of qosClass: **`Guaranteed`**
    - If the limits set `high` and requests set `low memory` (different) - after creating the pod, the pod definition yaml file the status of qosClass: **`Burstable`**
    - if the limits and requests are `not set` - after creating the pod, the pod definition file will has the status of qosClass: **`BestEffort`**
-----------------------------

### Taint and Toleration:
  - Taint is set at the `Node` level
  - Toleration is set at the `Pod` level
  
##### How to create a taint?
```
$ kubectl taint nodes <node-name>  key=value:taint-effect
## taint-effect defines what happens to the pod if they do not tolerate the taint.
Three taint-effect 
   - NoSchedule  [The pod will not be sceduled on that node at all.]
   - PreferNoSchedule  [The system tries to avoid placing the pod on the tainted node, but that is not guaranteed.]
   - NoExecute [ The new pods will NOT be scheduled on the tainted node, and existing pod if any running will be evicted if they don't tolerate the taint]
```
##### Using command 
```
$ kubectl taint node node1 app=frontend:NoSchedule
```
##### Using definition file of Pods
```
apiVersion: 
kind: Pod
metadata:
   name: simple-pod
spec:
   containers: 
   - name: nginx
     image: nginx
   tolerations:      # All the tolerations values should be in double quotes
   - key: "app"
     operator: "Equal"
     value: "frontend"
     effect: "NoSchedule"
```

 - Note:  
    - If the node is set with taint effect of NoExecute, any pod running befor the taint applied will be evicted.
    - Taint and toleration doesn't always guarantee that the tolerated pod to be created on that specific tained node. The pod can be created in different node which doesn't have taints as well. In order restrict those pods to be created on specific node we can use `NodeAffinity`.
    - Taint can be applied to Master node.
        - The Scheduler doesn't schedule the pods on the master node.
        - A taint is set on the Master node when the kubernetes is setup.
        - Best practice, not to deploy any pods on the Master node.
        - `$ kubectl describe node kubemaster | grep -i taints`
    
### Assing Pods to Nodes using `Node Selector` and `Node Affinity`:
   - If we want a pod to be executed on a specific node, then we can specify that using `nodeSelector` attribute in the pod defintion file.
   - In order to use the `nodeSelector`, create a label on the nodes or use the existing node.
 -STEP1
   - Creating a label for node
   ```
   $ kubectl label node <node-name> <label-key>=<label-value>
   $ kubectl label node node01 nodelabel1=high-performance
   
   # to list the labels of the nodes
   $ kubectl get nodes --show-labels
   ```
 -STEP2
   - Using the node label in the pods definition file.
  ```
  spec:
    containers:
    - name: nginx
      image: nginx
    nodeSelector:
        node1label: high-performance
  ```
  - Note: The `nodeSelector` option can isoloate the pod to run on a node. What happens if the node is not available or ready. What happens we need to create pod in different node when specific node is not available, or any of group of nodes.
  
  ##### Node Affinity is more expressive. since this allows express logics
   - Node Affinity
   - Inter-pod affinity/anti-affinity
   
  There are currently two types of node affinity, called `requiredDuringSchedulingIgnoredDuringExecution` and `preferredDuringSchedulingIgnoredDuringExecution`.
  
  ##### how to use NodeAffintiy:
   - In the pod definition file
  ```
  spec:
    affinity:
      nodeAffinity:
        requiredDuringSchedulingIgnoredDuringExecution:
          nodeSelectorTerms:
          - matchExpressions:
            - Key: nodelabel1
              operator : In
              values:
                - high-performance
                - ssd
         preferredDuringSchedulingIgnoredDuringExeuction:
         - weight: 1
           preference:
             matchExpressions:
             - key: label1
               operator: In
               values:
                 - labelvalue
  ```
-----------
### Job
  - In order to run a batch process we can use JOBs. The same can be achived to some extent with pods, when the restartPolicy is set to Never or OnFailure.
     - But pods try to restart often restartPolicy can help on it, but if we want the job to run for 3 successful completion and stop we need to use jobs.
  - To create the job definition file use below command
  ```
  $ kubectl create job job-name --image=<image-name> -o yaml --dry-run=client
  ```
  - By default the job will try to execute 6 times to achive competion. This can be increased by specifying the `backoffLimit` properties in the Job.
  - `completions: <number>` is used to tell the job, it should completed if there are atleast <number> of successful completions.
  - proeprty `parallelism: <number>` is used to create <number> of jobs parallely till the successful job completion is reached. 

------------
### CronJob
  - To create the defintion file for the crontjob use below command
  ```
  $ kubect create cronjob <job-name> --image=<image-name> --schedule="10 10 * * *" -o yaml --dry-run=client > cronjob1.yaml
  ```
  
  - After running creating the cronjob if we need to suspend it, we can use patch command to do it.
  ```
  $ kubectl patch cronjobs <job-name> -p '{ "spec" : {"suspend" : true }}'
  ```
  - If the spec.suspend flag is set to True, the cron job will not be scheduled to run in the future. but the already running job will be in process.
 -------------
 
 ### Services

   - Understanding the ports involved in the service resource type.
 
 ![image](https://user-images.githubusercontent.com/6425536/121905948-7d7c3f00-ccdf-11eb-95e3-269a86d63ecb.png)

 
