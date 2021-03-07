#### Below are different example, obtained from (source)[https://www.javarticles.com/2015/07/apache-camel-recipientlist-examples.html]

- usage with camel 2.15.1
- requires `camel-code`, `slf4j-api`, `slf4j-log4j12`,`camel-stream`,`camel-spring` dependency to execute,

```java
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
 
public class CamelRecipientListExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:start")
                    .recipientList(header("departments"));
                     
                    from("direct:account")
                    .log("Account department notified '${body}'");
                     
                    from("direct:hr")
                    .log("HR department notified '${body}'");
                     
                    from("direct:manager")
                    .log("Manager notified '${body}'");
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            template.sendBodyAndHeader("direct:start", "Sam Joined",
                    "departments", "direct:account,direct:hr,direct:manager");
        } finally {
            camelContext.stop();
        }
    }
}
```
```java
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
 
public class CamelRecipientListHeaderProcessorExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:start")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            String recipients = "direct:hr";
                            String employeeAction =
                            exchange.getIn().getHeader("employee_action", String.class);
                             if (employeeAction.equals("new")) {
                                recipients += ",direct:account,direct:manager";
                             } else if (employeeAction.equals("resigns")) {
                                 recipients += ",direct:account";
                             }
                             exchange.getIn().setHeader("departments", recipients);
                           }
                      })
                      .recipientList(header("departments"));
                     
                     
                    from("direct:account")
                    .log("Account department notified '${body}'");
                     
                    from("direct:hr")
                    .log("HR department notified '${body}'");
                     
                    from("direct:manager")
                    .log("Manager notified '${body}'");
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            System.out.println("************************");
            template.sendBodyAndHeader("direct:start", "Sam Joined",
                    "employee_action", "new");
            System.out.println("************************");
            template.sendBodyAndHeader("direct:start", "John Resigned",
                    "employee_action", "resigns");
            System.out.println("************************");
        } finally {
            camelContext.stop();
        }
    }
}
```

- Using tokenizer

```java
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
 
public class CamelRecipientListTokenizerExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:start")
                    .recipientList(header("departments").tokenize(">"));
                     
                    from("direct:account")
                    .log("Account department notified '${body}'");
                     
                    from("direct:hr")
                    .log("HR department notified '${body}'");
                     
                    from("direct:manager")
                    .log("Manager notified '${body}'");
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            template.sendBodyAndHeader("direct:start", "Sam Joined",
                    "departments", "direct:account>direct:hr>direct:manager");
        } finally {
            camelContext.stop();
        }
    }
}
```
- Ignore invalid Endpoint part of recpientlist
```java
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
 
public class CamelRecipientListIgnoreInvalidEndpointsExample {
    public static final void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:start")
                    .recipientList(header("departments")).ignoreInvalidEndpoints();
                     
                    from("direct:account")
                    .log("Account department notified '${body}'");
                     
                    from("direct:hr")
                    .log("HR department notified '${body}'");
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            template.sendBodyAndHeader("direct:start", "Sam Joined",
                    "departments", "direct:account,direct:hr,some:unknownEndpoint");
        } finally {
            camelContext.stop();
        }
    }
}
```
-------------
- Dynamic RecipientList
```java
public class Employee {
    private boolean isResigning;
    private boolean isNew;
    private boolean isOnLeave;
    private boolean isPromoted;
    private String msg;
    private String name;
     
    public Employee(String name) {
        this.name = name;
    }
// Getter AND Setter are not included
     
    public String toString() {
        return "Employee " + name + " " + msg;
    }
}
```

```java
public class EmployeeRouter {
    public String[] routeEmployee(Employee emp) {
        if (emp.isResigning()) {
            return new String[]{"direct:account", "direct:hr"};
        }
        if (emp.isNew()) {
            return new String[]{"direct:manager", "direct:account", "direct:hr"};
        }
        if (emp.isOnLeave()) {
            return new String[]{"direct:manager", "direct:hr"};
        }
        if (emp.isPromoted()) {
            return new String[]{"direct:hr", "direct:account"};
        }
         
        return new String[]{"direct:hr"};
    }
}
```

