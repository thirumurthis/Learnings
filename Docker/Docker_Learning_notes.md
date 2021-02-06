#### Download an image and run it in docker.
  - images are in dockerhub.
  
```
> docker pull nginx

## exploring the nginx image in the docker hub displays the command
```

#### The nginx image is downloaded, run the image.
```
> docker run nginx:latest

## To run in detach mode
> docker run nginx -d

```

#### To see if the docker container running use below command
```
> docker container ls 

> docker ps 

## To list all the process use -a
> docker ps -a
```

#### How to stop the running container
```
> docker stop <container-id-hash-or-name>
```

#### From the host we need to issue a request, that is from laptop open browser and invoke the url
  - to use 8080 from host to use 80 on the container
```
                                 -----------------
                                |                 |
    localhost:8080  ---->    | 80 |   Nginx       |
                                |   container     |
                                 -----------------
```                                 
   
```
 ## host 8080 to container 80
 
> docker run -d -p 8080:80 nginx
## NOTE: the image name should be at the end.
```

#### To map more than one port, say 3000 and 8000 to 80 

```
> docker run -d -p 3000:80 -p 8080:80 nginx

5ee8c3706af   nginx     "/docker-entrypoint.…"   45 seconds ago   Up 42 seconds   0.0.0.0:3000->80/tcp, 0.0.0.0:8080->80/tcp   pedantic_cerf
```

#### how to remove the container 
```
> docker rm <container-name-or-id>

## docker ps to list all the hash ids quitely

> docker ps -aq 

### To remove all the containers use

> docker rm $(docker ps -aq)
```

#### To start the SAME container (that is not running)

```
> docker start <name_of_the_container_that_is_not_running>
```

#### How to provide a `name` to a container. using --name
```
> docker run --name website -d -p8080:80 nginx

### as best practice try to name the container
```

#### How to format the output of the docker command
```
> docker ps -a --format="ID\t{{.ID}}\nName{{.Names}}"

### One way to use the format always is to export to an environment variable and use it
### in Linux
> export FORMAT="Info:\t{{.ID}}\t{{.Names}}"
> docker ps -a --format=$FORMAT
```

### docker Volumes 
  - allows share data, Files & Folder 
     - between host and container
     - between containers

```
                         ________________________
                        |     1. Create vloume   |
                        |                        |
                     |  80 |                     |
                        |                        |
                        |       container        |
     Host               |________________________|
                        
```

- Create a volume in side the container.
  - so any file created in the host will also be available in container.
  - also when a file is creatd in the container volume it will also be available in host

- check the docker hub for nginx documentation to setup the static content in display

- Step 1: Inside the host machine (laptop) create a folder, and place index.html with html content.
- Step 2: Navigate to the folder where the index.html is preser
- Step 3: issue command

```
> docker run --name demoweb -v <path_of_the_folder>:<path_within_the_container>:ro -d -p 8080:80 nginx 

> docker run --name demoweb -v C:/thiru/docker/volume:/usr/share/nginx/html:ro -d -p 8080:80 nginx

### localhost:8080, will yeild the index.html content from host machine.
```
 - Note: now if we edit the index.html content in the laptop, it will be reflected on the localhost:8080 path.

#### To execute command within the running nginx container use below command
```
> docker exec -it demoweb bash
```
- with reference to the volume the index file in the container is a readonly since the volume is readonly.

##### to host a sample website in the nginx.
  - search for bootstrap template, and pick one from the git. 
  - once cloned to the local, move the files to the volume path in the host.

### To share volumes between containers:
 - to share the volumes between container, we can use `volumefrom` command.
 - check `docker run --help`

 - already we have a vloume mounted, we will create another container
 ```
  > docker run --name demoweb2 --volumes-from demoweb -d -p 8081:80 nginx
 ```
  - Now hitting the `http:localhost:8081` will fetch the index.html from the host and display part of second container running
  

### Dockerfile
  - used to create our image with the steps

 ```
 FROM nginx:latest
 ADD . /usr/share/nginx/html  // copy all the content from the current folder to the container path
 ```
### to build the image
```
### navigate to where the dockerfile is present

> docker build --tag demoweb1:latest .
```


 
