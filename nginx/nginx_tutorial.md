### Nginx server

#### Setting up a sandbox environment.

**with Vagrant and VirtualBox**:
   - A sandbox Ubuntu server can be setup, using Vagrant, virtualBox. 
     - Then the nginx server can be installed using `apt install ngnix`.
     - The nginx is installed as service. To verify the status use following commands or use browser to login to `http://<ip-of-vagrant-box>:80/`
     - Commands `systemctl status nginx` or `systemctl status nginx --no-pager` or `systemctl is-active nginx`, etc.

**With Docker**
   - with Docker installed and running, we can download the community image `ubuntu/nginx` and start the container to play with.
      - Once the container is downloaded, use `docker run -d --name nginx-container -e TZ=UTC -p 8080:80 ubuntu/nginx`. use `http://localhost:8080/`.
      - To run command in the container use, `docker exec -it nginx-container /bin/bash`.

#### Directory structure of the nginx:
  - The directory of nginx config will be available at `/etc/nginx`.
  - Below is the list of directories
     - `nginx.conf` - contains the server configuration which rarely gets updated.
     - `conf.d`, `sites-available`, `sites-enabled` -> are folders where the server configuration file are stores. This is similar to the virtual host file used in apache.
         - vhost and server configuration are the terms used interchangably.
     - An example server configuration is stored in `/etc/nginx/sites-available/default` => this setsup the `welcome to nginx` page and serves up. 
     - Two other folders 
        - `/var/log/nginx/` => This is the directory where the logs are stored by nginx server.
        - `/var/www/` => This is where the actual files that serves up to the client. default folder is `/var/www/html`. But we create under `/var/www`
  
```
root@07c645245ccc:/etc/nginx# ls -lrth
total 64K
-rw-r--r-- 1 root root  664 Feb  4  2019 uwsgi_params
-rw-r--r-- 1 root root  636 Feb  4  2019 scgi_params
-rw-r--r-- 1 root root  180 Feb  4  2019 proxy_params
-rw-r--r-- 1 root root 1.5K Feb  4  2019 nginx.conf              (*)
-rw-r--r-- 1 root root 3.9K Feb  4  2019 mime.types
-rw-r--r-- 1 root root 2.2K Feb  4  2019 koi-win
-rw-r--r-- 1 root root 2.8K Feb  4  2019 koi-utf
-rw-r--r-- 1 root root 1007 Feb  4  2019 fastcgi_params
-rw-r--r-- 1 root root 1.1K Feb  4  2019 fastcgi.conf
-rw-r--r-- 1 root root 3.0K Feb  4  2019 win-utf
drwxr-xr-x 2 root root 4.0K Apr 21  2020 modules-available
drwxr-xr-x 2 root root 4.0K Apr 21  2020 conf.d                  (*)
drwxr-xr-x 2 root root 4.0K Nov 24 15:07 snippets
drwxr-xr-x 2 root root 4.0K Nov 24 15:07 sites-available         (*)
drwxr-xr-x 2 root root 4.0K Nov 24 15:07 sites-enabled           (*)
drwxr-xr-x 2 root root 4.0K Nov 24 15:07 modules-enabled

(*) - Indicates important files, which we review further.
```

### The nginx server, provides command line tools for various operation. 
    - The command line tool works as a root user
    
 ```
 $ nginx -h
 
root@07c645245ccc:~# nginx -h
nginx version: nginx/1.18.0 (Ubuntu)
Usage: nginx [-?hvVtTq] [-s signal] [-c filename] [-p prefix] [-g directives]

Options:
  -?,-h         : this help
  -v            : show version and exit
  -V            : show version and configure options then exit
  -t            : test configuration and exit
  -T            : test configuration, dump it and exit
  -q            : suppress non-error messages during configuration testing
  -s signal     : send signal to a master process: stop, quit, reopen, reload
  -p prefix     : set prefix path (default: /usr/share/nginx/)
  -c filename   : set configuration file (default: /etc/nginx/nginx.conf)
  -g directives : set global directives out of configuration file
 ```
    
#### To monitor the nginx service, using `systemctl`
```
$ systemctl start nginx
$ systemctl stop nginx
$ systemctl reload nginx   ## mostly we use this for any configuration updates, since it will make sure to perform a update without taking down the server
                           ## still the data will be served up
$ systemctl status nginx
```

#### Using the nginx command line
 - `nginx -t` are mostly used command.
 - The `-t` option will perform a testing on the configuration.
 - using `-t` will NOT stopping or reloading the service.

```
root@07c645245ccc:~# nginx -t
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```
 - `nginx -T` will test the configuration and prints it in the screen.
 - The `-T` option will be helpful, since nginx configuration are stored in different files, `-T` option will list them in one place.
 
#### the `nginx.conf` is the configuration file
  - most of the config files are C-syntax structure
  - uses {} - for block directive like below
  
  ```
  http {
   ...
   include /etc/nginx/conf.d/*.conf;
   include /etc/nginx/sites-enabled/*;
   ...
   }
  ```
  
  - The above include part is the directory where the configuration can be placed.
 
 ##### There are two ways to place the additional configuration to nginx
  - To create a `<some-name>.conf` file and place it at `conf.d` directory
  - To place the conf file in `sites-available` directory, and create a symbolic link in `sites-enabled` directory
 
 Note: By default the default config will be set with a symbolic link, which can be unlinked using `unlink` command.
 
 
