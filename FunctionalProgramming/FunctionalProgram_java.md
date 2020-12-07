### Functional programming 
  - functional programming is a way of organizing code that makes more scalable and maintainable.
  - Note functional programming and object oriented programming are NOT opposites.
  - In some areas, the OOPs programming has some drawback where the Functional programming helps.
     - In OOPs programming, in large programs it is difficult to recreate, in functional programming helps write bug free code.
  - Functional programming brings the mathematical approach. like in maths having a f(x) = x+1, where there is no possible bugs in this function.
  
  
##### Declaritive and imperative:
  __`Functional programming`__ is Declaritive style of programming.
     
   **Declaritive programming** - focus on What things are? (more like using the existing function)
```
       x = averate(inputArray); // the average() is already defined part of the program
```

   **Imperitive programming** - focus How to do things. (mostly like steps reqiured.)

```java
      x = 0;
      for (int i =0; i < inputarray.length(); i++)
         x = x+inputarray[i];
      x = x/inputarray.length(); // get averate of an serias of number
```
     
 #### Concepts of Functional programming
    - Immutability
    - Functional purity 
    - first-class functions

##### Immutability
  - in functional programming when the variable x is set to 5, like final int x=5; it is not changed further.
  - mostly the variable should be not change further, like PI in a programing language, where this value is not changed.
  - In functional programming, if we declare an employee object with name and age and wanted to update the age at
    latter point of time, we create a new object rather updating the same object.
  - Doing this is to avoid `state change`
  - since functional programming starts with immutable set of data, as `single source of data`.
  - advantage: 
      - the original data in a program is available and not changed.
      - program constructed is more easy to keep track of
  
##### Purity
   - a function should return the same output.
   
   ```
   public class Employee{
     private int age;
     public int getAge(){..
     
     public void setAge(){..
     
     public String toString(){
        return "age: "+this.age;
     
    # In the above scenario, the setter method for age changes the value and so invoking the toString() won't yeild the same output.
   ```
   
   ```
   # below is a pure function
   
   public int add( int x, int y){ // for same set of input the output will return same expected value
      return x+y; 
   }
   ```

##### First class functions
 - in general OOPs, Data and function are different type of entity.
    - in OOPs like not possiblity of function returning function, or passing function as argument
 - with functional programming, it is possible to to do return function and pass function as arguments.
    - this provides capability
 
 #### Functional Interface:
   - Treat function like other type like String, Integer, etc. in java.

 - Define function as variable.
```java

public class Demo{
  protected status class MathOp{
     public static Integer triple(Integer x){
        return x*3;
        }
  }
pulic static void main(String ...arges){
   // Function <T,R> -> T is the generic usage, where T - is argument data type, R - Retun value data type
   Function<Integer,Integer> triple = MapthOp::triple;
   Integer output = triple.apply(5)
  }
} 
```

#### Lambda expression:
  - In previous section we created a inner class and invoked that method, instead of performing it we can use lambda.
  - Lambda expression is a way to define the functional interface. And short-hand definition where we don't need to use class to define a method.
     - declared like `(Integer someArgs) -> someArg * 3;` // the values after the arrow operator is returned automatically.
  - The above example can be converted like below
```java
  Function<Integer, Integer> triple = (Integer x) -> x * 3;
  // the function is taking Integer arugment and return type is also Integer.
  
  // another example
  Function<String, Integer> stringLength = (str) -> str.length(); //  we can drop the parathensis if there is only one argument.
  Function<String, Integer> stringLength = str -> str.lenght();
  
  // if there are multiple lines
  Function<Integer, Integer> triple = x -> {
     Integer output = x * 3;
     return output;
  }
```
 - Above triple program with lambda
```java
  public class Demo1{
  
   public static void main(String... args){
      Function<Integer, Integer> absValue = x -> x < 0 ? -x : x; // return absolute value
      
      System.out.println("abs value: "+ absValue(-100)); 
   }
  }
```

#### Note the above approach of using Function<T, R> supports only with one arguments passed.
#### How to use the Functonal interface with no arguments or more than one arguments.
#### `BiFunctions` is one interface provided in java for passing  two arguments, below is example usage.

