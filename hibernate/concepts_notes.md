

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
  @OneToMany(mappedBy="employee")               // If we didn't specify the mappedBy, then a mapper table will be created which we don't want in this case
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
