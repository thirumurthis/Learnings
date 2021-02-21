### how to run a docker container
```
$ docker run <image-name>
### image name can be obtained from the hub.docker.com
### in case of the personal image, uploaded use the user name. 

### official repos mostly don't have the user name info.

$ docker run ngnix
```

### Command to list containers, `-a` to check the status of the container
```
$ docker ps

$ docker ps -a

## all running and exited container (if removed it will not be displayed)
```

### Stop the container
```
$ docker stop <container-name or Container-id>
```

### remove container permenantly
```
$ docker rm <container-name or container-id(few sha char is sufficient until it is unique)>
```

### To see the list of available images and size
```
$ docker images
```

### To remove the inmage from the docker
```
$ docker rmi <image-name>
## The docker container shouldn't be running make sure the cotainer is not running.
```

### To only pull and not execute/run the container.
```
$ docker pull <image-name>
```

## Why the container runs and exists immediately.
 - For example when using `docker run ubuntu` the image is downloaded and executed.
 - Immediately the container will be exited, use comand `docker ps -a` to check.

The above happens, because the docker container is not the actual OS itself.

The container is not meant to host the OS, it is to meant to perform a task.

Once the task or process is completed the container will exist.

This is the reason that when the ubuntu image (the base image) doesn't have any process so it exists.

##### To make the ubuntu image to run and exist after some time.

Instruct ubuntu in this case to execute a process
```
$ docker run ubuntu sleep 10
## the container will run and slepp for 10 seconds in this case.
```

### Execute a command on a running container:
```
$ docker exec <container-name> cat /etc/hosts

$ docker exec <container-name> ls -lrt
## in this case the container-name is the ubuntu container name provided by the docker 
## use docker ps -a to list the container name.
```

### Running docker in attach and detach mode.
```
$ docker run somewebapp
### the above command will run the containr in fore ground.

### to execute the docker container in detach mode.
$ docker run -d somewebapp
## in this case the docker container will run in background
```

### There is another option, to attach the running container back
```
$ docker attach <container-id, just few chars of sha>
## this command will attach the already running container 
```

### docker to run an ubuntu/centos image and prompt the bash automatically login to it.
```
$ docker run -it ubuntu bash

## option -it allows to display the interactive terminal.
## this code will logs directly into container.
```

### Exit code in `docker ps -a`
  - when force fully stopping a container `docker container stop <container-name>` then the exit code will be non-zero.
  
### Removing the container. (reclaims disk space)
```
 $ docker rm <container-name or container-id>
 
 ## to remove multiple container at same time.
 $ docker rm <container-id1> <container-id2> ...
```

- `busybox` is a light weight small image to play.

### Docker images can be removed using `docker rmi <image-id>`.
  - Note, if the container running or exited, the `rmi` command will not allow to delete the image.
  

### How to view the container logs in docker?
```
$ docker container logs <container-id/name>
```

### To login to the docker container using `exec`
```
$ docker exec -it <container-id>

or 

$d docker exec -it <cotnainer-id> /bin/bash
```

-----------
### Manually building the docker container
 - use `docker run -it alpine` to modify the alpine image.
 - the above command will started the thin linux box, and pormpts for user action
 - Create a simple nodejs code ui appliation.
 - Dowload the nodejs app `apk add nodejs` once downloaded.
 - Verify if the application is executing successfully by `node app.js`.
 - exit out of the container (cntrl +c).
 
 - get the container id of existing alpine container to see the difference.
 -  `$ docker container diff <container-id>` changes will be listed.
 - Now we need to create the new image since we made changes to the base with the nodejs app.
 - __How to create the image with the modified container?__
   - ` $ docker container commit <container-id>`
   - Giving a name to container, just tag it using below command.
   - `$ docker image tag <image-id> <tag-name-from-user(username/appname:version)>`
   - `$ docker image ls -a` lists all the local images.
- Upload the image to the dockerhub.
- ` docker push <image-name (username/application:version)>`
   
Note: when the images are pushed only the layer will be pushed to the hub. Since we made changes only to the alpine base, over it added nodejs app.

---------

