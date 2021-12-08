When creating a standalone spring application below scope are applicable
  - singleton  (spring default scope)
  - prototype 

##### Difference between GOF singleton vs Spring singleton.
  - GOF singleton, specifies through out the application there will only one instance. for example in java application in the JVM there will be one instance of the class.
  - Spring singleton, there will be only one instance per ApplicationContext.


##### `context.registerShutdownHook()` - This will make sure to close the context only when the main thread is closed. Where as `((ClasspathXmlApplicationcontext)context).close()` will close the context immidiately.

##### When injecting the prototype scope on a bean with singleton scope.
   - In this case, when Spring creates the bean instance it will add the prototype bean dependency.
   - Even though the prototype dependency is mentioned, the same class will be provided.
   - we can use proxymode to indicate the singletonscope bean, to use proxy
 ```
 @Scope(value="prototype",proxyMode = ScopedProxyMode.TARGET_CLASS )
 
 # alternate way to use above is 
 @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE,proxyMode = ScopedProxyMode.TARGET_CLASS)
 ```
 
 ##### Bean lifecycle notes:
   - by annotating the init method with `@PostConstruct` and destroy method with `@PreDestroy`. With `<context:annoation-config>` in the context.xml
      - note, in the above case, we can perform for example creation of database connection and closing the connection in predestroy option.
      - instead of using `<context:annotation-config/>` we can use the bean definition with `CommonAnnotationBeanPostProcessor` which will scan the @PostConstruct and @PreDestroy.
   - using `init-method` and `destroy-method` in the context.xml
   - by implementing InitalizeBean, DisposeBean, and corresponding `afterSetProperties()` and `destroy()` method. - This is not recommended approach.
   - We can also specify the `default-init` and `default-destroy` method at the <bean> schema defintion level in the spring context.
  

  ##### Web application scopes.
     - request  A bean will be created for all the http request if declared at bean level. ( request vs prototype - 
     - session
     - application (from spring 5.x+)
     - websocket  (from spring 5.x+)
  
  There can be multple dispatch servlet per spring application.
 
  
```
  
                    Servlet Context  (Application Scope), any bean with this scope will be accessed by all dispatch servlet's application context
                   /              \
        dispatchServlet (1)        dispatchServlet (2) ....   
          /                         \ 
        Application Context          Application Context 
```
  
####### Simple java config based Spring web application creation
  
  ```java 
  public class ApplicationInitializer1 implements WebApplicationInitializer{
    public void onStartup(ServletContext servletContext) throws ServletException{
      AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
      webApplicationContext.register(AppConfig.class);
      DispatchServlet ds1 = new DispatchServlet(webApplicationContext);
  
     ServletRegistration.Dynamic customDispatchServlet = servletContext.addServlet("customdispatchservlet",ds1);
     customDispatchServlet.setLoadOnStartup(1);
     customDispatchServlet.addMapping("/application1/*");
     
    }
  }
  
  @EnableWebMvc
  @Configuration
  @ComponentScan(basePackages = {"com.mycode.api"})
  public class AppConfig implements WebMvcConfigurer{
  
  }
  ```
