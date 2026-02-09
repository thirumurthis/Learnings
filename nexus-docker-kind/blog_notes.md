## Docker nexus registry backed with SSL and using image in kind cluster

In this article have deployed nexus sonatype in a docker container and showed how to configure to access the image from the private repository.

The motivation for this is was wanted to copy container images from one artifactory to another. This blog would help in development activities.
We can also automate the process of copying container using use python or go program to download and upload the images between repository.

The nexus sonatype is deployed and secured behind ngnix proxy with SSL termination at the proxy. The SSL certificate includes SAN instead of Comman name.

Pre-requisites:
  - Docker daemon installed in WSL2
  - Kind CLI
  - Git Bash for openssl for certificate generation

Summary:
 - The nexus and nginx container are deployed using docker compose
 - The Kind Cluster is updated with the private repo config in containerd so the image could be pulled from the private repo.

The docker compose file content is shown below and save the content to file docker-compose.yaml.

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

Create folder `config/ssl/` and generate the certificate using below command. During testing encountered certificate error so had to include SAN (subject alternative name) in certificate instead of legacy CN (common name) added in certificate.

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

- In Git Bash navigate to the path were the docker compose file exists and create `config` folder. Follow below steps, the command will create `server.crt` and `server.key`. These files will be mounted to the nginx proxy configured in docker compose.

```sh
openssl req -x509 -nodes -days 365 \
 -newkey rsa:2048 \
 -keyout config/ssl/server.key \
 -out config/ssl/server.crt \
 -config nexus.local.cnf \
 -extensions ext
```

The certificate needs to be added to docker truststore, below commadn will add the certificate to docker, and restart the docker. 

```sh
sudo mkdir -p /etc/docker/certs.d/nexus.local

sudo cp .config/ssl/server.crt /etc/docker/certs.d/nexus.local/

sudo systemctl restart docker
```

Below is the content of `nginx.conf` which is also mounted via docker-compose file, place the file in the `config` folder.

- Note:
  - The `proxy_pass http://nexus:8081`, the `nexus` is the backend service name defined in the docker compose file.
  - The `server_name nexus.local` includes dns name choosed on the certificate. This will be used in the browser to access.
  - Add the IP address and dns in the hosts file like this `127.0.0.1 nexus.local`


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

With the above configuration in place, run the docker compose container using below command, use `-d` option to run in detached mode.

```sh
docker compose up
```

From browser access `https://nexus.local`. Make sure to update the hosts file. 
The nexus application prompts for user name and password. `admin` is the default user, for inital password use below steps. 

```
## from the wsl use below command to find the path of the volume that is mounted

docker volume inspect ssl-nexus_nexus-data

## list the mount path to find the admin.password file command looks like below 
sudo ls -lrt /var/lib/docker/volumes/ssl-nexus_nexus-data/_data

sudo cat /var/lib/docker/volumes/ssl-nexus_nexus-data/_data/admin.password
```

To create a new repo login to nexus admin account, follow below step
   - Click on the user icon
   - Click the Settings option
   - Click on Repositories
   - Click on Create Repositories
   - Click on the `docker (hosted)` option

Below screenshot shows the screen after login to nexus application

<img width="2872" height="1282" alt="image" src="https://github.com/user-attachments/assets/4e70fc69-c20e-4202-a0c6-fbd9a3a87c2d" />

Selecting the docker (hosted) as seen in the screenshot below. Provide a repo name in my case used `my-docker`.

<img width="2566" height="1393" alt="image" src="https://github.com/user-attachments/assets/167b16a5-7020-4cd4-b4e6-458fbb87cfe2" />

After repo creation selecting the browse option should list the created repo

<img width="2879" height="1171" alt="image" src="https://github.com/user-attachments/assets/5378ac52-027c-41e1-b216-a3aaafbfc1be" />

With the repo now available use docker to login. The command looks like below when prompted provide the admin user and password created earlier.

```sh
docker login nexus.local
```

With below commands we can pull the image from docker.io and push to the private nexus registry.

```sh
docker pull nginx:alpine

docker tag busybox nexus.local/my-docker/busybox:02082026

docker push nexus.local/my-docker/busybox:02082026
```

<img width="2493" height="742" alt="image" src="https://github.com/user-attachments/assets/e10633c2-5e82-44a1-bdcf-e90111db0d4b" />

The nexus UI after publishing the docker images looks like below

<img width="1273" height="975" alt="image" src="https://github.com/user-attachments/assets/79bbd130-8a77-489a-9913-bccff98c7a03" />


We can mount the local docker socket to the docker container with docker CLI and pull and push the image to the private repository. The command looks like below.

```
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock docker:latest sh
```

Below screenshot shows the command executed in WSL2 within the docker container

<img width="2854" height="720" alt="image" src="https://github.com/user-attachments/assets/078bb1d4-7e9b-4355-8ffc-4b5eb2f5eb80" />

curl provides option to use unix socket and we can use it to fetch the list of images from the nexus registry the command is shown below.

```sh
curl -s --unix-socket /var/run/docker.sock http://nexus/images/json | jq .[].RepoTags
```

<img width="2208" height="803" alt="image" src="https://github.com/user-attachments/assets/1a0a06bb-ce41-467d-b46f-78cc50fd9c43" />


### Deploy kind cluster and use the image from private registry

The kind cluster configuration includes the containerd config with private registry and we create a pod in cluster to fetch image from private registry.

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

Save above configutation to afile as `kind-private-repo.yaml`, then use below command to create the cluster in docker daemon

```sh
kind create cluster --config kind-private-repo.yaml
```

Note, if the containerd configuration is not updated, kind cluster would not pull the image from the private repository.  Since the hosts and certs updated for containerd config we don't need to create secrets like mentioned below. Additionaly, if the artifactory is deployed in an seperate VM then we can create secret with the docker config.json and use `imagePullsecrets` config in pod manifest to pull image. Refer [kuberentes.io documentation](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/)

- Once the kind cluster is created we need to exec to the kind docker container and update the `/etc/hosts` file with the hosts value.
- Use `docker network inspect bridge` to find the gateway IP address for ssl-nexus-nexus-1 container in this case and update that ip address below
- With the host file updated in the container with below command, No need to restart the docker kind container.

```sh
docker exec -it private-repo-control-plane sh

echo "172.17.0.3 nexus.local" >> /etc/hosts
```

Once kind docker container host is updated kind cluster will be able to pull the image without any exception. This step might not be necessary, but not tested after updating continared configuratrion.  No need to restart the container after updating this hosts.

From the kind docker container once the host is updated try `curl -kiv https://nexus.local` and should see 200 response.

The pod manifest uses the image from the private registry and deploy to the kind cluster.

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

To deploy the pod manifest use the command `kubectl apply -f test-pod.yaml`. The status of the of the pod should be Running, describe to see the events.

Below is the screenshot of the described pod we could see the image pulled successfully.

<img width="2806" height="293" alt="image" src="https://github.com/user-attachments/assets/5f7ba3ac-5043-441a-af41-7c9469cb6281" />


Command to create the pod directly is listed below
 
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
