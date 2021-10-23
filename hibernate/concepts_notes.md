

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
       - `@Cacheable` - This tells hibernate that this entity is eligible for caching
       - `@Cache` - This is used to configure chace propertis like evict, etc.

 - To configure second level cache using ehcace, `hibernate.cfg.xml`
```xml
<hibernate-configuration>
   <session-factory>
     <property name="hibernate.connection.driver_class"><!-- update corresponding driver oracle or mysql --></property>
     <property name="hibernate.connection.url"><!--url info --></property>
     <property name="hibernate.connection.username"><!--user info --></property>
     <property name="hibernate.connection.password"><!-- info --></property>
     <property name="hibernate.connection.dialect"><!--dialect info --></property>
     <property name="hbm2ddl.auto">update<!--other values is create --></property>
     <property name="show_sql">true</property>
     
     <!-- for secondary level cache 1. enable it -->
     <property name="hibernate.cache.user_second_level_cache">true</property>
     <!-- specify the provider for the secondary level cache -->
     <property name="hibernate.cache.region.factory_class"> org.hibernate.cache.ehcache.EhcacheRegionFactory </property> <!-- hibernate 4 uses -->
   </session-factory>
</hibernate-configuration>
```
 - sepcify the `@Cacheable` over the entity 
 - `@Cache` for configuring it with option Read-only, Read-Write, none, transactonal
 
```java
@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_ONLY)
class Employee{
  @Id
  private int eid;
  private String name;
  @OneToMany(mappedBy="employee")
  private List<Laptop> laptop;
}
```
 - with the above configuration now, running with multiple session will fire query to DB only once.

### How to cache at the custom query
 - with the secondary level cache configured
```java
//main method 
    Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
    ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
    SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
    //Session 1
    Session session1 = sessionFactory.openSession()
    session1.beginTranscation();

    //defining custom query 
    Query query = session1.createQuery("from Employee where eid =101");
    // caching the result
    Employee e1 = query.uniqueResult(Employee.class, 101);   // using query object
    
    System.out.println(e1); 
    session1.getTransaction().commit();
    session1.close();
    
    // session 2
   Session session2 = sessionFactory.openSession()
    session2.beginTranscation();
   
    //defining custom query 
    Query query2 = session2.createQuery("from Employee where eid =101");
    Employee e2 = query2.uniqueResult(Employee.class, 101);   // using query object
    System.out.println(e2); 

    session2.getTransaction().commit();
    session2.close();
  // The above example will fire two queries since we are using custom queries.
  // In order to make use of secondary level cache for queries (refer below point)
```
  - To use the cache for the custom query, update the hibernate.cfg.xml with below property
 ```xml
<property name="hibernate.user_query_cache">true</property>
 ```
  - Then update the custom query object with `query.setCacheable(true)` in the transaction. (use this in the above code)

### HQL
  - In order to use complex query, joining multiple table.
  ```
   select <property-of-class> from <entity-class-name>
  ```
  - comparing SQL to HQL
  ```
  //sql 
  select * from Employee;
  
  //HQL
  from Employee;
  ```
  - Using sql in the hibernatives is called `native queries`
  - Using HQL, when executed will return the list of object, not the result set as in the native sql.
  - 
```
//main method 
    Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
    ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
    SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
    //Session 1
    Session session1 = sessionFactory.openSession()
    session1.beginTranscation();

    //defining custom query 
    Query query = session1.createQuery("from Employee");
    List<Employee> e1 = query.list();
   
   session1.getTransaction().commit();
    session1.close();
```
  - Note if we need to use specific columns in the HQL `select eid,name from Employee` and the result will be stored in and Object[]

```java
    Query query = session1.createQuery("select eid, name from Employee where eid =101");
    Object[] e1 = (Object[])query.uniqueResult();
    System.out.println("employee"+ e1[0]);
``` 
#### How to bind java vairable to the HQL query

```java
    int val = 101
    Query query = session1.createQuery(" from Employee where eid = :employeeid"); // using :
    query1.setParameter("employeeid",val);
    Employe e1 = (Employee)query.uniqueResult();
    System.out.println("employee"+ e1);
```

### How to use the Native SQL query
```java
//main method 
    Configuration config = new Configuration().configure().addAnnotatedClass(Employee.class).addAnnotatedClass(Laptop.class);
    ServiceRegistry sRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();
    SessionFactory sessionFactory = config.buildSessionFactory(sRegistry);
    //Session 1
    Session session1 = sessionFactory.openSession()
    session1.beginTranscation();

    SQLQuery query = session1.createSQLQuery("select * from Employee where eid > 101");
    // to convert the result to entity 
    query.addEntity(Student.class)
    
    List<Employee> e1 = query.list();
    e1.forEach(System.out::print)
    session1.getTransaction().commit();
    session1.close();
```
#### How to assign the specific rows of native sql query into a object
```java

    SQLQuery query = session1.createSQLQuery("select name,info from Employee where eid > 101");
    // convert the result to map
    query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
    
    List e1 = query.list();
    for (Object obj: e1){
      Map m = (Map) obj;
      System.out.println(m.get("name"));
      }
    session1.getTransaction().commit();
    session1.close();
```

### Persistent life cycle/ Object states
```
 - an object in java has 
     - newly instatiate state  (using new keyword)
     - destroy state (end state)

 - an object in hiberate
     - newly instatiate state  (using new keyword)
     - Transient state - When the entity class object is created it will be in transit state. In this state if the application/jvm is closed the data will be lost
     - Persistent - If we issue save() or persist() over the Entity class object, it will be in persistent state. In this state the data will be stored in database as well. The object in persistent state and any change to the object in this state will be written to database.
            - using get() or find() over the entity class object, the object will be in persistent state 
     - detached state - If the object is in persistent state which update the database everytime the object state changes. If we need to remove the persistent state on the object then it will be moved to detached state. using detach(). This is a way like Transient state, since any change to entity class is not written to DB.
     - remove state - when issuing delete() or remove() on the entity class object, the data is removed from the database but not from the java. The object is in removed state
```
