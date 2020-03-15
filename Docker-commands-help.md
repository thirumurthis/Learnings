Building docker image (using docker build)

Running docker image in container (using docker container run)
  Running docker as dettached mode or in background 
  
View docker images list (using docker image ls)

Trobuleshoot docker network

Using jib for deployment process.

Using multiple stages in Dockerfile

 FROM <image-name-from-registry> as <stage-name>
 ADD
 WORKDIR
 CMD

[Docker command list](https://gist.github.com/thirumurthis/b90b8d89d55c2856c707708071c252e1)

# Docker

Sample docker file (.dockerignore - similar to .gitignore)
```
FROM adoptopenjdk/openjdk8:latest
WORKDIR /app/test

COPY target/customer-0.0.1-SNAPSHOT.jar /app/test/
EXPOSE 8080
CMD ["java", "-jar", "/app/test/customer-0.0.1-SNAPSHOT.jar"]
```
_Note:_ RUN mkdir /app/test - didn't work in this case since and had to use WORKDIR

--------------------
 ##### list the images created
```
# lists the docker image
> docker images
> docker image -ls 
```

##### build the images
```
> docker build -t <image-name> 
  : -t is tagging a name
```

##### command to run the container
```
> docker run -p 8080:8080 -d <containername> 
  : -p publish to port from container to the local
  Note: in case of Windows 10 the deployed spring book application was not accessible
  Try default container ip <http://192.168.99.100:8080/>
```  
##### Docker toolbox to identify the `default container ip` [docker toolbox](https://devilbox.readthedocs.io/en/latest/howto/docker-toolbox/find-docker-toolbox-ip-address.html)
```
> docker-machine ip default
```

##### command to login to container and view the details
```
> docker exec -it <container-id> /bin/bash
```

##### command to troubleshoot the network
```
> docker network ls
  : lists the network info
```
  
##### command to inspect network
```
> docker network inspect <network-name-from-network-ls-command>
```

##### command to view the history of the image (jib maven plugin)
 - jib build oci/images in a layered fasion rather building the whole image
 - with jib no need for dockerfile, it has a opinated image which will be used for building

```
$ mvn package -P <profilename>(the credientials of the dockerhub.registry will be set in setting.xml of maven)
$ mvn jib:build -P<provilename>
-- jib doesn't require a docker daemon
```
``` 
> docker image history <image-name>
```

# pushing the image to the docker hub
```
docker tag <name> <username/project-name>:0.0.1

docker image ls 

docker push <username/project-name>:0.0.1
```

# To login to the local `Docker` image once the toolbox is started, use the below command
```
$ docker-machine ssh

## in case if the above command prompts for user-id/password try docker/tcuser.
## else find the machine name and try ssh with machine name.
```

# To list the `docker` bare metal machine status use
```
$ docker-machine ls

## output 
NAME      ACTIVE   DRIVER       STATE     URL                         SWARM   DOCKER     ERRORS
default   *        virtualbox   Running   tcp://192.168.99.107:2376           v19.03.5
```
**Note:**
  The name of the machine is default
  
##### Once logged into the machine, then sudo it using sudo -i (identity file)
