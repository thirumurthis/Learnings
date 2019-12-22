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
   
  ##### When launching a K8s, there is a default namespace where all objects is placed. Namespace is allowed to create whenenver the user wishes.




      
    
   
