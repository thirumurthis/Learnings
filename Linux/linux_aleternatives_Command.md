- used for setting up python on centos 7 since 3.8 is not offically available on yum repo
```
[root@centos7 ~]# alternatives --install /usr/bin/python python /usr/bin/python2 50
[root@centos7 ~]# alternatives --install /usr/bin/python python /usr/bin/python3.6 60
```
