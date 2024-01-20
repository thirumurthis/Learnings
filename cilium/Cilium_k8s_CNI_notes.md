Cilium 


installation
 - Cilicum CLI tool 
          - this uses kubernetes API directly to examine the cluster corresponding to an existing kubectl context.
 - helm chart 
          - this is meant for advanced installtiona and production environment where granular control for Cilim installation is reqired.
		  - this requires to manually select the best datapath and IPAM mode for particular k8s environment.

setting up environment locally:
 - k8s cluster appropriately configured and ready for an external CNI to be instaled.
 - in here we can configure in local using kind cluster as first step. (check the cilium doc)
 - the kubectl to be installed.
 
Download and install Kind.

- Yaml configuration file for 3- node kind cluster (with default CNI disabled)

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
  - role: worker
  - role: worker
networking:
  disableDefaultCNI: true
```

- To exeucte the above yaml content save it in a file and use below command

```
kind create cluster --config kindClusterConfig.yaml
```

- once command is completed, use below command to check the context

```
kubectl config current-context
```

or 

```
kubectl config get-contexts
```
 
- use below command to see if the nodes are correct using 

```
kubectl get nodes
```

### installing the Cilium cli
- Download the executable and install it in system.
- use below command to check the version
```
cilium version
```

- issue below command to intall Cilium

```
cilium install
```
  - Above command takes couple of minutes to complete the installation process.
  - From another terminal use Cilium cli command to watch the status

```
cilium status --wait
```

- Once the cilium is installed, we can enable `hubble`

```
cilium hubble enable --ui
```
  - Above command will reconfigure and restart the Cilium agent to ensure they hubble embedded service is enabled.
  - The command also installs cluster-wide Hubble components to enable cluster-wide network observability.

- To verify status of the Hubble
  
```
cilium status
```

- We can use the Cilium CLI tool to perform some connectivity tests.
  - Cilium CLI tools provdes command to install a set of conectivity tests in a dedicated k8s namespace
  - we can run these tests to validate that the Cilium install is fully operational
    
```
cilium connectivity test --request-timeout 30s --connect-timeout 10s

# should see the status of the test pass or failed in the output
```
   - There are dozens of connectivity tests suite, making sure aspects of network and policy enforcement work as expected.
   - The number of connectivity test varies from version to version in case there is an Cilium upate installed in cluster.
   - The connectivty test takes additional time to run at the first time, as they need to download container images needed for test deploymentes. Subsequent runs might be taking less time.
   - Test takes at leasr 10 minutes
   - Connectivity test requires at least two worker nodes to successfully deploy in cluster.
   - *The connectivity test pods will not be scehduled on nodes operating in the control-plane role*.
   - If the cluster did not provisioned with 2 worker nodes, the connectivity test will be waiting for the test environment to complete.

- Cilium installed in the cluster, confirm it in the cluster.

```
kubectl get nodes

kubectl get daemonstes --all-namespaces
# output shows cilium resource, this should be running all 3 nodes 

