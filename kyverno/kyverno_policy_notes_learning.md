
What is kyverno policy?


Policy: 
- Policy is set of rules that define how resources should be configured in a cloud native environment.
- This policy ensure compilance, security and efficiency.
- Every Policy is composed of 1 or more rules.
- Each rule has `match` and `exclude` - this is to filter on resources on which certain policy has to be applied.
  - Match and Exclude on resource type - Resource Kinds, Resource Names, Labels, Annotations, Operations, Namespaces, Namespace labels, (cluster) Roles, users, groups, Service Account
  - Four types of rules
     - 1. Validate - based on pattern matching rule returns true or false
     - 2. Mutate - mutatue the incoming request
     - 3. Generate - to create resource on the fly
     - 4. Verify Images - used for imanges inage signature and attesations

- Sample policy

```yaml
apiVersion: kyverno.io/v1
kind: ClusterPolicy
metadata:
  name: disallow-latest-tag
spec:
  validationFailureAction: Enforce
  background: true
  rules:
  - name: validate-image-tag
    match:
      any:
      - resources:
         kinds:
         - Pod
    validate:
      message: "An image tag is required (:latest is not allowed)"
      pattern:  # match the image based on the pattern
        spec:
          containers:
          - image: "!*:latest"
```

Note, the `validationFailureAction: Enforce` which means if the rule is false the request will be dropped. There is another option `Audit` which will allow the request but write to audit.

The above validate rule will not allow any image with latest tag.

Why we need policy Exception?

- The match and exclude, used to provide fine-grained control to resources, but this needs to be applied upfront. This might be limiting.
- Policies not directly editable
- Decouple policy authoring from exclusions
- Leads to collebration between teams in enterprises
  - review a policy exception before deploying
  - temporary exception to policies (using Cleanp Policies)
  - exceptions are short-lived

Flow diagram

```
             Start
               |
               |
        validate policy if enforce mode exists
               |
               |
     user/process sends violating resource
               | 
               |
               ?
     Matching PolicyException exists?
             /   \
           no      yes 
            |       |    
     resource       resource
     blocked        allowed
```

- To check if the `exception` flag is set to `true` on the deployed `kyverno` deployment.

```sh
$ k get deploy kyverno -n kyverno -o yaml | grep -i "exception"
 - --enablePolicyException=true
 - --exceptionNamespace=kyverno-exceptions
```

Note, the kyverno exception need to be applied in the exceptionNamespace `kyverno-exceptions` This namespace can be `kyverno` itself. This will be useful to deploy all exception to one namespace.

Below is an example exception where the manfiest should set the host* flags to false or not to be set in manifest

```yaml
# filename: kyverno-host.yaml
apiVersion: kyverno.io/v2beta1
kind: ClusterPolicy
metadata:
  name: disallow-host-namespaces
spec:
  validationFailureAction: Enforce
  background: false
  rules:
  - name: host-namespaces
    match:
      any:
      - resources:
          kinds:
            - Pod
    validate:
       message: >-
        Sharing the host namespaces is disallowed. The fields spec.hostNetwork, spec.hostIPC, and spec.hostPID must be unset or set to `false`.
       pattern:
         spec:
           =(hostPOD): "false"
           =(hostIPC): "false"
           =(hostNetwork): "false"
```

```
k apply -f kyverno-host.yaml
```

```
k get cpol
```

Say, we have a deployment like below which will be blocked due to the above kyverno policy

```yaml
# deploy-resource.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-tool
  namespace: test
  labels:
    app: busybox
spec:
    replicas: 1
    selector:
      matchLabels:
        app: busybox
    template:
      metadata:
         labels:
          app: busybox
      spec:
        hostIPC: true
        containers:
        - image: busybox:1.35
          name: busybox
          command: ["sleep", "1d"]
          resources:
            requests:
              memory: "64Mi"
              cpu: "150m"
            limits:
              memory: "128Mi"
              cpu: "250m"
```

```
$ k apply -f deploy-resources.yaml
-- we would see error message
```

### IMPORTANT UNDERSTANDING ON autogen
Note, in the error message we would see `rule autogen-host-namespaces failed at path /...`. The key think is the policy match to Pod, but we are deploying a deployment. Kyverno intelligent enough to determine the higher order controller like deployment, statefulset, job, etc. The kyverno generates rules for higher order deployment and rule name is autgen-host-namespaces, from autogen-<policy-name>

- Lets apply kyverno exception

```yaml
apiVersion: kyverno.io/v2alpha1
kind: PolicyException
metadata:
  name: delta-exception
  namespace: kyverno-exceptions  # important based on the deployed kyverno property exceptionNamespace
spec:
  exceptions:
   - policyName: disallow-host-namespaces
     ruleNames:
     - hose-namespaces
     - autogen-host-namespaces
   match:
     any:
     - resources:
        kinds:
        - Pod
        - Deployment
        namespaces:
        - test
        names:
        - test-tool*
```

Note, we have added the ruleName `autogen-host-names` which is the policy generated by kyverno for higher order resources. 

### IMPORTANT
Kyverno doesn't create autogen policy for higher order resources for PolicyExceptions, it is good practice to limit the policy exception for. That is the reason we also add the exception match to Deployment in this case.



