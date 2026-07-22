## Then the API endpoints should be accessible

If the API is not accessible, then try below option to add host to the coredns

kubectl edit configmap coredns -n kube-system

   .:53 {
        errors
        health {
           lameduck 5s
        }
        ready
		# add below 
        hosts {
            172.18.0.1 host.docker.internal
            fallthrough
        }
		
        kubernetes cluster.local in-addr.arpa ip6.arpa {
           pods insecure
           fallthrough in-addr.arpa ip6.arpa
           ttl 30
        } 
		
## add the hosts to the deployment deployed

kubectl patch -n hoppscotch deployment hoppscotch-community --type='json' -p='[
  {
    "op": "add",
    "path": "/spec/template/spec/hostAliases",
    "value": [
      {
        "ip": "172.18.0.1",
        "hostnames": ["host.docker.local"]
      }
    ]
  }
]'
