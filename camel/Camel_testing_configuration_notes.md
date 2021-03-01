#### Camel Test Kit:
  - `camel-test` jar provides few class to support testing
  
  | Class | description |
  |--------|----------|
  | org.apache.camel.test.junit4.TestSupport | abstract base test class with additional assertion methods. |
  | org.apache.camel.test.junit4.CamelTestSupport | base test class prepared for testing Camel routes. |
  | org.apache.camel.test.junit4.CamelSpringTestSupport | base test class prepared for testing Camel routes defined using Spring DSL. This class extends CamelTest Support and has additional Spring-related methods. |
  
  #### Using `CamelTestSupport` class
  - add below dependencies for camel test kit
 ```xml
<dependency>
   <groupId>org.apache.camel</groupId>
   <artifactId>camel-test</artifactId>
   <version>${camel.version}</version>  <!-- version defined in the properties tag -->
   <scope>test</scope>
</dependency>
<dependency>
   <groupId>junit</groupId>
   <artifactId>junit</artifactId>
  <version>${junit.version}</version>
  <scope>test</scope>
</dependency>
 ```
  - Sample java Test class to test, below is sample to validate `java DSL` route
   
 ```java
import java.io.File;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.

public class JavaDSLTest extends CamelTestSupport {
   @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
       return new FileHandlerRouter();  // we can driectly create a new Routebuilder() and override the configure method.
     };
   }

   public void setUp() throws Exception {
     clearupDir("target/input","target/output"); //Before startring the test case, cleanup the directory, the method is not Implemented here.
     super.setUp();
  }
  
  @Test
   public void testFileHandler() throws Exception {
     template.sendBodyAndHeader("file://target/input/", "test", Exchange.FILE_NAME, "message.txt"); // creates a file in.txt with test as content
     Thread.sleep(1000);
     File target = new File("target/output/message.txt");
     assertTrue("File not moved", target.exists());
     
     // VALIDATE THE CONTENT OF THE FILE, CAMEL PROVIDES CONVINENT convert system
     File target = new File("target/output/message.txt");
     assertTrue("File not moved", target.exists());  // assert if file moved to output folder
     
     // Below we are using camel file-based converter, which is automatically identified by Camel.
     String content = context.getTypeConverter().convertTo(String.class, target);
     assertEquals("test", content);  //match the content of the file received.
   }
}
 ```
  - Router class, that needs to be tested
 ```java 
 import org.apache.camel.builder.RouteBuilder;

 public class FileHandlerRoute extends RouteBuilder {
     @Override
     public void configure() throws Exception {
     from("file://target/input").to("file://target/output");
    }
}
```

#### Using the `SpringCamelTestSupport` class can be used for `Spring DSL` xml based route testing.
 - in this case the `testFileHandler()` and `setUp()` method remains the SAME as in the above example.
```java
import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class SpringDSLTest extends CamelSpringTestSupport {

     protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("context/camel-context.xml");  // we can also use FileSystemXmlApplicationContext as well.
      }
   //.... the setUp() and testFileHandle() method are same as the above example
 }
```

 - The `context/camel-context.xml`, spring based routes
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
                    http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd">
  
  <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
    <route>
       <from uri="file://target/input"/>
       <to uri="file://target/output"/>
    </route>
  </camelContext>
