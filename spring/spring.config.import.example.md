### Access Kubernetes secrets in Spring Boot application without loading to environment variables of the pod.

This blog will show how to access Kubernetes secrets in a Spring application without setting properties as environment variable in the container.

To better understand this blog, basic knowledge of Spring framework and Kubernetes (especially how to create secrets and mount secrets to access as environment variables in Pods) is recommended.

### Pre-requisites:
 - Docker desktop 
 - Kind CLI

### Spring configuration management
- Spring Boot framework already provides multiple way to externalize the configuration. Instead of specifying the properties in application.yaml we can created as environment variable. For example, say if we need to enable a specific Spring profile the environment variable SPRING_PROFILES_ACTIVE can be set with specific profile when running the application. 

### Access Kubernetes secret in Spring Boot application without configuring to environment variable
- In this approach the secret will be read by the Spring Boot application without loading to the environment variables.

   - The secret is mount in the pod manifest
   - The mount path of the secret is configured in Spring boot application with `spring.config.import`.

#### Demonstration of configuration management 
- Create a simple secret resource named secret-msg in Kubernetes cluster. Use below command

```
kubectl create secret generic secret-msg --from-literal=MESSAGE=from-k8s-secret-store
```
The created secret is mounted in Pod manifest. Below is the snippet of how secret is mounted.

```yaml
  volumeMounts:
       - name: secret-volume
          mountPath: /etc/secrets/
          readOnly: true
```

- The Spring Boot application.yaml file is configured with the mounted secret path.

```yaml
spring.config.import: "optional:configtree:/etc/secrets/"
```

- In Spring Boot controller, we can access the secret using the Value annotation.

```java
@Value("${message:default-from-app}")
private String message;
```

####  The Spring Boot application code and Kubernetes manifests

- Use [Spring Boot starter](https://start.spring.io) to create the Spring Boot application, include `spring-boot-starter-web` dependency. Extract the zip file and configure in preferred IDE.  Following are additional files to set the Spring Boot application.

- `application.yaml` content

```yaml
spring:
   application.name: configtree
   config.import: "optional:configtree:/etc/secrets/"
server.port: 8095
```

- The controller class includes `@value` annotation to read value from the message property, it is set with default value. This helps identify which values is being fetched when the application is running.

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ConfigController {
    private String RESPONSE= "{\"secret-msg\": \"%s\"}";

    @Value("${message:default-from-app}")
    private String message;

    @GetMapping("message")
    public String getMessage(){
        return String.format(RESPONSE,message);
    }
}
```

- To create docker image use the below content in the Dockerfile. Before using the Dockerfile to create the image the jar must be created. Note, this is not the ideal approach for production.

- The configtree is the project name used for demonstration. 

```Dockerfile
FROM openjdk:22-jdk-slim
VOLUME /tmp

COPY target/configtree-1.0.0.jar app.jar
EXPOSE 8091

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

- To build the docker image use below command, place the Dockerfile in the root of the Spring boot project application.

```sh
docker build -t configtree-app .
```

- The kind configuration used for this demonstration.

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: configtree
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 30095
        hostPort: 8095
```

- Command to create the kind cluster. Make sure the Docker desktop is running and issue below command

```
kind create cluster --config deploy/kind-config.yaml
```

- The image is not published to Docker hub, in order to use the image in Kind cluster it needs to be loaded using below command. The cluster name in this case is configtree and the image name is configtree-app

```
kind load docker-image configtree-app --name configtree
```

- Kubernetes deployment manifest for the spring boot application looks like below. 

   - Note, the mounted secret path is configured in the application yaml.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: configtree-app
  labels:
    app: configtree-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: configtree-app
  template:
    metadata:
      labels:
        app: configtree-app
    spec:
      containers:
        - name: configree
          image: configtree-app:latest
          imagePullPolicy: IfNotPresent 
          volumeMounts:
            - name: secret-volume
              mountPath: /etc/secrets/          # secret mount path
              readOnly: true
          ports:
            - containerPort: 8095
      volumes:
        - name: secret-volume
          secret:
            secretName: secret-msg      # name of the created secret
```

- NodePort service, this helps us to access the application running in the pod without port-forwarding.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: configtree-svc
spec:
  type: NodePort
  selector:
    app: configtree-app
  ports:
    - name: configtree-port
      protocol: TCP
      port: 8095
      targetPort: 8095
      nodePort: 30095
```

- Once deployed the application can be accessed which will render the message value from secret.

```
curl localhost:8095/v1/message
{"secret-msg": "from-k8s-secret-store"}
```

![image](https://github.com/thirumurthis/Learnings/assets/6425536/5f0f44bd-c9de-4dfd-bbef-09d4b116167c)

-------------------
#### Other approach to load secret to Pod environment variables

- There are other options in Kubernetes to configure the secret as environment variable to the pod. Refer [Kubernetes documentation](https://kubernetes.io/docs/tasks/inject-data-application/distribute-credentials-secure/#define-container-environment-variables-using-secret-data).

- Pod manifest to load all secret to Pod environment variable using `secretRef`.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: secret-ref
spec:
  containers:
  - name: envars-container
    image: nginx
    envFrom:
    - secretRef:
        name: secret-msg
```

- Alternatively we can use `secretKeyRef` to set as environment variable once specific key in the secret.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: secrets-key-ref
spec:
  containers:
  - name: envars-container
    image: nginx
    env:
    - name: MESSAGE
      valueFrom:
        secretKeyRef:
          name: secret-msg
          key: MESSAGE
```
