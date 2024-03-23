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

#### Environment variables
##### How to set `environment` variables and use it in pods, use the `env` array within the defintion yaml and other approach.

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
- Expose the Pod spec via environment variables.
- In below we use the defintion yaml properties to expose as environment variable.
- for example, if need to pod name to be used in the environment variable or podId we can de
- refer the env section `fieldRef` and `fieldPath`, it is associating to one env variable
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: dapi-envars-fieldref
spec:
  containers:
    - name: test-container
      image: registry.k8s.io/busybox
      command: [ "sh", "-c"]
      args:
      - while true; do
          echo -en '\n';
          printenv MY_NODE_NAME MY_POD_NAME MY_POD_NAMESPACE;
          printenv MY_POD_IP MY_POD_SERVICE_ACCOUNT;
          sleep 10;
        done;
      env:
        - name: MY_NODE_NAME
          valueFrom:
            fieldRef:
              fieldPath: spec.nodeName
        - name: MY_POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: MY_POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: MY_POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: MY_POD_SERVICE_ACCOUNT
          valueFrom:
            fieldRef:
              fieldPath: spec.serviceAccountName
  restartPolicy: Never
```
  - The output when vierifed using `kubectl logs pod/pod-name` for above defintion looks something like
    ```
    node01
    dapi-envars-fieldref
    default
    192.168.1.4
    default
    ```
-----------------------------------------------

### ConfigMaps
  - In the Pod definition file, say we have many environment variable declared as if the PODS grow it would be difficult to manage those environment variables.
  - `ConfigMaps` helps us to declare those environment variables (key value pair) centrally and refer it within the Pod definition file.

- Imperative way
```
## creating configMap using imperative way

$ kubectl create configmap <configMap-name> --from-literal=<key>=<value> --from-literal=<key>=<value>

## Another way to configure the configMap is using the file

$ kubectl create configmap <configmap-name> --from-file=<path-to-file>

  ### the file can be properties file with KEY-VALUE pair, other data files like sql, etc.
  ### the data from this file is read and stored under the name of the file.
```

- Declaritive using defintion yaml
```yaml
## Declerative way, using a definition yaml file

apiVersion: v1
kind: ConfigMap
metadata: 
   name: application-configuration
data:  # ** Instead of spec like in Pods/Deployment definition here it is data ***
   ENV_KEY1: value1
   ENV_KEY2: value2
```

- Name of the configmap will be used to associate with the pods.

#### Other ways to inject environment variables to pod:
  - through configMap 
```
envFrom: 
   - configMapRef:
       name: application-configuration 
```
  `Key Take` - when using configMapRef in yaml, it is an list use - under envFrom

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

- more example of configmap, injected using env to pod
```
spec:
  containers:
    - name: test-container
      image: k8s.gcr.io/busybox
      command: [ "/bin/sh", "-c", "env" ]
      envFrom:
      - configMapRef:
          name: special-config
          
```
----------------------------

### Secrets:
 - Like configMap, if wanted to store passwords in encrypted way we can use secrets.
 - base64 encoded text.

#### how to create secrets, in different apporach
- 1. Imperative way
   - passing key value pairs as literals
   - from file
```
## using command with key values 
$ kubectl create secret generic <secret-name> --from-literal=KEY1=VALUE1 --from-literal=KEY2=VALUE2
$ kubectl create secret generic test-secret --from-literal=User=VALUE1 --from-literal=Password=VALUE2

## using command with secrets stored in file
$ kubectl create secret generic my-secret-frm-file --from-file=secretfile.properties
```

  - Example of creating secrets with tls key and certificate
```
$ kubectl create secret tls secret-name1 --key /tmp/nginx.key --cert /tmp/nginx.crt
```

- 2. Declerative way using yaml defintion
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: dotfile-secret
data:
  .secret-file: dmFsdWUtMg0KDQo=  # This is encoded foramt 
```
#### Different ways to associate the secrets as environment variable to a POD
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

#### using kustomize to create secret
  1. secrets as literal
  2. secrets as file
  3. secrets from env

