#### What is `Policyfile` in cookbooks.
 - A Policyfile declares the name, run_list, sources, and attributes for a node or group of nodes. 
 - Policyfiles also handle all dependency resolution for your cookbooks. (Berks was older approach)
 
#### How to create a `wrapper` cookbook?
  - A `wrapper cookbook` is a pattern whereby calling one cookbook from within another, hence creating an inherent dependency and essentially using the wrapped cookbook as a library.
 
  - Say we have downloaded a cookbook from chef-supermarket repo or private repo.
  - we need to add maintain a specific version in our environment, we write a wrapper cookbook
 
  - Steps to create wrapper cookbook with a policyfile.
    - **Step 1:** Create a cookbook
    ```
    $ chef generate cookbook cookbooks/base -P
    ### -P = ensures a policyfile will be created within a cookbook
    ```
    - **Step 2:** Include the downloaded cookbook recipe in default.rb file, like below
    ```
    #### include_recipe '<cookbook-name>::<recipe-name>'
    include_recipe 'hardening::default'
    
    #### code to display message of the day in unix / windows
    if node['os'] == 'linux'
      file '/etc/motd' do
        content node['base']['message']
      end
    elsif node['os'] == 'windows'
      registry_key "HKEY_LOCAL_MACHINE\Software\Microsoft\Windows\CurrentVersion\Policies\System" do
        values [{:name => 'legalnoticetext', :data => 'node['base']['message']'}]
        action :create
        recursive true
      end
    end
    ```
    - **Step 3:** as we are using `node['base']['message']` this value is from attributes we need to create attributes
     ```
      > chef generate attribute cookbooks/base
      
      ### edit the file attributes/default.rb, add 
      default['base']['message'] = "message of the day from chef infra"
     ```
    
     - **Step 4:** we need to include the dependencies in metadata.rb file
       ```
       ...
       depends 'hardening'
       ...
       ```
  - Note: 
    - The hardening cookbook depends on  `os-hardening` and `windows-hardening` cookbooks
    - These dependent cookbook are NOT managed by us.
    ```
      What happens if the dependent cookbook is updated outside of our knowledge?
      - Policyfile helps to lock on the version of cookbook that is specified and that will be the one deployed.
    ``` 
  ##### Below is the Policyfile.rb file content.
  ```rb
  #
  # Policyfile.rb - Describe how you want Chef to build your system.
  # For more information on the Policyfile feature, visit
  # https://docs.chef.io/policyfile.html

  # A name that describes what the system you're building with Chef does.
  name 'base'

  # Where to find external cookbooks:
  default_source :supermarket

  # run_list: chef-client will run these recipes in the order specified.
  run_list 'base::default'

  # Specify a custom source for a single cookbook:
  cookbook 'base', path: '.'
  
  ### Add the dependent cookbook
  cookbook 'hardening', path: '../harderning'
  ```
   - The policyfile, contains the information of run_list within the cookbook where it resides (similar to Berksfile, combined with the Chef Role)
   
 ##### Elements of Policyfile.rb
 <details>
   <summary> Elements of Ploicyfile -click here </summary>

   - **name** - Used to reference this policyfile on the Chef server and it must be unique.
   - **default_source** - This is where we get cookbooks if they're not specifically declared in cookbook section below. 
                          This will usually be the public, or a private, supermarket, or Chef server.
   - **run_list** - When using policyfiles, then a node's run_list is defined within the policyfile, and node assigned to the node itself. 
                  - The policyfile is assigned to the node. This allows it to maintain consistent across environments.
   - **cookbook** - declares the non-default location where cookbooks can be found. (`cookbook 'hardening', path: '../harderning'`)
</details>

**IMPORTANT POINT**:
  - In above case, we are using the Policyfile in our application’s cookbook repo. 
  - In practice we may want to separate it into its own repository, because the frequent revisions of the lock file (outlined below) will clutter up your history.

#### How to create or lock the cookbook version to the node/ system?
  - Above we have defined within our policyfile what we'd like to run on a target machine. 
  - However, now we'd like to create snapshot containing the specific dependencies and specific versions that should be used. 
  - To do so we run the chef install policyfile command, as follows
  ```
  > chef install cookbooks/base/Policyfile.rb
  ```
  - Running chef install reads the Policyfile.rb and creates a Policyfile.lock.json file. 
  - This Policyfile.lock.json file is the actual policy used by Chef Client and contains unique references to the cookbooks in the run_list. 
  - The Policyfile.rb file is really only used as a human readable/editable file used to create the related Policyfile.lock.json file.
  - The Policyfile.lock.json file consolidates all dependencies in the run_list. 
  - This file gets downloaded to the node and read by chef-client. 
  - The chef-client will then download the precise versions of all dependencies and run them locally.

##### How does the chef-client know the Policyfile has changes?
  - The Policyfile.lock.json specifies not only the cookbooks required, but also the exact SHA fingerprint of all of the associated files and a checksum of the cookbook contents. 
  - If the contents change in any way, then the checksum will change and chef-client not run the policy.
  
  - If you were to use this same lock file on another workstation, then you can be certain it will use the same version of the cookbooks. 
  - If it cannot find these versions, then chef-client will return an error.
  
