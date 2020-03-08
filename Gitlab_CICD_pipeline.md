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

