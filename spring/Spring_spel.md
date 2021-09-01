- Create a Spring boot project using `spring web` and `spring Aop` depenecies

- `@component` and `@Bean` annotation can be used to tell spring to scan those class/bean.

```java
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
}
// implict way
@Component
class Car(){
  private final Foo foo; //spring will deduce this since we annotated with @Component
  Car(Foo foo){
     this.foo = foo;
    };
}

@Component
class Foo(){}
```

- how to tell explicitly to spring to inject 

```java
@SpringBootApplication
public class SpringBootDemo{
  public static void main(String ... args){
     SpringApplication.run(SpringBootDemo.class, args); 
  }
  @Bean
  Foo foo(){
    return new Foo();
  }
   // doing this we removed the @comonent annotation
  @Bean
  Car car(Foo foo){
    return new Car(foo);
  }
}

class Car(){
  private final Foo foo; //spring will deduce this since we annotated with @Component
  Car(Foo foo){
     this.foo = foo;
    };
}

class Foo(){}
```

