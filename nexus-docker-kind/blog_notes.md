## Docker nexus registry backed with SSL and using image in kind cluster

In this article have deployed nexus sonatype in a docker container and showed how to configure to access the image from the private repository.

The motivation for this is an use case where had to copy container images from one artifactory to another. This setup we could help in development activities.
This also provides option to use python or go program to automate the process of download and upload the images between repository.

To secure the Nexus Sonatype we use ngnix proxy and configure an certificate with SAN included, the SSL traffic terminates in the nginx proxy.

Pre-requisites:
  - Docker daemon installed in WSL2
  - Kind CLI
  - Git Bash for openssl to generate certificate

Deploy the nexus and nginx using docker compose, below is the docker compose file

```yaml
services:
  nexus:
    image: "sonatype/nexus3"
    volumes:
      - "nexus-data:/nexus-data"
    ports:
     - 8081:8081
    restart: always
    networks:
      - nginx_network
  proxy:
    image: "nginx:alpine"
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /var/run/docker.sock:/tmp/docker.sock:ro
      - ./config/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./config/ssl/:/etc/nginx/ssl
    restart: always
    networks:
      - nginx_network
volumes:
  nexus-data:

networks:
  nginx_network:
    driver: bridge
```


- Save the above content in a file named docker-compose.yaml.

- Create folder `config/ssl/` and generate the certificate using below command 

- When testing the configuration noticed there was certificate error to use SAN instead of legacy common name in certificate. 
Below is the content for SAN configuration, save it as nexus.local.conf

```
[req]
default_bits       = 2048
prompt             = no
default_md         = sha256
req_extensions     = req_ext
distinguished_name = dn

[dn]
CN = nexus.local

[req_ext]
subjectAltName = @alt_names

[alt_names]
DNS.1 = nexus.local

[ext]
subjectAltName = @alt_names
```

- Using the git bash navigate to the base path where the docker compose and config folder created. Execute below command
The command will create server.crt and server.key. This will be mounted to the nginx proxy in docker compose

```sh
openssl req -x509 -nodes -days 365 \
 -newkey rsa:2048 \
 -keyout config/ssl/server.key \
 -out config/ssl/server.crt \
 -config nexus.local.cnf \
 -extensions ext
```

Add the certificate to the truststore of the docker daemon and restart the docker process 

```sh
sudo mkdir -p /etc/docker/certs.d/nexus.local
sudo cp .config/ssl/server.crt /etc/docker/certs.d/nexus.local/

sudo systemctl restart docker
```

The nginx.conf is below which is also mounted in the docker-compose file

- The `proxy_pass http://nexus:8081` is the backend nexus service in this case. The service name could be found in the docker compose file.
- The `server_name nexus.local` is the URL used from the browser. Also, when using windows host, update the hosts file `127.0.0.1 nexus.local`

```
worker_processes 1;
error_log stderr notice;
events {
  worker_connections 1024;
}

http {

    # Unlimit large file uploads to avoid "413 Request Entity Too Large" error
    client_max_body_size 0;
    variables_hash_max_size 1024;
	log_format main '$remote_addr - $remote_user [%time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';
    access_log off;
    real_ip_header X-Real-IP;
    charset utf-8;
	  
	server {
		listen 443 ssl;
		listen [::]:443 ssl;
		server_name nexus.local;

		ssl_certificate /etc/nginx/ssl/server.crt;
		ssl_certificate_key /etc/nginx/ssl/server.key;
		
		ssl_prefer_server_ciphers on;

		location / {
			proxy_pass http://nexus:8081;
			proxy_set_header Host $host;
			proxy_set_header X-Real-IP $remote_addr;
			proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
			proxy_set_header X-Forwarded-Proto $scheme;
		}
	}

	server {
		listen 80;
		server_name nexus.local;
		return 301 https://$host$request_uri;
	}
}
```

- With the above configuration in place, run the docker container using 

```sh
docker compose up
```

- From browser access `https://nexus.local` update the hosts file with loopback ip to dns. The screen prompts for user name and password. Username is admin by default, for inital password use below steps. 

```
## from the wsl use below command to find the path of the volume that is mounted

docker volume inspect ssl-nexus_nexus-data

## list the mount path to find the admin.password file command looks like below 
sudo ls -lrt /var/lib/docker/volumes/ssl-nexus_nexus-data/_data

sudo cat /var/lib/docker/volumes/ssl-nexus_nexus-data/_data/admin.password
```

After login, navigate to the below screen as in snapshot
   - click on the person icon
   - Then, click the Settings option
   - Then, click on Repositories
   - Then, click on Create Repositories
   - Then, click on the docker (hosted)
       
<img width="2872" height="1282" alt="image" src="https://github.com/user-attachments/assets/4e70fc69-c20e-4202-a0c6-fbd9a3a87c2d" />

 - After selecting the docker (hosted) should see the screen like below, provide a name my case i used `my-docker`, which is the repo name.

<img width="2566" height="1393" alt="image" src="https://github.com/user-attachments/assets/167b16a5-7020-4cd4-b4e6-458fbb87cfe2" />

- After creation should see the repo listed in the browse screen

<img width="2879" height="1171" alt="image" src="https://github.com/user-attachments/assets/5378ac52-027c-41e1-b216-a3aaafbfc1be" />

