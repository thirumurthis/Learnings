### Chef

Ruby library are called as gem's

`Recipe` - is made up of ingredients, called chef resources.

`Resources` : are fundamental units of chef.

 Resource contains different elements
  - Type
  - Name
  - Properties
  - Action
  
 ```ruby
 package 'httpd' do
    version '1.1'
    action :install
 end
 
 package - type (resource type)
 'httpd' - name
  do - end => ruby block
  version - property ( each resources has specified list of properties that accepts, and it will be different for file, service, etc.)
  action - to take on, actions are the process of actually placing the resources in desired state. 
 
 ```
Defintion of `Resource` is a statement of configuration policy, also describes the desired state of infrastructure and steps need to bring it desired state.
  
  
Example of chef resources:

`service`
```ruby
service 'ntp' do
  action [ :enable, :start ]
end
```

`file`
```ruby 
file '/etc/motd' do
  content 'this is hello world'
end

file '/var/log/somelog.log' do
  mode 0644
  owner 'root'
  action :create
end  
```
`package`
```ruby
package 'httpd' do
  version '1.0.1'
  action :install
end
```

`Converge the instance ` is a state where the instance brought to desired state.

`cookbook` fundamental unit of configuration and policy distribution.
   Note: Create the cookbook under a `cookbooks directory` else when executing `sudo chef-client -z -r "recipe[cookbook-name::recpie]"` will work as expected. Else 

```sh
$ chef generate cookbook <cookbook-name>
```
```
recipes/
   | 
     default.rb -> default recipe
metadata.rb  -> who maintains it, versioning 
Berksfile -> manages the cookbook dependecies, right now it has the super market url and metadata.rb reference.   
```

Directory structure and issue command at this level
```
[vagrant@localhost chef-learn]$ tree .
.
├── cookbooks
│   └── lamp
│       ├── Berksfile
│       ├── Berksfile.lock
│       ├── CHANGELOG.md
│       ├── chefignore
│       ├── LICENSE
│       ├── metadata.rb
│       ├── nodes [error opening dir]
│       ├── README.md
│       ├── recipes
│       │   └── default.rb
│       ├── spec
│       │   ├── spec_helper.rb
│       │   └── unit
│       │       └── recipes
│       │           └── default_spec.rb
│       └── test
│           └── integration
│               └── default
│                   └── default_test.rb
├── nodes [error opening dir]
└── setup.rb

11 directories, 12 files
[vagrant@localhost chef-learn]$ pwd
/home/vagrant/chef-learn
[vagrant@localhost chef-learn]$ sudo chef-client -z -r "recipe[lamp::default]"

```
When executing the recpie the files are read from top to bottom and left to right

```ruby
# recipe file
package 'httpd' do
  action :install
end

file '/var/www/html/hello.html' do
  content "<h1> Hello From chef </h1>"
end

service 'httpd' do
  action :start
end
```

Note when the service resource name was "web" the below exception occurred.
In this case the systectl command executed with the name "web", when changed to 'httpd' it worked as expected.
```
# Scenario were exception occured when using web
Running handlers:
[2020-02-08T03:14:12+00:00] ERROR: Running exception handlers
Running handlers complete
[2020-02-08T03:14:12+00:00] ERROR: Exception handlers complete
Chef Client failed. 0 resources updated in 04 seconds
[2020-02-08T03:14:12+00:00] FATAL: Stacktrace dumped to /root/.chef/local-mode-cache/cache/chef-stacktrace.out
[2020-02-08T03:14:12+00:00] FATAL: Please provide the contents of the stacktrace.out file if you file a bug report
[2020-02-08T03:14:12+00:00] FATAL: Mixlib::ShellOut::ShellCommandFailed: service[web] (lamp::default line 15) had an error: Mixlib::ShellOut::ShellCommandFailed: Expected process to ex
it with [0], but received '5'
---- Begin output of /bin/systemctl --system start web ----
STDOUT:
STDERR: Failed to start web.service: Unit web.service not found.
---- End output of /bin/systemctl --system start web ----
Ran /bin/systemctl --system start web returned 5
[vagrant@localhost chef-learn]$
[vagrant@localhost chef-learn]$ vi  cookbooks/lamp/recipes/default.rb
[vagrant@localhost chef-learn]$ sudo chef-client -z -r "recipe[lamp::default]"
[2020-02-08T03:16:19+00:00] WARN: No config file found or specified on command line. Using command line options instead.
```

```
# Scenario where service had 'httpd' as name
Recipe: lamp::default
  * dnf_package[httpd] action install (up to date)
  * file[/var/www/html/hello.html] action create (up to date)
  * service[httpd] action start
    - start service service[httpd]

Running handlers:
Running handlers complete
Chef Client finished, 1/3 resources updated in 04 seconds
```

### Calling one recipe from another recipe using `default.rb` from cookbook and using `include_recipe` from another recipe file too.
```ruby
# default.rb
file `var/www/html/index.html' do
  content 'Add some content'
end

include_recpie 'lamp::web' //cookbook-name::recpie-name
When executing the default.rb, the web recipe will include.
```
  
 In order to execute the sudo chef-client command no need to specify the default recpie name if we need to execute it the content from default.rb file.
 
```bash
  $ sudo chef-client -z -r "recpie[lamp]"
  --not specifing the recpie name since default.rb will be used.
