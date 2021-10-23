

### How to group similar poperties into an object, and refer that in Entity class?
  - using `@Embeddable` annotation
```java 
@Embeddable
class UserName{
   private String fname;
   private String mname;
   private String lname;
   // getter setters
}

@Entity 
class User{
  @Id
  private int id;
  private UserName name;
  private String notes;
  //getter setter
}
```

### How to create a one-to-one relation ship?
  - using `@OneToOne` annotation
  - in below case lid will be created in the Employee table
```java
@Entity
class Laptop{
  @Id
  private int lid;
  private String laptopName;
}

@Entity
class Employee{
  @Id
  private int eid;
  private String name;
  @OneToOne
  private Laptop laptop;
}
```

### how to create a one-to-many relation ship?
 - Using Mapper table, below configuration will create a Mapper table
```java
@Entity
class Laptop{
  @Id
  private int lid;
  private String laptopName;
}
@Entity
class Employee{
  @Id
  private int eid;
  private String name;
  @OneToMany
  private List<Laptop> laptop;
}
```
 - If we don't want to create a mapper table, and load the Employee eid part of laptop object
   - use `@ManyToOne` annotation, and update the `@OneToMany` annotation with `mappedBy` properties with the @ManyToOne variable name
   - eid will be created in the Laptop table
```java
@Entity
class Laptop{
  @Id
  private int lid;
  private String laptopName;
  
  @ManyToOne   //this needs to be specified to say create a new column to store the employee eid in the laptop table
  private Employee employee
}
@Entity
class Employee{
  @Id
  private int eid;
  private String name;
  @OneToMany(mappedBy="employee")     // If we didn't specify the mappedBy, then a mapper table will be created which we don't want in this case
  private List<Laptop> laptop;
}
```

### How to create a many-to-may relation
```java
@Entity
class Laptop{
  @Id
  private int lid;
  private String laptopName;
  @ManyToMany
  List<Employee> employee;
}
@Entity
class Employee{
  @Id
  private int eid;
  private String name;
  @ManyToMany(mappedBy="employee")   // Note: if the mappedBy is NOT provied there will be sperate tables (laptop_employee table and employee_laptop table)
  private List<Laptop> laptop;
}

```

### How to insert or query the Database using Hibernate.
 - 1. Use Configuration class to configure the hibernate.cgf.xml file with database info
 - 2. Create a ServiceRegistry object passing the configuration properties
 - 3. Create a session from session factory by using service registry
 - 4. Begin transcation
 - 5. use `save` or `get` to insert or fetch data respecively
 - 5. commit transcation

```java

Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
Session session = sessionFactory.openSession()
session.beginTranscation();
session.save(employee); //An employee object is already created and stored in variable employee
session.getTransaction().commit();

 // possibly begin new transcation
 Employee e1 = session.get(Employee.class, 101); //101 - is eid stored in db
```

### Fetch Stratergy EAGER , LAZY
- `LAZY` fetching example.
- From the above relationship entity example, for below case lets assume @OneToMany relationship with no mapper table is used where Employee can have list of Laptops.
```java
Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
Session session = sessionFactory.openSession()
session.beginTranscation();

Employee e1 = session.get(Employee.class, 101);  // Note: toString() method is overrided in Employee class
System.out.println(e1.toString()); // This will print only the Employee table information, this will not FETCH the collection of laptop

//to print the laptop collection
Collection<Laptop> laptop = e1.getLaptop(); // Only at this time the collection query is fired and data is fetched which is LAZY fetch
System.out.println(laptop.toString());

session.getTransaction().commit();

```
- How to make the fetchtype as EAGER?
  - In the `@OneToMany(mappedBy="employee")` annotation include the `fetch` property
  - Note, this would be based on the requirement to enable EAGER fetch
```java

@Entity
class Laptop{
  @Id
  private int lid;
  private String laptopName;
  
  @ManyToOne   //this needs to be specified to say create a new column to store the employee eid in the laptop table
  private Employee employee
}
@Entity
class Employee{
  @Id
  private int eid;
  private String name;
  @OneToMany(mappedBy="employee",fetch=FetchType.EAGER)     // If we didn't specify the mappedBy, then a mapper table will be created which we don't want in this case
  private List<Laptop> laptop;
}

//main method 
Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
Session session = sessionFactory.openSession()
session.beginTranscation();

Employee e1 = session.get(Employee.class, 101);  // Note: toString() method is overrided in Employee class
System.out.println(e1.toString()); // Since EAGER is set, now the single query with LEFT OUTER join will be used to fetch the data of Collection

session.getTransaction().commit();
```

### Hibernate `caching`
 - Hibernate provides us with caching feature.
##### First level caching
   - First level caching (Default one provided by hibernate)
     - Say when a select query i fired and records are fetched, those data will be stored in the cache. This is first level cache
     - This first level cache is available for one particular session.
     - So when a new session either created by the same user or different user in an application will have its own first level cache.

   In the below code, enabling show_sql = true, only one query will be fired, and second get will get data from cache.
 
 ```java
    //main method 
    Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
    ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
    SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
    Session session1 = sessionFactory.openSession()
    session1.beginTranscation();

    Employee e1 = session1.get(Employee.class, 101);  // checks first level cache, if not then checks second level cache is configured and if data not exists will fetch from DB
    System.out.println(e1.toString()); 
    
    Employee e2 = session1.get(Employee.class, 101);  
    System.out.println(e1.toString()); 
    session.getTransaction().commit();

    // In the above case if the hibernate.cfg.xml -> set with show_sql property as true
    // There will be only the select query only
```
##### Secondary level caching
   - Secondary level caching (This needs to be configured)
     -  In secondary level caching, multiple session can use this cache
     -  In order to configure secondary level cache, we need to use 3rd party providers like EHCACHE, OS, SWARM. (ehcache - is more preferred)
 - Configuring Secondary level cache:
    - include the `ehcache` library, hibernate-ehcache libs.
    - update the `hibernate.cfg.xml`
    - In the Entity that needs to be cached requires an annotation 
       - `@Cachable` - This tells hibernate that this entity is eligible for caching
       - `@Cache` - This is used to configure chace propertis like evict, etc.



