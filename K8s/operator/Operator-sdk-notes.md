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
