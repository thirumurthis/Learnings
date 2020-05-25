
```
$ docker run <image-name>

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


