### Running Apache ArtemisMQ in Docker Desktop

In this blog will walkthrough,
  - How to run Apache Artemis broker instance in Docker Desktop.
  - How to configure Nginx server as reverse proxy to access the Aretmis Management UI console (Hawtio).

- It is easy to spin up the Docker Artemis instance with the `artemiscloud.io` image. Thanks to [Redhat community](https://artemiscloud.io/community/), they have created an Apache Artemis image. In this blog I am using that image which is available in [quay.io repository](https://quay.io).

#### Why we need reverse proxy? 

- Challenge is that accessing the Management UI Console as Apache Artemis uses `jolokia-access.xml` for security which restrict the Management UI aacess.By default the CORS is enabled.
- Usually we can edit this file based on our requirement, but it is difficult to edit in this base image.
- Also, there are no easy way to configure jolokia.

- I udpated `jolokia-access.xml` and tried to mount as volume to docker container, this didn't work. 
- The `/home/jboss/broker/etc/` path is overridden and existing artemis shell is not available in that path, so docker instance is not starting up.

Refer my [stack-overflow link](https://stackoverflow.com/questions/72672565/activemq-artemis-not-displaying-the-web-console-when-run-in-docker) for more details 

Lets dive in to the details.

### Running Artemis Docker instanse

- We can use below docker command to create the Docker instance.

```
docker run -e AMQ_USER=admin -e AMQ_PASSWORD=admin -p8161:8161 -p61616:61616 -p5672:5672 --rm --name artemis quay.io/artemiscloud/activemq-artemis-broker
```

Note: 

  - Exposing the TCP and AMQP ports so we can access the Artemis broker from the host machine.
  - Now, if we issue `http://localhost:8161/console` in browser we will notice a login page further we will not be able to see the queues, consumers, etc information. 

Refer below snapshot.

![image](https://user-images.githubusercontent.com/6425536/175756577-04188071-442a-4546-9b97-24e341b95c45.png)


#### Creating reverse-proxy using Nginx server

- In order to bypass the `jolokia-access.xml` security configuration we will be using the nginx server as proxy.
- Since the Nginx server instance is also running in the same network, we can easily configure the reverse proxy.

##### Run an instance of nginx in docker

```
> docker run -d --name nginx-proxy -p 80:80 nginx
```

###### Copy the default.conf file from the conatiner to our local Desktop. I created a temp directory and `cd` to that folder.

```
> docker cp nginx-proxy:/etc/nginx/conf.d/default.conf .
```

###### Get Aretmis docker conatiner hostname/ipaddress.
  - We will use this to updated in the `Origin` header in the default.conf file.

```
> docker exec nginx-proxy hostname -i
```

###### Content of the default.conf file

- In this `default.conf` I am enabling the CORS by setting the headers for different HTTP methods
- Also, included the header `Origin` with the Artemis docker container ipaddress (in my case it was 172.17.0.2).

```
server {
    listen       80;
    listen  [::]:80;
    server_name  localhost;

    location / {
     if ($request_method = 'OPTIONS') {
        add_header Origin http://172.17.0.2;
        add_header 'Access-Control-Allow-Origin' '*';

        add_header 'Access-Control-Allow-Credentials' 'true';
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS';
        add_header 'Access-Control-Allow-Headers' 'DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';

        add_header 'Access-Control-Max-Age' 86400;
        add_header 'Content-Type' 'text/plain charset=UTF-8';
        add_header 'Content-Length' 0;
        return 204; break;
     }

     if ($request_method = 'POST') {
        add_header Origin http://172.17.0.2;
        add_header 'Access-Control-Allow-Origin' '*';
        add_header 'Access-Control-Allow-Credentials' 'true';
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS';
        add_header 'Access-Control-Allow-Headers' 'DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';
     }
     if ($request_method = 'GET') {
        add_header Origin http://172.17.0.2;
        add_header 'Access-Control-Allow-Origin' '*';
        add_header 'Access-Control-Allow-Credentials' 'true';
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS';
        add_header 'Access-Control-Allow-Headers' 'DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';
     }

      proxy_pass http://172.17.0.2:8161/;
      proxy_set_header Origin http://172.17.0.2;
      proxy_set_header Host      $host:$server_port;
      proxy_set_header X-Real-IP $remote_addr;
    }

    #error_page  404              /404.html;
    # redirect server error pages to the static page /50x.html
    #
     error_page   500 502 503 504  /50x.html;
     location = /50x.html {
        root   /usr/share/nginx/html;
     }
}
```
- The updated `default.conf` file should be copied to the container.

```
> docker cp default.conf nginx-proxy:/etc/nginx/conf.d/default.conf
```

Note: 

  - Modified configuration can be updated when the Nginx server is running, we can issue a reload singal to inform Nginx to use the updated server config while running.
  
- Lets validate the update default.conf file is valid with below command

```
> docker exec nginx-proxy nginx -t
```

- We send reload singnal to inform Nginx to use the updated config with below command.

```
> docker exec nginx-proxy nginx -r reload
```

#### Output:

- Once updated, form the browser in host machine use the link `http://localhost:80/console` to view the queues, consumer, etc. details. Refer below snapshot 

![image](https://user-images.githubusercontent.com/6425536/175756631-acf007e5-08e4-44e8-8a2e-632935b1a161.png)

Reference Link:

  - [How to setup nginx reverse-proxy](https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/Docker-Nginx-reverse-proxy-setup-example)
