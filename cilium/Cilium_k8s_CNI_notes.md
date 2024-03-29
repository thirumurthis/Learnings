Cilium 

- eBPF is like how javascript is for browser.
- When the user clicks submit button the program gets executed. (very high overview), this is the similar for eBPF.

When the execev() is called, we can run a program. conceptually similar, the level and performace are different.
![image](https://github.com/thirumurthis/Learnings/assets/6425536/74348626-9a70-4d31-8db1-b14bf96a3ef1)

BPF overview
![image](https://github.com/thirumurthis/Learnings/assets/6425536/8e7365eb-9a8f-4840-aae9-ae0be8834fb5)


Cilium Architecture:
![image](https://github.com/thirumurthis/Learnings/assets/6425536/1145efd4-0ad7-481e-a204-22a0c2a58872)


Installation
 - Cilicum CLI tool 
          - this uses kubernetes API directly to examine the cluster corresponding to an existing kubectl context.
 - helm chart 
          - this is meant for advanced installtiona and production environment where granular control for Cilim installation is reqired.
		  - this requires to manually select the best datapath and IPAM mode for particular k8s environment.

Setting up environment locally:
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

--------------

Hubble:

Hubble can provide insights on below:

Service dependencies & communication map:

What and how frequently ther services are communicating with each other? 
Service dependency graph
What HTTP calls are being made? 
What Kafka topics does a service consume from or produce to?

Network monitoring & alerting:

Provides which network communication failed? Why communication failing? 
Is it DNS or it is an application or network problem? 
Is the communication broken on layer 4 (TCP) or layer 7 (HTTP)?
Which services have experienced a DNS resolution problem in the last 5 minutes? 
Which services have experienced an interrupted TCP connection recently or have seen connections timing out?
What is the rate of unanswered TCP SYN requests?

Application monitoring:

What is the rate of 5xx or 4xx HTTP response codes for a particular service or across all clusters?
What is the 95th and 99th percentile latency between HTTP requests and responses in my cluster? 
Which services are performing the worst? What is the latency between services?

Security observability:

Which services had connections blocked due to network policy? 
What services have been accessed from outside the cluster? 
Which services have resolved a particular DNS name?

All of this is made possible by eBPF, which provides deep visibility into the network datapath for all the application workloads running in your Kubernetes cluster. With Hubble, you are able to tap into this flow of information and filter it using contextual Kubernetes metadata.

*Hubble CLI*
```sh
$ hubble observe --since=1m -t l7 -j
```

-----
When Hubble is enabled as part of your Cilium-managed cluster, several additional components are configured:

Hubble server:
  - Runs on each Kubernetes node as part of Cilium agent operations
  - Implements the gRPC observer service, which provides access to network flows on a node
  - Implements the gRPC peer service used by Hubble Relay to discover peer Hubble servers

Hubble Peer Kubernetes Service:
  - Used by Hubble Relay to discover available Hubble servers in the cluster

Hubble Relay Kubernetes Deployment:
  - Communicates with Cluster-wide Hubble Peer service to discover Hubble servers in the cluster
  - Keeps persistent connections with Hubble server gRPC API
  - Exposes API for cluster-wide observability

Hubble Relay Kubernetes Service:
  - Used by the Hubble UI service
  - Can be exposed for use by Hubble CLI tool

Hubble UI Kubernetes Deployment
  - Act as a service backend for Hubble UI Kubernetes service

Hubble UI Kubernetes Service
  - Used for cluster networking visualizations

---

`Network flows` are a key concept in Hubble’s value. 
`Flows` are similar to a `packet capture` that you would get from a tool like *tcpdump*, except instead of focusing on the packet contents, flows are designed to help you better understand how packets flow through your Cilium-managed Kubernetes cluster.

`Flows` include contextual information that helps you figure out 
   - where in the cluster a network packet is coming from,
   - where it’s going, and
   - whether it was dropped or forwarded;
without having to rely on knowing the source and destination IP addresses of the packet.

Since Kubernetes pod IP addresses are ephemeral resources, they aren’t reliable enough to filter on. 
Simply capturing packets from the same IP address for several minutes, as you would do with a VM running in a data center, may not help diagnose Kubernetes pod networking issues and it certainly won’t help you construct trendable metrics for any application. `Flows` provide contextual metadata that is more durable inside of a Kubernetes cluster that you can filter on.

Moreover, you can expose the context made available by flows as labels in Prometheus metrics.
Flows make it possible to have metrics in your networking dashboard labeled in a way that corresponds to your application performance, even as they scale up or down. The metric labels derived from flows are another great benefit of Cilium’s identity model that just makes sense in a Cloud Native world, we’ll take a closer look at that in the next chapter.

Here’s an abbreviated example of a flow we’ve captured using the Hubble CLI tool so you can see what contextual metadata is available in a flow. This flow is from the Death Star service endpoint node, created when the TIE fighter pod was denied a PUT request made in the last chapter’s lab. The flow is captured using the Hubble CLI tool running on the same Kubernetes node as a Death Star backend endpoint.

```json
{
      "time": "2023-03-23T22:35:16.365272245Z",
      "verdict": "DROPPED",
      "Type": "L7",
      "node_name": "kind-kind/kind-worker",
      "event_type": {
           "type": 129
      },
      "traffic_direction": "INGRESS",
      "is_reply": false,
      "Summary": "HTTP/1.1 PUT http://deathstar.default.svc.cluster.local/v1/exhaust-port"
      "IP": {
"source": "10.244.2.73",
           "destination": "10.244.2.157",
           "ipVersion": "IPv4"
     },
     "source": {
          "ID": 62,
          "identity": 63675,
          "namespace": "default",
          "labels": [...],
          "pod_name": "tiefighter"
     },
     "destination": {
                "ID": 601,
                "identity": 25788,
                "namespace": "default",
                "labels": [...],
                "pod_name": "deathstar-54bb8475cc-d8ww7",
                "workloads": [...]
     },
     "l4": {
          "TCP": {
               "source_port": 45676,
               "destination_port": 80
            }
     },
     "l7": {
          "type": "REQUEST",
          "http": {
               "method": "PUT",
               "Url": "http://deathstar.default.svc.cluster.local/v1/exhaust-port",
               ...
          }
     }
}
```

There’s a lot of contextual information packed into a flow. Looking just at the top of the JSON object, from the Verdict and Type information we can see this flow resulted in a packet drop, associated with Layer 7 network policy logic. If we inspect the flow of the same PUT request made by an X-wing pod, we would see the same DROPPED verdict, but the TYPE would be L3_L4, because the X-wing connection to the exhaust-port was denied due to Layer 3-4 policy, not the Layer 7 policy we crafted in the previous lab. Skimming further through the JSON object, there’s a lot more information in the flows that can help diagnose networking problems quickly.

-----

Below command is used to filter the flow 
```
kubectl -n kube-system exec -ti pod/cilium-w7r54 -c cilium-agent -- hubble observe --from-label "class=tiefighter" --to-label "class=deathstar" --verdict DROPPED --last 1 -o json
```

This command makes use of the --from-label filter to make sure only flows with TIE fighter pods as the source are considered. The `--to-label` filter is used to make sure only flows with Death Star endpoint as the destination are considered. The `--verdict` filter is used to ensure only flows for DROPPED packets are considered.

---
After installing the Hubble CLI

- We can get access to the cluster-wide Hubble Relay service, we need to expose the Hubble Relay service to our workstation. We can do this using `kubectl port-forwarding` or just by using the Cilium CLI, which runs kubectl on our behalf. You may still have a Hubble Relay port-forward active on your workstation 
 
```
cilium hubble port-forward & hubble status
```

- Below is the output using the `hubble` CLI.

```
hubble observe --to-label "class=deathstar" --verdict DROPPED --all
Mar 24 01:41:14.758: default/tiefighter:58374 (ID:63675) -> default/deathstar-54bb8475cc-4pv5c:80 (ID:25788) http-request DROPPED (HTTP/1.1 PUT http://deathstar.default.svc.cluster.local/v1/exhaust-port)
Mar 24 01:41:17.254: default/tiefighter:48852 (ID:63675) -> default/deathstar-54bb8475cc-d8ww7:80 (ID:25788) http-request DROPPED (HTTP/1.1 PUT http://deathstar.default.svc.cluster.local/v1/exhaust-port)
Mar 24 01:41:26.495: default/xwing:59940 (ID:828) <> default/deathstar-54bb8475cc-d8ww7:80 (ID:25788) policy-verdict:none INGRESS DENIED (TCP Flags: SYN)
Mar 24 01:43:38.458: default/xwing:42378 (ID:828) <> default/deathstar-54bb8475cc-4pv5c:80 (ID:25788) policy-verdict:none INGRESS DENIED (TCP Flags: SYN)
```

- We can get access to the Hubble ui

```
cilium hubble ui
```

- Applying the DNS Egress policy to deny

To narrowly target the X-wing access to the DNS service, we can craft an explicit EgressDeny policy that applies only to the X-wing pods.

Save this CiliumNetworkPolicy definition to a file called xwing-dns-deny-policy.yaml:

```yaml
apiVersion: "cilium.io/v2"
kind: CiliumNetworkPolicy
metadata:
  name: "xwing-dns-deny"
spec:
  endpointSelector:
    matchLabels:
      class: xwing
  egressDeny:
  - toEndpoints:
    - matchLabels:
        namespace: kube-system
        k8s-app: kube-dns
```
The policy applies to all pods labeled "class=xwing" and will restrict packet egress to any endpoint labeled as "k8s-app=kube-dns" in the kube-system namespace.

- After applying the above policy

- If  invoke the API
```
kubectl exec xwing -- curl --connect-timeout 2 -s -XPOST deathstar.default.svc.cluster.local/v1/request-landing
command terminated with exit code 6
```

- view the traffic with hubble cli, we now see packets are being dropped, because this was a narrowly targeted EgressDeny policy.

```
hubble observe --label="class=xwing" --to-namespace "kube-system" --last 1
Mar 24 20:37:24.013: default/xwing:52779 (ID:828) <> kube-system/coredns-565d847f94-9gddx:53 (ID:49917) Policy denied DROPPED (UDP)
```

```
kubectl exec tiefighter -- curl --connect-timeout 2 -s -XPOST deathstar.default.svc.cluster.local/v1/request-landing
Ship landed
```
```
hubble observe --label="class=tiefighter" --to-namespace "kube-system" --last 1
Mar 24 20:36:06.285: default/tiefighter:37248 (ID:63675) -> kube-system/coredns-565d847f94-9gddx:53 (ID:49917) to-endpoint FORWARDED (UDP)
```

Note: You could achieve a similar result by changing the policy from an EgressDeny policy applied to the X-wing pods to an IngressDeny policy applied to the pods acting as kube-dns service endpoints. There’s always some flexibility in how you approach policy when both the source and destination are Cilium-managed endpoints.

----------

Transport encryption:

Cilium supports both IPsec and WireGuard to transparently encrypt traffic between nodes.

what is Transparent Encryption?

Microservices inside your cluster typically communicate across Kubernetes nodes that may be physically separated over a network that you may not have full control over. 
By encrypting the traffic between nodes in your cluster, you no longer have to implicitly trust the network itself, and you don’t have to worry about other systems sharing the network being able to read sensitive information passed between microservices in different nodes in your cluster.

In fact, certain regulation frameworks, such as PCI and HIPAA, have started to require encryption of data transmitted between networked services. Implementing transparent encryption provides a straightforward path forward to meeting those requirements.

Kubernetes doesn’t have a native feature to encrypt data in-transit inside the cluster. It’s up to cluster admins to decide how this will be implemented. Fortunately, Cilium offers an easy way to enable transparent node-to-node encryption, using WireGuard or IPSec, without having to modify any microservice application code or configuration. We'll be using WireGuard-based transparent encryption in the course labs.

------

Why to use WireGaurd or IPSec?

WireGuard and IPsec are both protocols to provide in-kernel transparent traffic encryption.

WireGuard is a simple to use, lightweight, virtual Private Network (VPN) solution that is built into the Linux kernel. WireGuard is a peer-based VPN solution. A VPN connection is made between peers simply by exchanging public keys (like exchanging SSH keys).

IPsec is a similar, older and FIPS-compliant solution.

When WireGuard or IPsec is enabled in Cilium, the Cilium agent running on each cluster node will establish a secure tunnel to other Cilium-managed nodes in the 

---

It is incredibly easy to set up transparent encryption in an existing Cilium-managed cluster.

Assuming you installed Cilium 1.13 using Helm in the previous chapter, you can enable transparent encryption in that existing Helm-managed cluster with:

```
helm upgrade cilium cilium/cilium --namespace kube-system \
  --reuse-values \
  --set l7Proxy=false \
  --set encryption.enabled=true \
  --set encryption.type=wireguard
```

And restart the cilium daemonset:

```
kubectl rollout restart daemonset/cilium -n kube-system
cilium status --wait
```

If you want to do a fresh Cilium install with the Cilium CLI tool, you can install with WireGuard encryption enabled using this command:

```
cilium install --encryption wireguard
```

Note: In Cilium 1.13, the WireGuard transparent encryption feature is incompatible with the embedded L7 HTTP proxy feature. We anticipate that future versions will no longer need to disable L7 proxy support when enabling encryption.

-----

Let’s enable WireGuard encryption using Helm:

```
helm upgrade cilium cilium/cilium --namespace kube-system \
  --reuse-values \
  --set l7Proxy=false \
  --set encryption.enabled=true \
  --set encryption.type=wireguard
```

Now let’s restart the Cilium daemonset to ensure the running Cilium agents pick up the configuration change.

```
kubectl rollout restart daemonset/cilium -n kube-system
```

Once the Cilium pods are restarted, they’ll update the CiliumNode custom resources that represent Cilium-managed Kubernetes nodes to include a new annotation holding the WireGuard public key for that node.

```
kubectl get -n kube-system CiliumNodes
NAME                  CILIUMINTERNALIP   INTERNALIP   AGE
kind-control-plane    10.0.1.213         10.89.0.7    18h
kind-worker           10.0.2.26          10.89.0.8    18h
kind-worker2          10.0.0.117         10.89.0.6    18h
```

```
kubectl get -n kube-system CiliumNode kind-worker -o json | jq .metadata.annotations
{
  "network.cilium.io/wg-pub-key": "a1PgUa1Bj/vLlDUDQ5qJP/uboCIYKq50GuPScmA7BXw="
}
```
--- 
To verify the Transparent Encryption:

We can use the Cilium agent clients available in the Cilium agent pods to check to see if the Agent has enabled encryption.

This agent has WireGuard enabled, and it sees the expected number of peers (2 peers in a 3 node cluster). Double check the number of peers makes sense for your cluster.


```
kubectl exec -n kube-system -ti ds/cilium -- cilium status |grep Encryption
Encryption: Wireguard [cilium_wg0 (Pubkey: a1PgUa1Bj/vLlDUDQ5qJP/uboCIYKq50GuPScmA7BXw=, Port: 51871, Peers: 2)]
```

If we check the IP devices available in the host network, we should see a new device named cilium_wg0:

```
kubectl exec -n kube-system -ti ds/cilium -- ip link |grep cilium
```

Let’s now verify that this new WireGuard link is being used for traffic destined for another node by making some Death Star landing requests while watching that device with tcpdump.

Below is the step to open interactive bash shell in the cilium agent container and install tcpdump:

```
kubectl exec -n kube-system -ti pod/cilium-2j6zl -- /bin/bash
root@kind-worker:/home/cilium#
```

```
apt-get update
apt-get -y install tcpdump
```

Once installed, we can watch the packet flow through the WireGuard network device:
```
tcpdump -n -i cilium_wg0
```
```
kubectl describe node kind-worker
```

If we make landing requests from the TIE fighter pod, we should see network traffic in the tcpdump output only when the TIE fighter communicates with the Death Star endpoint not running on the same node. Thus the tcpdump session will only see some of the requests. Let’s run the TIE fighter landing request a few times:

```
kubectl exec tiefighter -- curl -s -XPOST deathstar.default.svc.cluster.local/v1/request-landing
```

Here is what our tcpdump session captured after two successful landing requests:

```
20:56:24.842905 IP 10.0.2.188.53622 > 10.0.0.104.53: 64461+ A? deathstar.default.svc.cluster.local. (53)
20:56:24.843493 IP 10.0.2.188.53622 > 10.0.0.104.53: 64835+ AAAA? deathstar.default.svc.cluster.local. (53)
20:56:24.843976 IP 10.0.0.104.53 > 10.0.2.188.53622: 64461*- 1/0/0 A 10.96.129.196 (104)
```

On the first request, all we see is communication to a DNS server, which makes sense because in our cluster there is no core-dns pod running on the node shared by the TIE fighter pod. Thus, every landing request we make will result in a DNS request to a different node, and that request is now transparently encrypted via WireGuard, without having to reconfigure the DNS service to enable encryption. Cool!

However, there is no evidence of a connection to the Death Star backend endpoint on this first request. That is because the DNS request returned the backend address running on the same node as the TIE fighter. The WireGuard link we are watching wasn’t used because that connection ended up being node-local between pods running on the same node.

On the second landing request, we see both the DNS request and the HTTP request show up in the tcpdump session. For the second landing request, the DNS lookup returned the Deathstar backend running on a different node, and the encrypted WireGuard link was used for the node-to-node communication. Great! It’s working just as expected. Cilium is now providing transparent node-to-node communication, without making any changes to the deployed microservice workloads in the cluster, and without having to add resource-intensive sidecars into each pod.

If you repeat that process and attempt to interact with an IP address for an external service, there will be no traffic on the WireGuard link. Be careful though, if you access a service by DNS name, you still may see DNS request traffic to the kube-dns service on the encrypted WireGuard link.

-------------

Benefits Replacing kube-proxy:

CNI plugins, like Cilium, aren’t the only place where the container’s networking configuration is modified. The kube-proxy daemonset, which implements part of the Kubernetes services model, also interacts with the Linux container networking stack. kube-proxy adjusts the iptables ruleset controlling load balancing of Kubernetes services to pods acting as service endpoints using forwarding rules for virtual IP addresses. In fact, kube-proxy installs multiple iptables rules for each backend a service is serving. For each service added to Kubernetes, the list of iptables rules to be traversed grows exponentially! This can have serious performance impacts at large production scales.

With eBPF, it's possible to replace kube-proxy entirely with Cilium, so that eBPF programs are performing the same service endpoint load balancing for increased scalability. Once you’ve replaced kube-proxy with Cilium, you can significantly reduce the amount of churn in iptables rules, save on resource overhead, and speed up your cluster scaling

Kubeproxy functionality:

By default, Cilium only handles per-packet in-cluster load-balancing of ClusterIP services and kube-proxy is used for handling services of types NodePort and LoadBalancer and handling service ExternalIPs. Cilium’s eBPF-based kube-proxy replacement takes over handling of all service types and service ExternalIP handling. In addition, once kube-proxy replacement is enabled, Cilium will also handle HostPort allocations for containers with HostPort defined.

Enabling kube-proxy replacement:

The simplest way to enable Cilium’s kube-proxy replacement is to use the Cilium CLI tool to install Cilium on a fresh cluster prepared without kube-proxy enabled. The Cilium CLI tool will detect that the kube-proxy is not configured and automatically make the necessary Helm template configuration adjustments to the resource manifests it uses to install Cilium into the cluster.

If you are using Helm to facilitate the Cilium install, you’ll need to explicitly set a few Helm chart options that the Ciium CLI tool is able to auto-detect:

```
API_SERVER_IP=<your_api_server_ip>
API_SERVER_PORT=<your_api_server_port>
helm install cilium cilium/cilium --version 1.13.1 \
     --namespace kube-system \
     --set kubeProxyReplacement=strict \
     --set k8sServiceHost=${API_SERVER_IP} \
     --set k8sServicePort=${API_SERVER_PORT}
```

If you have kube-proxy already installed on the system, you’ll need to take additional steps to make sure kube-proxy is removed from the cluster, including any iptables rules kube-proxy has installed into the cluster nodes. Details on how to fully remove kube-proxy from a cluster are provided in the Cilium documentation.

Note: There are advanced workload situations where operators may want to use kube-proxy for some functions but rely on Cilium for others. It’s also possible to configure Cilium as a partial kube-proxy replacement, specifying which of the kube-proxy functions you want Cilium to be responible.

For this lab, we’ll need to start with a Kubernetes cluster created without kube-proxy installed. The details of how to do that will depend on the specific Kubernetes service you are using.

For convenience, here is a reference kind cluster configuration that you can use for a local lab cluster environment:

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
- role: worker
- role: worker
networking:
  disableDefaultCNI: true
  kubeProxyMode: none
```

Save this to the file kind-no-kp-config.yaml and then create a new kind cluster using the config:

```
kind create cluster --config kind-no-kp-config.yaml
Creating cluster "kind" ...
You can now use your cluster with:
```

```
kubectl cluster-info --context kind-kind
```

Now let’s confirm the cluster does not have kube-proxy installed:

```
kubectl get --all-namespaces daemonsets | grep kube-proxy
kubectl get --all-namespaces pods | grep kube-proxy
kubectl get --all-namespaces configmaps |grep kube-proxy
```
These should all return empty result

- Install Cilium with Kube-Proxy Replacement Enabled

Now, with the cluster prepared without kube-proxy, we can install Cilium using the Cilium CLI tool. It will auto-detect the need for the kube-proxy replacement and configure the Cilium Helm chart settings appropriately.

```
cilium install
```

We can first validate that the Cilium agent is running in the desired mode:

```
kubectl -n kube-system exec ds/cilium -- cilium status | grep KubeProxyReplacement
KubeProxyReplacement: Strict [eth0 10.89.0.15]
```

To validate that Cilium has taken over the duties normally performed by kube-proxy, you can create a NodePort service and validate it is still operating correctly. We’re using a NodePort service explicitly because NodePort service port allocation is one of the functions kube-proxy performs unless Cilium is configured to take over that functionality from kube-proxy.

In fact, the Cilium CLI tool’s connectivity tests includes a series of NodePort tests that are skipped by default unless the Cilium CLI tool detects Cilium has been configured to take over the NodePort service port allocation as part of kube-proxy replacement. To validate the kube-proxy replacement is working, we can use the Cilium CLI tool and have it run the full set of connectivity tests now and we should see NodePort services being created for testing:

```
cilium connectivity test --connect-timeout 30s --request-timeout 30s
```

Note: The full set of connectivity tests can take several minutes to complete.

Alternatively, we can test the NodePort connectivity manually, by deploying our own NodePort service and make sure it's operating correctly. We can start by creating a simple Nginx deployment and related NodePort service:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-nginx
spec:
  selector:
    matchLabels:
      run: my-nginx
  replicas: 2
  template:
    metadata:
      labels:
        run: my-nginx
    spec:
      containers:
        - name: my-nginx
          image: nginx
          ports:
            - containerPort: 80
```
Save this to the file my-nginx.yaml.

Apply the my-nginx.yaml deployment manifest to your cluster:

```
kubectl apply -f my-nginx.yaml
```

and verify that the Nginx pods are up and running:

```
kubectl get pods -l run=my-nginx -o wide
NAME                      READY  STATUS            RESTARTS AGE  IP     NODE         NOMINATED NODE  READINESS GATES
my-nginx-77d5cb496b-n6jmb  0/1   ContainerCreating     0    54s  <none> kind-worker  <none>          <none>
my-nginx-77d5cb496b-v7m4m  0/1   ContainerCreating     0    54s  <none> kind-worker2 <none>          <none>
```

Now we create the NodePort service:

```
kubectl expose deployment my-nginx --type=NodePort --port=80
service/my-nginx exposed
```
And verify the service exists:

```
kubectl get svc my-nginx
NAME      TYPE      CLUSTER-IP     EXTERNAL-IP  PORT(S)       AGE
my-nginx  NodePort  10.96.162.217  <none>       80:32166/TCP  54s
```

With the help of the Cilium client’s service list command, we can validate that Cilium’s eBPF kube-proxy replacement created the new NodePort service:

```
kubectl -n kube-system exec ds/cilium -- cilium service list
ID   Frontend          Service Type     Backend
1    10.96.0.1:443     ClusterIP        1 => 10.89.0.17:6443 (active)
2    10.96.0.10:53     ClusterIP        1 => 10.244.1.54:53 (active)
                                        2 => 10.244.1.31:53 (active)
3    10.96.0.10:9153   ClusterIP        1 => 10.244.1.54:9153 (active)
                                        2 => 10.244.1.31:9153 (active)
4    10.96.162.217:80  ClusterIP        1 => 10.244.2.209:80 (active)
                                        2 => 10.244.1.111:80 (active)
5    10.89.0.15:32166  NodePort         1 => 10.244.2.209:80 (active)
                                        2 => 10.244.1.111:80 (active)
6    0.0.0.0:32166     NodePort         1 => 10.244.2.209:80 (active)
                                        2 => 10.244.1.111:80 (active)
```

Here we see NodePort Cilium services were created corresponding to the listed service NodePort of 32166 that are directed to each of the Nginx backend services at port 80.

Note: This same NodePort is now available on all the nodes managed by Cilium and will redirect to one of the Nginx backends.

We can now curl from any of the Cilium agent containers, using the local port corresponding to the NodePort definition as a final verification that it's working. First, let’s get shell access to one of the Cilium agent containers:

```
kubectl -n kube-system exec -ti ds/cilium -- /bin/bash
root@kind-worker:/home/cilium#
```

Then let’s install curl into the container:

```
apt update
apt install curl
```

We can now use curl from the Cilium agent container to confirm the NodePort service port results in a response from one of the Nginx servers:

```
curl http://localhost:32166
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and working. Further configuration is required.</p>

<p>For online documentation and support please refer to <a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at <a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

What’s more, we can confirm from the Cilium agent container that there are no iptables rules associated with this service definition:

iptables-save | grep KUBE-SVC

This should return nothing, indicating there are no iptables rules associated with the NodePort service; this is now being managed entirely by Cilium.