```

Since in vagrant file we updated the forward port to forwar 80 from guest to host 8080, so when the service is enabled 
the http://localhost:8080 should render the index.html content.

### Custom Resources

`name_property`: allows the value for this property to be equal to the 'name' of the resource block
`instance_name` property is then used within the custom resource in many locations,defining paths to configuration files, services, and virtual hosts.
 
```ruby
resource_name : custResource1 //name of the resource

// name_property: true allows the value for this property to be equal to the 'name' of the resource block
property : instance_name, String , name_property : true

// this value will be passed by the another recipe from resource block.
property : version_info, String, required : true   

```
`new_resource` : example new_resource.version - used within the custom resource to use 
Any properties that are marked identity: true, desired_state: false, or name_property: true will be directly available from load_current_value

```ruby
property :action, String, name_property: true
property :content, String

load_current_value do |desired|
  puts "The user requested action = #{action} in the resource"
  puts "The user typed content = #{desired.content} in the resource"
end
```

### `Ohai` 
Tool to used by chef workstation for profiling system properties into json, which is more about the system information. 
This profile data can be used dynamically.


For example, ubuntu used apt-get package manager and centos uses Yum package manager, chef-client knows to use the appropriate package manger using the ohai utility

Ohai is a command line utility for getting the system inventory, which is of json format.

Using the `jq` utility you can read through json.
```unix
# command to use to get memory

$ ohai memory
$ ohai ipaddress 
$ ohai memory/total 

# we can use this commands within the recipe
```

Ohai command is used build the globally accessible object called `node`. which can be used within the recipe/template to access info

##### Adding the ip address and host name using templats to index.html

Command to create the template file
Note: Navigate to the corresponding cookbook and issue the below command.

```
$ chef generate template web-content.erb

# note, the command will automatically create the templates folder.
```

```ruby
# edit web-content.erb
<h1> Hello from Chef!! </h1>
<h3> Host Name </h3> : <%= node["hostname"] %>
<h4> Ip address </h4> : <%= node["ipaddress"] %>
```
```ruby
# edit default.rb add the below command, comment out the file resource using # on each line
# template command will create file resolving the dependency

template '/var/www/html/index.html' do
  source 'web-content.erb'
  action :create
end
```

##### To use the `node` global variable within the file resource using `#{.. }`
```ruby

# note: don't leave spaces between # and {
file `/var/www/html/index.html' do
  content "<h1> Hello from chef!!
    hostname : #{node['hostname']}
    memory : #{node['memory']['total']}
    platform : #{node ['platform']}  
    "
end
```

##### `node` attributes is tunable and can be accessed in all the cookbook.
node attributes are created inside the cookbooks, we need to create `attribute` within the cookbook

```unix
$ chef generate attributes cookbooks/lamp default
# note the command is issued from above cookbooks directory level
# default is the attribute file name, creates a attributes/default.rb
```

Synatx to define the attributes use `default`
```ruby
# cookbooks/lamp/attributes/default.rb

default['lamp']['hello_path']='/var/www/html/hello.html'

# default keyworkd is called precedence level, check docs for more detail
```
When executing the below file the file name attribute will be replaced accordingly.
The attributes can be used to set some global values which can be used accross cookbooks.

```ruby
# edit the cookbooks/lamb/default.rb - file resources

# replacing the actual file name with the node attribute created in the cookbooks/lamp/attribute/default.rb

file node['lamp']['hello_path'] do  // the usage with latest version is node.default['lamp']['hello_path']
   content "<h1> Hello from chef!!
    hostname : #{node['hostname']}
    memory : #{node['memory']['total']}
    platform : #{node ['platform']}  
    "
end
```

### `Berksshelf`
In a cookbook in case if hte `Berksshelf` is missing it can be created using below command

```unix
# navigate to appropriate cookbook where the Berskfile needs to be created
$ berks init
```

##### Resolving dependency using `berks install`

```unix

# navigate to appropriate cookbook
$ berks install
```
##### The `berks install` will resolve the dependencies and downloads the appropriate version of cookbook, from the supermarket or enterprise repo, ot `~/.berskfile/cookbooks` location.

##### Upload the cookbook to the server

`Berksfile.lock` locks the appropriate version to be uploaded to the chef server.
Whenever there is a change in the version of cookbook, issue ` berks install` and upload.

In case of any exception occured during upload process 

```unix
# within the cookbook directory that needs to be uploaded, use the below command
$ berks upload

