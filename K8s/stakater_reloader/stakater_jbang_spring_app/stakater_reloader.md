## Stakater reloader usage

In this article have demonstrated the use of Stakater reloader to reload the pod whenever the ConfigMap or Secret is updated. 

To demonstrate the usecase have used a simple Spring Boot application with JBang, the application displays the data from the application.yaml property via GET endpoint. JBang is used since the we can just create the applciation in a single file.

The container image is created for the application with Jbang base container using Dockerfile. The resource are deployed to the a KinD cluster.

Have used KinD cluster installed with Stakater reloader to deploy the application with Deployment and ConfigMap manifest annotated with Stakater reloader specific annotation based on which the pods reloads whenever the ConfigMap or Secret is updated. 

For more details refer the [Stakater reloader git repo](https://github.com/stakater/Reloader).

```yaml
  annotations:
    reloader.stakater.com/auto: "true"
```

### Complete source code

The complete source code with the structure could be found in my [git-repo](https://github.com/thirumurthis/projects/tree/main/stakater-jbang-spring-app)


### Spring Boot code

The JBang Spring boot application code is shown below. 
The files require to be placed under project structure. Say, we have the root project folder `stakater-jbang-spring-app` the below code is placed under the `app/` folder with file named `App.java`. The Dockerfile uses this path in the CMD soe when we run the container it can start the application.

The spring config location is used to specify the path of the application.yaml since it is placed under `config` folder. We could see the `RUNTIME_OPTIONS` in the below code which applies the options as java runtime argument, in this case `spring.config.location=file:./config/application.yaml`.

When we deploy the application to the Kubernetes cluster we use the Deployment manfiest and the `application.yaml` data is defined in the ConfigMap.

 Note, the `application.yaml` is placed under `config` directory different from the `app` directory itself, this is because when the ConfigMap is mounted as volume in the POD manifest with existing path like `/src/config` the exiting content in the container image will be override by the data in ConfigMap manfiest. When exec to the container image we could see the property `message: "default-from-app"` but when we deploy this would be `message: "message-from-k8s-configmap"`

 We could check this once the docker image is created, exec to the container using below `docker run -it --entrypoint sh jbang-spring-app` and check `cat config/application.yaml`.

 This will cause issue when application starts in Kubernetes.

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

package app;
//JAVA 25+

//RUNTIME_OPTIONS -Dspring.config.location=file:./config/application.yaml

//DEPS org.springframework.boot:spring-boot-dependencies:4.0.1@pom
//DEPS org.springframework.boot:spring-boot-starter-web

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@ComponentScan(basePackages = {".","app"})
@RestController
@RequestMapping("/api")

public class App{
    void main(String... args) {
        SpringApplication.run(App.class, args);
    }

    String formatStr = "{\"message\": \"%s\"}";

    @Value("${app.message}")
    private String msg;

    @GetMapping("/message")
    public String getMessage(){
        return String.format(formatStr, msg);
    }
}
```

To execute the application locally use below command in case of Windows

```sh
 jbang "<path>\<project-root>\app\App.java"
```

#### application.yaml

The application.yaml file is placed under the `config` folder of the root project directory (same level as the app directory). 

```yaml
# file to be created under <project-root>/config/application.yaml
# this path is used in RUNTIME_OPTIONS
app:
  message: "default-from-app"
```

#### Dockerfile

The JBang base image `jbangdev/jbang-action` is used to build the container image. Only necessary files are copied to the container and others are ignored by definting it in `.dockerignore` file.

```
FROM jbangdev/jbang-action

WORKDIR /src

COPY . .

EXPOSE  8080

CMD ["./app/App.java"]
```

`.dockerignore` file content is shown below which ignore some files or folder from getting copied to container image.   

```txt
Dockerfile
README.md
k8s/*
```

To create the docker image use below command, navigate to the root project folder.

```sh
 docker build -t jbang-spring-app .
```

To run the docker image in the docker use below command

```sh
 docker run -d -p 8080:8085 jbang-spring-app
```

#### Kuberentes resources

Below is the list of the Kuberentes manifest for Deployment, ConfigMap and Service to deploy into the KinD cluster with the generated image.

The Deployment and ConfigMap includes the Stakater annoation when we patch the config map the pods will reload itself.

```yaml
# file-name: <root directory>/k8s/app_resource_manfiest.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jbang-spring-app
  annotations:
    reloader.stakater.com/auto: "true"
spec:
  replicas: 3
  selector:
    matchLabels:
      app: jbang-spring-app
  template:
    metadata:
      labels:
        app: jbang-spring-app
    spec:
      volumes:
      - name: jbang-app-volume
        configMap:
          name: jbang-app-config
      containers:
        - name: jbang-spring-app
          image: jbang-spring-app
          imagePullPolicy: IfNotPresent
          volumeMounts:
          - name: jbang-app-volume
            mountPath: /src/config
            readOnly: true
          ports:
           - containerPort: 8080
---
# service
apiVersion: v1
kind: Service
metadata:
  name: jbang-app-service
spec:
  selector:
    app: jbang-spring-app
  ports:
    - protocol: TCP
      port: 8086
      targetPort: 8080
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: jbang-app-config
  annotations:
    reloader.stakater.com/auto: "true"
    #reloader.stakater.com/rollout-strategy: "restart" # rollout is the default stratergy
data:
  application.yaml: |
    app:
      message: "message-from-k8s-configmap"
---
```

### Deploying the resource to KinD cluster

With Docker running in the machine, we can use the kind CLI and we can create a cluster using below command

```sh
 kind create cluster --name test
```

To install the Stakater reloader using helm chart we use below command, the reloader deploys to the default namespace

```sh
helm repo add stakater https://stakater.github.io/stakater-charts
helm repo update
helm install reloader stakater/reloader
```

To deploy the application manifest we use below command, navigate to the root project directory.

```sh
# create a new namespace
kubectl create ns reloader

# create the application resource
kubectl -n reloader apply -f k8s/app_resource_manfiest.yaml
```

Once the application is deployed we could verify the application is deployed and working by checking the logs.

```sh
kubectl -n reloader logs deploy/jbang-spring-app
```

To access the deployed application we can port-forward the service deployed and access from a browser

Port forward the service

```sh
 kubectl -n reloader port-forward svc/jbang-app-service 8086:8086
```

To access the application use below url 

```txt
http://localhost:8086/api/message
```

The response would looke like 

```
{"message": "message-from-k8s-configmap"}
```

#### Patch the ConfigMap

When we patch the configmap we could see the pods are getting restarting automatically

```sh
 kubectl -n reloader patch configmap jbang-app-config --type merge \
  -p '{
    "data": {
      "application.yaml": "app:\n  message: \"new-message\""
    }
  }'
```
