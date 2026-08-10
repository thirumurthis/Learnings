To create a kind cluster we can use kind cli 

To install ansible on Ubuntu (https://docs.ansible.com/projects/ansible/latest/installation_guide/installation_distros.html)

```
$ sudo apt update
$ sudo apt install software-properties-common
$ sudo add-apt-repository --yes --update ppa:ansible/ansible
$ sudo apt install ansible
```

To use the kuberentes python package for ansible to work with above installation use 

```
sudo apt install python3-kubernetes
```

Alternate options 
If creating virtual environment, probaly install the ansible using pipx within the virtual environment
within virtual environment the pipx can be installed and then ansible and kuberentes can be installed in that environment. 


To install kuberentes python install pipx global cli, to install ansible globally 

```
sudo apt install pipx
pipx ensurepath
pipx install kubernetes
```
Below is steps to deploy using ansible

Create a inventory file inventory.ini and the content looks like below 

which specifies that we want to configure the localhost as a kuberentes node

```
[kuberentes-node]
localhost ansible_connection=local
```


Below is the playbook with tasks to configure components, in below Calico CNI is used. Flannel, Canal, etc are other CNI that can be used.

Save below content to install.yaml 

```yaml
- name: Install Kubernetes
  hosts: kubernetes-nodes
  tasks:
  - name: Install kubeadm, kubelet, and kubectl
    become: true
    apt:
      name: ['kubelet','kubeadm','kubectl']
      state: present

  - name: Initialize Kubernetes cluster
    become: true
    command: kubeadm init --pod-network-cidr=10.244.0.0/16

  - name: Copy kubeconfig to user's home directory
    become: true
    copy:
      src: /etc/kubernetes/admin.conf
      dest: /home/user/.kube/config
      remote_src: yes

  - name: Install Calico CNI
    become: true
    command: kubectl apply -f https://docs.projectcalico.org/v3.14/manifests/calico.yaml
```

To deploy the playbook use below command

```
ansible-playbook -i inventory.ini install.yaml
```

----- 

Installing apps to the kuberentes 

Ansible playbook 

install python
install ansible 
install pip3 kuberentes package use below command 

# below only allows to install in venv. Instead install the python3-kubernetes directly using sudo apt since the ansible is also installed directly
```
pip3 install kuberentes
```

using anisble galaxy install the kuberentes package using below command 

```
ansible-galaxy collection install kubernetes.core community.crypto
```

-deploy.yaml with certificate generated 
```yaml
- hosts: localhost
  vars:
    cert_common_name: "kind-cluster"
    cert_org_name: "app-1"
    cert_days: 360
    cert_key_path: "tls.key"
    cert_crt_path: "tls.crt"
    cert_csr_path: "tls.csr"
  tasks:
   - name: deploy nginx server  
     kubernetes.core.k8s:
	   state: present
	   src: deployment.yaml
   - name: deploy service
     kubernetes.core.k8s:
	   state: present
	   src: service.yaml
   - name: Generate private key 
     community.crypto.openssl_privatekey:
	  path: "{{cert_key_path}}"
	  size: 2048
	  type: RSA
	  state: present
	
   - name: Generate CSR (certificate signing request)
     community.crypto.openssl_csr:
	   path: "{{ cert_csr_path }}"
	   privatekey_path: "{{ cert_key_path }}"
	   common_name: "{{ cert_common_name }}"
	   organization_name: "{{ organization_name }}"
	   select_crypto_backend: cryptography
	   state: present
   - name: Generate self-sign certificate 
     community.crypto.x509_certificate:
	   path: "{{ cert_crt_path }}"
	   privatekey_path: "{{ cert_key_path }}"
	   provider: selfsinged
	   csr_path: "{{ cert_csr_path }}"
	   selfsinged_not_before:  "+0s"
	   selfsinged_not_after: "+{{ cert_days }}d"
   - name: update crt and key file name secret
      ansible.bultin.template:
        src: tls-secret.yaml.j2
        dest: tls-secret.yaml
   - name: deploy tls secret 
      kubernetes.core.k8s:
	    state: present
		src: tls-secret.yaml
		
```

deploy.yaml ansible playbook just to deploy niginx and service

```yaml
- hosts: localhost
  tasks:
   - name: deploy nginx server  
     kuberentes.core.k8s:
	   state: present
	   src: deployment.yaml
   - name: deploy service
     kubernetes.core.k8s:
	   state: present
	   src: service.yaml

```

deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: default
  labels:
    app: nginx-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx-app
  template:
    metadata:
      labels:
        app: nginx-app
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "64Mi"
            cpu: "250m"
          limits:
            memory: "128Mi"
            cpu: "500m"
```

service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
  namespace: default
  labels:
    app: nginx-app
spec:
  type: ClusterIP
  selector:
    app: nginx-app
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
```

- tls-secret.yaml.j2
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: nginx-tls-secret
  namespace: default
type: kuberentes.io/tls
data:
  tls.crt: "{{ lookup('file', 'tls.crt') | b64encode }}"
  tls.key: "{{ lookup('file', 'tls.key') | b64encode }}"

```

To deploy to the cluster, the kubeconfig should be updated. The default path is used if not specified otherwise.
To override use the env kubeconfig environment variable.

```
ansible-playbook install.yaml

ansible-playbook uninstall.yaml
```