#### How to build image using `Dockerfile`?
 - at the level where the application code exists (in this case the nodejs application) create the Dockerfile
 - Sample docker file looks like below,
 ```
 FROM alpine:latest
 RUN apk update & apk add nodejs
 RUN mkdir -p /usr/src/app
 WORKDIR /usr/src/app
 COPY ./app.js /usr/src/app
 EXPOSE 80
 CMD ["node","app.js"]
 ```
 - to build the image, use `docker image buid -t app-name:v1.1 .` (. - path of the dockerfile.)

----------

### Stateless application:
Previously, we looked at the stateless application, which doesn't have any data storage requirements.

### Persistent storage:
- Containers have `Ephemmeral Storage`
- So when the container dies, the data within the container is lost.
- One way is to make the container data persisted is to mount peristent storage volumes from the host machine to the container. 
  - Doing this will persist the data even when the container dies.
  - Since storage volume is external to the container, it is persisted. 
  - The storage in the host machine can be `directly connected storage volume `, `network storage volume` or `cloud storage volume`. 
  
### How to create and configure a persistent storage?  
##### check if there are docker volumes available?
`$ docker volume ls`

##### Create a persistent storage volume in host machine:
`$ docker volume create mysql-data0`

##### Mount this `mysql-data0` volume to say mysql data container.
`$ docker run --name demo-sql -v mysql-data0:/bar/lib/mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:latest`
   - -v => maps the data to volume
   - -e => set environment properties
 
 Note: Above command will download the image mysql and runs it.

##### once the container is started, then use `dokcer exec -it demo-sql /bin/bash`, and in prompt navigate to `cd /var/lib/mysql`. 
   - in the host machine inspect the volume
   ` $ dokcer volume inspect mysql-data0` => this will list the mountpoint of the data in host machine.
   - from the host machine list the info from the mountpoint path.
   
##### We can also create a directory and map that to volume.
  - in the same docker command with -v use the direcrory path /home/user/<dir-to-mount>
 
----------

### `Docker Compose` 
 - tool for defining an running multi-container docker application.
 - a yaml file used to define the applicaton services.
 
 - once the Dockerfile is defined for example say frontend, backend.
 - then create the `docker-compose.yml` file, so this can run the service in isolated environment.
 - finally use `dokcer-compose up` command to start and run entire app.
 
To perform the docker-compose to execute, install the docker-compose.

Sample docker compose file:
```
versrion: '1'
services:
   web:
     build: .
     ports:
      - "5050:5000"
   redis:
     image: "redis:alpine"
```
- above docker compose will set the web and redis backend instance. 


### What are container? 
  - Containers are set of processes running in virutal  isolated sandbox environment.
  - The isolated environment contains (below virtual of its own)
    - filesystem
    - process if
    - network interfaces
    - devices 
    - CPU
    - memory

 #### How linux OS creates this virtual environments?
  - Linux has virtualization (Linux Kernel) primitives, such as 
     - `namespaces`
     - `CGroups`
   - Linux also has Virtualization tools and drivers
     - `libcontainer`
     - `libvirt`
  - Docker containers are wrapper written over this linux tools and primitives.
 
 ##### What is namespace? 
  - analogus to the Class in java, the attributes are available within that class only.
  - Linux has different namespaces:
    - filesystem namespace (rootfs)
    - Process Namespace (procfs)
    - Network Namespace (netns)
    - Device/Mount Namespace
    - User Namespace
 
 - When there are multple container, the each container has its own namespace.
 - Also Processes running on one namespce cannot access the process running on another namespace.
 
#### Filesystem Namespace:
  - Filesystem is a hierarichal and starts with root.
  - To create a new filesystem namespace, download the root of tar ball in host machine. Paste the tar ball to any first tree of directory.
  - Then use the `chroot` command where the tarball file is located.
  
 Simple Demo of filesystem namespace: 
