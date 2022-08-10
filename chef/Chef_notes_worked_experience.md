
#### How to push the latest cookbook to Chef -server

```
knife cookbook upload <cookbook-name> [<cookbook_version>]

### the cookbook-name is present in metadata.rb
### version is optional (check documenation) for more details
### Navigate to the cookbook level
```

### To validate the cookbook in local and upload to chef server using berks
```
berks install
berks upload <cookbook_name> --no-freeze
```

### How to push the environment.json changes to the chef server using knife command

```
knife environment from file <environment-file.json path>
```

### How to update the databags to the chef server using knife 

```
knife dat bag from file <environment-name> <path-to-the-environment-config-file.json>
```

### If you have a chef push client configured, you can run it using knife 

```
knife job start <job-name> <node-name1> <node-name2>...
```

### How to compare different environment using knife command
```
knife environment compare <environment1> <environment2>

### above will list the cookbook version that is being defined
```

### How to lock specific cookbook version on the UAT/Prod evironment

 - in chef we use the environment.json file where we can specify the environment.json
 
```json 
 {
   "name" :"environment-name-example-test",
   "description": "environment used by the project",
   "cookbook_versions" : {
       "cookbook1" : "= 1.1.1",
       "cookbook2" : "= 2.0.0",
       }
   "json_class" : "Chef::Environment",
   "chef_type" : "environment",
   "default_attributes" : {
       "my-projet" : {
          "artifactory": {
             "url" : "https://your-artifactory-url"
          }
       }
   },
   "override_attributes" : {
      "tags" : [
        "if_this_tag_is_set_for_this_enviroemnt_enable_this",
        ]
        },
    // also specify the version of the artifact here like 
      "my-project-app" : {
        "version" : "1.10",
        "snapshot" : "1.0-SNAPSHOT"
      }
   }  
```

### how to execute chef-zero

```
sudo chef-client -z -o coobkook-name::receipe-name

sudo chef-client -z -o <run-list>
```

### check the ssh using knife command 

```
knife ssl check <chef-server-url>
knife ssl fetch <chef-server-url>
```
