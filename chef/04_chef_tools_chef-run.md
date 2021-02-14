#### using `chef-run` command
  - This is a utility to run adhoc Chef Infra commands on a remote server. 
  - It provides a good way to get started with Chef Infra, without requiring any infrastructure beyond SSH.
  

#### If docker instance is setup (refer [link](https://github.com/thirumurthis/Learnings/edit/master/chef/01_working_with_chef_notes.md)), we can use below commands.
 - After installing the docker
 - Download the docker-compose and start the container. exec into workstation container and follow below steps.
 
 - `chef-run` command that will directly execute the resources in the container web1
```
$ chef-run web1 file hello.txt
### web1 - is the target node
### file - is the resource type
### hello.txt - is the file name that will be created in the web1 node.
```

- chef-run command, to create a file resource with content
```
$ chef-run web1 file hello.txt content="hello world"
```
 - ssh into the web1 container view the content of that was created in web1 node use
```
$ ssh web1 cat /hello.txt

$ ssh web1 ls -l /hello.txt
```

- delete the file created using chef-run

```
$ chef-run web1 file hello.txt action=delete
```

##### Executing recipe using chef-run
  - create a recipe file as below and save it as recipe.rb
```rb
apt_update

package 'figlet'

directory '/tmp'

exectute 'print_hello_world' do
   command 'figlet Hello World!!! > /tmp/hello.txt'
   not_if { File.exists?('/tmp/hello.txt') }
end
```

- To execute the above recipe using `chef-run`
```
$ chef-run web1 recipe.rb
```

- After isntallation check the content using below command
```
$ ssh web1 cat /tmp/hello.txt
```

-----------------------
#### To execute a cookbook that was generated using `chef generate cookbook webserver`.
 - The name of the cookbook is webserver
 - with the cookbook template directory structure in place, below is the recipe.rb file content
 ```rb
 apt_update
 
 package 'apache2'
 
 template '/var/www/html/index.html'
    source 'index.html.erb'
 end
 
 service 'apache2' do
    action [:enable, :start]
 end
 ```
 - index.html.erb (template file)
 ```
 <h3> chef hello </h3>
  Host name : < % =node['hostname']% >
 ```
 
 - To execute the cookbook using `chef-run`
 ```
 $ chef-run web1 webserver
 
 ### to execute on more than one node
 $ chef-run web[1:2] webserver
 ```
 
 - Verify the server setup on the nodes.
   - will display the index.html converted content.
 ```
 $ curl web1 
 $ curl web2
 ```
 
 -------
 #### Setting up a `load balancer` in front of the web1/2 nodes
  - Create a cookbook `chef generate cookbook loadbalancer`
  - Use below is the content of the `default.rb`
 
 ```rb
 apt_update
 
 package 'haproxy'
 
 directory '/etc/haproxy'
 
 template '/etc/haproxy/haproxy.cfg'
 
 template '/etc/haproxy/haproxy.cfg' do
    source 'haproxy.cfg.erb'
 end
 
 service 'haproxy' do
    action [:enable, :start]
 end
 ```
 
 - Under template create a file haproxy.cfg.erb, with below content
 ```
 global
    log         127.0.0.1 local2

    chroot      /var/lib/haproxy
    pidfile     /var/run/haproxy.pid
    maxconn     4000
    user        haproxy
    group       haproxy
    daemon

    stats socket /var/lib/haproxy/stats

defaults
    mode                    http
    log                     global
    option                  httplog
    option                  dontlognull
    option http-server-close
    option forwardfor       except 127.0.0.0/8
    option                  redispatch
    retries                 3
    timeout http-request    10s
    timeout queue           1m
    timeout connect         10s
    timeout client          1m
    timeout server          1m
    timeout http-keep-alive 10s
    timeout check           10s
    maxconn                 3000

frontend  main
    bind                 *:80
    acl url_static       path_beg       -i /static /images /javascript /stylesheets
    acl url_static       path_end       -i .jpg .gif .png .css .js

    use_backend static          if url_static
    default_backend             app

backend static
    balance     roundrobin
    server      static 127.0.0.1:4331 check

backend app
    balance     roundrobin
    server web1 web1:80 weight 1 maxconn 100 check
    server web2 web2:80 weight 1 maxconn 100 check
 ```

##### Note: The recipe.rb in webserver cookbook and default.rb in loadbalancer cookbook are mostly similar but with different packages.

 - Within the docker compose, there is node lb which acts as a load balancer.
 - To execute the cookbook use below command
 
```
$ chef-run lb loadbalancer
## note lb is the node name.
```
 - Creating dynamic configuration using chef infra refer [link](https://docs.chef.io/server/)

- To verify use below command
```
$ curl lb 
### issuing the above command couple of times will hit web1 or web2 check the content of index.html where hostname is printed
```

- To remove all the setup from docker use  (exist from workstation container)
```
$ docker-compose down
Stopping workstation                  ... done
Stopping try-docker-chef-infra_web1_1 ... done
Stopping try-docker-chef-infra_web2_1 ... done
Stopping try-docker-chef-infra_lb_1   ... done
Removing workstation                  ... done
Removing try-docker-chef-infra_web1_1 ... done
Removing try-docker-chef-infra_web2_1 ... done
Removing try-docker-chef-infra_lb_1   ... done
Removing network try-docker-chef-infra_default

## to remove the images use below command
$ docker-compose down --rmi all 
```
