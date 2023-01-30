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


To connect to the repo, we need to use the 

curl -u <username>:<password> http://localhost:8082/artifactory/api/docker/docker-personal/v2/
Above curl response 
```
{
  "errors" : [ {
    "status" : 503,
    "message" : "status code: 503, reason phrase: In order to use Artifactory you must accept the EULA first"
  } ]
}
```
  
 - To fix the above we need to execute below command, and the response looks like in image below
 curl -XPOST -vu <username>:<password> http://localhost:8082/artifactory/ui/jcr/eula/accept

![image](https://user-images.githubusercontent.com/6425536/215364612-1a68d737-10cc-456e-b7d8-82b0bfe6d4fb.png)

Now, `curl -u admin:password http://localhost:8082/artifactory/api/docker/docker-personal/v2/` will display empty repsonse. `{}`
  
Then in order to login we need to use `docker login <artifactory url>`, make a note using `docker login localhost:8082` will not work in windows.

We need to update the `hosts` file in the windows, `C:\Windows\System32\drivers\etc\hosts` file. (add below)
  
```
  127.0.0.1 artifactory
```
 
With the above update hostname update the we can login to docker
  
```
> docker login http://artifactory:8082
Username: admin
Password:
Login Succeeded
```
  
 
 
 
