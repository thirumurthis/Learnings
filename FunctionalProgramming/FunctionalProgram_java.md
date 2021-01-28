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
       x = average(inputArray); // the average() is already defined part of the program
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
   
```java
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
    - in OOPs it is not possible for function to return another function, or passing function as argument.
 - with functional programming, it is possible to to do return function and pass function as arguments.
    - this provides capability
 
 #### Functional Interface:
   - Treat function like other type like String, Integer, etc. in java.

 - Define function as variable.
```java

public class Demo{
  protected static class MathOp{
     public static Integer triple(Integer x){
        return x*3;
        }
  }
public static void main(String ...arges){
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
		
		// The lambda representation ()->() might be confusing but first () returns a function itself.
		NoArgsFunction<NoArgsFunction<String>> returnHelloFunc = ()->()->"Hello from function";
		NoArgsFunction<String> getHelloMessage = returnHelloFunc.apply(); // The function ()->"Hello... is returned to this variable
		
		System.out.println("Message from function returned from function: \n"+getHelloMessage.apply()); // the functions message is printed.
	}
}

```
#### The function returning a function benefits to refactor code. 
 -Assume the below scenario, where the same operation sort of repeated multiple times, below can be refactored.

```java 
  public static Integer double(Integer x){ return x * 2;}
  public static Integer triple(Integer x){ return x * 3;}
  public static Integer quadraple(Integer x){ return x * 4;}
```

- refactored code
```java
package com.test.functions;

import java.util.function.Function;

public class ReturnFunctionDemo {

  //function is returned in this case
   public static Function<Integer,Integer> multiplier(Integer y) {
     return x -> x * y;
    }
 
   public static void main(String ...strings) {
    // the function returned and set in doubleit 
    // Creating function on the fly is main advantage of function returning function.
    Function<Integer, Integer> doubleit = ReturnFunctionDemo.multiplier(2);
    Function<Integer, Integer> tripleit = ReturnFunctionDemo.multiplier(3);
    Function<Integer, Integer> quadrapleit = ReturnFunctionDemo.multiplier(4);
		
    System.out.println(doubleit.apply(10));
    System.out.println(tripleit.apply(10));
    System.out.println(quadrapleit.apply(10));
   }
}
```

### Closures
  - Closure means when we define a function that returns another function, the function that we returned still has the internal scope of the function it returned it.
  - Example below
```java
package com.test.functions;
public class ClosureExample {

	public static void main(String args[]) {
		
		NoArgsFunction<NoArgsFunction<String>> returnHelloFunc = ()->{
			String name = "User"; // the name variable scope is accessible to returning function as well
			return () -> "Hello "+name;
		};
		NoArgsFunction<String> sayHello = returnHelloFunc.apply();
		
		System.out.println(sayHello.apply()); // the name cannot be accessed directly here in this area
	}
}
```
- The multplier function above which was refactred also applies `Closures`.

### High-order functions - This is the term when the function take another function as argument or return another function.
  - Example below, validating arguments say if we are developing a divide mathametical program, we need to handle divide-by-zero
  - It is always better to consider single function performing single operation. 
     - The function the divides should only divide, not to perform the divide-by-zero 
 
 - consider the below code wher single function performs validation and divide by zero on argument. 
 - but this can be improvised using `higher-order functions`. check the code following.
```java
package com.test.functions;

import java.util.function.BiFunction;

public class HigherOrderDemo {
	
	public static void main(String ...strings) {
		
	BiFunction<Float,Float,Float>	divideOperation  = (x,y) -> {
			if (y == 0f) {
				System.out.println("Error: divde-by-zero");
				return Float.MAX_VALUE;
			}
			return x/y;
		};
	 System.out.println(divideOperation.apply(10.0f, 0f));
	}
}
```
 - Using `higher-order functions` to validate argument validation, so each function has its own responsibility
```java
package com.test.functions;

import java.util.function.BiFunction;
import java.util.function.Function;

public class HigherOrderDemo {
	
