Sample of how to create active mq aretmis service (systemd) for rhel using chef

```
systemd_unit 'artemis.service' do

  content <<~EOU

  [Unit]

  Description=Apache ActiveMQ Artemis
  After=network.target

  [Service]

  Type=forking
  User=root
  Group=root
  Restart=always
  StandardOutput=journal
  StandardError=journal



  ExecStart=/opt/artemis/broker1/bin/artemis-service start
  ExecStop=/opt/artemis/broker1/bin/artemis-service stop

  [Install]
  WantedBy=multi-user.target
  EOU

  action [:create, :enable, :start]
end

```

use `action [:create, :disable, :delete] ` to delete the created service file.

----
 - To execute above in local use

```
# navigate to the cookbook directory, where you can have the file under recipe/systemExample.rb
$ sudo chef-client -z -o "recpie[demo::systemExample.rb]" -l debug
```

To validate :
 - use ` sudo systemctl status artemis.service` where you should see it is working.
