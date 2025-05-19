Once the WSL2 service.d/override.conf is updated and restarted. As mentioned in the `docker/Notes...md` follow below step

Refer doc - https://docs.docker.com/engine/daemon/remote-access/


```
\\wsl$\Ubuntu-24.04\usr\bin\docker
```



Settings > Build, Execution, Deployment > Docker 
-  Set the `Docker executable:` /usr/bin/docker
-  Set the `Docker Compose executable:` /usr/bin/docker-compose
-  Select the `Connect to docker daemon with:` set the `WSL` and select appropriate WSL distribution.

![image](https://github.com/user-attachments/assets/1795bc37-22e2-45fc-bdfb-bfb55c553bd1)

To configure the docker compose file from local as a application to run in Intellij

First select the edit configuration
![image](https://github.com/user-attachments/assets/939ba539-7f64-4c9f-81aa-7dc99ff76554)

Refer the path `Learnings/gRPC/code/grpc-sample/src/java/main/blog/docker-compose.yaml` for more info
![image](https://github.com/user-attachments/assets/2ceb5a1f-f6a9-4ae8-b1d8-7fc359e59b18)
