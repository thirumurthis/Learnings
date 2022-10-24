## Notes Using Java `Optional`

 - Don't return null, instead of empty object 
  - Say if we are about to return a collection of objects, then return empty list like below
    - We can iterate using empty collection like 
    ```
    public List<Customer> getCustomer(long id){
       // if customer for id not present in database 
       // return empty object like 
       
       return List.of();
    }
    ```

  - how to manage in case if the method to return single value, then return Optional
    ```
    public Optional<String> getUserName(long id){
      // if the name is not found then 
      return Optional.empty();
      // don't return null
    }
    ```
    - When we obtain the value from the Optional then use
       - Don't use the optional.get(), since it will sometime return null
       ```
         System.out.println(Optional.orElse("value not found"));
       ```
  - If a function always return a single value, and it always exists DON'T use Optional.
  - If a method may NOT return a single value then use Optional. 
  - Also if a function returns a collection, then DON'T return Optional. Since for collection we can send empty collection.
  - Don't use Optional<T> as parameter to functions, mostly use overloading
      - Also say if a function is going to perform a default action if the passed argument is null, then don't use optional.
  
      ```
      // don't use Optional like below
      public void setName(Optional<String> name){
         if(name.isPresent()){
            // handle different logic
         }else{
           // perform default operation
         }
      }
      // The usage would be,and every time the client code should use optional wrapped around 
      
      setName(Optional.empty());
      setName(Optional.of("userName"));
      
      ```
      - Handle the above case like below
      ```
      // to handle default action use below method
      public void setName(){
       // do something
      }
      
      public void setName(String name){
       // do something
      }
      ```
  - Optional can be used to field
  
 ---------------------
  ## Notes on the Iteration pattern using lambda
 
  - Iteration pattern
   - External Iterator
      - like `for (int i= 0; i <= N; i++){...}`
      - also, `for(var item : items){....}`, then we do use break, etc.
      
   In general we do alter the flow.
   
   - Internal Iterator
     - with the streams, we don't alter the flow instead create a pipeline
     - in functional programming we don't have statements like break, it is mostly expression. 
     - from Java 9, we use `takeWhile()` with the predicate
     - from Java 8, we had `limit()` which can be used as break (like in the imperative style)
     ```
     items.stream()
       .filter(name -> name.length() == 5)
       .map(String::toString)
       .forEach(System.out::println);
     ```
     - when using the functional programming, don't modify or mutate the object that is defined outside.
     - **Try to keep the function pure and immutable**
     - **Avoid shared mutable variables**
     ```
      var itemsToUpper = new ArrayList<String>();
      
      items.stream()
      .filter(name -> name.length() == 4)
      .map(String::toUppercase)
      .forEach(name -> itemsToUpper.add(name)); // don't mutate the object outside
      
      // If some other developer change the stream to parallelStream()
      // it will cause issue unknow side effects
      
      items.stream()
      .filter(name -> name.length() == 4)
      .map(String::toUppercase)
      .toList(); // create a new List of object
     ```
 ---------------------
 
 ## Strategy Pattern

- Using lambda as a light weight strategies

```java

public class Demo{
public static int computeTotal (List<Integer> inputNumbers, Predicate<Integer> selector){
    int total =0;
    
    for(var number : inputNumbers ){
       if( selector.test(number)){
          total += number;
       }
    }
    return number;
}

public static boolean isOddNum(int number){
  return number%2 != 0;
}

public static void main (String ... args){
    var inputNumbers = List.of(1,2,3,4,5,6,7,8,9,10);
    
    int totalValue = computeTotal (inputNumbers, ignore -> true);//55
    totalValue = computeTotal (inputNumbers, even -> even%2 == 0); // even sum
    totalValue = computeTotal (inputNumbers, odd -> odd%2 !=0); // odd sum
    // alternate using method reference
    totalValue = computeTotal (inputNumbers, Demo::isOddNum); //odd sum    
 }
}
```
 - The `computeTotal()` method can be converted completely to functional style like below
 
 ```
 public static int computeTotalFunc(List<Integer> numbers, Predicate<Integer> selector){
    return numbers.stream().filter(selector).mapToInt(item -> item).sum();
 }
 ```

 --------------------------
  ## Factory Method pattern with lambda

- Factory method using default method

- interface vs abstract class:
   - The golden rule that interface are better than abstract classes.
   - Interface can have implementation but they can't have non final fields.
      - i.e. Interface cannot carry state, but abstract class can carry state.

