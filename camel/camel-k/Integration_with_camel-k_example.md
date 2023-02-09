## Camel-K

- Apache Camel K is a lightweight cloud-integration platform that runs natively on Kubernetes.
- In this blog will show how to install Camel-K operator in Kubernetes (KIND) cluster.

- Have created a simple integration code (Java integration).
   - The integration code expose a REST endpoint using Camel component.
   - The message is passed as URL path variable.
   - The message is routed to `direct` endpoint and transform with a constant string.

### Pre-requisites
 - Docker Desktop (Docker daemon running).
 - Dockerhub account.
 - Kamel CLI installed.
 - KIND CLI installed.
 - Basic understanding on Kubernetes and Operator pattern.

#### Options to instal Camel-K operator

- There are different option to install Camel-K. Options are listed below, 
  - using [Helm charts](https://artifacthub.io/packages/helm/camel-k/camel-k)
  - using [OperatorHub](https://operatorhub.io/operator/camel-k)
  - using [Kamel CLI](https://camel.apache.org/camel-k/1.11.x/installation.html)
- In this blog the Camel-K operator is installed with **Kamel CLI** in KIND cluster.

> **Note:-**
> During Camel-K installation we need to configure the docker registry (docker.io).
> In this blog have used Dockerhub, we can use private registry as well.

### Install KIND cluster and deploy Nginx Ingress controller

- The ingress controller is used to access the REST endpoint from the host machine.

- Below is the KIND cluster configuration file which includes patch configuration for ingress controller, save this as `kamel-kind-cluster.yaml`.

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

- We need to use the above configuration in Kind CLI to create the cluster, the command usage is listed below.

```
kind create cluster --config kamel-kind-cluster.yaml --name kamel-ingress
```

- In order to apply the ingress controller configuration below command. Refer [kind documentation](https://kind.sigs.k8s.io/docs/user/ingress/#ingress-nginx_) for more info.

```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

### Install Camel-K operator in KIND cluster

- To install the Camel-K operator with Kamel CLI, we need to configure the docker registry (docker.io). For this first we need to create a secret in the cluster with the Docker credentials.

> **Note:-**
> - Creating a secret with the $HOME/.docker/config.json file directly didn't work for me. When running the integration, the logs indicated unauthorized access.
> - Below is the command to create secret from docker config file.
> ```
> kubectl create secret generic regcred \
>    --from-file=.dockerconfigjson=$HOME/.docker/config.json \
>    --type=kubernetes.io/dockerconfigjson
>```


### Install OLM in the cluster

 - This is optional step. By installing the OLM (Operator Lifecycle Management)  we are not required to use the option `--olm=false` in Kamel CLI during installation.
 
- To install the OLM, run below command from Gitbash.

```
curl -sL https://github.com/operator-framework/operator-lifecycle-manager/releases/download/v0.23.1/install.sh | bash -s v0.23.1
```

- To create the secret with the Docker credentials use below commands
   - Storing the Dockerhub credentials in the environment variable will help in case we need to automate the installation process.

```
# from powershell use below command, to accessing environment variable we use $env:<env-key>
kubectl -n default create secret docker-registry external-registry-secret --docker-username $env:DOCKER_USER --docker-password $env:DOCKER_PASS

# from shell (like git bash) use below command
kubectl -n default create secret docker-registry external-registry-secret --docker-username $DOCKER_USER --docker-password $DOCKER_PASS
```

- Below command will install the Camel-K operator in default namespace.

```
# the secret created above is passed in --registry-secret in this command
kamel install -n default --registry docker.io --organization <organization-name> --registry-secret external-registry-secret --wait
```

- If OLM is not installed in the cluster (as mentioned above), in this case then we need to use `--olm=false` in the command shown below.

```
kamel install -n default --olm=false --registry docker.io --organization <organization-name> --registry-secret external-registry-secret --wait
```

> **Info:**
>
> Camel-K operator installation takes sometime to install.
> Camel-K operator deploys as a pod, so to check the status of the pod in the default namespace use `kubectl get pods`.
>
> ![image](https://user-images.githubusercontent.com/6425536/217720112-da2a90e5-600a-46e4-afff-423a57bf409b.png)
>


### Running a simple integration 

- We can use Kamel CLI to run integration code developed in Java, groovy, etc. 
- The Camel-K operator gets deployed to cluster as a pod, we can simply check the status of the pod and make sure it is in running state before creating an integration.
 
- Below is a simple Java integration code, which uses Camel RouteBuilder and REST component to expose REST endpoint and routes the message.

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

- During development we can run the integration using `--dev` option, the command to create and run the integration looks like below.

```
kamel run WelcomeRoute.java --dev
```

- Using `--dev` will print the logs in console.
- With this flag any changes to the Java file will reflect immediately in the integration real time.

- Below is the console log output of successfully deployed integration.

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

- Kamel CLI can be used to check the status of integration, use below command.

```
kamel get
```
  - If the integration is running on a specific namespace we can use `kame get -n <namespace-name>`.

![image](https://user-images.githubusercontent.com/6425536/217706482-39ecead7-d7cf-493c-a764-98215c4a148d.png)

- By default Camel-K creates a `ClusterIP` service for this integration.

![image](https://user-images.githubusercontent.com/6425536/217711853-fbe6d5ad-9a14-473d-9c89-1b79f1aaac53.png)

- The endpoints exposed can be checked using the command `kubectl get endpoint`

![image](https://user-images.githubusercontent.com/6425536/217706735-3abd5379-130e-4af1-859c-0e5fe313c097.png)

- By default the Camel-K operator creates service as `ClusterIP`. In case if we want to expose the service as `NodePort` or `LoadBalancer`, we can use `Service Traits` to configure. The command looks like below.
        - There are different traits supported by Camel-K, refer the [Camel-K traits documentation](https://camel.apache.org/camel-k/1.11.x/traits/traits.html).

```
kamel run --trait service.enabled=true --trait service.node-port=true --trait service.type=NodePort -n <namespace> <file.java> --dev 
```

### Apply ingress rule to access the endpoint

- To access the REST endpoint from the host machine, we use Nginx Ingress control already deployed in cluster.
- We need a ingress rule configuration to route traffic to access the service.

- The ingress rule configuration manifest content is listed below save this in a file named `ingress-rule.yaml`.

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

- To apply the ingress rule use `kubectl apply -f ingress-rule.yaml` command.
- To check the ingress status use `kubectl get ingress` command.

### Access the REST endpoint and output

- Once the integration is deployed successfully and in running state, we can use `wget` to get access the endpoint. Below is the complete command.

```
wget -q -O - localhost/api/demo/hello
```

![image](https://user-images.githubusercontent.com/6425536/217705778-00d05132-06bd-4943-8b55-163885fa3220.png)

- Below is the operator and integration pod running in cluster, have used Lens to connect to the kind cluster.

![image](https://user-images.githubusercontent.com/6425536/217706212-b66f9421-fc5c-41f5-9caa-e00ab55dc9f6.png)

### Passing properties to the integration code

For a simple groovy script, where the `my.message` is fetched from the properties. Save the below content to a file `Message.groovy`.

```java
from('timer:props?period=1000')
    .log('{{my.message}}')
```

- To pass the property values, since we are not using `--dev`, the integration will be created

```
kamel run --property my.message="Example-From-Property" Message.groovy
```

![image](https://user-images.githubusercontent.com/6425536/217715614-4865e91d-9cdc-4dea-9b04-1e5ec639e1f5.png)

- Output looks like below

```
[1] 2023-02-09 03:46:30,116 INFO  [route1] (Camel (camel-1) thread #1 - timer://props) Example-From-Property
[1] 2023-02-09 03:46:31,090 INFO  [route1] (Camel (camel-1) thread #1 - timer://props) Example-From-Property
```

![image](https://user-images.githubusercontent.com/6425536/217715505-5e351772-6175-47ed-b349-1a9ca96fbfae.png)

### Additional information

- We can use ConfigMap with the configuration or properties file and pass it in runtime using Kamel CLI to the integration code. Refer the [Camel-K runtime configuration documentation](https://camel.apache.org/camel-k/1.11.x/configuration/runtime-properties.html)
- Promoting the integration to different environment, we can do it as well refer the [Camel-K promoting documentation](https://camel.apache.org/camel-k/1.11.x/running/promoting.html)