# if there are issues during upload, there is high chance the syntax of the cookbook is incorrect.
# use cookstyle a lint tool to validate the cookbook for syntax, etc.
```
### `knife` command:

The knife command interacts with the chef server, sample command to list the cookbook.
```unix
$ knife cookbook list
```

##### **`chef server`** is a centeralized location to store the policies like cookbook, and helps distribute these resources to different managed nodes. The nodes runs different or same cookbooks specified in the `run-list`.

##### **`Bootstraping`** is a rare scenario where the using a workstation (local machine), connect to the node and pull the chef cookbooks and execute the chef-client run.

#####  To execute the bootstrap command make sure to be at the chef-repo level, where the .chef folder exists.

```unix
$ knife bootstrap FDQN -p <port number> -x Username -P password -i /path/of/identityfile -N <node-name> -r 'recipe[lamp]' --sudo
#  -p - lower case p to authenticate on this port
# -r - runlist to execute
# the --sudo executed the bootstrap as root.
# FDQN - in our case using localhost, this can be a name to the machine
# if want to used identity file rather than using a password
$ knife bootstrap FDQN -i <identity-file> 
```

In case we needed to gather the config information of vagrant, issue the command.
```
# the vagrant instance should be up and running. check using vagrant status
$ vagrant ssh-config

# Execute this command in the node which needs the chef-client to be executed connecting to chef server.
# simply the node that needs to be provisioned using cookbooks.
```

##### `data bags` is a place where the senstive information can be stored, like database password, etc.

The information is stored as key value pairs.

To create a databags,
  - inside the chef-repo directory create a folder `data_bags` 
  - create the necessary directory, for example i wanted to store password info, so creating a folder `data_bags/databg1`
  - create a new file, within the `databg1` folder, a json file to store the data bag information
  - The requirement for the json within the database is the json file should have id
  
  ```json
  # databg1/databag_example.json
  { 
    "id" : "databag_example",
    "key1" : "value1",
    "key2" : "value2"
  }
  ```
  
  ##### Upload the databags to the chef-server, so all cookbooks can be accessing and use it.
  
  ```ruby
  # within the recipe file ".rb" to get data bags
  
  # create a local variable,  use chef helper method data_bag_item
  # data_bag_item('<name-of-databags-folder>','reference-of-the-databag-item-id')
  
  databgValue = data_bag_item ('databg1','databag_example');
  
  # using read values from the local variable
  
  printVariable = databgValue['key1']; // value1 will be set to the local variable
  ```

  ##### Before uploding the databags check if the data bags item already exists using below command
  
  Note: we are located insider `chef-repo` directory level
  ```unix
  $ knife data bag list
  # there shouldn't be any output; if there is no data bags in the chef-server
  ```
  
  ##### To Create the data bag
  ```unix 
  $ knife data bag create databg1
  # created  data_bag[databg1] response in case of successfull execution
  ```
  
  ##### To Add items to the data bag
  ```unix
  $ knife data bag from file databg1/databag_example
  ```
  
  ##### To view the databag list from the databag from the chef-server
  ```unix 
  $ knife data bag show databg1
  
  # use the databag name
  $ knife data bag show databg1 databag_example
  
  # note if the data bag is not unencrypted, there will be a warning message.
  # check the documentation for encrypted databags, chef vault.
  ```
  
  ##### Now we can upload cookbook using berks command
  
  Navigate to the cookbooks/lamp to issue `berks install` and then `bersks upload`.
  
  Note:
   - `berks upload` berkshelf checks the version in chef-server cookbook version 
   - If there is no change, then there will be message cookbook skipped with a message (frozen)
    
    In order to make the chef-server to refelect the changes in the workstation cookbook, update the metadata.rb version, then issue berks upload.
    The verson number used is symentic version `major.minor.patch` version number.
    
  ##### Set the run-list to the node
  
  ```unix
  ## to display the details of the node. Also displays runlist
  $ knife node show <node-name>
  
  ## Set the runlist using knife node
  $ knife node run_list set <node-name> "recipe[lamp]"
  ```
    
  ##### Converge the runlist in the specific node which is to be provisioned
  
  For the first time once logged in to the node that is to be provisioned, and upon issuing the below command
  it will check with the chef-server and updates the corresponding node.
  
  ```unix
  ## login to specific node, and issue chef-client command
  $  sudo chef-client
  ```
  
  chef-client default behaviour is to reach out the chef-server and pull the cookbook and recipes to be in desiered state.
  It will skip the resources that are in desiered state.
  
--------

##### Issue with the Chef-DK installable with Windows 10 home edition.

After installation of the chef-DK executable, opens the PowerShell with the below exception, 
[Link to fix above issue.](https://sqlwithmanoj.com/2017/06/09/powershell-error-import-module-file-azurerm-psm1-cannot-be-loaded-because-running-scripts-is-disabled-on-this-system/)

##### [ >>UPDATE<< ] Chef-DK updates has been stopped by Chef community, use *`chef workstation`*

```
Import-Module : File C:\opscode\chefdk\modules\chef\chef.psm1 cannot be loaded. The file
C:\opscode\chefdk\modules\chef\chef.psm1 is not digitally signed. You cannot run this script on the current system.
For more information about running scripts and setting execution policy, see about_Execution_Policies at
https:/go.microsoft.com/fwlink/?LinkID=135170.
At line:1 char:123
+ ... l | out-string | iex; Import-Module chef -DisableNameChecking;echo 'P ...
+                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : SecurityError: (:) [Import-Module], PSSecurityException
    + FullyQualifiedErrorId : UnauthorizedAccess,Microsoft.PowerShell.Commands.ImportModuleCommand
