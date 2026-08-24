
### Installing kind version 

- Pass the latest version
  - Path: /mnt/c/path/ansible
```
ansible-playbook kind-install.yaml --tags kind-install -e "version=v0.32.0"
```

### Install certificate manager helm chart

```
ansible-playbook cert-manager-install.yaml
```


### Install the apisix cert issuer and certificate 

```
ansible-playbook apisix-install.yaml --tags=apisix-cert-issuer
```


### Install seaweedfs 

- 1. operator

```
ansible-playbook seaweedfs-install.yaml --tags=seaweedfs-operator
```

- 2. certs

```
ansible-playbook seaweedfs-install.yaml --tags=seaweedfs-certs
```

- 3. cluster

```
ansible-playbook seaweedfs-install.yaml --tags=seaweedfs-cluster
```

- 4. access

```
 kubectl -n seaweedfs apply  -f seaweedfs/files/seaweedfs/access/
```

install awscli

```
sudo apt-get install -y awscli
```


export AWS_ACCESS_KEY_ID=$(kubectl get -n seaweedfs secret admin-s3-secret -o go-template='{{index .data "accessKey" | base64decode}}')
export AWS_SECRET_ACCESS_KEY=$(kubectl get -n seaweedfs secret admin-s3-secret -o go-template='{{index .data "secretKey" | base64decode}}')
export AWS_EC2_METADATA_DISABLED=true
export AWS_ENDPOINT_URL="https://s3.swfs.com"


aws s3 --no-ssl-verify ls 


openssl s_client -connect s3.swfs.com:443 -showcerts </dev/null 2>/dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > cert.pem

openssl x509 -in cert.pem -noout -text

openssl x509 -in cert.pem -noout -text | egrep "Not|Issuer|Subject|DNS"

aws s3 --ca-bundle cert.pem ls

aws s3 --ca-bundle cert.pem ls
aws s3api --ca-bundle cert.pem list-buckets

#### create bucket 
aws s3 --ca-bundle cert.pem mb s3://test-bucket 