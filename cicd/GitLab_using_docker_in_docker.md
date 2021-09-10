
#### Using below `gitlab-ci.yaml` will download the docker imagen, add a docker-in-docker client.

```yaml
stages:
   - build
   - test
   - deploy
  
 variables:
    DOCKER_IMAGE_TAG: $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA    # variables declared out is the global variables
                                                                 # CI_xxx variables provided by gitlab and configurable
                                                                 # the gitlab provides a docker registry, built in our repository
                                                                 # so the built image is stored in the registry locally
    
 build:
   stage: build
   image: docker:stable   # this is the stable image within the docker hub itself - client software packaged in it [The build will be running in this docker]
   services:              # sercives property used for 
      - docker:dind       # dind - docker-in-docker, this step will sartup the secondary docker container, running the dind image
                          # that dind image running a docker daemon.
   
   # below is used to connect to the docker daemon
   # from the docker client (that we are running the build) to connect to the docker-in-docker (dind) container
   # two container are running, 
   #### 1. docker that is building the app - this connects the #2 via tcp
   #### 2. another docker with docker-in-docker daemon.
   variables:    # variables declared at the job level is only visible to this job not accessible outside this job
      DOCKER_HOST: tcp://docker:2375/    # variables is to make docker client to connect using tcp to the docker itself using. the docker client to 
      DOCKER_DRIVER: overlay2
   before_script:
      - docker login -u gitlab-ci-token -p $CI_JOB_TOKEN $CI_REGISTRY
      - docker info
   script:
      - docker build -t $DOCKER_IMAGE_TAG .
      - docker push $DOCKER_IMAGE_TAG
      
```
- From the output of the stages:
   - The docker:dind container will be downloaded first and sets up once that service is up and running.
      - This is a sperate container that will be running in parallel to the other docker container used for actual build
   - The docker:stable container is downloaded and  starts running
      - In the docker:stable container, git clones the repository
      - does the docker login, using the CI_JOB_TOKEN provided for us.
      - other script are executed, docker pushes the image to the registry - Note the docker repository created by git itself, not the docker hub 
