In this blog will demonstarte how to use memchace in an simple python application to store the static data


I am using Oracle Cloud, and created an instance of Oracle Linux.

Note:-
   - I am not going to detail the steps to create the Oracle Cloud, I have created an Oracle Linux instance and setup the SSH keys in Putty so i can access from my Local


First install the memcache using 

```
sudo dnf install memcached
```

![image](https://user-images.githubusercontent.com/6425536/183714745-ff5aed09-6399-4537-b7dc-b16f70da806b.png)


Install the python package `pymemcache` using below command

```
sudo pip3 install pymemecache
```

![image](https://user-images.githubusercontent.com/6425536/183715412-88995960-48df-489f-ad0d-85daff160cb6.png)

Refer [Oracle documentation for installing memchaced on Oracle Linux](https://docs.oracle.com/cd/E17952_01/mysql-5.6-en/ha-memcached-install.html)

- Now we need to start the memcache service, intially the service will be disabled, this can be checked with below command

```
sudo systemctl status memcached
```

![image](https://user-images.githubusercontent.com/6425536/183717131-ed2287c1-ae62-4b7c-afaa-fdcb104c86cc.png)

- Start the service using `sudo systemctl start memcached` and check the status once again using above command. Refer the snapshot

![image](https://user-images.githubusercontent.com/6425536/183717819-2b69acb7-8ec9-4db3-a47a-0eddce1ea34b.png)


### if we wanted to flush all the values from the cache, we can use below command

```
$ echo "flush_all" > nc localhost 11211
```

![image](https://user-images.githubusercontent.com/6425536/183742561-e63516db-5f1d-4871-b3ff-29f392b27b12.png)
