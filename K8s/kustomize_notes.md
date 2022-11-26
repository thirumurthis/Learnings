
## Manage Kubernetes manifest using Kustomize

**Pre-requisites:**

 - Basic understanding of Kubernetes and how resources are deployed using yaml
- Kustomize CLI installed in the machine. To install refer the [documentation](https://kubectl.docs.kubernetes.io/installation/kustomize/) for more details


### What is Kustomize?

  - Kustomize provides a solution for customizing Kubernetes resource configuration free from templates and DSLs. Refer [kustomize.io](https://kustomize.io/)
 - Kustomize lets you customize raw, template-free YAML files for multiple purposes, leaving the original YAML untouched and usable as is.
  - `kubectl` client already supports kustomize
  - Kustomize configuration can applied directly to Kubernetes cluster using `kubectl apply -k <directory\to\kustomization.yaml>`
  
### High level overview

  - In enterprise project to deploy the application into Kubernetes cluster the resources will be defined in many of YAML manifest file. If there are lots of manifest file and in case we need to apply different configuration for each environment like dev, test or production we can use `helm` in order to render and deploy the manifest we need `helm` command. Kustomize can helps in managing the environment specific configuration in a manageable way.

  - With Kustomize we not need to template the manifest, the environment specific configuration can be applied to cluster directly using the `kubectl` command. The manifest can also be rendered using `kustomize` cli. 

- With the kustomize structure the base or raw manifest will never change.

#### How kustomize works?

  - Say, we have a manifest for our application in a directory named `base`, kustomize uses `kustomization.yaml` file that configures the path these manifest under the same directory using `resources` tag

> **Info:-**
> The `resources` tag is used to specify the path of the application manifest yaml this tag is identified by kustomize framework
>

  - In order to configure the environment specific configuration `overlay` folder is created, which contains folders specific to environment. With reference to the image below, we use `dev` and `test` folders refers the environment.

> **Info:-**
> Per Kustomize documentation example have used the folder name as `overlay`, this folder name can be different.
> 

  - `overlay\dev` folder contains `kustomization.yaml` file which defines the path to the base folder (in `resources` tag), and dev environment specific changes. For dev the configuration adds "-dev" to the name using `nameSuffix` and also updates the deployment replicas which is defined inline in the `kustomization.yaml`

  - `overlay\test` folder contains `kustomization.yaml` file which defines the path to the base folder (in `resources` tag), and path of the patch file using `patches` tag. The `deployment_patch.yaml` use patch transform to update the replicas of the deployment. There are other options to update the configuration of resources, using inline patch configuration where the deployment image tag and the secret properties are updated.

  - In order to render the environment specific manifest we can use the kustomize CLI command `kustomzie build /environment/directory/of/kustomization.yaml` or use ` kubectl apply -k /directory/of/kustomization.yaml`.In both case the command takes in the environment specific folder where the `kustomization.yaml` exists.

- The framework will use the `kustomize.yaml` configuration to identify the base manifest.


#### Kustomize project folder structure 

##### Dev environment specific representation

![image](https://user-images.githubusercontent.com/6425536/204075864-fddca82d-d08e-4400-adf9-4ddaa252f81d.png)


##### Test environment specific representation

 - With reference to the dev, the base folder is same for the test as well.
![image](https://user-images.githubusercontent.com/6425536/204076298-992b39cb-ff10-4a5f-85dd-1d73831c4715.png)


### Code sample

- Below are the YAML file used for this demonstration

- `base/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-app
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
```

- `base/secret.yaml`

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: demo-app-secret
type: Opaque
data:
  APP_NAME: demo_app
  ENV_CODE: DEV   # For test we need to update this to TEST
```

- `base/kustomization.yaml`

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
   - deployment.yaml
   - secret.yaml
```

### Dev environment specific configuration

- `overlay/dev/kustomization.yaml`

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
 - ../../base/

nameSuffix: -dev

# example of updating deployment replica from kustomization
# Note:- Kustomize supports patch update where the same 
# changes can be created as a patch, will see this example in 
# overlay/test directory for demo
# when using the replica in this kustomization.yaml, any change 
# to deployment replica will not be applied, noticed this update
# takes precedence
replicas:
- name: demo-app
  count: 1
```

### Render dev environment manifest 

- Command to generate dev specific manifest

```
# navigate to specific directory where the manifest is stored
# in this case kustomization foder contains the base and overlay folder
# refer the image above where the project structure is deplicted (fig 1)

> kustomize build overlay/dev
```

> **INFO:-**
> The manifest will be displayed in the stdout, we can redirect it to specific directory using the `-o` switch, usage `kustomize build overlay/dev -o output/` 
>

### Output of rendered dev manifest 

```yaml
apiVersion: v1
data:
  APP_NAME: demo_app
  ENV_CODE: DEV
kind: Secret
metadata:
  name: demo-app-secret-dev
type: Opaque
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx
  name: demo-app-dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - image: nginx:latest
        name: nginx
        ports:
        - containerPort: 80
```

### Test environment specific configuration

- `overlay/test/deployment_patch.yaml`

```yaml
- op : replace
  path: /spec/replicas
  value: 3
```

> **INFO:-**
> There are couple ways to specify the patch files 
>  - The target can be specified directly in the `deployment_patch.yaml` file like
> ```yaml
>  target: 
>    group: apps
>    version: v1
>    kind: Deployment
>    name: demo-app
>```
> - This patch file can directly define the deployment manifest yaml, with the test specific configuration. The `kustomization.yaml` file under test, will refer to the pathc using `patches` tag.
> - kustomize framework will automatically infer the type of patch to be used when using `patches` tag, like `patchesStrategicMerge` or `patchTransform`

- `overlay/test/kustomization.yaml`

  - Below yaml file defines different options to apply the patch, using external file and inline in the configuration yaml

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
 - ../../base/

nameSuffix: -test

patches:
  - path: deployment_patch.yaml
    target: 
      group: apps
      version: v1
      kind: Deployment
      name: demo-app
  - patch: |-
     - op: replace
       path: /spec/template/spec/containers/0/image
       value: nginx:1.21.0
    target:
      kind: Deployment
      name: demo-app
  - patch: |-
      apiVersion: v1
      kind: Secret
      metadata:
        name: demo-app-secret
        type: Opaque
      data:
        APP_NAME: demo_app
        ENV_CODE: TEST ## UPDATE TO TEST IN THIS PATCH
```

### Render test environment manifest 

- To render test environment specific manifest, the command is 
`kustomzie build overlay/test`

### Output of rendered test environment manifest

```yaml
apiVersion: v1
data:
  APP_NAME: demo_app
  ENV_CODE: TEST
kind: Secret
metadata:
  name: demo-app-secret-test
  type: Opaque
type: Opaque
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx
  name: demo-app-test
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - image: nginx:1.21.0
        name: nginx
        ports:
        - containerPort: 80
```

#### Additional Info

Kustomize do supports different options to customize the configuration like 
  - replacements
  - ConfigMap generator
  - SecretGenerator 
  - built-in plugin
  - etc...

Refer the [kustomize documentation](https://kubectl.docs.kubernetes.io/references/kustomize/kustomization/) for details.
