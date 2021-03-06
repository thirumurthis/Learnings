### Below resources can go within the recipes itself.

##### To execute a command and upate the yum

```ruby
execute 'centos_yum_update' do
  command 'sudo yum -y update --exclude=<package-name>*'  #for example if we need not update openssl, use yum list to identify the package name; * - wildcard
  ignore_failure true
end
```

##### To install the ruby package
```ruby
package 'ruby'
package 'rubygem-nokogiri' do
  not_if { node['hostname'] == 'domain1' }
end
```

##### To update yum_repoistory with the custom url
```ruby
yum_repository 'ms-repo' do
  description 'Cutom RPM Microsoft Repository'
  baseurl 'https://packages.microsoft.com/yumrepos/azure-cli'
  gpgkey 'https://packages.microsoft.com/keys/microsoft.asc'
  action :create
end
## Then install the package from RPM
package 'azure-cli'
```

#### Update the content using bash resources, below is just a sample not the exact way to setup
```ruby
bash 'update_content_disk_agent1' do
  code <<-EOF
  cat /etc/diskagent.conf | sed -e 's/EnableSwap=n/EnableSwap=y/' -e 's/SwapSizeMB=.*/SwapSizeMB=8192/' > /tmp/diskagent.conf
  cp /tmp/diskagent.conf /etc
  EOF
  not_if { node['roles'].include?('dev-frontend') || node['roles'].include?('dev-backend')}
end
```

##### Create custom account for application - group
```ruby
group 'myapp' do
  system true
  gid 30002
  members ['myapp-admin']
end
```
#### create group
```ruby
user 'appuser' do
  comment 'MYAPP Account'
  system true
  shell '/bin/bash'
  home '/home/myapp'
  uid 30002
  gid 30002
end
```

#### Disabling services in linux services
```ruby
# Create a service for activemq and set it up first
# the same approach can be used for other services too

execute 'disable_activemq_service' do
  user 'activemq'  # other possible users like root, etc.
  command 'systemctl disable activemq.service'
  returns [0,1]
  retries 3
  ignore_failure true
end
```

#### How to create a service in Linux [Link](https://scottlinux.com/2014/12/08/how-to-create-a-systemd-service-in-linux-centos-7/)

 - Create a user (in this example a activemq) `sudo adduser activemq -s /sbin/nologin`
 - Create a service file at `/etc/systemd/system/activemq.service` and add the following content
