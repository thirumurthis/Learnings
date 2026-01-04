## Stakater reloader usage

In this article have demonstrated the Stakater reloader usage. The Stakater reloader is used to reload the pods when there is any update in ConfigMap or Secret mounted to the pod in Kubernetes, this is similar to an use case in our project.

The article uses a simple Spring Boot application with JBang to demonstrate the reload scenario.
The Spring application exposes API endpoint which reads the property from the application.yaml. 
JBang is used since the applciation can be coded in single file. 
The Dockerfile is used to create the image and deployed to KinD cluster. The application is deployed as deployment, the application.yaml is defined in ConfigMap and mounted as volume in the pod.

For Stakater reloader to work the Deployment and the ConfigMap is updated with below annoation on the manfiest. There are few additional option available, refer the [documentation](https://github.com/stakater/Reloader) for more details

```yaml
  annotations:
    reloader.stakater.com/auto: "true"
```

### Complete source code

TBD add link 


### Spring Boot Application code

The complete code for JBang Spring boot application is shown below. Say, we create a root folder called `jbang-spring-app`, then place the below code content under `app/App.java`. The path is used in the Dockerfile to create the container image.
The code includes `RUNTIME_OPTIONS` which defined the java runtime argument with the `spring.config.location` so JBang spring can register the application.yaml during startup.
It is better to use a different directory to hold the application.yaml, since when we mount the application.yaml using ConfigMap in Kuberentes deployment manifest it would create this folder in the container.
Note, if we use the `app` directory and mount that path in manifest, the file in the app directory will be overrided during deployment. This will cause issue when application starts in Kubernetes.

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

#### application.yaml

The application.yaml file is placed under the `config` folder of the root project directory. This folder will be at the same level as the `app` directory.

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


`.dockerignore` file content

```
Dockerfile
README.md
k8s/*
```

#### Kuberentes resources

The necessary Kuberentes manifest to deploy the application using the generated image.
The deployment and the configmap includes the annoation which will be used by Stakater reloader, so when we patch the config map we could see the pods are getting reloded for any changes.

```yaml
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
    #reloader.stakater.com/rollout-strategy: "restart"
data:
  application.yaml: |
    app:
      message: "message-from-k8s-configmap"
---
```
