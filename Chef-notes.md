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
Tool is used by chef DK to get the system information. 
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

file node['lamp']['hello_path'] do
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
 
 ![image](https://user-images.githubusercontent.com/6425536/74597373-4cb8c680-5013-11ea-82e1-7cb326857b8c.png)
