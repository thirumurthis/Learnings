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
