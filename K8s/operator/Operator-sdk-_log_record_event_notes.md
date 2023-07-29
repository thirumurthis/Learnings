## Extending Kubernetes API with Operator-SDK

In this blog, will explain how to use operator-sdk to create additional resource in Kuberentes cluster. 
To start with you will how to scaffold the project and initalize it, which provides a starting point for developing the CRD's and Controllers. Note, not all points are explained in details, only focus on necessary aspect like the steps required for developing operators.

I used Windows machine for development and the operator-sdk CLI was installed in WSL2. The scaffold operator-sdk project can be opened in Visual Studio code for development, explained later.

### Pre-requsites:

  - Basic understanding of Kubernetes and Opeators
  - Docker Desktop
  - KIND CLI (we will use KinD cluster, install in Windows)
  - kubectl Installed in WSL2 and Windows
  - Go installed in WSL2
  - GNU make installed and updated in WSL2
  - Operator-SDK CLI installed in WSL2

## About Operator framework

- Operator framework provides tools to extend the Kuberentes API, `kubectl api-resource` will list the existing resources from the cluster API with operator framework we can create a new resource and deploy it to the cluster.

- Operator-SDK CLI provides option to scaffolded the project and updates the project with the resource cordinates like Kind, Version and Group used to identify once the new resource is deployed to cluster. Few of the files will be edited, api/<version/`*type.go` file contains the go struct where we can define the properties this will be used to generate(CRD). The reconciler logic goes into *controller.go, the entry point will be the `main.go` file. The operator-sdk project utlizes kubebuilder undeneath. The details are explained later. The operator-sdk already has the code to create new manager in `main.go` and provides the basic code structure for reconciler logic under `controllers/` folder.

- Once the CRD is deployed to the cluster, we need a controller logic which will monitor for any changes to the resources. For example, when the resource is created the controller will know of this event and in operator will invoke the defined function.
 
### Install GoLang in WSL2

