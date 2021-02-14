#### using `chef-run` command
  - This is a utility to run adhoc Chef Infra commands on a remote server. 
  - It provides a good way to get started with Chef Infra, without requiring any infrastructure beyond SSH.
  

#### if an docker instance is setup (as provided by the chef community for learning), we can use below commands.
 - This will directly execute the resources in the container
```
$ chef-run web1 file hello.txt
### web1 - is the target node
### file - is the resource type
### hello.txt - is the file name that will be created in the web1 node.
```

```
$ chef-run web1 file hello.txt content="hello world"
```
 - To view the content of that was created in web1 node use
```
$ ssh web1 cat /hello.txt

$ ssh web ls -l /hello.txt
```

- delete the file created using chef-run

```
$ chef-run web1 file hello.txt action=delete
```