##### 1. secrets using literal
- Create a folder, and create a file named "kustomization.yaml" or extension can be yml
- Copy below content to the file
- Note, no need to convert to base64 in this case
```yaml
secretGenerator:
- name: app-creds
  literals:
  - appuser=appuser
  - appsecret=secret
```
- From the folder, issue below command. The `.` current working dir can be replaced with `directory or folder path where kustomization.yaml file` exists
```
$ kubectl apply -k .
``` 
##### 2. secrets using file
- create a folder, and create a file named "kustomization.yaml"
- Create a file named `app-user.txt`, and add the content `appuser`
- create a file named `app-secret.txt`, and add the content `secret`
- Copy below content to the kustomization.yaml file
```yaml
secretGenerator:
- name: database-creds
  files:
  - appuser.txt
  - appsecret.txt
```
- From the folder issue below command
```
$ kubectl apply -k .
```

##### 3. secrets using env
- Create a folder and file named "kustomization.yaml"
- copy content below
```yaml
secretGenerator:
- name: app-pass-secret
  envs:
  - .env.secret
```
- create a file in the folder, `.env.secret` and add the content `appsecret`
- Now run the command `kubectl apply -f .` from that folder.

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

- Say, if there is an dashboard application which needs to connect to the Kuberenets cluster and get the pods running in it. In this case, the application can't directly access the k8s cluster, it requires a token. In this case we,
  - Create a service account
  - Assign right permission using RBAC mechanism
  - Export the service account token, to third party to access the application.

- What if the application is running/hosted within the same K8S cluster. then, simply mount the service token secret as a volume inside the pod hosting the third party application itself.
 - No need to provide this token manually.

- **Note:**
 - When namespace is created it includes a default service account. The service account includes a token associated to it.
 - This token is associated to the service account is a secret.
 - When we create a pod, by default the service account is mounted as volume to pod, which can be viewed with `kubectl describe pod/pod-name`.
 - The default service account has restriction, and is automatically attached to the pod when created.
 - If we need to attach a different service account that was created, we can update the Pod defintion yaml file (or deployment file)

```
spec:
   containers: 
     - name: nginx
       image: nginx
       serviceAccount: <service-account-newly-created>  # <---- the service account created with additional access
```
- Note, if the pod is already running and we need to add the new service account, the pod must be deleted and recreated with the changes.
- In case of the deployment, we can edit the service account which will trigger a rollout.

- In order to tell the pods NOT to use/mount the default service token we need to use `automountServiceAccountToken : false` like below in defintion
```
spec:
  containers:
    - name: nginx
      image: nginx
      automountServiceAccountToken: false   # this will make sure not to create the default token as volume when creating a pod
```

From kuberentes 1.22, **TokeRequestAPI** was introduced, since the jwt token earlier version is not bound to time. The JWT toke didn't include any expiry time. The new API is Audience bound, time bound and object bound making more secure.
- From 1.22, when new pod is created it doesn't relies on service account secret token

- `kubectl get pod/nginx -o yaml` output
```yaml
   #...
    volumeMounts:
    - mountPath: /var/run/secrets/kubernetes.io/serviceaccount
      name: kube-api-access-m8hw7
      readOnly: true
   #....
 volumes:
  - name: kube-api-access-m8hw7
    projected:
      defaultMode: 420
      sources:
      - serviceAccountToken:
          expirationSeconds: 3607     # <----------- time bound and projected volume
          path: token
``` 

- From 1.24 onwards, the `kubectl create serviceaccount my-sa` doesn't include or create any token
- To create a token we need to use `kubectl create token my-sa`. this new token from 1.24 will have the expiry date defined
- The usual expire date is 1hr, there are options to increase check documentation.
- In order to create service account with non expiry date, we need to use defintion
  ```
  # the service account should be availabe first
  apiVersion: v1
  kind: Secret
  type: kubernetes.io/service-account-token
  metadata:
    name: my-secret
    annotations:
      kuberentes.io/service-account.name: my-sa
  ```

