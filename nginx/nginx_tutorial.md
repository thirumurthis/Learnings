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
 
  **Note**: By default the `/var/nginx/sites-enabled/default` config will be set with a symbolic link.
 
 --------------------------
 
 ### How does nginx configuration works, basics.
 
 - configuration terminology
   - Directives - simple directives and block directives (represented using {}).
   - simple directives, consits of name and parameter seperated by spaces and ends with ";".
   - block directives, has the same structure as simple directives but parameter is enclosed with {}. Also, doesn't ends with ";".
   - if a block directives can have other directives inside braces, it is called **`context`**. (context example: events, http, server and location)
 - Directives placed in the configuration file outside of any contexts are considered as main cotnext.
    - The `events` and `http` directives reside in the main context.
    - `server` in `http` context.
    - `location` in `server` context. 
  - `#` indicates comment.
 
 #### Serving static content.
  - Lets understand how the configuration works in nginx server.
  - With the default setup, from docker sandbox environment, Edit the `/etc/nginx/sites-available/default` file.

- Lets serve up the html file from different directory `/data/www`
   - create a directory /data/www using `mkdir -p /data/www`.
   - create a index.html, with some comment say, `<html><h3> from /data/www/index.html </h3></html>`
   - create a directory /data/images using `mkdir /data/images`, download some image from git repo (with raw url).

#### Now lets update the default configuration (`/etc/nginx/sites-available/default)` with below content
```
server {
   location / {
     root /data/www;
     }
  }
```
- Since configuration is updated we need to reload the server
```
$ nginx -s reload
## sending a reload signal
```
NOTE: {} - is referred as block directive.
- in the above configuration we are saying nginx to use /data/www.
- when the request `curl http://localhost:80/` (within the docker container is used) the html page from the `data/www/index.html` will be served.
- since we have started the container with 8080:80 port forwarding, from the host machine use `http://localhost:8080/`
- `by default nginx listens to 80 port`, so we haven't sepcified listen directive here.

- Note: install `vim`, `curl`, `wget` utility using `apt install <utility>` which will be helpful.
 
##### Lets serve up the image from the `/data/images` directory, say the image file name is `**hello.png**`. update the configuration as below

```
server {
   location / {
      root /data/www;
   } 
   location /images/ {
      root /data;
   }
```
  - Now the request `http://localhost:80/images/hello.png` will serve up the image from /data/images/hello.png

  - Any issues, the logs can be found at `/var/log/nginx/error.log` in some case `/usr/local/nginx/logs`
---------------------------------------

### Setting up simple Proxy server, using nginx configuration

 - Simple proxy server, mean a server that receives requests, passes them to the proxied servers, retrieves responses from them, and sends them to the clients.
 
 - we define a proxy server, by using `server` block (derivaties).
 - The configuration can contain more than one `server` block.
 - Add the below configuration along with the static html configuration from previous section.
 
 ```
 server {
    listen 8080;
    root /data/up1;
    
    location / {
    }
 ```
 - Execute `nginx -s reload`
 - From docker, use `curl http://localhost:8080/` to view the index.html content from the /data/up1.
 - above configuration is a simple server listening on 8080.


#### Lets setup proxy server configuration, update the configuration file to add below content
```
## below is the configuration for listening to 8080 port
server {
  listen 8080;
  root /data/up1;
  location / {
  }
}

## Proxy configuration - any request coming to the 80 port will be proxied.
server {
  location / {
     proxy_pass http://localhost:8080;
   }
   
   location /images/ {
       root /data;
    }
  }
```
- As explained in the configuration now from the docker user `curl http://localhost:80/` the request will redirect and print the index from the /data/up1/index.html.

- The `location /images/` can be modifed to use regex, in below case we can check the extension and server up the data. The configuration now looks like below.

```
server {
  listen 8080;
  root /data/up1;
  location / {
  }
}

server {
   location / {
      proxy_pass http://localhost:8080;
    }
    ## configuration to use extension
    
    location ~ \.(gif|jpg|png)$ {
      root /data/images;  ## note the directory now changes to /data/images
    }
 }
```
 - With the above configuration, the hello.png can be rendered when using `wget http://localhost:80` from docker container. 
 - From host/laptop use `http://localhost:8080` in the browser to display the image (since we have started the docker container wiht port forwarding 8080:80)

