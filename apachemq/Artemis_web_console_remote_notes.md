##### In order to make the web console to be accessed from diffrent VM:

```
# bootstrap.xml

<web bind="http://localhost:8161"/>

// This needs to change

localhost to 0.0.0.0 
```

##### Even in case the web console is accessible from remote machine, there was a error
```
“TypeError: i[c] is undefined (http://<host>:8080/console/app/app.js?60f4925a5c3d0942:16:13963”

To fix the above issue the jolokia-access.xml

<restrict>

// included

<remote>
<host>127.0.0.1</host>
<host>localhost</host>
<host>192.168.0.0/16</host> <!-- submetmask enabled -->
</remote>
<!--
<cors>
-->

<!-- Allow cross origin access from localhost ... -->

// commented out for our usecase

<!--
 <allow-origin>*://localhost*</allow-origin>
-->

<!-- Options from this point on are auto-generated by Create.java from the Artemis CLI -->
<!-- Check for the proper origin on the server side, too -->

// commented out for our usecase

<!--
<strict-checking/>
</cors>
-->
</restrict>
```