### To decode the jwt token with jq 
```
 jq -R ' split(".") | select(length >0) | .[0],.[1] | @base64d | fromjson' <<< $(kubectl exec pod/nginx -- cat /var/run/secrets/kubernetes.io/serviceaccount/token)
```
- From the pod describe command fetch the service account mount path
```
controlplane $ jq -R ' split(".") | select(length >0) | .[0],.[1] | @base64d | fromjson' <<< $(k exec pod/nginx -- cat /var/run/secrets/kubernetes.io/serviceaccount/token)
{
  "alg": "RS256",
  "kid": "ZoxJnSXzhIi85Qp2hAft8ldinJr5DfTbIxpbDXZmWGE"
}
{
  "aud": [
    "https://kubernetes.default.svc.cluster.local"
  ],
  "exp": 1742710290,
  "iat": 1711174290,
  "iss": "https://kubernetes.default.svc.cluster.local",
  "kubernetes.io": {
    "namespace": "default",
    "pod": {
      "name": "nginx",
      "uid": "187ca2a5-e6ce-4b46-b770-9fa1e7b05026"
    },
    "serviceaccount": {
      "name": "default",
      "uid": "e8d45a4e-2032-4aa8-934c-11fbd5495f6c"
    },
    "warnafter": 1711177897
  },
  "nbf": 1711174290,
  "sub": "system:serviceaccount:default:default"
}
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

- **Note:** At container level, 
   - if only the limits is set in definition file after creating the pod, requested memory/cpu will be same as the limit value
   - if only the requests is set in defintion file after creating the pod, limit will be the max (utilizes all available in the Node)
   - ** Requests attribute memory/cpu should always be less than the limit attribute value.**
     
 - **Note:**
    - If a Container specifies its own memory limit, but does not specify a memory request, Kubernetes automatically assigns a memory request that matches the limit.
    - Similarly, if a Container specifies its own CPU limit, but does not specify a CPU request, Kubernetes automatically assigns a CPU request that matches the limit.
     
  - `ResoruceQuota` can also be used to hard limit number of resources that can be created like pods, service, deployment, etc. in the namespace
  - First create the namespace and then create the resourceQuota using `$ kubectl create resourcequota rq1 -n <name-space>` or using `yaml resource definition file`.
      - use `$ kubectl describe resourcequota rq1 -n <name-space>` list the hard limit and usage limit.

##### ResourceQuota imperitive 
 - Resource can be applied to a namespace to restrict the number resource eg. pod, configmap, memory, cpu usage.
 - To imperatively create the resource qouta use below command.
   ```
    $ kubectl create quota myrq --hard=cpu=1,memory=1G,pods=2 --dry-run=client -o yaml
   ```
 - When the resource quota created on specific `namespace` it will be applied to that namespace.
      
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

#### To remove the taint on a master node or control-plane node use 
`kubectl taint nodes node1 node-role.kubernetes.io/control-plane-`
- The master node describe displays the taints config like below,
```
 taints:
      - effect: NoSchedule
        key: node-role.kubernetes.io/control-plane 
```

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

    - Service object can be created without a selector, in that case the Endpoint is not automatically created, developer needs to create the Endpoint object associating the service.
 
 ##### Discovering the Service: Two modes
   - Environment variables 
           - If the deployment is created first followed by service then the environment variable in the pod will not be able to use the service.
           - The recommended approach is to create the Service first and thne Pod or deployment. 
           - This option is available out of the box in kubernetes.
           - `{SERVICE-NAME}_SERVICE_HOST` and `{SERVICE-NAME}_SERVICE_PORT`
   - DNS 
           - The CoreDNS option should be enabled to use this one.
           - A cluster-Aware DNS server such as CoreDNS watches the API for new services and creates a set of DNS records for each one.
           - If DNS has been enabled throughtout the cluster then all Pods should automatically be able to resolve Service by service name.
           - If we use DNS based discovery approach, we don't need to worry about the order of service object creation.
           - For example, 
                - if a service called `my-service` in kubernetes namespace `my-ns`, the control plane and DNS service create a DNS record `my-service.my-ns`
                - pods in the namespace `my-ns` should be able to find service by the name `my-service` or `my-service.my-ns`.
                - pods in other namespace must qualify the name as `my-service.my-ns`.
 
  ##### Headless Service:
     - Sometimes we don't need load-balancing and a single service IP. In this case we can create a service called `headless`, by sepcifying the `spec.clusterIp : None`.
     - For headless service, a service IP is not allocated.
     - How DNS is defined automatically depends upon the selector defined 
             - with selector (Endpoint is automatically created. DNS returns a type A record )
             - with no selector (Endpoint is not automatically created) 
        Check documentation for more details 
 
 Note: 
    - The hostname of the pod, sub-domain for a pod can be set in the Pod definition file as well, check documentation in that case.
    - `targetPort`: is the port the container accepts traffic on, `port:` is the abstracted Service port, which can be any port other pods use to access the Service 

 ** IMPORTANT ** :
   - If a deployment is created and a service is created, and for some reason if the service is not able to connect when using a temp pod.
   - say, there is a deployment temp-deploy (label: id=app-deploy), the pod labels ( id=app-deploy), the service selector should be associated to the labels of the Pod not the deployment.
 
 using temp pods to create command (curl, wget)
 ```
 $ kubectl -n <name-space> run --rm -it --image=nginx:alpine --restart=Never --command /bin/sh
 # issue curl command or wget ; not the -it in this case opens up terminal
 
 or 
 $ kubectl -n <name-space> run --rm -i --image=nginx:alpine --restart=Never --command curl -m 2 <service-name>:<service-port-exposed>
 
 $ kubectl -n <name-space> run --rm -i --image=busybox:stable --restart=Never --command wget -qO- <service-name>:<service-port-exposed>
 ```
  - As service can also be created using defintion yaml file, if there are some issues, probably might be mistake. check along with the debugging service.
 -----------
 ### Ingress 
     - Ingress controller :- 
            - actual implementation of ingress. There are different types of implementation available NGINX Ingress controller is managed by K8S community.
            - Other types available are trafeic, etc. check documentataion.
     - Ingress Resources :- 
            - This is the actual yaml definition file where the ingress rules are defined.
 
     - An Ingress is connected to a service, using NodePort type service expose the worker node and port to external world which is a security risk. Ingress address that solution.
     - Loadbalancer is an option provided by Cloud providers. 
 
 Step 1: Expose a deployment or pod, as a service. Say the service name is service is web and port 8080 is exposed.
 Step 2: Create a definition file for Ingress as below and pass the service name.
 
 ```
 apiVersion: networking.k8s.io/v1    ## Starting 1.20+ version use this version