```java
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
 
public class CamelRecipientListRouterExample {
    public static final void main(String[] args) throws Exception {
        JndiContext jndiContext = new JndiContext();
        jndiContext.bind("empRouter", new EmployeeRouter());
        CamelContext camelContext = new DefaultCamelContext(jndiContext);
        try {
            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:start")
                    .recipientList(method("empRouter", "routeEmployee"));
                     
                    from("direct:account")
                    .log("Account department notified '${body}'");
                     
                    from("direct:hr")
                    .log("HR department notified '${body}'");
                     
                    from("direct:manager")
                    .log("Manager notified '${body}'");
                }
            });
            ProducerTemplate template = camelContext.createProducerTemplate();
            camelContext.start();
            System.out.println("************************");
            Employee sam = new Employee("Sam");
            sam.setNew(true);
            sam.setMessage("Joined");
             
            template.sendBody("direct:start", sam);
             
            System.out.println("************************");
             
            Employee john = new Employee("John");
            john.setOnLeave(true);
            john.setMessage("On Leave");
            template.sendBody("direct:start", john);
             
            System.out.println("************************");
             
            Employee roy = new Employee("Roy");
            roy.setPromoted(true);
            roy.setMessage("Promoted");
            template.sendBody("direct:start", roy);
             
            System.out.println("************************");
             
            Employee ram = new Employee("Ram");
            ram.setResigning(true);
            ram.setMessage("Resigning");
            template.sendBody("direct:start", ram);
             
            System.out.println("************************");
        } finally {
            camelContext.stop();
        }
    }
}
```
---------------
 - Recipientlist using Spring DSL
- applicationcontext.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
 
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd
       ">
    <bean id="empRouter" class="com.javarticles.camel.EmployeeRouter" />
 
    <camelContext xmlns="http://camel.apache.org/schema/spring">
        <route>
            <from uri="direct:start" />
            <setHeader headerName="departments">
                <method ref="empRouter" method="routeEmployee" />
            </setHeader>
            <recipientList ignoreInvalidEndpoints="true">
                <header>departments</header>
            </recipientList>
        </route>
 
        <route>
            <from uri="direct:account" />
            <log message="Account department notified '${body}"/>
        </route>
 
        <route>
            <from uri="direct:hr" />
            <log message="HR department notified '${body}"/>
        </route>
 
        <route>
            <from uri="direct:manager" />
            <log message="Manager notified '${body}"/>
        </route>
 
    </camelContext>
 
</beans>
```
- camel standalone spring execution
```java
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
public class CamelRecipientListExampleUsingSpring {
    public static final void main(String[] args) throws Exception {
        ApplicationContext appContext = new ClassPathXmlApplicationContext(
                "applicationContext.xml");
        CamelContext camelContext = SpringCamelContext.springCamelContext(
                appContext, false);
        try {
            ProducerTemplate template = camelContext.createProducerTemplate();           
            camelContext.start();
            System.out.println("************************");
            Employee sam = new Employee("Sam");
            sam.setNew(true);
            sam.setMessage("Joined");
             
            template.sendBody("direct:start", sam);
             
            System.out.println("************************");
             
            Employee john = new Employee("John");
            john.setOnLeave(true);
            john.setMessage("On Leave");
            template.sendBody("direct:start", john);
             
            System.out.println("************************");
             
            Employee roy = new Employee("Roy");
            roy.setPromoted(true);
            roy.setMessage("Promoted");
            template.sendBody("direct:start", roy);
             
            System.out.println("************************");
             
            Employee ram = new Employee("Ram");
            ram.setResigning(true);
            ram.setMessage("Resigning");
            template.sendBody("direct:start", ram);
             
            System.out.println("************************");
        } finally {
            camelContext.stop();
        }
    }
}
```
