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
 ADD . /usr/share/nginx/html  // copy all the content from the current folder to the container path: in our case the index.html
 ```
### to build the image
```
### navigate to where the dockerfile is present

> docker build --tag demoweb1:latest .
```

##### With `node.js`  and `Expressjs` we can easily build website here.
 - Install the node.js and then follow the Expressjs to install using npm install

 - Create a new folder 
 - Navigate to it
 - issue `npm init` (input values when prompted)
 - issue `npm install express --save`
 - create a index.js file, as below.
 ```
 const express= require('express');
 const app = express();
 const port= 8000
 
// app.get('/',(req,res) => res.send("Node application"));
 app.get('/',(req,res) => res.json([{"name":"tim"}]);
 app.listen(port, () => console.log(`Server up in port ${port}`);
 ```
  - in command prompt, issue `node index.js` to start the server code.
  
#### how to create image with the above express code and run it.
 - Create a `Dockerfile` within the folder where index.js exists, content as below
```
FROM node:latest

WORKDIR /app    ## To create a working directory in the CONTAINER
                ## if the app dir exists in container use it else create a new one
                ##  Also any command folloing this command will be executed in this dir
                
ADD . .         ## The content in current host dir will be copied to the container /app
                ## since workdir is app
RUN npm install
CMD node index.js
```
 - **To build the image, issue **
 ```
 > docker build --tag user-service-api:latest .
 or
 > docker build -t user-service-api:latest .
 ```
- **Run a container with the image created **
```
> docker run --name user-api -d -p 8000:8000 user-service-api:image
## user-serivice-api is the image check using docker image ls
```

##### How to ingnore the folder that is not required? use `.dockerignore`
```
### in .dockerignore file content
node_modules
Dockerfile
```

##### The docker build with the Dockerfile can be improvised.
  - with the above Dockerfile, the performance is slow, since everytime there is change in the code, the npm install is executed.
  - we can take the advantage of caching
  
  ```
  FROM node:latest
  WORKDIR /app
  ADD package*.json ./  ### When there is a source code change we don't change the
                        ### package.json, only the index.js. the package.json gets 
                        ### updated only when new js library is added
  RUN npm install       ### now npm install will use package.json, as no change it will
                        ### fetch from cache
  ADD . .               ### copy the content from local to container workdir
  CMD node index.js
  ```
   - Above Dockerfile will run fast on consecutive executions.
   
 #### How to improve the docker image size
  - use linux apline distribution, refer the dockerhub documentation.
 
#### Tags and versioning:
   - allow to control image version.
   - any new changes can be pushed to new version of image.
   - in the Dockerfile, mostly don't use latest since the repo will update without the user knowledge. use the tag version within the Dockerfile.
   
##### Issuing the `docker build -t <image-name>:latest` again and again, will keep only one latest version of image. check using `docker image ls`
  - using, `docker build -t demoweb:v1` -> this will create a two images tag `v1` and `latest`. latest will point to v1 content here.
  - using, `docker build -t demoweb:v2` -> after execution, `docker image ls` will list three images tagged, v1, v2 and latest. Where latest points v2 content in this case.
  
  - To run specific version `docker run --name web-v1 -d -p 8080:80 demoweb:v1`
  - To run specific version `docker run --name web-v2 -d -p 8080:80 demoweb:v2`
  - To run specific version `docker run --name web -d -p 8080:80 demoweb` => this will be latest.
  
##### Docker registry (docker hub)
   - public 
   - private
Different docker registry prividers
    - docker hub
    - quay.io
    - Amazon ECR
    
- To ship the images built in local to docker registry, use below command.
  - In dockerhub, create a repository and we can push images to that repo within dockerhub
  
```
 > docker push <account-in-dockerhub>/<repository-name>:tagname
  ## if we create a repo mywebsite
  > docker push tim/mywebsite:tagname
```
#### If we need to push our local image
 - first tag the image in local to that of the repository

```
## first login in the terminal
> docker login

## add a tag for our image tag with repo tag
> docker tag demoweb:v1 tim/mywebsite:v1  => this will add a new tag

> docker tag demoweb:v2 tim/mywebsite:v2 

> docker tag demoweb:latest tim/mywebsite:latest 

## use docker image ls 
## now push this image to docker

> docker push tim/mywebsite:v1

> docker push tim/mywebsite:v2

> docker push tim/mywebsite:latest
```

##### Search in the docker hub for username and repo to pull it and use it

```
> docker pull tim/mywebsite:latest

### to validate remove the image and pull, then run it

### to run the image
> docker run --name demoweb -p 8000:80 -d tim/mywebsite
```

#### To debug the container or inspect it
```
> docker inspect <container-id/name>

### provides more info in json format abt container
```

##### How to view the logs in the container
```
> docker logs <container-id/name>

### to follow the logs, as running stream

> docker logs -f <container-id/name>
```

#### Check the content within the container using `docker exec`
```
> docker exec -it <container-name/id> /bin/bash
  - i => interactive
  - t => tty or terminal
  - /bin/bash => the command exected

## note if the /bin/bash is not working, use docker inspect to see what is the command in some case it would be /bin/sh.
```

------------------------

### Docker postgres image and connecting to it using spring-boot application
 - find the postgres image from dockerhub (use alpine for lesser size)
 - also check the docker hub documentation on how to start the instance.
 
```
> docker pull postgres:13.1-alpine
```
#### to spin up the postgres instance
```
> docker run --name staginddb -e POSTGRES_PASSWORD=admin -d -p 5432:5432 postgres:13.1-alpine
```
#### Exec to the postgres container, to check info
```
> docker exec -it stagingdb bash

## within the container terminal use
$ psql --help

$ psql -U postgres    ## postgres is the username which has root access, check docs

## To list the database users, type below command in the container
$  \du
Role name |.....
----------+-----
postgres  | Superuser, create role.....
```

#### Create a new database in postgres, from the container 
```
##3 after exec to container and issue psql -U postgres

$ create database dev;
CREATE DATABASE

$ \l    ## This will list the database

### To connect to database from psql

$ \c dev     ## \c database-name
```
 
 #### In order to connect to the psql of the docker container, install the psql/postgres in the laptop/host machine.
 
 ```
 > plsql -h localhost -p 5432 -U postgres
  Password for user postgres: admin/password
 # \c dev
 ```

#### Now we can create a spring boot application and access the table.
  - in spring configure the datasource, in below case application.yaml
```yaml
app:
  dataSource:
    postgres:
       port: 5432
       username: postgres
       password: admin/password
       host: localhost
       databaseName: dev
       jdbc-url: jdbc:postgresql://${app.dataSource.postgres.host}:${app.dataSource.postgres.port}/${app.dataSource.postgres.databaseName}
```
  - use database sql, within the classpath to create sql command to create table.

#### other postgres commands to view table, etc
```
$ psql -U postgres
$ \c dev
$ \d+;   ## This lists the table name.
$ \d+ <table-name>;  ## This list the specific table content
$ insert into <table-name>(field1, field2, field3) values (uuid_generate(), 'test','test');
 ## make sure to install the uuid extension, using below command
$ create extension if not exits "uuid-ossp";
```
