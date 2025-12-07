### when installing the etcd using kubeadm

- use the `kubectl get pods -n kube-system`

```sh
 kubectl exec etcd-controlplane -n kube-system -- sh -c "ETCDCTL_API=3 etcdctl --cacert /etc/kubernetes/pki/etcd/ca.crt --key /etc/kubernetes/pki/etcd/server.key --cert /etc/kubernetes/pki/etcd/server.crt get / --prefix --keys-only"
```
