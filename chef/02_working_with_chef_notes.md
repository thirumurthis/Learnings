#### Install the `chef workstation` for detail about the tools of check [link](https://github.com/thirumurthis/Learnings/blob/master/chef/chef_basics.md)

- Once the chef workstation is installed, create a cookbook using below command
```
> chef generate cookbook learn_chef
## The above command will create a directory and relevant cookbook artifacts
## navigate to learn_chef folder
```
- Content of the `kitchen.yml`, has default configuration to set up a test instance.
- below is the file where it uses `vagrant` driver. For vagrant, you need to set it up by installing `vagrant` and `virtualbox`
```yaml
---
driver:
  name: vagrant

## The forwarded_port port feature lets you connect to ports on the VM guest via
## localhost on the host.
## see also: https://www.vagrantup.com/docs/networking/forwarded_ports.html

#  network:
#    - ["forwarded_port", {guest: 80, host: 8080}]

provisioner:
  name: chef_zero

  ## product_name and product_version specifies a specific Chef product and version to install.
  ## see the Chef documentation for more details: https://docs.chef.io/workstation/config_yml_kitchen/
  #  product_name: chef
  #  product_version: 16

verifier:
  name: inspec

platforms:
  - name: ubuntu-20.04
  - name: centos-8

suites:
  - name: default
    verifier:
      inspec_tests:
        - test/integration/default
    attributes:
```
#### Details about kitchen.yml
<details>
  <summary>kitchen.yml - click</summary>
  
**instance** - A virtualized server with the bare essentials, eg. operating system, ssh- or WinRM-enabled networking, etc. 
                 (This key is not found in the kitchen.yml but is used by Test Kitchen to help keep track of your instances.)
                 
**platforms** - The operating system(s) or target environment(s) on which your policies are to be tested. eg: Windows, Ubuntu, CentOS, RHEL

**suites** - The policies and code which will be enforced on the test instance(s).

**driver** - The lifecycle manager responsible for implementing the instance-specific actions (in this case, Vagrant); 
                these actions can include creating, destroying, and installing the tools necessary to test your code on the test instance(s).

**provisioner** - The tool responsible for executing the suites against the test instance(s). Chef's Test Kitchen provisioner, [chef_zero](https://docs.chef.io/chef_solo/).
   
</details>

#### To use `docker` driver for test kitchen
```
> chef gem list
### above command will displays the list of gem installed 

> chef gem list kitchen-docker
### above command will tell us, if kicthen-docker is installed or not. (displays the version if already installed)

> chef gem install kitchen-docker
### above command will install the gem kitchen-docker
```

#### Once the `kitchen-docker` gem is installed, then use below command on the cookbook. 
 - Note: update the dirver in kitchen.yml from vagrant to docker.
 - use below command to verify if the dirver is recognized
 
```
> kitchen list 
Instance             Driver  Provisioner  Verifier  Transport  Last Action    Last Error
default-ubuntu-2004  Docker  ChefZero     Inspec    Ssh        <Not Created>  <None>
default-centos-8     Docker  ChefZero     Inspec    Ssh        <Not Created>  <None>
```
- To create the test instance, within the cookbook folder use below command
```
> kitchen create
```
 - Note: check this [Link](https://medium.com/software-configuration-manuals/a-step-by-step-guide-to-test-chef-using-test-kitchen-with-docker-9c4f4f4186e2)

#### To converge and apply the cookbook changes, use below command
```
> kitchen converge
```

#### Login using `kitchen ssh ubuntu` to verify the changes

##### Issues executing the docker driver in windows
```
protocol not avialable
```
  - To fix the above issue add below to the kitchen.yml file, refer [link](https://github.com/test-kitchen/kitchen-docker/issues/318)
    - this is a work around to fix.
  ```
  <% @socket = ENV['DOCKER_HOST'] ||
     RUBY_PLATFORM =~ /mingw32/ ?
       'npipe:////./pipe/docker_engine' :
       'unix:///var/run/docker.sock'
  %>
  driver:
    name: docker
    socket: <%= @socket %>
  ....  
  ```

`Note:` Even with the above changes, the windows had issue in running the instance.
    
  -------------------------------
  
 ##### per the chef learning, to stand up a workstation and corresponding web nodes follow below instruction
   - download the docker compose file from [Git](https://raw.githubusercontent.com/learn-chef/chef/master/docker-compose.yml). Make sure the docker is already installed.
 - Pull the images
 ```
 > docker-compose pull
 ```
 - Run the containers
 ```
 > docker-compose up -d
 ```
 - exec to work station docker container
 ```
 > docker exec -it workstation bash
 ```
 #### FOLLOW the `chef-run` sample, as per this [link](https://github.com/thirumurthis/Learnings/blob/master/chef/03_chef_tools_chef-run.md) once above docker instance is running.
 
 - To remove the container and images
 ```
 > docker-compose down --rmi all
 ```
  
  
