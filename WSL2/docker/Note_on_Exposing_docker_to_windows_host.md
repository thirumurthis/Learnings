To expose the docker daemon to windows host so Intellij can access, refer the link 

https://stackoverflow.com/questions/70891531/connect-to-wsl2-ubuntu-docker-from-windows-host



1. Add a config override to also expose the tcp socket:

- Create a file `sudo vi /etc/systemd/system/docker.service.d/override.conf` and add below content

```
 [Service]
 ExecStart=
 ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock -H tcp://0.0.0.0:2375
 ```

2. Reload and Restart the Docker daemon:

 ```
 sudo systemctl daemon-reload
 sudo systemctl restart docker
 ```

3. Set the environment variable in Windows to the correct endpoint:

```
 DOCKER_HOST=tcp://localhost:2375
```

4. Run any Docker command in Windows:

```
 docker ps
```

Note: If you want to mount a volume from Windows, this does not work directly as the Docker "context" is the one from WSL, so you need to use paths from WSL. So if you want to mount the folder c:\foo\bar into the container, you can use a command like (note the double //!):

```
docker run --rm -it -v //mnt/c/foo/bar://bar debian
```

From the git bash issue below command, it should return the details of docker daemon as json payload.

```
curl http://localhost:2375/version
```

Make sure to install docker compose, `sudo apt-get install docker-compose`.

In the Intellij IDE to configure the Docker from wsl2 follow (https://www.jetbrains.com/help/idea/settings-docker.html#virtual-machine-path)

Settings > Build, Execution, Deployment > Docker 
 Set the Docker executable: /usr/bin/docker
 Set the Docker Compose executable: /usr/bin/docker-compose
 Select the `Connect to docker daemon with:` set the `WSL` and select appropriate WSL distribution.
 