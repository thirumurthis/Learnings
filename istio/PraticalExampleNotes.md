Istio

- Istio operator based installation is deprecated.

- Currently, helm based installation is mostly followed.

- Below we use the default helm values on the charts

- add the repo for official istio. In production env we might use this differently, like fetching this info from private repo.
```
helm repo add istio https://istio-release.storage.googleapis.com/charts
```

- Lets serach for base chart, this is the first one to be installed before installing istiod.

```
helm search repo istio/base

## version used 1.17.1 
```

- Lets get the default values 
```
helm show values istio/base --version 1.17.1 > default-values/istio-base-values.yaml
```

- We will use terraform to install the istio

Step 1
  - first we need to authenticate with the cluster (in below we use EKS)

```
# we can use below to dynamically fetch the token from the EKS cluster
provider "helm" {
   kubernetes{
       host         = aws_eks_cluster.demo.endpoint
	   cluster_ca_certificate = base64decode(aws_eks_cluster.demo.certificate_authority[0].data)
	   exec{
	      api_version = "client.authentication.k8s.io/v1beta"
		  args        = ["eks","get-token", "--cluster-name", aws_eks_cluster.demo.id]
	   }
   }
}
```
- Alternatively the easiest way would be to use Kube config file
- just point the provider to the local machine

- helm-provider.tf
```
provider "helm"{
   kubernetes {
      config_path = "~/.kube/config"
   }
}
```

Step 2
 - we create a new base file for terraform with the istio base values file.

```
# if not using terraform use below commands to terminal to install
 helm repo add istio https://istio-release.storage.googleapis.com/charts
 helm repo update
 helm install demo-istio-base-release -n istio-system --create-namespace istio/base --set global.istioNamespace=istio-system
```

- To use terraform we need to create a tf file with below content
 - easily reproducible.
 - The set is used to override the values like in helm above

- istio-base.tf 
```
resource "helm_release" "istio_base"{
   name = "demo-istio-base-release"
   repository        = "https://istio-release.storage.googleapis.com/charts"
   chart             = "base"
   namespace         = "istio-system"
   chreate_namespace = true
   version           = "1.17.1"
   set {
      name = "global.istioNamespace"
      value = "istio-system"
   }
}
```

- In the latest version, the istio pilot citadel galley into single `istiod`
- we get the base values of it as well

- We get the default values for those from the repo as well

```
helm show values istio/istiod --version 1.17.1 > defaults/istiod-default-values.yaml
```

- In order for the ingress gateway to work we need to create some variables.

- To deploy the istiod using helm use below command

```
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update 
helm install demo-instio-release -n istio-system --create-namespace istio/istiod --set telemetry.enabled=true --set global.istioNamespace=true --set meshConfig.ingressService=istio-gateway 
```

Note, 
  - the ingressService and ingressSelector should be override inorder to make the cert manager to work as expected.
  
- Alternatively we can deploy it using terraform like below.
- copy the content to a file with tf extentions

- istiod-base.tf
```
resource "helm_release" "istiod" {
   name= "demo-istiod-release"
   
   repository       = "https://instio-release.googleapis.com/charts"
   chart            = "istiod"
   namespace        = "istio-system"
   create_namespace = true
   version          = 1.17.1 
   
   set {
      name = "telementry.enabled"
      value = "true"
   }
   
   set {
      name = "global.istioNamespace"
	  value = "istio-system"
   }
   
   set {
     name  = "meshConfig.ingressService"
	 value = "istio-gateway"
   }
   
   set {
     name  = "meshConfig.ingressSelector"
	 value = "gateway"
   }
   
   depends_on = [helm_releae.istio_base]
}
```

To execute the tf files, install the terraform first.
- navigate to the folder istio-example (where the tf and the default values files are stored).

- file structure tree looks like below
```
 istio-example
  |_ helm-defaults
     |_ istio-base-default.yaml
	 |_ istiod-base-default.yaml
  |_ istio-terraform
     |_ helm-profider.tf
	 |_ istio-base.tf
	 |_ istiod-base.tf
```
- from the terminal issue below command 

```
terraform init
```

- below will apply both helm charts.
```
terraform apply
```

- now in the cluster check the istio-system namespace and the crds got created.

== At this point the istio is configured in the cluster.

-- Example below is to manage traffic

- Create a namespace

```
apiVersion: v1
kind: Namespace
metadata:
  name: dev
  labels:
    monitoring: prometheus
	istio-injection: enabled  # <---- this label will create side car automatically
```

- Two deployments for the application which has lables like below, and two yaml files

version: v1 
version: v2 

These lables will show in kaili dashboard.

- A service manifest

- Istio Custom resource, destination rule which defines traffic rile
- The desitnation rule defines what backend application is used, which is called subsets. which is called v1 and v2.

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
   name: first-app
   namespace: dev
spec:
  host: first-app
  subsets:
    - name: v1
	  labels:
	    app: first-app
		version: v1
    - name: v2
	  labels:
	     app: first-app
		 version: v2
```

- next, Istio CRD virtual service is to be created 
 - the hosts has the same as in destinationrule, which is the target host
 - The route configuration has the subset v1 which routes traffic to this v1 apps
 - The v2 apps will be blocked at this time.
 - when we need to rollout the v2 we can route to v2. 
```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
    name: first-app
	namespace: dev
