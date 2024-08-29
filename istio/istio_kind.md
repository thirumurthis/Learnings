
Kind configuration

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
- role: worker
  extraPortMappings:
  - containerPort: 31000
    hostPort: 8180
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31043
    hostPort: 9443
    listenAddress: "127.0.0.1"
    protocol: TCP
  - containerPort: 31021
    hostPort: 15021
    listenAddress: "127.0.0.1"
    protocol: TCP
```

# deploy steps

kind create cluster --name istio-testin

-- helm add repo
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update

-- create a folder charts
mkdir charts
cd charts 

-- download install helm base
helm pull istio/base --untar

-- download install helm istiod
helm pull istio/istiod --untar

-- download install helm Istio gateway
helm pull istio/gateway --untar


-- deploy the Istio/base
-- navigate to charts/base
cd charts/base
helm upgrade -i istio-base . -n istio-system --create-namespace

-- deploy the Istio/istiod
-- navigate to charts/istiod
cd charts/istiod
helm upgrade -i istiod . -n istio-system --wait


--deploy the istio/gateway
-- navigate to gateway
cd charts/gateway
helm upgrade -i istio-gateway . -n istio-system

---------
-- create namespace and add the label for injecting side car for pods

kubectl label ns default istio-injection=enabled --overwrite

-- use below command to check if istiod pods are ready

kubectl wait pods --for=condition=Ready -l app=istiod -n istio-system

-- apply patch (NOTE THIS IS NOT OVERRIDING) Better to use kubectl edit

kubectl patch service istio-gateway -n istio-system --patch-file NodePortPatch.yaml