```

##### The above issue was resolved using below command, from power shell. `Set-ExecutionPolicy RemoteSigned`

```
PS C:\WINDOWS\system32> Import-Module chef
Import-Module : File C:\opscode\chefdk\modules\chef\chef.psm1 cannot be loaded. The file
C:\opscode\chefdk\modules\chef\chef.psm1 is not digitally signed. You cannot run this script on the current system.
For more information about running scripts and setting execution policy, see about_Execution_Policies at
https:/go.microsoft.com/fwlink/?LinkID=135170.
At line:1 char:1
+ Import-Module chef
+ ~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : SecurityError: (:) [Import-Module], PSSecurityException
    + FullyQualifiedErrorId : UnauthorizedAccess,Microsoft.PowerShell.Commands.ImportModuleCommand
```
```
PS C:\WINDOWS\system32>  Set-ExecutionPolicy RemoteSigned

Execution Policy Change
The execution policy helps protect you from scripts that you do not trust. Changing the execution policy might expose
you to the security risks described in the about_Execution_Policies help topic at
https:/go.microsoft.com/fwlink/?LinkID=135170. Do you want to change the execution policy?
[Y] Yes  [A] Yes to All  [N] No  [L] No to All  [S] Suspend  [?] Help (default is "N"): A

PS C:\WINDOWS\system32> Import-Module chef
WARNING: The names of some imported commands from the module 'chef' include unapproved verbs that might make them less
discoverable. To find the commands with unapproved verbs, run the Import-Module command again with the Verbose
parameter. For a list of approved verbs, type Get-Verb.
WARNING: Some imported command names contain one or more of the following restricted characters: # , ( ) {{ }} [ ] & -
/ \ $ ^ ; : " ' < > | ? @ ` * % + = ~
```

### Demo to work with Chef-server and Kinfe bootstrap from windows command

1. Using vagrant and virtual box, setup a new box, for demo i used `bento/centos-8.1`, and enabled the public_network in the `Vagrantfile`
2. From the windows command prompt, validated the connection to guest box using `ping hostname` in this case ping chef-node-vm1. chef-node-vm1 was the hostname of the vm which i wanted to execute the chef cookbook.
3. Issued `knife bootstrap` bootstrap command from windows command prompt.
```
 > knife bootstrap chef-node-vm1 -U vagrant -i  C:/<path>/chef-node/.vagrant/machines/default/virtualbox/private_key -N chef-node-vm1 --sudo
 
  The above command will connect to chef-node-vm1, and installs the chef-client to the node.
  
  Right now the command doesn't include any run list
```
 Content upon issuing the above `knife bootstrap` command.
```
Before you can continue, 2 product licenses
must be accepted. View the license at
https://www.chef.io/end-user-license-agreement/

Licenses that need accepting:
  * Chef Infra Client
  * Chef InSpec

Do you accept the 2 product licenses (yes/no)?

 > yes

Persisting 2 product licenses...
✔ 2 product licenses persisted.

+---------------------------------------------+
Connecting to chef-node-vm1
```
 4. Login to the vagrant machine (chef-node-vm1), then issue `sudo chef-client` commanf which will install the run list if specified.
 ```
 [vagrant@chef-node-vm1 ~]$ sudo chef-client
Starting Chef Infra Client, version 15.8.23
resolving cookbooks for run list: []
Synchronizing Cookbooks:
Installing Cookbook Gems:
Compiling Cookbooks...
[2020-02-16T00:26:14+00:00] WARN: Node chef-node-vm1 has an empty run list.
Converging 0 resources

Running handlers:
Running handlers complete
Chef Infra Client finished, 0/0 resources updated in 06 seconds
 ```
 4.1 Re-issue with the command with the run list, which automatically updates the 
 ```
 > knife bootstrap chef-node-vm1 -U vagrant -i  C:/thiru/chef-node/.vagrant/machines/default/virtualbox/private_key -N chef-node-vm1 -r 'recipe[lamp]' --sudo
 
Connecting to chef-node-vm1
The authenticity of host 'chef-node-vm1 (fe80::a00:27ff:fe22:f080%35)' can't be established.
fingerprint is SHA256:NK8IAID7Zffehl7Edq9wrgGIvj3baX0sD8ZUXEJ1icA.

Are you sure you want to continue connecting
? (Y/N) Y
Connecting to chef-node-vm1
Node chef-node-vm1 exists, overwrite it? (Y/N) Y
Client chef-node-vm1 exists, overwrite it? (Y/N) Y
Creating new client for chef-node-vm1
Creating new node for chef-node-vm1
Bootstrapping chef-node-vm1
 [chef-node-vm1] -----> Existing Chef Infra Client installation detected
 [chef-node-vm1] Starting the first Chef Infra Client Client run...
 [chef-node-vm1] Starting Chef Infra Client, version 15.8.23
 [chef-node-vm1] resolving cookbooks for run list: ["lamp"]
 [chef-node-vm1] Synchronizing Cookbooks:
 [chef-node-vm1]   - lamp (0.1.0)
 [chef-node-vm1] Installing Cookbook Gems:
