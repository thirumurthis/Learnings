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
	| Lables                      |    Identity    |
	-----------------------------------------------
	| ["role=frontend"            |   10           |
	| ["role=backend","role=admin"|  20            |
	
	```

   - A cilium identity is determined by Labels and is unique cluster-wide
   - An endpoint is assigned the identity which matches the endpoint's Security Relevant Labels. i.e. all endpoints which share the same set of Security Relevant Labels will share the same identity.
   - The unique numeric identifer associated with each identity is then used by eBPF programs in very fast lookup in the network datapath, and underpins how Hubber is able to provide Kubernetes-aware network observability.
   - As network packets enter or leave a node, Cilium eBPF programs map the source and destination IP address to appropriate numeric identity number and then decide which datapath action should be taken based on policy configuration referring to hose numeric identifiers.
   - Each cilium agent is responsible for updating identity-relevant eBPF maps with numberic identifiers relevant to endpoints running locally on the node by watching for updates to relevant Kubernetes resources.





