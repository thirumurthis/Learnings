

| Comparable | Comparator |
|----------|------------|
| Comparable sorts based on one properties of the class. `single sorting sequence` | Comparator provides support to `multiple sorting sequences` many properties in object |
| Comparable `modifies the original class`, we need to implement the class and override `compareTo()` | Comparator doesn't modify the original class |
| `compareTo()` method used for sorting | `compare()` method used for sorting |
| usage: `Collections.sort(List<>)` | Usage `Collections.sort(List<>, Comparator)` |
| Exists in `java.lang` package | Exists in `java.util` package |


##### Code implementation:
**Comparable**
```java
class Employee implements Comparable<Employee> {
   private int id;
   private String name;
   // constructor with args 
   //getter setter
   
   /*
   DRAWBACK: 
     if we need to change the sorting based on different field,
     this class needs to be changed. It is always recommended to use Comparator
   */
   @Override
   public int compareTo(Employee emp){
     // only applicable to one field
     if(id > emp.id) { return 1; }
     else if( id < emp.id) { return -1;}  // if less return -1
     else return 0;  // if the id and emp.id are equal;
   }
   
   public static void main(String ... args){
     List<Employee> empList1 = new ArrayList<>();
     Employee emp1 = new Employee (9,"Nine");
     Employee emp2 = new Employee (3, "Three");
     Employee emp3 = new Employee (5, "Five");
     empList1.add(emp1);
     empList1.add(emp2);
     empList1.add(emp3);
     System.out.println(empList1); // this will not print in sorted order
     // to apply sorting 
     Collection.sort(empList1);
     System.out.println(empList1); //prints in sorted order
   }
}
```
** Comparator **
  - In this case we can create a separate class for each id's.
```java
class Employee implements Comparable<Employee> {
   private int id;
   private String name;
   // constructor with args 
   //getter setter
}

class EmployeeIdComparator implements Comparator<Employee>{

 @Override
 public int compare (Employee emp1, Employee emp2){
    if (emp1.id > emp2.id) return 1; 
    else if (emp1.id < emp2.id) return -1;
    else return 0;   //ids are equal
 } 
}

class EmployeeNameComparator implements Comparator<Employee>{

 @Override
 public int compare (Employee emp1, Employee emp2){
    // simply using comparTo method of String class here
    return emp1.getName().compareTo(emp2.getName());
 } 
}

public class Test {
  public static void main(String ... args){
     List<Employee> empList1 = new ArrayList<>();
     Employee emp1 = new Employee (9,"Nine");
     Employee emp2 = new Employee (3, "Three");
     Employee emp3 = new Employee (5, "Five");
     Employee emp4 = new Employee (3, "SecondThree");
     empList1.add(emp1);
     empList1.add(emp2);
     empList1.add(emp3);
     empList1.add(emp4);
     
     System.out.println(empList1); // this will not print in sorted order
     // to apply sorting 
     Collections.sort(empList1, new EmployeeIdComparator());
     System.out.println(empList1);
     Collections.sort(empList1, new EmployeeNameComparator());
     System.out.println(empList1);
     Collections.sort(empList1, new EmployeeIdNameComparator()); // mulitple field sorting
     System.out.println(empList1);
   }
}
```
- Using comparator to sort based on second field. Say if the `id` field are equal then sort based on the next `name` field

```java

class EmployeeIdNameComparator implements Comparator<Employee>{

 @Override
 public int compare (Employee emp1, Employee emp2){
    if (emp1.id > emp2.id){
       return 1;
    }else if (emp1.id < emp2.id){ 
       return -1;
    } else { //ids are equal condition
       // if the ids are equal then perform to sort the name element like below instead of returning 0
       return emp1.getName().compareTo(emp2.getName());
    }   
  } 
}
```
