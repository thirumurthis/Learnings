- In this post we will run three Zookeeper Server Ensemble using Docker and connect using Docker zkcli client.

Pre-requisites:
   - Docker desktop installed
  
#### Create Configuration for each Zookeeper server

1. Create the zookeeper configuration for three Zookeeper server.

  - Note: 
   This configuration usually can be updated as zk.conf file, check the documentation in here we will use the configuration file as environment variable to the  docker container.
  
  - Create a folder named zookeeper
  - Save the below content in `zk1.env` file, similarly we do the same for other servers as well.
  
- Below is configuration for zookeeper1 server
    - Note: the ZOO_SERVERS key value should be in single line sperated by space for each server.

```
ZOO_TICK_TIME=3000
ZOO_INIT_LIMIT=5
ZOO_SYNC_LIMIT=2
ZOO_MY_ID=1
ZOO_STANDALONE_ENABLED=true
ZOO_ADMINSERVER_ENABLED=true
ZOO_SERVERS=server.1=zookeeper1:2888:3888;2181 server.2=zookeeper2:2888:3888;2181 server.3=zookeeper3:2888:3888;2181
```

- Below is configuration for Zookeeper2 server save as `zk2.env`, ZOO_MY_ID=2

```
ZOO_TICK_TIME=3000
ZOO_INIT_LIMIT=5
ZOO_SYNC_LIMIT=2
ZOO_MY_ID=2
ZOO_STANDALONE_ENABLED=true
ZOO_ADMINSERVER_ENABLED=true
ZOO_SERVERS=server.1=zookeeper1:2888:3888;2181 server.2=zookeeper2:2888:3888;2181 server.3=zookeeper3:2888:3888;2181
```

- Below is configuration for Zookeeper2 server save as `zk3.env`, ZOO_MY_ID=3

```
ZOO_TICK_TIME=3000
ZOO_INIT_LIMIT=5
ZOO_SYNC_LIMIT=2
ZOO_MY_ID=2
ZOO_STANDALONE_ENABLED=true
ZOO_ADMINSERVER_ENABLED=true
ZOO_SERVERS=server.1=zookeeper1:2888:3888;2181 server.2=zookeeper2:2888:3888;2181 server.3=zookeeper3:2888:3888;2181
```

#### Create docker bridge network for each Zookeeper to connect
 
2. Create a docker bridge network for each of the Server to connect

```
$ docker network create --driver bridge zk-cluster

## use below command to list the bridge network we created

$ docker network ls 
```

#### Run Zookeeper container exposing ports

3. Using the `docker run` command we will start three Zookeeper ensemble server
  - Below are the command to run the zookeeper image and expose the ports so host can connect to it.
  - Port usage 
     - `2181` - Client connection port
     - `2888` - is used by the leader node to communicate with follower node called `Quorum port`
     - `8080` - Admin server port, we can access the state of the node using API
     - `3888` - Leader election port (to vote a leader)
     
     Note: We only need to expose two port `2181` and `8080` since other ports are used by the Zookeeper Ensemble

```
# navigate to the zookeeper folder in gitbash terminal

# running container in detached mode and removing once stopped 
# passing the environment file along with created network 
# we are using the image zookeeper from the docker hub

docker run -d --rm -p 2181:2181 -p 8880:8080 --env-file zk1.env --name zookeeper1 --network zk-cluster zookeeper:latest

docker run -d --rm -p 2182:2181 -p 8881:8080 --env-file zk2.env --name zookeeper2 --network zk-cluster zookeeper:latest

docker run -d --rm -p 2183:2181 -p 8882:8080 --env-file zk3.env --name zookeeper3 --network zk-cluster  zookeeper:latest
```

#### Validate the Zookeeper services are running

- Running the above command will start the Zookeeper ensemble, we can validate using `docker ps` command.

```
$ docker ps
CONTAINER ID   IMAGE              COMMAND                  CREATED          STATUS          PORTS                                                                NAMES
d54accf53541   zookeeper:latest   "/docker-entrypoint.…"   9 seconds ago    Up 7 seconds    2888/tcp, 3888/tcp, 0.0.0.0:2183->2181/tcp, 0.0.0.0:8882->8080/tcp   zookeeper3
b09e008a0e94   zookeeper:latest   "/docker-entrypoint.…"   24 seconds ago   Up 22 seconds   2888/tcp, 3888/tcp, 0.0.0.0:2182->2181/tcp, 0.0.0.0:8881->8080/tcp   zookeeper2
c561495d7d49   zookeeper:latest   "/docker-entrypoint.…"   39 seconds ago   Up 36 seconds   2888/tcp, 0.0.0.0:2181->2181/tcp, 3888/tcp, 0.0.0.0:8880->8080/tcp   zookeeper1
```

#### Use Docker Zookeeper zkCli.sh to connect to the server ensemble

- Connect using Docker Zookeeper client (zkCli.sh) to the above ensemble
  - we are using the container name as host and corresponding client port to connect

```
$ docker run -it --rm --network zk-cluster --link zookeeper1:zookeeper1 zookeeper zkCli.sh -server "zookeeper1:2181,zookeeper2:2181,zookeeper3:2181"
```

- Snapshot of client connecting to the servers

![image](https://user-images.githubusercontent.com/6425536/173098787-9c4b8f02-d928-4930-a868-320f7a80e9fd.png)


#### Access the admin API from host to check the leader amongst the server ensemble

- Use `http://localhost:8880/commands/leader` to check which is the leader node, output should be  something like below
 
```
   {
    is_leader: false,
    leader_id: 2,
    leader_ip: "zookeeper2",
    command: "leader",
    error: null
    }
```

- Zookeeper2 url `http:///loclahost:8881/commands/leader` current this one is elected as leader

```
  {
    is_leader: true,
    leader_id: 2,
    leader_ip: "zookeeper2",
    command: "leader",
    error: null
    }
```

- Zookeeper3 url `http://localhost:8882/commands/leader`

```
   {
      is_leader: false,
      leader_id: 2,
      leader_ip: "zookeeper2",
      command: "leader",
      error: null
      }
```

Notes:
   - The above is not production ready, in case we need to persist the data, logs, etc. for which we need to mount volumes.
   - We can create the volumes for data and logs using `docker volume` and attach during `docker run` command
   - Sample command of how to create volume and attach to run command

   ```
   $ docker volume create --driver local --opt type=none --opt device=/c/thiru/zookeeper/data/zk1 --opt o=bind --name zk1-dataDir
   
   $ docker run -d --rm -p 2182:2181 --publish 8880:8080 --env-file zk1.env --mount source=zk12-dataDir,target=/data --mount source=zk1-logs,target=/logs 
--name zookeeper2 --network zk-cluster zookeeper:latest
   ```

Reference:
  - [1](https://farid-baharuddin.medium.com/setting-up-an-apache-zookeeper-cluster-in-docker-8960d5c23f5c)
  - [2](http://www.mtitek.com/tutorials/zookeeper/zkCli.php)
  - [3](https://hub.docker.com/_/zookeeper)