	public static void main(String ...strings) {
        
	 //Create a BiFunction to perform the divide operation alone
	 BiFunction<Float,Float,Float> safeDivide = (x,y)->(x/y);
	 
	 //Create a Function that takes and returns bifunction where we perform the arg validation
	 Function<BiFunction<Float,Float,Float>,BiFunction<Float,Float,Float>> argValidation =
	 (safeDivideFunc)->
		 (x, y ) -> {
			 if(y == 0f) {
				 System.out.println("Error: divde-by-zero");
				return Float.MAX_VALUE;
			 }
            return safeDivideFunc.apply(x,y);
		 };
	
	 //Call the argvalidation function passing the divide function, 
 	 BiFunction<Float, Float, Float> saferToDivide = argValidation.apply(safeDivide);
 	 
 	 System.out.println(saferToDivide.apply(10f, 0f));
 	 System.out.println(saferToDivide.apply(10f, 2f));
	}
}
```

### java Functional interface support on collection

 - `.map` function
 - Most case we have list of data, and we need to convert to some other value
   - example inch to centimeter, meter to feet, ojbect and create a list etc.
   - convention way is to for loop and assign it.
 - In order to work with functions for array, list it needs to be converted to `streams`. which can be done using `listvar.stream()`
 - To dobule the number, `listvar.stream().map(doubleit)..`, doubleit is a function iterface.
 
 - The map, reduce, filter doesn't mutate the actual data, only a copy of the data is modified.
 
```java
package com.test.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionStreamDemo {
	
	public static void main(String[] args) {
		Integer inputArray[] = {1,2,3,4,5,6,7,8,9,10,11,12,13};
		
		List<Integer> input = new ArrayList<>(Arrays.asList(inputArray));
		Function<Integer,Integer> doubleit = x->x*2;
		List<Integer> doubleValues = input.stream().map(doubleit).collect(Collectors.toList());
		doubleValues.forEach(System.out::println);
	}
}
```

### `.filter` function
 - To filter the data based on condition, syntax similar to map.
 - example, to filter the even number from the list, salary more than 1000, etc.
 
 - the difference between the map and filter is, type of function we passed to it.
 - for map we pass a function that returns a value for each element. 
 - for filter the a function that returns a boolean is used.
     - NOTE: when the pass a function object to filter, it is not completely true, it is `Predicate<T>`, 
 
 __`Predicate<T>`__: is a function that returns a boolean. This works like anyother functional interface, this takes one generic data type fpr an argument. Return type is booelan. Simply for filter we use the Predicate object, rather than a simple function.
 
 - To find the list of even numbers from the above sample program list
 ```java
  
  //better readability
  Predicate<Integer> evenCheck = e->e%2==0;
  List<Integer> evenValues = input.stream().filter(evenCheck).collect(Collectors.toList());
  
  //using lambda function directly within fitler
   List<Integer> evenValues = input.stream().filter(x -> x%2 == 0).collect(Collectors.toList());

 ```
 - Example string length greater than 4
 ```java
   String[] inputStr = {"hello","how","are","you","This","checks","length"};
   List<String> inputStrList = new ArrayList<>(Arrays.asList(inputStr));
   
   //Directly using Lambda expressiong
   List<String> outputStrList = inputStrList.stream().filter(e-> e!=null && e.length()>4).collect(Collectors.toList());
   System.out.println(outputStrList);
 ```
 
 ** Challenge use an `high-order function` to create the predicate. **
  - Determine the string length in more flexible way using the integer passed as an argument.
  
 ```java
   // create a function uses the Integer as input and return predicate.
   // this uses the closure
    Function<Integer,Predicate<String>> passStringAndLimit = (minLength)->
          { return (str)->str.length() > minLength;};	
	    
    Predicate<String> lengthTest = passStringAndLimit.apply(4);
	    
    List<String> outputStrHigherOrder = inputStrList.stream().filter(lengthTest).collect(Collectors.toList());
    System.out.println(outputStrHigherOrder);
 ```
-------------
 #### Reduce:
   - `reduce` functional interface takes an BinaryOperator as input
   ```
     reduce(startValue, function); //start value can be a first item to add
     
     reduce(binaryOperator); // reduce ((x,y)->x+y); -> this method will return an Optional ojbect as output
                             // this case the first element value will be used as start value.
   ```
   - reduce is the last function, no need to use the collect() operation.
   
   - The first argument is the accumulator, it is usually set to 0 or 0.0 or 0f etc according to datatype 
   - If first argument is not specified, the first element in the stream will be used
```
package com.test.functions;

import java.util.Arrays;
import java.util.List;