kubectl get deployments --all-namespaces
# the output shows cilium-test resources and cilium operator running.
```
-------------

Components in Cilium:

Cilium Operator: 
   - Is responsible for managing duties in the cluster which should logically handled once for the entire cluster.
   - This is not in critical path for any network traffic forwarding or network policy decision.
   - The cluster still continue to function if the operator is temporarily unavailable.

Cilium Agent :
   - This runs as `daemonset` so there is Cilium agent pod running on every node in the k8s cluster.
   - The agent performs most of the work associated with Cilium like,
      - interacts with kuberneted API server to synchronize cluster state
      - interacts with Linux kernel - loading eBPF programs and updating eBPF maps
      - interacts with Cilium CNI plugin executable, via filesystem socket, to get notified of newly scheduled workload
      - creates on-demand `DNS` and `Envoy proxies` as needed based on requested network policy
      - creates Hubble gRPC services when Hubble is enabled

Cilium client:
   - Cilium agent daemonset in each pod includes a Cilium client executable that can be used to,
       - inspect the state of cilium agent
       - inspect eBPF maps resources installed on the node
   - The client communicates with Cilium agent's REST API from inside daemonset pod.
   - Note: This client is not same as the Cilium CLI installed in the machine.
   - The Cilium client executable is included in each Cilium agent pod, and can be used as a diagnostic tool to troubleshoot Cilium operation if needed.
   - We rarely interact with the Cilium client as part of normal operation.

Cilium CNI plugin:
  - The Cilium agent daemonset also installs the Cilium CNI plugin into the kubernetes host filesystem and reconfigures the node's CNI to make use of the plugin.
  - The CNI plugin executable is separate from the Cilium agent, and is installed as part of the agent daemonset initialization
  - when required Cilium CNI plugin communicates with the running Cilium agent using a host filesystem socket.

Hubble relay: 
  - When Hubble is enabled as part of the Cilium-managed cluster, the Cilium agents running on each node are `restarted` to enable the Hubble gRPC service to provide node-local obeservability.
  - For `cluster-wide obeservability`, a `Hubble Relay` deployment is added to the cluster along with two addition services - `Hubble Observer service` and `the Hubble Peer service`.
  - Hubble Relay deployment provide cluster-wide obeservability by acting as an intermediary between the `cluster-wide Hubble Observer` and the `gRPC service that each Cilium agent` provides.
  - Hubble Peer service makes it possible for Hubble relay to detect when new Hubble-enabled Cilium agent becomes active in the cluster.
  - As a user, we will typically be interacting with the Hubble Observer service, using either the `Hubble CLI` or `Hubble UI` to get insights of network flow across cluster.

Cilium Cluster Mesh API Server:
  - The Cluster Mesh API server is an optional deployment that is only installed if the Cilium cluster Mesh feature is enabled.
  - Cilium cluster Mesh allows kubernetes services to be shared among multiple clusters.
  - Cilium cluster Mesh deploys an `etcd key-value store` in each cluster, to hold information about Cilium identities.
  - It also exposes proxy service for each of these etcd store.
  - Cilium agent running in any member of the same Cluster Mesh can use this service to read information about Cilium identiy state globally across the mesh.
  - This makes it possible to create and access global servces that span the Cluster Mesh.
  - Once the Cluster Mesh API service is available, Cilium agent running any Kubernetes cluster that is a member of the Cluster Mesh are then able to securely read from each cluster's etcd proxy thus gaining knowledge of Cilium identity state globally accross the mesh.
  - This makes it possible to create global serivce that span the cluster mesh.


Cilium Endpoints:
  - Cilium makes application containres available on the network by assigning them IP addresss.
  - All application containers which share a common IP address are grouped together, which Cilium refers as Endpoint.
  - Endpoints are an internal representation that Cilium uses to efficiently manage container connectivity and will create Endpoints as needed for all containers it manges.
  - And as it turns out, kubernetes pods maps directly to Cilium Endpoints, as kuberntes pods are defined as a group of containres operating in a common set of Linux kernel namespace and sharing an IP address.
  - In cilium manged cluster, Cilium will create an Endpoint for each Kuberentes pod running in the cluster.

Cilium Identities:
  - This is key concept that makes Cilium work as efficiently as it does is Cilium's notion of Identity.
  - All Cilium Endpoints are assigned a lable-based identity
	
	```
	etcd key-value store
	-----------------------------------------------
	| Lables                        |    Identity    |
	-----------------------------------------------
	| ["role=frontend"              |   10           |
	| ["role=backend","role=admin"] |   20           |
	
	```

   - A cilium identity is determined by Labels and is unique cluster-wide
   - An endpoint is assigned the identity which matches the endpoint's Security Relevant Labels. i.e. all endpoints which share the same set of Security Relevant Labels will share the same identity.
   - The unique numeric identifer associated with each identity is then used by eBPF programs in very fast lookup in the network datapath, and underpins how Hubber is able to provide Kubernetes-aware network observability.
   - As network packets enter or leave a node, Cilium eBPF programs map the source and destination IP address to appropriate numeric identity number and then decide which datapath action should be taken based on policy configuration referring to hose numeric identifiers.
   - Each cilium agent is responsible for updating identity-relevant eBPF maps with numberic identifiers relevant to endpoints running locally on the node by watching for updates to relevant Kubernetes resources.

--------------------
Network Policy:

- Better ways to secure Kubernetes workload netowrking by understanding the network policies.
- Label based Layer 3/4 network policy to secure cluster internal communication between pods and service.
- How to write layer 7 network ploicy making use of Cilium agent's embedded HTTP proxy.

Types of Network policy:
   - Network Policies allow users to define what traffic is permitted in a kubernetes cluster. Where traditional firewalls are configured to permit or deny traffic based on source or destination IP address and ports, *Cilium uses kubernetes identity information such as label selectors, namespace names, and even fully qualified domain names for defining rules* about what traffic is permitted and disallowed.
   - This allows network policies to work in a dynamic environment like kuberentes, where IP address are constantly being used and reused for different pods as those pods are created and destroyed.

   - When running Cilium on Kubernetes, we can define network policies using kuberentes resources.
   - Cilium agent will watch the Kuberentes API server for updates to network policies and will load the necessary eBPF programs and maps to ensure the desired network policy is implemented.
   - *Three network plocu formats are availabe* with Cilium enabled Kubernetes:
      - Standard kuberentes `NetworkPolicy` resource which supports Layer 3 and 4 policies
      - The `CiliumNetworkPolicy` resource which supports Layer 3,4 and 7 (application layer) policies
      - The `CiliumClusterWideNetworkPolicy` resource for specifying policies that apply to an entire cluster rather than a specified namespace.
    
  - Cilium supports using all of the policy types at the same time. However, caution should be applied when using multiple policy types, as it can be confusing to understand the complete set of allowed traffic accross multiple polcy types.
  - If close attention is not applied, this may lead to unintended policy behavior.
  - Visualization tool at `networkpolicy.io` can help understand the impact of different policy definitions.

 We will primarily focus on `CiliumNetworkPolcy` resource, as it represents a superset of the standard NetworkPolicy.

- NetworkPolicy resource is a standard kuberentes resource that lets to controll traffic flow at an IP address or port level (OSI level 3 or 4).
- This includes,
    - L3/L4 Ingress and Egress policy using lable matching
    - L3 IP/CIDR Ingress and Egress policy using IP/CIDR for cluster external endpoints
    - L4 TCP and ICMP port ingress and Egress policy
 
`NetworkPolicy.io` policy editor provides a great way to explore and craft L3 and L4 network policy, by providing with a graphical depiction of a cluster and letting you select the correct policy elements scoped for the desired type of network policy.
 - The policy editor supports both standard Kubernetes NteworkPolicy and CiliumNetworkPolicy resources.

![image](https://github.com/thirumurthis/Learnings/assets/6425536/5eb8634b-2e0d-4314-9e79-f98ce06380a2)

 - *Across the top*, there is an interactive service map visualization that you can use to create new policies.
 - The green lines indicate traffic flow that is allowed and the red lines indicate traffic flow that is denied by the current policy defintion.
 - We can configure Ingress and Egress policies targeting either clustet internal or external endpoints using the interactive service map UI.
 - *The lower left*, there is a read-only YAML description of the netwrok policy mathcing teh service map depiction above.
 - we can choose either to view the standard Kuberentes NetworkPolicy specification or the CiliumNetworkPolicy specification.
 - we can also download the policy to apply it to the cluster with kubectl.
 - The existing policy defintion can be uploaded and policy editor will update the visual service map representation to show how it works.
 - The visalization helps to ensure the policy work as intended.
 - *At lower right*, the editor provides tutorial interface populated with common situation to help how to craft policy.
 - we can use the area to upload Hubble flows and generate network policies from what Hubble can observe.
Note: The policy editor isn't yet able to craft the L7 policy tht CiliumNetowrkPolicy supports. To mitigate it, we can craft L3/4 policy and then extedn it to include L7 ingress/egress rules manually.

- The most significant difference between the `CiliumNetworkPolicy` and standard `NetworkPolicy` is support for L7 protocol-aware rules.
- In Cilium, its possible to craft protocol-specific L7 policy for different protocols, including HTTP, Kafka and DNS.
- The Layer 7 policy rules extend the Layer 4 policy `toPorts` secton for both ingres and egress, and as such are relatively easy to add `CiliumNetworkPolicy` yaml manifests crafted with the networkpolicy.io editor.

### What is network policy?
- `L7 HTTP Policy`
   - when any L7 HTTP policy is active for any endpoint running on a node, the Cilium agent on that node will start an embedded local-only HTTP proxy service and the eBPF programs will be instructed to forward packets on to that local HTTP proxy.
   - The HTTP proxy is responsible for interpreting the L7 network policy rules and forwarding the packet further if appropriate.
   - In addition, once the HTTP proxy is in place, we can gain L7 observibility in Hubble flows.
 
   - when writting L7 HTTP policy, there are several fields that the HTTP proxy can be used to match network traffic:
   - `Path` => An extended POSIX regex matched against the conventional path of a URL request. If omitted or empty, all paths are allowed.
   - `Method` => the method of a request, eg. GET, POST, PUT, PATCH, DELETE. If omitted all methods are allowed.
   - `Host` => An extended POSFIX regex mathced against the host header of a request. If omitted or empty, all hosts are allowed.
   - `Header` => A list of HTTP headers that mist be present in the request. If omitted or empty, requests are alloed regardless of the headers present.
 
   - Example below uses several L7 HTTP protocol rules featuring regex path defintion to extend the L4 policy limiting all endpoints which carry the labels app=myService to only be able to receive packets oon port 80 using TCP.
   - While commuincating on this port, the only HTTP API endpoints allowed will be:
        - `GET /v1/path1` => This matches the exact path "/v1/path1"
        - `PUT /v2/path2.*` => This matched all paths starting with "/v2/path2"
        - `POST .*/path3` => This matches all paths ending in "/path3" with the additional constraint that the HTTP header `X-My-Header` must be set to true.

```yaml
apiVersion: "cilium.io/v2"
kind: CiliumNetworkPolicy
metadata:
   name: "L7-rule-spec"
spec:
  endpointSelector:
    matchLables:
      app: myService
  ingress:
  - toPorts:
    - ports:
      - port: '80'
        protocol: TCP
      rules:
        http:
        - method: GET
          path: "/v1/path1"
        - method: PUT
          path: "/v2/path2.*"
          headers:
          - 'X-My-header: true'
```

- The rules block holds the L7 policy logic that extends the L4 policy.
- we can start with an L4 policy and provide granular HTTP API support by just adding the appropriate `rules` block as an attribute in `toPorts` list.

The hands on for educational purpose:

You are part of Empires Platform team taksed with developing a Death Star API to the Imperial Galactic Kubernetes Service. 
You have the service deployed, but you need to ensure only Imperial TIE fighters have access to make landing requests using Death star API via HTTP POST method call and don't have acccess to use the PUT method on any other paths in the API, like the exhast port path.
Not that any TIE fighter pilot would intentainally put anything in the exhaust port, but things happen, and tour team wants to be able to use Ciliums netowrk policy support as a safeguard just in case a TIE fighre pilot has a momentary lapse in judgement.
you really don't want Darth Vader losing faith in your ability to keep the Death Star service secure. 
- goal is to craft a CiliumNetworkPolicy resource to limit access to the Death Star service so TIE fighters can only make HTTP based landing request.

- Say, we had the Death Star application deployed including service definition, service backend pods, and pods acting as TIE figher clients that access the service using internal-only cluster communication.
- Refer the Cilium project which has example of Death Star demo application manifest which can be used.

  ```
  kubectl create -f https://raw.githubusercontent.com/cilium/cilium/HEAD/examples/minikube/http-sw-app.yaml

  # this creas service/deathstar, deployment.apps/deathstar, pod/tiefighter, pod/xwing
  ```
  - we will make sure the network policy denied the X-winfs access to the fuull Death Star service as well.
  - Use below command to check the resources
  ```
  kubectl get services
  ```
  ```
  kubectl get pods,CiliumEndpoints

  # Name                                 Endpoint ID   Identity ID
  cilciumendpoing.cilium.io/deathstar-*     1818         25788
  cilciumendpoing.cilium.io/deathstar-*     601          25788
  cilciumendpoing.cilium.io/tiefighter     62             63670  
  ```

  - Cilium has created endpoinfs corresponding to both Death Star backend port, as well as the Xwing and Tie Figher pods.

  Note:
  - Both `deathstar-*` endpoints share the same IDENTITY ID. This is where pods share the same Cilium Identity because they both have same set of security-relevant labels.
  - Cilium agents will use the Identity Id for endpoints matching relevant network policy to facilitate efficient key-value lookups in the operation of eBPF programs operating in the network datapath.
 
  - Till now the network policy is not deployed, so there should be nothing stopping either X-wing or TIE fighters rom accessing the cluster-internal Death Star service by its FQDN and then having either kube-proxy or Cilium forward the HTTP based landing request to one of the Death Star backend ports.

 - below command can be used to request form both pods
  ```
  kubectl exec xwing -- curl -s -XPOST deathstar.default.svc.cluster.local/v1/request-landing
  kubectl exec tiefighter -- curl -s -XPOST deathstar.default.svc.cluster.local/v1/request-landing
  ```
  
 - WE need to create and apply the network policy.
 - The simplest way to ensure the X-Wing pods don't have acess to Death star service endpoints in this cluster is to write a label-based L3 policy that takes advantage of the different labels used in the pods.
 - A L3 policy would restrict acess to all network ports at the endpoint. if we want to limit access to a specific port number, we can write Label based L4 policy.

 - The xwing pod has the lable org=alliance and tiegighre pod is labeled org=empire.

 - An L4 network policy refereing TCP port 80 that only allows pods labled with org=empire will prevent the xwing pod from acessing the Death Star service endpoints. We use the networkpolicy.io editor

- Edit the namespace and proivide the label, hit enter on for each key value on label. Save the changes will update in the yaml.
![image](https://github.com/thirumurthis/Learnings/assets/6425536/a6972147-6870-4df8-8e51-cb46ef29d521)

- In the interactive service-map UI, create a new policy and add "In Namespace" Ingress ploicy rule that uses org=empire as the pod selector expression and uses 80:TCP as the value for "To Ports" entry field

![image](https://github.com/thirumurthis/Learnings/assets/6425536/1c2ee991-75f7-4982-aa3e-aca897da1bcf)
- After adding the Ingress rules
![image](https://github.com/thirumurthis/Learnings/assets/6425536/b8a92f1d-fb7e-4ec4-a6e4-6909bd2d9469)

- After adding the section will be updated
![image](https://github.com/thirumurthis/Learnings/assets/6425536/752e3281-c5a8-4ad1-a66d-18e97aae0b7e)

- The service map in the policy editor should now indicate that only ingres from the pods labled org=empire in the same namespace as the Death star service will be able to access TCP port 80 on corresponding deathstar endpoints.

   - below is the yaml content from the editor
```yaml
apiVersion: cilium.io/v2
kind: CiliumNetworkPolicy
metadata:
  name: allow-empire-in-namespace
spec:
  endpointSelector:
    matchLabels:
      org: empire
      class: deathstar
  ingress:
    - fromEndpoints:
        - matchLabels:
            org: empire
      toPorts:
        - ports:
            - port: "80"
              protocol: TCP
```
- Note: This L4 policy specifically restricts ingress access to the deathstar-* pods acting as service endpoints and not the Death Star service itself.

- If we want to restrict a pod's egress to a limited number of service, we could create an egress policy for the client pod that reference alloewed services by name in the `toService` attribute of the Egress policy. In our case , that woudl mean wiriting Egress for both xwing and tiefighter pods with differeing `toService` information. for now single ingress policy is sufficient, just allows imperial unit access to the death star API and deny everything else access.

- Save the yaml content to a file and apply to the cluster. Now, if we try to access the service with xwing will not access death star service.
```
  kubectl exec xwing -- curl -s -XPOST deathstar.default.svc.cluster.local/v1/request-landing`
# command terminated with exit code 28
```
- All othe pods labled org=empire still have access to the full API, including exhaust port API endpoint.
```
kubectl exec tiefighter -- curl -s -XPUT deathstar.default.svc.cluster.local/v1/exhaust-port
Panic: deathstar exploded
```
- But we can fix that with L7 HTTP policy that limits access even further, so the exhaust-port API endpoint only availabe to Imperial maintance droid and not hotshot rookie pilots who can't tell a landing bay from an exhaust port.
- we can use CiliumNetworkPolicy Custom resource defintion to restrct access so it doesn't happen again.

```yaml
apiVersion: cilium.io/v2
kind: CiliumNetworkPolicy
metadata:
  name: allow-empire-in-namespace
spec:
  endpointSelector:
    matchLabels:
      org: empire
      class: deathstar
  ingress:
    - fromEndpoints:
        - matchLabels:
            org: empire
      toPorts:
        - ports:
            - port: "80"
              protocol: TCP
    - fromEndpoints:
        - matchLabels:
            org: empire
            class: maintanance-droid
        toPorts:
          - ports:
            - port: "80"
              protocol: TCP
          rules:
            http:
            - method: "PUT"
              path: "/v1/exhaust-port"
```
- save and apply the manifest to cluster. Now, we could see accessing the exhaust-port will throw access deined

```
 kubectl exec tiefighter -- curl -s -XPUT deathstar.default.svc.cluster.local/v1/exhaust-port
```

Note:- xwing trying to access the Death Star API is still denied access via the L4 policy, which has the packets dropped resulting in a conection timeout instead of an HTTP forbidden status message

we have successfully limited access to Death star APU so tie figher can access it,without giving then access to exhaust-port.

Info:
  - The difference in behaviour in how the L3/4 policy and L7 policy handled dropped packets is expected, because of the different impmentation being used.
  - For L3/L4 policy the eBPF programs running in the linux networ datapath are used to drop the packets, essentially eaten by a black hole in the network.
  - The L7 policy is implementing the embedded HTTP proxy and making decisons as if it were an HTTP server, denying requests and providing an HTTP status response back to the client with a raon as to why it was deined.
  - Regardless of the implementation used, we will be able to track that a packet was dropped at the DeathStar endpoint ingress using the hubble to examine network flows.
