
#### add autocomplete permanently to your bash shell
```
echo "source <(kubectl completion bash)" >> ~/.bashrc
```

#### use a shorthand alias for kubectl 
```
echo "alias k=kubectl" >> ~/.bashrc
````

#### To use alias to work with the autocomplete
```
echo "complete -F __start_kubectl k" >> ~/.bashrc
```

#### use a shorthand alias for setting namespace
```
echo "alias kn='k config set-context --current --namespace '" >> ~/.bashrc
```

#### restart the bashrc (~ = home directory)
```
source ~/.bashrc
```
#### export dry run and yaml option in the shell
```
export do="--dry-run=client -o yaml"
```

#### with all set above use below 
```
$ k create deployment nginx --image=nginx $do > nginx.yaml
```

#### The deletion of pod and deployment sometime takes some time, use force to delete immediately
```
export now="--grace-period=0 --force"
```

# to force kill your deployment after exporting the 
```
$ k delete deployments.apps nginx $now
```