spec:
    hosts:
	  - first-app
	http:
	 - route:
	    - destination:
		    host: first-app
			subset: v1
```

- once the above configuration is deployed the cluster, if we see the traffic will be routed to the deployed label version: v1.

## say, we wanted to route traffic to the deployment with label version: v2 which is the canery deployement.

- all we need to do is update the virtualservice and deploy it

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
    name: first-app
	namespace: dev
spec:
    hosts:
	  - first-app
	http:
	 - route:
	    - destination:
		    host: first-app
			subset: v1
		  weight: 90
	    - destination:
		    host: first-app
			subset: v2
		  weight: 10
```

- now if the above virtualservice is deployed, the traffic will be routed 10% to the v2 pods.

- we can update the apps are validated we can update the weightage to 50 
- This can be automated in the pipeline.

### How to inject side car proxy to resource

- in namespace using the labels
```
metadata:
  labels:
    istio-injection: enabled 
```

```
metadata:
  labels:
    istio-injection: disabled
```

- using port inject enable label.

```
# deployment manifest
template:
  metadata:
    labels:
	  app: first-app
	  istio: monitor
	  sidecar.istio.io/inject: "true"
```

- injecting manually, this is only used for development using kubectl and istoctl command.

### Istio gateway configuration to expose the cluster to internet.

- we use the helm chart to install it like the previous example. 
```
helm search rep istio/gateway
```
- get the default values yaml

```
helm show values isito/gateway --version 1.17.1  > helm-defaults/gateway-defaults.yaml
```

- in order to update the service and loadbalancer we can override it with terraform.

- istio-gateway.tf
```
# no values are being overridden right now.
resource "helm_release" "gateway" {
   name = "gateway"
   
   repository       = "https://instio-release.googleapis.com/charts"
   chart            = "gateway"
   namespace        = "istio-ingress"
   create_namespace = true
   version          = 1.17.1 
   depends_on = [
      helm_release.istio_base,
	  helm_release.istiod
   ]
}
```

- once updated, issue `terraform apply` command to install the helm charts and check the cluster

- ` kubectl get svc -n istio-ingress` once deployed the ip address should be listed in the loadbalancer service.

With the Gateway deployed,
- Say, we are deploying pods with label second-app
- The namespace, 2 deployment manfest (v1 and v2), the service (with different name) can be used.
- same destination rule manifest file is used like above.
- The virtual service changes, to include external DNS name

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
    name: second-app
	namespace: dev
spec:
    hosts:
	  - my.app.exampledomain.com
	  - second-app
	gateways:
	  - api  # <------ name of the gateway which will be created next
	http:
	 - match:
	    - uri:
		    prefix: /
	   route:
	    - destination:
		    host: first-app
			subset: v1
		  weight: 90
	    - destination:
		    host: first-app
			subset: v2
		  weight: 10
```

- Gateway manifest. To expose the app to internet we need to create the Gateway object.

```
apiVersion: networking.istio.io/v1beta1
kind Gateway
metadata:
  name: api
  namespace: dev
spec:
  selector:
     istio: gateway  <-- pod labels is being used. 
  servers:
    - port:
	    number: 80
		name: http
		protocol: HTTP
	  hosts:
	    - my.app.exampledomain.com  # <-- hostname or DNS name
```

- The `istio: gateway`, we can check the pods labled in the istio-ingress namespace using below command. 

```
kubectl get pods -n isito-ingress --show-labels
```


## To deploy TLS 
- deploy cert manager download the apache cert-manager and deploy to cluster
- Once deployed we need to create a issuer.

```
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: dev-cluster-issuer
spec:
  acme:
     # if using LetsEncrypt open source, it has a rate limiting
     server: https://acme-dev-v02.api.letsencrypt.org/directory
	 privateKeySecretRef:
	   name: dev-cluster-issuer
	 solvers:
	  - selector: {}
	    http01:
		  ingress:
		    class: istio  <--- what class is used to solve claim challenge.
```

- once the issuer is created, now we can create certificate:

```
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: my-api-exampledomain-com
  namespace: istio-ingress   <-----------certificate is deployed in this ns
spec:
  secretName: my-api-exampledomain-com-crt
  dnsName:
    - my.api.exampledomain.com
  issuerRef:
    name: dev-cluster-issuer
	kind: ClusterIssuer
	group: cert-manager.io
```
Note: The above will take a while, to fetch the certificate and resolve the challanges. Once the certificate is created validate using kuber comand

-- Once the certificate is created we can update the gate way

```
apiVersion: networking.istio.io/v1beta1
kind Gateway
metadata:
  name: api
  namespace: dev
spec:
  selector:
     istio: gateway  <-- pod labels is being used. 
  servers:
    - port:
	    number: 80
		name: http
		protocol: HTTP
	  hosts:
	    - my.app.exampledomain.com  # <-- hostname or DNS name
	- port:
	    number: 443
		name: https
		protocol: HTTPS
	  hosts:
	    - my.app.exampledomain.com
	  tls:
	    credentialName: my-api-exampledomain-com-crt
		mode: SIMPLE
```