-----------------

#### More directives, used in configuration file and understanding.

- Simple config, alternte approach, `unlink` the default config under `/etc/nginx/sites-available/`.
- Create a config file under `/etc/nginx/conf.d`, name it demosite.local.conf. Note: in this directory, the conf file should end with `.conf`.

```
server {
  listen 80;
  root /var/www/demosite.local; ## this will be path where the html,js files present.
}
```
 - Test the configuration using `nginx -t`.
 - reload the configuration using `nginx -s reload`.
 - Create the diractory `/var/www/demosite.local/` add index.html with simple content.
 - use curl command, to check the output using `curl http://localhost`

#### `default_server`, `server_name`, `index` directive.
  - nginx can server multiple files from the same IP address, we use **`default_server`** directive to tell nginx to use this server configuration when no other configurations match the name of the site being requested.
  - when using the `default_server` we need to add, the name of the server. This is also the name of the site.
  - Also including **`server_name`** will let nginx know which sites the server configuration applies to.
  - Else, when we are serving multiple sites from same ip address, nginx might serve the wrong content.
  - **`index`** - directive tells the nginx server which file to serve first for a request, the default one is index.html.
```
server {

   listen 80 default_server;
   
   server_name demosite.local www.demosite.local
   
   ## we are telling nginx to use index.html index.htm
   ## index will use the very first file it encounters from root directory
   index index.html index.htm index.php; 
   
   root /var/www/demosite.local
```
   - perform `nginx -t` and `nginx -s reload`, to validate and apply the configuration.
   - make sure to copy the website content, with the index.html to the `/var/www/demosite.local` folder in the container.
 
 Note: 
   - To copy file, when starting the container, use volume mount option `-v <path_in_host>:<path_in_container>` 
   - If using docker and port forwarded 8080, then use http://localhost:8080 on the browser.
 
 #### Adding `location` directives, for making configuration more roboust.
  - syntax usage of location directives
 ```
 server { 
  location [modifier] location_defintion { ## modifier is optional indicated using []
     location [modifier] location_defintion {  ## nested location block
     }
   }
 }
 ```
  - location directives are defined in server block.
  - inside location block, we can define any directives used within server directives.
  - location directives are useful, were can process requests without the needs of multiple server blocks. Thus we can proecss different request, in way to similar to server block in a way using location block.
  - when nginx process a location, tries to perform exact match, location with prefixes and location with regex.

 - location block within the above demosite.local config, using more additional directives
 - **`try_files`** directive gives nginx a set of files or directries to look for relative to location. the first file or directory that matches gets processed , if no items in the list is matched then the last item in the list is used as uri or error code.
 - **`auto_index`** directive, this disabled by default, when enabled using keyword on, the url will list the files in the browser within that directory.
 - **`error_page`** directive, this is used to display custom error pages.
 - **`internal`** - tells the nginx to process any redirects to custom 404 pages as internal redirects. This helps to serve the 404 pages quickly.
```
server {
  listen 80 default_server;
  root /var/www/demosite.local;
  server_name demosite.local www.demosite.local;
  index index.html index.html index.php;
  
  location / {
     try_files $uri $uri/ =404;  ## =404 tell server to display 404 when no file matched.
  }
  
  location /images {
    autoindex on;
  }
  
  ## when nginx cannot find any content to display for a request
  ## it displays a 404 error page
  ## if there are any server side issues, nginx displays 500+ codes
  
  ## below is to tell the nginx to display the custom pages instead of default
  ## when 404 or 500 exception occurs 
  ## for the custom pages, a location directive is added with the = modifier
  ## to sepcify exact match
    
  error_page 404 /404.html;
  location = /404.html {  ## = is the modifier know as exact match modifier
    internal;
    }
  
  error_page 500 502 503 504 /50x.html;
  location = /50x.html {
    internal;
  }
  
  ## Since there are no logic in the server side, to throw the 500 error code
  ## we have to manipulate it using fast CGI pass
  
  location = /500 {
     fastcgi_pass unix:/this/will/fail;
  } 
}
```
 - since we have the `/images` location defined in configuration with auto_index directive. the permission in the linux box should be appropriate for files use `chmod 644` and for directory use `chmod 755`.

 - in order to display 404 and 500 errors with custom file, use `http://localhost:8080/somepage` and `http://localhost:8080/500` (from local host/ laptop).
 - Make sure the file 404.html and 50x.html, exists in the file path.

