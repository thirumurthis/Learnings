### Create kind cluster


```
kind create cluster --name local-cluster
```


-- once kind cluster is created

### metallb helm installation

```
-- add repo
helm repo add metallb https://metallb.github.io/metallb
```

```
-- update repo
helm repo update
```

```
-- install metallb
helm upgrade -i metallb metallb/metallb
```

#### Install the IPAddressPool aned Layer2 advertise config

#### IPAddressPool
- search for the IPaddresspool and select configuration to see the config which looks like below

- For the range of ip address, use `docker inspect kind`, get the CIDR of ip address in this case `172.20.0.0/16`

```sh
kubectl apply -f - << EOF 
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: first-ip-pool
  namespace: default  #since the metallb deployed to default namespace
spec:
  addresses:
  - 172.20.0.100-172.20.0.120
EOF
```

#### L2Advertisement config
- Now we need to configure L2Advertisement config with the IPAddressPool config

```sh
kubectl apply -f - << EOF
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: metalb-l2ad
  namespace: default
spec:
  ipAddressPools:
  - first-ip-pool
EOF
```