kind: Ingress
metadata:
  name: example-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$1        # this is also NGINX ingress controller specific check the documentation for more info
spec:
  rules:
    - http:
        paths:
          - path: /
            pathType: Prefix  # other types available and depends on the implemention. ( ImplementationSpecific and Exact are other values)
            backend:
              service:                 # in lower version this section is represented as serviceName and servicePort directly.
                name: web
                port:
                  number: 8080
 ```
 
 Note 1 :
    - Creating multiple ingress definition will automatically identified by the Nginx ingress controller.
    - Say, i have an ingress defintions i namespace-a, the service are exposed for wear, watch application in the same namespace.
    - I have another application pay, which has a service in namespace-b and application also deployed in namespace-b.
    - by creating an new ingress resource for the pay service under the namespace-b, without any host section added. The same domain name was able to access the application.
 
  Note 2:
    - The created ingress resource contains a `default-http-backend` service which is used to handle any request that doesn't configured in the ingress resource rules.
    - by creating a seperate service with the name `default-http-backend` configured to a application specific 404 or redirection, the traffic will be using that service info.
  
  Note 3:
    - When setting up the NGINX controller, usuall the deployment deinfition is created and a service is exposed as NodePort. this opens up the node port to be accessed from external world.
    - since the incomming traffic in handled by the NGINX ingress controller deployment, we have to configure ingress resource to access the pods via service set in backend property of the ingress definition.
 
 --------------------------
 
 ### Network policy
    - Ingress
    - Egress
 - Within the cluster the created pods can communicate with each other since by default, it is created with Allow all.
 - Using network policy we can controll the flow of traffic to the pods.
 - The Ingress is incoming traffic, Egress is the outgoing traffic from that POD perspective.
 
 ##### The ntework policy created gets associated to a pod, which is defined in the definition file under spec: section using `podSelector`.
 - If the labels of the pod are placed in different namespace, even then the ingress traffic will be allowed if we restrict it.
 - using the `namespaceSelector` (in AND) within the yaml array the ingress traffic is restricted to that pod and namespace only.

 ```
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          user: alice
      podSelector:
        matchLabels:
          role: client
 ```
 
 sample defintion file:
 ```
 apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: test-network-policy
  namespace: default
spec:
  podSelector:
    matchLabels:
      role: db
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - ipBlock:
        cidr: 172.17.0.0/16
        except:
        - 172.17.1.0/24
    - namespaceSelector:
        matchLabels:
          project: myproject
    - podSelector:
        matchLabels:
          role: frontend
    ports:
    - protocol: TCP
      port: 6379
  egress:
  - to:
    - ipBlock:
        cidr: 10.0.0.0/24
    ports:
    - protocol: TCP
      port: 5978
 ```
 -------------------------
