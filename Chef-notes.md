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

### `Ohai` tool is used by chef DK to get the system information. For example, ubuntu used apt-get package manager and centos uses Yum package manager, chef-client knows to use the appropriate package manger using the ohai utility

Ohai is a command line utility for getting the system inventory, which is of json format.

Using the `jq` utility you can read through json.
```unix
# command to use to get memoru

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
