1. To create a self hosted Agent pool in ADO

  - From Azure ADO, select the project settings -> select Agent Pools -> use Add Agent pool button to create a Agent pool
     - Select the AgentPool type as self-hosted.
     - Provide a name.

2. We create a PAT token, go to the User profile, and click Personal Access Token, fill in the name and expiry duration and copy the token (store this value safe since this will not be displayed again)

3. Create a resource group using, portal or Az cli

4. create a azure template json file, and azure parameter json file
   - update the Az cli
   - Using `az account list -o table` to check under which subscription the cli is refering.
   - And use the below command to create the Agents witin the Agent pool
     - Note: THe agent pool information, image server url, location and vm size (Standard) Vm count can be provided in template/ parameter.  

   ```
   az deployment group create --name "azure-VM-scaleset-deployment" --resource-group <name-of-resource-group-created> --template-file <template-file.json> --parameters <parameter-file.json>   
   ```
   
   In normal scenario where we don't have a image to adhere to the Enterprise Security compliance, we use below steps to add agent to the agentpool
    - Create a VM using desired configuration (like windows , linux, etc)
    - Install the Azure DevOps agent msi/executable in that VM
    - Link this VM in the Agent Pool, using Existing VM option.
   