-  Download the filesystem from [link](https://alpinelinux.org/downloads/) mini root file system. 
 - Generally this filesystem namespace doesn't contain any kernel info.
 
 - In the linux host, 
   - create a Directory "fsnroot" and navigate to it
   - download the `mini root filesystem` and untar it
   - chroot `change root` to newely created directory. (this will change the root file system to a new root file system). 
   - execute process within the new file syste.
   - 
 ```
  $ mkdir fsnroot   ### Create a directory
  $ cd fsnroot
  $ wget <location-to-download-the-tarball>  ## for example using x86 online linux terminal.
  $ sudo tar xvf <tar.gz> 
  
  ### command to change the existing new root file system
  $ sudo chroot fsnroot /bin/bash   #### {chroot <directory> <command-to-execute-after-creation>}
  
  ### once within the new files system (within the new root file system execute 
  $ whoami
  $ pwd
  $ ps 
  $ls 
  
  #### To exit from the new root file system, type exit
  $ exit
  
  #### To change to the new root directory again, use the chroot command.
  $ chroot fsnroot /bin/bash
  ```
  - Try on online linux terminal, use [`jslinux`](https://bellard.org/jslinux/vm.html)
 ```
  $ mkdir fsnroot
  $ cd fsnroot
  $ wget https://dl-cdn.alpinelinux.org/alpine/v3.13/releases/x86/alpine-minirootfs-3.13.2-x86.tar.gz
  $ zcat alpine-minirootfs-3.13.2-x86.tar.gz | tar -xvf -   ## since this box, the tar didn't had the gzip utility, so using this command
  $ chroot fsnroot /bin/bash
  $ ps 
 ```
   -  Executing `tar -xvf` displayed `tar : invalid magic tar` message. This is because the tar file is gzipped and box didn't had added gzip with tar utility.


#### Process Namespace
  - isolated environment, for a group of process to run without intereption by other processess.
  - process running under one namespace is not even aware of process running on the other namespace.
  - process are numbered 1 to n, running on one namespace. When a process moved from one container to another the process id doesn't change since each container has its own namespace and numbered 1 to n.
  - each process in linux are represented as files in linux. (its easy for ps command to read these files for status.)
  - The process filesystem is mounted on /proc directory in linux. The ps command reads the files mounted on the /proc directory.
  - To create a separate process namespace use command,
  - ` unshare --fork --pid /bin/bash `-> this command will fork a child process and runs it in its own process namespace.
  - ` unshare -fp /bin/bash` - same option as above.
  - each process namespace in this case gets it own process filesystem.
  - to read and display the processes inside the process namespace it should be mounted on the `/proc` directory.
  - after the process is started, it can be mounted.
  ` mount -t proc proc /proc`
```
$ sudo unshare -fp /bin/bash

# navigate to the thin file system fnsroot and chroot it
$ sudo chroot fnsroot /bin/sh
 / # mount -t proc proc /proc
 / # ps -ef 
 ## lists the specific process
 ## the /bin/bash would be allocated with the pid 1
```
 
 Now if this process is moved to different container it will carry the process id value too.

 #### Network Namespace (netns)
  - Each network namespace, has its own set of interfaces, ip addresses, tcp, upd port address etc.
  ```
  # to create a new network namespace
  $ ip add netns <name>
  
  # to access and execute the created namespace
  $ ip netns exec <name> <command>
  
  
  # like the process that can be access at /proc directory 
  ## the newly created netns can also accessed or seen at directory
  $ cd /var/run/netns 
  ```
  
### how to create a network name space and add an veth0 ethernet interface.
```
$ sudo ip netns add demonetns
$ sudo ip netns exec demonetns /bin/bash
# create a new virtual interface
$ sudo ip link add type veth
# to start up the virtual interface veth, use below command.
$ sudo ifconfig veth0 up
## verify using ifconfig

```
Alternate command to perform the network namespace creation
```
$ nsenter --net=/var/run/netns/demonetns /bin/bash
```

##### How to execute namespaces in single command.
 - create `fnsroot`, `procfs`, `netns`
 ```
 $ sudo nsenter --net=/var/netns/demonetns \
 > unshare -fp --mount-proc=fnsroot/proc \
 > chroot fnsroot/ /bin/sh
 ```
  
 #### This is basically the magic of Linux kernel, where the namespace provides a way to create a container. So docker is the wrapper over the namespaces info.
 
 
