## Download the JFrog jcr (Jfrog Container Registry) zip for Windows.
  - Set the enviornment variable JF_PRODUCT_HOME, the path where the zip file is extracted.
  - Unzip the file, navigate to apps/ folder start the executable under metadata and observability.
  - Start the artifact.bat under the apps/bin folder
  - Once the application is started the UI can be accessed using `http://localhost:8081`


### Setting up Docker artifactory
- From the UI, create the Docker repository with a repository key.
- As the admin, click the `Get Started`, and click the Deploy Artifact option.

The option looks like below
![image](https://user-images.githubusercontent.com/6425536/215361234-37942daf-2b11-40f0-8a86-4db9ee4d1525.png)

The artifact section will look like below, the repository key used here is docker-personal.

![image](https://user-images.githubusercontent.com/6425536/215361279-3af5bb14-b05b-4f2d-ae39-2f9013435cf6.png)

Select Docker 
![image](https://user-images.githubusercontent.com/6425536/215361921-70e5fc04-67e0-4847-8b78-981c01971ec2.png)

Select the Set me up
![image](https://user-images.githubusercontent.com/6425536/215362254-e79ead91-8ee5-4b6d-bfbb-3f7bed8a99e3.png)

Follow instruction
![image](https://user-images.githubusercontent.com/6425536/215361935-fb557913-50e8-4385-8eca-761f22c66545.png)

To create the docker repo follow this instrcution
https://jfrog.awsworkshop.io/2_self_guided_setup/29_docker_repositories.html

Note:- we need to create a virtual repo and add the local repo to it. Doing so even after the main screen logs out we will still be able to see the artifacts.
No need to re-deploy everything again.


To connect to the repo, we need to use the 

```
curl -u <username>:<password> http://localhost:8082/artifactory/api/docker/docker-personal/v2/
```

In case if the above curl response throws 503, like below we need to accept the EULA agreement.
```
{
  "errors" : [ {
    "status" : 503,
    "message" : "status code: 503, reason phrase: In order to use Artifactory you must accept the EULA first"
  } ]
}
```
  
 - To accept the EULA execute below command, and the response looks like in image below

```
 curl -XPOST -vu <username>:<password> http://localhost:8082/artifactory/ui/jcr/eula/accept
```

![image](https://user-images.githubusercontent.com/6425536/215364612-1a68d737-10cc-456e-b7d8-82b0bfe6d4fb.png)

Now, `curl -u admin:password http://localhost:8082/artifactory/api/docker/docker-personal/v2/` will display empty repsonse. `{}`
  
Then in order to login we need to use `docker login <artifactory url>`, make a note using `docker login localhost:8082` will not work in windows.

We need to update the `hosts` file in the windows, `C:\Windows\System32\drivers\etc\hosts` file. (like below, add the line in hosts file)
  
```
  127.0.0.1 artifactory
```
 
Once the hosts file is updated we can login to docker like below
  
```
> docker login http://artifactory:8081
Username: admin
Password:
Login Succeeded
```

 We have a local program to build the image, use below commands to push image to private repo
  -So in this case, we need to (note, 8081 port, not the 8082)  
  Refer https://stackoverflow.com/questions/71132577/how-to-push-docker-image-to-self-hosted-artifactory
  
```
docker login myartifactory:8081

docker tag goapp:latest myartifactory:8081/docker-personal/goapp:v1.0

docker push myartifactory:8081/docker-personal/goapp:v1.0
```
  
  Image that got pushed to the repository
  
![image](https://user-images.githubusercontent.com/6425536/215382195-ff736023-7d07-4879-b59b-9f4ba3eb5f6a.png)

  
 
 
 
