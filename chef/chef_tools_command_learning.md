#### using `chef-run` command
  - This is a utility to run adhoc Chef Infra commands on a remote server. 
  - It provides a good way to get started with Chef Infra, without requiring any infrastructure beyond SSH.
  

#### in an docker instance setup, provided by the chef community for learning, below commands can be used.

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

