# Nodes and Pods

Nodes :- 

A K8s node should run
- kubelet process
- Docker container (or pod) environment
- kube-proxy process
- supervisord

Pods:
   - The application which uses to run like docker container, etc.
   
# Controllers
  - Deployment
  - ReplicaSet - (used along with the Deployment controller, which states at any point the K8s to run the specified number of pods)
  - DaemonSet - when this process is stopped it will clear out the pods too
  - jobs
  - Service - provide a endpoint and an IP which will be same throught the service life cycle.
    - internal ip (used by the K8s cluster - called cluster IP, different cluster node interacts with each other using this)
    - external ip (endpoint where the IP is exposed to internet)
      - there is Loadbalancer, which is provided by the Cloud providers like AWS, Azure, etc.

# Labels, selectors and namespace
  - Labels 
    - key value pair provided to a an object in k8s to identify or logically group the pods, can be identified using kubectl
  - Selectors
    - Equality-based
      - equals and not equal
    - Set-based
      - in, not in, exists 
 _Note: **Labels and Selectors** is powerful for logically identifying pods using kubectl, either to list or filter objects_
 
  - Namespace
    - Feature of k8s have multiple virtual cluster backed by same physical cluster.
    - Different team can use different namespace, which provides accountability for different team to access and run their application
    - One of the best way to divide cluster resources between multiple users which can be done using **resource quotas**
      - Names of resources (deployments and pods) should be unique within the namespace, not necessarliy accross separate namespaces
        - Different team can have an _application name authentication_ in their own namespaces
   
  ###### When launching a K8s, there is a default namespace where all objects is placed. Namespace is allowed to create whenenver the user wishes.

  ###### When a new Kuberntes application is installed, a new _namespace_ is installed, which doesn't interfere with existing cluster. 


# Kubelet
   - is k8s Node agent runs on each node
     - Roles:
       - communicates with the API server (in master node) to check if the pods have been assigned to nodes.
       - Executes the pod container via container engine (example: docker)
       - Mounts and runs pod volumes and secrets
       - Executes health checks to identify node/pod status, reports back to API server.
   - Prodspec YAML file
       - Kubelet gets the Prodspecs from API server and ensures containers described in YAML are running and healthy.
       - Kubelet manages only the containers that were created by the API server. (not any other container running on the node)
       - Tip: The kubelet can be manged from an HTTP end-point or a file (without a need of API server, advanced use cases)

# Kube-proxy
  - The _network proxy_ that runs on all the nodes is called _**kube proxy**_
  - This reflects the services defined in API on each node
     - perform network streaming or round-robin forwarding accross a set of backend nodes
  - Service cluster ip and port are currently found through Docker compitable env variables specifying ports opened by the service proxing
  
  ###### Modes of kube proxy:
    - user space (commonly used mode) 
    - iptables 
    - ipvs mode
 - Kube proxy continously monitors the API server (in master) for addition or removal of services.
 - Kube proxy opens a randomly chosen port on the local mode, for each new service. 
    - Any connection made to tht port are proxied to one of the corresponding backend pods.
    
----------------------

### Kubernetes resources- POD:
 ```
 // app pod resource file should have the apiVerion and kind field
 // metadata is mandtoary for all pod sample and it should have the name filed.
 
  apiVersion: v1  // string identifies the version of the schema this object should have
  kind: pod // this is the schema this object should have, in this case the object is pod
  
  metadata:   // necessary field
     name: firstapp-pod //mandatory field, this is used to uniquely identify the Pod among others, we can specify namespace and uid.
     labels:    /map of string key and values used to orgnize and catagorize object
       app: firstapp
  spec:   //specification and defines the desire state of the object
     containers:  // defines the list of containers belonging to the pod, static list and can't be changed at runtime
     - name: name-of-container // name to the container
       image: alpine //name of the docker image an java application
     
 ```
 
 ### Kubernetes Resources- Deployment:
   - Deployment object provides declarative updates for pods. 
       - Describe the desire state in the deployment object. The `Kubernetes clusters` changes the actual state to desired state at controlled rate. 
        - Multiple replicas of a pod can be created by specifing the number of replica.
        - Roll out a new version of the Pod or roll back to a previous version

```
apiVersion: apps/v1
kind: Deployment  // the schema is different
metadata:
   name: firstapp-deployment
   lables:
     app: firstapp
spec:
  replicas:3
  selector:
     matchLables:
        app: firstapp
     template:
       metadata:
          lables: 
             app: firstapp
          spec:
             containers:
             - name: firstapp-container
               image: myapp
               ports:
               - containerPort: 8080
```

