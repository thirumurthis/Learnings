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
     
     