Compiling Cookbooks...
....
Running handlers:
Running handlers complete
Chef Infra Client finished, 5/5 resources updated in 44 seconds
 ```
5. The `knife bootstrap` command push the node info to chef server. 
 
 ![image](https://user-images.githubusercontent.com/6425536/74600839-e1d7b180-504b-11ea-9726-b9ac6c5ad768.png)


6. Updated the cookbook to use the node.default[][] values from attribute folder, verified in virtual box using chef-client -z
7. Pushed the cookbook to chef-server using `knife upload cookbooks/lamp` from the chef-repo directory level.
8. In the chef-node-vm1 host, i issued the `sudo chef-client` command, which chef-client made sure to sync up with the chef-server and updated the cookbook as hosted in the runlist.

```
[vagrant@chef-node-vm1 ~]$ sudo chef-client
Starting Chef Infra Client, version 15.8.23
resolving cookbooks for run list: ["lamp"]
Synchronizing Cookbooks:
  - lamp (0.1.1)
Installing Cookbook Gems:
Compiling Cookbooks...
Converging 4 resources
Recipe: lamp::default
  * dnf_package[httpd] action install (up to date)
  * template[/var/www/html/index.html] action create (up to date)
  * file[{}] action create
    - create new file {}
    - update content in file {} from none to a68087
    --- {}      2020-02-16 01:39:52.669485404 +0000
    +++ ./.chef-{}20200216-7007-w9qvdf  2020-02-16 01:39:52.668485386 +0000
    @@ -1 +1,6 @@
    +<h1> Hello from chef!!</h1>
    +    hostname : chef-node-vm1 <br>
    +    memory : 841104kB <br>
    +    platform : centos <br>
    +
    - restore selinux security context
  * service[httpd] action start (up to date)
  * service[httpd] action enable (up to date)

