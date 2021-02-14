 - chef is now widely called as chef infra.
 
##### `Chef Infra`
  - a powerful automation platform that transforms infrastructure configuration into code. 
     - Heart of this tool uses `Chef Infra Client`, which typically runs as an agent on the systems managed by Chef. 
     - The client runs Chef libraries called `Cookbooks`, which declare the desired state of your system using infrastructure-as-code. 
     - The chef client set on the nodes, ensures the system is inline with the declared policy in cookbooks.  

##### `Chef InSpec`
  - provides a language for describing security and compliance rules.
  
##### `Chef Habitat`
  - an open source automation solution for defining, packaging, and delivering applications to almost any environment regardless of operating system or platform.
     - This is accomplished by declaring the build and runtime instructions for the application in a `Habitat Plan file`. 
     - The application is then built in a cleanroom environment that bundles the application alongside its deployment instructions into a single deployable Habitat artifact file (.HART). 
     - This artifact is then deployed by the `Habitat Supervisor`, which monitors the application lifecycle, including deploying runtime configuration updates without having the rebuild the application.
  
##### `Chef Workstation`
 - Chef Workstation provides all the tools to get started with infrastructure-as-code. 
 - Chef Workstation can be installed on laptop/workstation to begin working with Chef software.
 - The Workstation project bundles together all the common software needed when building automation instructions for tools like Chef Infra and Chef InSpec. 
 - It also includes common debugging and testing tools for all your automation code. 

##### `Chef Workstation` includes:
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

##### `Chef Automate`
   - an enterprise visibility and metrics tool that provides actionable insights for any systems that you manage using Chef tools and products.
   
##### `Chef Desktop`
   - allows IT teams to automate the deployment, management, and ongoing compliance of IT resources.
      -  of any large fleet of IT resources – such as laptops, desktops, and kiosk workstations – from a centralized location.
   - Under the hood, `Chef Desktop` is built on Chef Infra with some Azure components.
   - Chef Desktop services target the following basic functions:
      - Hard drive encryption
      - Password policy to set complexity and other elements
      
##### `Chef Engterprise Automation Stack (EAS)`
   - is full suite of enterprise infrastructure, application and DevSecOps automation technologies for delivering change quickly, repeatedly, and securely.
