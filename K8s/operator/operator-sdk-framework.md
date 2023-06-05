- Install `git`, `make` in the WSL2

- The operator code in this example will be auto scaling the deployments in the specific namespace.
 
## 1. Init the operator project structure for the operator 

```
$ operator-sdk init --domain podscaler.com --repo github.com/thirumurthis/podscaler-operator
```

###  output:
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

## 2. Create the API by specifying resource coorinates the Kuberntes API would uniquely identify resources

```
$ operator-sdk create api --group scaler --version v1alpha1 --kind PodScaler --resource --controller
```

### output:
```
Writing kustomize manifests for you to edit...
Writing scaffold for you to edit...
api/v1alpha1/podscaler_types.go
controllers/podscaler_controller.go
Update dependencies:
$ go mod tidy
Running make:
$ make generate
mkdir -p /mnt/c/goOperator/podscaler-operator/bin
test -s /mnt/c/goOperator/podscaler-operator/bin/controller-gen && /mnt/c/goOperator/podscaler-operator/bin/controller-gen --version | grep -q v0.11.1 || \
GOBIN=/mnt/c/goOperator/podscaler-operator/bin go install sigs.k8s.io/controller-tools/cmd/controller-gen@v0.11.1
/mnt/c/goOperator/podscaler-operator/bin/controller-gen object:headerFile="hack/boilerplate.go.txt" paths="./..."
Next: implement your new API and generate the manifests (e.g. CRDs,CRs) with:
$ make manifests
```

### to build the image and deploying the operator.
make docker-build docker-push IMG="example.com/memcached-operator:v0.0.1"

# to generate the manfest yaml after making the changes
make mainfests

# the change should be done to 
![image](https://github.com/thirumurthis/Learnings/assets/6425536/ae479f8f-5449-4e41-b35e-adf5f7ae0189)

```go
/*
Copyright 2023.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// PodScalerSpec defines the desired state of PodScaler
type PodScalerSpec struct {
	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "make" to regenerate code after modifying this file

	ScaleCount  int32           `json:"scaleCount"`
	Deployments []NameNamespace `json:"deployments"`
}

type NameNamespace struct {
	Name      string `json:"name"`
	Namespace string `json:"namespace"`
}

// PodScalerStatus defines the observed state of PodScaler
type PodScalerStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "make" to regenerate code after modifying this file
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// PodScaler is the Schema for the podscalers API
type PodScaler struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   PodScalerSpec   `json:"spec,omitempty"`
	Status PodScalerStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// PodScalerList contains a list of PodScaler
type PodScalerList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []PodScaler `json:"items"`
}

func init() {
	SchemeBuilder.Register(&PodScaler{}, &PodScalerList{})
}
```
- sample CRD yaml (based on the property provided below like `deployment`

```yaml
apiVersion: scaler.podscaler.com/v1alpha1
kind: PodScaler
metadata:
  labels:
    app.kubernetes.io/name: podscaler
    app.kubernetes.io/instance: podscaler-sample
    app.kubernetes.io/part-of: podscaler-operator
    app.kubernetes.io/managed-by: kustomize
    app.kubernetes.io/created-by: podscaler-operator
  name: podscaler-sample
spec:
  scaleCount: 5
  deployments: 
    - name: podscale
      namespace: project
```
- The yaml file code, controller file
![image](https://github.com/thirumurthis/Learnings/assets/6425536/0b821d73-126f-4662-8689-34a8c814cee4)

```go

package controllers

import (
	"context"
	"fmt"
	"time"

	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	scalerv1alpha1 "github.com/thirumurthis/podscaler-operator/api/v1alpha1"
	v1 "k8s.io/api/apps/v1"
)

// PodScalerReconciler reconciles a PodScaler object
type PodScalerReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=scaler.podscaler.com,resources=podscalers,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=scaler.podscaler.com,resources=podscalers/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=scaler.podscaler.com,resources=podscalers/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the PodScaler object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.14.1/pkg/reconcile
func (r *PodScalerReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	// TODO(user): your logic here
	log.Log.Info("reconsiler invoked")
	podScaler := &scalerv1alpha1.PodScaler{}
	err := r.Get(ctx, req.NamespacedName, podScaler)

	if err != nil {
		return ctrl.Result{}, nil
	}

	scaleCount := podScaler.Spec.ScaleCount
	for _, deploy := range podScaler.Spec.Deployments {
		deployment := &v1.Deployment{}

		err := r.Get(ctx,
			types.NamespacedName{
				Namespace: deploy.Namespace,
				Name:      deploy.Name,
			},
			deployment,
		)
		if err != nil {
			return ctrl.Result{}, err
		}

		log.Log.Info("invoke scale count")
		if deployment.Spec.Replicas != &scaleCount {
			deployment.Spec.Replicas = &scaleCount
			log.Log.Info(fmt.Sprint("deployment %v", deployment))
			err := r.Update(ctx, deployment)
			if err != nil {
				return ctrl.Result{}, err
			}
		}
	}

	return ctrl.Result{RequeueAfter: time.Duration(30 * time.Second)}, nil
}

// SetupWithManager sets up the controller with the Manager.
func (r *PodScalerReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&scalerv1alpha1.PodScaler{}).
		Complete(r)
}
```

### To generate the CRD manifest yaml issue below command
```
$ make manifests
```

### Any updates to the `*types.go` file, will be used to generate the CRD yaml. The generated yaml will be under below path
```
config/crd/bases/scaler*
```

### First CRD should be installed, then 
```
$ kubectl apply -f config/crd/bases/scaler.podscaler.com_podscalers.yaml
```

#### If the `kubectl` from wsl not able to connect to kind cluster, then copy the .kube/config to the $HOME/.kube/config
```
cp /mnt/c/Users/<username>/.kube/config ~/.kube/config
```

### To deploy the operator to the local Kind cluster for development run below command from the WSL2
```
$ make run 
```

#### The output of deployed operator in local cluster

![image](https://github.com/thirumurthis/Learnings/assets/6425536/a03133f2-5c9a-4caf-93bd-fc6bf8e0e208)

