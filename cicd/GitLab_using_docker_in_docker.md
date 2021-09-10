
#### Using below `gitlab-ci.yaml` will download the docker imagen, add a docker-in-docker client.

```yaml
stages:
   - test                             # order of execution is a key, if the test fails per the job declared, the build will not happen saving some performance benefit.
   - build
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
      
  test:      #JOB NAME           # This is the job name, this can be any name
    stage: test                  # this is the stage reference, that this job is used, THIS ASSIGNMENT IS IMPORTANT, else this will be running as TEST stage, in parallel
    image: node:latest
    services:
      - name: postgres:latest
        alias: db
    variables:
       POSTGRES_DB: "${DATABASE_NAME}"
       POSTGRES_USER: "${DB_USER}"
       POSTGRES_PASSWORD: "${DB_PASS}"
       DATABASE_URL: "postgers://${DB_USER}:${DB_PASS}@db/${DATABASE_NAME}
    script:
     - ./script-to-test.sh
      
```
- From the output of the stages:
   - The docker:dind container will be downloaded first and sets up once that service is up and running.
      - This is a sperate container that will be running in parallel to the other docker container used for actual build
   - The docker:stable container is downloaded and  starts running
      - In the docker:stable container, git clones the repository
      - does the docker login, using the CI_JOB_TOKEN provided for us.
      - other script are executed, docker pushes the image to the registry - Note the docker repository created by git itself, not the docker hub 
-------------

##### Build Stages, Variables

 - Stage and jobs: 
    - This is kind of container for the job.
    - Each stage has one or more job in it, each job is run in the order they are declared
    - If any job in the stage makes the stage as failed.
    - If the stage is marked as failed, the subsequent stage will be skipped.
  - Note: Any unassigned job is assigned to "test" stage by default
  
 -------------------
 #### Running the job in parallel and running more than one job in single stage.
   - Say if we need to test the application on different version on node.
   - The test stage wanted to run in parallel. Simply assinging the jobs to same stage, then it will run in parallel.
 ```yaml
  ## same as the above yaml file, adding additional test stage
  stages:
     - test
     - build
     
   test-node10:
      stage: test    # USED TEST STAGE
      image: node:10
      script:
        - ./test-app-script.sh
        
   test-nodelatest:
      stage: test   # USED SAME TEST STAGE
      image: node:latest
      script:
        - ./test-app-script.sh
 ```
   - Another option, in a case if we need to run the same job multiple time to identify occasionally occuring bugs. we can use parallel property in job
 ```yaml
  ## same as the above yaml file, adding additional test stage
  stages:
     - test
     - build
     
   test-node10:
      stage: test    # USED TEST STAGE
      image: node:10
      parallel: 5      # This will run the same instance multiple times in parallel.
      script:
        - ./test-app-script.sh
        
   test-nodelatest:
      stage: test   # USED SAME TEST STAGE
      image: node:latest
      script:
        - ./test-app-script.sh
 ``` 
#### Speeding of the build by cache
  - GitLab CI mostly runs with a clean environment every time.
  - So it has to download the necessary dependencies everytime.
     - say the same node image of specific version is being used but downloaded every time there by using resources.
 
 - the cache can be applied at global level, so it can be applied to all the jobs.
 - Use of key `CI_COMMIT_REF_SLUG` -  this variable is provided by GITLAB CI for us, this indicates to define an id for cache and use it
 - In a big project, there will commits happening in multiple feature and master branches. This key will provide a key to the cache of specific branch.
    - the Gitlab will provide a best guess and associates an id to cache, say if this had identified as master branch and when the build is executed on the master branch it will use the cached info.
    - so seperate branches will have a different cache. 
    - note: first time when the build runs, the folder won't be available, so it will display the message and created it.
           - the dependencies are zipped and used for latter builds of same branch
 ```yaml
  ## same as the above yaml file, adding additional test stage
  stages:
     - test
     - build
     
   test-node10:
      stage: test    
      image: node:10
      cache:                        # This is the configuration for cache NOTE, THIS CAN BE USED AT THE GLOBAL LEVEL AS WELL                       
         key: ${CI_COMMIT_REF_SLUG} # this variable is provided by GITLAB CI for us, this indicates to define an id for cache and use it
         paths:
          - node_modules/           # when npm install runs, the package are stored in node_modules/
      parallel: 5      
      script:
        - ./test-app-script.sh
``` 
#### how to capture the artifacts:
  - The artifacts are downloadable form web.
  - Say in the pipe line, we need to get the npm audit on the json file, to download of the build.
