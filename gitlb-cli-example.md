## Gitlab sample docker build using hosted Gitlab Shared Runner for free.

Check the sample project created, link to [my-first-project](https://gitlab.com/thirumurthis/my-first-project)

This project has a simple Hello java class, which needs to be build using the open-jdk-image, then add to the artifacts.
Once the artifacts are in place like Hello.java and Hello.class, the docker image needs to be created.

The Dockerfile will take care of building the images, and the gitlab-ci.yml will be taking care of deploying/package the images.

Gitlab also registers it to a docker.hub. More about the using the [Gitlab runner](https://about.gitlab.com/blog/2016/04/05/shared-runners/) 
this is provided for free. 

```

Step 1/4 : FROM alpine:latest
latest: Pulling from library/alpine
89d9c30c1d48: Pulling fs layer
89d9c30c1d48: Verifying Checksum
89d9c30c1d48: Download complete
89d9c30c1d48: Pull complete
Digest: sha256:c19173c5ada610a5989151111163d28a67368362762534d8a8121ce95cf2bd5a
Status: Downloaded newer image for alpine:latest
 ---> 965ea09ff2eb
Step 2/4 : ADD Hello.class Hello.class
 ---> fbfa27b9f9b8
Step 3/4 : RUN apk --update add openjdk8-jre
 ---> Running in b64f52e563a6
fetch http://dl-cdn.alpinelinux.org/alpine/v3.10/main/x86_64/APKINDEX.tar.gz
fetch http://dl-cdn.alpinelinux.org/alpine/v3.10/community/x86_64/APKINDEX.tar.gz
........
........
Executing busybox-1.30.1-r2.trigger
Executing ca-certificates-20190108-r0.trigger
Executing java-common-0.2-r0.trigger
OK: 85 MiB in 53 packages
Removing intermediate container b64f52e563a6
 ---> 95dd604c1070
Step 4/4 : ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","Hello"]
 ---> Running in 802375ed48e5
Removing intermediate container 802375ed48e5
 ---> b09f12441806
Successfully built b09f12441806
Successfully tagged registry.gitlab.com/thirumurthis/my-first-project:latest
[32;1m$ docker push "$CI_REGISTRY_IMAGE"[0;m
The push refers to repository [registry.gitlab.com/thirumurthis/my-first-project]
75747ede371a: Preparing
2409deab72da: Preparing
77cae8ab23bf: Preparing
2409deab72da: Pushed
77cae8ab23bf: Pushed
75747ede371a: Pushed
latest: digest: sha256:3763ef928bcda3084ce6e12235b3a8f7c64b4c97ad40be6047069cf25a06cad6 size: 947
section_end:1574615172:build_script
....[0K[32;1mJob succeeded
[0;m
```
