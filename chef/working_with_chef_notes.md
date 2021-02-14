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

