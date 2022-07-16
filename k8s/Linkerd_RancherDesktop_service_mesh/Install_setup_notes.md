
### Installing Linkerd in RancherDesktop

In this blog will demonstrate setting up Linkerd service mesh in RancherDesktop.

Will be demonstrating to inject the linkerd proxy to each container when we create a pod.
Linkerd service mesh, injects the linkerd proxy to monitor traffics, etc which will be running as a `sidecar` container.

Pre-requsities:
  - `kubectl` installed from kubernetes.io
  - optionally, `helm CLI` but not necessary.

#### Installing RancherDesktop 

  - Download the RancherDesktop binary for windows and install it, follow the instruction in [rancherdesktop.io](https://docs.rancherdesktop.io/getting-started/installation/). This is very easy.
 
![image](https://user-images.githubusercontent.com/6425536/179358899-22bfc282-b9af-4d12-b4f5-221c35575027.png)

> RancherDesktop uses k3s Kubernetes distribution, developed and maintained by SUSE/Rancher.
> RancherDesktop provides a single node cluster. Currently, multi-node support is not available.
> RancherDesktop also has a Dashboard to view namespace, containers, etc.

### Validate the RancherDesktop created cluster
  
  - Launch the RancherDesktop by clicking the icon.
  - It will take few minutes to start the cluster, progress can be seen in the GUI.

> **Note**
> The RancherDesktop will use the `rancher-desktop` context. 
> In case if you where using DockerDesktop, switch context to `rancher-desktop` using kubectl.

  - Create the first Pod in the cluster,
     - Open a command prompt and issue `kubectl run pod1 --image=nginx`
     - verify the status using `kubectl get pods`, the status of `pod1` should be running.

> **Note**
> - RancherDesktop GUI provides port forwarding option, if we create a service we can easy forward traffic from host laptop. 
> - I will be using the Linkerd Demo application to install and demonstrate how to access the application using port forward option later.

![image](https://user-images.githubusercontent.com/6425536/179358930-c7528fe7-3f01-44cf-8150-a54bb3c2a2bf.png)

#### Install Linkerd Service Mesh

  - Download and Install the Linkerd CLI binary from [linkerd website](https://linkerd.io/2.11/getting-started/#step-1-install-the-cli), [Github release](https://github.com/linkerd/linkerd2/releases/), extract this binary and place in a directory.
  - Add the executable path in the windows Environment path variable, so it will be recognized in command prompt or powershell.

##### Validate Linkerd CLI 

  - If Linkerd CLI is installed correctlly, open command prompt and issue `linkerd version` command this should display the version like in below snippet. 
  
  ```
  Client version: stable-2.11.3
  Server version: stable-2.11.3
  ```
  
##### Install Linkerd service mesh in RancherDesktop cluster.
  
   - Using the linkerd cli, we can generate the deployment yaml, and use kubectl command to install it in the cluster.
   - Details and steps are documented in the linkerd website, refer this [link refence](https://linkerd.io/2.11/getting-started/#step-5-explore-linkerd) for more details.
   
   - We can use below command to install linkerd service mesh, piping the linkerd install output to kubectl directly.
   
   ```
   > linkerd install | kubectl apply -f - 
   ```
   
   - Once installed, we see that resources running under the `linkerd` namespace
 
 ![image](https://user-images.githubusercontent.com/6425536/179359031-f70eb14e-42bc-4a10-bae9-4d77c4f480f7.png)

  
   - To verify and check if everything is installed correctly use `linkerd check` command.

![image](https://user-images.githubusercontent.com/6425536/179358871-fd4e64b8-fadb-445b-9cbf-3c5ca9159fe8.png)   

##### Inject Linkderd proxy - automatically

  - To inject linkerd proxy automatically we need to create a specific `annotation` in the namespace.
  - If hte annotation is available, creating any container on the containers will automaatically inject the proxy.

- To verify, create a namepsace and create an annoation like below

```
# Creating an emojivoto namespace used for linkerd demo application as well
> kubectl creaet namespace emojivoto

# create annotation on the emojivoto namespace
> kubectl annotation namespace emojivoto linkerd.io/inject=enabled
```

- With the above configuration, if we issue `kubectl run pod1 --image=nginx`, it will create a container and we can notice the proxy injected automicatlly.
- Use `kubectl -n emojivoto get pods`, notice the Pod Ready state indicating `2/2`. We can describe the pod and see the porxy injected

![image](https://user-images.githubusercontent.com/6425536/179359481-29773ca8-277e-425e-93db-eb35a88c752f.png)

![image](https://user-images.githubusercontent.com/6425536/179359575-0f2afdfb-7cf4-4751-9328-d3faea709bb5.png)

##### Inject Linkerd proxy - manually
  
  - In order to maunally inject, we need to use linkerd cli command.
  - Once we build the deployment descriptor yaml file, we need to pass the file to `linkerd inject` command

```
# use linkerd inject to mutated the custom deployment with linkerd proxy configuration
> cat mydeployment.yml | linkerd inject > deploymentwithproxy.yml

# deploy using kubectl
> kubectl apply -f deploymentwithproxy.yml
```

#### Monitoring the Linkderd proxy using Linkerd viz dashboard
 
 - We need to install the linkerd viz to the cluster using below command
 - 
```
```


- linkerd check

