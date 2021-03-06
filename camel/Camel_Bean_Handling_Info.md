### Spring beans can be easily integrated with Camel context and used.

 - We have a simple POJO class HelloBean, and lets see how to invoke the sayHello() method.

```java
public class HelloBean {
  public String sayHello(String name){
     return "Hello from Bean To "+name;
  }
}
```
 - 1. One way to do this is, using `Process` in the RotueBuilder, below example using java DSL (a hard way, not using Spring container).
```java
  public class HelloRouter extends RouteBuilder{
     @Override
     public void configure() throws Exception{
       from("direct:input")
         .process( new Process(){
           public void process(Exchange exchange) throws Exception{
              String name = exchange.getIn().getBody(String.class);
              //create a bean
               HelloBean hello = new HelloBean();
               String output = hello.sayHello(name);
              exchange.getOut().setBody(output);
           }
         });
     }
  }
```

- 2. Second apporach, to use Spring DSL xml
```xml
  <bean id="helloBean" class="HelloBean"/>  <!-- autowire this to the HelloRouter class -->
  <bean id="helloRouter" class="HelloRouter"/>
  <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
    <routeBuilder ref="helloRouter"/>
</camelContext>
```
```java
  public class HelloRouter extends RouteBuilder{
     @Autowired
      HelloBean helloBean;
     @Override
     public void configure() throws Exception{
       from("direct:input")
         .process( new Process(){
           public void process(Exchange exchange) throws Exception{
              String name = exchange.getIn().getBody(String.class);
               String output = helloBean.sayHello(name);  //using Autowired bean from spring container
              exchange.getOut().setBody(output);
           }
         });
     }
  }
```
- 3. Third approach, will not required the Process or RouteBiulder, within the spring DSL
```xml
  <bean id="helloBean" class="HelloBean"/>  <!-- autowire this to the HelloRouter class -->
  <bean id="helloRouter" class="HelloRouter"/>
  <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
    <route>
       <from uri="direct:input"/>
       <bean ref="helloBean" method="sayHello"/>
    </route>
</camelContext>
```
- Within the java DSL RotueBuilder
```java
 public void configure() throws Exception {
  from("direct:input").beanRef("helloBean","sayHello"); // we can ommit the method name as well, if the bean has ONLY ONE method 
 }
// In java DSL, we can use below 
//  from ("direct:input").bean(HelloBean.class);  // since there is only one method. 
// above bean () doesn't require to preregister the bean in registry. since we are providng the class name in the bean (). 
```
