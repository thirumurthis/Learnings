Azure Monitoring:
   Alerts -> **New Alert rule** 
   
```
      Pick Resource name
      Select condition (47 signals pre-defined, like percentage cpu usage)
      Create or select Action group
      Input Alert Details (alert rule name)
      Description
      choose "Enable rule upon creation"
```

![image](https://user-images.githubusercontent.com/6425536/71857506-a802ab00-309c-11ea-9f45-67744068c561.png)

  Create **Action Group**:
```
     Input 
          Action group name
          Short name
          Subscription
          Resource group
     Additionaly
     Input Action group name _Unique name_
     Select Action Type from Dropdown (Autmoation Runbook, Azure Function, Email/sms/push/voice, webhook,etc)
    hit OK
```

![image](https://user-images.githubusercontent.com/6425536/71857393-3b87ac00-309c-11ea-8b12-6396d385875c.png)
    
    
    **To delete the alert rule:**
      Alerts -> Manage Alert Rule -> select the appropriate alert rule and delete.
    
    **To delete the Action group:**
     Alerts -> Manage actions -> select the appropriate action group and delete 
   
