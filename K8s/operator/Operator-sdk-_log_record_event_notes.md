Install Go, make and operator-sdk in WSL2 when working in window machine

Currently I am using Kind (version 1.27.0)


- kind cluster version used 

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

- Go version

```
$ go version
go version go1.20.4 linux/amd64
```

- make version

```
$ make -version
GNU Make 4.3
Built for x86_64-pc-linux-gnu
Copyright (C) 1988-2020 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
```

```
$ operator-sdk version
operator-sdk version: "v1.30.0", commit: "b794fe909abc1affa1f28cfb75ceaf3bf79187e6", kubernetes version: "1.26.0", go version: "go1.19.10", GOOS: "linux", GOARCH: "amd64"
```

- Create a folder and issue below command to scaffolding the operator skd project
```
operator-sdk init --domain greetapp.com --repo github.com/thirumurthis/app-operator
```

### Output:
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

## Create the api with the resource coordinates within the initialized operator project
```
$ operator-sdk create api --group scaler --version v1alpha1 --kind DeploymentScaler --resource --controller
```

### Output:
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

- We are using latest version of the pacakges, so we update the `go.mod` file, looks like below.

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

First we add a new property to the CRD called Name, and read this value from the reconciler
and print it in the log
1. Lets add a name to the `*_types.go` which will be the name of the app and file by adding a name string 
  From the below code, 
   1 and 2 are annotation added for length validation 
   3 is the new property name for hte resource
  
```go
package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type GreetSpec struct {

    //Name of the resource 
    // +kubebuilder:validation:MaxLength=15   // --->  1
    // +kubebuilder:validation:MinLength=1    // --->  2
    Name string `json:"name"`    //------------------> 3
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

With the above changes, we need to issue `make generate` and `make manifest`. 
- the sample CRD manifest will be created, in this case will look like below

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
  name: first-app
```
- Controller changes
- At this point there are NO changes to other files,
```go
func (r *GreetReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	// TODO(user): your logic here
	log.Log.Info("Reconciler invoked..") // -------------> Logs the message passed
	instance := &greetv1alpha1.Greet{}   //--------------> create an instance of the CRD 

	err := r.Get(ctx, req.NamespacedName, instance)

	if err != nil {
		return ctrl.Result{}, nil
	}
 
    appName := instance.Spec.Name

    log.Log.Info(fmt.Sprintf("app Name for CRD is - %s ",appName))
    if instance.Spec.Name != "" {
	  log.Log.Info(fmt.Sprintf("appName for CRD is - %s ",instance.Spec.Name))
    } else {
      log.Log.Info("instance.Spec.Name - NOT FOUND")
    }

    if err := r.Get(ctx, req.NamespacedName, instance); err == nil {
      log.Log.Info(fmt.Sprintf("GET invoked reconile for resource name - %s", instance.GetName()))
    }

    return ctrl.Result{}, nil
}

```
- now with the above change, we need to apply it to local kind environment
- Below command will deploy the controller changes to local kind cluster, the output looks like below..
note: the log statement added is displayed, when the above CRD is applied
```
$ make install run
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

## output 

![image](https://github.com/thirumurthis/Learnings/assets/6425536/782d4fab-7529-4584-9371-d69b0237e3cd)

- We can describe the CRD
It looks like below, note there are no event info, next we will try to add a report object and track the event.

 ![image](https://github.com/thirumurthis/Learnings/assets/6425536/c898b5fe-1cc4-48f3-b5b2-329694e2b1e4)


# Adding recorder event to the operator
- Add recorder to the struct in the *controller.go file and import the record

```
 import(
  ....
    "k8s.io/client-go/tools/record"
  ....
)

type GreetReconciler struct {
	client.Client
	Scheme *runtime.Scheme
	Recorder record.EventRecorder
}
```
- Controller code that updates and adds the recorder.

```go
func (r *GreetReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	log.Log.Info("Reconciler invoked..")
        instance := &greetv1alpha1.Greet{}
	err := r.Get(ctx, req.NamespacedName, instance)
       
	if err != nil {
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
- In main we included the records and get  the `Recorder` from manager.

 ```go
    if err = (&controllers.GreetReconciler{
	Client: mgr.GetClient(),
	Scheme: mgr.GetScheme(),
        Recorder: mgr.GetEventRecorderFor("greet-controller"), //MANUAL: added the recorder
    }).SetupWithManager(mgr); err != nil {
	setupLog.Error(err, "unable to create controller", "controller", "Greet")
	os.Exit(1)
    }
```

### output image where the events are displayed when we describe the CRD

![image](https://github.com/thirumurthis/Learnings/assets/6425536/4ac8148b-9ca4-472a-8fd5-e63bfc317c42)

- In below code if the Object is the reason of the event. In the above snapshot at the bottom we could see the value
```
 r.Recorder.Event(instance, corev1.EventTypeWarning, "Object", fmt.Sprintf("Created - %s ",appName))
```

### Output where the updated reason

![image](https://github.com/thirumurthis/Learnings/assets/6425536/11cb3ad3-3f50-439d-be7a-9d2c6f407511)

## Below is an example how to dynamically update the status update

- So when we issue `kubectl get greet/greet-sample -w`,

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

