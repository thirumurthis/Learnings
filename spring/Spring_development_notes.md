#### How to set browser cache in spring application
```java
//in controller method
public List<Employee> getAllEmployees(final HttpServeletResponse response){
 response.setHeader("Cache-Control", "private, max-age=432000, stale-while-revalidate=432000"); // cache for 5 days 3600*24*5
 //perform business logic
 return employeeService.getAllEmployees();
}

```
