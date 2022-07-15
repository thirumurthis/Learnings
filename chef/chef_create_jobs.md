### To create kinfe job

In the cookbook, in an recipe file, we need to update the 

- chef-client.rb

```
default['chef_client']['config']['chef_server_url']= 'https://...'
default['chef_client']['config']['validation_client_name']= 'name-for-validate'
default['chef_client']['config']['validation_key'] = '/path/to/chef/certificate.pem'
default['chef_client']['config']['log_level'] = ':debug'

debug['chef_client']['cron']['user_cron_d'] = true
debug['chef_client']['cron']['log_file'] = 'filename_for_leg'
debug['chef_client']['cron']['append_log'] = false
debug['chef_client']['cron']['hour'] = 10
debug['chef_client']['cron']['minute'] = 0
debug['chef_client']['cron']['splay'] = 1200

# simple custom config
if node['hostname'] == 'node-1'
  default['chef_client']['cron']['weekday'] = 1
end

# define list of jobs

default['push_jobs']['whitelist'] = {
  'reload-deamon' => 'systemctl daemon-reload',
  'restart-chef-client' => 'chef-client',
  'restart-artemis' => 'sudo su - artemis-user -c \'. /etc/broker/bin/artemis restart\' ',
  'stop-app1' => 'sudo su - <user>  -c  \' cd ~/path; script_to_stop_app.sh stop \' '
}

```

with the above in a cookbook, and deployed from knife we can issue below command to stop or restart process

```
knife job start restart-artemis <node1> <node2>
```
