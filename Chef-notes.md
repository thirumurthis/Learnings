### Chef

Ruby library are called as gem's

`Recipe` - is made up of ingredients, called chef resources.
`Resources` : are fundamental units of chef.
 Resource contains different elements
     - Type
     - Name
     - Properties
     - Action
  
 ```
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