Running handlers:
Running handlers complete
Chef Infra Client finished, 1/5 resources updated in 33 seconds
```
##### checking the status of the node from the work station.

```
[vagrant@chef-ws-vm1 chef-repo]$ knife node show chef-node-vm1
Node Name:   chef-node-vm1
Environment: _default
FQDN:        chef-node-vm1
IP:          10.0.2.15
Run List:    recipe[lamp]
Roles:
Recipes:     lamp, lamp::default
Platform:    centos 8.1.1911
Tags:
```

###### If you don't want the chef infra server to run chef cookbooks, then install the chef workstation in the local machine and use the `chef-client` to execute the cookbooks locally. This is used for executing and testing cookbook and recipes within the specific cookbook directory.

Executing the chef-client should be done with caution, since it will look for hte cookbook and default information to be executed.

### Utilities `chef` and `knife` to work with chef assest

`chef`- utility used to generate resources like cookbook, recipies repostiory etc.  To manage the local chef work station itself like installing gems, etc.
`kinfe` - utility provides connectivity between local chef repostiory and chef infra server instance. Like query and work with the submit cookbook data and submit to infra server, qindex data, kick start the 
 
`chef-run` - using ssh the temporary run-list can be created and run those in remote nodes directly no need for chef infra server.

Hosted chef: manage.chef.io
port used 443

 
Organization is group at highlevel mangaement in the chef host which contains cookbook, etc.
Organisation should be unique, within the chef hosted server.

`Chef infra client` - this gets installed in all the managed nodes, and registered to chef infra server. During registering it uses valid authenticated certificates.
  this makes sure that the version in the infra server applicable is same as that in the chef infra client by syncing the cache of the node.
 
When a node is registered to the chef infra server, the chef-infra client int the managed node uses Ohai to profile the local node and send properties and metadata back to chef infra server.

The infra server now has an perspective of that node as well as other node nodes under the organization.
 
As chef organizer, have to administer which cookbook to be run on the node. One method to use the `role`, for example define a role web-server, to install apache2 server or nginx server. Each cookbook is tied to specific version for robustness.

`run-list`: Then assign this role to nodes using run_lists, run-lists is modular, used to assign individual cookbook to node or list of cookbook to node.
run_list contains all resources which the node needs, in order to converge on the desired outcome.
When the node recieves this run_list from the infra server the chef infra client expands the implicit list of resources to develop an action plan, along with the necessary dependencies.
 
Infra client checks with the chef infra server, to see which cookbooks are contained with the role, and generate the resources and actions.

run-list, seems to be unique to every node and are calculated every time chef infra client runs even in case of no changes. Don't need to single run_list and assign same run_list to multiple nodes.

Make use of roles to set the run-list  also `environments` can be used.
`environment` gives control to specify versions and attributes, this gives ability to declare, for example the development environemnt uses the latest version and production development can use specific  version of cookbook (may be older) which can be achived by the environment construct.

 ### Role
 
 - Assigning cookbooks to nodes using roles, where the run_list can be used to implement for sepcific nodes.
 
 ```
 if you have a role defined for specific to server and needs to be applied to specific node, define the role within in a chef-rep/role/server.json
 ```

Sample role json defintion file, assume the helloworld cookbook is already available.

 ```json
 {
   "name" : "hello",
   "chef_type" : "role",
   "json_class" : "Chef::Role",
   "default_attributes" : {
   },
   "description" : "Hello world example for roles",
   "run_list" : [
     "recipe[helloworld::default]"
   ]
   
 ```
 
 ```
Then use the knife command to push to assign that role to a run list.
There can be one or more roles assigned to a run list, and this will be executed in the specified order.
 
 ```
 
 #### Roles and run list:

```
# roles location within the chef-repo
chef-repo
  |_ .chef
  |_ cookbooks
  |_ databags
  |_ ploicyfiles
  |_ roles
     |_ hello.json
  
# to see the run list info on a specific node
$ knife node show <node-name>
...
..
Run List:
...

```

```
Upon setting up the run list to the nodes, the information can also be seen in the chef infra server UI. under policy section.

# issuing chef client in managed node
$ sudo chef-client
// if there is no run list then there won't be any resources to converge.

From the chef workstation. lets set the run list 

$ knife node run_list set <node-name> 'role[hello]'

# node the below command will display the run list setup
$ knife node show <node-name> 

# issuing the sudo chef-client now will use the cookbook specified in the role and executes that resource.

NOTE:
  if the run list is cleared out.
  $ knife node run_list set <node-name> ''
  Then, after issuing sudo chef-node on the mananged node will not deleted the previously executed resources by the previous role or cookbook.

Removing the role from run list will not reverse the effect of already executed role or cookbook on that node.
```

 #### Approach to manage the chef nodes:
 
  - 1. There are multiple ways to managing  chef nodes, this is one of approach the use of roles and run list was one recommeded way.
 
 - 2. Recently, there was another apporach emerged, were to use policy files for managing. 
 
 Still both the approach is applicable.
  
##### chef super market
private supermarkets, requires chef infra server for Oauth.

##### Kinfe search command example

The hosted chef infra server has some list of properties and some nodes bootstrapped.

Below are few commands to search the nodes.
The ohai properties are synced about the node, under the attributes If you click the nodes and clicking the attributes tab.

The ohai information is indexed and searchable.
```
// the details information of the nodes present in the chef infra server.

// To search and display the instanced present in the chef infra server
$ knife search node '*:*' -i
   
// To search and display different properties, the ohai parameters.
$ knife serach node '*:*' -a lsb.description

// if wanted to serach the specific id on the ohai properties (ie. the key of the json properties) with the values
 $ knife search node 'lsb_id:Debian'
 # note the usage of _
 
$ knife search node 'cloud_provider:azure' -i 

$ knife search node 'cloud_provider:azure' -a ipaddress -a fqdn

$ knife search 'ipaddress:192.168.1.2'

# wild card with ? - single character within 0-1..
$ knife search 'ipaddress:192.168.1.?'

# using boolean condition
$ kinfe serach 'cloud_provider:azure AND lsb_id:Ubuntu'
```

### Test kitchen

uses either Bento or Packer, the testing should start with a clean virtualized vm. 

infosec is used to validate the test cases to see if the desired output is achieved.

```
# assume a cookbook with resources

cookbooks
|_ example1
    |_ recipies
       |_ default.rb
       |_ node-info.rb
    |_ test
       |_ integration
          |_ defailt
             |_ default-test.rb
             |_ node-info_text.rb
    |_ kitchen.yml
```
```
# default.rb (below is the content of this file)
  include_recipe 'example1::node-info'
```
```
# node-info.rb (below is the content of this file)
  
  # ruby variable # is ruby comments
  filename = 'node-info.txt'
  
  file '/#{filename}' do
    content 'For test'
  end
```
```
 # kitchen.yml - (below is the content of this file)
 
   driver: 
      name: vagrant
   
   provisioner:
       name: chef_zero
   
   verifier:
       name: inspec
       
    platforms:
      - name: ubuntu-19.04
      
    suites:
      - name: default
        verifier:
           inspec_tests:
              - test/integration/default
        attributes:

Notes:
   - driver, test kitchen uses Hashicorp vagrant to provision the testing environment, there are other option to provision directly in Azure.
   
   - provisioner, the toolset which will be used to build and test the cookbook resources on temporary node is chef_zero. The chef_zero, doesn't require chef infro server instance for provisioning. 
   
   - verifier,is test verifier. /there is other option serverspec.
   
   - platforms: used to specify the testing environment, in this example a single ubuntu 19.04 is specified, but we can specify different version of ubuntu os itself here, in this case we can add the list here.
   
   - suites:
        which will be executed against the node.
      
```
```
# default_test.rb - This file should be present under the test folde, which contains the default sample. 

# example test, to be replaced with user own test
unless os.windows?
   describe user('root'), :skip do
      it { should exist }
   end
end

# example test, to be replaced with user own test
describe port(80), "skip do
  it { should_not be_listening }
end
```

```ruby
# node-info_test.rb (actual test cases for the cookbook example1)

# native ruby variable declartion to store value
filename = 'node-info.txt'

describe file("/#{filename}") do
  it { should exist }
end

describe file("/#{filename}") do
  its('content') { should_not be nil }
end

describe file("/#{filename}") do
  its('content') { should match 'For test' }
end

# it or its line above is insec language, not much decleartive.
# which is not declerative check the online documentation.
```
Test kitchen requires access to virtialization layer like Virtualbox, Hyper V, VMWare. Some system doesn't support nested virtualization.

```
# command below provides the configuration of the cookbook test.
# this command looks has to be executed from corresponding cookbook 
# folder in this case example1
# this command lists what action was taken earlier, whether the test was executed, etc.

// list info using the kitchen.yml file within the cookbook folder, is any operation is performed earlier.
$ kitchen list

# to start the process for test, use below command.
# this command will communicate with the vagrant and downloads the platforms info and caches it locally. 
# by default the kitchen knows to communicate with Hyper v, etc
# but the defaults can be overrided, in the kitchen.yml
$ kitchen create
```
```
# if we executed this below after kitchen create will list the previous execution state.
$ kitchen list 
```
```
# to bootstrap the test virtual machine with the chef-infra client 
# and copies the cookbook resources with dependencies with the run list and reports the status
# use the below command, will install the chef-infra client to the virtual node/ test machine/box
$ ktichen converge
```
```
# to manually verify the test cases execution manually, use
# login to the test virtual vm box, and see whether the file exists in this case.
$ kitchen login
```
```
# To execute the inspec steps.
# this is already defined, this provides the status of the code.
$ kitchen verify

# any change to cookbook, better write the test cases first.
```
```
# the below command will delete the virtual machine created and other information within that machine. 
# Note that the vagrant cache image will not be deleted.
$ kitchen destroy
```
##### Automate testing
```
# Above is a step-by-step execution, in order to automate the above process, then use below command 
$ kitchen test
// all the above mentioned process is combined and executed.
```

#### Ruby recipe DSL
 - define and declare resources within recipes
 - pass node parameters and define call actions
 - native ruby script can be used within recipe
 
 `include_recipe` is an example, the order in which it is being used in default.rb, is the order of execution too.
 if there are duplicate include_recipe of the same resources, only the first instance is executed subsequent includes are ignored.
 
 Methods: 
    DSL methods, can make use of attributes, data bags and serach results.
    The recipe DSL has some in-built helper methods, for example file resouces  applicable on windows can have validation in helper method, so execution on non windows machine will not have an impact.
    There are helper methods, to work with windows itself, if we need to working with Windows registry, etc.
    The DSL can be used to declare, create, find, edit and delete resources.
    Test using node properties, check if ip address present.
    
 ```
 # sample repository code
 
 apt_repository 'powershell' do
   uri "https://packages.microsoft.com/ubuntu/#{node['lsb']['release']}
   components ['main']
   arch 'amd64'
   key 'https://packages.microsoft.com/keys/microsoft.asc'
   action :add
 end
 
 package 'apt-taransport-https' do
   action :upgrade
 end
``` 
```
# write the test kitchen to check if the powershell is installed.
$ kitchen test

```

Example for helper method
```
# in the previos example1 cookbook, node-info.rb file.
# assume we are including below content, note the if 
# here the platform_family is a helper method
# below will be executed only on debian os not on windows os.
# if we change the debian to windows and execute it the whole bloc k will be ignored.

folder= '/'
filename='node-info.txt'
content='For test'
if platform_family?("debian")
  file "#{folder}#{filename} do
     content "#{content}"
  end
end
```

##### Resource, recpie and cookbook all was developed using native Ruby.

The chef itself ships with the ruby binaries, no need to install and manage the ruby installation separetly.
But it is still possible to install ruby seperately and configure chef to use that specific installed ruby.

For native ruby, check the ruby documentation itself.

```
# declaring variables
 input1 = 1
 
# executing mathemtics expression
 3 + 4 
 
 # working with string, similar to bash shell, etc.
 'string is \' declared' # \ escape charater
 
 # string manipulation can be achived using ruby function
 input = test
 input.upcase # TEST
 
 '2 + 2' # this will be treated as string not exprestion.
 
 # accessing the value
 input = Hello
 
 # double quotes (STRING INTERPLATION)
 "#{input} World"  # output, Hello world - this is called string interpolation.
 
 # single quotes, WYSIWYG
 '#{input} World'  # ouput, #{input} World, treats everything as literal.
 
```
##### Ruby Arrays

```
input= ['/home/user/a','/home/user/b']  # explicit array. the string are in single quotes, we can use double quotes in case we need to use the attributes of the node. known as expicit array.

%w(/home/user/a /home/user/b)   # called whitespace array, ruby short cut to declare an explicit array, each element is separated by white space 

# some usage create a folder iterating the array
# path is a named element, it can be any thing
# value of each element is represented here as path
# whether are not to create a folder or not using the action
%w(/home/user/a /home/user/b).each do |path|
   directory path do
     inherits true
     action :create
   end
end
```
using the ruby native in chef recipe DSL

```ruby

# variables
packages = [
'ack',
'silverseracher-ag',
'htop',
'jq',
'pydf'
]

# using a helper method to check the os is debian
if platform_family?("debian")
  packages.each do |name|
   package name do
      action :upgrade
   end
  end
 end
```
```
# sample test code

packages=[
'ack',
'silverseracher-ag',
'htop',
'jq',
'pydf'
]

# os.debian is a helper method.
# loop through the package and validate if installed.
if os.debian?
  packages.each do |name|
     describe package(name) do
        it { should be_installed }
      end
   end
end

# note using TEST & REPAIR method if the package is already installed you could see when using kitchen converge will not install and display message up to date.
```

##### Wrapper cookbooks
  - This can be used to change the default behavior of the community cookbook, without duplicating or re-writting it.
  - The chef supermarket has the exact same cookbook what you intended to develop, but that cookbook's default input values are not matching your requirement. This is one of situation where a wrapper cookbook comes in handy.
  - wrapper cookbook calls the specific community cookbook and proivide your range of cookbooks.
  - the `include_recipe` can be used to call multiple cookbook
  - this approach can be also used to reduce or simplify the run list which can be minimized.
  - wrapper cookbook can be used to pin specific version of the community cookbook. The version constraints can be provided in the wrapper cookbook, there is a limit in this approach, if the different version of the same community cookbook is used in different cookbook.
  - Important: one Properties of wrapper cookbook doesn't define or declare any resources, if any resources needs to be used then use a different cookbook and use that in the wrapper.
  
```
# Wrapper cookbook where there is no resources are defined
cookbooks
|_ example2
    |_ metadata.rb
    |_ recipes
       |_ default.rb
       
# metadata.rb

name 'node-linux'
version '0.2.10'
chef_version '~> 15.0'

# this is the version constraint., if this is not specified
# the when converging the resources chef can use any version.

# example below chef-client version upto 11.3.5 not less than this minor version
depends 'chef-client' ,'~> 11.3.5'
depends 'packages', '< 1.1.0'

----
# recipes/default.rb

include_recipe 'chef-client'
include_recipe 'packages::default'
 
```
using wrapper cookbooks for roles
```json
{
"name": "node-linux1",
"description": "to linux node",
"chef_type": "role",
"json_class": "Chef::Role",
"default_attributes": {
},
"override_attributes":{
},
"run_list": [
   "recipe[example2::default]" // this is the wrapper cookbook, which will intern call the dependent cookbook.
   ]
}
```
Cookbook versioning:

Semantic Versioning:
  - If there are new functionality major or minor updates happens part of the development the version needs to be updated in order reflect this changes to the managed nodes.
  - version number can also be contorlled using roles/environments/wrapper cookbook/ policy files.
  - syntax, a.b or a.b.c (a or a.b.c.d are not allowed - no characters is allowed.)
```
Constraining cookbook version
```
 = 2.1.1 
cookbook will use exactly uses that version (and only version 2.1.1)
 
 > 2.1.1
 node will be using any version greater than 2.1.1, which includes the major version too.) not that 2.1.1 version it can also include 3.0.0+
 
 < 2.1.1
 node will be using any version less than but not the 2.1.1 version
 
 <= 2.1.1.
 node use less than or equal to
 
 ~> 2.1.1
 (pesimesstic constraint or approximately greater than equal to)
 Any version 2.1.1 or higher and NOT the major version 3.0.0. in here 3.0.0 will not be used, only within the major version 2 will be used.
 
```

Products of chef on continuous delivery,
- chef compliance
- chef reporting
- chef push jobs
extend the functionality of chef server. This helps configuration manangement. 

Chef Automate recent version v.2 is a dedicated to automation.
Inspec can integrate with wider chef ecosystem by reporting the results and publishing reporting.
Chef Habitat -  Application build automation tool. Using builder account to explore this feature in chef.

Security and compliance:

`DevSecOps:` The process of integrating security practises in to Development and operation tasks.
Everyone becomes accountable for ensuring their solutions are secure.

What is InSpec?
 open source project for tesing infrastructure and applications.
 InSpec containre per-defined resurces & support for custom resources.
 To audit platform of the environment as part of governence.
 Community profiles available for auditing profile from chef supermarket.
 
 No need for Inspec code to be present within the cookbooks, we can create a profile. 
 
```
sample/app-profile
 |_ Reamdme.md
 |_ controls
    |_ application1.rb
 |_ libraries
     |_ custom-resource.rb
 |_ files
     |_ services.yml  
 |_ inspec.yml [ Contains the profile description, dependencies and supported platform ]
     
     
     application.rb - contains the test
     files/services.yml - contains files, static resource needed for testing. 
     
```
InSpec Auditing Scenarios:
  Test hardening Windows and Linux OS against official CIS benchmarks.
  Test whether Azure resources are deployed and configured correctly.
  Test database server config angainst DevSec baselines. (dev-sec.io)
  
 InSpec and Chef Automate:
   Scan results can be sent to Chef Automate instance or via Chef server.
   Compliance results are visible as a UI dashboard within chef automate.
   Chef automate can centralize the results if multiple chef server is running.
   use Audit cookbook, [Git](https://github.com/chef-cookbooks/audit)
```

 ```
 # command used to directly execute the inspec to the corresponding nodes. Regardless of where the system is located, if we have access using ssh in this case we can execute the command to audit.
 # vagarant:vagrant (user and password default)
 # 192.168.1.3322 ip-address and redirect port
 # last parameter is the inspec test location. we can use the git location in here too.
 
  $ inspec exec -t ssh://vagrant:vagrant@192.168.1.33:22 .\test\integration\default\packages_test.rb
  
  inspec tool doesn't require any pre-requistes like ruby to installed, only requires an connectivity
 ```
  
  
  
