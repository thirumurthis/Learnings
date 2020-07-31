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
