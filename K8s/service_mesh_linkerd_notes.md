## in windows to install the linkerd
 - fetch the installable from the github project.

Once the binary is extraced and set in the Environment variables path, we can use below command
```
> linkerd install | kubectl apply -f -
```

## in order to atuo inject proxy to the container we can use below command to create an annotation

```
# to use default namespace use below command
> kubectl annotate namespace default linkerd.io/inject=enabled

# we can use different namespace, say demo which is created alread
> kubectl annotate namespace demo linkerd.io/inject=enabled
```

## if we need to manually inject the proxy as a side car then we need use the linkerd cli
 - use below command, with the deployment yaml or descrptor file
```
> cat mydeployment.yaml | linkerd inject --manual - 
```

> **Note** 
>  With the above command the linkerd will inject the necessary ojbect required to create the proxy
>  all will be updated in the yaml file, and displayed in console
>  The above deintion yaml can be stored in a file and piped into kubectl to deploy.
