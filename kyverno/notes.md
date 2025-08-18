## installation

helm repo add kyverno https://kyverno.github.io/kyverno/
helm repo update


# to install the kyverno with the exeption
helm upgrade --install kyverno kyverno/kyverno -n kyverno --create-namespace \
  --set features.policyExceptions.enabled=true \
  --set features.policyExceptions.namespace="kyverno-exceptions"

- The ploicyexception flag should be set in the kyverno-admimission-controller deployment
- if they are not set and the namespace is not specified than it will not work.
- so check the deployment manifest. so restarts the deployment.
- with the deployed kyverno validation and exception policy it should work. 
- Note the exception policy should be deployed to the `kyverno-exceptions` namespace.
- else will not effect the clusterpolicy

## optional

helm upgrade -install kyverno/kyverno-policies -n kyverno

## only for repoca count

helm install kyverno kyverno/kyverno -n kyverno --create-namespace \
--set admissionController.replicas=3 \
--set backgroundController.replicas=2 \
--set cleanupController.replicas=2 \
--set reportsController.replicas=2

