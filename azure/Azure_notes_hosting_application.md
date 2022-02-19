- In Azure to expose the applicaion hosted in the Azure cloud by using `Application Gateway`.

- Configuration glimpse  (L7 loadbalancers, which uses HTTP based redirection)

#### `Configuration` 
   - enables `Autoscale` 
   - Minimum instance count (based on requirement) 2 in my case
   - Maximum instance count (based on requirement) 5 in my case
   - HTTPS2 enabled

#### Backend pool
 - Note: Create a backend pool, by providing the server IP's where the application will be provided.
   - `Add backend pool without targets` - No
   - `Backend targets` - Provide Ip Address or FQDN (1 or more values). In our project the UI application was running in two VM servers, and those ip address where configured here.
   - `Associate Rule` - Create a rule and add it here. (create the rules before adding the backend pool)

#### Http Settings
 - Note: add settings 
    - `Http Settings name` - in our case we used HTTP (selected)
    - `Backend Port` - where our UI application entry port used, it was the tomcat application and useddefault 8080 port here.
    - `Aditional settings` 
        - `Cookie based affinity` - Enabled
        - `Affinity cookie name` - name for the cookie
        - `Connection draining` - Disable
        - `Request time out (optional)` - 1000 (in seconds - the application gateway will wait to recevie a response from the backend pool before it returns a connection time out)
        - `Override backend path` - optional and not provided
     - `Host Name`
        - `Override with new host name` - No
        -  `Host name override` - `Override with specific domain` option selected. (so no input provided)
        -  `Use custom probe` - yes selected
        -  `custom probe` - name for the custom probe (this will be the health check probe created in the Application gateway) 

#### `Frontend IP Configuration`
  - Note: create and set the ip address. Create two one public type and private type
     - `Type` - public
     - `Name` - provide a name
     - `public ip address` - select the ip address (this will be associated ip address craed in app-gateway)
     - `Associated listeners` - None

#### Listeners:
   - Note: Application Gateway provides native support for Websocket accross all gateway sizes. If a websocket traffic is received on App gateway, it is automatically directed
           to the websocket enables backend server using appropariate backend ppoll as specified in the gateway rules. below is the endpoint listeners
      - `Listener name` - name for the listener
      - `Frontend Ip` - Private
      - `Port` - 443 
      - `Protocol` - Https
      - `Choose certificate` - Select existing (we provide an already created certificate)
      - `Certificate` - choose the already uploaded certificate from dropdown
      - `Renew or edit selected certificate` - not selected
      - `Enalble SSL profile` - not selected
      - `Associate rule` - path Rule created under the rules, which was defined already (* - check documentaton how to create this)
      - `Additional Settings`
         - `Listener type` - Multi site
         - `Host type` - Single (selected)
         - `Host name` - provided a name with the domain we had, example `myapp-services.project.mycompany.com`
         - `Error page url` - No selected
           
#### Rule
 - Note: Configure a routing tule to send traffic from a given frontend IP address to one or more backend targets. A routing rule must contain a listener and at least one backend target

   - `Rule name` - provide a name
   - `Listener tab` - select the created Http listener in this section
   - `Backend targets tab` 
       - `Target type` - Backend pool (selected) another option is redirection
       - `Backend target` - Choose the backend target to which this rule should route traffic. If creating path-bases rule, traffic will be sent to this backend target unless you specify otherwise below.
       - `Http Settings` - The Http settigns define behavior for this rule, including the port and protocol used in the backend and setting such as cookie-based session afffinity and connection draining
       - `Path based routing`
         - `Path based rules` (create path)
         - Path : /project/*  
         - Target name: <this will be the service name>  
         - Http setting name : name of the created in http settings 
         - Backend pool : select the backend pool created.

#### Health Probes
   - Note: we can add probes here
     - `Name`- name for the probe
     - `Protocol` - HTTP selected
     - `Host` - loopback ip (127.0.0.1) selected
     - `Pick hostname from backend HTTP settings` - No
     - `Pick port from backend Http settins` - yes
     - `Path` - If the hosted application has an endpoint which provide 200 OK result configure it here.
     - `interval` - 15 seconds
     - `Timeout` - 30 seconds
     - `Unhealthy threshold` - 2
     - `Use probe mathcing conditions` - No
     - `Http Settings` - select created http settings
     - enable the check box want to test the backend health before adding the probe for testing


