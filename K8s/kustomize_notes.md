
## Managing Kubernetes manifest using Kustomize

### What is Kustomize?
  - Kustomize is template-free way to customize application configuration. Refer [kustomize.io](https://kustomize.io/)
  - Kustomize is built into `kubectl` and when the Kustomize configuration can be directly applied to the Kubernetes cluster directly using `kubectl apply -k <directory\to\kustomization.yaml>`
  
#### High level overview
  - In enterprise project to deploy application the resources will be defined in lots of Yaml manifest file. In case if we need to apply environment (dev,test,production) specify changes to those manifest we can use `helm`. When using `helm` the manifest are deinfed using templates, to generate the manifest we use helm command.  
  - With Kustomize we not need to template the manifest, the environment specific configuration can be applied using erither the `kubectl` command or we can use the `ksutomize` cli to render the manifest. In this case we have a base manifest whcih nver changes, any envrionment specific changes will be manages in specific directories.

#### How kustomize works?
  - Say, we have a manifest for our application in a directory named `base`, we require an  `kustomization.yaml`file which configures the path thes manifest under the same directory 
> **Info:-**
> The `resource` tag is used to specify the path of the application manifest yaml this tag is identified by Kustomize CLI
>
  - In order to configure the envronment specific configuration we need to create a `overlay` folder, which contains environemnt name, with reference to the image above have created `dev` and `test` environment folders.
  
> **Info:-**
> Per Kustomize documentation i have used `overlay` folder which is best practice this folder can have different name as well.
> 

  - `overlay\dev` folder contains `kustomization.yaml` file which defines the path to the base folder, and dev environment specific changes. In this case adding a namePrefix and updating replica
  - `overlay\test` folder contains `kustomization.yaml` file which defines the path to the base folder, and refence to patch file that has definition to replace the replica and an inline patch to update image tags.
  - When running the kustomize CLI command to render the environment specific manifest, the Kustomize will use the `kustomization.yaml` under specific environment to identify the base manifest configuration (kustomization.yaml under the base defines which resources to use, using the `resources` tag)


#### Kustomization manifest folder structure and dev environment depiction
![image](https://user-images.githubusercontent.com/6425536/204075864-fddca82d-d08e-4400-adf9-4ddaa252f81d.png)


#### Test environment specific configuration
 - With reference to the dev, the base folder is same for the test as well.
![image](https://user-images.githubusercontent.com/6425536/204076298-992b39cb-ff10-4a5f-85dd-1d73831c4715.png)


#### `base/deployment.yaml`
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

#### `base/secret.yaml`
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

#### `base/kustomization.yaml`
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
   - deployment.yaml
   - secret.yaml
```

- Below are the `dev` environment specific configuration

#### `overlay/dev/kustomization.yaml`
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

### To render the dev environment manifest, in this case using the kustomize CLI

- Below command will generate the dev spcecific manifest

```
# navigate to specific directory where the manifest is stored
# in this case kustomization foder contains the base and overlay folder
# refer the image above where the project structure is deplicted (fig 1)

> kustomize build overlay/dev
```
> **INFO:-**
> The manifest will be displayed in the stdout, we can redirect it to specific directory using the `-o` switch, usage `kustomize build overlay/dev -o output/` 
>

#### Rendered dev manifest 
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

### `overlay/test/deployment_patch.yaml`
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

#### `overlay/test/kustomization.yaml`
  - Below yaml file defines different configuration that can be used to apply the patch
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

### To render the test specific manifest, like we did for dev environment, we need to use Kustomize CLI `kustomzie build overlay/test`

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

Kustomize do supports different options to customize the configuration like 
  - replacements
  - ConfigMap generator
  - SecretGenerator 
  - built-in plugin
  - etc...

Refer the [kustomize documentation](https://kubectl.docs.kubernetes.io/references/kustomize/kustomization/) for details.
