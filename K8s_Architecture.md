# Nodes and Pods

Nodes :- 
A K8s node should run
    - kubelet process
    - Docker container (or pod) environment
    - kube-proxy process
    - supervisord

Pods:
   - The application
   
   
# Controllers
  - Deployment
  - ReplicaSet - (used along with the Deployment controller, which states at any point the K8s to run the specified number of pods)
  - DaemonSet - when this process is stopped it will clear out the pods too
  - jobs
  - Service - provide a endpoint and an IP which will be same throught the service life cycle.
    - internal ip (used by the K8s cluster - called cluster IP, different cluster node interacts with each other using this)
    - external ip (endpoint where the IP is exposed to internet)
      - there is Loadbalancer, which is provided by the Cloud providers like AWS, Azure, etc.
      
