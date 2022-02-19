Required:
```
Install - ChefDK (2.0+), Ruby (2.5+), Docker (17+)
Update – Chef drivers kitchen-docker, kitchen-inspec
```

Create a cookbook using below command:
```
chef generate cookbook demo1
```

In demo cookbook, create a .kitchen.yml file. (with content at below content)
```
suites – verifier: inspec_tests points to test cases directory within cookbook
```

Add a recipe to create `/tmp/text.txt` file with content hello world

##### content
```
driver:
  name: docker

provisioner:
   name: chef_zero
   always_update_cookbooks: true
   client_rb:
      chef_license: accept

platforms:
- name: centos
  driver_config:
    image: 'centos:7'
    platform: rhel

verifier:
   name: inspec

suites:
  - name: default
    run_list:
      - recipe[demo1::default]
    verifier:
      inspec_tests:
       - test/recipes

transport:
  name: docker

```

#### Temp testcase to check if the file is created
```
describe file("/tmp/text.txt") do 
          it { should exist } 
        end

```

- Command execution

```
List test kitchen status
$ kitchen list
Create the environment using docker image
$ kitchen create
Converge the recipes to the docker image
$ kitchen converge
Verify the test cases 
$ kitchen verify
Destroy local environment
$ kitchen destroy
```
