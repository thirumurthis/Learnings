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
 
> docker run nginx -d -p 8080:80
```
