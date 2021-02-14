
### Below is how to start the chef development and manage the code.

#### STEP1: - `chef generate repo`
 - The chef generate command gives you the ability to get started writing Chef Infra code. 
 - To get started, you'll probably want a repository in which to store all your chef code. 
 - From your command line, change directories to your home directory and then generate a new Chef Infra repository.
    - lest name it as "learnchef-repo" by using below command 
```   
  $ chef generate repo learnchef-repo
```
 Note: Open with visual studio code, there is a plugin for chef development to build code.
 
##### `chef generate repo` command output directory structures
  - `cookbook` directory - To store all the cookbooks
  - `policyfiles` directory - To store overacrhing policies in one place
  - `data_bags` directory - To store secrets and keys, required to manage environment

#### STEP2: - `chef generate cookbooks`
  - Navigate to the learnchef-repo folder
  - To create cookbooks under the repo, use below command
  ```
  $ chef generate cookbook cookbooks/learnchef
  ### note you are within the repo directory.
  ```

#### STEP3: - `knife`
 - after the cookbooks are created and updated with content use `knife` command to push cookbook to chef infra server

##### what is `knife`?
 - `knife`, a command-line tool that provides an interface between your local `Chef Infra` code and the `Chef Infra Server` that manages all the moving parts of your Chef ecosystem.

 - `knife` helps you manage and maintain:
    - Nodes
    - Cookbooks and recipes
    - The enforcement of Policies
    - Resources within various cloud environments
    - The installation of Chef Infra Client onto nodes
    - Searching of indexed data about your infrastructure

 - With `knife`, we can create distinct profiles that allow you to quickly and easily switch from interacting with one Infra Server to another Infra Server. 
 - Also with `knife` profiles, we can switch between multiple organizations on the same Infra Server.
 
- In order to work with knife we need a Chef Infra Server for knife to interact with.

 #### what does the command - `knife bootstrap` do?
  - The `knife bootstrap` command allows to initiate a process that installs Chef Infra Client on the target system 
    and configures the Infra Client to be able to communicate with the Infra Server.
    
  -  It also registers the target system as a "node" on the Infra Server, allowing the Infra Server to manage which 
     and when policies are enforced, and thus automating your infrastructure management.
     
  - Options allows us to define the desired Policy Group, runlist or secrets at the same time.
  
  [More info on `knife bootstrap` - check this link](https://docs.chef.io/workstation/knife_bootstrap/)
  
 #### what does the command - `knife node` do?
   - Once target system is bootstrapped, the knife node command allows us to update the policies on any one or more of your managed nodes. 
   - With this command, we can change, remove or add assigned policies, update the node's target environment, and view information about existing nodes.
   
   [More info on `kinfe node` - chefk this link](https://docs.chef.io/workstation/knife_node/)
   
 #### what does the commands - `knife ssh` and `knife winrm` do?
  - Once the system is bootstrapped we use these command to connect to the remote nodes.
  - If we need to connect remotely to a system, the `knife ssh` and `knife winrm` commands will allow you to connect via SSH or WinRM respectively 
     and issue commands or investigate the system from your command line (workstation).
  
  Check links [ssh] (https://docs.chef.io/workstation/knife_ssh/), [winrm] (https://docs.chef.io/workstation/knife_windows/#winrm)
  
  
