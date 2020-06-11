### To attach the K8s dashboard follow the below link. (refer the other Kubernetes cluster installation in this project)

From the Kuberentes master or workstation provide the following command:

```
$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.1/aio/deploy/recommended.yaml
```

In order to access the url, as per the docs, i issued the 
```
$ kubectl proxy
### this didn't work.
```

When i use the `http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/` i got the exception.

Below error displayed in the web browser console
```
Error: 'dial tcp 192.168.1.2:8443: i/o timeout
```

Tried out option to fix the time out issue:
##### Option 1 (didn't work)
- I initally edited the dashboard service from `type: ClusterIp` to `type: NodePort` as suggested in some forums. But this didn't work.
```
kubectl -n kubernetes-dashboard edit service kubernetes-dashboard
```

##### Option 2 (actually worked)
Below option fixed the issue, i was able to see the k8s dasboard:
 - I cleaned up the deployment, services, and namespace of `kubernetes-dashboard`.
 - Redeployed the service using the steps in the documentation using `4 kubectl apply -f https://raw.github....`
 - From the workstation, used port-forwarding option command used was below.
```
$  kubectl port-forward -n kubernetes-dashboard service/kubernetes-dashboard 8080:443
Forwarding from 127.0.0.1:8080 -> 8443
Forwarding from [::1]:8080 -> 8443
Handling connection for 8080
Handling connection for 8080
```
 - Opened the link `https://localhost:8080`
   - since there was no certificate setup, have opened up in unsecured way.
   - on opening up the login screen it requested for kube config or bearer token
   - Generated the token using below command
   ```
   $ kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')
   ```
   
Resources/link:

Creating sample user [link](https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/creating-sample-user.md)

Bear token info [link](https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/README.md#bearer-token)