public class ReduceFunction {
	public static void main(String[] args) {
       
		Integer a[] = {10,20,30,40,50,60,70,80};
		List<Integer> input = Arrays.asList(a);
		
		Integer sum = input.stream().reduce(0,(acc,item)->acc+item);
		System.out.println(sum);
		
		BinaryOperator<Integer> add= (x,y) -> x+y;
		//passing the function as input
		Optional<Integer> result = input.stream().reduce(add);
		System.out.println(result.get());
	}
}
```
 
 ** Note: `BinaryOperator` is a BiFunction where the argument and return Type all are same, like `BiFunction<Integer,Integer,Integer>`. **

##### `Collect` function usage
  - with `reduce` the return type should be the same as the input type. for example, Integer should return Integer, etc.
  - with `collect` the return type can be anything like string, list, map, etc.
```
  inputList.stream().collect(Collector<T,A,R>);
  // Note: Collector<T,A,R> - IS NOT A FUNCTIONAL INTERFACE
  
  Usage:
   intputList.stream().collect(Collector.toList()); // used mostly
```
 - Custom collectors can  also be created, check documentation for this.
 
Example usage:
```java
package com.test.functions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectExample {
	public static void main(String[] args) {
		String[] input = {"one","two","three","four","five","six","seven","eight","nine"};
		List<String> inputList = Arrays.asList(input);
		
		List<String> output =inputList.stream()
			             .filter(a -> a.length()>2)
			             .collect(Collectors.toList());
		System.out.println(output);
	}
}
```

 - using `toSet()`, `counting()`, `joining()` , `groupingBy()` and `partitionBy()`
 ```java
  # the input is same as the above example code
  		Set<String> outputSet =inputList.stream()
	             .filter(a -> a.length()>2)
	             .collect(Collectors.toSet()); // order not maintained, unique items
		System.out.println(outputSet); //output: [nine, six, four, one, seven, two, three, five, eight]

                // returns a Long value
		long count =inputList.stream()
	             .filter(a -> a.length()>2)
	             .collect(Collectors.counting());
		System.out.println(count); //output: 9
		
		// returns a Map
		Map<Integer,List<String>> groupStr = inputList.stream()
                                       .collect(Collectors.groupingBy(element-> element.length()));
		System.out.println(groupStr);  //output: {3=[one, two, six], 4=[four, five, nine], 5=[three, seven, eight]}
		
		//partition by groups based on condition, either true or false as key
		Map<Boolean,List<String>> partitionBy = inputList.stream()
				                                .collect(Collectors.partitioningBy(element->element.length()>4));					
		System.out.println(partitionBy); //output: {false=[one, two, four, five, six, nine], true=[three, seven, eight]}
		
				String joinStr = inputList.stream().filter(element->element.length()>3)
				         .collect(Collectors.joining(","));
		System.out.println(joinStr); //output: three,four,five,seven,eight,nine
 ```

#### Parallel streams
  - This are similar to stream but all the task are performed in parallel by the streams
    - process data in parallel, java takes care of creating the threads.
    - increase performance.
  - we can use all the function like map(), filter(), reduce() in parallel stream as well
  - `stream()` is serial processing, using syso it can be observed.
  - if we are processing using parallel streams even though the thread performs parallely the original order of input is preserved.
  ```
   inputList.parallelStream().collect(Collectors.toList());
  ```
  
  Example:
  ```java
  package com.test.functions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParallelStreamDemo {

	public static void main(String[] args) {
		String[] input = {"one","two","three","four","five","six","seven","eight","nine"};
		List<String> inputList = Arrays.asList(input);
		
		List<String> upperStrList = inputList.parallelStream()
				                    .map(element -> element.toUpperCase())
				                    .collect(Collectors.toList());
		System.out.println(upperStrList);
				
	}
}
```

##### Advanced concepts:
  - Partial application
  - Recursion
  - Composition

 - Partial Application:
    - we take a function with some number of arguments 
 ```
   Integer add(Integer a, Integer b, Integer c){
    return a+b+c
   }
   
   //in this case it takes two arguments
   Integer add2(Integer b,Integer c){
     return 2+b+c;
   }
 ```
 - Function with fixed arguments as above, when called should pass the value to argument at same time and place or order.
 - In `Partial application`, we can pass the arugments to function in different places in the code and get the result once the function gets all of the arugumets.
 - The `Partial application` helps in configure more generic function to specific function.
    - In case if one of the arugments is same in a function, this is potential candidate to use as partial application.
    
```java
package com.test.functions;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PartialFunctionsDemo {

	public static void main(String[] args) {
		
		TriFunctions<Integer,Integer,Integer,Integer> sum = (a,b,c)->a+b+c;
		
		//partial application
		Function<Integer,BiFunction<Integer,Integer,Integer>> sum2argPartial = 
				(a)->(b,c)->sum.apply(a,b,c);
		
		//2 arg partial
		BiFunction<Integer, Integer, Integer> sum10 = sum2argPartial.apply(10);
		
		System.out.println(sum10.apply(100,120));
		
		// converting the same to pass only one argument within
		
		BiFunction<Integer,Integer, Function<Integer,Integer>> sumPartial =
				(a,b)->(c)->sum.apply(a, b, c);
		
		Function<Integer,Integer> add15And10 = sumPartial.apply(15,10);
		
		System.out.println(add15And10.apply(20));
	}
}

