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

### How does the Bean is identified or resolved within camel.
  - Using **`Service Activator Pattern`** - describes a service that can be invoked easily from both messaging and non-messaging services.
  - Below diagram explains the pattern, the service activator itself is the bean node, which is represented as `BeanProcessor` in Camel.
  - Below is how Camel works with the beans.

![image](https://user-images.githubusercontent.com/6425536/110218226-4fc86400-7e6d-11eb-986c-5b0b5b1351cd.png)

  - Now we need to know how to get those beans or look for it. This is where the `registry` is used in camel.
  - When camel works with the beans, it looks them up in the registry to locate them.
  - The camel registry is an abstraction that sits between the caller and the real registry.
  - The registry in camel is merely a **`Service Provider Interface (SPI)`** defined in `org.apache.camel.spi.Registry` interface.
  - Below is ways to lookup for the beans using Camel 3+ `using camelContext.getRegistry().lookupByNameAndType(String beanName, Class)` class here will be typecast
  
 ```java
  // Loading the spring dsl 
  ApplicationContext appContext = new ClassPathXmlApplicationContext("delivery-service-client-remote-context.xml");
  
  // fetching the camel context within the spring dsl, where the <camelContext id="camel"... being used.
  SpringCamelContext camelContext = (SpringCamelContext)appContext.getBean("camel");
  try {
      camelContext.start();
      // using camelContext.getRegistry().lookupByNameAndType()
      // deliveryInfo is a bean defined in the context.xml of spring DSL
      UpdateDeliveryDetailsFromClient shipmentReceiver = camelContext.getRegistry().lookupByNameAndType("deliveryInfo",UpdateDeliveryDetailsFromClient.class);
      String updateStatus = shipmentReceiver.updateDelivery("From Renton");
 ```
 ##### Different type of Registry within camel.
  - SimpleRegistry => a map-based registry used for testing or running standalone.
```
SimpleRegistry registry = new SimpleRegistry();
registry.put("helloBean", new HelloBean());
context = new DefaultCamelContext(registry); //pass the registry
```

  - JndiRegistry => integrates with the JNDI-based registry. This is a Default registry. Like, simpleregistry this is often used for testing or running standalone.
    - This is useful when using Camel together with the Java EE application server that provides JNDI-based registry out of box. 
```
  CamelContext context = new DefaultCamelContext(); // this will by default inclure the JNDI registry
```
   - Sample code, how JNDI registry leverged on Websphere application
```java
Hashtable env = new Hashtable();
   env.put(Context.INITIAL_CONTEXT_FACTORY,"<connection factory context Class>");
   env.put(Context.PROVIDER_URL,"<provider URL>");
Context ctx = new InitialContext(env);  // create a context object
JndiRegistry jndi = new JndiRegistry(ctx); // Pass the context object to the jndi registry
CamelContext context = new DefaultCamelContext(jndi); // pass jndi to the camel context, so it overrides the default 
```
   - JNDI registry bean within Spring DSL
```xml
<bean id="registry" class="org.apache.camel.impl.JndiRegistry /> <!-- Pass the parameter using hashtable in spring bean way -->
```

  - ApplicationContextRegistry => this is the Default registry when Camel is used with Spring. This is the default when the spring xml is setup.
```xml
<!-- ApplicationContextRegistry set default when using the Spring DSL
 <camelContext> tag lets Camel know to use this registry  -->
<camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">
   <route>
    <from uri="direct:start"/>
     <!--... more routes -->
    </route>
</camelContext>
```
  - OsgiServiceRegistry => when camel is used in OSGI environment. Camel will lookup for service in the OSGI registry, if not then falls back to regular registry such as Spring ApplicationContextRegistry.
 
 #### Below diagram depicts how Camel looks up and invokes the method in a bean using BeanProcessor(service activator)
  - Camel has to compute which bean and method to invoke at runtime, where as java code is linked at compile time
 
![image](https://user-images.githubusercontent.com/6425536/110219296-32969400-7e73-11eb-8fca-b579aa4f2ecf.png)

```
### Explaining the steps involved the beanprocessor is uses exchange to invoke bean and it method

1 Looks up the bean in the registry.
2 Selects the method to invoke on the bean.
3 Binds to the parameters of the selected method (for example, using the body of the input message as a parameter)
4 Invokes the method.
5 Handles any invocation errors that occur (any exceptions thrown from the bean will be set on the Camel exchange for further error handling)
6 Sets the method’s reply (if there is one) as the body on the output message on the Camel exchange.
```

  - Why does Camel need to select a method?
```
beans can have overloaded methods, and in some cases the method name isn’t specified either, which means Camel has to pick among all methods on the bean.
say if a bean has below method:
      String print(String s);
      int print(int number);
      void compute(String something);
There are a total of three methods for Camel to select among. 
If we explicitly tell Camel to use the print method, we are still left with two methods to choose from. 
```
  - When compiler compiles below code, doesn't throw any issue if we misspelled the method name from print to prent.
  - During compile time, only validation happens is on the paramter passed to bean() method.
  - During runtime, if the method name is misspelled there will be a `MethodNotFound` exception will be thrown, not prent method exits.
  - To avoid that, we can only pass the class name to bean() method. As mentioned below.
```java
    from("direct:start").bean(PrintBean.class, "print").to("log:reply");  // method name passed as parameter
    from("direct:start").bean(PrintBean.class).to("log:reply");  //No method name passed, and Camel has to resolve this using method-selection algorithm
```
 #### Method-selection algorithm at very high level
   - If the method name is mentioned in `CamelBeanMethodName` header use it. Also check if the method exists in the bean.
   - If the method name is explicitly define use it. Also check if the method exists in the bean.
   - If method name not specified, check if the message body can be converted to Processor and execute it.
   - If method name not specified, check if hte message body is of class BeanInvocation class and execute it.
   - If method name not specified, if there is only one method annotated with `@Handler` use that method.
   - If method name not specified, check the if there is only one method with only one parameter.
   - If method nmae not specified, find best matching method and use it.

#### `@Handler` annotation
   - With the above method-selection algorithm, lets check how the below bean method will be resolved
```
  from("direct:start").bean(PrintBean.class).to("log:reply");
```
  - Lets say the PrintBean class has below two methods with same type of arguments. 
 ```java
  public class PrintBean {
     public String print(String message){
        return "print: " + name;
       }
       
     public String hello(String name){
        return "hello "+ name +" !!";
       }
  }
 ```
  - In this case the Camel will throw, `AmbigiousMethodCallException` exception.
 
 - **`How to resolve the above issue and make Camel to invoke correct method`**.
   - By explicitly specifiying the method name in the bean() method. `from("direct:start").bean(PrintBean.class,"hello").to("log:reply");`
   - Another way is to use `@Handler` annotation on one method, so will invoke this method by default.
     - The @Handle is Camel specific annoation.
 
 #### Bean parameter binding
 - How Camel adapts to the parameter on the method signature. Any bean can have multiple parameters and camel has to pass meaningful values. This process is know as `bean parameter binding`.