```java 
public class Demo{

  public static void main(String ... args){
     BiFunction<Integer, Integer, Integer> add = (x,y) -> x+y;
     
     System.out.println("add : "+ add.apply(10,20));
   }
  }
```

#### For more than two argument, java didn't provide any interface in this case, we can define our own case.
 - Below is the implementation for No args and three args functional interface.
 
 ```java
 //------------- INTERFACE for FUNCTION
package com.test.functions;
public interface TriFunctions <T,U,V,R>{
	public R apply(T t,U u, V v);
}

 //------------- INTERFACE for FUNCTION
package com.test.functions;
public interface NoArgsFunction<R> {
	public R apply();
}

//------------- MAIN IMPLMENTATION

package com.test.functions;
public class ApplyFunction {
	public static void main(String[] args) {
  
		TriFunctions<Integer, Integer, Integer, String> trifunction = (x,y,z) -> "Sum of "+x+" + "+y+" + "+z+" = "+ (x+y+z);
   	  System.out.println(trifunction.apply(10, 20, 30)); //use apply to access the function
      
		NoArgsFunction<String> sayHello = ()->"hello";
			System.out.print(sayHello.apply());
	}
}
```

#### The usage of Function interface, is when in need of mockup data for development or testing, refer below example.
  - use the above NoArgsFunction Functional interface
  - based on the loadData function correspondimg method is loaded.
  
```java
package com.test.functions;

public class SampleFunctionalInterfaceUsage {
	
	public static class Employee{
		private String name;
		private int age;
		
		public Employee(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}
	
	public static class DataLoader{
		NoArgsFunction<Employee> loadData;
		
		public DataLoader(Boolean isDev) {
			this.loadData = isDev ? this::loadFakeData:this::loadRealData;
		}
		
		private Employee loadFakeData() {
			System.out.println("Fake data loader");
			return new Employee("FakeName",100);
		}
		
		private Employee loadRealData() {
			System.out.println("Real data loader");
			return new Employee("Real Name",35);
		}
	}
	
	public static void main(String ...args) {
		final Boolean IS_DEV = true;
		
		DataLoader dataload = new DataLoader(IS_DEV);
		System.out.println(dataload.loadData.apply());
	}
}
```

### Passing Functions as arguments
  - Function itself as function arguments.
  
```java 
package com.test.functions;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class PassFunctionAsArgsMain {
	
   public static Integer add(Integer a, Integer b) {
	   return a+b;
   }
	
   public static Integer diff(Integer a, Integer b) {
	   return a-b;
   }

   //Function which takes another function as argument, defined as parameter here.
   static Integer operate10And20 (BiFunction<Integer,Integer,Integer> operate) {
		return operate.apply(10, 20);
	}
   	
    public static void main(String args[]) {
       // invoke the function which requires an function as argument.
	System.out.println(PassFunctionAsArgsMain.operate10And20(PassFunctionAsArgsMain::add));
	System.out.println(PassFunctionAsArgsMain.operate10And20(PassFunctionAsArgsMain::diff));
	
	// since the function can take anothe function as argument, we can define own function iterface using LAMBDA
	//passing the lambda as function arguments
	System.out.println(PassFunctionAsArgsMain.operate10And20((x,y)->x*10+y*10)); // the passed values 20 and 30 now will be used to compute - 300
    }
}
```

### Returning a function from another function
  - A basic functon returned from another function
```java
package com.test.functions;

public class ReturnFunctionDemo {

	public static void main(String ...strings) {
		//NoArgs function returns another function and declaration is below
		// the NoArgs functional interface is already defined.
		
		// The representation ()->() might be confusing but first () returns a function itself.
		NoArgsFunction<NoArgsFunction<String>> returnHelloFunc = ()->()->"Hello from function";
		NoArgsFunction<String> getHelloMessage = returnHelloFunc.apply(); // The function ()->"Hello... is returned to this variable
		
		System.out.println("Message from function returned from function: \n"+getHelloMessage.apply()); // the functions message is printed.
	}
}

```
