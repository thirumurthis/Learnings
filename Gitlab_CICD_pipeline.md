### To setup gitlab CI pipe line.

From gitlab.com create a project, include a docker file (if needed).
If using a Java project, just added java classes to the gitlab project from local and push it.

Whch also incuded a `.gitlab-ci.yml` file, a Yaml formatted file which gitlab can read.

In order to run the pipeline, gitlab uses
  - `shared gitlab runner` (free and hosted by gitlab)
  - `Custom gitlab runner` (installed in local)

In order to let gitlab use the custom gitlab runner, we need to specify the tags in the `.gitlab-ci.yml` instruction, 
so when pipeline starts gitlab will use that specific gitlag runner.

##### Setting up the gitlab runner locally.

Note: 
 -  Download the gitlab runner locally and `install` it.
 - `Register` the repository in this case gitlab.com, check the `Settings -> General -> Runner (expand)`
 - Use the `token` to register, Gitlab runner will prompt the user for an tag name. 
 - `Tag name of the gitlab-runner is important, since this will be used to identify the runner. This tag will be included in the yml file.`
  
##### Start the git lab runner `> gitlab-runner.exe start` (check the help), so when there is a push or merge request the pipe line will start.

By default after 8.8 version of Gitlab the `Container Registry` enabled by default. 

Documentation Link to add docker login info and use the docker images from the container registry.

  - Use the command to pull the docker image (`$ docker pull <registry.gitlab..image>`) if it needs to be shared or executed locally.
  
[Gitlab Container registry docs](https://gitlab.com/help/user/packages/container_registry/index)

The pipeline will execute and push the image to the container registry. After execution check for image version under `package -> Container Registr`.

The steps in the pipeline will list the pull and push info, that is specified. There are default .gitlab-ci.yml template to play with.

##### Variables in Gitlag
Create variable in gitlab `Settings -> CI/CD -> Variable (expand)` add variables.
This variables can be used within the `.gitlab-ci.yml` file.

Sample `.gitlab-ci.yml` file:

Note: 
  - SERVER is the actual VM which needs to be deployed or tested.
  - ssh command below will execute the steps in that vm.
  - So when any commit is pushed to branch, only the `test stage` will be kick started.
  - When any merge happens to master `deploy stage` will happen.

```yaml
image: ubuntu:latest
variable
     WORKING_DIR: ${CI_PROJECT_NAME}
	 BRANCH: ${CI_COMMIT_REF_NAME}
	 REPOSITORY: <git project url eg. https://gitlab.com/user/project.git>

stages:
     - test
     - deploy
  
test:
     stage: test
	before_script:
	- 'which ssh-agent || ( apt-get update -y && apt-get install openssh-client -y )'
	- mkdir -p ~/.ssh
	- eval $(ssh-agent -s)
	- '[[ -f /.dockerenv ]] && echo -e "Host *\n\tStrictHostKeyChecking no\n" > ~/.ssh/config'
	script:
	    - ssh-add < (echo "$PR_KEY")  //Variable declared in the gitlab settings.
		- rm -rt .git   // remove any git references
		- ssh -o StrictHostJeyChecking=no username@"$SERVER-TEST" "rm -rf ~/${WORKING_DIR}; mkdir ~/${WORKING_DIR}; git clone -b ${BRANCH} ${REPOSITORY}; cd ~/${WORKING_DIR};"  // add other commands that needs to be executed like install
	only:
	    - branches
	 except:
	    - master
		
deploy:
     stage: deploy
	 before_script:
	- 'which ssh-agent || ( apt-get update -y && apt-get install openssh-client -y )'
	- mkdir -p ~/.ssh
	- eval $(ssh-agent -s)
	- '[[ -f /.dockerenv ]] && echo -e "Host *\n\tStrictHostKeyChecking no\n" > ~/.ssh/config'
	script:
	    - ssh-add < (echo "$PR_KEY")  //Variable declared in the gitlab settings.
		- rm -rt .git   // remove any git references
		- ssh -o StrictHostJeyChecking=no username@"$SERVER-DEV" "rm -rf ~/${WORKING_DIR}; mkdir ~/${WORKING_DIR}; git clone -b ${BRANCH} ${REPOSITORY}; cd ~/${WORKING_DIR};"  // add other commands that needs to be executed like install, deploy instruction
	 only:
	    - master
```      

Courtesy: 
Reference [1](https://www.youtube.com/watch?v=Mj-RdXlNE9E&list=PLaFCDlD-mVOlnL0f9rl3jyOHNdHU--vlJ&index=9)