</beans>
```
-------

#### Externalizing properties
 - Lets create a properties file
 ```properties
 ## file-name: filehandler-test.properties
 file.input=test/files/inbox
 file.output=test/files/outbox
 ```
  ##### 1. using Camel-core properties placeholder
   - Camel-core jar, already includes a properties placeholder.
   - In camel-context xml, we defined a bean with `org.apache.camel.component.properties.PropertiesComponent`.
   ```xml
     <!-- ... -->
     <bean id="properties" class="org.apache.camel.component.properties.PropertiesComponent">
        <property name="location" value="classpath:filehandler-test.properties"/>
     </bean>
     <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
       <route>
         <from uri="{{file.input}}"/>  <!-- The way we reference properties key is {{}} NOTE the spring way ${} -->
         <to uri="{{file.output}}"/>
       </route>
    </camelContext>
    <!-- ... -->
   ```
   - Instead of creating the properties as spring bean, we can use propertyPlaceholder within the camel context like below
   ```xml
    <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
       <propertyPlaceholder id="properties" location="classpath:filehandler-test.properties"/>
       <route>
         <from uri="{{file.input}}"/>
         <to uri="{{file.output}}"/>
        </route>
    </camelContext>
   ```
   - How the Test class, gets update when externalizing the properties.
   ```java
     public class CamelRiderTest extends CamelSpringTestSupport {
       private String inputDir;
       private String outputDir;
      
      // NOW we are injecting the input endpoint 
      @EndpointInject(uri = "file:{{file.input}}")
      private ProducerTemplate inbox;
      
      public void setUp() throws Exception {
         super.setUp();
         // since the context.xml is not externalized, we need to pass in the value to context in test class
         inputDir = context.resolvePropertyPlaceholders("{{file.input}}");
         outputDir = context.resolvePropertyPlaceholders("{{file.output}}");
         cleanupDir(inputDir,outputDir); //Note this class implementation is not included in this class.
      }
      
      @Override
      protected AbstractXmlApplicationContext createApplicationContext() {
            //NOTE: We are passing PROD properties as first argument, TEST properties as second argument.
            // Since the TEST properties is passed as second argument, any properties value will be overriden even if the PROD properties is read in here
            return new ClassPathXmlApplicationContext(new String[]{"camelinaction/filehandler-prod.xml","camelinaction/filehandler-test.xml"});
      }
      
      // the testFileHandle () method will be the same as above example, with only one change instead of using template we need to use inbox (ProducerTemplate)
   ```
   
   - Notes:
   ```
     Spring allows to load multiple files as args and have the next file override the previous args — so we define the CamelContext once, along with filehandler-prod.xml. 
     Because filehandler-test.xml is defined as the second argument, this will override identical beans from the former files. 
     We leverage this to override the properties bean and instruct it to load a different properties file, the filehandler-test.properties file.
     
     @EndpointInject in RouteBuilder class is used to dynamically inject endpoints for the environment. This can still be used in Java DSL also.
   ```
   - To load the properties file from the Java DSL test case
   ```java
   //...
   protected CamelContext createCamelContext() throws Exception {
      CamelContext context = super.createCamelContext();
      PropertiesComponent prop = context.getComponent("properties", PropertiesComponent.class);  // Override the camelcontext, and add the proeprties in Java DSL
      prop.setLocation("classpath:filehandler-test.properties");
      return context;
    }
    
   protected RouteBuilder createRouteBuilder() throws Exception {
      return return new RouteBuilder() {
           public void configure() throws Exception {
              from("file:{{file.input}}").to("file:{{file.output}}");  //This can also be a spearate java class extending routebuilder.
           }
       };
    }
   //... The same test class as above, where the testFileHandle() and setUp() method will be the same. 
   ```
   
  ##### 2. Using Spring properties placeholder
  ```xml

  <context:property-placeholder properties-ref="properties"/>
  <util:properties id="properties" location="classpath:filehandler-test.properties"/>
  <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
     <endpoint id="inbox" uri="file:${file.input}"/>   <!-- In spring we cannot use ${} directly within the route, from -->
     <endpoint id="outbox" uri="file:${file.output}"/> <!-- but, ${} can be used wit the endpoint, so we add spring properties like this -->
     <route>
        <from ref="inbox"/>
        <to ref="outbox"/>
     </route>
  </camelContext>
  ```
-----------
##### TIPS
```
Camel has a component - Jasypt component which can be used to encrypt sensitive information in the properties file. 
Provides a jar to encrypt and decrypt values.
```
Refer [Camel component - Jasypt](https://camel.apache.org/components/3.8.x/others/jasypt.html)

