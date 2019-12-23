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
    ```
    
    - To view the running application
    ```
    > minikube service <deployment-name>
    ```
     
