 - chef is now widely called as chef infra.
 
##### `Chef Workstation`
 - Chef Workstation provides all the tools to get started with infrastructure-as-code. 
 - Chef Workstation can be installed on laptop/workstation to begin working with Chef software.
 - The Workstation project bundles together all the common software needed when building automation instructions for tools like Chef Infra and Chef InSpec. 
 - It also includes common debugging and testing tools for all your automation code. 

##### Chef Workstation includes:
 - The `Chef Workstation App`
 - `Chef Infra Client`, which is an agent that runs locally on every node that is under management by Chef Infra Server. 
     - When Chef Infra Client runs, it performs all of the steps required for bringing a node into the expected state. 
     - You can also run Chef Infra Client locally in testing scenarios.
 - `Chef InSpec`, provides a language for describing security and compliance rules (that can be shared between software engineers, operations, and security engineers).
 - `Chef (CLI) Command Line Tool`, which allows you to apply dynamic, repeatable configurations to your servers directly over SSH or WinRM via chef-run. 
     - This provides a quick way to apply config changes to the systems you manage whether or not they’re being actively managed by Chef Infra, without requiring any pre-installed software.
 - `Test Kitchen`, used to test cookbooks across any combination of platforms and test suites before deploying the cookbooks to actual infrastructure nodes.
 - `Cookstyle`, which is a code `linting` tool that helps you write better Chef Infra cookbooks by detecting and automatically correcting style, syntax, and logic mistakes in cookbook code.
 
 - Below command will list the installed package details.
```
$ chef --version
Chef Workstation version: xxx
Chef Infra Client version: xxxx
Chef InSpec version: xxxx
Chef CLI version: xxxx
Chef Habitat version: xxxx
Test Kitchen version: xxxx
Cookstyle version: xxxx
```
