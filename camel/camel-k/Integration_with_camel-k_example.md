## Camel-K

- Apache Camel K is a lightweight cloud-integration platform that runs natively on Kubernetes. In this blog we will see how to install the Camel-K operator in Kubernetes cluster, for demonstration I have used KIND cluster.

- We will create a simple integration, where we will expose a REST endpoint to send a message and route it transforming with some constant.

Camel-K can be installed in Kubernetes platform in different ways, 
  - using [Helm charts](https://artifacthub.io/packages/helm/camel-k/camel-k)
  - using [OperatorHub](https://operatorhub.io/operator/camel-k)
  - using [Kamel CLI](https://camel.apache.org/camel-k/1.11.x/installation.html)

Pre-requisities:
 - Docker Desktop (Docker daemon running).
 - Dockerhub account.
 - Kamel CLI installed.
 - KIND CLI installed.
 - Basic understanding on Kubernetes and Operator pattern.

In this blog the Camel-K operator is installed to Kind cluster using Kamel CLI. 

> **Note:-**
> During Camel-K installation we need to provide the docker registry (in this case have used dockerhub).

### Create KIND cluster and setup Ingress controller

- In order to access the REST endpoint we will also install ingress in the KIND cluster.

- Kind cluster configuration file, save this as `kamel-kind-cluster.yaml`
   - This configuration includes ingerss realted properties

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
```

Use Kind CLI to create cluster using above configuration. The command is as follows,

```
kind create cluster --config kamel-kind-cluster.yaml --name kamel-ingress
```

- To install ingress we can use below manifest file, refer [kind documentation](https://kind.sigs.k8s.io/docs/user/ingress/#ingress-nginx_) for more info.

```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

## Install Camel-K in cluster

- Using Kamel CLI to install Camel-K. During installation of Camel-K we need to register the docker.io registry. The Docker credentials will be created as a secret. 

> **Note:-**
>
> Below didn't work for me,
> - Creating a sceret with the $HOME/.docker/config.json file using below command
> ```
> kubectl create secret generic regcred \
>    --from-file=.dockerconfigjson=$HOME/.docker/config.json \
>    --type=kubernetes.io/dockerconfigjson
>```
>

- Installing OLM in the cluster
 - This step is optional, if we install the OLM, then we don't need to use the `--olm=false` in the `kamel install` command.
 
To install the OLM, use below command, run this command in git bash shell

```
curl -sL https://github.com/operator-framework/operator-lifecycle-manager/releases/download/v0.23.1/install.sh | bash -s v0.23.1
```

- We need to create a secret with the docker cridentials using below command. Store the docker credentials in environment variable.

```
# from powershell use below command, to accessing environment variable we use $env:<env-key>
kubectl -n default create secret docker-registry external-registry-secret --docker-username $env:DOCKER_USER --docker-password $env:DOCKER_PASS

# from shell (like git bash) use below command
kubectl -n default create secret docker-registry external-registry-secret --docker-username $DOCKER_USER --docker-password $DOCKER_PASS
```

- Install the Camel-k Operator using Kamel CLI, we will install the operator in default namespace, the command looks like below

```
# the secret created above is passed in --registry-secret in this command
kamel install -n default --registry docker.io --organization <organization-name> --registry-secret external-registry-secret --wait
```

- If the OLM is not installed the command will look like below 
```
kamel install -n default --olm=false --registry docker.io --organization <organization-name> --registry-secret external-registry-secret --wait
```

- It will take sometime to install the Camel-K operator. Check the pod status in the default namespace or the logs.


### Running a simple integration 

- Once the Camel-K operator is in running state, we can use Kamel CLI to run an integration

- Below is the java code, which will expose REST endpoint using Camel Routes.

```java

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;

public class WelcomeRoute extends RouteBuilder {

   @Override
   public void configure() throws Exception {
	  restConfiguration().bindingMode("auto");

		rest("/api")
			.get("/demo/{info}")
			.to("log:info")
			.to("direct:msg");
		from("direct:msg")
				.transform().simple("msg received - ${header.info}");
   }
}
```

To run the integration in a dev mode we can use below command

```
kamel run WelcomeRoute.java --dev
```

- Using `--dev` will print the logs in console, and helpful to debug the application. Also with this flag any changes to the file will update the integration realtime.

- If everything installed correctly the console logs will look like below

```
Condition "Ready" is "True" for Integration welcome-route: 1/1 ready replicas
[1] 2023-02-09 02:36:48,267 INFO  [org.apa.cam.k.Runtime] (main) Apache Camel K Runtime 1.16.0
[1] 2023-02-09 02:36:48,325 INFO  [org.apa.cam.qua.cor.CamelBootstrapRecorder] (main) Bootstrap runtime: org.apache.camel.quarkus.main.CamelMainRuntime
[1] 2023-02-09 02:36:48,334 INFO  [org.apa.cam.mai.MainSupport] (main) Apache Camel (Main) 3.19.0 is starting
[1] 2023-02-09 02:36:48,517 INFO  [org.apa.cam.k.lis.SourcesConfigurer] (main) Loading routes from: SourceDefinition{language='java', type='source', location='file:/etc/camel/sources/.\WelcomeRoute.java', }
[1] 2023-02-09 02:36:55,022 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.19.0 (camel-1) is starting
[1] 2023-02-09 02:36:55,161 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Routes startup (started:2)
[1] 2023-02-09 02:36:55,162 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started route1 (direct://msg)
[1] 2023-02-09 02:36:55,162 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started route2 (rest://get:/api:/demo/%7Binfo%7D)
[1] 2023-02-09 02:36:55,163 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.19.0 (camel-1) started in 732ms (build:0ms init:605ms start:127ms)
[1] 2023-02-09 02:36:55,874 INFO  [io.quarkus] (main) camel-k-integration 1.11.1 on JVM (powered by Quarkus 2.14.0.Final) started in 18.400s. Listening on: http://0.0.0.0:8080
[1] 2023-02-09 02:36:55,879 INFO  [io.quarkus] (main) Profile prod activated.
[1] 2023-02-09 02:36:55,889 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-bean, camel-core, camel-direct, camel-java-joor-dsl, camel-k-core, camel-k-runtime, camel-kubernetes, camel-log, camel-platform-http, camel-rest, cdi, kubernetes-client, security, smallrye-context-propagation, vertx]
```

- Camel Operator creates pod for the integration, the status can be checked using `kamel get` command, for namespace use `-n <namespace-name>`.
![image](https://user-images.githubusercontent.com/6425536/217706482-39ecead7-d7cf-493c-a764-98215c4a148d.png)

- By default Camel-K creates a `ClusterIP` service, we can check the exposed endpoints using `kubectl get endpoint`
![image](https://user-images.githubusercontent.com/6425536/217706735-3abd5379-130e-4af1-859c-0e5fe313c097.png)

- In case if we want the service to be exposed as NodePort or LoadBalanceer, we can use `Service Traits`, below is how the command looks like
- There are different traits supported by Camel-K check the [documentation](https://camel.apache.org/camel-k/1.11.x/traits/traits.html)
```
kamel run --trait service.enabled=true --trait service.node-port=true --trait service.type=NodePort -n <namespace> <file.java> --dev 
```

#### Installing ingress rule to access the endpoint from host machine

- Below is the ingress rule configuration details, save it in a file named `ingress-rule.yaml`

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kamel-welcome-route
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
  - http:
      paths:
      - pathType: Prefix
        path: /(/|$)(.*)
        backend:
          service:
            name: welcome-route
            port:
              number: 80
```

- Command to apply the ingress configuration to kind cluster is below

```
kubectl apply -f ingress-rule.yaml
```

- Check if the ingress is created using `kubectl get ingress`

#### Access the REST endpoint 

```
wget -q -O - localhost/api/demo/hello
```

##### Output
![image](https://user-images.githubusercontent.com/6425536/217705778-00d05132-06bd-4943-8b55-163885fa3220.png)

- Should see the integration running as a pod, using Lens to connect to the kind cluster
![image](https://user-images.githubusercontent.com/6425536/217706212-b66f9421-fc5c-41f5-9caa-e00ab55dc9f6.png)