#### How to view the checksum information on policyfile?
```
 > chef describe-cookbook cookbooks/base
```
#### How to override the attributes of cookbook from policy file?
 - We can also define attribute values within the policyfile, giving you the ability to overwrite default values defined within the cookbook and increasing the flexibility of the cookbook.

`Note`: 
```
Adding attributes to policyfiles can be done and may be useful in some cases and for local testing.
It is not generally advised to do this in production or where you have many servers to manage, unless they're environment related (e.g. production vs acceptance).
```

 - update the attribute in the Policyfile.rb, like below
 ```rb 
 ...
 run_list 'base::default'

 # Specify a custom source for a single cookbook:
 cookbook 'base', path: '.'
 cookbook 'hardening', path: '../hardening'

 # Policyfile defined attributes
 default['base']['message'] = "Chef hardening example. Policyfile created at #{Time.now.utc}\n"
 ```
 #### Since we have updated the Policyfile.rb, we need to update the policyfile.lock.json. How to do that?
 ```
 > chef update Policyfile.rb
 ```
 
 - In local, we can use `kicthen list`, `kitchen create`, `kitchen converge`, `kitchen destroy` to validate the cookbooks.
 
 -------------------------
 
 ##### The policyfile can be created within the cookbook directory or outside the cookbook directory
  ###### how to create a simple policy file
  ```
  > chef generate ploicyfile
  ### will create a ploicyfile skeleton in the present working directory
  ```
   - Note the command will generate a file Policyfile.rb, this can be renamed to any name with .rb extension.
  - The `name` in the policyfile.rb, is used by chef-client to identify the policy. This should be descriptive
  - The `default_source` is where the chef should look for cookbooks, more than one location can be provided. we can use private supermarket, chef-server, artifactory, etc.
       - we can also provide individual cookbook reference with the location in the file.
  - The `run_list` is collection of cookbook to be runned by client. we cannot sepcify empty run list, specify a cookbook recpie.
      - this is similar to providing run_list via command in chef solo/zero.
      
  ###### how to install the Policyfile?
  ```
  > chef install
  
  ## this will take the runlist and create  a policyfile.lock.json
  ## the cookbooks are downloaded to local cache and then the lock file is created and checksum is created.
  ```
  
  ##### To test the policy file, use below command
  ```
  > kitchen converge
  ```
 
 Chef Automate - is enterprise version of chef server with more functionality.
 
  ##### How to push the policy file to Chef infra server? using `chef push`
  - The command requires two part:
    - 1. created Policyfile.lock.json 
    - 2. Policy group to push it to. (this is the environment)
    
  ```
  ## assume already the lock file is created in the directory
  
  > chef push staging Policyfile.lock.json
  
  ## staging - is the environment 
  ```
   - The `chef push` command will read the policy lock file and upload the files from the cache which was made during `chef install`/`chef update` command, to chef server checking the cheksum match.
   - These files are stored in the chef server as artifact with the checksum validation.
  
  ##### Since we have pushed the files to server, we need to bootstrap the nodes
    - to do this we use knife command
  ```
  > kinfe bootstrap <host-name> --sudo -x <user> -N <node-name>
  ```
   - The `bootstrap` command 
       - installs the chef-client 
       - runs the chef-client
       
  ##### Now actually the node is not set with any run list.
    - we can run the policy file after using the knife command
    - 
  ```
  > knife node policy set <node-name> <policy-group-or-environment> <policy-name> 
  
  ### ploicy-file is the name set within the policy file NOT the name of the file.
  ```
  ##### The above `knife node policy` doesn't run chef-client, we can run if from our local workstation
  
   **Below is the command to run the chef-client remotely on a node**
  ```
  > knife ssh name:<node-name> sudo chef-client
  ```
  
  ##### The node can be bootsrapped and the policy file can be applied in single command as well, by providing policy name and group in the bootstrap command itself.
  
  ##### Promoting the policy lockfile to different environment.
   - over time to manage the policy lockfile will be difficult, there are ways to mitigate
     - push the policy lock file (Policyfile.lock.json) to git and use it from there part of the work flow to promot to different environment.
     
  ##### If the git approach is not possible, then we can use below approach.
   - download the policy file from one environment to a Policyfile.lock.json
   ```
   > chef show-policy <policy-name> <policy-group-or-environment> > Policyfile.lock.json
   ```
   - install the downloaded policy 
   ```
   > chef install Policyfile.lock.json
   ## this will download to local cache
   ```
   - use below command to push to the chef server
   ```
   > chef push <new-policy-group-or-environment> Policyfile.lock.json
   ```
   - finally use bootstrap command to setup the new environemnt. using bootstrap command
   ```
   > knife bootstrap <env1> --sudo -x USER -N <node-name> --policy-group <group-name> --policy-name <ploicy-name>
   ```
   
  
