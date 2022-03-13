With reference to the link below 

https://devopscube.com/kubernetes-cluster-vagrant/

1. I cloned the git project

```
git clone https://github.com/scriptcamp/vagrant-kubeadm-kubernetes
```

2. when issuing vagarant up, there was an issue with the Netowrk of Vagrant.
  - Networking issue reported and fixed as mentioned in this link https://github.com/thirumurthis/Learnings/blob/master/vagrant/vagrantdetails.md

After updating the Network interface driver and updating the hostupdater vagrant plugin, the installation of K8s progressed.
