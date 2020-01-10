# setting up chef-repo in VM

After getting access to the chef-server navigate to `Administrator` -> `Starter Kit` -> Download the starter kit

After Unzip the file, make sure the setup the organizational chefk server url in `~/.chef/knife.rb` if not updated correctly.


in the cookbooks folder under chef-starter, clone the cookbooks from git or user chef-client command to create cookbook.

To push the databags file towards the server, where the environment is defined.

```
$ knife environment from file <path-of-json>
```

This command will update the enivornment json file to the server, and this can be validated in chef server under Environment directory.

Check `Policy` tab -> `Environments` -> Environment Name -> Attributes tab (at bottom)


The `knife supermarket` subcommand is used to interact with cookbooks that are located in on the public Supermarket as well as private Chef Supermarket sites. A user account is required for any community actions that write data to the Chef Supermarket; however, the following arguments do not require a user account: download, search, install, and list
