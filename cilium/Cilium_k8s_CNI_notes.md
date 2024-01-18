Cilium 


installation
 - Cilicum CLI tool 
          - this uses kubernetes API directly to examine the cluster corresponding to an existing kubectl context.
 - helm chart 
          - this is meant for advanced installtiona and production environment where granular control for Cilim installation is reqired.
		  - this requires to manually select the best datapath and IPAM mode for particular k8s environment.

for this lab we need
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