- For loading hte images url, make sure to append '/'. `http://localhost:8080/images/`

 ------------------------
 
 ### How to identify logs in nginx server.
  - In the existing `/etc/nginx/nginx.conf` the error logs are configured to `/var/log/nginx/` location with access.log (access info, like ip address, path of the file served, etc) and error.log file (contains operation details).
  

#### How to customize the logging in nginx for sample application.
  - Lets set the logs for the demosite.local.
  - Edit the configuration file, `/etc/nginx/conf.d/demosite.local.conf`
  - if we use the default log file and we have multiple application hosted in nginx, all the site/application logs will be added to the default log which will become cumbersum to handle.
  - we can specify logs 
 
```
...
access_log /var/log/nginx/demosite.local.access.log;
error_log /var/log/nginx/demosite.local.error.log;
...
```
#### The complete config file with the error and access log configured, with reference to demosite.local application
  
```
server {
  listen 80 default_server;
  root /var/www/demosite.local;
  server_name demosite.local www.demosite.local;
  index index.html index.html index.php;

  access_log /var/log/nginx/demosite.local.access.log;
  error_log /var/log/nginx/demosite.local.error.log;
  
  location / {
     try_files $uri $uri/ =404;
  }
  location /images {
    autoindex on;
    access_log /var/log/nginx/demosite.local.images.access.log;
    error_log /var/log/nginx/demosite.local.images.error.log;
  }
  error_page 404 /404.html;
  location = /404.html {
    internal;
    }
  error_page 500 502 503 504 /50x.html;
  location = /50x.html {
    internal;
  }
  location = /500 {
     fastcgi_pass unix:/this/will/fail;
  }
}
```

### Troubleshooting the nginx server
  - using `nginx -t` will verify the configuartion.
  - use `nginx -s reload` or `systemctl reload nginx` if the nginx is not running
  - Check if the port is not being used, with below commands,
    -  `sudo lsop -P -n -i :80 -i :443 | grep LISTEN`
    -  `sudo netstat -plan | grep nginx` => gives the id of the process listening on port
  - check the logs at `/var/log/nginx/*`
----------------------

### Using nginx configuration for fastCGI proxy.
- Installing php
```
$ apt install php-fpm php-mysql

## php-fpm => fastCGI process manager and required dependencies manager
           => this will be istalled as service, this fastCGI service will process any php file recieved by the web server.
           
## php-mysql => is package allows to connect to my sql database 
```

- verify using below commands
```
$ php --version
$ systemctl status php*  [type tab to auto fill the service name]
```

- since the php installed in order to serve the php files, we need to update the configruation file. (demosite.local.conf)
- with exising content in the previous demosite.local.conf add below location directive. place it anywhere within the server block directives. (i am placing above the error_page directive)
- **`include`** directive is used to bring in the code that are outside the config folder. 
- **`fastcgi_pass`** => this directive tells the nginx to use unix socket, communicating with the unix socket is much faster.
- **`fastcgi_intercept_error`** => this tells nginx, any error returned by the fastCGI server (here it is php-fpm), the nginx should process this error within the `error_page` directive  
```
...
location ~ \.php$ {
    # check within /etc/nginx/ folder, the fastcgi-php.conf file will contain 
    # helpful directives for performance and security.    
   include snippets/fastcgi-php.conf; 
   fastcgi_pass unix:/var/run/php/php7.2-fpm.sock;
   fastcgi_intercept_errors on;
}
...
```
- Lets write some php code to verify
   - below will list the php info (not recommended to be used in production)
   - store the content in a file under the `/var/www/demosite.local/info.php`
```
 <?php phpinfo(); phpinfo(INFO_MODULES); ?>
```
- reload the nginx server using `nginx -s reload`;
- 

