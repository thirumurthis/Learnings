### Steps to setup ingress-nginx controller (the kubernetes community managed version)

- Use the installation guide as mentioend in this [link](https://kubernetes.github.io/ingress-nginx/deploy/#provider-specific-steps)

- For a local kubernetes master and 2 worker node installed in local, use the `bare-metal` instruction.

- use below command:
```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/provider/baremetal/deploy.yaml
```

After everything setup, check the ingress-nginx namespace for the pods and svc running.

##### Use below command to just makes sure everything is working

```
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```  

After everything was setup correctly, when executing a ingress-rule noticed below error message:

Note: k set with the alias for kubectl `(alias k=kubectl)`

```
[vagrant@kmaster learn]$ k apply -f ingressresource.yaml
Error from server (InternalError): error when creating "ingressresource.yaml": Internal error occurred: failed calling webhook "validate.nginx.ingress.kubernetes.io": Post https://ingress-nginx-controller-admission.ingress-nginx.svc:443/extensions/v1beta1/ingresses?timeout=30s: net/http: request canceled while waiting for connection (Client.Timeout exceeded while awaiting headers)
```
##### As per the Git issue, if the VadlidatingWebhookconfiguration is deleted, it is exepcted to work.
```
[vagrant@kmaster learn]$ kubectl get -A ValidatingWebhookConfiguration
NAME                      WEBHOOKS   AGE
ingress-nginx-admission   1          3m15s
```

#### Solution to fix the webhook issue
```
[vagrant@kmaster learn]$ kubectl delete -A ValidatingWebhookConfiguration ingress-nginx-admission
validatingwebhookconfiguration.admissionregistration.k8s.io "ingress-nginx-admission" deleted
```

##### After deleting the below yaml file worked.
```yaml
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: watch-wear-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - http:
     paths:
     - path: /watch
       backend:
         serviceName: watch-service
         servicePort: 8080
     - path: /wear
       backend:
         serviceName: wear-service
         servicePort: 8080

```

Some references:
 ```
 [vagrant@kmaster learn]$ k get pods
NAME                            READY   STATUS    RESTARTS   AGE
watch-deploy-67496c5475-67rvg   1/1     Running   0          8h
watch-deploy-67496c5475-lhwdx   1/1     Running   0          8h
wear-deploy-85c6d85d6-5xx4z     1/1     Running   0          8h
[vagrant@kmaster learn]$ k logs pod/ingress-nginx-controller-75f84dfcd7-wpwg9 -n ingress-nginx
-------------------------------------------------------------------------------
NGINX Ingress controller
  Release:       0.33.0
  Build:         git-589187c35
  Repository:    https://github.com/kubernetes/ingress-nginx
  nginx version: nginx/1.19.0

-------------------------------------------------------------------------------

I0704 23:17:42.004106       6 flags.go:204] Watching for Ingress class: nginx
......

 ```
 
 ```
 [vagrant@kmaster learn]$ k get ing
NAME                 CLASS    HOSTS   ADDRESS         PORTS   AGE
watch-wear-ingress   <none>   *       172.42.42.101   80      5h9m
[vagrant@kmaster learn]$ k describe ing/watch-wear-ingress
Name:             watch-wear-ingress
Namespace:        default
Address:          172.42.42.101
Default backend:  default-http-backend:80 (<error: endpoints "default-http-backend" not found>)
Rules:
  Host        Path  Backends
  ----        ----  --------
  *
              /watch   watch-service:8080 (192.168.33.246:8080,192.168.33.247:8080)
              /wear    wear-service:8080 (192.168.33.250:8080)
Annotations:  nginx.ingress.kubernetes.io/rewrite-target: /
Events:       <none>
```

```
$ kubectl get events -n=ingress-nginx
```
#### Get the  services port nodeport info
```
[vagrant@kmaster learn]$ k get svc
NAME            TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
kubernetes      ClusterIP   10.96.0.1      <none>        443/TCP    20d
watch-service   ClusterIP   10.99.192.47   <none>        8080/TCP   8h
wear-service    ClusterIP   10.106.29.3    <none>        8080/TCP   8h
[vagrant@kmaster learn]$ k get svc -n=ingress-nginx
NAME                                 TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
ingress-nginx-controller             NodePort    10.102.129.197   <none>        80:31780/TCP,443:30523/TCP   5h27m
ingress-nginx-controller-admission   ClusterIP   10.104.192.217   <none>        443/TCP                      5h27m
[vagrant@kmaster learn]$

```

since the nodePort is available for the nginx ingress controller,
  - using the url http://172.42.42.101:31780/watch the correponding service got executed and appropriate page got loaded.
  - (in this case the ip address 172.42.42.101 was the worker node where the ingress-nginx controller pod got installed, port was the service port.)