Thats all is needed to set up the repo.

Lets login to the the repo from the docker, use below command to login to the repo when prompted provide the admin user and password updated earlier.

```
docker login nexus.local
```

To check if the repo is working we can push an image to the docker repo.

```
docker pull nginx:alpine

docker tag busybox nexus.local/my-docker/busybox:02082026

docker push nexus.local/my-docker/busybox:02082026
```

<img width="2493" height="742" alt="image" src="https://github.com/user-attachments/assets/e10633c2-5e82-44a1-bdcf-e90111db0d4b" />

Once the docker images is pushed we could see the manifest in nexus ui

<img width="1273" height="975" alt="image" src="https://github.com/user-attachments/assets/79bbd130-8a77-489a-9913-bccff98c7a03" />


We can also access the docker repo from an container running in docker, we mount the docker.sock unix socket to the docker container

```
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock docker:latest sh
```

- screen shot of how the command execution within the docker container looks
<img width="2854" height="720" alt="image" src="https://github.com/user-attachments/assets/078bb1d4-7e9b-4355-8ffc-4b5eb2f5eb80" />

Additionally, we can access the version details of the docker using socket in curl command like below
- Note, the nexus is the docker service which is being accessed by the socket

```sh
curl --unix-socket /var/run/docker.sock http://nexus/images/json | jq .
```

- Configuring the kind cluster with the configuration of containerd with registry.

To create the kind cluster to access the private SSL registry, we use below configuration 
  - The configuration updates the containerd configuration
  - Mount the certificates from `/etc/docker/certs.d/nexus.local/server.crt` which was added as truststore earlier
  - The `hosts.toml` and the `server.crt` file is also placed under `containerd/` folder and mounted to `/etc/containerd/certs.d/nexus.local`
  - The `config.json` is the same file created with auth when issuing `docker login nexus.local` under the `$HOME/.docker/config.json`.

    hosts.toml file content
    ```toml
    ---
    [host."https://nexus.local:443/v2/my-docker"]
      capabilities = ["pull", "resolve"]
      skip_verify = true
      override_path = true
    ---
    ```

```yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: private-repo
nodes:
- role: control-plane
  # This option mounts the host docker registry folder into
  # the control-plane node, allowing containerd to access them. 
  extraMounts:
    - containerPath: /etc/docker/certs.d/nexus.local
      hostPath: /etc/docker/certs.d/nexus.local
    - containerPath: /var/lib/kubelet/config.json
      hostPath: /mnt/c/thiru/edu/tmp/ssl-nexus/config.json
    - containerPath: /etc/containerd/certs.d/nexus.local/
      hostPath: /mnt/c/thiru/edu/tmp/ssl-nexus/containerd/
containerdConfigPatches:
- |-
  [plugins."io.containerd.grpc.v1.cri".registry]
    config_path = "/etc/containerd/certs.d"
```

- Save the configutation to afile as `kind-private-repo.yaml`, then use below command to create the cluster

```sh
kind create cluster --config kind-private-repo.yaml
```

Note, if the containerd configuration is not updated, the image from the private repository couldn't be pulled by the kind cluster. Since we have updated the certs and containerd config we don't need to create secrets like mentioned below.
Additionaly, if the artifactory is deployed in an seperate VM then we can create secret with the docker config.json and use `imagePullsecrets` config in pod manifest to pull image. Refer [kuberentes.io documentation](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/)

- The above command will create a kind cluster, now we need to update the docker container `/etc/hosts` file with the hosts value.
- Use the `docker network inspect bridge` and find the gateway for ssl-nexus-nexus-1 container and update that ip address below

```
docker exec -it private-repo-control-plane sh

echo "172.17.0.3 nexus.local" >> /etc/hosts
```

Note, the above host update for the kind cluster to pull the image but this might not be needed which I am not concrete about. No need to restart the container after updating this hosts.

- Once the ip and dns update, try to use `curl -kiv https://nexus.local` to see if 200 response is received.

With the above updates, now we can create the manfiest with the private registry, like below

```yaml
# file-name: test-pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-reg
spec:
  restartPolicy: Never
  # below is not needed it will work without this change
  hostAliases:
  - ip: "172.17.0.3"
    hostnames:
    - "nexus.local"
  containers:
  - name: private-reg-container
    image: nexus.local/my-docker/nginx:alpine
    command: ["/bin/sh", "-c", "sleep 15m"]
```

- To deploy the pod, use `kubectl apply -f test-pod.yaml`, this will deploy the pod uses the image from private directory.

Describe the pod we could see the image pulled successfully.

<img width="2806" height="293" alt="image" src="https://github.com/user-attachments/assets/5f7ba3ac-5043-441a-af41-7c9469cb6281" />


- Command to create the pod directly is listed below
 
```sh
kubectl run test --image=nexus.local/my-docker/busybox:020826 -- sleep 3600
```

```
...
Events:
  Type    Reason     Age   From               Message
  ----    ------     ----  ----               -------
  Normal  Scheduled  15s   default-scheduler  Successfully assigned default/test to private-repo-control-plane
  Normal  Pulled     14s   kubelet            Container image "nexus.local/my-docker/busybox:020826" already present on machine and can be accessed by the pod
  Normal  Created    14s   kubelet            Container created
  Normal  Started    14s   kubelet            Container started
```
