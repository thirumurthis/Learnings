In datrabricks when  i used the GreenShot application to grab the snapshot 
in the Debug console browser there was a message 
Do not copy-paste anything here. This can be used to compromise your account.
JQMIGRATE: Migrate is installed logging active, version 3.2.0
=============================

=============================
Databricks architecture - 
Two parts:  
    - Computing part      
        - contains the cluster       
        - VNet injection to be compatible with Company Security 
        - Teh template is within Company compliance group which needs to be used
    - Public control pane
        - Notebboks
        - jobs
        - security
        - VNET: this will be required to increase for more number of address
        - Currently not many cluster is used
        
