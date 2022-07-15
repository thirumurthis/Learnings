### The project had a databag configuration.

 - The strcuture is each environment had different folder for each environment.
 - use the environment.json, to configure and freeze the coobkook version. (so in prod and uat we need to freeze the version)


During the devlopment, we update the cookbook recipe, for every functionality change we update the metadata.rb version in the cookbook.

- the configuration are generated dynamically where for each environmnent, the chef managed using 

```
databag
  |_ prod
     |_ ...
  |_ preprod
     |_ ....
  |_ preprod-east
     |_ ...
  |_ test
     |_  config.json
     |_ environment.json
```

Once we make the change to the cookbook, we used to manually push the changes to the Chef server using  below knife command
```
# download and update the cookbook from the git repo, update the changes
# sync to the master or corresponding branch

knife upload cookbook <cookbook-name>

# the cookbook-name is the name present in the metadata.rb
```

- Below is the structure, navigate to the project-cookbook, and then issue above command.

```
chef-repo  (downloaded from the chef-server and setup is already done)
 |_ cookbooks
       |_ project-cookbook
          |_ metadata.rb
          |_ .... other rb (attribute, template, etc. folder)
```

- if any changes done to databags config.json, to push that to the chef server we use below command.

```
# navigate to the databags folder, refer the structure above

knife data bag from file test test/config.json

<test => is the environment that the config to be updated, which can be found in the chef-server
```

- if any change to the environment and in order to push that change to the chef-server.

```
# navigate to the databags - in case we update the new version to be deploy in test environment

knife environment from file test/environment.json
```