The factory method can help us dealing with the default methods in interfaces

```
package com.kafka.example.firstapp.dp;


interface Department{
    // we are about to treat this interface as a factory
    // say defining a private variable in interface is not possible
    // so below is not going to work
    // private Accounts account;
    // and we try to print the value in the default method below

    // in this case we can define a method like
    // this abstract method

    Accounts getAccounts();

    default void belongsTo(){
        System.out.println("from department "+getAccounts()); //using abstract method
    }
}

interface Accounts{}
class Banker implements Accounts {}
class Teller implements Accounts{}

class PublicBank implements Department{
    private Banker banker = new Banker(); //using the accounts implementation

    public Accounts getAccounts() {
        return banker;
    }
}

class PrivateBank implements Department{

    private Teller teller = new Teller(); //using the accounts implementation
    public Accounts getAccounts() {
        return teller;
    }
}

public class Demo{

  public static void fromWhere(Department department){
     department.belongsTo();
  }
  
  public static void main(String ... args){
     fromWhere(new PublicBank()); // prints: from department <package>Banker@76fb509a
     fromWhere(new PrivateBank());// prints: from department <package>Teller@4d405ef7
  }
}
```

- Abstract Factory is uses delgation as design tool.
- Factory Method uses inheritance as a design tool.

----------------
## Lazy evaluation using Functional programming

- short circuiting

```
public class Demo{

 public static int calculate(int value){
    System.out.println("calculate method called");
    return value * 100;
 }
 
 public static void main(String ... arg){
   int value = 5;
   
   // Since we are using && when the first condition is not 
   // met, the compiler won't execute the compute() method 
   // this is called short-circuiting
   
   if( value > 5 && compute(value) > 100){ 
     // if we store the compute to a variable, the compiler will
     // evaluated egaerly, but using them directly in the if 
     // it will evaluate lazily
   
      System.out.println(" value greater than 5");
   }else{
      System.out.println(" value smaller than 5");
   }
 }
}
```
- using the Lambda's

```
class Lazy<T>{
   private T instance;
   private Supplier<T> supplier;
   
   public Lazy<T>(Supplier<T> supplier){
     this.supplier = supplier;
   }
   
   public T get(){
     if(instance == null){ 
       // with the != you get the null pointer if the condition is matched
       // so it proves that this method is not invoked by the below lazy code
        instance = supplier.get();
     }
     return instance;
   }
}

public class Demo{

    public static void main(String ... args){
       int value = 5;
       // Below will still perform an eager evaluation
       Lazy<Integer> temp = new Lazy(calculate(value));
       
       // We can make the evaluation lazy by passing lambda
       Lazy<Integer> evalLazy = new Lazy(() -> calculate(value));
       
       if(value > 5 && evalLazy.get() > 100){
          System.out.print("if block");
       }else{
          System.out.print("else");
       }
    }
}
```
- If we need to postponed the evaluation we can pass a functional inteface to a method.

 --------------
 
## Decorator pattern

 - Functions are composable
 
```
public class ComposeFunc{
  
  public static void print(int number, String message, Function<Integer,Integer> func){
    System.out.println(number +" "+message + ": " + func.apply(number));
  }
  
  public static void main(String ... args){
     Function<Integer, Integer> increment = num -> num+1;
     Function<Integer, Integer> double = num -> num *2;
     
     print(10,"increment",increment);
     print(10,"double",double);
     
     //using function composition usage
     //more like of combining function
     print(10,"increment and double",increment.andThen(double));
  }
}
```

```

import java.awt.Color;
class Camera{
  private Function<Color, Color> filter;
  
  public Camera(Function<Color,Color>... filters){
    //filter = input -> input;
    //Below is just creating a pipeline for the function and trying to 
    //reduce multiple function to single function
    filter = Stream.of(filters)
             .reduce(Function.identity(), Function::andThen);
  }
  
  public Color snap(Color input){
     return filter.apply(input);
  }
}

public Class Demo{

  public static void print(Camera camera){
    System.out.println(camera.snap(new Color(125,125,125)));
  }
  pubic static void main(String ... args){
    print(new Camera());
    print (new Camera(Color::brighter));
    
    // combine more function
    print(new Camera(Color::brighter,Color::darker));
  }
}
```

- Above is helpful in case where we have a flow of data and we need to perform validation, transform and encrypt, etc. This decorator will be helpful. 

---------
 
