#### knife profiles
 - `knife` allows to create multiple profiles using `credentials.rb` file.
 - Benefit of using knife profiles is the ability to quickly and easily switch from one Chef Infra server to another Chef Infra server. 
 - With knife profiles we can also switch between multiple organizations on the same Chef Infra Server.
 - This is especially beneficial in cases where there are very large infrastructure, perhaps spanning many different regions where having multiple Chef Infra servers or many Chef Infra server organizations might be used.
 
##### How to configure knife profile?
 - configure knife profiles by adding them to the `~/.chef/credentials` file in home directory on workstation. 
 - The credentials file is TOML (Tom's Obvious, Minimal Language) formatted. 
    - Each profile is listed as a separate ‘table’ name of our choice, and is followed by key-value pairs.

  - If `.chef` folder exists, we need to add the user1.pem or certificate from Chef Infra server.
     - If the file didn't exists create one under the home directory. `cd ~` or `mkdir ~/.chef` in linux.
  - In actual practice, the user1.pem will be provided by the Chef Infra server administrator or from some one from organization. (Can be obtained from chef starter kit)

##### How to setup profiles on configuration file?
   - create a file `credentials` under `~/.chef/` and update the following content
   ```
   [learn-chef]
   client_name = "user1"
   client_key = "~/.chef/user1.pem"
   chef_server_url = "https://organization.com/organization/"  ## get this url form organization or use chef hosted url.
   ```
   - Now navigate to home directory, `cd ~`
   - Set the config profile that needs to be used by the knife command using below command
   ```
   > knife config use-profile learn-chef
   ### This will output, that learn-chef profile set as default
   ```

##### How to fetch the SSL certifcate from the Chef Infra Server using knife
```
> knife ssh fetch
### this will output that certificate place in trust store
```

#### How to verify if the knife can talk to the Chef infra server
```
> knife client list
### outputs the list of chef infra server organization as response
```
