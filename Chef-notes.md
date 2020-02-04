
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