- in the UI, there should be a download job artifact section, where we can download.
 ```yaml
  ## same as the above yaml file, adding additional test stage
  stages:
     - test
     - build
     
   test-node10:
      stage: test    
      image: node:10
      cache: 
         key: ${CI_COMMIT_REF_SLUG}
         paths:
          - node_modules/
      artifacts:              # key used
         paths:
           - package-lock.json         # need to get this file after build
           - npm-audit.json            # need to get this file after build
      parallel: 5      
      script:
        - ./test-app-script.sh
``` 
 - In case if the build fails, the aritifact will not be saved part of the above configuration
 - often git lab doesn't capture the artifacts if the stage fails, in the above case the aritifact is generated and then test fails. 
   - in this case if we need artifacts we can add configuration.

 ```yaml
  ## same as the above yaml file, adding additional test stage
  stages:
     - test
     - build
     
   test-node10:
      stage: test    
      image: node:10
      cache: 
         key: ${CI_COMMIT_REF_SLUG}
         paths:
          - node_modules/
      artifacts:              # key used
         when : always       # There are three options, success (default), unsuccesful, always
         expire_in: 1 week   # to hold on to the artifact for 1 week and delet it after, so we don't use more disk spaces of stored artifacts
         paths:
           - package-lock.json         # need to get this file after build
           - npm-audit.json            # need to get this file after build
      parallel: 5      
      script:
        - ./test-app-script.sh
``` 

#### How to pass the captured artifacts from one job to another
 - Automatic artifacts between stages
 - declaring dependencies explictly

- By default without declaring any stages the default order of stage is build, test , deploy.
  - Since the stages is not mentioned, any aritifacts that are produced in by the job will be available to other job by default
  
```yaml
buid:
  stage: build
  script:
   - echo "demo for the artifact to pass between jobs" > artifact.txt
  artifacts:
    paths:
      - artifact.*

test:
  stage: test
  script:
    - cat artifact.*
```
  - But what happens is want certain artifacts to be available to certain jobs?
```yaml
buid:
  stage: build
  script:
   - echo "demo for the artifact to pass between jobs" > artifact.txt
  artifacts:
    paths:
      - artifact.*
buid2:
  stage: build
  script:
   - echo "Second content for artifact2" > artifact2.txt
  artifacts:
    paths:
      - artifact2.*

test:                # since the dependencies were not defined, both the artifacts will be displayed
  stage: test
  script:
    - cat artifact*.*
    
 ## adding the artifact created by build2 to this job
 
test2:
 stage: test 
 dependencies:   # this is way we pass the artifact of one job to another, in this case the output pipeline will list only the build2 artifacts only
   - build2
 script:
   - cat artifact*.*
```

#### Passing variables between jobs
 - Declaring and using variables
 - Declaring group variables
- The variables has to be set in the GitLab UI, under `Settings` -> `CI/CD` -> `Environment Variables` section
   - Add the VAR1 and some value; (protected option, just makes it available to build time, but not displayed for other users, based on roles)
 
 - Groups and sub-groups:
    - Groups are collection of projects
      - we can apply the variables at the group level, so this will be applied to the sub-groups and its individual projects.
 - when we run the Runpipeline manually, by using the gitlab ui via button in the Run Pipeline button, we can override the group variable or variables with different values.

```yaml
build:
  script:
    - "echo Var1 $VAR1"
    - "echo Var2 $VAR2"
    - "echo $GRPVAR"
    - ./script.sh
    
 ## script.sh - content
 ##  echo " from within the script of git ci variable used: $VAR1"
```
