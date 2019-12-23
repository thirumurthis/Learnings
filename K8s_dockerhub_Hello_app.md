# First K8s and Docker application
- Install Docker
  - Create a sample spring boot application
     - Dockerfile 
  - validate locally
    ```
     # from the dockerfile location
     > docker build -t <image-name> .
     # running the image locally
     > docker run -d -p 8080:8080 <image-name>
    ```
  - push it to docker hub
  ```
    # create tag  (version or use latest)
    > docker tag <image-name> <username>/<image-name>:<version>
    # push the tag to docker hub
    > docker push <username>/<imange-name>:<version>
  ```
 - If Minikube is setup with kubectl (below deployment and service is opininated)
   - use kubectl command to deploy the image 
   ```
    > kubectl run <deployment-name> --image=<username/imagename>:<version> --port=<port the app runs>
   ```
   - use kubectl commnand to validate the deployment is successfull with below commands
   ```
    > kubectl get deployments
    # get replica sets
    > kubectl get rs
    # get deployment details
    > kubectl describe deployments
   ```
   - once the deployment is successfull expose the service
   ```
    > kubectl expose deployment <deployment-name> --type=NodePort
    # note NodePort is one type, there is another option clusterIP (check the K8s architecture md file for more info)
   ```
    - Minikube dashboard can be used to view the deployment
    ```
    > minikube dashboard
    ```
    
    - To view the running service info
    ```
    > kubectl get services
    
    # output:
    C:\HelloFromDocker>kubectl get services
     NAME            TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
     hello-service   NodePort    10.96.10.33   <none>        8800:31377/TCP   15s
     kubernetes      ClusterIP   10.96.0.1     <none>        443/TCP          7h18m
     
     # note: the 8800 port was exposed in the application
    ```
        
    - To view the running application
    ```
    > minikube service <deployment-name>
    ```
          
 # Kubectl command to get the yaml deployment file command:
 ```
 # in the above example use the below command to list all the info of the deployments and services 
 > kubectl get all 
 
 # use the deployment name from the list to see the yaml file
 > kubectl get <deployment-name> -o yaml
 ```

# Kubectl command to `scale` the container

```
# kubectl scale --replicas=<no-of-replicas> <deploy>/<deployment-name>
> kubectl scale --replicas=3 deploy/hello-service

> kubectl get rs
# output
 NAME                       DESIRED   CURRENT   READY   AGE
 hello-service-64b799fcc8   3         3         3       7h58m
```