- To install Go, follow the instruction in this [Go documenation](https://go.dev/doc/install)

```
wget https://dl.google.com/go/go1.20.4.linux-amd64.tar.gz
sudo tar -xvf go1.20.4.linux-amd64.tar.gz
sudo mv go /usr/local
```

- Open the `~/.bashrc` file and add the below variables so it will update as environment variables

```
export GOROOT=/usr/local/go
export GOPATH=$HOME/go
export PATH=$GOPATH/bin:$GOROOT/bin:$PATH
```

- To find the Go version, issuing `go version` command we should see the response like below.

```
$ go version
go version go1.20.4 linux/amd64
```

### Install KinD CLI in Windows

Download and install KinD CLI, this can be installed via Chocolatey, refer the [documentation]( https://community.chocolatey.org/packages/kind)

The Kubernetes cluster that was created using KinD, details below.

```
~$ kubectl version -o yaml
clientVersion:
  buildDate: "2023-05-17T14:20:07Z"
  compiler: gc
  gitCommit: 7f6f68fdabc4df88cfea2dcf9a19b2b830f1e647
  gitTreeState: clean
  gitVersion: v1.27.2
  goVersion: go1.20.4
  major: "1"
  minor: "27"
  platform: linux/amd64
kustomizeVersion: v5.0.1
serverVersion:
  buildDate: "2023-06-15T00:36:28Z"
  compiler: gc
  gitCommit: 25b4e43193bcda6c7328a6d147b1fb73a33f1598
  gitTreeState: clean
  gitVersion: v1.27.3
  goVersion: go1.20.5
  major: "1"
  minor: "27"
  platform: linux/amd64
```

### Install GNU make in WSL2 

- To install GNU make version, simply use `sudo apt install make`

```
$ make -version
GNU Make 4.3
Built for x86_64-pc-linux-gnu
Copyright (C) 1988-2020 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
```

### Install Operator-SDK CLI in WSL2

- To install operator-sdk refer the Operator SDK [documentation](https://sdk.operatorframework.io/docs/installation/), it is self explainotary.

- Once installed, we can view the version refer below snippet for command and output response.

```
$ operator-sdk version
operator-sdk version: "v1.30.0", commit: "b794fe909abc1affa1f28cfb75ceaf3bf79187e6", kubernetes version: "1.26.0", go version: "go1.19.10", GOOS: "linux", GOARCH: "amd64"
```

## Creating new Operator-SDK project for development

- Create a new folder, using the operator-sdk CLI we can initalize the project. The `operator-sdk init` command will scaffold the necessary files. The command looks like below, the switch options explained below.

```
operator-sdk init --domain greetapp.com --repo github.com/thirumurthis/app-operator
```

### Output of operator-sdk init

```
Writing kustomize manifests for you to edit...
Writing scaffold for you to edit...
Get controller runtime:
$ go get sigs.k8s.io/controller-runtime@v0.14.1
Update dependencies:
$ go mod tidy
Next: define a resource with:
$ operator-sdk create api
```

- The switch option in the operator-sdk command above used to scaffold a project.

  - `--domain` in the command is required which will identify the resource when using `kubectl api-resource`

```
$ kubectl api-resources | grep -e greet -e NAME
NAME        SHORTNAMES   APIVERSION                             NAMESPACED   KIND
greets                   greet.greetapp.com/v1alpha1            true         Greet
```
  - `--repo` is uses to specify that this project is outside go path


## Update the scaffolded project with the API with operator-sdk CLI

- As mentioned above the API is identified by Kuberentes cluster using Kind, Version and Group, we using operator-sdk with these coordinates as switch options to update the already created operator-sdk project.
- This will update the project strucutre by updating *type.go file and other files.
- The outut of `operator-sdk init` command also directs this step to be next. 
- The kind and version will be used in the CRD manifest.


- Command to use operator-sdk CLI to create API on the scaffolded project

```
$ operator-sdk create api --group greet --version v1alpha1 --kind Greet --resource --controller
```

### Output of operator-sdk create api command

```
Writing kustomize manifests for you to edit...
Writing scaffold for you to edit...
api/v1alpha1/deploymentscaler_types.go
controllers/deploymentscaler_controller.go
Update dependencies:
$ go mod tidy
Running make:
$ make generate
mkdir -p /mnt/c/goOperator/deploymentscaler/bin
test -s /mnt/c/goOperator/deploymentscaler/bin/controller-gen && /mnt/c/goOperator/deploymentscaler/bin/controller-gen --version | grep -q v0.11.1 || \
GOBIN=/mnt/c/goOperator/deploymentscaler/bin go install sigs.k8s.io/controller-tools/cmd/controller-gen@v0.11.1
/mnt/c/goOperator/deploymentscaler/bin/controller-gen object:headerFile="hack/boilerplate.go.txt" paths="./..."
Next: implement your new API and generate the manifests (e.g. CRDs,CRs) with:
$ make manifests
```

Note:
  - The operator-sdk project includes a Makefile with bunch of useful commands that will be used during development.
  - In WSL2 terminal, if we are already navigated to the project folder issue `make manifests` which will generate CRD file. This yaml file will be under `config/crd/bases/`, in this case the file name will start with greet.


### Update the go mod with the lastest librariy version

- This step is optional, we can update the `go.mod` file from the porject structure so the project can use the the latest kubernetes libraries. These are the latest version from go package documentation at the time of writing. 
- The `go.mod` file with the updated version looks like below. Once the file is updated, issue `go mod tidy` to download the latest libraries. `go.mod` file is more like depenedency manager for Go, like maven for Java or pacakge.json in node.js.

```go
go 1.19

require (
        github.com/onsi/ginkgo/v2 v2.9.5
        github.com/onsi/gomega v1.27.7
        k8s.io/apimachinery v0.27.4
        k8s.io/client-go v0.27.4
        sigs.k8s.io/controller-runtime v0.15.0
)
```

### Adding new property to the CRD

- Lets add new property to the CRD called `Name`, latter we will see how we can read this value from the reconciler code and print this in log

- The `Name` property should be updated in the api/v1alpha1/`*_types.go`, where `v1alpha1` is the version used in the `operator-sdk create api` command. 

- The new property is updated in the `GreetSpec` struct.
   - On top of the new property we add two markers which will perform length validation (comments 1 and 2).
   - Note, the comment `//Name of the resource` will be displayed in the CRD, when we create one from this project using `make manifests`. Once deployed to the cluster the same will be displayed in `kubectl explain <crd-resource>`
  - Other section of the type.go file remains unchanged

- With the below change, if we issue `make 

```go
package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type GreetSpec struct {

    //Name of the resource 
    // +kubebuilder:validation:MaxLength=15   // --->  1 maker for validation
    // +kubebuilder:validation:MinLength=1    // --->  2 maker for validation
    Name string `json:"name"`    //------------------> 3 new property for the CRD
}

// GreetStatus defines the observed state of Greet
type GreetStatus struct {
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
type Greet struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   GreetSpec   `json:"spec,omitempty"`
	Status GreetStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// GreetList contains a list of Greet
type GreetList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Greet `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Greet{}, &GreetList{})
}
```

- With the above code change, if we issue `make manifests` command, this will create the CRD file under `config/crd/bases/`. The newly added properties looks like below

```yaml
            properties:
              name:
                description: Name of the resource
                maxLength: 15
                minLength: 1
                type: string
            required:
            - name
            type: object
```

### Update the controller code to read the new property

- Whenever the `api/v1alpha1/`*_types` file is updated, we need to use `make generate` command. This command will perform updates the project with the newly included properties.

- Below is the reconciler Go code created by operator-sdk under `controllers`.

- In the Reconcile function, the `log.Log` is used to log simple message
- We initialize an instance of the Greet CRD object which has Name property
- The `r.Get()` will fetch the CRD instance from the Cluster, in case of error when obtaining the object simply return it.
- If the CRD instance is availabe, we get the name and log it.
- At this point there are NO changes to other files in the project.

```go
func (r *GreetReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	log.Log.Info("Reconciler invoked..") // -------------> Logs the message passed
	instance := &greetv1alpha1.Greet{}   //--------------> initalize an instance of the CRD 

	err := r.Get(ctx, req.NamespacedName, instance)

	if err != nil {
		return ctrl.Result{}, nil
	}
 
    appName := instance.Spec.Name        // Get the name from the CR deployed 

    log.Log.Info(fmt.Sprintf("app Name for CRD is - %s ",appName))
    if instance.Spec.Name != "" {
	  log.Log.Info(fmt.Sprintf("appName for CRD is - %s ",instance.Spec.Name))
    } else {
      log.Log.Info("instance.Spec.Name - NOT FOUND")
    }

    return ctrl.Result{}, nil
}
```

### Create the Custom resource for the Greet CRD

 - The skeleton of the Custom Resource file will be created when we issue `make manifests`, where the newly added property is used here `name: first-app`

```yaml
apiVersion: greet.greetapp.com/v1alpha1
kind: Greet
metadata:
  labels:
    app.kubernetes.io/name: greet
    app.kubernetes.io/instance: greet-sample
    app.kubernetes.io/part-of: app-op
    app.kubernetes.io/managed-by: kustomize
    app.kubernetes.io/created-by: app-op
  name: greet-sample
spec:
  # TODO(user): Add fields here
  name: first-app
```

### Deploy operator to local KinD cluster

- The operator-sdk includes additional make targets, to help in development. Issuing below command will deploy the controller changes to local kind cluster. The output looks like below..

Note: 
   - The log statement added in the reconciler will be is displayed once we deploy the Custom resoruce to cluster.
   - In below first I deployed the Custom resource first, so when the operator changes where deployed it picked and printed the log message.

Info:
   - If `make generate install run` from the WSL2 terminal was not able to find the Kuberentes cluster, you can copy the kube config from the windows `.kube` to WSL2 path. 
   - Form WSL2 terminal, you can issue the command `cp /mnt/c/Users/<username>/.kube/config ~/.kube/config`

```
$ make generate install run
....
go run ./main.go
2023-07-23T13:28:13-07:00       INFO    controller-runtime.metrics      Metrics server is starting to listen    {"addr": ":8080"}
2023-07-23T13:28:13-07:00       INFO    setup   starting manager
2023-07-23T13:28:13-07:00       INFO    Starting server {"kind": "health probe", "addr": "[::]:8081"}
2023-07-23T13:28:13-07:00       INFO    starting server {"path": "/metrics", "kind": "metrics", "addr": "[::]:8080"}
2023-07-23T13:28:13-07:00       INFO    Starting EventSource    {"controller": "greet", "controllerGroup": "greet.greetapp.com", "controllerKind": "Greet", "source": "kind source: *v1alpha1.Greet"}
2023-07-23T13:28:13-07:00       INFO    Starting Controller     {"controller": "greet", "controllerGroup": "greet.greetapp.com", "controllerKind": "Greet"}
2023-07-23T13:28:13-07:00       INFO    Starting workers        {"controller": "greet", "controllerGroup": "greet.greetapp.com", "controllerKind": "Greet", "worker count": 1}
2023-07-23T13:28:28-07:00       INFO    Reconciler invoked..
2023-07-23T13:28:28-07:00       INFO    app Name for CRD is - first-app
2023-07-23T13:28:28-07:00       INFO    appName for CRD is - first-app
2023-07-23T13:28:28-07:00       INFO    GET invoked reconile for resource name - greet-sample
```

### Output once operator deployed

![image](https://github.com/thirumurthis/Learnings/assets/6425536/782d4fab-7529-4584-9371-d69b0237e3cd)

- We can describe the CRD
It looks like below, note there are no event info, next we will try to add a report object and track the event.

 ![image](https://github.com/thirumurthis/Learnings/assets/6425536/c898b5fe-1cc4-48f3-b5b2-329694e2b1e4)


## Tracking Event in the Controller

- To record the events, first we need to add recorder to the GreetReconciler struct in the `controllers/*controller.go` file and import the library for record.

```
 import(
  ....
    "k8s.io/client-go/tools/record"    // Import the library for record
  ....
)

type GreetReconciler struct {
	client.Client
	Scheme *runtime.Scheme
	Recorder record.EventRecorder      // New Recorder event
}
```

- We will use the Recorder event object to record events and the code looks like below

```go
func (r *GreetReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	log.Log.Info("Reconciler invoked..")
        instance := &greetv1alpha1.Greet{}
	err := r.Get(ctx, req.NamespacedName, instance)
       
	if err != nil {
	    // If there are errors we record an event.
	    r.Recorder.Event(instance, corev1.EventTypeWarning, "Object", "Failed to read Object")
        return ctrl.Result{}, nil
	}
 
       appName := instance.Spec.Name
       // Recorder event added
       r.Recorder.Event(instance, corev1.EventTypeWarning, "Object", fmt.Sprintf("Created - %s ",appName))

       //..... other code
	return ctrl.Result{}, nil
}
```

- Since the `GreetReconciler` struct is updated to include a recorder object, the `main.go` needs to fetch the recorder object from the manager and set to the struct.

 ```go
    if err = (&controllers.GreetReconciler{
                Client: mgr.GetClient(),
                Scheme: mgr.GetScheme(),
                Recorder: mgr.GetEventRecorderFor("greet-controller"), //Obtain the recorder
    }).SetupWithManager(mgr); err != nil {
        setupLog.Error(err, "unable to create controller", "controller", "Greet")
        os.Exit(1)
    }
```

### Output events are displayed when we describe the CRD

- If the operator is running in local cluster stop it. `make generate install run` 
- Delete the CR if it already deployed using `kubectl delete`
- Once deployed the operator will display the logs

![image](https://github.com/thirumurthis/Learnings/assets/6425536/4ac8148b-9ca4-472a-8fd5-e63bfc317c42)

- In Recorder.Event the "Object" is the displayed as reason of the event. 

```
 r.Recorder.Event(instance, corev1.EventTypeWarning, "Object", fmt.Sprintf("Created - %s ",appName))
```

### Output where the Recorder.Event is updated with different value

![image](https://github.com/thirumurthis/Learnings/assets/6425536/11cb3ad3-3f50-439d-be7a-9d2c6f407511)


## Update the status of the resource in Controller

- Once the Controller and Custom resources are deployed we can track the status with `kubectl get greet/greet-sample -w`,

![image](https://github.com/thirumurthis/Learnings/assets/6425536/4c1df07f-add4-48b4-a8e3-f63e22563705)

- In the `*_type.go` file we need to add the marker like below over the corresponding `Greet` struct in this case.

```
//+kubebuilder:printcolumn:name="APPNAME",type="string",JSONPath=".spec.name",description="Name of the app"
//+kubebuilder:printcolumn:name="STATUS",type="string",JSONPath=".status.status",description="Status of the app"
```

- Code snippet of the `type.go` file, in the `GreetStatus`, which we included `Status` string type.
- In the reconciler will be updated status dynamically in the code which we will update in the `controller.go`.

```
// GreetStatus defines the observed state of Greet
type GreetStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "make" to regenerate code after modifying this file
        Status string `json:"status,omitempty"`
}

//Don't leave any space between the marker - ADD below 

//+kubebuilder:object:root=true
//+kubebuilder:printcolumn:name="APPNAME",type="string",JSONPath=".spec.name",description="Name of the app"
//+kubebuilder:printcolumn:name="STATUS",type="string",JSONPath=".status.status",description="Status of the app"
//+kubebuilder:subresource:status
// +operator-sdk:gen-csv:customresourcedefinitions.displayName="Greet App"
// +operator-sdk:gen-csv:customresourcedefinitions.resources="Deployment,v1,\"A Kubernetes Deployment of greet app\""

type Greet struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   GreetSpec   `json:"spec,omitempty"`
	Status GreetStatus `json:"status,omitempty"`
}
```

### `main.go` 

```
func (r *GreetReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)
	log.Log.Info("Reconciler invoked..")
	instance := &greetv1alpha1.Greet{}

	if err := r.Get(ctx, req.NamespacedName, instance); err != nil {
             r.Recorder.Event(instance, corev1.EventTypeWarning, "Object", "Failed to read Object")
             log.Log.Info("Error while reading the object")
             return ctrl.Result{},client.IgnoreNotFound(err)
	}
 
       appName := instance.Spec.Name

       if instance.Spec.Name != "" {
	  log.Log.Info(fmt.Sprintf("appName for CRD is - %s ",instance.Spec.Name))
          r.Recorder.Event(instance, corev1.EventTypeWarning, "Greet", fmt.Sprintf("Created - %s ",appName))
       } else {
          log.Log.Info("instance.Spec.Name - NOT FOUND")
       }

      // update the ok when it is blank
      if instance.Status.Status == "" {
          instance.Status.Status = "OK"
          log.Log.Info("instance.Spec.Name - is set to OK")
      }

      // update the status with client
      if err := r.Status().Update(ctx, instance); err != nil {
             log.Log.Info("Error while reading the object")
             return ctrl.Result{},client.IgnoreNotFound(err)
      }
      return ctrl.Result{}, nil
}
```

### output
- Once the `manifest` is updated and deployed with the `kubectl apply -f <manifest>`, then on watching the resources should see the status being updated. 

![image](https://github.com/thirumurthis/Learnings/assets/6425536/fbb3b88b-fd7a-4d2b-9bb4-36d4f3976e7d)