```
More of example to use function over function:

```java
		// Function can return function intern function
		
		Function<Integer, Function<Integer, Function<Integer,Integer>>> add =
				(a)->(b)->(c)-> sum.apply(a, b, c);
		Function<Integer,Function<Integer,Integer>> add20 = add.apply(20);
		Function<Integer,Integer> add10 = add20.apply(10);
		
		System.out.println(add10.apply(15));
```
 ##### `Currying` 
    - passing argument like below is called `currying`, notice mulitple arguments passed in the function
    ```
    	Function<Integer, Function<Integer, Function<Integer,Integer>>> add = (a)->(b)->(c)-> sum.apply(a, b, c);
    ```
    
 #### Recursion
    - function calling function, possible to lead to infinity loop
    - This is similar to the way the OOP's recursion
 
 #### Composition
  - take a series of smaller modular function to create one complex.
  - Example:  function to perform doubing, and function to perform substraction nd combining them to single complex function
    -  > f(x) = 2x and f(x) = x-1  ==> f(x) = 2x-1;
  - compose in java somehow works in reverse order
    - that is, the function passed to compose will be executed first and the outer function latter.
  
  - Composition can be applied to any type of function other than mathematical function.
  - In theory, any set of function can be composed as `long as each function returns the SAME type of DATA.`
     - in below case we see the Integer is returned from one function to another.
  
```java
package com.test.functions;

import java.util.function.Function;

public class ComposeDemo {
	public static void main(String[] args) {
		
		Function<Integer,Integer> doubleIt = x->x*2;
		Function<Integer,Integer> subtractOne = x->x-1;
		
		//composing two function 
		// the doubleIt function performed first and then subtractOne is executed.
		// so for 2x-1 will be below. if we use doubleit and subract one will yeild different result.
	//composing two function 
		// the doubleIt function performed first and then subtractOne is executed.
		Function<Integer,Integer> doubleSubtract = subtractOne.compose(doubleIt);
		Function<Integer,Integer> doubleSubtract1 = doubleIt.compose(subtractOne);
		System.out.println(doubleSubtract.apply(10)); //output: 19  (2*10)-1
		System.out.println(doubleSubtract1.apply(10)); // outputs : 18  (2*(10-1))
	}
}
```
   - Example: Get list of employees reversed and in Upper case.
   - using mulitple compose function __`andThen()`__.

```java
package com.test.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComposeDemo2 {
	public static void main(String[] args) {
		
		Employee employe1 = new Employee("Tony","20");
		Employee employe2 = new Employee("Cody","20");
		Employee employe3 = new Employee("Tim","20");
		Employee employe4 = new Employee("Rosy","20");
		Employee employe5 = new Employee("Clint","20");
		Employee employe6 = new Employee("Ava","20");
		List<Employee> employees = new ArrayList<>();
		employees.add(employe1);
		employees.add(employe2);
		employees.add(employe3);
		employees.add(employe4);
		employees.add(employe5);
		employees.add(employe6);
		
		Function<Employee,String> getName = str -> str.getName();
		Function<String,String> upperCase = str -> str.toUpperCase();
		Function<String,String> strReverse = str -> new StringBuilder(str).reverse().toString();
		// andThen -> used for compose
		Function<Employee,String> getNameProcessed = getName.andThen(strReverse).andThen(upperCase);
		String output = employees.stream().map(getNameProcessed).collect(Collectors.joining(", "));
		System.out.println(output);// output: YNOT, YDOC, MIT, YSOR, TNILC, AVA
	}
}
//employee object
class Employee{
	private String name;
        private String age;
	public Employee(String name ,String age) { this.name=name; this.age=age;}
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public String getAge() {return age;}
	public void setAge(String age) {this.age = age;}
}

```
    

