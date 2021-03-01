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
    - Sample java Test class to test
 ```java
import java.io.File;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.

public class FirstTest extends CamelTestSupport {
   @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
         return new FileHandlerRouter();  // we can driectly create a new Routebuilder() and override the configure method.
  };
}

  @Test
   public void testMoveFile() throws Exception {
     template.sendBodyAndHeader("file://target/input/", "test", Exchange.FILE_NAME, "message.txt"); // creates a file in.txt with test as content
     Thread.sleep(1000);
     File target = new File("target/output/message.txt");
     assertTrue("File not moved", target.exists());
   }
}
 ```
    - Router class, that needs to be tested
 ```java 
 import org.apache.camel.builder.RouteBuilder;

 public class FileMoveRoute extends RouteBuilder {
     @Override
     public void configure() throws Exception {
     from("file://target/input").to("file://target/output");
    }
}
```