### Kubernetes Resources- Service:
  
  Kubernets Pods are ephimeral, so the Ip address assigned to them cannot be relied upon for application to communicate.
  
  The `Kubernetes Service` provides logical collection of Pods and provides well defined API for application to communicate.
  
  `Kubernetes Service` also provides a simple light-wieght Layer 4 DNS based load balancing for the PODs beloning to the service.  
  
  Pods are loosely coupled using labels, `a service define a lables that must exists on a POD in order to be included as part of the lables`. Deployment will create the PODs using lables.
  
  Pods belonging to a service can be dynamically scalled up and down, the service name provdies a stable endpoint for other Services to refer.
  
  Kubernetes can be exposed outside the cluster, using cloud provided loadbalancer or specific ip address.
  
  ```
  apiVersion: v1
  kind: Service  //scehma or indicates this is service object
  metadata:
     name: firstapp-service
  spec:
    selector: // define label that must match on the pods, to be included in this service any traffic to this service is routed to the included pods.
       app: firstapp
    ports:  // port information
      - port: 80
        targetPort: 8080
  ```
  
  Kubernets Resources:
     
   `DaemonSet`:
        - Ensures that all or some node in the cluster runs the copy of the pod.
        - As nodes added to the cluster, the pods are added to them.
        - As nodes removed from the cluster, the pods are garbage collected.
        - Deleting the DaemonSet will clean up the pods it created.
      
   `Jobs`:
        - Creates one or more pods and ensures the specified number of them are successfully terminitated.
        - As pods successfully completes, the Job tracks successful completion.
        - When the specified number of success completion is reached the job itself will be completed.
        - Deleting the job will clean up the pods it created.
        
   `CronJob`:
        - Creates a Job on time-based schedule. Similar to the crontab and specified in cron format. One Object refers to one line in the resource. 
        
   `StatefulSets`:
        - Workload API object, used to manage statefull application. 
        - It provides gaurantees of ordering and uniquely identifing pods.
        - For example, this can be used for database workloads in containers. 
 
 ---------------
 ### How does a `Kubernetes Cluster` works?
 
 ```
 
Control Plane [Controls the running containers/application in data plane]

   |  Cluster Manager  |                |  Scheduler |

------------------------------------------------------
     
Data Plane [where the runs containers and application]
   
   Containers          containers          containers

 ```
 
Rarely interact with the `Data plane`, most of the controlling happens from the `Control plane`. 
 
 ##### Control Plane
  ```
     Master Node -  responsible for maintaining the desired state of the cluster
     For availability and redendency, recommended to replicate the master node.

     etcd  - core presistent layer, etcd is distributed key value store. Cirtical data for the cluster is stored.  Along with the master etcd needs to be run.
     
     Note: The master node and the etcd can be co-located, which means, for availablity and redendency reasons by co-locating the master and etcd, we can use 3 nodes instead of 6 node.
     This comes with a trade off when upgrading the Kubernetes cluster, we need to make sure the Quroam on etcd or have to reboot instances.
     
 ```
 
 Kubernetes Master:
  ```
   API server [services rest operation and provides a frontend to the cluster shared state thorugh which all other components interacts.]
   
   Controller Manager [Is a Daemon this embeds core control loop came with the kubernetes, it polls the shared state of cluster thorugh the API server and make changes changs to attempt move the current state to desired state.]
       Some of the controller manager:
            - Replication controller
            - Endpoint controller
            - Namespace controller
   
   Scheduler [ Topology aware component]
   
   Cloud Controller [ Daemon ]
   
   Add-ons 
   
   DNS [ provides Name resolution for the cluster]
 ```
  
  `Kubernetes Worker (runs two process)`
  
  ```
   Kubelet (process)
      - communicates with the Kubernetes Master and creates pods.
      - Also ensure, that the container in Pods are running and healthy.
      
  Kube-proxy (process)
      - Network proxy, provides networking service on each node.
      - The Kubernetes master interacts with the nodes.
  ```
  
  How Kubernetes application deployment work?
  
  Both Control plane and Data plane work together in deployment process.
  
  Kubernetes resource manifest is used to define K8s resources.
  
  ```
  
  Kubectl (CLI)                        ---Talks->     Control Plane
  (kubectl create -f deployment.yaml)                      |  <uses internal 
                                                           |  component to create 
                                                           |  resources on data plane>
                                                           ˇ
                                                       Data Plane
  ```
  
  As a developer, repack the Docker image with the new application logic and update the resource manifest file and redeploy the resources.
  
  Multiple K8s resources can be easily deployed in K8s cluster.
  
  `Kubernetes Cluster`:
  Can be deployed in 
      - Desktop
      - On-premises
      - cloud
  
Minikube - a single node K8s cluster, light weight uses Type 2 HyperViser (like virtualbox).

To build docker image use Docker desktop. It seemsly integrates with the Kubernetes.

[Git Hello App Link](https://github.com/thirumurthis/Learnings/blob/master/K8s/K8s_dockerhub_Hello_app.md)


  
  

