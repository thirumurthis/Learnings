Experience on handling SSO in one of the project.

- Create a proxy url, this url will be used by the user in the browser. 
    - This url will thi the SSO proxy server.
    - The proxy server, will prompt the user with login/certificate based authentication.
    - After successful authentication, the user is routed to the registered application (backend url) with the organization.
    - The application server, then gets the logon user information from the request header or certifcate from proxy.

Note: In my application, i had to expose an REST endpoint build using Apache CXF which is consumed by another system, for this the policy needs to be updated in the proxy saying to allow URLs with specifi pattern or wild chards without redirecting to SSO login page.

- Since tomcat may use more port, not all the port is not opened in the IP table, request the sys admin to enable ports to be accessed in the server.
 
 - To setup certificate based locking.
   - In the application server generate the certificate using java keytool. use below command.
    ```
     $ kyetool -certreq -keyalg RSA -alias <alias-name> -keystore <keystorename>
    ```
   - Use the generated content in the Centralized certitifate management system to create the generalized token.
   - For any other additional certificate chain, to generate the keystore and truststore import using below keytool command
   ```
   # provided with the certiticate chain
   $ keytool importcert -alias my-app-1 -keystore keystorefilename -trustcerts -file provided-file-chain-cert
   ```

 - Implementing SSO in tomcat 8 to use SSO Proxy below update to server.xml
 
```xml
<!--
Global Naming Resources
The element (Line 9-15) defines the JNDI (Java Naming and Directory Interface) resources, that allows Java software clients to discover and look up data and objects via a name.
The default configuration defines a JNDI name called UserDatabase via the element (Line 10-14), which is a memory-based database for user authentication loaded from “conf/tomcat-users.xml”.
-->
 <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>
  
<!-- Enabling ssl and redirecting request from different port to 8443 -->

<!-- The application will hit this port like https://mydomain.com:8899 -->
<!-- which will be then redirect to port 8999, that is congigured as https -->
  <Connector port="8899" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8999" />
			   
    <!--  Add ip address of the current server, for hardening sercuity -->
    <Connector address="<ip-address-of-curren-server>" SSLEnabled="true" clientAuth="true"
               keystoreFile="<keystore-file-path-.jks.extension>"
               keystorePass="passwordforkeystore-used-when-created"
               truststoreFile="path-to-the-keystore-when-created"
               truststorePass="passwordfortruststire" maxThreads="200" port="8999"
               protocol="HTTP/1.1" scheme="https" secure="true" sslProtocol="TLS"/>
    
	<!-- request to the 8030 port is also redriected to 8999 https -->
    <Connector port="8030" protocol="AJP/1.3" redirectPort="8999" />

<!-- A Realm is a database of user, password, and role for authentication (i.e., access control). You can define Realm for any container, such as Engine, Host, and Context, and Cluster. -->
  <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
</Realm>
<!--... -->
```

Update the tomcat-user.xml with specific roles
```xml

<tomcat-users>
<role rolename="manager-gui"/>
<user username="tomcat" password="s3cret" roles="manager-gui"/>

  <role rolename="proxy-user-role"/>
<!— Generate the certificate from Certificate authority, and include that info here use the actual Certificate Name of proxy client certificate (used for web  authentication) below. -->
<user username="CN=<cert-name>, OU=<added-when-created>, O=<company-name>, C=US" password="" roles="proxy-user-role"/>
<!-- if we have personal certificate, we can use user like above -->

```

- Web.xml
```xml
<!-- Login Configuration for Application - create constraints -->
  <security-constraint>
    <web-resource-collection>
       <web-resource-name>MY SECRET APP</web-resource-name>
       <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
       <role-name>proxy-user-role</role-name>
    </auth-constraint>
  </security-constraint>

  <login-config>
    <auth-method>CLIENT-CERT</auth-method> <!-- certificate type used -->
    <realm-name>my-app</realm-name>
  </login-config>

  <!-- Security roles referenced by this web application -->
  <security-role>
    <description>
      Role defined in tomcat-user.xml will be used and required to access  Application protected by Web SSO (proxy)
    </description>
    <role-name>proxy-user-role</role-name>
  </security-role>
```

What is Web Single Sign On  (SSO) - is the standard enterprise web access solution for all applications.

Proxy - is a service that provides Single Sing On (SSO) protection and forwarding traffic without use of traditional webgate and certs.

Oracle access manager - older standard used in SSO 
PingIdentity  (Ping Access Gateway Proxy) - new standard used in SSO by application

Most of the time using HttpWatch, the traffic can be troubleshooted