```
[Unit]
Description=activemq Service
After=network.target

[Service]
Type=simple   # other possible value is Forking
User=activemq
Group=activemq
ExecStart=/opt/apache-activemq/bin/activemq start
ExecStop=/opt/apache-activemq/bin/activemq stop

Umask=007
RestartSec=10
Restart=on-abort  # other possible value is Always  

[Install]
WantedBy=multi-user.target
```
  - Reload the `systemctl` dameon process `$ sudo systemctl daemon-reload`
  - start the service `$ sudo systemctl start activemq.service` 
 Note: The /opt/apache-activemq/bin should be accessible by the activemq user.
 
 #### Options to rotate logs
 ```ruby 
 logrotate_app 'rotate-log-myapp' do
  frequency 'daily'
  path      '/var/log/myapp/application.log'
  options   ['compress']
  rotate    5
  create    '644 root root'
  postrotate 'kill -3 myapp  >> /var/log/myapp/rotated_log_status.log 2>&1'
end
 ```
 
 ##### Setting up cron job using resources
 ```ruby
 # variable to hod the path
 
 cwd_path = ::File.join('/home', 'myapp')
 cron_d 'cron_monitor_myapp_stat' do
  action (node['tags'].include?('stats_enabled')) ? :create : :delete
  minute '0'
  hour '*/5'
  user 'myapp-user'
  command "(export CWD_PATH=#{cwd_path};. $CWD_PATH/shell/myapp-monitor.sh) > /tmp/logs/myappmonitor-cron.out 2>&1"
end
 ```
 
 ##### Edit a file and update the content using ruby block within chef recipe [link](https://stackoverflow.com/questions/52056832/use-chef-file-insert-line-if-no-match-method-for-cidr-address)
   - Doc `#insert_line_if_no_match(regex, newline) ⇒ Object`  [Link for doc](https://www.rubydoc.info/gems/chef/Chef%2FUtil%2FFileEdit:insert_line_if_no_match)
 ```ruby
 ruby_block 'update content' do
  block do
    file = Chef::Util::FileEdit.new("#{node['myapp']['index']}/index.html")
    file.insert_line_if_no_match('Version app-v-01', 'Version app-v-0N')
    file.write_file
  end
  not_if { node['roles'].include?('dev-node')}
   only_if { node.chef_environment != 'dev-frontend' }  # we can use && operator too over here.
end
 ```
 
 ##### Restart servcice using chef recipe
 ```ruby
 service 'activemq' do
  action [:enable, :restart]
end
 ```
 ##### Working with templates in chef recipe, executes the tempalte if the file doesn't exists ignores otherwise
   - The variabels are passed as ruby hash/dictionary and simple value
 ```ruby
 template '/home/myapp/test.properties' do
   source 'test.properties.erb'
   owner 'myapp-user'
   group 'myapp'
   variables(
      con: { "variable1" => "TRUE-NEWVALUE", "variable2" => "TRUE"},
      from: node['fqdn']
)
   not_if "test -f /home/myapp/test.properties"
end

## TEMPLATE FILE under ~/chef-repo/cookbooks/starter/template/test.properties.erb

## Below is the content of template file and note the usage of @con['variablename'], for accessing fata.
## Since the ruby hash is used, below is the way to access it. In a way if we pass the databags info we need to use the same approach.

key1 =TRUE/ <%= @con['variable1'] %>
key2 =TREE/ <%= @con['variable2'] %>
from= <%= @from %>
```
##### Template another example of passing variable using direct hash
```ruby
hostnamevalue=node['hostname']
portvalue=8080
template '/etc/testproperties.xml' do
  variables(
    'hostname_info' => hostnamevalue ,
    'web_port' => portvalue
  )
  source 'testproperties.xml.erb'
  owner 'myapp-user'
  group 'myapp'
  mode '0755'
end

## The template file is located under ~/chef-repo/cookbooks/<cookbook-name>/template/testproperties.xml.erb

# below content is part of the template file, use <%= @variablename %> for accessing passed variables in template
# resource the values are passed as => not using : refer the previous example

 <web bind="http://<%= @hostname_info %>:<%= @web_port %>" path="web">
```

## Template file and passing databags configuration
```ruby
dbag_data = data_bag_item("myapp-databag-dev", 'dev')  # the directory of the databags and id value of the databags is passed as parameter

template '/opt/apache-activemq/bin/profile.cmd' do
  source 'profile.cmd.erb'
  owner 'myapp-user'
  group 'myapp'
  mode '0755'
  variables ({
    conf: dbag_data
})
end

## Template file content:
Environment=<%= @conf['property1'] %>

## databag content under data_bags/myapp-databag-dev/devconfig.json
## not the databag can contain json object within a variable.
{
  "id": "dev",
  "property1": "valuefromdatabag",
 ....
  "proeprtyn": { "key1":"value1", "key2":"value2"}
 }

```

#### chef recipe for appending a string to a file if it not exists
   - The JAVA_ARGS are already inclded, we need to add to the end of the file
   - just using the >> redirection here.
   - resource executes only when the files exists and if there is no matching string starting with JAVA_ARGS..
```ruby
filepathloc='/opt/apache-activemq'
execute 'args_to_file' do
   command "echo 'JAVA_ARGS=\"$JAVA_ARGS -Djava.rmi.server.hostname=localhost\"' >> " + filepathloc +"/etc/profile.cmd"
   only_if 'test -f ' + artemis_broker_instance + '/etc/artemis.profile'
   not_if "grep -q '^JAVA_ARGS=\"$JAVA_ARGS -Djava.rmi.server.hostname=localhost\"' " + filepathloc +"/etc/profile.cmd"
end

## usd grep -q for quite print
```

##### create a link using recipe chef resources and File utility of ruby
 ```ruby
 # link for a file
 link '/opt/activemq' do
  owner 'activemq'
  to '/opt/apache-activemq-5.10.0/bin/activemq'
  only_if { ::File.exist?('/opt/apache-activemq-5.10.0/bin/activemq') }  # execute this resource only if the file exists
end

# link for a folder
link "/opt/apache-activemq" do
  to "/opt/activemq-5.11.0"
  owner 'activemq'
  group 'activemq'
  mode '0755'
  not_if 'test -L /opt/apache-activemq'  # don't execute this resource if the link already exists
  action :create
end
 ```
